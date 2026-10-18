# Hệ thống Quản lý Đề tài Sinh viên – Khoa CNTT

> **Cập nhật 18/10/2026:** Quy tắc mới theo rubric ở [PHAM_VI_CHUC_NANG.md](PHAM_VI_CHUC_NANG.md): nhóm 3–5 khi đăng ký, hai giai đoạn không chồng nhau, Trưởng bộ môn duyệt/phân công GVHD trong bộ môn. Email đơn giản theo Lab 6 tại [HUONG_DAN_EMAIL.md](HUONG_DAN_EMAIL.md); deploy Render + Aiven tại [TRIEN_KHAI.md](TRIEN_KHAI.md). Các mốc kiểm thử ghi ngày cũ bên dưới là lịch sử, không thay thế kết quả kiểm thử bản mới.


## Bối cảnh, quyết định hiện hành, tiến độ và hướng dẫn tiếp tục

**Mốc tổng hợp: 01/10/2026, giờ Việt Nam.** Đối chiếu lịch sử trao đổi, hai PDF của giảng viên, tài liệu và code local. Cập nhật thêm cách chia package theo yêu cầu giảng viên. Chưa fetch lại GitHub; các kết quả kiểm thử bên dưới ghi đúng thời điểm đã thực hiện.

**Cách sử dụng:** sao chép toàn bộ tài liệu này vào cuộc trò chuyện AI mới. Khi cần sửa code, gửi thêm repository/bản ZIP chứa code hiện tại và các tài liệu database được liệt kê ở mục 10. AI không tự có quyền đọc file trên máy chỉ từ đường dẫn trong tài liệu.

## 1. Bối cảnh và nguyên tắc làm việc

- Đây là đồ án môn Lập trình web của **nhóm 4 thành viên**. Tôi đảm nhiệm **thành viên 1: tài khoản và hồ sơ**.
- Tôi đã học Spring Framework, Spring MVC và Spring Boot; ưu tiên Spring Boot, code vừa sức sinh viên, dễ đọc, có thể giải thích và tự sửa khi bảo vệ.
- Website quản lý đề tài môn học, nghiên cứu khoa học, tiểu luận chuyên ngành và khóa luận tốt nghiệp trong khoa CNTT.
- **Không phải hệ thống đăng ký tín chỉ/môn học.** Tên “Hệ thống Quản lý Đăng ký Môn học” từng xuất hiện trong mẫu yêu cầu cũ là nhầm phạm vi. Không có môn tiên quyết, tích lũy tín chỉ, lớp học phần hoặc thời khóa biểu học tập.
- Nhóm 4 người làm đồ án này khác với nhóm sinh viên trong website: nhóm thực hiện một đề tài trên website tối đa **3 SV**.
- Yêu cầu của giảng viên: **không đặt nghiệp vụ/dịch vụ trong Controller hoặc View**. Service chứa nghiệp vụ và transaction. Không chỉ tạo code chạy được; phải hiểu được code.
- Dùng tên bảng/file/nhánh bình thường, mô tả đúng chức năng; không đặt tên mới có chữ “codex”, “AI” hoặc tên mang tính công cụ sinh code.
- Ưu tiên hoàn thiện chức năng và giao diện MVC trước; REST API có thể bổ sung sau khi thật sự cần.
- Người dùng đã yêu cầu làm đầy đủ phần thành viên 2 để dự phòng, rồi tiếp tục thành viên 3 và **thành viên 4** ngày 01/10/2026. Bản local đã có cả bốn phần. Không tự xóa dữ liệu, sửa migration đã dùng hoặc ghi đè công việc của người khác.

## 2. Quyết định hiện hành và những phương án đã bỏ

| Nội dung | Quyết định đang dùng |
|---|---|
| Thiết kế database | **24 bảng nghiệp vụ**, thay mô hình 27 bảng trước đây |
| Kiến trúc | Một ứng dụng Spring Boot monolith, tổ chức package theo tính năng |
| Giao diện | Spring MVC `@Controller` + Thymeleaf + HTML/CSS |
| Xác thực | Spring Security form login, session, BCrypt, CSRF |
| Phân quyền | Vai trò và quyền trên từng bản ghi; `@PreAuthorize` và kiểm tra trong Service |
| Truy cập DB | Spring Data JPA/Hibernate, Entity mapping, `JpaRepository` |
| Giao dịch | `@Transactional` tại Service; khóa bi quan và UNIQUE khi cần chống tranh chấp |
| Migration | Flyway; Hibernate `ddl-auto=validate` |
| Điểm GVHD | **Không có điểm GVHD cho đề tài mình hướng dẫn** |
| Trigger | Bản 24 bảng **không có trigger nghiệp vụ**; các quy tắc liên bảng phải làm tại Service |

Không áp dụng lại các đề xuất cũ: Java 16, 27 bảng, JWT, React/Vue, microservices hoặc bộ trigger lớn. Đề xuất ban đầu tính cả điểm GVHD đã được thay thế bằng quy tắc GVHD không chấm đề tài mình hướng dẫn.

Không làm trong phạm vi hiện tại: SV tự đề xuất đề tài, nhiều nhóm cùng thực hiện một đề tài, rubric nhiều tiêu chí, bảo vệ lại, import Excel hàng loạt, email/realtime, mobile app, Kafka/Redis và nhật ký hệ thống tổng quát. Việc lưu các phiên bản điểm/kết quả vẫn bắt buộc.

## 3. Nghiệp vụ đã thống nhất

### 3.1. Vai trò và trách nhiệm

Các vai trò tài khoản cố định: `ADMIN`, `TRUONG_KHOA`, `CAN_BO_KHOA`, `GIANG_VIEN`, `SINH_VIEN`. Một tài khoản có thể có nhiều vai trò.

| Người sử dụng | Trách nhiệm |
|---|---|
| Sinh viên | Xem đề tài công bố, tạo/tham gia nhóm, nhận lời mời, xem kết quả của nhóm mình |
| Nhóm trưởng | Mời thành viên, đăng ký/đổi đề tài khi được phép, nộp báo cáo |
| Giảng viên | Đề xuất đề tài; hướng dẫn hoặc chấm đề tài được phân công |
| GVHD | Hướng dẫn 1 đề tài theo phân công, không được chấm chính đề tài đó |
| GVPB | Nhập điểm tổng và nhận xét phản biện |
| Hội đồng | 3–5 GV, gồm đúng 1 chủ tịch, 1 thư ký và các ủy viên; thành viên chấm điểm, chủ tịch tổng hợp đánh giá |
| Cán bộ khoa | Duyệt/công bố đề tài, duyệt đăng ký, phân công, lập hội đồng/xếp lịch, xử lý sửa điểm, công bố kết quả |
| Trưởng khoa | Có quyền quản lý của cán bộ khoa; riêng việc tạo đợt thuộc trưởng khoa |
| Quản trị viên | Tạo/sửa/khóa tài khoản, gán vai trò, quản lý hồ sơ/bộ môn; được đăng thông báo |

Nhóm trưởng, GVHD, GVPB, chủ tịch/thư ký/ủy viên là tư cách trong nhóm hoặc phân công, không phải các role đăng nhập mới cần thêm vào `tai_khoan_vai_tro`.

