# Email theo Lab 6: JavaMailSender + Thymeleaf + @Async

## Luồng xử lý

Khoa xác nhận/công bố → KetQuaService lưu kết quả và phát ThongBaoKetQua → transaction commit → EmailService nhận sự kiện, chạy @Async → dựng HTML bằng Thymeleaf → gửi riêng một thư cho từng SV trong nhóm.

TransactionalEventListener(AFTER_COMMIT) chỉ bổ sung để không gửi thư cho giao dịch đã rollback. Sự kiện mang chuỗi và danh sách địa chỉ, không mang entity JPA sang luồng nền. Không có nghiệp vụ email trong controller/view. Không cần REST API để demo.

File chính:
- email/dto/ThongBaoKetQua.java: nội dung thông báo độc lập database.
- email/service/EmailService.java: JavaMailSender, MimeMessageHelper, TemplateEngine, @Async và AFTER_COMMIT.
- templates/mail/ket-qua-de-tai.html: mẫu HTML, UTF-8, th:text để escape nội dung.
- QuanLyDeTaiApplication.java: @EnableAsync.

Không có bảng hàng đợi, scheduler hoặc màn hình thử gửi lại. Gửi thất bại được ghi log, không làm mất điểm đã công bố; vẫn thử gửi cho các SV còn lại. Thư đang gửi có thể mất khi tắt ứng dụng, không có tự khôi phục. Đây là giới hạn được chấp nhận của phiên bản đơn giản theo lab.

## Chạy Mailpit trên Windows, không cần Docker

1. Tải bản Windows amd64 từ [Mailpit releases](https://github.com/axllent/mailpit/releases), giải nén ngoài thư mục source.
2. Mở terminal tại nơi chứa mailpit.exe, chạy `./mailpit.exe --listen 127.0.0.1:8025 --smtp 127.0.0.1:1025`.
3. Mở http://localhost:8025. Mailpit chỉ nhận thư thử, không chuyển đến Gmail người nhận.
4. Trong terminal chạy project, bật MAIL_ENABLED=true. application.yml mặc định MAIL_HOST=localhost, MAIL_PORT=1025, MAIL_AUTH=false, MAIL_STARTTLS=false. Không cần tài khoản email/mật khẩu.
5. Giữ các biến DB_USERNAME, DB_PASSWORD như trước rồi chạy `mvnw.cmd spring-boot:run`.

PowerShell: `$env:MAIL_ENABLED = "true"`.
CMD: `set MAIL_ENABLED=true`.
Hai cú pháp không dùng thay nhau. File .env không được Maven/Spring tự đọc; chỉ compose tự đọc .env. Với Maven phải đặt biến môi trường ở terminal/IDE.

## Kịch bản demo

1. Nhóm đã được chấp thuận có 3–5 SV, mỗi tài khoản có email riêng.
2. Hoàn tất các phiếu chấm → tổng hợp → khoa xác nhận → công bố.
3. Màn hình web trở về mà không đợi SMTP. Sau đó Mailpit có một email HTML cho mỗi SV; đúng đề tài, đợt và phiên bản công bố.
4. Link trong thư dẫn đến /sinh-vien/ket-qua; phải đăng nhập và chỉ xem kết quả của mình.
5. Dừng Mailpit rồi công bố một kết quả khác: kết quả vẫn được lưu, log báo gửi thất bại.
6. Công bố lại sau sửa điểm tạo thông báo cho phiên bản mới. Không bấm lại công bố cùng phiên bản để gửi lại thư; service chặn công bố lặp.
7. Giao dịch rollback không gửi thư (đã có test tự động).

## Khi chuyển sang SMTP thật

Đặt MAIL_HOST/PORT/USERNAME/PASSWORD/FROM, bật MAIL_AUTH và MAIL_STARTTLS theo nhà cung cấp. Không ghi bí mật vào Git. Trong profile prod mặc định auth/TLS bật. MAIL_ENABLED mặc định false cho đến khi có cấu hình phù hợp.

Lab có nhắc Mailtrap ở vài đoạn nhưng các bước thực hành dùng Mailpit, nên nhóm dùng Mailpit. Việc thấy thư trong Mailpit không chứng minh Gmail nhận thư. Xem TRIEN_KHAI.md về giới hạn Render Free.
