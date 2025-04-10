package com.edupapers.app.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;

import com.edupapers.app.models.Download;
import com.edupapers.app.models.Paper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadManager {
    private static DownloadManager instance;
    private final Context context;
    private final OkHttpClient client;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Map<String, Download> activeDownloads;
    private final List<DownloadCallback> callbacks;
    private final String downloadDirectory;

    public interface DownloadCallback {
        void onDownloadStarted(Download download);
        void onDownloadProgress(Download download, int progress);
        void onDownloadCompleted(Download download);
        void onDownloadFailed(Download download, String error);
        void onDownloadPaused(Download download);
        void onDownloadResumed(Download download);
    }

    private DownloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient();
        this.executorService = Executors.newFixedThreadPool(3); // Allow 3 concurrent downloads
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.activeDownloads = new HashMap<>();
        this.callbacks = new ArrayList<>();
        
        // Create download directory
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "EduPapers");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.downloadDirectory = dir.getAbsolutePath();
    }

    public static synchronized DownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadManager(context);
        }
        return instance;
    }

    public void addCallback(DownloadCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void removeCallback(DownloadCallback callback) {
        callbacks.remove(callback);
    }

    public void startDownload(Paper paper) {
        if (activeDownloads.containsKey(paper.getId())) {
            return; // Download already in progress
        }

        String fileName = paper.getCourseCode() + "_" + paper.getYear() + "_" + paper.getSemester() + ".pdf";
        String filePath = new File(downloadDirectory, fileName).getAbsolutePath();
        
        Download download = new Download(paper, filePath);
        activeDownloads.put(paper.getId(), download);

        notifyDownloadStarted(download);

        executorService.execute(() -> {
            try {
                downloadFile(download);
            } catch (IOException e) {
                notifyDownloadFailed(download, e.getMessage());
            }
        });
    }

    private void downloadFile(Download download) throws IOException {
        Request request = new Request.Builder()
                .url(download.getPaper().getDownloadUrl())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }

            long contentLength = body.contentLength();
            download.setFileSize(formatFileSize(contentLength));

            File outputFile = new File(download.getFilePath());
            try (InputStream input = body.byteStream();
                 FileOutputStream output = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[4096];
                long downloaded = 0;
                int read;

                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    downloaded += read;
                    
                    // Calculate progress
                    int progress = (int) ((downloaded * 100) / contentLength);
                    notifyDownloadProgress(download, progress);
                }

                download.setCompleted(true);
                notifyDownloadCompleted(download);
            }
        } finally {
            activeDownloads.remove(download.getPaper().getId());
        }
    }

    public void pauseDownload(String downloadId) {
        Download download = activeDownloads.get(downloadId);
        if (download != null) {
            // Logic to pause the download
            download.setPaused(true);
            notifyDownloadPaused(download);
        }
    }

    public void resumeDownload(String downloadId) {
        Download download = activeDownloads.get(downloadId);
        if (download != null && download.isPaused()) {
            // Logic to resume the download
            download.setPaused(false);
            startDownload(download.getPaper()); // Restart the download
            notifyDownloadResumed(download);
        }
    }

    public void cancelDownload(String downloadId) {
        Download download = activeDownloads.remove(downloadId);
        if (download != null) {
            // Implement cancel logic here
            notifyDownloadFailed(download, "Download cancelled");
        }
    }

    public boolean isDownloading(String paperId) {
        return activeDownloads.containsKey(paperId);
    }

    public List<Download> getActiveDownloads() {
        return new ArrayList<>(activeDownloads.values());
    }

    private void notifyDownloadStarted(Download download) {
        mainHandler.post(() -> {
            for (DownloadCallback callback : callbacks) {
                callback.onDownloadStarted(download);
            }
        });
    }

    private void notifyDownloadProgress(Download download, int progress) {
        mainHandler.post(() -> {
            download.setProgress(progress);
            for (DownloadCallback callback : callbacks) {
                callback.onDownloadProgress(download, progress);
            }
        });
    }

    private void notifyDownloadCompleted(Download download) {
        mainHandler.post(() -> {
            for (DownloadCallback callback : callbacks) {
                callback.onDownloadCompleted(download);
            }
        });
    }

    private void notifyDownloadFailed(Download download, String error) {
        mainHandler.post(() -> {
            for (DownloadCallback callback : callbacks) {
                callback.onDownloadFailed(download, error);
            }
        });
    }

    private void notifyDownloadPaused(Download download) {
        mainHandler.post(() -> {
            for (DownloadCallback callback : callbacks) {
                callback.onDownloadPaused(download);
            }
        });
    }

    private void notifyDownloadResumed(Download download) {
        mainHandler.post(() -> {
            for (DownloadCallback callback : callbacks) {
                callback.onDownloadResumed(download);
            }
        });
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type != null ? type : "application/octet-stream";
    }

    public void cleanup() {
        executorService.shutdown();
        callbacks.clear();
        activeDownloads.clear();
    }
}
