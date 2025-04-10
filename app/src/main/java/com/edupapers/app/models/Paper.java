package com.edupapers.app.models;

import java.io.Serializable;
import java.util.Date;

public class Paper implements Serializable {
    private String id;
    private String courseCode;
    private String title;
    private String year;
    private String semester;
    private String downloadUrl;
    private String fileSize;
    private boolean isDownloaded;
    private Date uploadDate;
    private String fileType;
    private long downloadCount;

    public Paper() {
        // Empty constructor needed for Firebase
    }

    public Paper(String id, String courseCode, String title, String year, 
                String semester, String downloadUrl, String fileSize) {
        this.id = id;
        this.courseCode = courseCode;
        this.title = title;
        this.year = year;
        this.semester = semester;
        this.downloadUrl = downloadUrl;
        this.fileSize = fileSize;
        this.isDownloaded = false;
        this.uploadDate = new Date();
        this.fileType = "pdf";
        this.downloadCount = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public String getFullTitle() {
        return courseCode + " - " + title + " (" + year + " " + semester + ")";
    }

    @Override
    public String toString() {
        return "Paper{" +
                "id='" + id + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", semester='" + semester + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", isDownloaded=" + isDownloaded +
                ", downloadCount=" + downloadCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paper paper = (Paper) o;
        return id.equals(paper.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
