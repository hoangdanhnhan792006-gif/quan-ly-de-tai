# Hướng dẫn phát triển backend cho nhóm 4 thành viên

> **Cập nhật 18/10/2026:** Quy tắc mới theo rubric ở [PHAM_VI_CHUC_NANG.md](PHAM_VI_CHUC_NANG.md): nhóm 3–5 khi đăng ký, hai giai đoạn không chồng nhau, Trưởng bộ môn duyệt/phân công GVHD trong bộ môn. Email đơn giản theo Lab 6 tại [HUONG_DAN_EMAIL.md](HUONG_DAN_EMAIL.md); deploy Render + Aiven tại [TRIEN_KHAI.md](TRIEN_KHAI.md). Các mốc kiểm thử ghi ngày cũ bên dưới là lịch sử, không thay thế kết quả kiểm thử bản mới.


## 1. Kiến trúc thống nhất

**Bổ sung quản lý tài khoản 18/10/2026:** Admin được xóa tài khoản khác chưa có hồ sơ/dữ liệu liên quan và đặt lại mật khẩu tài khoản khác. Nghiệp vụ ở `TaiKhoanService`, form mới `DatLaiMatKhauForm`, giao diện `account/dat-lai-mat-khau.html`. Không tự xóa; muốn đổi mật khẩu chính mình phải nhập mật khẩu cũ ở chức năng cá nhân. FK RESTRICT bảo vệ dữ liệu, không sửa V1/V2. Phiên đăng nhập có dấu kiểm tra mật khẩu để bị kết thúc khi mật khẩu đã đổi; không lưu mật khẩu rõ trong phiên.

Test MySQL phần tài khoản: đặt `TEST_ACCOUNT_DB_URL` theo mẫu `jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai_thu_taikhoan_<ten_rieng>`, cùng `TEST_DB_USERNAME`/`TEST_DB_PASSWORD`. Phải tạo schema rỗng và cấp quyền cho tài khoản test trên đúng schema đó; không dùng database chính. Chạy `TaiKhoanMySqlTest` để kiểm tra xóa kèm vai trò, rollback khi FK chặn, phân quyền và đăng nhập/phiên cũ sau đặt lại mật khẩu. Bỏ biến môi trường thì lớp này được bỏ qua.

Ứng dụng là Spring Boot monolith, tổ chức package theo tính năng. Mỗi tính năng đi theo một luồng hoàn chỉnh:

```text
<module>/
├── model/         Entity và enum của module
├── repository/    Interface truy cập database
├── service/       Nghiệp vụ và transaction
├── controller/    Nhận request, gọi service, trả view
├── dto/           Dữ liệu trả về giao diện
└── form/          Dữ liệu nhập và validation

src/main/resources/templates/<module>/   View Thymeleaf (.html)
src/main/resources/static/               CSS, JavaScript, hình ảnh
```

Tên chuẩn là `model`, `service`, `controller`, `dto`. Với Spring Boot + Thymeleaf, View nằm ở `resources/templates`, không tạo package Java `view`. `repository` và `form` được tách riêng để dễ đọc. Module nào chưa có code ở tầng nào thì chưa cần tạo thư mục trống cho tầng đó.

Module `account` có thêm `config/` cho cấu hình khởi tạo admin và `security/` cho filter kiểm tra phiên. `SecurityConfig` dùng chung vẫn ở package `config`. Test nằm dưới `src/test/java`, chia theo package tương ứng với code được kiểm thử.

- Controller chỉ nhận request, kiểm tra form và gọi service.
- Quy tắc nghiệp vụ và `@Transactional` đặt trong service.
- Repository chỉ truy cập dữ liệu, không chứa nghiệp vụ.
- Entity bảo vệ trạng thái bằng phương thức có tên nghiệp vụ; không tạo public setter cho mọi trường.
- Form nhập liệu dùng Bean Validation. Không bind trực tiếp request vào entity.
- Thymeleaf nhận DTO hoặc dữ liệu chỉ đọc từ controller.

Ví dụ đọc một luồng: `account/controller/TaiKhoanController.java` → `account/service/TaiKhoanService.java` → `account/repository/TaiKhoanRepository.java`, dùng Entity ở `account/model/` và hiển thị HTML tại `templates/account/`.

## 2. Phân công không chồng chéo

| Thành viên | Package sở hữu | Bảng chính | URL sở hữu |
|---|---|---|---|
| 1 | `account`, `hoso` | `tai_khoan`, `tai_khoan_vai_tro`, `bo_mon`, `sinh_vien`, `giang_vien` | `/admin/**`, `/ho-so/**` |
| 2 | `dotdangky`, `detai` | `dot_dang_ky`, `de_tai`, `huong_dan` | `/khoa/dot-dang-ky/**`, `/de-tai/**` |
| 3 | `nhom`, `dangky` | `nhom`, `thanh_vien_nhom`, `loi_moi_nhom`, `dang_ky` | `/sinh-vien/nhom/**`, `/sinh-vien/dang-ky/**`, `/khoa/dang-ky/**` |
| 4 | `baocao`, `danhgia`, `thongbao` | 12 bảng còn lại | `/bao-cao/**`, `/giang-vien/cham-diem/**`, `/khoa/hoi-dong/**`, `/thong-bao/**` |

Các file dùng chung sau do trưởng nhóm quản lý: `pom.xml`, `application.yml`, `SecurityConfig`, `common/**` và `db/migration/**`. Thành viên cần thay đổi các file này phải trao đổi trước khi code.

## 3. Quy tắc Git

```text
main                       Bản ổn định để nộp hoặc trình diễn
develop                    Nhánh tích hợp của nhóm
feature/<ten-ngan-gon>     Một chức năng đang phát triển
fix/<ten-loi>              Một lỗi cần sửa
```

