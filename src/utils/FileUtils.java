package utils;

import common.FileInfo;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    
    /**
     * Format kích thước file thành dạng dễ đọc (B, KB, MB, GB)
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        DecimalFormat df = new DecimalFormat("#,##0.#");
        return df.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    /**
     * Tìm kiếm file theo tên (không phân biệt hoa thường)
     */
    public static List<FileInfo> searchFiles(File[] files, String searchQuery) {
        List<FileInfo> result = new ArrayList<>();
        
        if (files == null || searchQuery == null || searchQuery.trim().isEmpty()) {
            return result;
        }
        
        String query = searchQuery.toLowerCase().trim();
        
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().contains(query)) {
                FileInfo fileInfo = new FileInfo(
                    file.getName(),
                    file.length(),
                    formatFileSize(file.length()),
                    file.lastModified()
                );
                result.add(fileInfo);
            }
        }
        
        return result;
    }
    
    /**
     * Lấy extension của file
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
    
    /**
     * Kiểm tra file có phải là ảnh không
     */
    public static boolean isImageFile(String fileName) {
        String ext = getFileExtension(fileName);
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || 
               ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }
    
    /**
     * Kiểm tra file có phải là document không
     */
    public static boolean isDocumentFile(String fileName) {
        String ext = getFileExtension(fileName);
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || 
               ext.equals("txt") || ext.equals("rtf") || ext.equals("odt");
    }
    
    
    public static String getUniqueFileName(String directory, String originalName) {
        File file = new File(directory, originalName);
        if (!file.exists()) {
            return originalName;
        }
        
        String nameWithoutExt = originalName;
        String extension = "";
        
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex != -1) {
            nameWithoutExt = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }
        
        int counter = 1;
        String newName;
        do {
            newName = nameWithoutExt + "_" + counter + extension;
            file = new File(directory, newName);
            counter++;
        } while (file.exists());
        
        return newName;
    }
}