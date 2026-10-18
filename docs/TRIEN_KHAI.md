# Deploy theo hướng dẫn của thầy: GitHub → Render → Aiven

## Trạng thái hiện tại

Project dùng Java 21, Spring Boot MVC/Thymeleaf, MySQL và Flyway. Đã có Dockerfile, profile prod và cấu hình qua biến môi trường. Chưa tạo dịch vụ Aiven/Render, chưa có URL online. Không coi build JAR thành công là deploy thành công.

## 1. Kiểm thử và đóng gói

Trong thư mục chứa pom.xml, chạy `mvnw.cmd clean verify`. Kiểm thử MySQL chỉ chạy khi cấu hình schema thử riêng theo hướng dẫn backend. File chạy: `target/quan-ly-de-tai-0.0.1-SNAPSHOT.jar`.

Dockerfile build bằng Java 21 và chạy Java 21. Render tự build Docker; không bắt buộc cài Docker trên máy. Không cần chạy lại git init hoặc thay remote đã có. Chỉ commit source sau khi rà soát `git status`; không commit target, .mvn-repository, .env, mật khẩu và uploads.

## 2. Aiven MySQL

- Tạo tài khoản Aiven, chọn MySQL và đúng **Free Plan**. Ảnh trang 6 của Deployment.pdf minh họa gói trial developer-1 có giá sau trial, không chọn theo ảnh đó.
- Chọn vùng khả dụng trên gói miễn phí, không nâng gói chỉ để chọn vùng.
- Ghi host, port, database, user, password vào nơi riêng. Không dùng mật khẩu root local trên cloud.
- Dùng database rỗng cho lần đầu; Flyway chạy V1 rồi V2. Giữ `ddl-auto=validate`, không đổi sang update như ví dụ Todo.
- Kiểm tra phiên bản MySQL do Aiven cấp trước khi chạy migration; không mặc định giống 8.0.46 local.
- Connector/J dùng tham số `sslMode` (đúng hoa thường), không sao chép `sslmode=require` trong ví dụ. Ưu tiên VERIFY_IDENTITY và cấu hình chứng chỉ CA/truststore theo hướng dẫn Aiven. REQUIRED yêu cầu mã hóa nhưng không xác minh danh tính máy chủ.

## 3. Render Web Service

Tạo tài khoản bằng GitHub → New Web Service → chọn repository quan-ly-de-tai → Docker → Free. Nếu repo chứa trực tiếp pom.xml và Dockerfile thì để Root Directory trống; nếu repo có thư mục bao ngoài thì chọn đúng thư mục chứa hai file đó.

Các biến môi trường:

| Biến | Giá trị |
|---|---|
| SPRING_PROFILES_ACTIVE | prod |
| DB_URL | jdbc:mysql://HOST:PORT/DATABASE?serverTimezone=UTC&sslMode=VERIFY_IDENTITY (cấu hình CA phù hợp) |
| DB_USERNAME | User Aiven |
| DB_PASSWORD | Mật khẩu Aiven, nhập ở Render, không ghi Git |
| APP_PUBLIC_URL | URL HTTPS Render của ứng dụng |
| UPLOAD_DIR | /app/uploads cho bản thử; đây là ổ tạm trên Render Free |
| MAIL_ENABLED | false trong lần deploy đầu chưa có SMTP truy cập được |
| APP_BOOTSTRAP_ADMIN_ENABLED | true chỉ trong lần tạo quản trị đầu tiên, sau đó false |
| APP_BOOTSTRAP_ADMIN_USERNAME/PASSWORD/FULL_NAME/EMAIL | Giá trị riêng do nhóm đặt |

PORT nhận từ Render, mặc định 8080. Tài khoản ví dụ của bài Todo không phải tài khoản project này. Không bật bootstrap với mật khẩu mẫu đã công khai.

Prod bật cookie secure; truy cập HTTPS. Không tắt secure để chữa lỗi đăng nhập. Database cũ phải sao lưu và kiểm tra lịch sử Flyway trước, không bật baseline tùy tiện.

## 4. Kiểm thử URL online

- Log báo ứng dụng khởi động và Flyway chạy thành công.
- Mở /login; đăng nhập từng vai trò; kiểm tra menu và thao tác trái quyền.
- Tạo dữ liệu demo và chạy quy trình đăng ký → báo cáo → chấm → công bố → xem điểm theo đợt.
- Thử trên thiết bị khác; không đưa dữ liệu thật của SV lên bản thử.

## 5. Hai giới hạn chưa được bài Todo mẫu giải quyết

Theo [Render Free](https://render.com/docs/free), file upload mất khi restart/redeploy/spin-down, và SMTP 25/465/587 bị chặn. Vì thế:

- Bản thử Render hiện chưa đáp ứng lưu báo cáo bền vững. Trước nghiệm thu toàn bộ online phải chọn nơi lưu tệp ngoài hoặc môi trường có lưu trữ bền vững. Không quảng cáo rằng dữ liệu MySQL giữ được đồng nghĩa file báo cáo cũng giữ được.
- Mailpit trên máy cá nhân không được Render truy cập bằng localhost. Email local kiểm chứng theo Lab 6; email thật từ cloud cần SMTP/đường gửi được hỗ trợ và cấu hình riêng. Không triển khai Mailpit công khai không có bảo vệ.
- Chưa giải quyết hai mục này thì chỉ coi là deploy thử web/database, chưa phải nghiệm thu đầy đủ.

## 6. Sau khi thầy chấm xong

Sao lưu dữ liệu/tệp cần giữ, rồi chủ tài khoản tự dọn các dịch vụ không còn dùng. Không xóa trước buổi chấm vì thầy có thể cần truy cập link. Chỉ dùng gói miễn phí, không tự nâng gói trả phí.

Nguồn: Deployment.pdf của thầy; [Aiven Free MySQL](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier); [Connector/J TLS](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-using-ssl.html). Kiểm tra lại gói dịch vụ khi tạo tài khoản.
