package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String username;
    private String password; // Trong thực tế nên hash password
    private String displayName;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
    
    public User(String username, String password, String displayName) {
        this.userId = generateUserId();
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.createdDate = LocalDateTime.now();
        this.lastLogin = LocalDateTime.now();
    }
    
    private String generateUserId() {
        // Sinh ID ngắn gọn từ UUID
        String uuid = UUID.randomUUID().toString();
        return "USER_" + uuid.substring(0, 8).toUpperCase();
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("User[%s, username=%s, displayName=%s]", 
            userId, username, displayName);
    }
}