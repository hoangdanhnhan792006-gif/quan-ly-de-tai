# Hệ thống Quản lý Đề tài Sinh viên — Phạm vi chức năng

## 1. Mục tiêu project

Xây dựng website quản lý toàn bộ quy trình thực hiện đề tài sinh viên tại Khoa CNTT, từ mở đợt đăng ký, công bố đề tài, lập nhóm, đăng ký đề tài, nộp báo cáo, phản biện, chấm điểm đến công bố kết quả.

Hệ thống hỗ trợ bốn loại đề tài:

- `MON_HOC`: đề tài môn học.
- `NCKH`: nghiên cứu khoa học.
- `TLCN`: tiểu luận chuyên ngành.
- `KLTN`: khóa luận tốt nghiệp.

Project không phải hệ thống đăng ký tín chỉ. Không có môn tiên quyết, tín chỉ, lớp học phần hoặc thời khóa biểu.

## 2. Công nghệ thống nhất

- Java 21, Spring Boot 3.5.
- Spring MVC và Thymeleaf; giao diện render từ server.
- Spring Data JPA/Hibernate.
- Spring Security form login và session.
- Bean Validation.
- Flyway quản lý database.
- MySQL 8.0, InnoDB, `utf8mb4`.
- Một ứng dụng monolith; không dùng frontend SPA, JWT hoặc microservices.

## 3. Vai trò người dùng

- **Sinh viên:** xem đợt và đề tài, tạo/tham gia nhóm, xem đăng ký và kết quả của nhóm mình.
- **Nhóm trưởng:** mời thành viên, đăng ký đề tài và nộp báo cáo.
- **Giảng viên:** đề xuất đề tài, hướng dẫn hoặc chấm các đề tài được phân công.
- **GVPB:** nhập điểm và nhận xét phản biện.
- **Thành viên hội đồng:** nhập điểm và nhận xét hội đồng.
- **Chủ tịch hội đồng:** theo dõi và tổng hợp đánh giá của hội đồng.
- **Trưởng bộ môn:** duyệt nội dung, phân công 1–2 GVHD cho đề tài thuộc bộ môn trong hồ sơ giảng viên của mình.
- **Cán bộ khoa:** công bố đề tài, duyệt đăng ký, phân công phản biện/chấm, tổ chức hội đồng và công bố kết quả.
- **Trưởng khoa:** có quyền của cán bộ khoa và được tạo đợt đăng ký.
- **Quản trị viên:** tạo, khóa và quản lý tài khoản; đăng thông báo.

## 4. Chức năng cần triển khai

### 4.1. Tài khoản và hồ sơ

- Đăng nhập, đăng xuất và đổi mật khẩu.
- Quản trị viên tạo tài khoản; người dùng không tự đăng ký.
- Khóa hoặc mở khóa tài khoản.
- Admin xóa tài khoản khác chưa có hồ sơ SV/GV hoặc dữ liệu liên quan; không tự xóa. Tài khoản đã sử dụng dùng khóa để giữ lịch sử.
- Admin sửa tên đăng nhập, họ tên, email, vai trò, trạng thái và mật khẩu trong cùng màn hình Sửa tài khoản. Để trống cả hai ô mật khẩu thì giữ mật khẩu cũ; nhập mới thì phải xác nhận và hợp lệ. Toàn bộ thay đổi lưu trong một transaction.
- Tên đăng nhập/email không trùng. Đổi tên vẫn giữ nguyên ID và hồ sơ liên quan; phiên cũ cần đăng nhập lại. Admin không tự khóa hoặc bỏ quyền ADMIN.
- Người dùng đổi mật khẩu của chính mình ở mục Đổi mật khẩu, có kiểm tra mật khẩu hiện tại; không dùng màn hình admin để bỏ qua bước này.
- Các ô mật khẩu mặc định che và có nút con mắt hiện/ẩn riêng; không hiển thị lại mật khẩu đã lưu.
- Khi xóa, đổi tên đăng nhập, mật khẩu hoặc quyền, các phiên cũ bị kết thúc ở lần truy cập tiếp theo. Trạng thái chỉ đổi khi admin chọn trạng thái mới; sửa mật khẩu không tự mở khóa.
- Gán một hoặc nhiều vai trò cho tài khoản.
- Quản lý bộ môn, hồ sơ sinh viên và hồ sơ giảng viên. Hồ sơ SV bắt buộc tài khoản, mã SV, lớp hành chính và khóa học; học vị GV được để trống.
- Băm mật khẩu bằng BCrypt.
- Chặn URL và thao tác trái quyền ở backend.

### 4.2. Đợt đăng ký