### 3.2. Đợt đăng ký và đề tài

- Bốn loại: `MON_HOC`, `NCKH`, `TLCN`, `KLTN`.
- Đợt có mã/tên/loại; khoảng GV đề xuất; khoảng SV đăng ký; hạn nộp báo cáo; trạng thái và người tạo.
- Hai giai đoạn có mục đích riêng nhưng **được chồng thời gian** theo quyết định của nhóm: GV có thể tiếp tục đề xuất trong khi SV chọn các đề tài đã công bố.
- `han_nop_diem_pb` cấp đợt chỉ có với TLCN/KLTN; loại khác phải NULL. `ngay_bao_cao_hd` cấp đợt chỉ có với KLTN; loại khác phải NULL.
- NCKH vẫn có hội đồng; lịch cụ thể được lưu ở lần đánh giá, không đặt vào trường ngày hội đồng cấp đợt dành riêng KLTN.
- Chỉ GV đề xuất; SV chọn danh sách đã công bố. Mỗi đề tài thuộc một đợt và một bộ môn, có nội dung/yêu cầu/sản phẩm dự kiến.
- Khoa duyệt, từ chối và công bố đề tài. Người đề xuất không mặc nhiên là người hướng dẫn.
- Có 1–2 GVHD khi công bố; không phân biệt hướng dẫn chính/phụ trong mô hình hiện tại.
- Trạng thái đợt: `NHAP`, `TRIEN_KHAI`, `KET_THUC`, `HUY`.
- Trạng thái đề tài: `NHAP`, `CHO_DUYET`, `TU_CHOI`, `DA_DUYET`, `DA_CONG_BO`.

### 3.3. Nhóm, lời mời và đăng ký

- SV tạo nhóm, trở thành nhóm trưởng; SV được mời phải chấp nhận mới là thành viên.
- Nhóm có tối đa 3 SV và đúng 1 nhóm trưởng. Mỗi SV chỉ thuộc 1 nhóm **trong cùng một đợt**; được tham gia các đợt khác nhau.
- Chỉ nhóm trưởng đăng ký; mỗi nhóm có 1 đăng ký hiện hành; mỗi đề tài chỉ có 1 nhóm giữ chỗ/thực hiện tại một thời điểm.
- Request đăng ký hợp lệ thành công trước được giữ chỗ trong lúc `CHO_DUYET`. Không có waitlist.
- Khi đăng ký `CHAP_THUAN`, khóa việc đổi thành viên, nhóm trưởng và đề tài. Trước chấp thuận được thay đổi theo điều kiện của đợt; bị từ chối có thể chọn lại nếu còn hạn.
- Đăng ký `TU_CHOI`/`HUY` không giữ chỗ. Thiết kế gọn cập nhật dòng đăng ký hiện hành, không lưu toàn bộ lịch sử mọi lần đổi đề tài.
- Đổi đề tài phải trong **một transaction**: nếu đề tài mới đã bị chiếm, rollback để giữ nguyên đề tài cũ.
- Service kiểm tra quyền, thời gian, đề tài công bố, nhóm hợp lệ; khóa các bản ghi liên quan theo thứ tự thống nhất rồi kiểm tra lại trước khi ghi.

### 3.4. Báo cáo và tệp

- Chỉ nhóm trưởng nộp; mỗi bộ báo cáo bắt buộc đủ **PDF + slide + mã nguồn**.
- Được nộp nhiều phiên bản trước hạn chung theo đợt. Bản hợp lệ cuối được chốt để chấm; giữ lại phiên bản cũ.
- Loại tệp trong DB: `BAO_CAO_PDF`, `SLIDE`, `MA_NGUON`; một phiên bản không được trùng loại tài liệu.
- Lưu metadata trong DB; phần lưu tệp thực tế, kiểm tra nội dung và quyền tải xuống còn phải triển khai.
- Upload tệp ngoài transaction DB; khi xác nhận nộp phải kiểm tra lại người nộp, thời hạn và đủ ba tệp. Có xử lý dọn tệp khi lưu metadata thất bại.

### 3.5. Phản biện, hội đồng và điểm

| Loại | Hình thức |
|---|---|
| MON_HOC | Một GVPB riêng |
| TLCN | Một GVPB riêng |
| NCKH | GVPB trước, hội đồng sau |
| KLTN | GVPB trước, hội đồng sau |

- Hội đồng 3–5 GV; chủ tịch và thư ký là hai thành viên khác nhau. Tất cả thành viên hội đồng chấm.
- Một GV được chấm nhiều đề tài; **không chấm đề tài mình hướng dẫn**. Phải kiểm tra cả khi thêm người chấm và khi đổi GVHD.
- GVPB kiêm thành viên hội đồng chỉ có **một phân công và một điểm được tính** cho đề tài.
- Mỗi GV nhập một điểm tổng 0–10 và nhận xét. Dùng `BigDecimal`/`DECIMAL`, không dùng điểm 0 thay cho chưa nộp.
- Điểm cuối = tổng điểm của những GV hợp lệ / số GV đó; làm tròn 1 chữ số rồi xét đạt từ 4.0. Ví dụ 3.94 → 3.9 không đạt; 3.95 → 4.0 đạt.
- **Không dùng `AVG(DISTINCT diem)`**: hai người cùng cho 8 vẫn là hai người chấm.
- Chỉ tổng hợp khi đủ phiếu bắt buộc. Chủ tịch tổng hợp phần hội đồng; khoa xác nhận/công bố; SV chỉ xem kết quả của đề tài mình tham gia.
- Một lần đánh giá chính thức, không bảo vệ lại. Chốt cùng một bộ báo cáo cho hai giai đoạn.

### 3.6. Sửa điểm, snapshot và thông báo

- GV gửi yêu cầu sửa điểm, có lý do → khoa duyệt/từ chối → nếu duyệt thì tạo phiếu điều chỉnh → tính lại → xác nhận/công bố kết quả mới.
- Không sửa đè phiếu đã nộp hoặc kết quả đã công bố. `chi_tiet_ket_qua` lưu chính xác phiên bản phiếu được dùng cho từng kết quả.
- Trong lúc điều chỉnh, SV vẫn thấy snapshot công bố cũ kèm nhãn **“Đang điều chỉnh”**. Không lấy điểm đang sửa để thay trực tiếp kết quả cũ.
- Trưởng khoa, cán bộ khoa, admin đăng thông báo nhà trường/khoa; có tệp đính kèm; chỉ người đã đăng nhập xem. Trạng thái: nháp, công bố, thu hồi.

## 4. Stack và quy tắc kỹ thuật hiện tại

| Thành phần | Cấu hình đã có |
|---|---|
| JDK | Java 21; `pom.xml` đặt `java.version=21` |
| Spring Boot | 3.5.16 trong `pom.xml` |
| Maven | Wrapper tải Maven 3.9.16; không cần cài Maven riêng nếu wrapper hoạt động |
| Web | Spring MVC, Thymeleaf, Bean Validation |
| Dữ liệu | Spring Data JPA/Hibernate; MySQL Connector/J; Flyway |
| Security | Spring Security 6 theo dependency Spring Boot; form login/session, BCrypt, CSRF, Method Security |
| MySQL đã xác minh trước đây | Server 8.0.46; InnoDB; utf8mb4; `FOREIGN_KEY_CHECKS=1`; strict SQL mode |
| Kiểm thử | Spring Boot Test, JUnit, Mockito, MockMvc, Spring Security Test |

