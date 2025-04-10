package com.edupapers.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edupapers.app.R;
import com.edupapers.app.adapters.PapersAdapter;
import com.edupapers.app.models.Paper;
import com.edupapers.app.utils.DownloadManager;
import com.edupapers.app.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MidtermActivity extends BaseActivity implements 
        PapersAdapter.OnPaperClickListener,
        NetworkUtils.NetworkCallback {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private View emptyView;
    private ProgressBar progressBar;
    private PapersAdapter adapter;
    private DownloadManager downloadManager;
    private NetworkUtils networkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midterm);

        // Set toolbar title
        getSupportActionBar().setTitle(R.string.nav_midterm);

        initializeViews();
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        setupNetworkCallback();

        // Load papers
        loadPapers();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        searchView = findViewById(R.id.search_view);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_bar);

        downloadManager = DownloadManager.getInstance(this);
        networkUtils = NetworkUtils.getInstance(this);
    }

    private void setupRecyclerView() {
        adapter = new PapersAdapter(this, downloadManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                updateEmptyView();
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPapers);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void setupNetworkCallback() {
        networkUtils.addCallback(this);
    }

    private void loadPapers() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Replace with actual API call
        // For now, using demo data
        List<Paper> papers = getDemoPapers();
        adapter.setPapers(papers);
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        updateEmptyView();
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

    private List<Paper> getDemoPapers() {
        List<Paper> papers = new ArrayList<>();
        papers.add(new Paper("1", "CS101", "Introduction to Programming", "2023", "Spring", 
                "https://example.com/papers/cs101.pdf", "2.5 MB"));
        papers.add(new Paper("2", "CS201", "Data Structures", "2023", "Spring", 
                "https://example.com/papers/cs201.pdf", "3.1 MB"));
        papers.add(new Paper("3", "CS301", "Database Systems", "2023", "Spring", 
                "https://example.com/papers/cs301.pdf", "2.8 MB"));
        return papers;
    }

    @Override
    public void onDownloadClick(Paper paper, int position) {
        if (!networkUtils.isConnected()) {
            showError(getString(R.string.msg_no_internet));
            return;
        }

        if (paper.isDownloaded()) {
            openPaper(paper);
        } else {
            downloadManager.startDownload(paper);
            showMessage(getString(R.string.msg_download_started));
        }
    }

    @Override
    public void onPaperClick(Paper paper, int position) {
        if (paper.isDownloaded()) {
            openPaper(paper);
        }
    }

    private void openPaper(Paper paper) {
        File file = new File(getExternalFilesDir(null), 
                paper.getCourseCode() + "_" + paper.getYear() + "_" + paper.getSemester() + ".pdf");
        
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
    public void onNetworkAvailable() {
        // Enable download functionality
    }

    @Override
    public void onNetworkLost() {
        showError(getString(R.string.msg_no_internet));
    }

    @Override
    protected void onDestroy() {
        networkUtils.removeCallback(this);
        super.onDestroy();
    }
}
