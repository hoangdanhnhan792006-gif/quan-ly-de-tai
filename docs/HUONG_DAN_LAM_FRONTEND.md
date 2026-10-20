# Hướng dẫn bảo trì frontend và ghép với backend

Cập nhật 20/10/2026. FE đã được ghép vào ứng dụng MVC; tài liệu dùng khi chỉnh tiếp, không yêu cầu dựng lại giao diện hay project FE riêng.

## 1. Nhận code và đọc tài liệu nào?

Nhận **toàn bộ repository**, bắt đầu từ bản mới nhất của nhánh `develop`. Đọc theo thứ tự:

1. [README](../README.md): cài Java 21, MySQL, chạy ứng dụng và tạo admin đầu tiên.
2. [Phạm vi chức năng](PHAM_VI_CHUC_NANG.md): hiểu người dùng, nghiệp vụ và các chức năng cần có.
3. **Tài liệu này**: trang cần sửa, dữ liệu cần giữ và cách ghép giao diện.
4. [Hướng dẫn test web](HUONG_DAN_TEST_CHUC_NANG_WEB.md): tạo dữ liệu, thử từng vai trò và từng luồng.
5. [Hướng dẫn email](HUONG_DAN_EMAIL.md): chỉ cần khi thử Mailpit hoặc sửa mẫu email.

Mỗi máy tự cấu hình MySQL và tạo tài khoản web thử; dữ liệu và mật khẩu máy người khác không đi theo Git. Không cần gửi thêm mật khẩu, SQL riêng cho frontend hoặc toàn bộ lịch sử thảo luận.

## 2. Cách làm thống nhất

Project dùng **Spring Boot + Spring MVC + Thymeleaf**, xác thực bằng session. Giao diện HTML hiện tại đã kết nối backend; frontend thiết kế lại bố cục, màu sắc, menu, bảng, form, thông báo và responsive trực tiếp trên các trang này.

Luồng dữ liệu: `Trình duyệt → Controller → Service → Database → DTO/Model → Thymeleaf → trình duyệt`.

- Chạy Spring Boot và xem bằng `http://localhost:8080`, hoặc tên miền local HTTPS khi máy đã cấu hình theo HTTPS_LOCAL.md. Mở HTML trực tiếp hoặc bằng Live Server chỉ xem phần tĩnh, không kiểm tra được dữ liệu, session và quyền.
- Giữ kiến trúc MVC hiện có. Chưa cần React/Vue, JWT, REST API hoặc project frontend chạy riêng. Nếu muốn đổi kiến trúc, phải thống nhất lại API và xác thực trước khi code.
- Backend quyết định quyền, trạng thái, thời hạn, giữ chỗ, tính điểm và công bố. JavaScript chỉ hỗ trợ trải nghiệm và kiểm tra nhập liệu sớm.

## 3. Thành phần dùng chung đang có

- `templates/fragments/layout.html`: head, font/CSS và favicon. `dieu-huong.html`: sidebar theo quyền + mobile; `nhan.html`: nhãn vai trò/trạng thái.
- `static/css/app.css`: màu, font, form, bảng/nút; giữ management-table, table-actions, account-table để nút link/form cùng chiều cao và cột Khóa/Mở khóa không làm lệch Xóa.
- `static/js/form.js`: con mắt, đối chiếu mật khẩu và chặn required chỉ dấu cách, kể cả input không khai báo type.
- `static/js/menu.js`: menu mobile, Esc và đánh dấu trang đang mở. `chong-gui-lai.js`: khóa nút trong lúc nộp tệp, mở lại khi quay về từ bfcache.
- `static/images/logo.png`: logo người dùng cung cấp, dùng ở trang chủ/login/menu/favicon. Không tạo ảnh giả hoặc link ổ đĩa cá nhân.

### Thư mục cần sử dụng

