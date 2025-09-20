package server;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class TCPFileServer {
	private static final int PORT = 12345;
    private static final String SERVER_FILES_DIR = "server_files";
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning = false;
    
    public TCPFileServer() {
        threadPool = Executors.newCachedThreadPool();
        File serverDir = new File(SERVER_FILES_DIR);
        if (!serverDir.exists()) {
            serverDir.mkdirs();
        }
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Server đã khởi động trên port " + PORT);
            System.out.println("Đang chờ client kết nối...");
            
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client đã kết nối: " + clientSocket.getInetAddress());
                
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, SERVER_FILES_DIR);
                threadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Lỗi server: " + e.getMessage());
            }
        }
    }
    
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Lỗi khi đóng server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        TCPFileServer server = new TCPFileServer();
        
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nĐang đóng server...");
            server.stop();
        }));
        
        server.start();
    }

}
