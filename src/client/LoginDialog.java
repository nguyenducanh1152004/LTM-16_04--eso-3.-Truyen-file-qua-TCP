package client;

import common.User;
import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField displayNameField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton switchButton;
    private UserDatabase userDatabase;
    private User loggedInUser;
    private boolean isLoginMode = true;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    
    public LoginDialog(Frame parent) {
        super(parent, "Đăng nhập / Đăng ký", true);
        userDatabase = new UserDatabase();
        initializeGUI();
    }
    
    private void initializeGUI() {
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 152, 219));
        JLabel titleLabel = new JLabel("PHẦN MỀM TRUYỀN FILE BẰNG TCP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // Card panel for login/register
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Login panel
        JPanel loginPanel = createLoginPanel();
        cardPanel.add(loginPanel, "LOGIN");
        
        // Register panel
        JPanel registerPanel = createRegisterPanel();
        cardPanel.add(registerPanel, "REGISTER");
        
        // Bottom panel with switch button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        switchButton = new JButton("Chưa có tài khoản? Đăng ký ngay");
        switchButton.setForeground(new Color(52, 152, 219));
        switchButton.setBorderPainted(false);
        switchButton.setContentAreaFilled(false);
        switchButton.setFocusPainted(false);
        switchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        switchButton.addActionListener(e -> switchMode());
        bottomPanel.add(switchButton);
        
        add(titlePanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginButton = new JButton("Đăng nhập");
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(150, 35));
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton, gbc);
        
        // Enter key listener
        passwordField.addActionListener(e -> handleLogin());
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JTextField regUsernameField = new JTextField(15);
        panel.add(regUsernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JPasswordField regPasswordField = new JPasswordField(15);
        panel.add(regPasswordField, gbc);
        
        // Display Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel displayNameLabel = new JLabel("Tên hiển thị:");
        panel.add(displayNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        displayNameField = new JTextField(15);
        panel.add(displayNameField, gbc);
        
        // Register button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        registerButton = new JButton("Đăng ký");
        registerButton.setBackground(new Color(155, 89, 182));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setPreferredSize(new Dimension(150, 35));
        registerButton.addActionListener(e -> handleRegister(regUsernameField, regPasswordField));
        panel.add(registerButton, gbc);
        
        return panel;
    }
    
    private void switchMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            cardLayout.show(cardPanel, "LOGIN");
            switchButton.setText("Chưa có tài khoản? Đăng ký ngay");
        } else {
            cardLayout.show(cardPanel, "REGISTER");
            switchButton.setText("Đã có tài khoản? Đăng nhập");
        }
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập đầy đủ thông tin", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        User user = userDatabase.login(username, password);
        if (user != null) {
            loggedInUser = user;
            JOptionPane.showMessageDialog(this, 
                "Đăng nhập thành công!\nID của bạn: " + user.getUserId(), 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Tên đăng nhập hoặc mật khẩu không đúng", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
    
    private void handleRegister(JTextField regUsernameField, JPasswordField regPasswordField) {
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String displayName = displayNameField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (displayName.isEmpty()) {
            displayName = username; // Sử dụng username làm display name mặc định
        }
        
        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, 
                "Mật khẩu phải có ít nhất 4 ký tự", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (userDatabase.register(username, password, displayName)) {
            JOptionPane.showMessageDialog(this, 
                "Đăng ký thành công!\nBạn có thể đăng nhập ngay bây giờ.", 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Chuyển về màn hình đăng nhập
            switchMode();
            usernameField.setText(username);
            passwordField.setText("");
            usernameField.requestFocus();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public User getLoggedInUser() {
        return loggedInUser;
    }
    
    public static User showLoginDialog(Frame parent) {
        LoginDialog dialog = new LoginDialog(parent);
        dialog.setVisible(true);
        return dialog.getLoggedInUser();
    }
}