Luồng chuẩn: **Form/DTO → Controller → Service → Repository → Entity/MySQL**. Controller không gọi Repository trực tiếp; View chỉ trình bày, không quyết định nghiệp vụ.

- Package gốc: `com.khoacntt.quanlydetai`; chia theo `account`, `hoso`, `dotdangky`, `detai`, `nhom`, `dangky`, `baocao`, `danhgia`, `thongbao`, `common`, `config`, `web`.
- Trong mỗi module: `model/` chứa Entity/enum; `repository/` truy cập DB; `service/` chứa nghiệp vụ; `controller/` xử lý HTTP; `dto/` cho dữ liệu hiển thị; `form/` cho dữ liệu nhập. View vẫn ở `src/main/resources/templates/<module>/`, không tạo package Java `view`. Module chưa triển khai chỉ có các tầng đã có code; bổ sung tầng còn lại khi làm chức năng.
- `account/config/` chứa properties khởi tạo admin, `account/security/` chứa filter phiên; cấu hình bảo mật dùng chung ở `config/SecurityConfig.java`. Test được chia package tương ứng. Đổi package không thay đổi URL, bảng database hoặc nghiệp vụ.
- Form riêng có validation, không bind request vào Entity. DTO trả dữ liệu hiển thị, không đưa hash mật khẩu lên View.
- Constructor injection; Entity có phương thức cập nhật có tên rõ nghĩa, không mở setter tùy tiện cho mọi trường.
- Service đọc dùng `@Transactional(readOnly = true)`; mỗi thao tác ghi là public service method có `@Transactional`.
- Quyền tổng quát dùng `@PreAuthorize`; quyền sở hữu/nhóm trưởng/người được phân công phải kiểm tra trong Service.
- Lỗi không có dữ liệu dùng `KhongTimThayException`; lỗi nghiệp vụ dùng `NghiepVuException`; không đưa stack trace/SQL ra màn hình.
- `spring.jpa.open-in-view=false`: chuyển Entity thành DTO trong phạm vi transaction; không dựa vào lazy loading ở View.
- Tên DB/cột snake_case. Cột generated map `insertable=false, updatable=false`. Đọc DDL trước khi tạo mapping.
- Thời điểm UTC; hiển thị/nhập theo giờ Việt Nam; ngày hội đồng cấp đợt là ngày địa phương. Khoảng đăng ký dùng `[bắt đầu, kết thúc)`. Phần đợt/đề tài/nhóm/đăng ký đã dùng `Clock` và `common/time/ThoiGian`, kiểm thử biên hạn và đọc/ghi MySQL UTC. Hikari đặt session UTC, Hibernate dùng `java_time_use_direct_jdbc=true` để không đổi múi giờ thêm lần nữa với `LocalDateTime`. Module 4 đã theo cùng quy ước.
- Upload đã triển khai: tối đa 50 MiB/tệp, request 155 MiB để chừa multipart cho ba tệp. Báo cáo PDF, slide PDF/PPTX, mã nguồn ZIP; thông báo nhận PDF/PPTX/DOCX/ZIP. Lưu ngoài static, theo UPLOAD_DIR (mặc định uploads).

## 5. Database chuẩn và trách nhiệm bảo vệ dữ liệu

**Database ứng dụng: `quan_ly_de_tai`.** `DoAn_QuanLyDeTai` chỉ là tên kết nối Workbench. `sys` là schema hệ thống MySQL, giữ nguyên. Có thể có `flyway_schema_history` ngoài 24 bảng nghiệp vụ, không tính đó là bảng nghiệp vụ thứ 25.

| Nhóm | Các bảng |
|---|---|
| Tài khoản/hồ sơ – 5 | `bo_mon`, `tai_khoan`, `tai_khoan_vai_tro`, `sinh_vien`, `giang_vien` |
| Đợt/đề tài – 3 | `dot_dang_ky`, `de_tai`, `huong_dan` |
| Nhóm/lời mời – 3 | `nhom`, `thanh_vien_nhom`, `loi_moi_nhom` |
| Đăng ký/báo cáo – 3 | `dang_ky`, `bao_cao`, `bao_cao_tep` |
| Hội đồng/đánh giá – 4 | `hoi_dong`, `thanh_vien_hoi_dong`, `danh_gia`, `phan_cong_cham` |
| Điểm/kết quả – 4 | `phieu_cham`, `yeu_cau_sua_diem`, `ket_qua`, `chi_tiet_ket_qua` |
| Thông báo – 2 | `thong_bao`, `thong_bao_tep` |

Các quan hệ quan trọng: tài khoản có nhiều vai trò, tối đa một hồ sơ mỗi loại; GV thuộc bộ môn; đề tài thuộc đợt/bộ môn và nối với GV qua `huong_dan`; nhóm nối SV qua `thanh_vien_nhom`; đăng ký nối nhóm–đề tài; báo cáo có nhiều phiên bản/tệp; lần đánh giá có các phân công chấm; mỗi phân công có các phiên bản phiếu; mỗi kết quả tham chiếu các phiếu nguồn qua `chi_tiet_ket_qua`.

Nguồn cấu trúc chạy cùng ứng dụng: **`src/main/resources/db/migration/V1__khoi_tao_24_bang.sql`**. Không sửa V1 đã chia sẻ; cần đổi schema thì tạo migration tiếp theo được trưởng nhóm thống nhất. Không bật `ddl-auto=update`.

- Tất cả FK của thiết kế hiện tại dùng `ON DELETE RESTRICT / ON UPDATE RESTRICT`.
- UNIQUE giữ một SV/một nhóm/một đợt; một đăng ký hiện hành/nhóm; không trùng loại tài liệu, người chấm và phiên bản.
- `dang_ky.co_giu_cho` = 1 khi `CHO_DUYET`/`CHAP_THUAN`, còn lại NULL; `UNIQUE(de_tai_id, co_giu_cho)` chống hai nhóm giữ cùng đề tài.
- UNIQUE trên cột generated giới hạn tối đa một trưởng nhóm, chủ tịch/thư ký, GVPB, yêu cầu sửa đang xử lý. Service phải bảo đảm số lượng tối thiểu và trạng thái phù hợp.
- FK kép bảo vệ liên kết đúng đợt, đúng đăng ký và đúng phân công sở hữu phiếu.
- CHECK kiểm tra trạng thái, khoảng thời gian, điểm 0–10 và điều kiện cùng dòng.
- `ket_qua.diem_cuoi` tự tính từ `tong_diem / so_nguoi_cham`; `dat` xét trên điểm đã làm tròn. Service phải bảo đảm tổng và số người khớp các dòng chi tiết.
- Không có trigger nghiệp vụ. Giới hạn 3 SV, 1–2 GVHD, 3–5 thành viên hội đồng, quyền, hạn nộp, đủ ba tệp, cấm GVHD chấm và bất biến lịch sử còn cần Service + test. Không tuyên bố SQL đã bảo vệ tất cả.
- Mỗi thành viên dùng MySQL/database local riêng. Git lưu cấu trúc/migration và code, không tự đồng bộ dữ liệu giữa các máy.
- Flyway hiện bật `baseline-on-migrate=true`, phiên bản 1. Với schema mới rỗng: chạy V1. Với schema cũ không có lịch sử: baseline **không tự chứng minh đủ/đúng 24 bảng**; chỉ dùng khi đã đối chiếu cấu trúc. Hibernate mới validate các Entity đã map.

