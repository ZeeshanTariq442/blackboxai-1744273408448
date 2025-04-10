package com.edupapers.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edupapers.app.R;
import com.edupapers.app.models.Download;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadViewHolder> {

    private List<Download> downloads;
    private final OnDownloadClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnDownloadClickListener {
        void onOpenClick(Download download);
        void onDeleteClick(Download download);
        void onInfoClick(Download download);
    }

    public DownloadsAdapter(OnDownloadClickListener listener) {
        this.downloads = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        Download download = downloads.get(position);
        holder.bind(download);
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public void setDownloads(List<Download> downloads) {
        this.downloads = downloads;
        notifyDataSetChanged();
    }

    public void removeDownload(Download download) {
        int position = downloads.indexOf(download);
        if (position != -1) {
            downloads.remove(position);
            notifyItemRemoved(position);
        }
    }

    class DownloadViewHolder extends RecyclerView.ViewHolder {
        private final TextView filenameText;
        private final TextView infoText;
        private final MaterialButton openButton;
        private final MaterialButton deleteButton;
        private final ImageButton moreButton;

        DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            filenameText = itemView.findViewById(R.id.text_filename);
            infoText = itemView.findViewById(R.id.text_info);
            openButton = itemView.findViewById(R.id.btn_open);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            moreButton = itemView.findViewById(R.id.btn_more);
        }

        void bind(Download download) {
            filenameText.setText(download.getFileName());
            
            String info = String.format("%s • Downloaded on %s",
                    download.getFileSize(),
                    dateFormat.format(download.getDownloadDate()));
            infoText.setText(info);

            openButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenClick(download);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(download);
                }
            });

            moreButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInfoClick(download);
                }
            });

            // Make the entire item clickable to open the file
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenClick(download);
                }
            });
        }
    }
}
