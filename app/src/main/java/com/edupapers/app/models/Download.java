package com.edupapers.app.models;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class Download implements Serializable {
    private String id;
    private String fileName;
    private String filePath;
    private String fileSize;
    private Date downloadDate;
    private String mimeType;
    private Paper paper;
    private boolean isCompleted;
    private int progress;

    public Download() {
        // Empty constructor
        this.downloadDate = new Date();
        this.isCompleted = false;
        this.progress = 0;
    }

    public Download(Paper paper, String filePath) {
        this.id = paper.getId();
        this.fileName = paper.getCourseCode() + "_" + paper.getYear() + "_" + paper.getSemester() + ".pdf";
        this.filePath = filePath;
        this.paper = paper;
        this.downloadDate = new Date();
        this.isCompleted = false;
        this.progress = 0;
        this.mimeType = "application/pdf";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public Date getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(Date downloadDate) {
        this.downloadDate = downloadDate;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper paper) {
        this.paper = paper;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    // Utility methods
    public File getFile() {
        return new File(filePath);
    }

    public boolean exists() {
        return getFile().exists();
    }

    public boolean delete() {
        File file = getFile();
        return file.exists() && file.delete();
    }

    public String getFormattedSize() {
        if (fileSize == null) return "Unknown size";
        return fileSize;
    }

    public String getFormattedDate() {
        if (downloadDate == null) return "Unknown date";
        return android.text.format.DateFormat.getDateFormat(null).format(downloadDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Download download = (Download) o;
        return id.equals(download.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Download{" +
                "fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", downloadDate=" + downloadDate +
                ", isCompleted=" + isCompleted +
                ", progress=" + progress +
                '}';
    }
}