## 6. Quá trình đã thực hiện và bằng chứng

| Giai đoạn | Việc đã làm | Trạng thái/bằng chứng |
|---|---|---|
| Phân tích | Đọc đề, làm rõ nghiệp vụ, xác định nhóm 4 người và kiến thức đã học | Quyết định mới nằm trong phạm vi chức năng và bộ DB 24 bảng |
| Thu gọn | Thay phương án 27 bảng bằng 24; chọn MVC/session thay kiến trúc phức tạp | Giữ nghiệp vụ cốt lõi; không xây bộ trigger lớn |
| Database, 22/09/2026 | DDL, ERD, từ điển, dữ liệu mẫu, SQL test và runner Python | 24 bảng; 61/61 ca SQL đạt; thử hai kết nối tranh đề tài đạt; báo cáo 08 |
| Môi trường | Xác minh MySQL 8.0.46, khôi phục quyền root khi quên mật khẩu, cấu hình login-path và tài khoản ứng dụng | Không đưa mật khẩu vào tài liệu bàn giao |
| Khung ứng dụng | Java 21, Maven wrapper, Spring Boot, Flyway/JPA validate, đăng nhập, phân quyền, lỗi chung, enum, hướng dẫn nhóm | Khung đã được đưa lên GitHub theo trao đổi trước |
| Thành viên 1, 24/09/2026 | Tài khoản, bộ môn, hồ sơ, đổi mật khẩu, phân quyền, màn hình MVC | Đã code local; 42/42 test đạt; đã chạy HTTP + MySQL trên schema riêng |
| Dọn môi trường | Người dùng xóa hai schema test cũ; giữ `quan_ly_de_tai` và `sys`; đổi mật khẩu root | Chỉ ảnh hưởng các kết nối root trên máy đó; không đổi mật khẩu website/doan_dev |
| Rà soát 01/10/2026 | Đọc lại code, tài liệu, PDF, trạng thái Git và báo cáo test đang có | Lần tổng hợp ban đầu chỉ đọc, chưa fetch remote |
| Chia package 01/10/2026 | Theo yêu cầu giảng viên, tách model/repository/service/controller/dto/form, cập nhật import, test và tài liệu | Chạy lại `clean test`: 42/42 đạt; giữ nguyên nội dung xử lý, URL và schema |

### 6.1. Phần thành viên 1 đã có

| URL | Chức năng | Quyền |
|---|---|---|
| `/login`, `/logout` | Đăng nhập/đăng xuất; logout qua POST | Theo Spring Security |
| `/dashboard` | Trang điều hướng | Đã đăng nhập |
| `/admin/tai-khoan` | Danh sách/tạo/sửa, nhiều vai trò, khóa/mở khóa | ADMIN |
| `/admin/bo-mon` | Danh sách/tạo/sửa/ngừng hoạt động | ADMIN |
| `/admin/sinh-vien` | Danh sách/tạo/sửa hồ sơ | ADMIN |
| `/admin/giang-vien` | Danh sách/tạo/sửa hồ sơ, gán bộ môn | ADMIN |
| `/ho-so` | Xem hồ sơ của chính mình | Đã đăng nhập |
| `/ho-so/doi-mat-khau` | Đổi bằng mật khẩu hiện tại | Đã đăng nhập |

Đã kiểm tra trùng username/email/mã; hồ sơ phải khớp vai trò; không chuyển hồ sơ sang tài khoản khác; không gỡ vai trò SV/GV khi đã có hồ sơ. Admin không tự khóa hoặc tự bỏ ADMIN. Không xóa tài khoản/bộ môn qua UI; khóa/ngừng hoạt động để giữ liên kết. Bộ môn ngừng hoạt động giữ GV cũ nhưng không nhận gán mới.

`KiemTraTaiKhoanFilter` làm thay đổi khóa/vai trò có hiệu lực với phiên cũ ở request tiếp theo. Đổi mật khẩu kiểm tra mật khẩu cũ, xác nhận, khác mật khẩu hiện tại; tối thiểu 8 ký tự/tối đa 72 byte UTF-8; BCrypt; đăng xuất phiên đang thao tác. **Chưa có cơ chế bảo đảm đăng xuất mọi thiết bị khi đổi mật khẩu.**

Giao diện Thymeleaf cơ bản đã dùng được; không đồng nghĩa toàn bộ UI của website đã hoàn thiện. Chưa có tự đăng ký, quên mật khẩu/email hay reset mật khẩu bởi admin.

### 6.2. Phần thành viên 2 đã có

- Entity/Repository/Service/Controller/Form/DTO cho `dotdangky`, `detai`, `huong_dan`; 6 trang Thymeleaf và menu theo vai trò. Chi tiết và cách chạy thử ở mục 11 của `docs/HUONG_DAN_LAM_BACKEND_NHOM.md`.
- Trưởng khoa tạo/sửa đợt nháp, triển khai/kết thúc/hủy; khoa xem danh sách và chi tiết. Chỉ sửa khi nháp; chưa mở lại đợt; hủy bị chặn nếu có nhóm giữ chỗ/được duyệt.
- GV tạo nháp, sửa, gửi duyệt, gửi lại sau từ chối trong hạn; chỉ thao tác đề xuất của mình. Khoa duyệt/từ chối có lý do, phân công/thay 1–2 GVHD và công bố. Người đề xuất không mặc nhiên là GVHD.
- Người đăng nhập xem/lọc đề tài công bố; GV xem đề xuất của mình và đề tài được hướng dẫn. Đề tài chưa công bố có kiểm tra quyền theo bản ghi.
- Kiểm tra giờ theo loại, bộ môn/tài khoản hoạt động, số GVHD và xung đột với phân công chấm/hội đồng. Các thao tác khóa theo **đợt → đề tài** trong transaction; module 3/4 cần dùng cùng thứ tự.
- Không sửa V1 hoặc thêm bảng. Bản local dùng dự phòng, chưa commit/push; không thay thế công việc trên máy/nhánh của thành viên 2.

### 6.3. Phần thành viên 3 đã có

