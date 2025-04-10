package com.edupapers.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edupapers.app.R;
import com.edupapers.app.models.Paper;
import com.edupapers.app.models.Download;
import com.edupapers.app.utils.DownloadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PapersAdapter extends RecyclerView.Adapter<PapersAdapter.PaperViewHolder> {

    private List<Paper> papers;
    private List<Paper> filteredPapers;
    private final OnPaperClickListener listener;
    private final DownloadManager downloadManager;

    public interface OnPaperClickListener {
        void onDownloadClick(Paper paper, int position);
        void onPaperClick(Paper paper, int position);
    }

    public PapersAdapter(OnPaperClickListener listener, DownloadManager downloadManager) {
        this.papers = new ArrayList<>();
        this.filteredPapers = new ArrayList<>();
        this.listener = listener;
        this.downloadManager = downloadManager;
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paper, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        Paper paper = filteredPapers.get(position);
        holder.bind(paper, position);
    }

    @Override
    public int getItemCount() {
        return filteredPapers.size();
    }

    public void setPapers(List<Paper> papers) {
        this.papers = papers;
        this.filteredPapers = new ArrayList<>(papers);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredPapers.clear();
        if (query.isEmpty()) {
            filteredPapers.addAll(papers);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (Paper paper : papers) {
                if (paper.getCourseCode().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    paper.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredPapers.add(paper);
                }
            }
        }
        notifyDataSetChanged();
    }

    class PaperViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, DownloadManager.DownloadCallback {

        private final TextView serialText;
        private final TextView courseCodeText;
        private final TextView titleText;
        private final TextView infoText;
        private final Button downloadButton;
        private final ProgressBar progressBar;
        private final TextView progressText;
        private Paper paper;

        PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            serialText = itemView.findViewById(R.id.text_serial);
            courseCodeText = itemView.findViewById(R.id.text_course_code);
            titleText = itemView.findViewById(R.id.text_title);
            infoText = itemView.findViewById(R.id.text_info);
            downloadButton = itemView.findViewById(R.id.btn_download);
            progressBar = itemView.findViewById(R.id.progress_download);
            progressText = itemView.findViewById(R.id.text_progress);

            itemView.setOnClickListener(this);
            downloadButton.setOnClickListener(v -> {
                if (listener != null && paper != null) {
                    listener.onDownloadClick(paper, getAdapterPosition());
                }
            });
        }

        void bind(Paper paper, int position) {
            this.paper = paper;
            
            serialText.setText(String.valueOf(position + 1));
            courseCodeText.setText(paper.getCourseCode());
            titleText.setText(paper.getTitle());
            infoText.setText(String.format("%s • %s • %s",
                    paper.getYear(),
                    paper.getSemester(),
                    paper.getFileSize()));

            updateDownloadState();

            if (downloadManager.isDownloading(paper.getId())) {
                downloadManager.addCallback(this);
            }
        }

        private void updateDownloadState() {
            if (paper.isDownloaded()) {
                downloadButton.setText(R.string.action_open);
                downloadButton.setIcon(itemView.getContext().getDrawable(R.drawable.ic_open));
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
            } else if (downloadManager.isDownloading(paper.getId())) {
                downloadButton.setText(R.string.action_downloading);
                downloadButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
            } else {
                downloadButton.setText(R.string.action_download);
                downloadButton.setIcon(itemView.getContext().getDrawable(R.drawable.ic_download));
                downloadButton.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (listener != null && paper != null) {
                listener.onPaperClick(paper, getAdapterPosition());
            }
        }

        @Override
        public void onDownloadStarted(Download download) {
            if (paper != null && paper.getId().equals(download.getPaper().getId())) {
                downloadButton.setText(R.string.action_downloading);
                downloadButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                progressText.setText("0%");
            }
        }

        @Override
        public void onDownloadProgress(Download download, int progress) {
            if (paper != null && paper.getId().equals(download.getPaper().getId())) {
                progressBar.setProgress(progress);
                progressText.setText(String.format("%d%%", progress));
            }
        }

        @Override
        public void onDownloadCompleted(Download download) {
            if (paper != null && paper.getId().equals(download.getPaper().getId())) {
                paper.setDownloaded(true);
                updateDownloadState();
                downloadManager.removeCallback(this);
            }
        }

        @Override
        public void onDownloadFailed(Download download, String error) {
            if (paper != null && paper.getId().equals(download.getPaper().getId())) {
                updateDownloadState();
                downloadManager.removeCallback(this);
            }
        }

        @Override
        public void onDownloadPaused(Download download) {
            if (paper != null && paper.getId().equals(download.getPaper().getId())) {
                downloadButton.setText(R.string.action_resume);
                downloadButton.setEnabled(true);
            }
        }

        @Override
        public void onDownloadResumed(Download download) {
            if (paper != null && paper.getId().equals(download.getPaper().getId())) {
                downloadButton.setText(R.string.action_downloading);
                downloadButton.setEnabled(false);
            }
        }
    }
}