| Đường dẫn trong project | Công dụng |
|---|---|
| `src/main/resources/templates/` | Các trang HTML động cần thiết kế lại |
| `templates/fragments/dieu-huong.html` | Menu dùng chung; đường dẫn này nằm dưới `src/main/resources/` |
| `src/main/resources/static/css/app.css` | CSS dùng chung; thống nhất người phụ trách nếu nhiều người cùng sửa |
| `src/main/resources/static/` | Thêm ảnh, CSS, JavaScript tại đây và liên kết từ template |
| `src/main/java/com/khoacntt/quanlydetai/<module>/controller/` | Đọc URL, tên trang và `model.addAttribute(...)` |
| `src/main/java/com/khoacntt/quanlydetai/<module>/dto/` | Các trường dữ liệu backend đưa lên giao diện |
| `src/main/java/com/khoacntt/quanlydetai/<module>/form/` | Tên trường gửi lên, kiểu dữ liệu và validation |
| `src/main/java/com/khoacntt/quanlydetai/<module>/model/` | Tra enum vai trò/trạng thái khi cần; giữ nguyên mã enum |

Tệp báo cáo/thông báo được tải qua Controller có kiểm tra quyền. Không chuyển tệp upload sang `static`, không dùng đường dẫn ổ đĩa làm link tải.

## 4. Bản đồ màn hình đã có

Tên file tính từ `src/main/resources/templates/`. Trong mỗi hàng, các file sau dấu phẩy vẫn thuộc thư mục ghi ở đầu. Nhiều URL dùng chung template; giữ các điều kiện phân biệt chế độ hiện có.