Quy trình cho một chức năng:

1. Cập nhật `develop` mới nhất.
2. Tạo nhánh `feature/...` từ `develop`.
3. Chỉ sửa module được phân công.
4. Chạy `./mvnw clean test` trước khi merge.
5. Merge vào `develop` sau khi một thành viên khác đọc lại code.
6. Chỉ merge `develop` vào `main` khi ứng dụng khởi động được với MySQL thật.

Không commit mật khẩu, `.env`, `application-local.yml`, thư mục `uploads`, `target` hoặc `.mvn-repository`.

## 4. Quy tắc database và Flyway

- `V1__khoi_tao_24_bang.sql` là cấu trúc gốc đã kiểm thử. Không chỉnh sửa V1 sau khi nhóm bắt đầu chia sẻ database.
- Khi thật sự cần đổi schema, trưởng nhóm tạo migration tuần tự tiếp theo như `V2__them_cot_x.sql`.
- Không dùng `spring.jpa.hibernate.ddl-auto=update`. Giá trị bắt buộc là `validate`.
- Không tắt foreign key, unique constraint hoặc check constraint để làm cho code chạy tạm.
- Xử lý race condition trong service bằng `@Transactional`, khóa bi quan tại repository khi cần và UNIQUE constraint của database.
- Khi UNIQUE constraint từ chối một thao tác đồng thời, service phải chuyển lỗi thành `NghiepVuException` với thông báo dễ hiểu.

### Database trên máy của từng thành viên

Mỗi thành viên sử dụng một MySQL Server và một database local trên máy của mình. Không cấu hình cả nhóm cùng kết nối vào database trên máy của một thành viên vì dữ liệu thử nghiệm, thao tác xóa và migration có thể ảnh hưởng lẫn nhau.

Sau khi clone project, tạo một database rỗng trong MySQL Workbench:

```sql
CREATE DATABASE quan_ly_de_tai
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;
```

Mỗi người tự cấu hình tài khoản MySQL của máy mình bằng biến môi trường:

```powershell
$env:DB_USERNAME = "ten_tai_khoan_mysql"
$env:DB_PASSWORD = "mat_khau_mysql"
```

Sau đó chạy:

```powershell
.\mvnw.cmd spring-boot:run
```

Nếu `quan_ly_de_tai` đang rỗng, Flyway sẽ tự chạy `V1__khoi_tao_24_bang.sql` và tạo đủ 24 bảng nghiệp vụ. Không cần import thêm một file SQL riêng. Nếu database đã có sẵn đúng 24 bảng, Flyway sẽ ghi nhận baseline phiên bản 1 và không tạo lại bảng.

Các quy tắc bắt buộc:

- Chỉ tạo database rỗng; không tự sửa cấu trúc bảng bằng giao diện Workbench.
- Không sửa, đổi tên hoặc xóa `V1__khoi_tao_24_bang.sql`.
- Không gửi file dump chứa dữ liệu thử nghiệm lên repository nếu chưa được nhóm thống nhất.
- Không commit mật khẩu MySQL, file `.env`, `application-local.yml` hoặc `application-local.properties`.
- Dữ liệu thử nghiệm của mỗi thành viên có thể khác nhau; cấu trúc bảng phải giống nhau và được Flyway quản lý.
- Khi cần thay đổi cấu trúc database, báo trưởng nhóm để tạo migration mới và kiểm tra trên database rỗng trước khi merge.
- Trước khi mở Pull Request, kiểm tra ứng dụng khởi động được và Flyway không báo lỗi validation hoặc checksum.

## 5. Quy tắc transaction

- Phương thức chỉ đọc dùng `@Transactional(readOnly = true)`.
- Mỗi thao tác nghiệp vụ ghi dữ liệu là một public service method có `@Transactional`.
- Không mở transaction trong controller.
- Không gọi API hoặc ghi file vật lý giữa một transaction database kéo dài.
- Đăng ký/đổi đề tài phải khóa bản ghi đề tài trước khi kiểm tra chỗ và ghi đăng ký.
- Việc thêm thành viên phải khóa nhóm và kiểm tra lại giới hạn 3 thành viên trong cùng transaction.

## 6. Phân quyền

Các vai trò cố định: `ADMIN`, `TRUONG_KHOA`, `CAN_BO_KHOA`, `GIANG_VIEN`, `SINH_VIEN`.

Controller dùng quyền tổng quát, ví dụ:

```java
@PreAuthorize("hasAnyRole('TRUONG_KHOA', 'CAN_BO_KHOA')")
```

Quyền theo bản ghi phải kiểm tra trong service. Ví dụ, có vai trò sinh viên chưa đủ để nộp báo cáo; service còn phải xác nhận sinh viên là nhóm trưởng của đúng nhóm.

Không dựa vào việc ẩn nút trên giao diện để bảo vệ nghiệp vụ.

## 7. Quy ước lỗi và validation

- Không tìm thấy dữ liệu: ném `KhongTimThayException`.
- Vi phạm nghiệp vụ: ném `NghiepVuException`.
- Sai định dạng form: dùng Jakarta Bean Validation và trả lại chính form kèm lỗi.
- Không bắt `Exception` chung trong controller.
- Không hiển thị stack trace hoặc nội dung SQL cho người dùng.

## 8. Definition of Done cho một chức năng

Một chức năng chỉ hoàn thành khi có đủ:

- Entity/repository cần thiết và mapping khớp DDL.
- Service chứa đầy đủ kiểm tra nghiệp vụ và transaction.
- Controller cùng form/DTO riêng.
- Màn hình Thymeleaf hoạt động ở cả trường hợp thành công và lỗi.
- Phân quyền cho URL và kiểm tra quyền theo dữ liệu.
- Ít nhất một test cho luồng thành công và một test cho quy tắc quan trọng.
- `./mvnw clean test` thành công.
- Không có mật khẩu hoặc đường dẫn riêng của máy trong commit.

