# Hướng dẫn phát triển backend — nhóm 4 thành viên

Bản hiện hành ngày 20/10/2026. Các module đã có code và FE đã ghép. Tài liệu này dùng để nhận việc, bảo trì và kiểm thử; không phải kế hoạch giao nhóm viết lại từ đầu.

## 1. Kiến trúc và phân công module

```text
Trình duyệt → Controller + Form/Validation → Service → Repository → MySQL
Trình duyệt ← HTML Thymeleaf ← Model/DTO ← Service
```

- `model`: entity và enum; `repository`: JPA/query/khóa; `service`: nghiệp vụ, quyền bản ghi và transaction.
- `controller`: nhận request, binding/validation, gọi service, đưa DTO vào Model, trả view hoặc redirect. Không gọi repository trực tiếp để thực hiện nghiệp vụ.
- `form`: dữ liệu nhập và annotation validation; `dto`: dữ liệu trả cho view. Không bind thẳng entity từ request.
- View ở `src/main/resources/templates/<module>/`; CSS/JS/logo ở `static/`.
- Constructor injection, không tạo repository/service bằng new. Service chỉ đọc dùng readOnly khi phù hợp; mutation dùng @Transactional. `@Validated` cho kiểm tra đầu vào service, `@Valid` cho form controller.

| Thành viên theo phân công module | Package | Trách nhiệm |
|---|---|---|
| 1 | account, hoso | Tài khoản, phiên đăng nhập, vai trò, hồ sơ, bộ môn |
| 2 | dotdangky, detai | Đợt, đề xuất/duyệt/công bố đề tài, GVHD |
| 3 | nhom, dangky | Lời mời, thành viên, nhóm trưởng, giữ chỗ và duyệt đăng ký |
| 4 | baocao, danhgia, thongbao, email | Báo cáo/tệp, hội đồng, chấm/sửa/công bố điểm, thông báo/email |

Đây là phân chia trách nhiệm kỹ thuật. Bảng phân công trong Word phải phản ánh đúng người thực hiện/review/test thực tế, không coi bảng này là chứng cứ đóng góp. Người dùng hiện là thành viên 1; đã yêu cầu hoàn thiện cả module khác để tích hợp, dự phòng và kiểm thử.

## 2. Database trên máy mới

MySQL local 8.0.46, InnoDB, utf8mb4; giữ strict mode và FOREIGN_KEY_CHECKS. Không xóa schema hệ thống MySQL. Workbench là công cụ quản lý, không phải database server.

Nếu máy chưa có schema, chạy trong kết nối quản trị MySQL:

```sql
CREATE DATABASE quan_ly_de_tai
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'doan_dev'@'localhost' IDENTIFIED BY 'TU_DAT_MAT_KHAU_MANH_CUA_MAY';
GRANT ALL PRIVILEGES ON quan_ly_de_tai.* TO 'doan_dev'@'localhost';
```

Thay mật khẩu mẫu trước khi chạy. Nếu schema/user đã có thì dùng lại, không chạy CREATE lại hoặc đặt lại mật khẩu người khác. Quyền ALL ở đây chỉ trên schema đồ án để Flyway chạy DDL, không phải quyền quản trị toàn server. Không đưa mật khẩu thật vào source.

Chạy HTTP và tạo admin theo [README](../README.md). Mỗi máy tự tạo bộ môn, tài khoản, hồ sơ và dữ liệu thử theo [test web](HUONG_DAN_TEST_CHUC_NANG_WEB.md). Không cần import dump của máy trưởng nhóm.

Nguồn schema: [V1](../src/main/resources/db/migration/V1__khoi_tao_24_bang.sql) + [V2](../src/main/resources/db/migration/V2__bo_sung_truong_bo_mon.sql). Có 24 bảng nghiệp vụ; V2 chỉ thêm giá trị vai trò TRUONG_BO_MON, không thêm bảng. Flyway còn tạo flyway_schema_history. JPA dùng ddl-auto=validate.