| Phần / người dùng | Template | URL chính | Nội dung cần giữ |
|---|---|---|---|
| Dùng chung | `home.html`, `login.html`, `dashboard.html` | `/`, `/login`, `/dashboard` | Trang chủ, đăng nhập, dashboard; chỉ hiển thị thống kê khi backend có dữ liệu |
| Menu | `fragments/dieu-huong.html` | Nhúng vào các trang | Menu theo quyền, hồ sơ, đổi mật khẩu, đăng xuất |
| Tài khoản — admin | `account/danh-sach.html`, `tao.html`, `sua.html` | `/admin/tai-khoan` | Tạo/sửa, nhiều vai trò, khóa/mở khóa, xóa; tên đăng nhập/trạng thái/mật khẩu mới nằm trong trang Sửa |
| Mật khẩu cá nhân | `account/doi-mat-khau.html` | `/ho-so/doi-mat-khau` | Mật khẩu hiện tại, mới, xác nhận |
| Bộ môn — admin | `hoso/bo-mon-danh-sach.html`, `bo-mon-form.html` | `/admin/bo-mon` | Tạo/sửa, hoạt động/ngừng, xóa có điều kiện |
| Hồ sơ SV — admin | `hoso/sinh-vien-danh-sach.html`, `sinh-vien-form.html` | `/admin/sinh-vien` | Chọn tài khoản, mã SV, lớp, khóa học, sửa/xóa |
| Hồ sơ GV — admin | `hoso/giang-vien-danh-sach.html`, `giang-vien-form.html` | `/admin/giang-vien` | Chọn tài khoản, mã GV, bộ môn, học vị, sửa/xóa |
| Hồ sơ cá nhân | `hoso/ca-nhan.html` | `/ho-so` | Thông tin đúng người đăng nhập |
| Đợt — khoa | `dotdangky/danh-sach.html`, `form.html`, `chi-tiet.html` | `/khoa/dot-dang-ky` | Loại, các hạn, triển khai/kết thúc/hủy; trưởng khoa tạo đợt |
| Đề tài | `detai/danh-sach.html`, `form.html`, `chi-tiet.html` | `/de-tai`, `/de-tai/cua-toi`, `/de-tai/quan-ly` | Danh sách/bộ lọc, đề xuất/gửi duyệt, bộ môn duyệt/phân công, khoa công bố |
| Nhóm — SV | `nhom/danh-sach.html`, `form.html`, `chi-tiet.html` | `/sinh-vien/nhom` | Nhóm theo đợt, lời mời, trưởng/thành viên, đổi tên/rời/giải thể |
| Đăng ký — nhóm trưởng | `dangky/form.html` | `/sinh-vien/dang-ky/{nhomId}` | Chọn/đổi đề tài; vào từ chi tiết nhóm |
| Duyệt đăng ký — khoa | `dangky/quan-ly.html`, `nhom.html` | `/khoa/dang-ky` | Xem nhóm/thành viên/đề tài, chấp thuận/từ chối và lý do |
| Báo cáo | `baocao/danh-sach.html`, `chi-tiet.html` | `/bao-cao`, `/bao-cao/dang-ky/{id}` | Hạn nộp, ba tệp bắt buộc, phiên bản, tải tệp, quyền nộp |
| Hội đồng — khoa | `danhgia/hoi-dong.html`, `hoi-dong-form.html` | `/khoa/hoi-dong` | Chủ tịch/thư ký/ủy viên, chốt/hoàn thành |
| Kế hoạch — khoa | `danhgia/ke-hoach.html` | `/khoa/danh-gia/tao` | Chọn đăng ký/GVPB, lịch/địa điểm PB, hội đồng nếu áp dụng |
| Đánh giá — GV/khoa | `danhgia/danh-sach.html`, `chi-tiet.html` | `/danh-gia`, `/danh-gia/{id}` | Chốt báo cáo, mở chấm, điểm, sửa điểm, tổng hợp, xác nhận/công bố, lịch sử |
| Kết quả — SV | `danhgia/ket-qua-sinh-vien.html` | `/sinh-vien/ket-qua` | Lọc theo đợt, điểm/nhận xét, đạt, phiên bản, Đang điều chỉnh |
| Thông báo | `thongbao/danh-sach.html`, `form.html`, `chi-tiet.html` | `/thong-bao`, `/thong-bao/quan-ly` | Danh sách/chi tiết, soạn/sửa, tệp, công bố/thu hồi/xóa đúng quyền |
| Lỗi | `error/403.html`, `404.html`, `500.html`, `business.html` | Backend hiển thị theo tình huống | Thông báo rõ, nút quay lại; không hiện stack trace |
| Email, nếu sửa | `mail/ket-qua-de-tai.html` | Xem trong Mailpit | HTML email, CSS inline; giữ dữ liệu động và link kết quả |

`{id}` là ID thật. **ID nhóm, đăng ký, đề tài, đánh giá và kết quả khác nhau**; không thay thế cho nhau dù giá trị tình cờ giống nhau. Dùng link/action sẵn có để giữ đúng loại ID.

## 5. Dữ liệu và form: giữ gì để ghép được ngay?

### 5.1. Cách đọc một trang

Ví dụ báo cáo: đọc `baocao/controller/BaoCaoController.java` để biết Model có `hoSo` và `form`; đọc `baocao/dto/HoSoBaoCaoDto.java` để biết `dangKyId`, `maNhom`, `tenDeTai`, `hanNop`, `coTheNop`, `baoCaos`; đọc `baocao/form/NopBaoCaoForm.java` để biết dữ liệu nhập; rồi sửa `templates/baocao/chi-tiet.html`.

Áp dụng **Controller → DTO/Form → template** cho phân hệ khác. Form là dữ liệu nhập; DTO là dữ liệu hiển thị. Không đổi tên thuộc tính Java thành tên cột snake_case của SQL.

### 5.2. Các quy tắc khi đổi bố cục