## 9. Thứ tự tích hợp đề xuất

1. Tài khoản, hồ sơ và dữ liệu bộ môn.
2. Đợt đăng ký, đề tài và hướng dẫn.
3. Nhóm, lời mời và đăng ký giữ chỗ.
4. Báo cáo và tệp đính kèm.
5. Hội đồng, phân công chấm và phiếu chấm.
6. Tổng hợp, sửa điểm, công bố kết quả và thông báo.

Các module có thể code song song, nhưng chỉ tích hợp màn hình nghiệp vụ khi module phụ thuộc phía trước đã có interface/service ổn định.

## 10. Phần thành viên 1 đã triển khai

Phần này dùng Spring MVC + Thymeleaf, đăng nhập bằng session của Spring Security. Chưa cần REST API hoặc JWT. Luồng xử lý: **Form → Controller → Service → Repository → MySQL**; controller chỉ chuyển dữ liệu và hiển thị lỗi, service kiểm tra nghiệp vụ.

| Chức năng | Đường dẫn | Quyền |
|---|---|---|
| Danh sách, tạo, sửa thông tin và vai trò; khóa/mở khóa tài khoản | `/admin/tai-khoan` | `ADMIN` |
| Danh sách, tạo, sửa và ngừng hoạt động bộ môn | `/admin/bo-mon` | `ADMIN` |
| Danh sách, tạo và sửa hồ sơ sinh viên | `/admin/sinh-vien` | `ADMIN` |
| Danh sách, tạo và sửa hồ sơ giảng viên, gán bộ môn | `/admin/giang-vien` | `ADMIN` |
| Xem hồ sơ của chính mình | `/ho-so` | Người đã đăng nhập |
| Đổi mật khẩu bằng mật khẩu hiện tại | `/ho-so/doi-mat-khau` | Người đã đăng nhập |

**Quy tắc đã xử lý:**

- Tên đăng nhập, email, mã SV/GV và mã bộ môn không trùng. Một tài khoản có tối đa một hồ sơ của mỗi loại; phải có đúng vai trò trước khi tạo hồ sơ.
- Khi sửa hồ sơ không được chuyển sang tài khoản khác. Khi đã có hồ sơ SV/GV thì phải giữ vai trò tương ứng.
- Admin không được tự khóa hoặc tự bỏ quyền `ADMIN`. Khóa tài khoản hoặc đổi vai trò sẽ kết thúc phiên cũ ở yêu cầu tiếp theo.
- Mật khẩu được băm BCrypt; mật khẩu mới tối thiểu 8 ký tự, tối đa 72 byte UTF-8. Đổi thành công sẽ đăng xuất phiên đang dùng để đăng nhập lại.
- Không xóa tài khoản/bộ môn có dữ liệu liên quan; dùng khóa tài khoản hoặc ngừng hoạt động bộ môn. Bộ môn ngừng hoạt động vẫn giữ hồ sơ GV cũ nhưng không nhận phân công mới.
- Form ghi dữ liệu có CSRF. Service kiểm tra quyền, không chỉ ẩn nút trên giao diện. Trang hồ sơ lấy người dùng từ phiên đăng nhập, không nhận ID người khác từ URL.

**Các file nên đọc để hiểu cách làm:**

1. `account/controller/TaiKhoanController.java` và `account/form/TaiKhoanForm.java`: nhận form, validation và trả giao diện.
2. `account/service/TaiKhoanService.java`: kiểm tra tài khoản, vai trò, mật khẩu và transaction.
3. `hoso/service/HoSoService.java`: liên kết tài khoản với hồ sơ, kiểm tra vai trò và bộ môn.
4. `account/security/KiemTraTaiKhoanFilter.java`: cập nhật hiệu lực khóa tài khoản/đổi quyền cho phiên đang đăng nhập.
5. `src/test/java/com/khoacntt/quanlydetai/account` và `hoso`: kiểm thử service và màn hình MVC.

**Chạy thử:**

1. Khởi động lại ứng dụng bằng cấu hình MySQL hiện có; đăng nhập tài khoản `ADMIN` đã tạo. Không cần tạo lại database hoặc import SQL.
2. Vào **Bộ môn** tạo một bộ môn; vào **Tài khoản** tạo tài khoản có vai trò `SINH_VIEN` hoặc `GIANG_VIEN`.
3. Vào **Sinh viên/Giảng viên** tạo hồ sơ cho tài khoản vừa tạo; thử sửa và nhập trùng mã để kiểm tra thông báo lỗi.
4. Mở cửa sổ ẩn danh, đăng nhập tài khoản mới để xem **Hồ sơ của tôi**, đổi mật khẩu; xác nhận không truy cập được `/admin/tai-khoan`.
5. Từ cửa sổ admin khóa tài khoản đó; tải lại trang ở cửa sổ còn lại để kiểm tra phiên đã bị kết thúc.

Chạy bộ kiểm thử trong thư mục project:

```powershell
.\mvnw.cmd --settings .mvn\settings.xml clean test
```

Kiểm thử tự động dùng mock, không ghi vào database cá nhân. Phần này cũng đã chạy thử HTTP với MySQL 8.0.46 trên schema riêng: tạo tài khoản/hồ sơ, kiểm tra trùng, phân quyền, CSRF, đổi mật khẩu, khóa/mở khóa và thay đổi vai trò. Migration V1 và cấu trúc 24 bảng được giữ nguyên.

## 11. Phần thành viên 2 đã triển khai

Bản local này có đầy đủ phần đợt đăng ký, đề tài và GVHD để dùng dự phòng theo yêu cầu của thành viên 1. Chưa tự push hoặc thay thế nhánh của thành viên 2; khi tích hợp cần so sánh hai bản và chọn một bản thống nhất.

