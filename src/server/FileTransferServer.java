package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import common.*;

public class FileTransferServer extends JFrame {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private JTextArea logArea;
    private JLabel statusLabel;
    private boolean isRunning = false;
    
    public FileTransferServer() {
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("File Transfer Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("Server Status: STOPPED");
        statusLabel.setForeground(Color.RED);
        statusPanel.add(statusLabel);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton startButton = new JButton("Start Server");
        JButton stopButton = new JButton("Stop Server");
        JButton clearLogButton = new JButton("Clear Log");
        
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        clearLogButton.addActionListener(e -> logArea.setText(""));
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(clearLogButton);
        
        // Log area
        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        add(statusPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void startServer() {
        if (isRunning) return;
        
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            statusLabel.setText("Server Status: RUNNING on port " + PORT);
            statusLabel.setForeground(Color.GREEN);
            
            logMessage("Server started on port " + PORT);
            
            // Accept clients in separate thread
            new Thread(this::acceptClients).start();
            
        } catch (IOException e) {
            logMessage("Error starting server: " + e.getMessage());
        }
    }
    
    private void stopServer() {
        if (!isRunning) return;
        
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Disconnect all clients
            for (ClientHandler client : clients.values()) {
                client.disconnect();
            }
            clients.clear();
            
            statusLabel.setText("Server Status: STOPPED");
            statusLabel.setForeground(Color.RED);
            logMessage("Server stopped");
            
        } catch (IOException e) {
            logMessage("Error stopping server: " + e.getMessage());
        }
    }
    
    private void acceptClients() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
                
            } catch (IOException e) {
                if (isRunning) {
                    logMessage("Error accepting client: " + e.getMessage());
                }
            }
        }
    }
    
    public synchronized void registerClient(String clientId, ClientHandler handler) {
        clients.put(clientId, handler);
        logMessage("Client registered: " + clientId + " (" + clients.size() + " total clients)");
    }
    
    public synchronized void unregisterClient(String clientId) {
        clients.remove(clientId);
        logMessage("Client disconnected: " + clientId + " (" + clients.size() + " total clients)");
    }
    
    public ClientHandler getClient(String clientId) {
        return clients.get(clientId);
    }
    
    public Set<String> getClientList() {
        return new HashSet<>(clients.keySet());
    }
    
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new FileTransferServer().setVisible(true);
        });
    }
}