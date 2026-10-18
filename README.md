# Hệ thống quản lý đề tài sinh viên

> **Cập nhật 18/10/2026:** Quy tắc mới theo rubric ở [docs/PHAM_VI_CHUC_NANG.md](docs/PHAM_VI_CHUC_NANG.md): nhóm 3–5 khi đăng ký, hai giai đoạn không chồng nhau, Trưởng bộ môn duyệt/phân công GVHD trong bộ môn. Email đơn giản theo Lab 6 tại [docs/HUONG_DAN_EMAIL.md](docs/HUONG_DAN_EMAIL.md); deploy Render + Aiven tại [docs/TRIEN_KHAI.md](docs/TRIEN_KHAI.md). Các mốc kiểm thử ghi ngày cũ bên dưới là lịch sử, không thay thế kết quả kiểm thử bản mới.


Ứng dụng đồ án của Khoa CNTT, xây dựng bằng Java 21, Spring Boot 3.5, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, Flyway và MySQL 8.

## Yêu cầu môi trường

## Nhận bản code để kiểm thử cùng nhóm

Bản tích hợp để test nằm trên nhánh **develop**:

```powershell
git clone --branch develop https://github.com/hoangdanhnhan792006-gif/quan-ly-de-tai.git
cd quan-ly-de-tai
```

Nếu đã có project, lưu/commit công việc của mình trước khi chuyển nhánh; sau đó `git switch develop` và `git pull --ff-only origin develop`. Không ghi đè thay đổi đang làm của thành viên khác.

Mỗi máy cần MySQL riêng, tạo database rỗng `quan_ly_de_tai` (utf8mb4) và tài khoản có quyền trên database đó theo [hướng dẫn backend](docs/HUONG_DAN_LAM_BACKEND_NHOM.md). Đặt `DB_USERNAME`, `DB_PASSWORD` của máy mình. Git không chứa dữ liệu MySQL, mật khẩu hay tệp đã upload của máy người khác. Lần khởi động đầu trên database rỗng, Flyway chạy V1 và V2; không cần import thêm SQL. Tạo admin theo mục bên dưới, rồi tạo tài khoản/dữ liệu thử theo [hướng dẫn test web](docs/HUONG_DAN_TEST_CHUC_NANG_WEB.md).

Email mặc định tắt. Khi muốn thử email, làm theo [Lab 6 với Mailpit](docs/HUONG_DAN_EMAIL.md). Đây là bản dành cho nhóm kiểm thử, chưa phải xác nhận đã deploy hoặc hoàn tất mọi ca test giao diện.

### Phần mềm cần cài

- JDK 21
- Maven 3.9+
- MySQL 8.0
- Database `quan_ly_de_tai`
- Tài khoản database dành riêng cho ứng dụng, mặc định là `doan_dev`

## Biến môi trường

Không ghi mật khẩu MySQL vào Git. Trước khi chạy, thiết lập:

```powershell
$env:JAVA_HOME = "<duong_dan_thu_muc_JDK_21_tren_may_ban>"
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

Database rỗng: Flyway chạy V1 tạo 24 bảng nghiệp vụ và V2 bổ sung vai trò trưởng bộ môn. Database đã có lịch sử Flyway: chỉ chạy migration còn thiếu. Với database cũ chưa có lịch sử, chỉ dùng baseline sau khi đã đối chiếu cấu trúc; baseline không tự kiểm tra đủ 24 bảng. Hibernate dùng `ddl-auto=validate`, không tự tạo hoặc sửa bảng. Không chỉnh sửa V1 đã áp dụng.

## Cấu trúc package

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

Trong từng module, tách các tầng `model`, `repository`, `service`, `controller`, `dto`, `form` khi có code tương ứng. Entity và enum nằm trong `model`; nghiệp vụ và transaction nằm trong `service`. View Thymeleaf nằm tại `src/main/resources/templates/<module>/`, không nằm trong package Java. Module `account` có thêm `config` và `security` cho cấu hình/filter riêng.

## Tài liệu dành cho nhóm

Đã có luồng tài khoản/hồ sơ → đợt/đề tài/GVHD → nhóm/đăng ký → báo cáo → phản biện/hội đồng → chấm/sửa/công bố kết quả, cùng thông báo có tệp. Cách chạy thử từng vai trò ở mục 10–13 của hướng dẫn backend.

Test thông thường không cần MySQL. Hai bộ tích hợp `NhomDangKyMySqlTest` và `ThanhVienBonMySqlTest` mặc định bỏ qua; chỉ bật với schema thử riêng theo mục 12–13, không dùng database chính.

Báo cáo bắt buộc có PDF, slide PDF/PPTX và source ZIP, mỗi tệp tối đa 50 MiB. Tệp lưu ở `uploads` (hoặc biến `UPLOAD_DIR`); cần giữ thư mục này cùng database khi sao lưu/deploy. Không đặt tệp trong static và không commit uploads lên Git.

- Tổng hợp bối cảnh, tiến độ và việc cần làm; dùng để bàn giao cho thành viên hoặc AI mới: [docs/TONG_HOP_DU_AN.md](docs/TONG_HOP_DU_AN.md).
- Phạm vi và các chức năng cần code: `docs/PHAM_VI_CHUC_NANG.md`.
- Quy tắc phân công, Git, database và Definition of Done: `docs/HUONG_DAN_LAM_BACKEND_NHOM.md`.