| Chức năng | Đường dẫn | Quyền |
|---|---|---|
| Xem danh sách và chi tiết đợt | `/khoa/dot-dang-ky` | Trưởng khoa, cán bộ khoa |
| Tạo/sửa đợt nháp, triển khai, kết thúc, hủy | `/khoa/dot-dang-ky` | Trưởng khoa |
| Xem và lọc đề tài công bố theo đợt/bộ môn | `/de-tai` | Đã đăng nhập |
| Xem đề tài mình đề xuất hoặc được hướng dẫn | `/de-tai/cua-toi` | Giảng viên |
| Tạo/sửa đề xuất, gửi duyệt, gửi lại sau từ chối | `/de-tai/tao`, `/de-tai/{id}` | GV đề xuất |
| Duyệt/từ chối, phân công/thay GVHD, công bố | `/de-tai/quan-ly`, `/de-tai/{id}` | Trưởng khoa, cán bộ khoa |

**Quy tắc của bản triển khai:**

- Trưởng khoa sửa lịch/loại đợt khi còn `NHAP`; triển khai rồi thì không sửa. `KET_THUC` đóng nghiệp vụ ngay khi thao tác; chưa có mở lại. Chỉ hủy đợt nháp/đang triển khai và không có đăng ký `CHO_DUYET`/`CHAP_THUAN`.
- GV chỉ tạo, sửa và gửi đề xuất trong đợt `TRIEN_KHAI`, từ thời điểm bắt đầu (bao gồm) đến trước thời điểm kết thúc. Chỉ sửa/gửi đề tài của chính mình ở trạng thái `NHAP` hoặc `TU_CHOI`; không chuyển sang đợt khác.
- Hai khoảng GV/SV được chồng nhau. Hạn báo cáo không trước hạn SV đăng ký; hạn điểm PB bắt buộc riêng TLCN/KLTN và không trước hạn báo cáo; ngày hội đồng cấp đợt bắt buộc riêng KLTN và không trước ngày hạn điểm PB. Trường không áp dụng phải bỏ trống.
- Khoa duyệt khi đợt còn triển khai, kể cả đã hết thời gian GV đề xuất. Từ chối phải có lý do. Gửi lại sẽ xóa thông tin lần duyệt cũ; bản này không lưu lịch sử mọi lần duyệt đề xuất.
- Phân công sau khi duyệt: 1–2 GV có tài khoản hoạt động. Không tự gán người đề xuất làm GVHD. Có thể đổi danh sách GVHD sau công bố khi đợt còn triển khai; thay đổi thực hiện trong một transaction.
- Chặn GV đã có phân công chấm hoặc thuộc hội đồng của đề tài trở thành GVHD. Công bố phải có đủ 1–2 GVHD hợp lệ và bộ môn đang hoạt động.
- Đề tài chưa công bố chỉ khoa, người đề xuất và GVHD được xem. Đề tài của đợt hủy không xuất hiện trong danh sách công bố; đợt kết thúc vẫn xem được để tra cứu.
- `ADMIN` không mặc nhiên có quyền khoa hoặc giảng viên. Muốn thử từng luồng phải tạo tài khoản đúng vai trò; giảng viên phải có hồ sơ GV.

**Chạy thử trên web:**

1. Khởi động lại Spring Boot. Admin tạo bộ môn, tài khoản trưởng khoa/cán bộ khoa, 2 tài khoản GV kèm hồ sơ và một tài khoản SV. Dùng các cửa sổ trình duyệt/ẩn danh riêng hoặc đăng xuất khi đổi vai trò.
2. Trưởng khoa vào **Đợt đăng ký**, tạo đợt `MON_HOC`; đặt giờ GV bắt đầu trước hiện tại, kết thúc sau hiện tại; nhập các hạn hợp lệ và bấm **Triển khai**.
3. GV vào **Đề tài của tôi → Đề xuất đề tài**, lưu nháp rồi gửi duyệt.
4. Khoa vào **Quản lý đề tài**, thử từ chối có lý do; GV sửa và gửi lại; khoa chấp thuận.
5. Khoa phân công 1–2 GVHD rồi công bố. Thử công bố khi chưa có GVHD để xem lỗi nghiệp vụ.
6. SV vào **Đề tài**, lọc theo đợt/bộ môn và xem chi tiết. Tiếp tục chức năng nhóm/đăng ký ở mục 12.

**File cần đọc và lưu ý khi tích hợp:**

- Hai package `dotdangky` và `detai` đều có `model`, `repository`, `service`, `controller`, `form`, `dto`. View ở `templates/dotdangky` và `templates/detai`.
- Nghiệp vụ nằm trong `DotDangKyService`/`DeTaiService`. Form validation kiểm tra đầu vào; controller nhận form, gọi service và trả trang/flash message. Không gọi repository từ controller.
- File dùng chung đã bổ sung/sửa: `ClockConfig`, `common/time/ThoiGian`, `SecurityConfig`, `DieuHuongAdvice`, navigation, CSS và `application.yml`.
- Giờ nhập/hiển thị là `Asia/Ho_Chi_Minh`; `DATETIME` trong DB lưu giá trị UTC. Dùng `ThoiGian` để chuyển đổi, `Clock` để lấy giờ. Hikari đặt session MySQL UTC; Hibernate dùng `java_time_use_direct_jdbc` để tránh chuyển lệch thêm 7 giờ khi ghi `LocalDateTime`. Không tự chuyển đổi các dữ liệu lịch sử đang có.
- Thứ tự khóa hiện tại: **đợt → đề tài**. Thành viên 3/4 cần dùng cùng thứ tự trong transaction, đọc lại điều kiện sau khi khóa. Nếu khóa nhiều đề tài thì theo ID tăng dần. Thành viên 4 đã chặn chiều ngược: không thêm GVHD vào phân công chấm/hội đồng; xem mục 13.
- Truy vấn kiểm tra đăng ký/chấm/hội đồng dùng native SQL trên 24 bảng hiện có; không cần thêm bảng hay sửa migration V1.