- Module `nhom` và `dangky` có Entity/Repository/Service/Controller/Form/DTO và 6 trang Thymeleaf; menu **Nhóm và đăng ký** cho SV, **Duyệt đăng ký** cho khoa. Mục 12 của hướng dẫn backend mô tả URL, quy tắc và chạy thử.
- Tạo nhóm và trưởng nhóm ban đầu; đổi tên; mời/thu hồi/chấp nhận/từ chối; đổi trưởng; đưa thành viên ra/rời/giải thể. Chỉ hoạt động trong khoảng SV đăng ký, trước chấp thuận; 1–3 thành viên, đúng một trưởng, mỗi SV một nhóm/đợt.
- Nhóm trưởng chọn/đổi/hủy đề tài; một dòng đăng ký hiện hành; giữ chỗ khi chờ duyệt; khoa từ chối có lý do hoặc chấp thuận. Chấp thuận khóa thay đổi nhóm/đề tài.
- Đổi đề tài thất bại giữ nguyên chỗ cũ; giải thể trước chấp thuận hủy giữ chỗ và giải phóng thành viên. Không xóa dòng nhóm/đăng ký. Lời mời khác mất hiệu lực khi SV có nhóm, nhóm đủ 3, đổi trưởng, giải thể hoặc được duyệt.
- `DANG_LAP`: đang lập; `DA_CHOT`: chờ duyệt đăng ký, vẫn được sửa thành viên; `DANG_THUC_HIEN`: đã chấp thuận và khóa thay đổi; `GIAI_THE`: đã giải thể; `HOAN_THANH` chưa do module 3 thiết lập.
- Khóa theo **đợt → đề tài cũ/mới theo ID tăng dần → nhóm**, dùng `READ_COMMITTED` cho thao tác ghi của module 3 và `DeTaiService`. Tuần tự hóa theo đợt là lựa chọn gọn cho đồ án; chưa tối ưu tải lớn.
- Có kiểm tra đề tài đã xem trên form duyệt: nếu nhóm vừa đổi đề tài, khoa phải tải lại trước khi duyệt.
- Không đổi schema/V1. Code local chưa commit/push; cần so sánh với phần đồng đội đã làm trước khi tích hợp.

### 6.4. Phần thành viên 4 đã code local

- `baocao`, `danhgia`, `thongbao` có đầy đủ các tầng MVC và 11 trang Thymeleaf. Nộp bộ ba tệp, phiên bản báo cáo, kế hoạch PB/hội đồng, phiếu nháp/chính thức, sửa điểm có duyệt, kết quả theo phiên bản và thông báo đính kèm đã dùng được.
- MON_HOC/TLCN một GVPB; NCKH/KLTN PB trước và hội đồng sau. Hội đồng 3–5 GV; GVHD bị chặn cả hai chiều phân công; GVPB kiêm hội đồng chỉ tính một điểm. Chủ tịch tổng hợp nếu có hội đồng; khoa tổng hợp nếu chỉ PB.
- Khi mở PB sau hạn báo cáo, chốt phiên bản cuối cho cả hai bước. Tổng hợp cần đủ phiếu; điểm cuối do cột generated MySQL làm tròn 1 chữ số, đạt >=4.0. Công bố chuyển nhóm HOAN_THANH.
- Sửa điểm giữ phiếu/kết quả cũ; GV xin sửa, khoa khác người yêu cầu duyệt, GV nộp phiên bản mới rồi tổng hợp/xác nhận/công bố lại. SV chỉ thấy snapshot đã công bố, có nhãn điều chỉnh; hỗ trợ quy trình này khi đợt KET_THUC.
- Tệp lưu riêng bằng UUID, tải qua kiểm tra quyền; nếu upload/ghi metadata lỗi thì dọn tệp mới. Cần volume bền vững khi deploy; chưa có quét virus hoặc cơ chế tự dọn tệp mồ côi sau sự cố máy chủ.
- Thông báo có nháp/công bố/thu hồi; chỉ sửa sau thu hồi. Khoa quản lý bài của mình, admin quản lý mọi bài; người đăng nhập chỉ xem bài và tệp đã công bố.
- Khóa theo đợt → đề tài → nhóm; xếp lịch khóa thêm các GV theo ID tăng dần. Thời gian dùng Clock/UTC như các module trước. Không đổi schema/V1, không tạo thêm vai trò tài khoản, không thêm REST/JWT.
- Chi tiết URL, thứ tự demo, giới hạn tệp, phạm vi kiểm tra lịch và lệnh test ở **mục 13 của docs/HUONG_DAN_LAM_BACKEND_NHOM.md**. Code local chưa commit/push.

### 6.5. Phạm vi kiểm thử đã thực hiện

- SQL ngày 22/09: 61 ca đạt, dữ liệu mẫu 121 INSERT phủ 24 bảng, có thử tranh chỗ bằng hai kết nối. Đây là test ràng buộc DB, **không phải test transaction đăng ký ở Spring**.
- Spring ngày 24/09: **42 test, 0 failure, 0 error**, gồm test Entity/bootstrap/login, Service tài khoản/hồ sơ/bộ môn và MockMvc. Sau khi chia package ngày **01/10/2026**, đã chạy lại `clean test`, vẫn **42/42 đạt**. Báo cáo lần mới ở `target/surefire-reports` và bị tạo lại mỗi lần chạy test.
- Đã thử HTTP với MySQL thật trên schema tách biệt: tạo/sửa hồ sơ, dữ liệu trùng, CSRF, chặn SV vào admin, hồ sơ cá nhân, khóa/mở khóa, đổi mật khẩu, thay vai trò. Schema thử riêng của lần này đã được dọn.
- Sau khi thêm thành viên 2 ngày 01/10: **85/85 test đạt**, gồm 42 ca cũ và 43 ca mới (12 service đợt, 18 service đề tài, 13 MVC/phân quyền).
- Đã chạy HTTP + MySQL thật trên schema riêng cho đủ 4 loại: tạo/sửa/triển khai, từ chối/gửi lại/duyệt, thay GVHD, công bố, quyền xem, UTC, kết thúc/hủy. Dữ liệu liên module kiểm tra được việc chặn GV chấm/thành viên hội đồng thành GVHD và chặn hủy đợt có đăng ký. Hai request công bố đồng thời chỉ một thành công.
- Sau thành viên 3: `clean test` bật MySQL đạt **124/124**, gồm **120 test unit/MVC** (thêm 26 nghiệp vụ và 9 MVC) và **4 test tích hợp MySQL**. Test tích hợp có trong source và chỉ chạy khi cấu hình `TEST_DB_URL` cho schema riêng; đã kiểm tra mặc định bỏ qua 4 ca. Schema thử đã được dọn, không nạp fixture vào database chính.
- HTTP + MySQL đã qua: luồng từ mở đợt/công bố đến nhóm được duyệt; hai nhóm tranh chỗ; đổi đề tài thất bại và đổi chéo giữ chỗ cũ; hai lời mời đồng thời cho một SV; nhóm không quá 3; tạo nhóm đồng thời; duyệt đồng thời sửa thành viên; quyền, CSRF, đổi trưởng, rời/giải thể, từ chối/thu hồi/mời lại và biên thời gian UTC.
- Thành viên 4 có thêm 9 ca unit (tệp và điều phối upload) và 15 ca MySQL + MockMvc (bốn loại, POST/multipart, phân quyền, snapshot và concurrency). Xem mục 13 hướng dẫn backend. Chưa xác minh triển khai hosting; cần nhóm tự thao tác lại trên trình duyệt.
- Lần xác minh toàn bộ sau thành viên 4 ngày 01/10/2026: **`clean verify` đạt 148/148**, gồm 129 unit/MVC + 4 MySQL thành viên 3 + 15 MySQL thành viên 4; không lỗi hoặc bỏ qua, đóng gói JAR thành công. Chỉ dùng schema thử riêng, không nạp dữ liệu thử vào `quan_ly_de_tai`.

