# Kiểm thử bản rubric và Lab 6 — 18/10/2026

## Kết quả đã chạy

- Java 21, Maven Wrapper, MySQL Server 8.0.46.
- `verify`: BUILD SUCCESS, 160 test, 0 failure, 0 error, 0 skipped.
- MySQL integration dùng hai schema thử mới riêng biệt; không sửa database chính.
- Flyway V1 + V2 chạy thành công trên schema rỗng; V1 giữ nguyên.
- Mailpit 1.31.6 chạy riêng, chỉ bind loopback. Nhận đúng 3 email HTML cho sv1/sv2/sv3@example.test; mỗi thư chỉ có một người nhận, UTF-8 đúng và nội dung động được escape.
- Test xác nhận gửi sau commit, không gửi khi rollback, chạy ở luồng nền, lỗi một thư vẫn thử gửi các thư còn lại.
- Không gửi thư ra Internet. Tiến trình Mailpit kiểm thử đã dừng; tài khoản MySQL dùng cho test đã thu hồi.
- Log cục bộ nằm trong `.mvn-repository/lab6-full-test.log` và `.mvn-repository/lab6-result.json` (không đưa cache lên Git).

## Thay đổi được kiểm tra

- Nhóm 3–5 khi đăng ký; nhóm đang thành lập được ít hơn 3; giới hạn 5 khi nhận lời mời đồng thời.
- Mỗi SV một nhóm trong một đợt; giữ chỗ và đổi đề tài an toàn.
- Hai khoảng đăng ký không chồng nhau khi tạo/sửa/triển khai; đợt cũ không mở đăng ký SV trong lúc giai đoạn GV còn diễn ra.
- Trưởng bộ môn duyệt và phân công GVHD trong bộ môn của hồ sơ giảng viên; chặn bộ môn khác.
- Kết quả SV được lọc theo đợt thuộc đăng ký của chính SV.
- Các test đánh giá, báo cáo, sửa điểm, công bố và bảo mật hiện có tiếp tục đạt.

## Database hiện tại và hướng dẫn chuyển tiếp

Database chính tại thời điểm kiểm tra chỉ có V1 trong lịch sử Flyway. Khi khởi động bản mới trên local, Flyway áp dụng V2 để bổ sung giá trị vai trò TRUONG_BO_MON. Không đổi hoặc xóa dữ liệu học thuật cũ. Không có V3/email queue trong bản phát hành này, vẫn 24 bảng nghiệp vụ.

Migration hàng đợi email thử nghiệm ở lượt trước chỉ áp dụng trên schema thử, chưa chạy trên database chính và chưa phát hành; đã bỏ khỏi source khi chuyển sang Lab 6. Không tái sử dụng schema thử cũ đã có migration thử nghiệm đó để chạy ứng dụng.

Admin gán đồng thời GIANG_VIEN và TRUONG_BO_MON, tạo hồ sơ GV có bộ môn phụ trách. Không tự cấp quyền này cho tài khoản có sẵn. Nhóm đã được chấp thuận theo đề cũ không tự bị xóa; đăng ký/chấp thuận mới phải đủ 3–5 SV. Đợt nháp có lịch cũ chồng nhau cần sửa trước khi triển khai.

## Chưa xác nhận

- Chưa chạy Docker build trên máy này; chưa tạo Aiven/Render, chưa có URL online.
- Chưa kiểm chứng email đến Gmail hoặc SMTP cloud.
- Render Free cần giải pháp lưu tệp bền vững và đường gửi email phù hợp trước nghiệm thu đầy đủ online.
- Bộ test không thay thế buổi demo trình duyệt của nhóm với dữ liệu/bối cảnh thực tế.

Đọc HUONG_DAN_EMAIL.md để chạy Mailpit theo Lab 6 và TRIEN_KHAI.md để chuẩn bị Render/Aiven.
