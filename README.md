<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   NETWORK PROGRAMMING
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>


## 📖 1. Giới thiệu
Sản phẩm được xây dựng nhằm mô phỏng quá trình truyền file giữa các máy tính trong mạng thông qua giao thức TCP.
Trong mô hình này, Server đóng vai trò trung gian, chịu trách nhiệm lắng nghe kết nối từ Client, tiếp nhận dữ liệu (file) từ Client gửi đến và lưu trữ file trên server.

**Mục tiêu chính:**

Hiểu rõ cách hoạt động của giao thức TCP trong việc truyền dữ liệu.
- Nắm vững cơ chế kết nối Client – Server.
- Thực hành xử lý dữ liệu file (upload/download).
- Xây dựng giao diện người dùng thân thiện với Java Swing.

**Chức năng cơ bản:**

- Upload file từ Client lên Server.
- Tìm kiếm file theo tên trên Server.
- Download file từ Server về Client.
- Hiển thị thông tin file (tên, kích thước, ngày chỉnh sửa).

---

## 🛠️ 2. Công nghệ sử dụng

- **Ngôn ngữ lập trình:** Java (JDK 8+)
- **Giao thức mạng:** TCP Socket (`java.net.Socket`, `java.net.ServerSocket`)
- **Xử lý đa luồng:** `ExecutorService` để phục vụ nhiều Client đồng thời
- **Công nghệ giao diện:** Java Swing (`JFrame`, `JButton`, `JTable`, `JProgressBar`, `JFileChooser`)
- **Cơ chế truyền dữ liệu:** `DataInputStream` và `DataOutputStream` để truyền file và lệnh (`UPLOAD`, `DOWNLOAD`, `SEARCH`)

### 📚 Thư viện sử dụng
- `java.net` - Socket communication  
- `java.io` - File I/O operations  
- `javax.swing` - GUI components  
- `java.awt` - Layout managers và Color  
- `java.util` - Collections và Date formatting  
- `java.util.concurrent` - Thread pool management  

### 🖥️ Công cụ & Môi trường phát triển
- **Công cụ phát triển:** Eclipse IDE   
- **Phiên bản JDK:** Java SE 8+ (khuyến nghị Java 21)  
- **Hệ điều hành:** Windows 10/11 (đa nền tảng: Linux, macOS)  

---

## 3. Một số hình ảnh hệ thống 
## 4. Các bước cài đặt 

## 5. Liên hệ cá nhân

- 📧 Email:anhnguyen0934422067@gmail.