**Kiểm thử:** bộ test của dự án gồm 85 ca, trong đó phần mới có 12 ca service đợt, 18 ca service đề tài và 13 ca MVC/phân quyền. Chạy bằng lệnh `clean test` ở mục 10. Đã kiểm tra HTTP + MySQL 8.0.46 trên schema riêng: đủ 4 loại đợt, vòng từ chối/gửi lại/duyệt, thay GVHD, công bố, quyền xem, giờ UTC, chặn xung đột chấm/hội đồng và hai yêu cầu công bố đồng thời chỉ một thành công. Kiểm thử này không thay cho kiểm thử giữ chỗ/đăng ký của thành viên 3.

## 12. Phần thành viên 3 đã triển khai

Module `nhom` và `dangky` đã có đầy đủ Entity, Repository, Service, Controller, Form/DTO và 6 trang Thymeleaf. Không thêm bảng, không sửa migration V1.

| Chức năng | Đường dẫn | Quyền |
|---|---|---|
| Nhóm của tôi, lời mời nhận được, lịch SV đăng ký đang mở | `/sinh-vien/nhom` | Sinh viên có hồ sơ |
| Tạo nhóm, đổi tên, mời/thu hồi, chuyển trưởng nhóm, đưa thành viên ra, giải thể | `/sinh-vien/nhom/{id}` | Nhóm trưởng |
| Chấp nhận/từ chối lời mời | `/sinh-vien/nhom/loi-moi/{id}/phan-hoi` | Đúng SV nhận lời mời |
| Rời nhóm | `/sinh-vien/nhom/{id}/roi` | Thành viên; trưởng nhóm phải chuyển quyền hoặc giải thể |
| Chọn/đổi/hủy đăng ký đề tài | `/sinh-vien/dang-ky/{nhomId}` | Nhóm trưởng |
| Xem đăng ký, xem thành viên nhóm, chấp thuận/từ chối | `/khoa/dang-ky` | Trưởng khoa, cán bộ khoa |

**Quy tắc đã xử lý:**

- Tạo/sửa nhóm và đăng ký trong khoảng SV đăng ký của đợt `TRIEN_KHAI`, tính theo giờ máy chủ: gồm mốc mở, không gồm mốc đóng. Mã nhóm duy nhất trong đợt; người tạo là nhóm trưởng. Tên nhóm có thể sửa; không chuyển nhóm sang đợt khác.
- Mỗi SV chỉ thuộc một nhóm trong cùng đợt, nhưng được tham gia đợt khác. Tài khoản phải hoạt động và có hồ sơ SV. Người ngoài nhóm không xem được chi tiết; khoa được xem để duyệt.
- Nhóm có 1–3 SV và đúng một trưởng nhóm. Lời mời chưa đồng ý không chiếm vị trí thành viên. Không gửi trùng lời mời đang chờ, không mời SV đã có nhóm trong đợt.
- Khi SV vào/tạo nhóm, các lời mời khác của SV trong cùng đợt mất hiệu lực. Khi đủ 3 người, đổi trưởng, giải thể hoặc được chấp thuận, các lời mời còn chờ của nhóm mất hiệu lực. Sau hạn đăng ký không thể chấp nhận lời mời dù dòng lịch sử còn ghi chờ.
- Chuyển trưởng chỉ cho thành viên hiện tại; bỏ cờ trưởng cũ và flush trước khi đặt cờ mới, tất cả trong một transaction. Không có thời điểm commit mà nhóm thiếu hoặc có hai trưởng.
- Nhóm đang chờ duyệt vẫn đổi thành viên/trưởng được. Khoa chấp thuận thì khóa đổi tên/thành viên/trưởng, rời/giải thể, đổi/hủy đề tài. Khoa vẫn được duyệt sau hạn SV nếu đợt còn triển khai.
- Đề tài phải cùng đợt, đã công bố và có 1–2 GVHD hoạt động. Chỉ nhóm trưởng đăng ký. Đề tài có nhóm `CHO_DUYET`/`CHAP_THUAN` thì nhóm khác không chiếm được chỗ.
- Đổi đề tài cập nhật đúng dòng đăng ký hiện hành trong một transaction. Hết chỗ hoặc lỗi ràng buộc thì rollback, giữ nguyên đăng ký cũ. Từ chối bắt buộc có lý do và trả chỗ; nhóm được chọn lại khi còn hạn.
- Form khoa gửi kèm ID đề tài đã xem. Nếu nhóm vừa đổi đề tài, yêu cầu duyệt cũ bị từ chối và yêu cầu tải lại. Khoa kiểm tra lại thành viên/GVHD khi chấp thuận.
- Hủy đăng ký chỉ áp dụng `CHO_DUYET`. Giải thể trước chấp thuận hủy đăng ký giữ chỗ, kết thúc lời mời và xóa liên kết thành viên; giữ dòng nhóm/đăng ký để tra cứu.
- Trạng thái nhóm: `DANG_LAP` khi tạo/từ chối/hủy đăng ký; `DA_CHOT` khi chờ duyệt (chưa khóa thành viên); `DANG_THUC_HIEN` khi chấp thuận; `GIAI_THE` khi giải thể. `HOAN_THANH` dành cho giai đoạn kết quả của thành viên 4.

**Transaction và tích hợp:**