- Giữ `xmlns:th`, `th:action`, `th:href`, `method`, `enctype`, `th:object`, `th:field`, `name/value` và ID trong URL/input ẩn. Có thể đổi class, bố cục, label nhưng giữ liên kết label/input.
- Giữ ý nghĩa `th:if`, `th:unless`, `th:each`, `th:text` và cờ quyền/hành động trong DTO; có thể chuyển sang phần tử khác nếu vẫn hoạt động đúng.
- Giữ POST cho thao tác lưu/xóa/duyệt/công bố/đăng xuất. Form POST dùng `th:action` để tích hợp CSRF; không xóa input `_csrf` khỏi HTML đã render.
- Giữ tên/giá trị nút submit: `nop=false/true` phân biệt lưu nháp/nộp chính thức; `chapThuan=true/false` phân biệt chấp thuận/từ chối.
- Đăng nhập dùng form POST `/login` với input `username`, `password`; đăng xuất dùng POST `/logout`. Hai chức năng này do Spring Security xử lý, không có Form Java riêng trong bảng tra cứu. Giữ session hiện có; không tự lưu token hay mật khẩu ở trình duyệt.
- Nút chỉ mở modal/menu dùng `type="button"`; nút gửi dùng `type="submit"`. Không lồng form trong form.
- Upload giữ `multipart/form-data`. Báo cáo có input **`pdf`, `slide`, `source`**, mỗi lần nộp đủ ba tệp. Khi có lỗi cần cho chọn lại tệp vì trình duyệt không tự điền tệp cũ.
- Giữ thông báo `thanhCong`, `loi` và khối lỗi `#fields`. Lỗi nhập liệu thường trả lại form; thành công thường chuyển trang.
- Mật khẩu mặc định là `type="password"`; `static/js/form.js` thêm nút con mắt hiện/ẩn từng ô. Giữ script và class `password-field/password-toggle` khi sửa CSS; `/js/**` được mở cho trang đăng nhập. Không điền lại mật khẩu vào HTML, lưu localStorage hoặc ghi console.
- Trang Sửa có `tenDangNhap`, `trangThai`, `matKhauMoi`, `xacNhanMatKhau`; để trống hai ô mật khẩu thì giữ mật khẩu hiện tại. Giữ `data-optional="true"` trên ô mật khẩu mới. Trạng thái và vai trò luôn gửi cùng form. Không thêm lại màn hình đặt lại riêng.
- Hồ sơ SV bắt buộc `lop`, `khoaHoc`; tạo/đổi tên nhóm bắt buộc `tenNhom`; đề tài bắt buộc `yeuCau`, `sanPhamDuKien`. Giữ `required`, dấu * và lỗi `th:errors` dưới các trường mới. Các trường chỉ có dấu cách bị chặn ở backend.
- Hiển thị nội dung người dùng bằng `th:text`; không đổi nhận xét/thông báo thành `th:utext` hoặc `innerHTML` khi chưa có xử lý an toàn.
- Input ngày giờ dùng `datetime-local`, nhập giờ Việt Nam. Backend đã chuyển UTC khi lưu và đổi lại khi hiển thị; frontend không tự cộng thêm 7 giờ.
- Giữ mã enum/ID trong `option value`, chỉ sửa nhãn hiển thị. Checkbox nhiều vai trò/thành viên giữ tên trường dạng tập hợp.

Ví dụ phần tử cần giữ khi thiết kế báo cáo:

```html
<h1 th:text="${hoSo.tenDeTai}"></h1>
<form th:if="${hoSo.coTheNop}" th:object="${form}"
      th:action="@{/bao-cao/dang-ky/{id}/nop(id=${hoSo.dangKyId})}"
      method="post" enctype="multipart/form-data">
    <!-- Giữ khối lỗi và toàn bộ trường của form gốc. -->
    <label for="pdf">Báo cáo PDF</label>
    <input id="pdf" type="file" name="pdf" accept=".pdf" required>
    <!-- Còn slide, source, ghiChu và nút submit trong template gốc. -->
</form>
```

Đây chỉ là ví dụ cấu trúc; không dùng để chép đè toàn bộ trang hiện có.

