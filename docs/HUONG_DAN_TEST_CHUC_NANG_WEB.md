# Hướng dẫn test website Quản lý Đề tài Sinh viên

Làm theo thứ tự: **chuẩn bị → quản trị → luồng Môn học → sửa điểm → luồng hội đồng → thử lỗi và phân quyền**.

## 1. Chuẩn bị trước khi bắt đầu

### 1.1. Chạy website

Mỗi thành viên dùng MySQL và mật khẩu trên máy mình. Máy mới cần tạo database rỗng `quan_ly_de_tai` và tài khoản có quyền trên database đó theo [README](../README.md) và [hướng dẫn backend](HUONG_DAN_LAM_BACKEND_NHOM.md). Flyway tạo cấu trúc khi khởi động; không import lại SQL vào database đang có dữ liệu.

Mở terminal tại thư mục project có `pom.xml` và `mvnw.cmd`, kiểm tra `./mvnw.cmd -v`: Java phải là **21**. Chọn đúng cú pháp terminal bên dưới.

**PowerShell:**

```powershell
$env:DB_USERNAME = "doan_dev"
$env:DB_PASSWORD = "<mat_khau_mysql_tren_may_ban>"
$env:MAIL_ENABLED = "true"
.\mvnw.cmd --settings .mvn\settings.xml spring-boot:run
```

**CMD:**

```cmd
set "DB_USERNAME=doan_dev"
set "DB_PASSWORD=mat_khau_mysql_tren_may_ban"
set "MAIL_ENABLED=true"
mvnw.cmd --settings .mvn\settings.xml spring-boot:run
```

Thay các giá trị mẫu trước khi chạy. Nếu mật khẩu chứa ký tự đặc biệt của CMD, nên dùng PowerShell và đặt mật khẩu trong dấu nháy đơn. Không gửi mật khẩu lên Git. File `.env` không tự được đọc khi chạy Maven.

- Chờ ứng dụng khởi động xong rồi mở http://localhost:8080/login.
- Tài khoản web khác tài khoản MySQL `root`/`doan_dev`. Chưa có admin thì làm mục **Tạo quản trị viên đầu tiên** trong README; tạo xong phải tắt `APP_BOOTSTRAP_ADMIN_ENABLED` và chạy lại.
- Khi đổi code hoặc biến môi trường: **Ctrl+C → chạy lại trong cùng terminal** để giữ cấu hình database. Chỉ chạy một ứng dụng trên cổng 8080.
- Hai tab cùng profile trình duyệt dùng chung đăng nhập. Để thử nhiều vai trò đồng thời, dùng profile riêng hoặc trình duyệt khác nhau; nếu không, đăng xuất rồi đăng nhập lần lượt.

### 1.2. Chuẩn bị Mailpit để xem thư khi công bố