- `QuyTacNhom` chứa các kiểm tra dùng chung; `NhomService` xử lý nhóm/lời mời; `DangKyService` xử lý giữ chỗ và duyệt. Controller không gọi repository.
- Thứ tự khóa ghi: **đợt → đề tài cũ/mới theo ID tăng dần → nhóm → xử lý đăng ký/thành viên/lời mời**. Các thao tác trong cùng đợt được tuần tự hóa bằng khóa đợt; phù hợp quy mô đồ án, chưa tối ưu cho tải đăng ký lớn.
- Phương thức ghi của thành viên 3 và `DeTaiService` dùng `READ_COMMITTED`: sau khi chờ khóa, đọc được dữ liệu vừa commit. Vẫn giữ UNIQUE ở MySQL làm lớp bảo vệ cuối.
- Phần thành viên 4 đã dùng cùng thứ tự khóa, kiểm tra đăng ký chấp thuận và tư cách trưởng nhóm khi nộp; chỉ chuyển nhóm sang HOAN_THANH lúc công bố kết quả.
- File chung cập nhật: `SecurityConfig`, `DieuHuongAdvice`, menu, thêm `findByMaSv` vào `SinhVienRepository`, mức cô lập cho thao tác ghi của `DeTaiService`.

**Chạy thử bằng trình duyệt:**

1. Dùng phần thành viên 1 tạo ít nhất 4 tài khoản SV kèm hồ sơ, một GV kèm hồ sơ và trưởng khoa/cán bộ khoa.
2. Dùng phần thành viên 2 triển khai đợt đang trong thời gian SV đăng ký; duyệt, phân công GVHD và công bố ít nhất 2 đề tài.
3. SV thứ nhất vào **Nhóm và đăng ký → Tạo nhóm**, mời SV thứ hai theo mã SV. SV thứ hai đăng nhập riêng, đồng ý ở trang **Nhóm và đăng ký**.
4. Nhóm trưởng vào chi tiết nhóm, **Chọn / Đổi đề tài**. Thử tạo nhóm khác và chọn cùng đề tài để thấy thông báo hết chỗ.
5. Khoa vào **Duyệt đăng ký**, mở mã nhóm để kiểm tra thành viên; thử từ chối có lý do rồi để nhóm đăng ký lại.
6. Khoa chấp thuận; tải lại trang nhóm để thấy các nút thay đổi đã khóa. Thử gửi lại URL/form cũ cũng bị backend chặn.

**Kiểm thử tự động và MySQL thật:**

- 120 test unit/MVC: 85 ca cũ, 26 ca nghiệp vụ nhóm/đăng ký và 9 ca MVC/phân quyền mới.
- 4 test tích hợp trong `nhom/integration/NhomDangKyMySqlTest.java`: tranh một đề tài, hai lời mời cho cùng SV, nhiều chấp nhận khiến nhóm có nguy cơ vượt 3 người, đổi đề tài thất bại giữ chỗ cũ.
- Test tích hợp mặc định **bỏ qua** nếu không có `TEST_DB_URL`; chạy `clean test` thông thường không ghi MySQL. Lần kiểm tra cuối ngày 01/10/2026: `clean test` bật MySQL đạt **124/124**, không failure/error/skipped; đã xác minh riêng khi bỏ biến môi trường thì 4 ca tích hợp được bỏ qua. Luồng HTTP và race duyệt/sửa thành viên, tạo nhóm đồng thời cũng đạt. Schema thử của lần này đã được dọn, database chính giữ nguyên.
- Để chạy lại test tích hợp, tự tạo schema thử **rỗng** ở MySQL local, ví dụ `quan_ly_de_tai_thu_tv3_nhom`, cấp quyền cho tài khoản test trên đúng schema đó. Flyway tự tạo 24 bảng. Test chỉ chấp nhận URL local `127.0.0.1:3306` và tiền tố schema `quan_ly_de_tai_thu_tv3_`; từ chối URL database chính.
- Test tạo dữ liệu có mã riêng mỗi lần chạy, không tự xóa schema. Các tài khoản fixture không dùng để đăng nhập web. Sau test, chỉ dọn đúng schema thử do mình tạo.

```powershell
$env:TEST_DB_URL = "jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai_thu_tv3_nhom?serverTimezone=UTC"
$env:TEST_DB_USERNAME = "<TAI_KHOAN_MYSQL_CO_QUYEN_TREN_SCHEMA_THU>"
$env:TEST_DB_PASSWORD = "<MAT_KHAU_TREN_MAY_BAN>"
.\mvnw.cmd --settings .mvn\settings.xml "-Dtest=NhomDangKyMySqlTest" test
Remove-Item Env:TEST_DB_URL, Env:TEST_DB_USERNAME, Env:TEST_DB_PASSWORD
```

Phần thành viên 3 hoàn thành đến bước nhóm được khoa chấp thuận. Luồng tiếp theo của thành viên 4 đã có ở mục 13. Code đang local; cần review và thống nhất với đồng đội trước khi commit/push/tích hợp.

## 13. Phần thành viên 4 đã triển khai

Ba module `baocao`, `danhgia`, `thongbao` có Entity, Repository, Service, Controller, Form/DTO và 11 trang Thymeleaf. Nghiệp vụ nằm trong Service. Giữ nguyên 24 bảng và migration V1, không cần tạo lại database chính.

### Chức năng và đường dẫn

