package common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileInfo {
    private String name;
    private long size;
    private String formattedSize;
    private long lastModified;
    private String formattedDate;
    
    public FileInfo(String name, long size, String formattedSize, long lastModified) {
        this.name = name;
        this.size = size;
        this.formattedSize = formattedSize;
        this.lastModified = lastModified;
        this.formattedDate = formatDate(lastModified);
    }
    
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
    
    // Getters
    public String getName() { return name; }
    public long getSize() { return size; }
    public String getFormattedSize() { return formattedSize; }
    public long getLastModified() { return lastModified; }
    public String getFormattedDate() { return formattedDate; }
    
    @Override
    public String toString() {
        return name + " (" + formattedSize + ") - " + formattedDate;
    }
}