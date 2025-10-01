package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransferHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum TransferType {
        SENT, RECEIVED
    }
    
    public enum TransferStatus {
        SUCCESS, FAILED, CANCELLED
    }
    
    private String fileName;
    private long fileSize;
    private String partnerId;
    private TransferType type;
    private TransferStatus status;
    private LocalDateTime timestamp;
    private long transferTime; // in milliseconds
    private String errorMessage;
    
    public TransferHistory(String fileName, long fileSize, String partnerId, 
                          TransferType type, TransferStatus status) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.partnerId = partnerId;
        this.type = type;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
    
    public TransferHistory(String fileName, long fileSize, String partnerId, 
                          TransferType type, TransferStatus status, long transferTime) {
        this(fileName, fileSize, partnerId, type, status);
        this.transferTime = transferTime;
    }
    
    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    
    public TransferType getType() { return type; }
    public void setType(TransferType type) { this.type = type; }
    
    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public long getTransferTime() { return transferTime; }
    public void setTransferTime(long transferTime) { this.transferTime = transferTime; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
    
    public String getFormattedFileSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }
    
    public String getFormattedTransferTime() {
        if (transferTime < 1000) return transferTime + " ms";
        if (transferTime < 60000) return String.format("%.1f s", transferTime / 1000.0);
        return String.format("%.1f min", transferTime / 60000.0);
    }
    
    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s", 
            getFormattedTimestamp(), 
            type.toString().toLowerCase(),
            fileName,
            getFormattedFileSize(),
            partnerId,
            status.toString().toLowerCase());
    }
}