### 5.3. Quyền và trạng thái cần thể hiện

Menu dùng các biến từ `account/controller/DieuHuongAdvice.java`: `laQuanTriVien`, `laTruongKhoa`, `laCanBoKhoa`, `laTruongBoMon`, `laGiangVien`, `laSinhVien`. `laCanBoKhoa` bao gồm trưởng khoa. Một tài khoản có nhiều vai trò thì thấy các menu tương ứng, không chỉ lấy vai trò đầu tiên.

Quyền bản ghi dùng các cờ đang có, ví dụ `nhom.truongNhom`, `nhom.coTheSua`, `hoSo.coTheNop`, `p.duocCham`, `p.duocXinSua`, `danhGia.laChuTich`. Không bật tất cả nút chỉ vì người dùng là GV/khoa. Không thêm `#authentication` khi chưa có dialect hỗ trợ; dùng biến Model đang có.

| Phần | Quy tắc giao diện cần giữ |
|---|---|
| Nhóm | 3–5 SV khi đăng ký/chấp thuận, một trưởng; nhóm đang lập có thể ít hơn 3; sau duyệt/hết hạn khóa theo cờ backend |
| Giữ chỗ | Phân biệt còn trống/đang giữ/đã nhận; chờ phản hồi backend mới báo đăng ký thành công |
| Đợt | Loại và các hạn rõ ràng; GV/SV không chồng giai đoạn |
| Đề tài | Bộ môn duyệt/phân công, khoa công bố; khác với chấp thuận nhóm đăng ký |
| Báo cáo | PDF + slide PDF/PPTX + ZIP, tối đa 50 MiB/tệp; giữ lịch sử và bản được dùng chấm |
| Kế hoạch | MON_HOC/TLCN không có hội đồng; NCKH/KLTN có hội đồng 3–5 GV; bỏ trống phần không áp dụng |
| Mở chấm | Lập kế hoạch khác mở phản biện; chỉ mở khi đủ giờ/hạn/báo cáo. Hội đồng sau PB hoàn tất và tới lịch |
| Điểm | Phân biệt nháp/chính thức; GVHD không chấm; GVPB kiêm hội đồng chỉ tính một điểm |
| Kết quả | Tổng hợp → xác nhận → công bố là ba thao tác; SV chưa thấy bản chưa công bố |
| Sửa điểm | Giữ bản công bố cũ và nhãn Đang điều chỉnh; không tự thay bằng điểm chưa công bố |
| Tài khoản | Mật khẩu mới tích hợp vào Sửa cùng tên đăng nhập và trạng thái; không tự xóa hoặc bỏ qua mật khẩu cũ của mình; tài khoản có dữ liệu dùng Khóa |
| Thông báo | Nháp/công bố/thu hồi, bài và tệp đúng quyền |

Điểm cuối, đạt/không đạt, quyền và trạng thái lấy từ backend; không tự tính lại bằng JavaScript.

## 6. Cách làm và bàn giao

1. Chạy giao diện gốc và thử từng vai trò trước khi sửa. Chuẩn bị dữ liệu theo hướng dẫn test để tránh nhầm màn hình trống với lỗi.
2. Chốt màu, font, menu, bảng, form, nút, badge và thông báo chung. Làm đăng nhập/dashboard trước, rồi từng phân hệ.
3. Sửa và thử ngay với backend thật sau mỗi phân hệ; không đợi xong hết mới ghép.
4. Kiểm tra máy tính/điện thoại: menu dùng được, bảng cuộn ngang khi cần, form có label, văn bản dài không phá bố cục.
5. Phân biệt chưa có dữ liệu, chưa tới giờ, chưa đủ quyền và lỗi hệ thống. Không thêm biểu đồ/số liệu minh họa rồi coi là đã kết nối backend.

