package client;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import common.*;

public class ClientGUI extends JFrame {
    // Network settings
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    // Network components
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientId;
    private User currentUser;
    private boolean connected = false;
    
    // GUI Components
    private JTextArea logArea;
    private JList<String> clientList;
    private DefaultListModel<String> clientListModel;
    private JTextField targetClientField;
    private JLabel statusLabel;
    private JLabel clientIdLabel;
    private JLabel userInfoLabel;
    private JProgressBar transferProgress;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    
    // Transfer history
    private List<TransferHistory> transferHistoryList = new ArrayList<>();
    
    // Current transfer state
    private File pendingFile;
    
    // Connection components for visibility control
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton refreshButton;
    private JButton selectFileButton;
    private JButton logoutButton;
    
    public ClientGUI() {
        // Hiển thị màn hình đăng nhập trước
        currentUser = LoginDialog.showLoginDialog(this);
        
        if (currentUser == null) {
            // User đóng dialog mà không đăng nhập
            System.exit(0);
        }
        
        // Sử dụng UserID làm ClientID
        clientId = currentUser.getUserId();
        
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Phần mềm truyền file bằng TCP - " + currentUser.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panels
        createTopPanel();
        createCenterPanel();
        createBottomPanel();
        
        // Window settings
        setSize(900, 650);
        setLocationRelativeTo(null);
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                System.exit(0);
            }
        });
        
        // Auto connect after login
        autoConnect();
    }
    
    private void autoConnect() {
        // Tự động kết nối sau khi đăng nhập
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (connect()) {
                        statusLabel.setText("Trạng thái: Đã kết nối");
                        statusLabel.setForeground(Color.GREEN);
                        updateConnectionUI(true);
                        requestClientList();
                    }
                });
            }
        }, 500);
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        userInfoLabel = new JLabel("👤 " + currentUser.getDisplayName() + " (" + clientId + ")");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 13));
        userInfoLabel.setForeground(new Color(52, 73, 94));
        
        clientIdLabel = new JLabel("| ID: " + clientId);
        clientIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        clientIdLabel.setForeground(new Color(127, 140, 141));
        
        statusLabel = new JLabel("| Trạng thái: Đang kết nối...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.ORANGE);
        
        statusPanel.add(userInfoLabel);
        statusPanel.add(clientIdLabel);
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalGlue());
        
        // Logout button
        logoutButton = new JButton("Đăng xuất");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 11));
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setPreferredSize(new Dimension(100, 25));
        logoutButton.addActionListener(e -> handleLogout());
        statusPanel.add(logoutButton);
        
        // Connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout());
        connectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        connectButton = new JButton("Kết nối lại");
        disconnectButton = new JButton("Ngắt kết nối");
        refreshButton = new JButton("Làm mới danh sách");
        
        // Trang trí nút kết nối
        connectButton.setBackground(new Color(46, 204, 113));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Arial", Font.BOLD, 12));
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        
        // Trang trí nút ngắt kết nối
        disconnectButton.setBackground(new Color(231, 76, 60));
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.setFont(new Font("Arial", Font.BOLD, 12));
        disconnectButton.setBorderPainted(false);
        disconnectButton.setFocusPainted(false);
        
        // Trang trí nút làm mới
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setBorderPainted(false);
        refreshButton.setFocusPainted(false);
        
        connectButton.addActionListener(e -> {
            if (connect()) {
                statusLabel.setText("| Trạng thái: Đã kết nối");
                statusLabel.setForeground(Color.GREEN);
                updateConnectionUI(true);
                requestClientList();
            } else {
                statusLabel.setText("| Trạng thái: Kết nối thất bại");
                statusLabel.setForeground(Color.RED);
            }
        });
        
        disconnectButton.addActionListener(e -> {
            disconnect();
            statusLabel.setText("| Trạng thái: Đã ngắt kết nối");
            statusLabel.setForeground(Color.RED);
            clientListModel.clear();
            updateConnectionUI(false);
        });
        
        refreshButton.addActionListener(e -> {
            if (connected) {
                requestClientList();
            }
        });
        
        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);
        connectionPanel.add(refreshButton);
        
        // Initial visibility
        connectButton.setVisible(false);
        
        topPanel.add(statusPanel, BorderLayout.NORTH);
        topPanel.add(connectionPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            disconnect();
            dispose();
            // Mở lại cửa sổ đăng nhập
            SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
        }
    }
    
    private void updateConnectionUI(boolean isConnected) {
        connectButton.setVisible(!isConnected);
        disconnectButton.setVisible(isConnected);
        refreshButton.setVisible(isConnected);
        if (selectFileButton != null) {
            selectFileButton.setEnabled(isConnected);
        }
    }
    
    private void createCenterPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // File Transfer Tab
        JPanel transferPanel = createTransferPanel();
        tabbedPane.addTab("📤 Truyền file", transferPanel);
        
        // History Tab
        JPanel historyPanel = createHistoryPanel();
        tabbedPane.addTab("📋 Lịch sử", historyPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Left side - Client list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Danh sách Client Online"));
        leftPanel.setPreferredSize(new Dimension(200, 0));
        
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setFont(new Font("Arial", Font.PLAIN, 12));
        leftPanel.add(new JScrollPane(clientList), BorderLayout.CENTER);
        
        // Center - File transfer controls
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Điều khiển truyền file"));
        
        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Target client input
        JPanel targetPanel = new JPanel(new FlowLayout());
        targetPanel.add(new JLabel("ID Client đích:"));
        targetClientField = new JTextField(15);
        targetPanel.add(targetClientField);
        controlPanel.add(targetPanel);
        
        // File selection and send
        JPanel filePanel = new JPanel(new FlowLayout());
        selectFileButton = new JButton("📁 Chọn & Gửi file");
        
        // Trang trí nút gửi file
        selectFileButton.setBackground(new Color(155, 89, 182));
        selectFileButton.setForeground(Color.WHITE);
        selectFileButton.setFont(new Font("Arial", Font.BOLD, 12));
        selectFileButton.setBorderPainted(false);
        selectFileButton.setFocusPainted(false);
        selectFileButton.setPreferredSize(new Dimension(150, 35));
        
        selectFileButton.addActionListener(e -> selectAndSendFile());
        filePanel.add(selectFileButton);
        controlPanel.add(filePanel);
        
        // Progress bar
        transferProgress = new JProgressBar(0, 100);
        transferProgress.setStringPainted(true);
        transferProgress.setString("Sẵn sàng");
        transferProgress.setBackground(Color.WHITE);
        transferProgress.setForeground(new Color(46, 204, 113));
        transferProgress.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        controlPanel.add(transferProgress);
        
        // Auto-fill target from selection
        clientList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = clientList.getSelectedValue();
                if (selected != null && !selected.equals(clientId)) {
                    targetClientField.setText(selected);
                }
            }
        });
        
        centerPanel.add(controlPanel, BorderLayout.NORTH);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // History table
        String[] columns = {"Thời gian", "Loại", "Tên file", "Dung lượng", "Đối tác", "Trạng thái", "Thời lượng"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(historyModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setBackground(new Color(52, 73, 94));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        historyTable.setGridColor(new Color(189, 195, 199));
        
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setOpaque(true);
                    if (isSelected) {
                        label.setBackground(table.getSelectionBackground());
                        label.setForeground(table.getSelectionForeground());
                    } else {
                        label.setBackground(table.getBackground());
                        label.setForeground(table.getForeground());
                    }
                    label.setToolTipText(value != null ? value.toString() : null);
                }
                return c;
            }
        });
        
        historyTable.setShowGrid(true);
        historyTable.setIntercellSpacing(new Dimension(1, 1));
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Clear history button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Xóa lịch sử");
        
        clearButton.setBackground(new Color(230, 126, 34));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        clearButton.setPreferredSize(new Dimension(120, 30));
        
        clearButton.addActionListener(e -> {
            transferHistoryList.clear();
            historyModel.setRowCount(0);
            appendLog("Đã xóa lịch sử truyền file");
        });
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Nhật ký hoạt động"));
        
        logArea = new JTextArea(8, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 249, 250));
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        JPanel logControlPanel = new JPanel(new FlowLayout());
        JButton clearLogButton = new JButton("Xóa nhật ký");
        
        clearLogButton.setBackground(new Color(149, 165, 166));
        clearLogButton.setForeground(Color.WHITE);
        clearLogButton.setFont(new Font("Arial", Font.BOLD, 11));
        clearLogButton.setBorderPainted(false);
        clearLogButton.setFocusPainted(false);
        clearLogButton.setPreferredSize(new Dimension(100, 25));
        
        clearLogButton.addActionListener(e -> logArea.setText(""));
        logControlPanel.add(clearLogButton);
        
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(logControlPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // =============== NETWORK METHODS ===============
    
    private boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Register with server using UserID
            Message registerMessage = new Message(Message.MessageType.REGISTER, 
                clientId, "SERVER", "Client registration: " + currentUser.getDisplayName());
            out.writeObject(registerMessage);
            out.flush();
            
            // Wait for confirmation
            Message confirmation = (Message) in.readObject();
            if (confirmation.getType() == Message.MessageType.REGISTER_CONFIRM) {
                connected = true;
                
                // Start message handler thread
                new Thread(this::handleMessages).start();
                
                appendLog("Đã kết nối với server với ID: " + clientId);
                appendLog("Xin chào, " + currentUser.getDisplayName() + "!");
                return true;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            appendLog("Kết nối thất bại: " + e.getMessage());
            return false;
        }
        
        return false;
    }
    
    private void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            appendLog("Đã ngắt kết nối khỏi server");
        } catch (IOException e) {
            appendLog("Lỗi khi ngắt kết nối: " + e.getMessage());
        }
    }
    
    private void handleMessages() {
        while (connected) {
            try {
                Message message = (Message) in.readObject();
                SwingUtilities.invokeLater(() -> processMessage(message));
                
            } catch (EOFException e) {
                break;
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    appendLog("Lỗi khi nhận tin nhắn: " + e.getMessage());
                }
                break;
            }
        }
    }
    
    private void processMessage(Message message) {
        switch (message.getType()) {
            case CLIENT_LIST_RESPONSE:
                handleClientListResponse(message);
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
                
            case ERROR:
                appendLog("Lỗi từ server: " + message.getContent());
                break;
                
            default:
                appendLog("Nhận được tin nhắn không xác định: " + message.getType());
        }
    }
    
    private void handleClientListResponse(Message message) {
        String[] clients = message.getContent().split(",");
        clientListModel.clear();
        for (String client : clients) {
            if (!client.equals(clientId) && !client.trim().isEmpty()) {
                clientListModel.addElement(client);
            }
        }
        appendLog("Danh sách client đã cập nhật (" + clientListModel.size() + " client đang online)");
    }
    
    private void handleFileRequest(Message message) {
        FileInfo fileInfo = (FileInfo) message.getData();
        String senderName = message.getSenderId();
        
        String msg = String.format("Yêu cầu gửi file từ %s:\n\nTên file: %s\nDung lượng: %s\n\nBạn có chấp nhận file này không?",
            senderName, fileInfo.getFileName(), formatFileSize(fileInfo.getFileSize()));
        
        int option = JOptionPane.showConfirmDialog(this, msg, "Yêu cầu nhận file", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        boolean accept = (option == JOptionPane.YES_OPTION);
        respondToFileRequest(senderName, accept);
    }
    
    private void handleFileResponse(Message message) {
        boolean accepted = "ACCEPT".equals(message.getContent());
        String senderName = message.getSenderId();
        
        if (accepted) {
            appendLog("Yêu cầu gửi file đã được chấp nhận bởi " + senderName);
            if (pendingFile != null) {
                sendFile(senderName, pendingFile);
                pendingFile = null;
            }
        } else {
            appendLog("Yêu cầu gửi file đã bị từ chối bởi " + senderName);
            pendingFile = null;
        }
    }
    
    private void handleFileTransfer(Message message) {
        FileInfo fileInfo = (FileInfo) message.getData();
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileInfo.getFileName()));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            
            try {
                updateProgress(0, "Đang nhận file...");
                
                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                    fos.write(fileInfo.getFileData());
                }
                
                TransferHistory history = new TransferHistory(fileInfo.getFileName(), 
                    fileInfo.getFileSize(), fileInfo.getSenderId(), 
                    TransferHistory.TransferType.RECEIVED, 
                    TransferHistory.TransferStatus.SUCCESS);
                addTransferHistory(history);
                
                updateProgress(100, "Nhận file thành công");
                appendLog("Đã nhận file: " + fileInfo.getFileName() + " từ " + fileInfo.getSenderId());
                
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateProgress(0, "Sẵn sàng");
                    }
                }, 2000);
                
            } catch (IOException e) {
                appendLog("Lỗi khi lưu file: " + e.getMessage());
                updateProgress(0, "Lỗi khi lưu file");
                
                TransferHistory history = new TransferHistory(fileInfo.getFileName(), 
                    fileInfo.getFileSize(), fileInfo.getSenderId(), 
                    TransferHistory.TransferType.RECEIVED, 
                    TransferHistory.TransferStatus.FAILED);
                history.setErrorMessage(e.getMessage());
                addTransferHistory(history);
            }
        }
    }
    
    // =============== ACTION METHODS ===============
    
    private void selectAndSendFile() {
        String targetId = targetClientField.getText().trim();
        if (targetId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ID client đích", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!connected) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối với server", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            if (selectedFile.length() > 50 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this, "File quá lớn (tối đa 50MB)", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            pendingFile = selectedFile;
            sendFileRequest(targetId, selectedFile);
        }
    }
    
    private void requestClientList() {
        if (!connected) return;
        
        try {
            Message message = new Message(Message.MessageType.CLIENT_LIST_REQUEST, 
                clientId, "SERVER", "Get client list");
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            appendLog("Lỗi khi yêu cầu danh sách client: " + e.getMessage());
        }
    }
    
    private void sendFileRequest(String targetClientId, File file) {
        if (!connected) return;
        
        try {
            FileInfo fileInfo = new FileInfo(file.getName(), file.length(), 
                null, clientId, targetClientId);
            
            Message message = new Message(Message.MessageType.FILE_REQUEST, 
                clientId, targetClientId, fileInfo);
            out.writeObject(message);
            out.flush();
            
            appendLog("Đã gửi yêu cầu file tới " + targetClientId + ": " + file.getName());
            
        } catch (IOException e) {
            appendLog("Lỗi khi gửi yêu cầu file: " + e.getMessage());
        }
    }
    
    private void respondToFileRequest(String requesterClientId, boolean accept) {
        if (!connected) return;
        
        try {
            String response = accept ? "ACCEPT" : "REJECT";
            Message message = new Message(Message.MessageType.FILE_RESPONSE, 
                clientId, requesterClientId, response);
            out.writeObject(message);
            out.flush();
            
            appendLog("Yêu cầu file đã " + (accept ? "được chấp nhận" : "bị từ chối") + 
                " cho " + requesterClientId);
            
        } catch (IOException e) {
            appendLog("Lỗi khi phản hồi yêu cầu file: " + e.getMessage());
        }
    }
    
    private void sendFile(String targetClientId, File file) {
        if (!connected) return;
        
        new Thread(() -> {
            try {
                updateProgress(0, "Đang đọc file...");
                
                byte[] fileData = readFileBytes(file);
                
                updateProgress(50, "Đang gửi file...");
                
                FileInfo fileInfo = new FileInfo(file.getName(), file.length(), 
                    fileData, clientId, targetClientId);
                
                Message message = new Message(Message.MessageType.FILE_TRANSFER, 
                    clientId, targetClientId, fileInfo);
                
                long startTime = System.currentTimeMillis();
                out.writeObject(message);
                out.flush();
                long endTime = System.currentTimeMillis();
                
                TransferHistory history = new TransferHistory(file.getName(), file.length(), 
                    targetClientId, TransferHistory.TransferType.SENT, 
                    TransferHistory.TransferStatus.SUCCESS, endTime - startTime);
                
                SwingUtilities.invokeLater(() -> {
                    addTransferHistory(history);
                    updateProgress(100, "Gửi file thành công");
                    appendLog("Đã gửi file thành công: " + file.getName() + 
                        " (" + formatFileSize(file.length()) + ") tới " + targetClientId);
                    
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            updateProgress(0, "Sẵn sàng");
                        }
                    }, 2000);
                });
                
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("Lỗi khi gửi file: " + e.getMessage());
                    updateProgress(0, "Lỗi khi gửi file");
                    
                    TransferHistory history = new TransferHistory(file.getName(), file.length(), 
                        targetClientId, TransferHistory.TransferType.SENT, 
                        TransferHistory.TransferStatus.FAILED);
                    history.setErrorMessage(e.getMessage());
                    addTransferHistory(history);
                });
            }
        }).start();
    }
    
    // =============== UTILITY METHODS ===============
    
    private byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            return baos.toByteArray();
        }
    }
    
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void addTransferHistory(TransferHistory history) {
        transferHistoryList.add(history);
        
        Object[] row = {
            history.getFormattedTimestamp(),
            history.getType() == TransferHistory.TransferType.SENT ? "gửi" : "nhận",
            history.getFileName(),
            history.getFormattedFileSize(),
            history.getPartnerId(),
            history.getStatus() == TransferHistory.TransferStatus.SUCCESS ? "thành công" : 
            history.getStatus() == TransferHistory.TransferStatus.FAILED ? "thất bại" : "đã hủy",
            history.getTransferTime() > 0 ? history.getFormattedTransferTime() : "-"
        };
        
        historyModel.addRow(row);
    }
    
    private void updateProgress(int value, String text) {
        SwingUtilities.invokeLater(() -> {
            transferProgress.setValue(value);
            transferProgress.setString(text);
        });
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    // =============== MAIN METHOD ===============
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new ClientGUI().setVisible(true);
        });
    }
}