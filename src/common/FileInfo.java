package common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String fileName;
    private long fileSize;
    private byte[] fileData;
    private String senderId;
    private String receiverId;
    private LocalDateTime timestamp;
    private String fileHash; // For integrity check
    
    public FileInfo(String fileName, long fileSize, byte[] fileData, String senderId, String receiverId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = LocalDateTime.now();
        this.fileHash = generateHash(fileData);
    }
    
    private String generateHash(byte[] data) {
        // Simple hash for demonstration - in production use SHA-256
        if (data == null) return "";
        int hash = 0;
        for (byte b : data) {
            hash = hash * 31 + (b & 0xff);
        }
        return String.valueOf(Math.abs(hash));
    }
    
    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    
    @Override
    public String toString() {
        return String.format("FileInfo[%s, %d bytes, %s->%s]", 
            fileName, fileSize, senderId, receiverId);
    }
}