1. Tải Mailpit Windows từ [trang phát hành](https://github.com/axllent/mailpit/releases), giải nén ngoài thư mục code. Nếu đã có `mailpit.exe` thì dùng lại.
2. Mở terminal riêng tại thư mục chứa `mailpit.exe`, chạy:

```powershell
.\mailpit.exe --listen 127.0.0.1:8025 --smtp 127.0.0.1:1025
```

3. Giữ terminal Mailpit mở, vào http://localhost:8025. Chưa công bố kết quả thì hộp thư trống là bình thường.
4. Đảm bảo terminal Spring Boot đã đặt `MAIL_ENABLED=true` **trước khi chạy ứng dụng**. Nếu ứng dụng đang chạy, Ctrl+C, đặt biến rồi chạy lại.

Cấu hình local mặc định: SMTP `localhost:1025`, không cần tài khoản, mật khẩu hay TLS. Mailpit chỉ nhận thư thử, không chuyển đến Gmail. Xem thêm [hướng dẫn email](HUONG_DAN_EMAIL.md) nếu cần xử lý cấu hình.

### 1.3. Chuẩn bị tài khoản, hồ sơ và tệp

Đăng nhập admin, tạo bộ môn đang hoạt động, ví dụ `THU_CNTT`. Tiếp theo tạo tài khoản, rồi tạo hồ sơ SV/GV tương ứng. Các tên dưới đây là gợi ý tự tạo, không phải tài khoản có sẵn.

| Tài khoản gợi ý | Vai trò cần gán | Dùng để làm gì |
|---|---|---|
| Admin hiện có | ADMIN | Quản lý tài khoản, bộ môn và hồ sơ |
| `thu_truongkhoa` | TRUONG_KHOA | Tạo/triển khai đợt và xử lý nghiệp vụ khoa |
| `thu_canbokhoa` | CAN_BO_KHOA | Công bố đề tài, duyệt đăng ký, tổ chức đánh giá, công bố điểm |
| `thu_truongbomon` | GIANG_VIEN + TRUONG_BO_MON | Duyệt đề tài, phân công GVHD trong bộ môn mình |
| `thu_gv1` | GIANG_VIEN | Đề xuất đề tài và làm GVHD |
| `thu_gv2` | GIANG_VIEN | GVPB, có thể kiêm ủy viên hội đồng |
| `thu_gv3` | GIANG_VIEN | Chủ tịch hội đồng |
| `thu_gv4` | GIANG_VIEN | Thư ký hội đồng |
| `thu_sv1`–`thu_sv6` | SINH_VIEN | Nhóm A: SV1–3; nhóm B: SV4–6 |
| `thu_phu` | SINH_VIEN | Tài khoản phụ chưa có hồ sơ để thử khóa/xóa/đặt lại mật khẩu |

- Tất cả GV và trưởng bộ môn phải có hồ sơ GV, gắn vào `THU_CNTT`. Trưởng bộ môn phụ trách bộ môn ghi trong hồ sơ của mình.
- SV1–6 phải có hồ sơ SV. Chỉ tạo tài khoản đăng nhập là chưa đủ để làm nghiệp vụ.
- Tài khoản phải đang hoạt động; tên đăng nhập, email, mã SV/GV riêng biệt. Có thể dùng email thử `thu_sv1@example.test` để xem trong Mailpit.
- ADMIN không tự có quyền khoa/GV/SV. Dùng các tài khoản riêng ở bảng để tránh nhầm quyền.
- Chuẩn bị báo cáo PDF V1 và V2 có nội dung khác nhau, slide PDF/PPTX, mã nguồn ZIP có tệp bên trong và một PDF thông báo. Mỗi tệp tối đa **50 MiB**; không dùng tệp chỉ đổi đuôi để làm tài liệu hợp lệ.
- Dùng tiền tố `THU_` cho dữ liệu thử. Việc test trên web sẽ lưu dữ liệu vào database ứng dụng đang kết nối.

## 2. Test quản trị và đăng nhập

Dùng tài khoản phụ cho thao tác đổi quyền, khóa, xóa hoặc đặt lại mật khẩu; giữ các tài khoản chính để chạy luồng đề tài.

| Chức năng | Cách thao tác | Điều cần kiểm tra |
|---|---|---|
| Đăng nhập/đăng xuất | Đăng nhập đúng, đăng xuất, mở lại trang riêng; thử mật khẩu sai | Đúng thì vào dashboard; sai hoặc đã đăng xuất thì không truy cập được trang riêng |
| Tạo và sửa tài khoản | Admin → Quản lý tài khoản → Tạo tài khoản; sau đó Sửa | Lưu được thông tin hợp lệ; tên đăng nhập/email trùng hoặc thiếu trường bị từ chối |
| Bộ môn và hồ sơ | Admin tạo/sửa bộ môn, hồ sơ SV/GV; ngừng rồi bật lại bộ môn phụ | Hồ sơ gắn đúng tài khoản/vai trò; không trùng mã; bộ môn ngừng hoạt động không nhận gán GV mới |
| Khóa/mở khóa | Tài khoản phụ đang đăng nhập ở trình duyệt khác; admin bấm Khóa; người đó tải lại trang | Phiên cũ kết thúc; tài khoản khóa không đăng nhập được; mở khóa thì đăng nhập được |
| Đổi vai trò | Admin sửa vai trò tài khoản phụ; người đó tải lại rồi đăng nhập lại | Phiên cũ kết thúc, quyền mới có hiệu lực; không được bỏ vai trò SV/GV khi hồ sơ còn tồn tại |
| Đổi mật khẩu cá nhân | Người dùng → Đổi mật khẩu → nhập mật khẩu cũ, mật khẩu mới, xác nhận | Phải đúng mật khẩu cũ; đổi xong đăng nhập bằng mật khẩu mới, mật khẩu cũ không dùng được |
| Admin đặt lại mật khẩu | Admin → Quản lý tài khoản → Đặt lại mật khẩu của tài khoản phụ → nhập mới và xác nhận | Không cần mật khẩu cũ; mật khẩu mới đăng nhập được; các phiên cũ bị đăng xuất khi tải lại; không tự mở khóa tài khoản |
| Xóa tài khoản | Tạo tài khoản phụ chưa có hồ sơ/dữ liệu → Xóa → xác nhận | Tài khoản biến mất và không đăng nhập được; hủy hộp xác nhận thì không xóa |
| Chặn xóa tài khoản đã dùng | Thử xóa tài khoản có hồ sơ hoặc đã tạo đợt/thông báo | Có thông báo từ chối, dữ liệu giữ nguyên; dùng Khóa nếu muốn ngừng sử dụng |

Admin không được tự khóa, tự xóa hoặc tự bỏ quyền ADMIN. Muốn đổi mật khẩu của chính admin, dùng **Đổi mật khẩu** và nhập mật khẩu hiện tại.

Khi thử đổi/đặt lại mật khẩu, kiểm tra thêm: xác nhận không khớp, mật khẩu dưới 8 ký tự, quá 72 byte UTF-8 hoặc trùng mật khẩu hiện tại phải bị từ chối. Mật khẩu đúng vẫn phải dùng được sau các lần thử sai.

## 3. Test luồng Môn học từ đầu đến cuối

### Bước 1 — Trưởng khoa tạo và triển khai đợt

Đăng nhập `thu_truongkhoa` → **Đợt đăng ký** → tạo đợt loại **MON_HOC**. Đặt mã/tên dễ nhận biết, ví dụ `THU_MH_01`.

Chuẩn bị tài khoản và tệp xong mới đặt lịch. Nếu bắt đầu test lúc 14:00, có thể dùng lịch sau; **đổi theo giờ thực tế trên máy**, không nhập nguyên các giờ đã trôi qua.

| Mốc | Ví dụ | Làm gì trong thời gian này |
|---|---|---|
| GV đăng ký | 14:00–14:20 | GV đề xuất, trưởng bộ môn duyệt/phân công, khoa công bố |
| SV đăng ký | 14:20–14:50 | SV tạo nhóm, nhận lời mời, đăng ký và khoa chấp thuận |
| Hạn nộp báo cáo | 15:10 | Nhóm trưởng nộp cả V1 và V2 trước giờ này |
| Lịch phản biện | 15:15 | Nhập sau ở màn hình lập kế hoạch đánh giá |

**GV kết thúc không muộn hơn SV bắt đầu.** Cho đủ thời gian thao tác; đợt đã triển khai không sửa lịch trên web. MON_HOC để trống hạn điểm PB và ngày hội đồng. Kiểm tra thông tin rồi **Triển khai**.

### Bước 2 — GV đề xuất đề tài

Đăng nhập `thu_gv1` → **Đề tài của tôi** → đề xuất đề tài:

1. Chọn đúng đợt đang mở và bộ môn.
2. Nhập mã, tên, nội dung và các thông tin bắt buộc.
3. Lưu nháp, thử sửa, rồi **Gửi duyệt**.

Đề tài phải chuyển sang chờ duyệt. Làm bước này trong hạn GV đăng ký.

### Bước 3 — Trưởng bộ môn duyệt, phân công; khoa công bố

1. Đăng nhập `thu_truongbomon` → danh sách duyệt đề tài → mở đề tài vừa gửi → chấp thuận.
2. Phân công `thu_gv1` làm GVHD. Lần đầu dùng một người cho đơn giản; hệ thống cho phép 1–2 GVHD. Người đề xuất không tự được gán hướng dẫn.
3. Đăng nhập `thu_canbokhoa` hoặc trưởng khoa → công bố đề tài.
4. Đăng nhập SV để kiểm tra thấy đề tài đã công bố, đúng đợt/bộ môn/GVHD. Chưa tới giai đoạn SV thì chờ tới giờ mới tạo nhóm và đăng ký.

### Bước 4 — SV tạo nhóm và nhận lời mời

Trong thời gian SV đăng ký:

1. Đăng nhập SV1 → **Nhóm và đăng ký** → tạo nhóm A. SV1 trở thành nhóm trưởng.
2. SV1 mời SV2 và SV3.
3. Đăng nhập SV2, vào phần lời mời và đồng ý; làm tương tự với SV3.
4. Quay lại SV1, kiểm tra nhóm đủ **3 SV và đúng một trưởng**. Lời mời chưa được đồng ý chưa tính là thành viên.

Nhóm đang lập có thể ít hơn 3 người; khi đăng ký/chấp thuận phải đủ **3–5 người**.

### Bước 5 — Nhóm trưởng đăng ký, khoa chấp thuận

1. SV1 → chi tiết nhóm → **Chọn / Đổi đề tài** → chọn đề tài đã công bố.
2. Kiểm tra đăng ký ở trạng thái chờ duyệt, đề tài đã được giữ chỗ.
3. Khoa → **Duyệt đăng ký** → mở đúng nhóm và đề tài → chấp thuận.
4. Kiểm tra nhóm đang thực hiện; thay đổi thành viên, nhóm trưởng và đề tài bị khóa.

**Duyệt đề tài ở bước 3 khác với chấp thuận nhóm đăng ký ở bước này.** Phải hoàn tất cả hai mới tới phần thực hiện/nộp bài.

### Bước 6 — Nhóm trưởng nộp báo cáo

Đăng nhập SV1 → **Báo cáo** → mở nhóm/đề tài:

1. Nộp đủ **báo cáo PDF + slide PDF/PPTX + mã nguồn ZIP**, trước hạn báo cáo.
2. Nộp thêm một bộ đầy đủ với PDF V2 có nội dung khác V1.
3. Tải lại cả hai phiên bản để kiểm tra đúng tệp. Mỗi lần nộp là một bộ đủ ba mục; không chỉ gửi riêng PDF mới.

Thành viên thường được xem/tải đúng quyền nhưng không được nộp thay nhóm trưởng.

### Bước 7 — Khoa lập kế hoạch phản biện

Khoa → **Đánh giá → Lập kế hoạch đánh giá**:

1. Chọn đúng nhóm/đề tài đã được chấp thuận.
2. Chọn GVPB là `thu_gv2`, **khác GVHD**.
3. Nhập lịch phản biện từ hạn báo cáo trở đi và địa điểm phản biện.
4. Với MON_HOC/TLCN, **để trống toàn bộ phần Hội đồng**: hội đồng, giờ bắt đầu/kết thúc và địa điểm.
5. Bấm **Lưu kế hoạch**, kiểm tra lại thông tin trên trang chi tiết.

Có thể lập kế hoạch trước giờ phản biện. Sau khi lưu, nếu chưa tới giờ thì chờ; không cần tạo lại đợt. Chỉ sửa kế hoạch trước khi bắt đầu chấm.

### Bước 8 — Chờ đủ điều kiện rồi mở phản biện

Chờ **qua hạn nộp báo cáo và tới giờ phản biện**. Khoa mở chi tiết đánh giá → **Chốt báo cáo cuối và mở phản biện**.

Kiểm tra bản dùng chấm là **V2**, trạng thái phản biện chuyển sang đang chấm. Không mở được thì kiểm tra giờ/hạn và nhóm đã có bộ báo cáo hợp lệ chưa. Không đổi đồng hồ máy hoặc sửa SQL để vượt hạn.

### Bước 9 — GVPB nhập và nộp điểm

Đăng nhập `thu_gv2` → **Đánh giá** → mở đề tài được giao:

1. Xem báo cáo, nhập thử **8.0** và nhận xét.
2. Bấm **Lưu nháp**, kiểm tra dữ liệu được giữ nhưng chưa hoàn tất chấm.
3. Bấm **Nộp chính thức**, kiểm tra phản biện hoàn tất.

Điểm chính thức không sửa trực tiếp; muốn sửa phải gửi yêu cầu như mục 4.

### Bước 10 — Khoa tổng hợp, xác nhận và công bố

1. Khoa mở chi tiết đánh giá → phần **Tổng hợp kết quả** → nhập nhận xét → **Tạo phiên bản kết quả**.
2. Kiểm tra điểm **8.0, Đạt** vì đợt MON_HOC này chỉ có một GVPB chấm.
3. Bấm **Xác nhận**, rồi **Công bố cho sinh viên**. Đây là hai thao tác riêng.
4. Trước khi công bố, đăng nhập SV kiểm tra chưa thấy điểm; sau công bố, SV1–3 phải thấy đúng điểm và nhận xét ở **Kết quả**, lọc theo đợt.
5. SV ngoài nhóm không được thấy kết quả của nhóm A.

### Bước 11 — Xem email trong Mailpit

Mở http://localhost:8025 sau khi công bố:

- Mỗi SV trong nhóm phải có một thư riêng gửi tới email đã khai báo trong tài khoản.
- Mở thư, kiểm tra tên đề tài, đợt, phiên bản kết quả và tiếng Việt.
- Bấm link xem kết quả: cần đăng nhập đúng tài khoản để xem dữ liệu của mình.
- Nếu chưa có thư, kiểm tra Mailpit còn chạy, Spring Boot đã bật `MAIL_ENABLED=true` từ lúc khởi động, và thao tác vừa làm đã là **Công bố**, không chỉ **Xác nhận**.

Không bấm công bố lại cùng phiên bản để gửi lại thư. Ứng dụng chưa hỗ trợ tự gửi lại thư thất bại.

## 4. Test sửa điểm sau khi công bố

Dùng kết quả 8.0 của luồng vừa làm:

1. **GV2:** mở chi tiết đánh giá → nhập lý do xin sửa → **Gửi yêu cầu sửa**.
2. **SV1:** mở Kết quả → vẫn thấy **8.0** và nhãn **Đang điều chỉnh**.
3. **Khoa:** mở yêu cầu, nhập nhận xét và bấm **Chấp thuận sửa**.
4. **GV2:** nhập **9.0** cùng nhận xét mới → **Nộp chính thức**.
5. **SV1:** kiểm tra vẫn thấy bản đã công bố cũ; nhập điểm mới chưa làm kết quả công khai đổi ngay.
6. **Khoa:** tổng hợp lại → **Tạo phiên bản kết quả → Xác nhận → Công bố cho sinh viên**.
7. **SV1:** kiểm tra điểm mới **9.0**, hết nhãn điều chỉnh; Mailpit có thư phiên bản mới.
8. **Khoa:** kiểm tra lịch sử còn phiếu/kết quả cũ, không bị ghi đè.

Nếu đề tài có hội đồng, **chủ tịch** tổng hợp lại ở bước 6; khoa vẫn xác nhận và công bố. Thử thêm một yêu cầu bị khoa từ chối: phải có nhận xét, GV không được nhập phiếu điều chỉnh theo yêu cầu bị từ chối.

## 5. Test luồng có hội đồng và hai loại còn lại

### 5.1. Chọn loại đợt

| Loại | Đánh giá | Hạn điểm PB cấp đợt | Ngày hội đồng cấp đợt |
|---|---|---|---|
| MON_HOC | GVPB riêng | Để trống | Để trống |
| TLCN | GVPB riêng | Bắt buộc | Để trống |
| NCKH | PB rồi hội đồng | Để trống | Để trống; vẫn phải xếp giờ hội đồng trong kế hoạch |
| KLTN | PB rồi hội đồng | Bắt buộc | Bắt buộc, khớp ngày hội đồng trong kế hoạch |

Với TLCN, chạy như mục 3 và bảo đảm GVPB nộp trước hạn điểm PB. Với NCKH/KLTN, làm thêm các bước hội đồng dưới đây. Mỗi loại dùng một đợt riêng; một SV được tham gia nhóm ở các đợt khác nhau.

### 5.2. Tạo hội đồng và lập kế hoạch KLTN

1. Trưởng khoa tạo đợt KLTN: lịch GV/SV không chồng nhau, hạn báo cáo trước lịch PB, PB trước hạn điểm PB và trước giờ hội đồng. Dành đủ thời gian GVPB nhập điểm.
2. Thực hiện lại mục 3, bước 2–6: đề xuất → duyệt/phân công → công bố → nhóm → đăng ký/chấp thuận → nộp báo cáo.
3. Khoa → **Hội đồng** → tạo hội đồng cùng đợt: **GV3 chủ tịch, GV4 thư ký, GV2 ủy viên**. Không đưa GV1/GVHD vào.
4. Kiểm tra đủ ba người khác nhau rồi **Chốt**. Hội đồng còn nháp không dùng cho kế hoạch.
5. Khoa lập kế hoạch: chọn GVPB **GV2**, hội đồng vừa chốt, giờ/địa điểm PB và hội đồng. Hội đồng sau PB; ngày hội đồng KLTN phải khớp ngày đã khai báo ở đợt.

### 5.3. Chấm và công bố

1. Chờ đủ điều kiện như mục 3, bước 8; khoa chốt báo cáo và mở PB.
2. GV2 nhập **8.0**, nhận xét và nộp chính thức trước hạn điểm PB.
3. Chờ tới giờ hội đồng; sau khi PB hoàn tất, khoa mở chi tiết đánh giá → **Mở chấm hội đồng**.
4. GV3 nộp **7.0**, GV4 nộp **9.0** và nhận xét. GV2 đã có điểm PB, không cần phiếu thứ hai vì kiêm ủy viên.
5. **GV3/chủ tịch** mở chi tiết → nhập nhận xét tổng hợp → **Tạo phiên bản kết quả**.
6. **Khoa** xác nhận và công bố. SV xem kết quả, Mailpit nhận thư.
7. Kiểm tra điểm cuối **(8 + 7 + 9) / 3 = 8.0**; mỗi GV chỉ tính một điểm, GVHD không có điểm chấm.
8. Khoa thử hoàn thành hội đồng khi mọi đề tài được giao đã chấm xong. Nếu còn đề tài chưa xong phải bị chặn.

Thử thêm GVPB ngoài hội đồng: dùng một GV khác không phải GVHD làm GVPB, ba thành viên hội đồng chấm riêng → cần đủ **bốn người chấm**. Kiểm tra tương tự với hội đồng hợp lệ tối đa năm GV.

## 6. Test thông báo và xóa có điều kiện

### Thông báo

1. Khoa/admin → **Thông báo → Soạn thông báo** → chọn đơn vị, nhập tiêu đề/nội dung → lưu nháp.
2. Thêm tệp đính kèm; SV chưa được xem bài hoặc tệp nháp.
3. Công bố bài → SV/GV đã đăng nhập xem và tải được; người chưa đăng nhập không xem được.
4. Thu hồi → SV không còn xem/tải. Sửa nội dung khi đã thu hồi rồi công bố lại để kiểm tra vòng cập nhật.
5. Thử thiếu trường, tệp rỗng/sai định dạng/quá 50 MiB hoặc thêm quá 10 tệp → bị từ chối. Admin quản lý được bài; cán bộ khoa khác người đăng không được tự sửa/thu hồi bài đó.

### Xóa dữ liệu

Dùng bản ghi phụ để thử; không xóa dữ liệu cần chạy demo.

| Loại dữ liệu | Thử xóa hợp lệ | Trường hợp phải bị chặn |
|---|---|---|
| Tài khoản | Admin xóa tài khoản khác chưa có hồ sơ/dữ liệu | Tự xóa hoặc đã có liên kết; không tự xóa kèm hồ sơ |
| Bộ môn, hồ sơ SV/GV | Admin xóa bản ghi chưa được sử dụng | Có dữ liệu tham chiếu; hồ sơ GV giữ vai trò trưởng bộ môn cần bỏ vai trò trước |
| Đợt | Trưởng khoa xóa đợt nháp chưa có dữ liệu liên quan | Đợt đã triển khai hoặc đã có liên kết |
| Đề tài | GV xóa đề tài nháp của mình trong hạn được sửa | Đề tài của người khác, ngoài hạn hoặc không còn nháp |
| Thông báo | Người có quyền xóa bài nháp chưa có tệp | Bài có tệp/đã công bố; bài công bố dùng Thu hồi |

## 7. Test lỗi, quyền và thao tác đồng thời

Làm trên nhóm/đợt phụ. Một thao tác bị từ chối đúng quy tắc là hệ thống xử lý đúng; không phải lúc nào hiện thông báo từ chối cũng là lỗi.

| Phần | Cách thử | Điều cần thấy |
|---|---|---|
| Đợt | Nhập lịch đầu >= cuối, GV/SV chồng nhau; thiếu hoặc nhập thêm hạn/ngày không áp dụng | Báo lỗi; cán bộ khoa không tạo được đợt thay trưởng khoa |
| Đề tài | SV xem đề tài chưa công bố; trưởng bộ môn khác bộ môn thử duyệt/phân công | Không được xem/xử lý trái quyền |
| GVHD | Thử 0 hoặc 3 GVHD; công bố khi chưa phân công | Bị chặn; danh sách 1–2 GV hợp lệ được chấp nhận |
| Nhóm | Đăng ký khi chỉ 1–2 SV; thêm người thứ 6; vào hai nhóm cùng đợt | Bị chặn; nhóm 3–5 người đăng ký được |
| Lời mời | Từ chối/thu hồi; gửi trùng lời mời; mời SV đã có nhóm cùng đợt | Không tự thêm thành viên; không nhận lời mời đã thu hồi |
| Sửa nhóm | Đổi trưởng, rời nhóm, sửa thành viên trước duyệt khi còn hạn; thử lại sau chấp thuận | Trước đủ điều kiện được làm, sau duyệt bị khóa |
| Đăng ký | Thành viên thường chọn đề tài; đổi sang đề tài đã được giữ | Bị chặn; đổi thất bại phải giữ nguyên đăng ký cũ |
| Từ chối đăng ký | Khoa từ chối có lý do; nhóm chọn đề tài khác khi còn hạn | Được đăng ký lại; đề tài bị từ chối giữ chỗ được giải phóng |
| Báo cáo | Thiếu tệp, tệp giả/rỗng, quá dung lượng; thành viên thường nộp hoặc trưởng nộp quá hạn | Bị từ chối, không tạo phiên bản thiếu tài liệu |
| Tải tệp | Lấy link thật bằng người có quyền, mở bằng SV/GV ngoài phân công hoặc khi đã đăng xuất | Không tải được trái quyền; thành viên nhóm/GVHD/người chấm được giao/khoa tải đúng quyền |
| Hội đồng | Thử 2 hoặc 6 người, trùng GV, thiếu chủ tịch/thư ký, hội đồng nháp hoặc khác đợt | Bị chặn |
| Cấm GVHD chấm | Gán GVHD làm GVPB/thành viên hội đồng; hoặc đổi GVHD thành người đã được giao chấm | Bị chặn cả hai chiều |
| Lịch đánh giá | Mở PB sớm, mở hội đồng khi PB chưa xong/chưa tới giờ; phân công cùng GV trùng lịch | Bị chặn; trùng địa điểm hội đồng trong cùng đợt cũng bị chặn |
| Điểm | Nhập -1, >10, quá hai chữ số thập phân, thiếu nhận xét; thử hai biên 0 và 10 | Dữ liệu sai bị chặn, hai biên hợp lệ được chấp nhận |
| Tổng hợp | Thiếu phiếu chính thức hoặc người không phải chủ tịch tổng hợp đề tài có hội đồng | Bị chặn |
| Làm tròn/đạt | Trên đề tài phụ, tất cả người chấm nhập 3.95; đề tài khác tất cả nhập 3.94 | Lần lượt 4.0 đạt và 3.9 không đạt; xét ngưỡng sau làm tròn một chữ số |
| Sửa điểm | Chưa được duyệt mà sửa, gửi yêu cầu trùng, tự duyệt yêu cầu của mình, công bố khi còn điều chỉnh dang dở | Bị chặn; bản đã công bố cũ được giữ |
| Xem kết quả | SV đổi bộ lọc đợt, thử ID đợt/nhóm của người khác | Chỉ xem được đề tài mình tham gia và kết quả đã công bố |
| URL trực tiếp | SV/GV mở `/admin/tai-khoan`; thử thay ID trong link nhóm/báo cáo/đánh giá bằng ID thật của người khác | Backend chặn dù tự gõ URL, không chỉ ẩn menu |
| CSRF | Trên form thử, dùng DevTools xóa input ẩn `_csrf` rồi gửi | Bị chặn, dữ liệu không đổi; tải lại trang để lấy form mới |
| Email lỗi | Dừng Mailpit, công bố kết quả của đề tài phụ khác | Kết quả vẫn được công bố, log báo gửi thất bại; thư không tự được gửi lại khi mở Mailpit |

**Thử gần đồng thời bằng hai profile độc lập:**

1. Hai nhóm đủ 3–5 người cùng chọn một đề tài còn trống → chỉ một nhóm giữ được.
2. Một SV đồng ý lời mời từ hai nhóm cùng đợt → chỉ thuộc một nhóm.
3. Nhóm đang bốn người, hai SV khác cùng nhận lời → chỉ thêm một người, tổng tối đa năm.

Bấm tay chỉ kiểm tra sơ bộ. Nếu cần kiểm thử transaction tự động, xem hướng dẫn backend; không chạy script test vào database chính.

Khi gặp vấn đề, kiểm tra đúng vai trò/hồ sơ, đúng hạn và đúng trạng thái trước. Nếu vẫn lỗi, gửi người phụ trách **vai trò đang dùng, mã đợt/nhóm/đề tài, các bước thao tác, thời gian, ảnh lỗi và phần đầu log có ERROR/Caused by**; không gửi mật khẩu. Sau khi deploy, chạy lại luồng chính bằng URL online.
