# Hệ thống Quản lý Đề tài Sinh viên — Khoa CNTT

Đồ án nhóm 4 thành viên. Một ứng dụng Spring Boot MVC quản lý đề tài từ mở đợt, đề xuất, duyệt, lập nhóm, đăng ký, nộp báo cáo đến chấm và công bố kết quả.

**Tài liệu cập nhật theo code và kết quả làm việc đến 20/10/2026.** Quy tắc hiện hành: nhóm 3–5 SV khi đăng ký; hai giai đoạn GV/SV không chồng nhau; GVHD không chấm đề tài mình hướng dẫn.

## 1. Địa chỉ và môi trường

| Môi trường | Địa chỉ | Database |
|---|---|---|
| Local HTTP, mặc định | http://localhost:8080 | MySQL local, quan_ly_de_tai |
| Local tên miền thật, profile local-domain | https://qldt.nhom4qldt.id.vn:8443 | MySQL local, quan_ly_de_tai |
| Online, profile prod | https://nhom4qldt.id.vn | Aiven, defaultdb |
| Địa chỉ Render dự phòng | https://quan-ly-de-tai.onrender.com | Cùng database Aiven |

Bản local và online không đồng bộ dữ liệu. Tên miền local chỉ truy cập được trên máy đã cấu hình hosts và có chứng chỉ riêng; người khác dùng URL online. Không copy PFX hoặc mật khẩu cho cả nhóm.

## 2. Công nghệ

Java 21; Spring Boot 3.5.16; Spring MVC + Thymeleaf; Spring Data JPA/Hibernate; Spring Security form login/session; Bean Validation; Flyway; MySQL. Local đã dùng MySQL 8.0.46, Aiven đã ghi nhận MySQL 8.4.11. Có 24 bảng nghiệp vụ và bảng lịch sử Flyway. Không dùng React/Vue, JWT hoặc REST API riêng.

## 3. Đọc file nào?

| Bạn cần làm gì? | Tài liệu |
|---|---|
| Bàn giao toàn bộ bối cảnh cho thành viên/AI mới | [Tổng hợp dự án](docs/TONG_HOP_DU_AN.md) |
| Hiểu chức năng, vai trò, FR/NFR | [Phạm vi chức năng](docs/PHAM_VI_CHUC_NANG.md) |
| Hiểu 24 bảng, PK/FK, ràng buộc | [Cơ sở dữ liệu](docs/CO_SO_DU_LIEU.md) |
| Chạy máy mới, sửa backend, Git, test tự động | [Hướng dẫn backend](docs/HUONG_DAN_LAM_BACKEND_NHOM.md) |
| Chỉnh giao diện không làm mất liên kết backend | [Hướng dẫn frontend](docs/HUONG_DAN_LAM_FRONTEND.md) |
| Thao tác kiểm thử theo từng vai trò | [Hướng dẫn test web](docs/HUONG_DAN_TEST_CHUC_NANG_WEB.md) |
| Viết Chương 5 Word, chạy luồng và chụp minh chứng | [Hướng dẫn Chương 5](docs/HUONG_DAN_VIET_CHUONG_5.md) |
| Chạy Mailpit, xem email kết quả | [Hướng dẫn email](docs/HUONG_DAN_EMAIL.md) |
| Chạy HTTPS local/tên miền, quản lý PFX | [HTTPS local](docs/HTTPS_LOCAL.md) |
| Deploy, DNS, HTTPS online, biến Render | [Triển khai](docs/TRIEN_KHAI.md) |

Use case nguồn: [USE_CASE.puml](docs/USE_CASE.puml). Thông tin hiện hành nằm trong các file trên; không dùng bản kế hoạch/thiết kế cũ bên ngoài repo để ghi đè code.

## 4. Nhận code và chuẩn bị database

```powershell
git clone --branch develop https://github.com/hoangdanhnhan792006-gif/quan-ly-de-tai.git
cd quan-ly-de-tai
.\mvnw.cmd -v
```

Maven phải nhận **Java 21**. Không bắt buộc cài Maven riêng vì đã có Wrapper. Nếu đã có repo, lưu công việc hiện tại trước khi chuyển nhánh/pull; không clone đè vào thư mục đang làm.