## 7. Git, môi trường local và cách chạy

Repository: https://github.com/hoangdanhnhan792006-gif/quan-ly-de-tai.git

Thư mục project trên máy hiện tại:

```text
C:\Users\Admin\Documents\Codex\2026-09-17\t-i-ang-h-c-m\quan-ly-de-tai
```

**Trạng thái local kiểm tra ngày 01/10/2026:** nhánh `feature/tai-khoan-ho-so`; HEAD `ca6dc1b` (`Cap nhat pham vi chuc nang cho nhom`). Có file modified và untracked của phần thành viên 1, chia lại package, bản dự phòng thành viên 2 và phần thành viên 3–4. **Các phần này chưa commit, chưa push, chưa merge.** Không kết luận gì về thay đổi mới của đồng đội trên GitHub vì chưa fetch remote.

Quy trình nhóm: nhánh `feature/...` → kiểm thử → thành viên khác review → merge `develop` → kiểm tra tích hợp → merge `main` khi ổn định. Trước pull/chuyển nhánh phải giữ các file chưa commit; không dùng reset/clean để làm mất code.

Java 26 có thể vẫn là mặc định của lệnh `java` trên máy. Project dùng Java 21: đặt `JAVA_HOME` và kiểm tra **Java mà Maven thực sự dùng** bằng `mvnw.cmd -v`.

Ví dụ PowerShell, thay đường dẫn JDK và mật khẩu bằng giá trị trên máy đang chạy:

```powershell
cd "C:\Users\Admin\Documents\Codex\2026-09-17\t-i-ang-h-c-m\quan-ly-de-tai"
$env:JAVA_HOME = "C:\Users\Admin\.jdks\jdk-21.0.12.1+1"
$env:DB_USERNAME = "doan_dev"
$env:DB_PASSWORD = "<MAT_KHAU_MYSQL_CUA_DOAN_DEV_TREN_MAY_BAN>"
.\mvnw.cmd -v
.\mvnw.cmd --settings .mvn\settings.xml spring-boot:run
```

Mở `http://localhost:8080`. Dừng bằng Ctrl+C. Maven wrapper có thể cần mạng ở lần tải dependency đầu tiên. File settings trên dùng cache `.mvn-repository` trong project; không commit cache.

```powershell
.\mvnw.cmd --settings .mvn\settings.xml clean test
.\mvnw.cmd --settings .mvn\settings.xml package
```

DB_URL mặc định là `jdbc:mysql://127.0.0.1:3306/quan_ly_de_tai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC`. Máy mới tạo schema rỗng rồi để Flyway tạo bảng; máy hiện tại không cần tạo lại/import lại. Không trộn import DDL thủ công và migration vào một schema đang có dữ liệu.

Bootstrap admin chỉ dùng lần đầu theo README: `APP_BOOTSTRAP_ADMIN_ENABLED=true`, cùng các biến USERNAME/PASSWORD/FULL_NAME/EMAIL; tạo xong tắt ENABLED. Bootstrap bỏ qua username đã tồn tại, **không phải công cụ reset mật khẩu**. Không có mật khẩu đăng nhập website mặc định được công bố trong bản tổng hợp này.

Phân biệt rõ ba loại thông tin:

- `root`: quản trị MySQL trên máy; đổi mật khẩu không mất dữ liệu, không ảnh hưởng đồng đội dùng MySQL riêng.
- `doan_dev`: tài khoản kết nối MySQL của ứng dụng; không tự đổi theo mật khẩu root.
- Tài khoản trong `tai_khoan`: đăng nhập website, mật khẩu băm BCrypt; không dùng mật khẩu MySQL để đăng nhập web.

Login-path `codex_doan` là tên cấu hình MySQL client cũ, không phải database/project. Đã dùng kết nối này thành công ngày 01/10 để tạo và kiểm tra schema riêng cho thành viên 2; không đưa mật khẩu vào source/tài liệu.

Nếu Spring Boot dừng với “exit code 1”, đọc lỗi gốc phía trên (`Caused by`, lỗi datasource/Flyway/port), không kết luận từ dòng Maven cuối cùng. Không đưa mật khẩu thật vào chat, code, `.md`, GitHub hoặc ZIP nộp bài.

## 8. Phân công và việc cần làm tiếp

| Thành viên | Package | Công việc | Trạng thái bản local đang kiểm tra |
|---|---|---|---|
| **1 – tôi** | `account`, `hoso` | Tài khoản, xác thực, vai trò, bộ môn, hồ sơ | Đã có Service/Controller/UI/test; cần review, commit, push và tích hợp |
| 2 | `dotdangky`, `detai` | Đợt, đề xuất/duyệt/công bố đề tài, 1–2 GVHD | Đã code đủ Service/Controller/UI/test làm bản dự phòng local; cần review và thống nhất tích hợp |
| 3 | `nhom`, `dangky` | Nhóm, lời mời, trưởng nhóm, giữ chỗ, duyệt/đổi đăng ký | Đã code Service/Controller/UI/test, qua HTTP và race condition MySQL; cần review/tích hợp |
| 4 | `baocao`, `danhgia`, `thongbao` | Báo cáo, tệp, hội đồng, chấm/sửa/công bố điểm, thông báo | Đã có luồng MVC, tệp, chấm/sửa/công bố và test MySQL; chờ review/bàn giao |

Không coi bảng database hoặc enum đã tồn tại là chức năng đã code xong. Không suy ra thành viên khác chưa làm ở nhánh/máy riêng từ tình trạng local này.

### Giai đoạn A – Bàn giao phần thành viên 1

- [ ] Người phụ trách tự đọc các Service và thử thao tác bằng tài khoản admin/SV/GV; hiểu Entity mapping, Repository, transaction và quyền.
- [ ] Review các file chung đã sửa: `SecurityConfig`, `NguoiDungHienTai`, `HomeController`, navigation/CSS và hướng dẫn nhóm.
- [ ] Kiểm tra diff, chạy lại test nếu code đã đổi; commit đầy đủ cả file untracked, push feature và mở PR khi thực hiện bàn giao Git.
- [ ] Sau review mới merge `develop`; không tự ghi đè module đồng đội.
- [ ] Nếu cần để cả nhóm clone là có từ điển/ERD/SQL test, đưa bộ tham khảo DB từ thư mục ngoài repo vào vị trí docs/database được nhóm thống nhất; không tạo nguồn migration cạnh tranh V1.

### Giai đoạn B – Luồng đợt → đề tài → đăng ký

