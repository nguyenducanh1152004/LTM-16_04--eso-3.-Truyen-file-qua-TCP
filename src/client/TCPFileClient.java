package client;

import common.FileInfo;
import utils.FileUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPFileClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String CLIENT_FILES_DIR = "client_files";
    
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private boolean isConnected = false;
    
    public TCPFileClient() {
        File clientDir = new File(CLIENT_FILES_DIR);
        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
    }
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            isConnected = true;
            System.out.println("Đã kết nối đến server " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Không thể kết nối đến server: " + e.getMessage());
            return false;
        }
    }
    
    public void disconnect() {
        try {
            if (isConnected) {
                dataOut.writeUTF("DISCONNECT");
                socket.close();
                isConnected = false;
                System.out.println("Đã ngắt kết nối khỏi server");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi ngắt kết nối: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
    
    public String uploadFile(File file, UploadCallback callback) {
        if (!isConnected()) {
            return "Chưa kết nối đến server";
        }
        
        try {
            dataOut.writeUTF("UPLOAD");
            dataOut.writeUTF(file.getName());
            dataOut.writeLong(file.length());
            
            
            String response = dataIn.readUTF();
            
            if (response.equals("FILE_EXISTS")) {
                String message = dataIn.readUTF();
                
                boolean overwrite = callback.onFileExists(message);
                
                dataOut.writeUTF(overwrite ? "YES" : "NO");
                
                if (!overwrite) {
                    String cancelResponse = dataIn.readUTF();
                    return "Upload bị hủy";
                }
                
                response = dataIn.readUTF();
            }
            
            if (!response.equals("READY_TO_RECEIVE")) {
                return "Server từ chối nhận file: " + response;
            }
            
            // Bắt đầu upload file
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                
                byte[] buffer = new byte[8192];
                long totalSent = 0;
                long fileSize = file.length();
                int bytesRead;
                
                while ((bytesRead = bis.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;
                    
                    int progress = (int) ((totalSent * 100) / fileSize);
                    callback.onProgress(progress, totalSent, fileSize);
                }
                
                dataOut.flush();
            }
            
            // Đọc kết quả upload
            String result = dataIn.readUTF();
            if (result.equals("UPLOAD_SUCCESS")) {
                callback.onSuccess("Upload thành công!");
                return "Upload thành công file: " + file.getName();
            } else {
                callback.onError("Upload thất bại");
                return "Upload thất bại: " + result;
            }
            
        } catch (IOException e) {
            callback.onError("Lỗi kết nối: " + e.getMessage());
            return "Lỗi upload: " + e.getMessage();
        }
    }
    
    public List<FileInfo> searchFiles(String query) {
        List<FileInfo> results = new ArrayList<>();
        
        if (!isConnected()) {
            return results;
        }
        
        try {
            // Gửi command SEARCH
            dataOut.writeUTF("SEARCH");
            dataOut.writeUTF(query);
            
            int fileCount = dataIn.readInt();
            
            // Đọc thông tin từng file
            for (int i = 0; i < fileCount; i++) {
                String fileName = dataIn.readUTF();
                long fileSize = dataIn.readLong();
                String formattedSize = dataIn.readUTF();
                long lastModified = dataIn.readLong();
                
                FileInfo fileInfo = new FileInfo(fileName, fileSize, formattedSize, lastModified);
                results.add(fileInfo);
            }
            
        } catch (IOException e) {
            System.err.println("Lỗi tìm kiếm: " + e.getMessage());
        }
        
        return results;
    }
    
    public String downloadFile(String fileName, DownloadCallback callback) {
        if (!isConnected()) {
            return "Chưa kết nối đến server";
        }
        
        try {
            dataOut.writeUTF("DOWNLOAD");
            dataOut.writeUTF(fileName);
           
            String response = dataIn.readUTF();
            
            if (response.equals("FILE_NOT_FOUND")) {
                return "Không tìm thấy file trên server";
            }
            
            if (!response.equals("FILE_FOUND")) {
                return "Lỗi server: " + response;
            }
            
            // Đọc thông tin file
            long fileSize = dataIn.readLong();
            String formattedSize = dataIn.readUTF();
            
            // Callback hỏi user có muốn download không
            boolean confirmDownload = callback.onConfirmDownload(fileName, formattedSize, fileSize);
            
            if (!confirmDownload) {
                dataOut.writeUTF("CANCEL_DOWNLOAD");
                return "Download bị hủy";
            }
            
            // Xác nhận download
            dataOut.writeUTF("CONFIRM_DOWNLOAD");
            
            String serverResponse = dataIn.readUTF();
            if (serverResponse.equals("DOWNLOAD_CANCELLED")) {
                return "Server hủy download";
            }
            
            if (!serverResponse.equals("SENDING_FILE")) {
                return "Lỗi server: " + serverResponse;
            }
            
            String uniqueFileName = FileUtils.getUniqueFileName(CLIENT_FILES_DIR, fileName);
            File downloadFile = new File(CLIENT_FILES_DIR, uniqueFileName);
            
            // Bắt đầu download
            try (FileOutputStream fos = new FileOutputStream(downloadFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                
                byte[] buffer = new byte[8192];
                long totalReceived = 0;
                int bytesRead;
                
                while (totalReceived < fileSize && (bytesRead = dataIn.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalReceived += bytesRead;
                    

                    int progress = (int) ((totalReceived * 100) / fileSize);
                    callback.onProgress(progress, totalReceived, fileSize);
                }
                
                if (totalReceived == fileSize) {
                    callback.onSuccess("Download thành công!");
                    return "Download thành công: " + uniqueFileName;
                } else {
                    downloadFile.delete(); // Xóa file lỗi
                    callback.onError("Download không hoàn chỉnh");
                    return "Download thất bại - file không hoàn chỉnh";
                }
            }
            
        } catch (IOException e) {
            callback.onError("Lỗi kết nối: " + e.getMessage());
            return "Lỗi download: " + e.getMessage();
        }
    }
    
    public interface UploadCallback {
        boolean onFileExists(String message);
        void onProgress(int percent, long current, long total);
        void onSuccess(String message);
        void onError(String message);
    }
    
    public interface DownloadCallback {
        boolean onConfirmDownload(String fileName, String fileSize, long sizeBytes);
        void onProgress(int percent, long current, long total);
        void onSuccess(String message);
        void onError(String message);
    }
}