**Git:** lưu/commit công việc đang làm trước khi chuyển nhánh. Lấy bản mới `develop`, tạo nhánh riêng như `feature/giao-dien`. Thống nhất người phụ trách CSS/menu chung. Push nhánh frontend và tạo Pull Request vào `develop` để kiểm tra trước khi ghép.

Nếu thiếu dữ liệu hoặc cần hành động mới, gửi người làm backend: **trang đang làm → dữ liệu/thao tác thiếu → ví dụ hiển thị → tên trường/URL dự kiến**. Thống nhất Controller/DTO/Form trước, không tự đổi tên dữ liệu trong HTML rồi chờ backend đoán.

### Checklist trước khi bàn giao

- [ ] Chạy trong Spring Boot với dữ liệu thật; form tạo/sửa/duyệt và các chức năng mới vẫn hoạt động.
- [ ] Menu/nút đúng vai trò, bản ghi, hạn và trạng thái.
- [ ] Có trạng thái rỗng, thông báo thành công/lỗi, lỗi form và trang 403/404 rõ ràng.
- [ ] Không có dữ liệu demo cố định thay cho dữ liệu backend, không lộ mật khẩu hoặc đường dẫn tệp nội bộ.
- [ ] Không mất CSRF, session, upload/download và điều hướng sau POST.
- [ ] Đã thử responsive và các bước tương ứng trong hướng dẫn test web.
- [ ] Pull Request ghi trang đã sửa, ảnh giao diện, thay đổi dữ liệu nếu có và nhánh/commit dùng bàn giao.

## 7. Tra nhanh tên trường gửi về backend

Bảng dưới lấy từ Form Java hiện có. Không phải tất cả trường đều bắt buộc; đọc annotation validation và template gốc để biết trường bắt buộc và danh sách lựa chọn. Các thao tác dùng `@RequestParam` hoặc nút submit riêng thì giữ `name/value` trong Controller và template.

