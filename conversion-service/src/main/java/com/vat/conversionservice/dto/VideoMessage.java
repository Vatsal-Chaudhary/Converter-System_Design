package com.vat.conversionservice.dto;

public class VideoMessage {
    private String fileId;

    private String userId;

    @Override
    public String toString() {
        return "VideoMessage{" +
                "fileId='" + fileId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public VideoMessage() {}

    public VideoMessage(String fileId, String userId) {
        this.fileId = fileId;
        this.userId = userId;
    }
}
