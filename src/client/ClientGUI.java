package client;

import common.FileInfo;
import utils.FileUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class ClientGUI extends JFrame {
    private TCPFileClient client;
    private JButton connectButton, disconnectButton;
    private JButton uploadButton, searchButton, downloadButton;
    private JTextField searchField;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel connectionStatusLabel;
    
    private final String[] columnNames = {"Tên File", "Kích Thước", "Ngày Sửa"};
    
    public ClientGUI() {
        client = new TCPFileClient();
        initializeGUI();
        setupEventHandlers();
        updateConnectionStatus(false);
    }
    
    private void initializeGUI() {
        setTitle("TCP File Transfer Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        JPanel topPanel = createConnectionPanel();
        add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = createMainPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = createStatusPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Kết Nối Server"));
        
        connectButton = new JButton("Kết Nối");
        connectButton.setBackground(new Color(0, 150, 0));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        disconnectButton = new JButton("Ngắt Kết Nối");
        disconnectButton.setBackground(new Color(200, 0, 0));
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.setFont(new Font("Arial", Font.BOLD, 12));
        disconnectButton.setEnabled(false);
        
        connectionStatusLabel = new JLabel("Chưa kết nối");
        connectionStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        connectionStatusLabel.setForeground(Color.RED);
        
        panel.add(connectButton);
        panel.add(disconnectButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("Trạng thái:"));
        panel.add(connectionStatusLabel);
        
        return panel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel uploadPanel = createUploadPanel();
        mainPanel.add(uploadPanel, BorderLayout.NORTH);
        
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createUploadPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Upload File"));
        
        uploadButton = new JButton("Chọn File để Upload");
        uploadButton.setBackground(new Color(0, 123, 255));
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFont(new Font("Arial", Font.BOLD, 12));
        uploadButton.setEnabled(false);
        
        panel.add(uploadButton);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Tìm Kiếm và Download File"));
     
        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchInputPanel.add(new JLabel("Tìm kiếm:"));
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setEnabled(false);
        searchInputPanel.add(searchField);
        
        searchButton = new JButton("Tìm Kiếm");
        searchButton.setBackground(new Color(255, 193, 7));
        searchButton.setForeground(Color.BLACK);
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.setEnabled(false);
        searchInputPanel.add(searchButton);
        
        downloadButton = new JButton("Download File Đã Chọn");
        downloadButton.setBackground(new Color(40, 167, 69));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFont(new Font("Arial", Font.BOLD, 12));
        downloadButton.setEnabled(false);
        searchInputPanel.add(downloadButton);
        
        panel.add(searchInputPanel, BorderLayout.NORTH);
       
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setRowHeight(25);
        fileTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        fileTable.setFont(new Font("Arial", Font.PLAIN, 11));
        
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setVisible(false);
        
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventHandlers() {
       
        connectButton.addActionListener(e -> {
            connectButton.setEnabled(false);
            statusLabel.setText("Đang kết nối...");
            
            SwingUtilities.invokeLater(() -> {
                boolean connected = client.connect();
                updateConnectionStatus(connected);
                
                if (connected) {
                    statusLabel.setText("Đã kết nối thành công");
                } else {
                    statusLabel.setText("Kết nối thất bại");
                    connectButton.setEnabled(true);
                }
            });
        });

        disconnectButton.addActionListener(e -> {
            client.disconnect();
            updateConnectionStatus(false);
            statusLabel.setText("Đã ngắt kết nối");
            clearTable();
        });
        
        uploadButton.addActionListener(e -> handleUpload());
       
        searchButton.addActionListener(e -> handleSearch());
        
        searchField.addActionListener(e -> handleSearch());
        
        downloadButton.addActionListener(e -> handleDownload());
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (client.isConnected()) {
                    client.disconnect();
                }
                System.exit(0);
            }
        });
    }
    
    private void updateConnectionStatus(boolean connected) {
        connectButton.setEnabled(!connected);
        disconnectButton.setEnabled(connected);
        uploadButton.setEnabled(connected);
        searchButton.setEnabled(connected);
        searchField.setEnabled(connected);
        
        if (connected) {
            connectionStatusLabel.setText("Đã kết nối");
            connectionStatusLabel.setForeground(new Color(0, 150, 0));
        } else {
            connectionStatusLabel.setText("Chưa kết nối");
            connectionStatusLabel.setForeground(Color.RED);
            downloadButton.setEnabled(false);
        }
    }
    
    private void handleUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Chọn file để upload");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Hiển thị thông tin file và hỏi xác nhận
            String message = String.format(
                "Bạn có muốn upload file này không?\n\n" +
                "Tên file: %s\n" +
                "Kích thước: %s\n" +
                "Đường dẫn: %s",
                selectedFile.getName(),
                FileUtils.formatFileSize(selectedFile.length()),
                selectedFile.getAbsolutePath()
            );
            
            int confirm = JOptionPane.showConfirmDialog(
                this, message, "Xác nhận Upload", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                startUpload(selectedFile);
            }
        }
    }
    
    private void startUpload(File file) {
        // Disable buttons during upload
        uploadButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setString("Đang upload...");
        statusLabel.setText("Đang upload: " + file.getName());
        
        // Create callback
        TCPFileClient.UploadCallback callback = new TCPFileClient.UploadCallback() {
            @Override
            public boolean onFileExists(String message) {
                return JOptionPane.showConfirmDialog(
                    ClientGUI.this, message, "File đã tồn tại",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION;
            }
            
            @Override
            public void onProgress(int percent, long current, long total) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(percent);
                    progressBar.setString("Upload: " + percent + "% (" + 
                        FileUtils.formatFileSize(current) + "/" + 
                        FileUtils.formatFileSize(total) + ")");
                });
            }
            
            @Override
            public void onSuccess(String message) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText(message);
                    uploadButton.setEnabled(true);
                    JOptionPane.showMessageDialog(ClientGUI.this, message, 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
            @Override
            public void onError(String message) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Lỗi: " + message);
                    uploadButton.setEnabled(true);
                    JOptionPane.showMessageDialog(ClientGUI.this, message, 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        };
        
        // Start upload in background thread
        new Thread(() -> {
            String result = client.uploadFile(file, callback);
            System.out.println("Upload result: " + result);
        }).start();
    }
    
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập từ khóa tìm kiếm", "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Đang tìm kiếm: " + query);
        searchButton.setEnabled(false);
        
        new Thread(() -> {
            List<FileInfo> results = client.searchFiles(query);
            
            SwingUtilities.invokeLater(() -> {
                updateFileTable(results);
                statusLabel.setText("Tìm thấy " + results.size() + " file");
                searchButton.setEnabled(true);
            });
        }).start();
    }
    
    private void updateFileTable(List<FileInfo> files) {
        clearTable();
        
        for (FileInfo file : files) {
            Object[] row = {
                file.getName(),
                file.getFormattedSize(),
                file.getFormattedDate()
            };
            tableModel.addRow(row);
        }
        
        downloadButton.setEnabled(files.size() > 0);
    }
    
    private void clearTable() {
        tableModel.setRowCount(0);
        downloadButton.setEnabled(false);
    }
    
    private void handleDownload() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn file để download", "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        startDownload(fileName);
    }
    
    private void startDownload(String fileName) {
        downloadButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setString("Đang download...");
        statusLabel.setText("Đang download: " + fileName);
        
        TCPFileClient.DownloadCallback callback = new TCPFileClient.DownloadCallback() {
            @Override
            public boolean onConfirmDownload(String fileName, String fileSize, long sizeBytes) {
                String message = String.format(
                    "Bạn có muốn download file này không?\n\n" +
                    "Tên file: %s\n" +
                    "Kích thước: %s",
                    fileName, fileSize
                );
                
                return JOptionPane.showConfirmDialog(
                    ClientGUI.this, message, "Xác nhận Download",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
                ) == JOptionPane.YES_OPTION;
            }
            
            @Override
            public void onProgress(int percent, long current, long total) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(percent);
                    progressBar.setString("Download: " + percent + "% (" + 
                        FileUtils.formatFileSize(current) + "/" + 
                        FileUtils.formatFileSize(total) + ")");
                });
            }
            
            @Override
            public void onSuccess(String message) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText(message);
                    downloadButton.setEnabled(true);
                    JOptionPane.showMessageDialog(ClientGUI.this, message, 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
            @Override
            public void onError(String message) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Lỗi: " + message);
                    downloadButton.setEnabled(true);
                    JOptionPane.showMessageDialog(ClientGUI.this, message, 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        };
        
        new Thread(() -> {
            String result = client.downloadFile(fileName, callback);
            System.out.println("Download result: " + result);
        }).start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}