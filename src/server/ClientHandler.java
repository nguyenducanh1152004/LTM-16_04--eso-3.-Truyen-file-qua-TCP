package server;

import common.FileInfo;
import utils.FileUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String serverFilesDir;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    
    public ClientHandler(Socket clientSocket, String serverFilesDir) {
        this.clientSocket = clientSocket;
        this.serverFilesDir = serverFilesDir;
    }
    
    @Override
    public void run() {
        try {
            dataIn = new DataInputStream(clientSocket.getInputStream());
            dataOut = new DataOutputStream(clientSocket.getOutputStream());
            
            String command;
            while ((command = dataIn.readUTF()) != null) {
                switch (command) {
                    case "UPLOAD":
                        handleFileUpload();
                        break;
                    case "SEARCH":
                        handleFileSearch();
                        break;
                    case "DOWNLOAD":
                        handleFileDownload();
                        break;
                    case "DISCONNECT":
                        System.out.println("Client ngắt kết nối");
                        return;
                    default:
                        dataOut.writeUTF("UNKNOWN_COMMAND");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client đã ngắt kết nối: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
            }
        }
    }
    
    private void handleFileUpload() throws IOException {
        // Đọc thông tin file
        String fileName = dataIn.readUTF();
        long fileSize = dataIn.readLong();
        
        System.out.println("Nhận file: " + fileName + " (" + FileUtils.formatFileSize(fileSize) + ")");
        
        // Tạo file trên server
        File serverFile = new File(serverFilesDir, fileName);
        
        if (serverFile.exists()) {
            dataOut.writeUTF("FILE_EXISTS");
            dataOut.writeUTF("File đã tồn tại trên server. Có muốn ghi đè không?");
            
            String response = dataIn.readUTF();
            if (!response.equals("YES")) {
                dataOut.writeUTF("UPLOAD_CANCELLED");
                return;
            }
        }
        
        // Bắt đầu nhận file
        dataOut.writeUTF("READY_TO_RECEIVE");
        
        try (FileOutputStream fos = new FileOutputStream(serverFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            
            byte[] buffer = new byte[8192];
            long totalReceived = 0;
            int bytesRead;
            
            while (totalReceived < fileSize && (bytesRead = dataIn.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalReceived += bytesRead;
            }
            
            if (totalReceived == fileSize) {
                dataOut.writeUTF("UPLOAD_SUCCESS");
                System.out.println("Upload thành công: " + fileName);
            } else {
                dataOut.writeUTF("UPLOAD_ERROR");
                serverFile.delete(); 
            }
            
        } catch (IOException e) {
            dataOut.writeUTF("UPLOAD_ERROR");
            if (serverFile.exists()) {
                serverFile.delete();
            }
            throw e;
        }
    }
    
    private void handleFileSearch() throws IOException {
        String searchQuery = dataIn.readUTF().toLowerCase();
        System.out.println("Tìm kiếm file: " + searchQuery);
        
        File serverDir = new File(serverFilesDir);
        File[] files = serverDir.listFiles();
        
        if (files == null) {
            dataOut.writeInt(0); // Không có file nào
            return;
        }
        
        List<FileInfo> matchingFiles = FileUtils.searchFiles(files, searchQuery);
        
        // Gửi số lượng file tìm được
        dataOut.writeInt(matchingFiles.size());
        
        // Gửi thông tin từng file
        for (FileInfo fileInfo : matchingFiles) {
            dataOut.writeUTF(fileInfo.getName());
            dataOut.writeLong(fileInfo.getSize());
            dataOut.writeUTF(fileInfo.getFormattedSize());
            dataOut.writeLong(fileInfo.getLastModified());
        }
    }
    
    private void handleFileDownload() throws IOException {
        String fileName = dataIn.readUTF();
        File fileToDownload = new File(serverFilesDir, fileName);
        
        if (!fileToDownload.exists()) {
            dataOut.writeUTF("FILE_NOT_FOUND");
            return;
        }
        
        // Gửi thông tin file
        dataOut.writeUTF("FILE_FOUND");
        dataOut.writeLong(fileToDownload.length());
        dataOut.writeUTF(FileUtils.formatFileSize(fileToDownload.length()));
        
        // Đợi xác nhận từ client
        String confirmation = dataIn.readUTF();
        if (!confirmation.equals("CONFIRM_DOWNLOAD")) {
            dataOut.writeUTF("DOWNLOAD_CANCELLED");
            return;
        }
        
        // Bắt đầu gửi file
        dataOut.writeUTF("SENDING_FILE");
        
        try (FileInputStream fis = new FileInputStream(fileToDownload);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = bis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
            
            dataOut.flush();
            System.out.println("Đã gửi file: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Lỗi gửi file: " + e.getMessage());
            throw e;
        }
    }
}