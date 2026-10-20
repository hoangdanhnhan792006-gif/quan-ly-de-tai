# Triển khai online — Render, Aiven và tên miền riêng

Cập nhật 20/10/2026. Bản online đã chạy, đăng nhập admin và tạo tài khoản/hồ sơ được; FE đã ghép. Render đã hiển thị Verified và Certificate Issued cho tên miền riêng. Đây là hướng dẫn cấu hình và cập nhật bản hiện tại, không yêu cầu tạo lại dịch vụ.

## 1. Thông tin bản triển khai

| Thành phần | Giá trị |
|---|---|
| URL chính | https://nhom4qldt.id.vn |
| www | https://www.nhom4qldt.id.vn, chuyển về URL chính |
| URL Render | https://quan-ly-de-tai.onrender.com |
| Git branch | develop |
| Runtime | Docker, Java 21, profile prod |
| Render compute | Free |
| Root Directory | Trống, Dockerfile và pom.xml ở root repo |
| Dockerfile Path | ./Dockerfile |
| Health Check | /login |
| Cổng bên trong | PORT do Render cấp; log đã ghi nhận 10000 |
| Database | Aiven MySQL, defaultdb; log đã ghi nhận MySQL 8.4.11 |

Local và Aiven là hai database riêng. Deploy không tự chuyển tài khoản, điểm, dữ liệu hoặc uploads từ máy cá nhân lên cloud. Đăng nhập online dùng tài khoản trong Aiven.

## 2. Aiven và SSL database

```text
jdbc:mysql://quan-ly-de-tai-hoangdanhnhan792006-b337.g.aivencloud.com:24935/defaultdb?serverTimezone=UTC&sslMode=VERIFY_IDENTITY
```

User cloud hiện dùng avnadmin; mật khẩu nhập trực tiếp trong Render Environment. Không dùng mật khẩu root/doan_dev local.

`deploy/aiven-ca.pem` là CA công khai, được commit. Dockerfile dùng keytool nhập CA vào truststore có sẵn của Java và đưa truststore sang runtime. Không thay bằng PFX của HTTPS local và không bỏ xác minh SSL để chữa lỗi kết nối.

Flyway đã xác nhận V1/V2 và schema up to date trên Aiven. Log có cảnh báo phiên bản MySQL 8.4 mới hơn mức được Flyway đang dùng kiểm thử chính thức; cảnh báo này không làm lần deploy hiện tại thất bại. Khi nâng dependency phải kiểm tra migration, không sửa checksum V1/V2 cũ hoặc bật ddl-auto=update.

## 3. Biến môi trường Render

| Key | Giá trị/cách đặt |
|---|---|
| SPRING_PROFILES_ACTIVE | prod |
| DB_URL | JDBC URL ở mục 2 |
| DB_USERNAME | avnadmin |
| DB_PASSWORD | Mật khẩu Aiven, chỉ lưu trong Environment |
| APP_PUBLIC_URL | https://nhom4qldt.id.vn — nên đặt khi dùng tên miền riêng |
| UPLOAD_DIR | /app/uploads, ổ tạm |
| MAIL_ENABLED | false ở bản online hiện tại |
| APP_BOOTSTRAP_ADMIN_ENABLED | false sau khi đã tạo admin đầu tiên |
| JAVA_TOOL_OPTIONS | -XX:TieredStopAtLevel=1 |

Nếu APP_PUBLIC_URL chưa đặt, profile prod lấy RENDER_EXTERNAL_URL; web vẫn có thể truy cập nhưng link trong email có thể dùng địa chỉ onrender.com. Cần tự kiểm tra Environment trên dashboard, không coi hướng dẫn là bằng chứng biến đã được lưu.

Chỉ lần đầu trên schema rỗng mới cần bật bootstrap và đặt APP_BOOTSTRAP_ADMIN_USERNAME/PASSWORD/FULL_NAME/EMAIL. Sau khi đăng nhập được, tắt bootstrap; có thể xóa bốn biến thông tin bootstrap. Không bật lại để đổi mật khẩu một tài khoản đã tồn tại; dùng chức năng tài khoản trên web.

Không thêm LOCAL_SSL_PASSWORD hoặc LOCAL_SSL_KEY_STORE lên Render. Không bật local-domain/local-https trên cloud. Render cấp HTTPS ngoài ứng dụng; Java bên trong vẫn nhận HTTP qua PORT, secure cookie và forward headers đã cấu hình.

## 4. Tên miền riêng và HTTPS

Tên miền đăng ký tại Tenten: nhom4qldt.id.vn; ảnh quản lý ghi hạn 20/10/2028. Theo dõi phí/điều kiện gia hạn khi hết ưu đãi, không coi miễn phí vĩnh viễn.

DNS online đang cấu hình theo hướng dẫn Render:

| Loại | Host | Giá trị |
|---|---|---|
| A | @ | 216.24.57.1 |
| CNAME | www | quan-ly-de-tai.onrender.com |

Render Settings → Custom Domains có nhom4qldt.id.vn và www.nhom4qldt.id.vn. Cả hai đã Verified/Certificate Issued, www chuyển về root. Render tự cấp/gia hạn TLS của phần online; không cần tải chứng chỉ win-acme lên.

`qldt.nhom4qldt.id.vn:8443` là bản local riêng, ánh xạ hosts trên máy người phát triển. Không thêm @ hoặc www vào hosts về 127.0.0.1, nếu không chính máy đó có thể không vào bản Render như mong muốn. Không xóa bản ghi mail hoặc DNS khác chỉ để gắn web.

Nếu đổi dịch vụ hoặc tên miền, lấy lại hướng dẫn DNS trên dashboard trước khi sửa; giá trị trong tài liệu là cấu hình tại ngày cập nhật.

## 5. Cập nhật code lên online

1. Chạy/test bản local đã ghép; rà git status và diff, tránh đưa bí mật vào commit.
2. Commit đúng file, tích hợp develop sau review. Nếu đang trên nhánh tích hợp được nhóm thống nhất và dùng quy trình hiện tại, push HEAD:develop; không dùng lệnh này từ nhánh tính năng chưa review.
3. Render → Manual Deploy → Deploy latest commit. Không tạo service/database mới cho mỗi lần cập nhật.
4. Theo dõi build, log Spring Boot, Flyway và dòng Started; chờ Status Live.
5. Mở URL chính, Ctrl+F5, đăng nhập và thử luồng chịu ảnh hưởng. Chỉ Live không chứng minh mọi nghiệp vụ đã được kiểm thử.

Auto-Deploy nên để Off khi nhóm còn ghép code; kiểm tra dashboard để biết trạng thái thực tế. Không cần bật lại chỉ vì thêm tên miền.

## 6. Khi deploy chậm hoặc lỗi

| Hiện tượng | Cách xử lý |
|---|---|
| Building đang tải Maven | Theo dõi dòng log mới; không bấm deploy nhiều lần |
| Port scan timeout | Xem Java có dòng Started chưa; cấu hình hiện đã đọc PORT đúng. Kiểm tra JAVA_TOOL_OPTIONS, giới hạn tài nguyên, log mới trước khi đổi cổng |
| Access denied 1045 | Kiểm tra đúng user/mật khẩu cloud trong Environment, không dùng root/doan_dev local |
| Lỗi chứng chỉ Aiven | Kiểm tra CA công khai, hostname, sslMode và truststore; không bỏ xác minh |
| Flyway checksum/migration lỗi | Dừng sửa thủ công; đối chiếu source và history, sao lưu trước xử lý |
| Domain Verified nhưng Certificate Pending | Giữ DNS, chờ và tải lại; không cần deploy lại chỉ để xin HTTPS |
| Live nhưng giao diện cũ | Kiểm tra commit deploy/branch, rồi Ctrl+F5 |
| Lần mở đầu chậm | Render Free có thể ngủ khi không dùng; mở trước buổi demo |

Từng có lượt startup timeout; thêm TieredStopAtLevel=1 rồi log ghi Started sau khoảng 83 giây. Đây là kết quả đã quan sát, không phải cam kết mọi lần khởi động cùng thời gian.

## 7. Giới hạn bản demo và sao lưu

- Render Free không có persistent disk; upload có thể mất khi restart/redeploy/spin-down. File metadata còn trong MySQL không có nghĩa file vẫn tải được.
- MAIL_ENABLED đang tắt. Mailpit ở localhost của máy cá nhân không phải SMTP mà Render có thể truy cập. Cổng SMTP 25/465/587 bị chặn trên Free; chưa cấu hình email Internet cho bản online.
- Nhóm chọn demo đầy đủ báo cáo/email bằng local, online minh chứng deploy theo phạm vi đồ án. Chưa làm object storage, email HTTP API hoặc tunnel; không cần tự thêm chỉ để đổi tên miền.
- Sao lưu dữ liệu/tệp quan trọng trước dọn demo. Không xóa Aiven/Render/tên miền trước buổi chấm.
- Chỉ dùng gói miễn phí, không tự nâng gói hoặc bật dịch vụ trả phí. Theo dõi quota và thông báo của nhà cung cấp.

Nguồn: Deployment.pdf của thầy; [Render Custom Domains](https://render.com/docs/custom-domains), [Render DNS](https://render.com/docs/configure-other-dns), [Render Free](https://render.com/docs/free), [Connector/J SSL](https://dev.mysql.com/doc/connector-j/en/connector-j-server-authentication.html).
