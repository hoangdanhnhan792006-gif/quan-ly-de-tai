# Hệ thống quản lý đề tài sinh viên

Ứng dụng đồ án của Khoa CNTT, xây dựng bằng Java 21, Spring Boot 3.5, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, Flyway và MySQL 8.

## Yêu cầu môi trường

- JDK 21
- Maven 3.9+
- MySQL 8.0
- Database `quan_ly_de_tai`
- Tài khoản database dành riêng cho ứng dụng, mặc định là `doan_dev`

## Biến môi trường

Không ghi mật khẩu MySQL vào Git. Trước khi chạy, thiết lập:

```powershell
$env:JAVA_HOME = "C:\Users\Admin\.jdks\jdk-21.0.12.1+1"
$env:DB_USERNAME = "doan_dev"
$env:DB_PASSWORD = "mat-khau-mysql-cua-may"
```

`DB_URL` có giá trị mặc định:

```text
jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
```

## Chạy ứng dụng

```powershell
.\mvnw.cmd spring-boot:run
```

Mở `http://localhost:8080`.

Chạy kiểm thử và đóng gói:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd package
```

## Tạo quản trị viên đầu tiên

Database mặc định được giữ sạch. Khi cần tạo tài khoản quản trị đầu tiên, đặt các biến môi trường dưới đây rồi chạy ứng dụng một lần:

```powershell
$env:APP_BOOTSTRAP_ADMIN_ENABLED = "true"
$env:APP_BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = "mat-khau-toi-thieu-8-ky-tu"
$env:APP_BOOTSTRAP_ADMIN_FULL_NAME = "Quản trị viên"
$env:APP_BOOTSTRAP_ADMIN_EMAIL = "admin@example.edu.vn"
.\mvnw.cmd spring-boot:run
```

Sau khi thấy thông báo đã tạo tài khoản, dừng ứng dụng và đặt `APP_BOOTSTRAP_ADMIN_ENABLED=false`. Không ghi mật khẩu thật vào file hoặc Git.

## Database và Flyway

Migration `V1__khoi_tao_24_bang.sql` là nguồn tạo cấu trúc cho database mới. Database `quan_ly_de_tai` trên máy hiện đã có đúng 24 bảng nên Flyway sẽ tạo bản ghi baseline phiên bản 1 mà không chạy lại DDL. Hibernate dùng `ddl-auto=validate`, không tự tạo hoặc sửa bảng.

## Cấu trúc package ban đầu

```text
com.khoacntt.quanlydetai
├── account       Tài khoản, vai trò và xác thực
├── hoso          Bộ môn, hồ sơ sinh viên và giảng viên
├── dotdangky     Đợt đăng ký
├── detai         Đề tài và giảng viên hướng dẫn
├── nhom          Nhóm và lời mời nhóm
├── dangky        Đăng ký/giữ chỗ đề tài
├── baocao        Báo cáo và tệp nộp
├── danhgia       Hội đồng, chấm điểm và kết quả
├── thongbao      Thông báo
├── common        Xử lý lỗi và bảo mật dùng chung
├── config        Cấu hình Spring
└── web           Trang dùng chung
```

## Tài liệu dành cho nhóm

- Phạm vi và các chức năng cần code: `docs/PHAM_VI_CHUC_NANG.md`.
- Quy tắc phân công, Git, database và Definition of Done: `docs/HUONG_DAN_LAM_BACKEND_NHOM.md`.
