package client;

import common.User;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase {
    private static final String USER_DATA_FILE = "users.dat";
    private Map<String, User> users;
    
    public UserDatabase() {
        users = new HashMap<>();
        loadUsers();
    }
    
    /**
     * Đăng ký user mới
     */
    public boolean register(String username, String password, String displayName) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        if (users.containsKey(username.toLowerCase())) {
            return false; // Username đã tồn tại
        }
        
        User newUser = new User(username.toLowerCase(), password, displayName);
        users.put(username.toLowerCase(), newUser);
        saveUsers();
        return true;
    }
    
    /**
     * Đăng nhập
     */
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        
        User user = users.get(username.toLowerCase());
        if (user != null && user.checkPassword(password)) {
            user.updateLastLogin();
            saveUsers();
            return user;
        }
        
        return null;
    }
    
    /**
     * Kiểm tra username đã tồn tại chưa
     */
    public boolean usernameExists(String username) {
        return users.containsKey(username.toLowerCase());
    }
    
    /**
     * Lấy thông tin user theo username
     */
    public User getUserByUsername(String username) {
        return users.get(username.toLowerCase());
    }
    
    /**
     * Đổi mật khẩu
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = users.get(username.toLowerCase());
        if (user != null && user.checkPassword(oldPassword)) {
            user.setPassword(newPassword);
            saveUsers();
            return true;
        }
        return false;
    }
    
    /**
     * Lưu dữ liệu users vào file
     */
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu dữ liệu user: " + e.getMessage());
        }
    }
    
    /**
     * Tải dữ liệu users từ file
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) {
            return; // File chưa tồn tại, database rỗng
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(USER_DATA_FILE))) {
            users = (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi khi tải dữ liệu user: " + e.getMessage());
            users = new HashMap<>();
        }
    }
    
    /**
     * Lấy tổng số user
     */
    public int getUserCount() {
        return users.size();
    }
    
    /**
     * Xóa tất cả users (dùng cho testing)
     */
    public void clearAllUsers() {
        users.clear();
        File file = new File(USER_DATA_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}