- Schema mới: để Flyway tạo bảng. Không chạy thêm SQL khởi tạo vào schema đã có bảng.
- Schema đã có lịch sử: Flyway áp dụng migration còn thiếu và kiểm tra checksum.
- Schema cũ chưa có lịch sử: sao lưu và đối chiếu từng bảng/index/FK trước khi baseline; baseline không tự chứng minh schema đúng.
- Không sửa V1/V2 đã phát hành; thay đổi schema phải có V3 hoặc migration tiếp theo, sau khi cả nhóm thống nhất.
- Không đổi ddl-auto thành update/create để che lỗi mapping. ID hiện map Integer; database INT UNSIGNED không đồng nghĩa Java hỗ trợ toàn bộ dải unsigned.
- Từ điển bảng và ràng buộc xem [Cơ sở dữ liệu](CO_SO_DU_LIEU.md).

## 3. Cấu hình môi trường

| Cách chạy | Profile | Cổng | DB và chứng chỉ |
|---|---|---|---|
| HTTP local | Mặc định | 8080 | DB_USERNAME/DB_PASSWORD; DB_URL có mặc định local |
| HTTPS localhost | local-https | 8443 | MySQL local; localhost.p12 riêng của máy |
| HTTPS tên miền local | local-domain | 8443 | MySQL local; PFX win-acme; hosts trỏ 127.0.0.1 |
| Render | prod | PORT do Render cấp | Aiven; CA công khai trong Docker, HTTPS do Render |

Chỉ bật một profile. Không đặt prod cho local để chữa lỗi kết nối. Không dùng tài khoản Aiven cho profile local-domain. Chứng chỉ, khóa riêng, mật khẩu, uploads, target và cache Maven không theo Git.

`.env.example` chỉ là mẫu biến; Maven không tự nạp `.env`. Nhập biến trong terminal hoặc cấu hình IDE. Mật khẩu web do admin cấp khác mật khẩu database và mật khẩu PFX. HTTPS và cách nhớ mật khẩu một lần mỗi terminal xem [HTTPS local](HTTPS_LOCAL.md); email xem [hướng dẫn email](HUONG_DAN_EMAIL.md).

## 4. Phân quyền và bảo mật cần giữ

- Vai trò lưu trong tai_khoan_vai_tro: ADMIN, TRUONG_KHOA, CAN_BO_KHOA, TRUONG_BO_MON, GIANG_VIEN, SINH_VIEN.
- Nhóm trưởng, GVHD, GVPB, chủ tịch/thư ký/ủy viên là tư cách theo bản ghi, không phải các enum quyền đăng nhập riêng.
- Trưởng bộ môn cần cả GIANG_VIEN + TRUONG_BO_MON và hồ sơ GV có bộ môn. ADMIN không tự có quyền khoa/GV/SV.
- SecurityConfig chặn nhóm URL; @PreAuthorize và service kiểm tra quyền, người sở hữu, bộ môn, thành viên/phân công. Ẩn menu không thay thế kiểm tra backend.
- POST cho mutation, có CSRF. Không chuyển xóa/duyệt/công bố thành GET.
- BCrypt; không thể xem lại mật khẩu đã băm. Admin chỉ đặt mật khẩu mới cho tài khoản khác; đổi mật khẩu của mình phải nhập mật khẩu cũ.
- Sửa thông tin/tên đăng nhập/quyền/trạng thái/mật khẩu cùng một transaction; lỗi thì không lưu một phần. Không tự khóa/xóa/bỏ quyền ADMIN. Không bỏ vai trò SV/GV khi hồ sơ còn tồn tại.
- Filter kiểm tra lại tài khoản ở request tiếp theo; thay đổi mật khẩu, vai trò, trạng thái, tên đăng nhập hoặc xóa có thể kết thúc phiên cũ. Không lưu mật khẩu/token trong localStorage.
- th:text cho dữ liệu người dùng; không tự đổi sang th:utext. Không trả stack trace hoặc thông tin mật khẩu lên view.