| Chức năng | Đường dẫn | Quyền |
|---|---|---|
| Danh sách/bộ báo cáo và lịch sử phiên bản | `/bao-cao`, `/bao-cao/dang-ky/{id}` | Nhóm thực hiện, GVHD, GV chấm được giao, khoa |
| Nộp đủ ba tệp trong một phiên bản | Trang chi tiết báo cáo | Chỉ nhóm trưởng, đăng ký đã chấp thuận, trước hạn |
| Tạo/sửa hội đồng nháp, chốt thành viên, hoàn thành | `/khoa/hoi-dong` | Trưởng khoa/cán bộ khoa |
| Lập/sửa kế hoạch, mở phản biện, mở hội đồng | `/danh-gia`, `/khoa/danh-gia/tao` | Trưởng khoa/cán bộ khoa |
| Nhập nháp/nộp phiếu, xin sửa điểm | `/danh-gia/{id}` | GV có phân công của chính mình |
| Duyệt sửa, xác nhận và công bố kết quả | `/danh-gia/{id}` | Trưởng khoa/cán bộ khoa |
| Tổng hợp kết quả | `/danh-gia/{id}` | Chủ tịch hội đồng; khoa nếu chỉ có GVPB riêng |
| Xem kết quả đã công bố | `/sinh-vien/ket-qua` | SV thuộc nhóm thực hiện |
| Xem thông báo | `/thong-bao` | Người đã đăng nhập |
| Tạo/sửa, đính kèm, công bố/thu hồi thông báo | `/thong-bao/quan-ly` | Khoa quản lý bài mình đăng; admin quản lý mọi bài |

`ADMIN` không mặc nhiên có quyền khoa/GV/SV. Chủ tịch, thư ký và GVPB là phân công nghiệp vụ của tài khoản GV, không phải vai trò tài khoản mới.

### Quy tắc triển khai cần nắm

- Nộp báo cáo gồm **PDF + slide PDF/PPTX + mã nguồn ZIP**, cả ba bắt buộc trong mỗi lần nộp. Mỗi tệp tối đa 50 MiB; cấu hình request 155 MiB để chừa phần multipart cho ba tệp. Trước hạn được nộp phiên bản mới; giữ bản cũ.
- Chỉ mở phản biện khi **đã đến hạn báo cáo và giờ phản biện**. Khi mở, chốt bản nộp cuối; PB và hội đồng dùng cùng bản. Không sửa kế hoạch sau khi bắt đầu chấm.
- `MON_HOC/TLCN`: một GVPB. `NCKH/KLTN`: một GVPB trước, hội đồng sau; chỉ mở hội đồng khi PB hoàn tất và đã đến giờ hội đồng.
- Hội đồng có 3–5 GV khác nhau: một chủ tịch, một thư ký, 1–3 ủy viên. Chỉ sửa thành phần khi nháp; chốt rồi thì khóa. Chỉ hoàn thành hội đồng khi mọi đề tài được giao đã chấm xong hội đồng.
- GVHD không được làm GVPB/thành viên hội đồng chấm chính đề tài. Module đề tài cũng chặn đổi người chấm thành GVHD.
- KLTN phải xếp hội đồng đúng ngày báo cáo cấp đợt. TLCN/KLTN phải nộp phiếu PB ban đầu **trước** hạn điểm PB. Môn học/NCKH không có hạn điểm PB riêng.
- Chặn trùng lịch GV giữa các đợt: trùng thời điểm PB, PB nằm trong giờ hội đồng, hai khoảng hội đồng giao nhau. Chặn trùng tên địa điểm hội đồng trong cùng đợt. PB hiện có một thời điểm, chưa có khoảng thời lượng; chưa quản lý phòng dùng chung toàn trường.
- Mỗi GV một phân công/điểm cho đề tài, kể cả kiêm PB và hội đồng. Phiếu nộp cần điểm 0–10 (tối đa 2 chữ số thập phân) và nhận xét; nháp chưa được tính điểm.
- Tổng hợp phải đủ mọi người chấm duy nhất. Cột generated MySQL tính `ROUND(tong_diem / so_nguoi_cham, 1)`; đạt khi điểm đã làm tròn >= 4.0. `chi_tiet_ket_qua` giữ đúng phiên bản phiếu nguồn.
- Khoa xác nhận rồi công bố; lần công bố đầu chuyển nhóm sang `HOAN_THANH`. Sinh viên không xem điểm nháp/kết quả chưa công bố hoặc kết quả của nhóm khác.
- Sửa điểm: GV xin sửa → khoa khác người yêu cầu duyệt → mở phiếu phiên bản mới → GV nộp → chủ tịch/khoa tổng hợp → khoa xác nhận/công bố lại. Không được tự duyệt yêu cầu của mình dù có nhiều vai trò.
- Khi còn yêu cầu chờ duyệt/được sửa nhưng chưa nộp thì không tổng hợp/công bố. Chấp thuận sửa sẽ hủy kết quả chưa công bố để tránh dùng nguồn điểm cũ. Bản đã công bố giữ nguyên; SV tiếp tục thấy bản đó kèm “Đang điều chỉnh” đến khi công bố lại. Được sửa qua quy trình này khi đợt đã kết thúc.
- GV được hủy yêu cầu còn chờ duyệt/đã duyệt nhưng chưa nộp điều chỉnh; khoa từ chối phải có lý do. Không sửa đè phiếu đã nộp, không bảo vệ lại.
- Thông báo phải thu hồi trước khi sửa; hỗ trợ công bố lại. Tối đa 10 tệp/bài, định dạng PDF/PPTX/DOCX/ZIP. Bài nháp/thu hồi và tệp của bài chỉ người đăng/admin xem được.

### Tệp và transaction