- [x] Bản dự phòng thành viên 2 có Entity, Repository, Service, Form/DTO, Controller, UI và test cho đợt/đề tài/GVHD.
- [ ] Thành viên 2 đọc/chạy thử, so sánh với nhánh riêng nếu đã làm; chọn bản để review và tích hợp.
- [x] Thành viên 3 có tạo nhóm, mời/chấp nhận/từ chối, đổi trưởng nhóm, sửa thành viên và đăng ký/duyệt/đổi đề tài.
- [x] Chốt thứ tự khóa dùng chung; kiểm thử hai nhóm tranh cùng đề tài, hai lời mời chấp nhận đồng thời và nhóm đầy 3 người.
- [x] Kiểm thử đổi đề tài thất bại giữ chỗ cũ; duyệt đăng ký đồng thời với sửa thành viên; biên thời gian mở/đóng và quyền người thao tác.
- [x] Luồng HTTP với MySQL chạy được: trưởng khoa mở đợt → GV đề xuất → khoa công bố → SV lập nhóm → trưởng nhóm đăng ký → khoa chấp thuận.
- [ ] Thành viên 3 tự đọc/chạy thử; so sánh nhánh riêng, review và tích hợp.

### Giai đoạn C – Báo cáo → đánh giá → kết quả

- [x] Thành viên 4 làm lưu/tải tệp, đủ 3 tài liệu, phiên bản báo cáo và chốt bản dùng chấm.
- [x] Làm hội đồng/phân công/lịch, cấm GVHD chấm ở cả hai chiều thay đổi và GVPB kiêm hội đồng chỉ một điểm.
- [x] Làm phiếu chấm, tổng hợp đủ phiếu, xác nhận/công bố, xem kết quả đúng nhóm.
- [x] Làm quy trình sửa điểm có phê duyệt và snapshot cũ trong lúc điều chỉnh.
- [x] Làm thông báo có tệp, quyền đăng/xem và trạng thái.
- [x] Đầu ra: ít nhất một luồng GVPB riêng và một luồng GVPB + hội đồng chạy xuyên suốt; giữ đúng lịch sử sau sửa điểm.

- [ ] Thành viên 4 tự đọc các Service, thao tác trên trình duyệt và review trước khi tích hợp.

### Giai đoạn D – Tích hợp, triển khai và nộp

- [ ] Tích hợp nhánh nhóm, thống nhất navigation/giao diện và dữ liệu demo có tài khoản đăng nhập thật, file thật.
- [ ] Kiểm thử luồng thành công, dữ liệu lỗi, truy cập trái quyền, concurrency quan trọng và Flyway trên schema mới.
- [ ] Chọn hosting miễn phí chạy Java 21/Spring Boot, phương án MySQL và nơi lưu tệp; cấu hình secrets và HTTPS. Chưa chọn nhà cung cấp; kiểm tra điều kiện hiện hành khi chọn.
- [ ] Deploy, kiểm tra dữ liệu/tệp còn sau restart, đăng nhập và tải tệp đúng quyền; không chỉ kiểm tra trang chủ.
- [ ] Hoàn thiện báo cáo Word, use case, ERD, từ điển dữ liệu, ảnh các flow và bảng phân công.
- [ ] Chuẩn bị ZIP đúng tên, mã nguồn sạch và hướng dẫn cài đặt/deploy; mỗi người luyện giải thích phần code của mình.

Chưa có ngày nộp và số giờ/tuần được xác nhận. Các giai đoạn trên là thứ tự triển khai, không phải lịch tuần đã cam kết. Thành viên 4 có phạm vi rộng; nếu thiếu thời gian cần trao đổi phân chia việc sau khi hoàn thành module phụ thuộc, không tự cắt nghiệp vụ đã chốt.

## 9. Yêu cầu nộp của giảng viên

Đã đọc lại hai PDF gốc ngày 01/10/2026. Đây là yêu cầu của tài liệu môn học, tách biệt với các lựa chọn triển khai của nhóm:

- Nhóm 3–5 người; nhóm hiện tại có 4 người, phù hợp.
- Nộp **Word `.doc/.docx`**, thư mục mã nguồn và các tập tin liên quan; **PowerPoint không bắt buộc**.
- **Bắt buộc triển khai trên một hosting miễn phí.** Chạy localhost chưa đáp ứng yêu cầu này.
- Gom bài vào một thư mục và nén ZIP. Mẫu trong đề: `GroupXX_MSSV1_MSSV2_MSSV3_MSSV4_MSSV5.zip`; điền số nhóm và MSSV theo nhóm thực tế/hướng dẫn lớp. Tài liệu nhấn mạnh sai tên ZIP sẽ không được chấm. Chưa có số nhóm và danh sách MSSV để đặt tên cuối.
- Cấu trúc Word được gợi ý: (1) tổng quan và phân công; (2) MVC và ít nhất 3 module/kỹ thuật Spring; (3) use case diagram, yêu cầu chức năng/phi chức năng; (4) ERD, đầy đủ bảng/quan hệ/cột; (5) ảnh và mô tả các giao diện/flow quan trọng; (6) kết luận/ưu nhược điểm/hướng phát triển; phụ lục cài đặt/cấu hình; tài liệu tham khảo.
- Có thể giải thích các kỹ thuật đã dùng thật: Spring MVC, Spring Data JPA/Entity mapping/Repository, Spring Transaction, Spring Security, Bean Validation. Không liệt kê kỹ thuật chưa triển khai như thể đã dùng.

Báo cáo Word, hosting và gói ZIP cuối chưa được xác minh là đã hoàn thành. Bản tổng hợp Markdown này không thay thế Word bắt buộc.

## 10. Những file cần giữ và gửi cho AI/đồng đội

### 10.1. Trong repository `quan-ly-de-tai`

Các đường dẫn sau tính từ thư mục repository, để vẫn dùng được khi clone trên máy khác.

