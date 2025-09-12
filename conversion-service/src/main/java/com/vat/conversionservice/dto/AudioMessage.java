package com.vat.conversionservice.dto;

public class AudioMessage {
    private String fileId;
    private String originalVideoId;
    private String userId;
    private String filename;

    public AudioMessage() {}

    public AudioMessage(String fileId, String originalVideoId, String userId, String filename) {
        this.fileId = fileId;
        this.originalVideoId = originalVideoId;
        this.userId = userId;
        this.filename = filename;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getOriginalVideoId() { return originalVideoId; }
    public void setOriginalVideoId(String originalVideoId) { this.originalVideoId = originalVideoId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}
