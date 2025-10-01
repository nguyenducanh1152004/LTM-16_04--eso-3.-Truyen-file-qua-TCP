package server;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import common.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private FileTransferServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientId;
    private boolean connected = true;
    
    public ClientHandler(Socket socket, FileTransferServer server) {
        this.socket = socket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Receive client registration
            Message regMessage = (Message) in.readObject();
            if (regMessage.getType() == Message.MessageType.REGISTER) {
                this.clientId = regMessage.getSenderId();
                server.registerClient(clientId, this);
                
                // Send confirmation
                sendMessage(new Message(Message.MessageType.REGISTER_CONFIRM, 
                    "SERVER", clientId, "Registration successful"));
            }
            
            // Handle client messages
            while (connected) {
                try {
                    Message message = (Message) in.readObject();
                    handleMessage(message);
                } catch (EOFException e) {
                    break;
                } catch (ClassNotFoundException | IOException e) {
                    server.logMessage("Error reading from client " + clientId + ": " + e.getMessage());
                    break;
                }
            }
            
        } catch (IOException | ClassNotFoundException e) {
            server.logMessage("Error in client handler for " + clientId + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case CLIENT_LIST_REQUEST:
                sendClientList();
                break;
                
            case FILE_REQUEST:
                handleFileRequest(message);
                break;
                
            case FILE_RESPONSE:
                handleFileResponse(message);
                break;
                
            case FILE_TRANSFER:
                handleFileTransfer(message);
                break;
                
            case PING:
                sendMessage(new Message(Message.MessageType.PONG, "SERVER", clientId, "pong"));
                break;
                
            default:
                server.logMessage("Unknown message type from " + clientId + ": " + message.getType());
        }
    }
    
    private void sendClientList() {
        try {
            String clientList = String.join(",", server.getClientList());
            sendMessage(new Message(Message.MessageType.CLIENT_LIST_RESPONSE, 
                "SERVER", clientId, clientList));
        } catch (Exception e) {
            server.logMessage("Error sending client list to " + clientId + ": " + e.getMessage());
        }
    }
    
    private void handleFileRequest(Message message) {
        String targetClientId = message.getTargetId();
        ClientHandler targetClient = server.getClient(targetClientId);
        
        if (targetClient != null) {
            // Forward the request to target client
            targetClient.sendMessage(message);
            server.logMessage("File request forwarded from " + clientId + " to " + targetClientId);
        } else {
            // Target client not found
            sendMessage(new Message(Message.MessageType.ERROR, "SERVER", clientId, 
                "Target client " + targetClientId + " not found"));
        }
    }
    
    private void handleFileResponse(Message message) {
        String targetClientId = message.getTargetId();
        ClientHandler targetClient = server.getClient(targetClientId);
        
        if (targetClient != null) {
            targetClient.sendMessage(message);
            server.logMessage("File response forwarded from " + clientId + " to " + targetClientId);
        }
    }
    
    private void handleFileTransfer(Message message) {
        String targetClientId = message.getTargetId();
        ClientHandler targetClient = server.getClient(targetClientId);
        
        if (targetClient != null) {
            targetClient.sendMessage(message);
            
            FileInfo fileInfo = (FileInfo) message.getData();
            server.logMessage("File transfer: " + fileInfo.getFileName() + 
                " (" + formatFileSize(fileInfo.getFileSize()) + ") from " + 
                clientId + " to " + targetClientId);
        }
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    public void sendMessage(Message message) {
        try {
            if (out != null && connected) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            server.logMessage("Error sending message to " + clientId + ": " + e.getMessage());
            disconnect();
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            server.logMessage("Error closing connection for " + clientId + ": " + e.getMessage());
        }
        
        if (clientId != null) {
            server.unregisterClient(clientId);
        }
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public boolean isConnected() {
        return connected && !socket.isClosed();
    }
}