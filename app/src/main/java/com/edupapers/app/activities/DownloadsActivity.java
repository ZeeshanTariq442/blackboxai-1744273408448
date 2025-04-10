package com.edupapers.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edupapers.app.R;
import com.edupapers.app.adapters.DownloadsAdapter;
import com.edupapers.app.models.Download;
import com.edupapers.app.utils.DownloadManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadsActivity extends BaseActivity implements DownloadsAdapter.OnDownloadClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private DownloadsAdapter adapter;
    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        // Set toolbar title
        getSupportActionBar().setTitle(R.string.nav_downloads);

        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadDownloads();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        emptyView = findViewById(R.id.empty_view);
        
        TextView emptyTitle = emptyView.findViewById(R.id.empty_title);
        TextView emptyDescription = emptyView.findViewById(R.id.empty_description);
        
        emptyTitle.setText(R.string.no_downloads_title);
        emptyDescription.setText(R.string.no_downloads_description);

        downloadManager = DownloadManager.getInstance(this);
    }

    private void setupRecyclerView() {
        adapter = new DownloadsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadDownloads);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void loadDownloads() {
        File downloadsDir = new File(getExternalFilesDir(null), "EduPapers");
        List<Download> downloads = new ArrayList<>();

        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            File[] files = downloadsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".pdf")) {
                        Download download = createDownloadFromFile(file);
                        if (download != null) {
                            downloads.add(download);
                        }
                    }
                }
            }
        }

        adapter.setDownloads(downloads);
        swipeRefreshLayout.setRefreshing(false);
        updateEmptyView();
    }

    private Download createDownloadFromFile(File file) {
        String fileName = file.getName();
        // Remove .pdf extension
        String name = fileName.substring(0, fileName.length() - 4);
        String[] parts = name.split("_");
        
        if (parts.length >= 3) {
            String courseCode = parts[0];
            String year = parts[1];
            String semester = parts[2];
            
            Download download = new Download();
            download.setId(name);
            download.setFileName(fileName);
            download.setFilePath(file.getAbsolutePath());
            download.setFileSize(formatFileSize(file.length()));
            download.setDownloadDate(new Date(file.lastModified()));
            download.setCompleted(true);
            
            return download;
        }
        return null;
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOpenClick(Download download) {
        File file = new File(download.getFilePath());
        if (!file.exists()) {
            showError(getString(R.string.msg_file_not_found));
            return;
        }

        Uri uri = FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (Exception e) {
            showError(getString(R.string.msg_no_pdf_viewer));
        }
    }

    @Override
    public void onDeleteClick(Download download) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                    File file = new File(download.getFilePath());
                    if (file.delete()) {
                        adapter.removeDownload(download);
                        updateEmptyView();
                        showMessage(getString(R.string.msg_file_deleted));
                    } else {
                        showError(getString(R.string.msg_delete_failed));
                    }
                })
                .setNegativeButton(R.string.dialog_no, null)
                .show();
    }

    @Override
    public void onInfoClick(Download download) {
        String info = String.format(
                "File Name: %s\nSize: %s\nDownloaded: %s",
                download.getFileName(),
                download.getFileSize(),
                android.text.format.DateFormat.getDateFormat(this).format(download.getDownloadDate())
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_file_info)
                .setMessage(info)
                .setPositiveButton(R.string.dialog_ok, null)
                .show();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
