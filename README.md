<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
      TRUYỀN FILE QUA TCP
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
Hệ thống Truyền file qua TCP được xây dựng nhằm mô phỏng quá trình truyền file giữa các máy tính trong mạng thông qua giao thức TCP.
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
- **Xử lý đa luồng:** `ExecutorService` 
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

## 🚀 3. Một số hình ảnh hệ thống 

- Giao diện Hệ thống.
<p align="center"> <img width="800" height="800" alt="image" src=image1.png/> </p>
<p align="center"><i>Hình ảnh 1</i></p>

- Giao diện Chọn file.

<p align="center"> <img width="800" height="800" alt="image" src=image2.png/> </p>
<p align="center"><i>Hình ảnh 2</i></p>

- Giao diện Thông báo và hiển thị thông tin file.

<p align="center"> <img width="800" height="800" alt="image" src=image3.png/> </p>
<p align="center"><i>Hình ảnh 3</i></p>

- Giao diện Tìm kiếm file.
<p align="center"> <img width="800" height="800" alt="image" src=image4.png/> </p>
<p align="center"><i>Hình ảnh 4</i></p>

## 📝 4. Các bước cài đặt

#### Bước 1: Chuẩn bị môi trường
 - **Kiểm tra Java**: Mở terminal/command prompt và chạy:
   ```bash
   java -version
   javac -version
   ```
   Đảm bảo cả hai lệnh đều hiển thị phiên bản Java 8 trở lên.

- **Chuẩn bị IDE**: Khởi động Eclipse IDE và chọn workspace là thư mục vừa tạo.

#### Bước 2: Tạo project và cấu trúc
**Tạo Java Project**:
   - **File** → **New** → **Java Project**
   - **Project name**: `TCPFileTransfer`
   - **JRE**: Sử dụng default JRE (*Java 21*)
   - Bỏ check **"Create module-info.java file"**
   - Click **Finish**

**Tạo cấu trúc package**: Trong thư mục `src`, tạo các package:
   ```
   src/
   ├── server/
   ├── client/
   ├── common/
   └── utils/
   ```
   - *Cách tạo: Right-click `src` → **New** → **Package** → Nhập tên package → **Finish***

**Tạo các file Java**:
   - `server/TCPFileServer.java` (*với main method*)
   - `server/ClientHandler.java` (*implement Runnable*)
   - `client/TCPFileClient.java`
   - `client/ClientGUI.java` (*extends JFrame, với main method*)
   - `common/FileInfo.java`
   - `utils/FileUtils.java`

#### Bước 3: Copy mã nguồn
**Copy source code**: Sao chép nội dung code vào từng file tương ứng đã tạo.

- **Organize imports**: Sử dụng **Ctrl+Shift+O** để tự động import các thư viện cần thiết.

- **Kiểm tra lỗi**: Đảm bảo không có lỗi compile trong Project Explorer.

#### Bước 4: Chạy ứng dụng

**Khởi động Server:**
- **Right-click** file `TCPFileServer.java`
- **Run As** → **Java Application**
- Server sẽ khởi động trên port **12345** mặc định
- Console hiển thị:
   ```
   Server đã khởi động trên port 12345
   Đang chờ client kết nối...
   ```

**Khởi động Client:**
- **Right-click** file `ClientGUI.java`
- **Run As** → **Java Application**  
- Giao diện GUI sẽ xuất hiện
- Click nút **"Kết Nối"** để kết nối đến Server
- Status sẽ chuyển thành **"Đã kết nối"** (*màu xanh*)
- Server console sẽ hiển thị: `Client đã kết nối: /127.0.0.1`

---
 

## 5. Liên hệ cá nhân
- **Sinh viên thực hiện:** **Nguyễn Đức Anh**
- **Khoa công nghệ thông tin – Trường Đại học Đại Nam**  
- 🌐 Website: [https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)  
- 📧 Email: [anhnguyen0934422067@gmail.com]
- 📱 Fanpage: [AIoTLab - FIT DNU](https://www.facebook.com/DNUAIoTLab)