| Module / Form | Trường dữ liệu | File tham chiếu |
|---|---|---|
| `account/DoiMatKhauForm` | `matKhauCu`, `matKhauMoi`, `xacNhanMatKhau` | [DoiMatKhauForm.java](../src/main/java/com/khoacntt/quanlydetai/account/form/DoiMatKhauForm.java) |
| `account/SuaTaiKhoanForm` | `tenDangNhap`, `trangThai`, `matKhauMoi`, `xacNhanMatKhau`, `hoTen`, `email`, `vaiTros` | [SuaTaiKhoanForm.java](../src/main/java/com/khoacntt/quanlydetai/account/form/SuaTaiKhoanForm.java) |
| `account/TaiKhoanForm` | `tenDangNhap`, `matKhau`, `hoTen`, `email`, `vaiTros` | [TaiKhoanForm.java](../src/main/java/com/khoacntt/quanlydetai/account/form/TaiKhoanForm.java) |
| `baocao/NopBaoCaoForm` | `ghiChu`, `pdf`, `slide`, `source` | [NopBaoCaoForm.java](../src/main/java/com/khoacntt/quanlydetai/baocao/form/NopBaoCaoForm.java) |
| `dangky/DangKyForm` | `deTaiId` | [DangKyForm.java](../src/main/java/com/khoacntt/quanlydetai/dangky/form/DangKyForm.java) |
| `dangky/DuyetDangKyForm` | `deTaiId`, `chapThuan`, `lyDo` | [DuyetDangKyForm.java](../src/main/java/com/khoacntt/quanlydetai/dangky/form/DuyetDangKyForm.java) |
| `danhgia/DuyetSuaDiemForm` | `chapThuan`, `nhanXet` | [DuyetSuaDiemForm.java](../src/main/java/com/khoacntt/quanlydetai/danhgia/form/DuyetSuaDiemForm.java) |
| `danhgia/HoiDongForm` | `dotId`, `maHoiDong`, `tenHoiDong`, `chuTichId`, `thuKyId`, `uyVienIds` | [HoiDongForm.java](../src/main/java/com/khoacntt/quanlydetai/danhgia/form/HoiDongForm.java) |
| `danhgia/KeHoachDanhGiaForm` | `dangKyId`, `phanBienId`, `hoiDongId`, `lichPhanBienLuc`, `diaDiemPhanBien`, `hoiDongBatDau`, `hoiDongKetThuc`, `diaDiemHoiDong` | [KeHoachDanhGiaForm.java](../src/main/java/com/khoacntt/quanlydetai/danhgia/form/KeHoachDanhGiaForm.java) |
| `danhgia/PhieuChamForm` | `diem`, `nhanXet`, `nop` | [PhieuChamForm.java](../src/main/java/com/khoacntt/quanlydetai/danhgia/form/PhieuChamForm.java) |
| `danhgia/TongHopForm` | `nhanXet` | [TongHopForm.java](../src/main/java/com/khoacntt/quanlydetai/danhgia/form/TongHopForm.java) |
| `danhgia/YeuCauSuaDiemForm` | `lyDo` | [YeuCauSuaDiemForm.java](../src/main/java/com/khoacntt/quanlydetai/danhgia/form/YeuCauSuaDiemForm.java) |
| `detai/DeTaiForm` | `dotId`, `boMonId`, `maDeTai`, `tenDeTai`, `noiDung`, `yeuCau`, `sanPhamDuKien` | [DeTaiForm.java](../src/main/java/com/khoacntt/quanlydetai/detai/form/DeTaiForm.java) |
| `detai/DuyetDeTaiForm` | `chapThuan`, `nhanXet` | [DuyetDeTaiForm.java](../src/main/java/com/khoacntt/quanlydetai/detai/form/DuyetDeTaiForm.java) |
| `detai/HuongDanForm` | `giangVienIds` | [HuongDanForm.java](../src/main/java/com/khoacntt/quanlydetai/detai/form/HuongDanForm.java) |
| `dotdangky/DotDangKyForm` | `maDot`, `tenDot`, `loai`, `gvBatDau`, `gvKetThuc`, `svBatDau`, `svKetThuc`, `hanNopBaoCao`, `hanNopDiemPb`, `ngayBaoCaoHd` | [DotDangKyForm.java](../src/main/java/com/khoacntt/quanlydetai/dotdangky/form/DotDangKyForm.java) |
| `hoso/BoMonForm` | `maBoMon`, `tenBoMon`, `dangHoatDong` | [BoMonForm.java](../src/main/java/com/khoacntt/quanlydetai/hoso/form/BoMonForm.java) |
| `hoso/GiangVienForm` | `taiKhoanId`, `maGv`, `boMonId`, `hocVi` | [GiangVienForm.java](../src/main/java/com/khoacntt/quanlydetai/hoso/form/GiangVienForm.java) |
| `hoso/SinhVienForm` | `taiKhoanId`, `maSv`, `lop`, `khoaHoc` | [SinhVienForm.java](../src/main/java/com/khoacntt/quanlydetai/hoso/form/SinhVienForm.java) |
| `nhom/DoiTenNhomForm` | `tenNhom` | [DoiTenNhomForm.java](../src/main/java/com/khoacntt/quanlydetai/nhom/form/DoiTenNhomForm.java) |
| `nhom/MoiThanhVienForm` | `maSv`, `loiNhan` | [MoiThanhVienForm.java](../src/main/java/com/khoacntt/quanlydetai/nhom/form/MoiThanhVienForm.java) |
| `nhom/NhomForm` | `dotId`, `maNhom`, `tenNhom` | [NhomForm.java](../src/main/java/com/khoacntt/quanlydetai/nhom/form/NhomForm.java) |
| `thongbao/ThongBaoForm` | `tieuDe`, `noiDung`, `donViBanHanh` | [ThongBaoForm.java](../src/main/java/com/khoacntt/quanlydetai/thongbao/form/ThongBaoForm.java) |