- Trưởng khoa tạo, sửa, triển khai, kết thúc hoặc hủy đợt.
- Thông tin gồm tên, loại, thời gian GV đăng ký và thời gian SV đăng ký.
- TLCN/KLTN phải có hạn nộp điểm phản biện.
- KLTN phải có ngày báo cáo hội đồng.
- Hai giai đoạn không chồng nhau: gv_ket_thuc <= sv_bat_dau.
- Hệ thống tự kiểm tra đợt đang mở trước khi cho thực hiện nghiệp vụ.

### 4.3. Đề tài và giảng viên hướng dẫn

- Giảng viên đề xuất đề tài trong thời gian được phép.
- Đề tài thuộc đúng một bộ môn. Khi tạo/sửa phải có mã, tên, nội dung, yêu cầu thực hiện và sản phẩm dự kiến.
- Trưởng bộ môn duyệt/từ chối và phân công GVHD trong bộ môn của mình; khoa công bố đề tài.
- Sinh viên chỉ được xem và chọn đề tài đã công bố.
- Mỗi đề tài có từ một đến hai GVHD.
- Người đề xuất không mặc nhiên trở thành GVHD.
- GVHD không được chấm đề tài mình đang hướng dẫn.

### 4.4. Nhóm sinh viên

- Sinh viên tạo nhóm và mặc nhiên trở thành nhóm trưởng. Mã nhóm và tên nhóm bắt buộc; tạo hoặc đổi tên không chấp nhận chỉ dấu cách.
- Nhóm trưởng gửi lời mời; sinh viên được mời phải chấp nhận.
- Nhóm có tối đa năm thành viên và đúng một nhóm trưởng. Nhóm đang lập được ít hơn ba; khi đăng ký và chấp thuận phải đủ 3–5 SV.
- Một sinh viên chỉ thuộc một nhóm trong cùng một đợt.
- Được thay đổi thành viên hoặc nhóm trưởng trước khi đăng ký được chấp thuận.
- Sau khi đăng ký được chấp thuận, thành viên và nhóm trưởng bị khóa thay đổi.

### 4.5. Đăng ký và giữ chỗ đề tài

- Chỉ nhóm trưởng được đăng ký đề tài.
- Mỗi nhóm chỉ có một đăng ký hiện hành.
- Mỗi đề tài chỉ dành cho một nhóm trong một đợt.
- Nhóm gửi đăng ký trước được giữ chỗ trong khi chờ khoa duyệt.
- Nhóm khác không được đăng ký đề tài đang được giữ chỗ.
- Khoa có thể chấp thuận hoặc từ chối đăng ký.
- Nhóm bị từ chối được chọn đề tài khác nếu thời gian đăng ký vẫn còn.
- Đổi đề tài phải thực hiện trong một transaction; nếu đề tài mới đã bị chiếm thì đăng ký cũ vẫn được giữ nguyên.

### 4.6. Báo cáo

- Chỉ nhóm trưởng được nộp báo cáo.
- Một bộ báo cáo bắt buộc có đủ: PDF, slide và mã nguồn.
- Có hạn nộp báo cáo chung theo đợt.
- Được nộp nhiều phiên bản trước hạn.
- Bản hợp lệ cuối cùng được dùng để chấm; không xóa các phiên bản cũ.
- Kiểm tra loại tệp, dung lượng, quyền tải xuống và quyền xem tệp.

### 4.7. Phản biện và hội đồng

- `MON_HOC` và `TLCN`: GVPB riêng.
- `NCKH` và `KLTN`: GVPB trước, sau đó chấm hội đồng.
- Mỗi hội đồng có từ ba đến năm giảng viên.
- Hội đồng có đúng một chủ tịch và một thư ký; các thành viên còn lại là ủy viên.
- Khoa lập hội đồng, thêm thành viên, xếp lịch và phân công đề tài.
- Một giảng viên được chấm nhiều đề tài nhưng không được chấm đề tài mình hướng dẫn.
- GVPB đồng thời là thành viên hội đồng chỉ có một điểm được tính cho đề tài.

### 4.8. Điểm và kết quả

- Mỗi giảng viên chấm nhập một điểm tổng từ 0 đến 10 và nhận xét.
- Điểm cuối là trung bình cộng điểm của các giảng viên hợp lệ.
- Làm tròn đến một chữ số thập phân.
- Kết quả đạt khi điểm cuối từ 4,0 trở lên.
- Chỉ tổng hợp khi đã có đủ phiếu chấm bắt buộc.
- Khoa xác nhận và công bố kết quả.
- Sinh viên tra cứu theo đợt và chỉ xem kết quả của đề tài mình tham gia.
- Sau khi công bố được commit, gửi email HTML nền cho từng SV; demo bằng Mailpit theo Lab 6.
- Kết quả đã công bố được lưu theo phiên bản, không sửa đè.