| File/thư mục | Vai trò |
|---|---|
| `docs/TONG_HOP_DU_AN.md` | Chính tài liệu này: bối cảnh để chuyển sang cuộc trò chuyện mới |
| `docs/PHAM_VI_CHUC_NANG.md` | Danh sách chức năng và phạm vi hiện hành; dùng thay các file “kế hoạch” cũ |
| `docs/HUONG_DAN_LAM_BACKEND_NHOM.md` | Phân công, Git, kiến trúc, DB local; mục 10/11/12 hướng dẫn thành viên 1/2/3 và test MySQL |
| `README.md` | Môi trường, biến cấu hình, bootstrap admin và lệnh chạy |
| `pom.xml` | Java/dependency/plugin Maven |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.mvn/settings.xml` | Chạy Maven và cấu hình cache |
| `.gitignore` | Ngăn đưa mật khẩu/cấu hình local/cache/file sinh ra vào Git |
| `src/main/resources/application.yml` | Datasource, Flyway, JPA, upload, bootstrap; dùng biến môi trường |
| `src/main/resources/db/migration/V1__khoi_tao_24_bang.sql` | Nguồn schema chuẩn để ứng dụng tạo DB mới |
| `src/main/java/com/khoacntt/quanlydetai/` | Toàn bộ code backend; cần gửi toàn bộ khi nhờ sửa/tích hợp |
| `src/main/resources/templates/`, `static/` | Giao diện Thymeleaf, CSS và tài nguyên web |
| `src/test/java/` | Test tự động, cần giữ khi bàn giao |

File đọc trọng tâm phần thành viên 1: `account/service/TaiKhoanService.java`, `TaiKhoanController.java`, `TaiKhoanRepository.java`, `TaiKhoan.java`, `MatKhauController.java`, `KiemTraTaiKhoanFilter.java`, `DatabaseUserDetailsService.java`, `KhoiTaoQuanTriVien.java`; `hoso/service/HoSoService.java`, `BoMonService.java`, các Entity/Repository/Controller và `form/`, `dto/`; `config/SecurityConfig.java`; `common/security/NguoiDungHienTai.java`; các test tương ứng. Các đường dẫn module ở câu này nằm dưới package gốc đã nêu.

### 10.2. Bộ database ngoài repository

Vị trí hiện tại: `C:\Users\Admin\Documents\Codex\2026-09-17\t-i-ang-h-c-m\outputs\database_24_bang\`.

**Thư mục này nằm ngoài Git root `quan-ly-de-tai`; clone repository không tự tải được bộ này.**

| File | Cần dùng khi nào? |
|---|---|
| `00_DOC_TRUOC.md` | Đọc cách tổ chức DB, giữ chỗ, snapshot và ranh giới DB/Service |
| `01_SCHEMA_MYSQL_24_BANG.sql` | DDL độc lập cho schema thử rỗng; ứng dụng chính dùng migration V1 |
| `02_TU_DIEN_DATABASE_24_BANG.md` | Tra chính xác cột/kiểu/NULL/khóa/constraint/index và nghiệp vụ |
| `03_ERD_24_BANG.mmd` | Nguồn sơ đồ Mermaid; render thành hình cho báo cáo |
| `04_DU_LIEU_MAU.sql` | Fixture SQL phủ 24 bảng, chỉ nạp vào schema thử phù hợp |
| `05_KIEM_THU_RANG_BUOC.sql` | 61 ca kiểm thử ràng buộc/phép tính |
| `06_CHAY_KIEM_THU.py` | Runner tạo schema riêng, nạp DDL/seed/test và kiểm tra hai kết nối |
| `07_HUONG_DAN_KIEM_THU.md` | Cách chạy, quyền cần có, phạm vi và giới hạn |
| `08_BAO_CAO_KIEM_THU.md` | Bằng chứng SQL đã chạy ngày 22/09/2026, không phải báo cáo tiến độ hiện tại |

Dữ liệu mẫu SQL có tài khoản **BI_KHOA**, hash marker kiểm thử và đường dẫn tệp giả; không dùng các tài khoản đó để demo đăng nhập hoặc xem như tệp báo cáo thật. Không nạp fixture lên `quan_ly_de_tai` đang sử dụng.

### 10.3. Tài liệu gốc và những thứ không cần gửi

- Đề gốc: `D:\MonHoc\lap-trinh-web\Đồ án cuối kỳ.pdf`.
- Quy định nộp: `D:\MonHoc\lap-trinh-web\Yêu cầu khi nộp đồ án cuối kỳ.pdf`.
- Giữ hai PDF để đối chiếu khi phát sinh khác biệt giữa diễn giải của nhóm và yêu cầu giảng viên.
- Không đưa vào Git/ZIP chia sẻ code: mật khẩu, `.env`, `application-local.*`, file client MySQL riêng, `target/`, `.mvn-repository/`, `logs/`, `uploads/` có dữ liệu riêng, bản sao thông tin khôi phục root.
- Không cần gửi lại toàn bộ chat cũ nếu đã gửi bản này và đúng source. Nếu AI cần kiểm tra code, **chỉ gửi link GitHub hiện tại là chưa đủ** vì phần thành viên 1 chưa commit/push; hãy gửi bản source local hoặc bàn giao Git trước.

## 11. Điểm lệch tài liệu và việc chưa chốt cần lưu ý

1. Từ điển DB cũ ghi `INT UNSIGNED` map Java `Long`, nhưng Entity/Repository đang dùng `Integer` và đã qua JPA validation với MySQL trong lần kiểm thử trước. **Không tự đổi riêng một module sang Long**. Cần thống nhất/cập nhật ghi chú, và kiểm tra lại mapping nếu thay kiểu. Integer chỉ phủ miền số dương đến 2^31−1.
2. Các file DB tháng 9 có câu “chưa có backend”, “bước tiếp theo khởi tạo Spring Boot” và “hai schema test được giữ lại”. Đây là lịch sử: hiện đã có khung và các module thành viên 1–4; người dùng đã xóa hai schema test cũ.
3. Script test cũ vẫn dùng tiền tố `codex_doan_test_*` và kiểm tra tiền tố ở các script liên quan. Nếu muốn đổi tên theo sở thích hiện tại, sửa đồng bộ runner/seed/test/hướng dẫn và kiểm tra quyền schema; không đổi riêng một chỗ rồi tắt chặn an toàn.
4. Báo cáo 08 nhắc `outputs/database_test_runs`; thư mục đó không có trong danh sách outputs khi kiểm tra lần này. Giữ báo cáo tổng hợp hiện có; nếu cần log gốc mới thì chạy lại trên schema thử, không giả định log vẫn tồn tại.
5. Ví dụ tên schema `ql_de_tai_sv` trong tài liệu cũ là ví dụ lịch sử, **DB ứng dụng hiện tại là `quan_ly_de_tai`**.
6. Chưa chốt hosting/MySQL host/lưu trữ file khi deploy, ngày nộp, số nhóm và MSSV. Bản local dùng thư mục uploads; các định dạng tệp hiện hành ghi ở mục 13 hướng dẫn backend.
7. Lịch hội đồng là lịch đánh giá đề tài. Bản local chặn trùng GV giữa các đợt và trùng địa điểm hội đồng trong cùng đợt. PB chỉ có thời điểm, chưa có thời lượng; chưa quản lý phòng dùng chung toàn trường.
8. Chỉ có bản local được đọc ngày 01/10. Trước khi tiếp tục, kiểm tra Git và file thực tế để biết đồng đội có cập nhật gì mới; không coi tài liệu bàn giao là trạng thái bất biến.

## 12. Chỉ dẫn cho AI tiếp nhận

Hãy dùng tài liệu này làm bối cảnh hiện hành của đồ án; các phương án cũ chỉ là lịch sử. Tôi là thành viên 1, muốn giải pháp vừa sức sinh viên và hiểu được cách hoạt động.

Trước khi sửa: đọc `docs/PHAM_VI_CHUC_NANG.md`, `docs/HUONG_DAN_LAM_BACKEND_NHOM.md`, migration V1 và code liên quan; kiểm tra nhánh/diff để giữ công việc chưa commit. Nếu chỉ có tài liệu mà chưa có source thì nói rõ giới hạn, không khẳng định đã xem code.

Giữ MVC + Thymeleaf/session, nghiệp vụ tại Service, 24 bảng, không sửa V1. Phân biệt yêu cầu đã chốt, code đã có và việc chưa làm. Không tự thêm kiến trúc phức tạp, không dùng lại mật khẩu từ chat cũ, không thay đổi/xóa DB chính để chạy test.

Khi báo hoàn thành, ghi rõ thay đổi, các test thật sự đã chạy và phần chưa kiểm chứng. Với triển khai/Git, phân biệt code local, commit, push, merge và deploy. Công việc gần nhất phù hợp là **chạy thử/review/bàn giao thành viên 1–4**, so sánh nhánh đồng đội, tích hợp và chuẩn bị deploy/báo cáo. Không tự push hay ghi đè nhánh đồng đội.