## 5. Transaction và thao tác đồng thời

- Một phương thức service public tương ứng một thao tác nghiệp vụ nguyên tử.
- Thứ tự khóa chính ở nhóm/đăng ký: đợt → đề tài cũ/mới theo ID tăng dần → nhóm; đọc lại trạng thái sau khi lấy khóa. Phần đăng ký/nhóm dùng READ_COMMITTED tại các mutation cần thiết.
- Nhận lời mời phải khóa, kiểm tra lại SV đã có nhóm cùng đợt và nhóm còn chỗ; giới hạn hiện tại 5, không dùng quy tắc 3 của bản đề bài cũ.
- Đăng ký trước giữ chỗ trong trạng thái CHO_DUYET hoặc CHAP_THUAN. UNIQUE ở DB là bảo vệ cuối; không chỉ kiểm tra exists trước save.
- Đổi đề tài thất bại phải giữ đăng ký cũ; không hủy đăng ký cũ trong transaction riêng trước khi thử đề tài mới.
- Phần đánh giá dùng QuyTacDanhGia để thống nhất khóa, tư cách và trạng thái. Không tự bổ sung luồng khóa ngược làm tăng nguy cơ deadlock.
- Dữ liệu học thuật dùng FK RESTRICT; xóa chỉ khi chưa sử dụng. Không bật cascade remove để làm nút Xóa chạy được.
- Ghi tệp không rollback cùng MySQL. Luồng nộp dùng transaction và dọn tệp khi thất bại; xem NopBaoCaoService/TepThongBaoService/LuuTepService trước khi sửa.
- Email phát sau commit bằng sự kiện bất biến và @Async. Không truyền entity lazy sang luồng nền, không gửi SMTP bên trong transaction chấm/công bố.

## 6. Quy tắc tệp và thời gian

PDF báo cáo + slide PDF/PPTX + mã nguồn ZIP là bắt buộc cho mỗi phiên bản. Mỗi tệp tối đa 50 MiB; kiểm tra cả nội dung, cấu trúc ZIP và tổng dữ liệu giải nén tối đa 200 MiB. Thông báo hỗ trợ PDF/DOCX/PPTX/ZIP, tối đa 10 tệp/bài. Tệp có khóa nội bộ; tải qua controller kiểm tra quyền, không để trong static.

Thời điểm lưu UTC; form nhập/hiển thị giờ Việt Nam qua ThoiGian. Ngày hội đồng KLTN là ngày địa phương. Không cộng thêm 7 giờ ở JavaScript. Không đổi đồng hồ máy/sửa SQL để vượt điều kiện thời gian khi demo.

## 7. Chạy kiểm thử tự động

Thông thường:

```powershell
.\mvnw.cmd --settings .mvn\settings.xml clean verify
```

settings.xml chỉ đưa Maven cache vào .mvn-repository của project. Nó không chứa mật khẩu DB. Wrapper tải Maven khi cần.

Có ba lớp tích hợp MySQL, mặc định bỏ qua nếu chưa có biến tương ứng:

| Lớp | Biến URL | Schema bắt buộc |
|---|---|---|
| TaiKhoanMySqlTest | TEST_ACCOUNT_DB_URL | quan_ly_de_tai_thu_taikhoan_* |
| NhomDangKyMySqlTest | TEST_DB_URL | quan_ly_de_tai_thu_tv3_* |
| ThanhVienBonMySqlTest | TEST_TV4_DB_URL | quan_ly_de_tai_thu_tv4_* |

Chỉ nhận host 127.0.0.1:3306. Tạo ba schema rỗng khác nhau, cấp tài khoản test quyền trên đúng ba schema. Không dùng quan_ly_de_tai, Aiven hoặc tài khoản chính có dữ liệu thật.

Ví dụ sau khi đã tạo schema và tài khoản test:

```powershell
$env:TEST_ACCOUNT_DB_URL = "jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai_thu_taikhoan_nhom?serverTimezone=UTC"
$env:TEST_DB_URL = "jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai_thu_tv3_nhom?serverTimezone=UTC"
$env:TEST_TV4_DB_URL = "jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai_thu_tv4_nhom?serverTimezone=UTC"
$env:TEST_DB_USERNAME = "tai_khoan_test_cua_may"
$env:TEST_DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host "Mat khau MySQL test" -AsSecureString)).Password
.\mvnw.cmd --settings .mvn\settings.xml verify
Remove-Item Env:TEST_ACCOUNT_DB_URL, Env:TEST_DB_URL, Env:TEST_TV4_DB_URL, Env:TEST_DB_USERNAME, Env:TEST_DB_PASSWORD
```

Test tự tạo fixture; tài khoản fixture không phải tài khoản demo trình duyệt. Test không tự xóa schema; chủ máy dọn đúng schema thử khi không cần. Tệp thử của bộ đánh giá nằm trong target và được test dọn theo đường dẫn cố định.

Test SMTP tùy chọn EmailMailpitTest: đọc biến tại lớp và hướng dẫn email trước khi bật. Không nhầm 1 test skipped với lỗi, nhưng phải ghi đúng phần nào đã chạy.

Nếu môi trường Windows bị kẹt ở Mockito self-attach/pipe, có thể truyền javaagent trực tiếp cho lượt test:

```powershell
.\mvnw.cmd --settings .mvn\settings.xml "-DargLine=-javaagent:.mvn-repository/org/mockito/mockito-core/5.17.0/mockito-core-5.17.0.jar" verify
```

Chỉ dùng lệnh này khi jar Mockito tương ứng đã được tải. Nếu nâng phiên bản dependency thì cập nhật đường dẫn theo phiên bản thực tế. Đây là tùy chọn chạy test, không phải cấu hình Render.

## 8. Làm việc với Git và deploy

Nhánh tích hợp nhóm là develop; không suy ra main đã được cập nhật. Lưu công việc của mình trước khi pull/merge. Không reset --hard hoặc force push nhánh chung để xử lý xung đột.

Quy trình: lấy develop mới → nhánh tính năng → code/review/test → commit đúng file → tích hợp develop → cả nhóm kiểm tra → Manual Deploy trên Render. Auto-Deploy được khuyến nghị tắt trong lúc nhóm tích hợp để tránh cập nhật bản demo khi chưa kiểm tra.

Máy hiện tại từng làm trên feature/tai-khoan-ho-so và đẩy HEAD vào develop. Chỉ dùng `git push origin HEAD:develop` khi đó thực sự là bản tích hợp cả nhóm đã thống nhất; thành viên làm nhánh riêng nên push nhánh riêng và review trước.

Trước khi commit: `git status`, `git diff`, chọn file, `git diff --cached`. Không add .local-https, PFX/P12/key, .env, uploads, target hoặc cache. CA deploy/aiven-ca.pem là chứng chỉ công khai, được phép commit.

## 9. Điều kiện hoàn tất một thay đổi

- Đúng phạm vi chức năng và quyền bản ghi; không dồn nghiệp vụ vào controller/view.
- Có form/DTO phù hợp, validation backend và thông báo dễ hiểu.
- Đối chiếu đường dẫn, input, CSRF, enum với view nếu ảnh hưởng giao diện.
- Test đúng phạm vi thay đổi; các quy tắc transaction quan trọng cần kiểm tra đồng thời.
- Chạy được local, Flyway không lỗi checksum, không ghi database chính khi test tự động.
- Cập nhật tài liệu nghiệp vụ nếu thay quy tắc; ghi rõ môi trường và giới hạn khi báo cáo.
- Người nhận module phải giải thích được luồng, annotation và xử lý lỗi của phần mình trước khi bảo vệ.