### 4.9. Sửa điểm

- Giảng viên gửi yêu cầu sửa điểm và nêu lý do.
- Khoa chấp thuận hoặc từ chối yêu cầu.
- Chỉ được nhập phiếu điều chỉnh khi yêu cầu đã được chấp thuận.
- Sau khi tính lại, khoa xác nhận và công bố phiên bản kết quả mới.
- Trong thời gian điều chỉnh, sinh viên vẫn xem kết quả cũ kèm trạng thái “Đang điều chỉnh”.
- Không hỗ trợ bảo vệ hoặc đánh giá lại trong phiên bản này.

### 4.10. Thông báo

- Trưởng khoa, cán bộ khoa và quản trị viên được đăng thông báo.
- Thông báo có thể đính kèm tệp.
- Chỉ người đã đăng nhập được xem.
- Hỗ trợ trạng thái nháp, đã công bố và thu hồi.

### 4.11. Xóa có điều kiện

- Admin xóa bộ môn/hồ sơ SV/GV chưa được sử dụng. FK RESTRICT chặn xóa khi có tham chiếu, kể cả truy cập đồng thời.
- GV chỉ xóa đề tài nháp của mình trong thời gian được sửa. Trưởng khoa chỉ xóa đợt nháp chưa có dữ liệu liên quan.
- Người có quyền xóa thông báo nháp chưa có tệp đính kèm. Thông báo đã công bố dùng thu hồi.
- Giữ nguyên dữ liệu học thuật đã sử dụng; dùng khóa/ngừng hoạt động/hủy khi phù hợp.

## 5. Database

Hệ thống sử dụng 24 bảng nghiệp vụ, chia theo các nhóm:

- Tài khoản và hồ sơ: `bo_mon`, `tai_khoan`, `tai_khoan_vai_tro`, `sinh_vien`, `giang_vien`.
- Đợt và đề tài: `dot_dang_ky`, `de_tai`, `huong_dan`.
- Nhóm: `nhom`, `thanh_vien_nhom`, `loi_moi_nhom`.
- Đăng ký và báo cáo: `dang_ky`, `bao_cao`, `bao_cao_tep`.
- Hội đồng và đánh giá: `hoi_dong`, `thanh_vien_hoi_dong`, `danh_gia`, `phan_cong_cham`.
- Điểm và kết quả: `phieu_cham`, `yeu_cau_sua_diem`, `ket_qua`, `chi_tiet_ket_qua`.
- Thông báo: `thong_bao`, `thong_bao_tep`.

File cấu trúc chuẩn là `src/main/resources/db/migration/V1__khoi_tao_24_bang.sql`. Không sửa trực tiếp V1 sau khi đã chia sẻ project. Mọi thay đổi schema phải tạo migration Flyway mới. V2 bổ sung TRUONG_BO_MON; không có bảng email, vẫn 24 bảng nghiệp vụ.

## 6. Phạm vi không thực hiện

- Đăng ký môn học, tín chỉ, môn tiên quyết và thời khóa biểu.
- Sinh viên tự đề xuất đề tài.
- Một đề tài cho nhiều nhóm.
- Nhóm có hơn năm sinh viên.
- Chấm điểm theo rubric nhiều tiêu chí.
- Bảo vệ lại hoặc đánh giá lại.
- Import Excel hàng loạt.
- Thông báo realtime, hàng đợi email bền vững và ứng dụng di động.
- Frontend React/Vue, JWT, microservices, Kafka hoặc Redis.

## 7. Tiêu chí hoàn thành chức năng

Một chức năng được xem là hoàn thành khi:

- Có entity/repository cần thiết và mapping đúng database.
- Nghiệp vụ nằm trong service và có transaction phù hợp.
- Controller không truy cập repository trực tiếp.
- Có form/DTO riêng và kiểm tra dữ liệu đầu vào.
- Có giao diện Thymeleaf sử dụng được.
- Kiểm tra đủ vai trò và quyền theo bản ghi.
- Hiển thị thông báo dễ hiểu khi thao tác thất bại.
- Có test cho luồng thành công và quy tắc nghiệp vụ quan trọng.
- Chạy `./mvnw clean test` thành công trước khi mở Pull Request.

Quy tắc phân công module, Git, database local và cách mở Pull Request nằm trong `docs/HUONG_DAN_LAM_BACKEND_NHOM.md` của repository.