- `UPLOAD_DIR` tùy chọn; mặc định thư mục `uploads` ngoài static/templates. Không commit tệp upload. Khi deploy phải dùng thư mục/volume lưu bền vững và sao lưu cùng database.
- Tên lưu là UUID, tên gốc chỉ dùng khi tải xuống. Kiểm tra chữ ký PDF, cấu trúc ZIP/Office, đường dẫn ZIP, dung lượng và giới hạn tổng giải nén 200 MiB/10.000 entry. Đây không phải chức năng quét virus hoặc kiểm tra nội dung học thuật.
- Tải tệp đi qua controller/service kiểm tra quyền bản ghi; response là attachment với `nosniff` và `no-store`.
- `NopBaoCaoService` lưu ba tệp rồi gọi `BaoCaoService` có transaction để kiểm tra lại quyền/hạn và ghi metadata nguyên bộ. Nếu thao tác thất bại thì dọn tệp mới. Tệp và DB không có transaction chung: sự cố máy chủ tắt đột ngột vẫn có thể để lại tệp không có metadata.
- Ghi báo cáo/chấm/sửa/công bố dùng `READ_COMMITTED`, khóa theo **đợt → đề tài → nhóm** qua `QuyTacNhom`. Khi xếp lịch, khóa thêm các GV theo ID tăng dần để ngăn hai đợt đặt cùng GV cùng giờ.
- Không gọi Repository từ Controller; không tính điểm ở View. Các nút ẩn/hiện chỉ hỗ trợ giao diện; Service vẫn kiểm tra quyền và trạng thái.

### Chạy thử trên web

1. Khởi động lại ứng dụng bằng Java 21. Chuẩn bị tài khoản khoa, nhóm SV đã được duyệt và GVHD; tạo thêm GVPB khác GVHD. Với NCKH/KLTN cần ít nhất 3 GV hội đồng khác GVHD; GVPB có thể là một trong ba người đó.
2. Chọn hạn báo cáo còn ở tương lai khi tạo đợt. Nhóm trưởng vào **Báo cáo**, nộp đủ ba tệp thật; thử nộp lần hai và xem hai phiên bản.
3. Với NCKH/KLTN, khoa vào **Hội đồng**, tạo 3–5 thành viên và chốt. Vào **Đánh giá → Lập kế hoạch**, chọn nhóm, GVPB và thời gian; điền hội đồng nếu có.
4. Đến hạn báo cáo và giờ PB, khoa mở phản biện. GVPB vào **Đánh giá**, lưu nháp rồi nộp chính thức. Thử bằng GVHD hoặc GV khác để kiểm tra chặn quyền.
5. Với NCKH/KLTN, đến giờ hội đồng khoa mở hội đồng; các thành viên còn lại nộp điểm. GV kiêm nhiệm không nộp thêm điểm thứ hai.
6. Chủ tịch tổng hợp nếu có hội đồng; khoa tổng hợp nếu chỉ PB riêng. Khoa xác nhận rồi công bố. SV vào **Kết quả**.
7. GV xin sửa điểm; khoa duyệt; kiểm tra SV vẫn thấy điểm cũ và nhãn điều chỉnh. GV nộp điểm mới, tổng hợp/xác nhận/công bố lại để xem phiên bản mới.
8. Khoa/admin thử **Thông báo**: lưu nháp, thêm tệp, công bố, thu hồi, sửa và công bố lại.

Không thay giờ hệ thống hoặc sửa trực tiếp database chính để bỏ qua hạn. Có thể tạo đợt demo với các mốc cách nhau vài phút; test tự động dùng Clock riêng.

### Kiểm thử

- Lần xác minh ngày 01/10/2026: `clean verify` bật cả hai schema thử đạt **148/148 test**, không failure/error/skipped; đóng gói JAR thành công. Gồm 129 test unit/MVC và 19 test tích hợp MySQL (4 nhóm/đăng ký, 15 thành viên 4).
- Test thông thường có thêm `LuuTepServiceTest` (5 ca) và `NopBaoCaoServiceTest` (4 ca): định dạng/tệp giả, đường dẫn, dung lượng, quyền trước upload và dọn tệp khi lỗi.
- `danhgia/integration/ThanhVienBonMySqlTest.java` có **15 ca với MySQL thật + MockMvc**, gồm bốn loại đề tài, render 11 trang, POST/multipart, CSRF, tải tệp đúng quyền, điểm 0/10 và nhập sai, ngưỡng làm tròn, snapshot sửa điểm, từ chối/hủy, GVHD hai chiều, hạn PB, hội đồng 3–5, thông báo, upload/chấm đồng thời và xếp cùng GV giữa hai đợt.
- Test tích hợp mặc định bỏ qua. Chỉ nhận biến `TEST_TV4_DB_URL` có host `127.0.0.1:3306` và schema `quan_ly_de_tai_thu_tv4_*`; không chấp nhận DB chính. Dùng `TEST_DB_USERNAME`, `TEST_DB_PASSWORD` cho tài khoản MySQL được cấp quyền đúng schema thử.
- Tạo schema thử **rỗng**, ví dụ `quan_ly_de_tai_thu_tv4_nhom`; Flyway tạo cấu trúc. Test tự sinh fixture, lưu tệp thử trong `target/tep-kiem-thu-tv4` và dọn tệp khi kết thúc; không tự xóa schema. Tài khoản fixture không dùng đăng nhập bằng trình duyệt.
- Các kiểm thử này chạy qua Spring/MockMvc và ghi/đọc tệp thật; nhóm vẫn cần tự thao tác trên trình duyệt và kiểm tra hosting.

```powershell
$env:TEST_TV4_DB_URL = "jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai_thu_tv4_nhom?serverTimezone=UTC"
$env:TEST_DB_USERNAME = "<TAI_KHOAN_MYSQL_CO_QUYEN_TREN_SCHEMA_THU>"
$env:TEST_DB_PASSWORD = "<MAT_KHAU_TREN_MAY_BAN>"
.\mvnw.cmd --settings .mvn\settings.xml "-Dtest=ThanhVienBonMySqlTest" test
Remove-Item Env:TEST_TV4_DB_URL, Env:TEST_DB_USERNAME, Env:TEST_DB_PASSWORD
```

Bản thành viên 4 hiện có ở local. Cần thành viên phụ trách đọc hiểu/chạy thử, review phần dùng chung và so sánh với nhánh riêng trước khi commit/push/tích hợp.