Mỗi máy có MySQL và database riêng. Tạo schema rỗng và tài khoản theo [hướng dẫn backend](docs/HUONG_DAN_LAM_BACKEND_NHOM.md). Flyway tự chạy V1 + V2; không import thêm SQL vào schema đã được ứng dụng khởi tạo.

Git không mang theo dữ liệu MySQL, tài khoản web, mật khẩu, tệp upload hay chứng chỉ cá nhân của máy trưởng nhóm. Database mới trống dữ liệu là bình thường.

## 5. Chạy nhanh HTTP trên máy mới

Trong **PowerShell**, ở thư mục có pom.xml:

```powershell
$env:DB_USERNAME = "doan_dev"
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host "Mat khau MySQL local" -AsSecureString)).Password
.\mvnw.cmd --settings .mvn\settings.xml spring-boot:run
```

Mở http://localhost:8080/login. Mật khẩu MySQL không phải mật khẩu đăng nhập web. File `.env` không tự được Maven/Spring đọc.

Chưa có admin: đặt các biến sau rồi khởi động một lần:

```powershell
$env:APP_BOOTSTRAP_ADMIN_ENABLED = "true"
$env:APP_BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host "Mat khau admin web moi" -AsSecureString)).Password
$env:APP_BOOTSTRAP_ADMIN_FULL_NAME = "Quan tri vien"
$env:APP_BOOTSTRAP_ADMIN_EMAIL = "admin@example.test"
.\mvnw.cmd --settings .mvn\settings.xml spring-boot:run
```

Admin đã có thì không cần khởi tạo lại. Sau lần tạo thành công, Ctrl+C, đặt `$env:APP_BOOTSTRAP_ADMIN_ENABLED = "false"`, bỏ mật khẩu bootstrap khỏi terminal rồi chạy lại. Không có mật khẩu web mặc định chung cho cả nhóm.

## 6. Chạy HTTPS tên miền trên máy đã cấu hình

Chỉ áp dụng khi đã có PFX riêng và ánh xạ hosts theo [HTTPS local](docs/HTTPS_LOCAL.md):

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
$env:DB_USERNAME = "doan_dev"
& .\scripts\Chay-HttpsLocal.ps1 -TenMien
```

Lần đầu hỏi mật khẩu PFX và MySQL nếu cần. Dừng bằng Ctrl+C, chạy lại cùng lệnh trong cùng terminal sẽ dùng mật khẩu đã nhớ. Đóng terminal phải nhập lại. Không dùng `powershell.exe -File` nếu muốn nhớ mật khẩu giữa các lần chạy.

## 7. Kiểm thử, đóng gói và cập nhật online

```powershell
.\mvnw.cmd --settings .mvn\settings.xml clean verify
```

Test MySQL và SMTP chỉ chạy khi bật đúng biến môi trường thử riêng; xem hướng dẫn backend. Build thành công không có nghĩa mọi bộ tích hợp đã chạy. JAR: `target/quan-ly-de-tai-0.0.1-SNAPSHOT.jar`.

Sau khi review và test: commit trên nhánh làm việc, tích hợp vào `develop`, rồi Render → Manual Deploy → Deploy latest commit. Cấu hình prod giữ trên Render; không mang PFX local lên hosting. Rà `git diff --cached` để bảo đảm không có bí mật trước khi commit.

## 8. Giới hạn cần ghi đúng khi báo cáo

- Email đã kiểm chứng bằng Mailpit local; MAIL_ENABLED trên online đang tắt, chưa xác nhận gửi tới hộp thư Internet thật.
- Upload trên Render Free dùng ổ tạm, có thể mất khi restart/redeploy/spin-down. Dữ liệu Aiven được giữ không có nghĩa tệp upload cũng được giữ.
- Theo xác nhận của người làm đồ án, hosting free là phần không bắt buộc. Nhóm chọn demo đầy đủ ở local, giữ bản online để minh chứng triển khai; chưa làm Supabase Storage, Brevo hoặc tunnel.
- Cần hoàn thành Word, ảnh minh chứng, ERD/Use case đưa vào báo cáo, phân công đúng đóng góp thực tế và luyện demo/vấn đáp.
