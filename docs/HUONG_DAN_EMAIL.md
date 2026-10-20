# Hướng dẫn email thông báo kết quả

Cập nhật ngày 20/10/2026. Tài liệu này mô tả email theo Lab 6 và cách demo bằng Mailpit; không yêu cầu người nhận có hộp thư thật.

## 1. Cách hệ thống gửi thư

Khoa xác nhận/công bố → `KetQuaService` lưu kết quả và phát `ThongBaoKetQua` → giao dịch commit → `EmailService` nhận sự kiện, chạy `@Async` → Thymeleaf dựng HTML → gửi riêng một thư cho từng sinh viên trong nhóm.

`@TransactionalEventListener(AFTER_COMMIT)` ngăn gửi thư cho giao dịch đã rollback. Sự kiện mang dữ liệu đã sao chép, không mang entity JPA sang luồng nền. Controller và view không xử lý gửi email.

Các file chính trong `src/main/resources` và package ứng dụng:

- `email/dto/ThongBaoKetQua.java`: dữ liệu thông báo độc lập với phiên JPA.
- `email/service/EmailService.java`: `JavaMailSender`, `MimeMessageHelper`, Thymeleaf và listener sau commit.
- `templates/mail/ket-qua-de-tai.html`: nội dung HTML UTF-8, dùng `th:text` để escape dữ liệu.
- `QuanLyDeTaiApplication.java`: bật `@EnableAsync`.

Không có bảng hàng đợi hoặc chức năng gửi lại thư. SMTP lỗi được ghi log, không hủy điểm đã công bố; hệ thống tiếp tục thử gửi cho những người còn lại. Thư đang chờ có thể mất khi ứng dụng dừng.

## 2. Chạy Mailpit tại máy local

1. Tải bản Windows phù hợp từ [Mailpit releases](https://github.com/axllent/mailpit/releases), giải nén ngoài source.
2. Mở terminal tại thư mục chứa Mailpit và chạy:

```powershell
.\mailpit.exe --listen 127.0.0.1:8025 --smtp 127.0.0.1:1025
```

3. Mở [giao diện Mailpit](http://localhost:8025).
4. Trong terminal chạy ứng dụng, đặt biến môi trường trước khi khởi động:

```powershell
$env:MAIL_ENABLED = "true"
$env:MAIL_HOST = "127.0.0.1"
$env:MAIL_PORT = "1025"
$env:MAIL_AUTH = "false"
$env:MAIL_STARTTLS = "false"
$env:MAIL_FROM = "qldt@example.test"
```

Giữ cấu hình database của máy. Chạy HTTP theo [README](../README.md), hoặc HTTPS bằng script trong [HTTPS local](HTTPS_LOCAL.md). `APP_PUBLIC_URL` phải phù hợp địa chỉ demo để link trong thư đúng; profile `local-domain` đã đặt URL tên miền local.

Mailpit không yêu cầu tài khoản SMTP, không chuyển thư đến Gmail. Maven/Spring không tự đọc file `.env`; hãy đặt biến trong cùng terminal hoặc cấu hình IDE. Dùng cú pháp PowerShell như trên, không trộn với cú pháp `set` của CMD.

## 3. Kịch bản kiểm tra trên web

1. Chuẩn bị nhóm đã được chấp thuận, có 3–5 sinh viên và email riêng.
2. Hoàn tất đủ phiếu chấm hợp lệ → tổng hợp → Khoa xác nhận → công bố.
3. Kiểm tra Mailpit có thư HTML cho từng sinh viên, đúng đề tài, đợt và phiên bản công bố. Màn hình web không phải đợi hoàn tất SMTP.
4. Mở link kết quả trong thư: phải đăng nhập, sinh viên chỉ thấy kết quả của mình.
5. Công bố lại sau quy trình sửa điểm: thư thể hiện phiên bản mới. Không dùng thao tác công bố lặp cùng phiên bản để gửi lại thư; service chặn thao tác này.
6. Với dữ liệu thử khác, dừng Mailpit rồi công bố: kết quả vẫn được lưu, log báo lỗi SMTP.

Xem đầy đủ các bước chuẩn bị dữ liệu tại [hướng dẫn test web](HUONG_DAN_TEST_CHUC_NANG_WEB.md). Không công bố/sửa điểm dữ liệu thật chỉ để thử email.

## 4. Test SMTP tự động tùy chọn

Khi Mailpit đang nghe cổng 1025, chạy tại thư mục project:

```powershell
$env:TEST_SMTP_PORT = "1025"
.\mvnw.cmd --settings .mvn\settings.xml "-Dtest=EmailMailpitTest" test
Remove-Item Env:TEST_SMTP_PORT -ErrorAction SilentlyContinue
```

Test dùng SMTP tại `127.0.0.1`; nếu không đặt `TEST_SMTP_PORT` thì bỏ qua. Nội dung kiểm tra gồm giao dịch rollback/commit và gửi thư độc lập cho người nhận. Kết quả kiểm thử đã ghi nhận nằm trong [tổng hợp dự án](TONG_HOP_DU_AN.md); các bước trên là hướng dẫn chạy lại.

## 5. SMTP thật và demo online

Để gửi ra hộp thư thật, cấu hình `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_AUTH`, `MAIL_STARTTLS` theo nhà cung cấp. Không ghi mật khẩu vào Git. Profile prod mặc định bật auth/TLS; `MAIL_ENABLED` mặc định false.

Bản Render Free hiện dùng `MAIL_ENABLED=false`; SMTP tại các cổng 25/465/587 bị hạn chế trên gói này. Demo email đầy đủ thực hiện ở local bằng Mailpit. Xem [triển khai](TRIEN_KHAI.md) để biết phạm vi online. Chưa triển khai API gửi mail bên thứ ba hoặc hàng đợi gửi lại.
