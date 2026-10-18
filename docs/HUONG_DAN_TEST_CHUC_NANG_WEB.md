# Hướng dẫn test chức năng web

Cập nhật: 18/10/2026. Dùng cho nhóm chạy thử trên trình duyệt trước khi demo và deploy.
Đây là danh sách việc cần kiểm tra, không phải xác nhận các ca đã chạy đạt.

## 1. Chuẩn bị

### Chạy ứng dụng

Mở **PowerShell** tại thư mục project có `pom.xml` và `mvnw.cmd`:

```powershell
.\mvnw.cmd -v
$env:DB_USERNAME = "doan_dev"
$env:DB_PASSWORD = "<mat_khau_database_tren_may_ban>"
.\mvnw.cmd --settings .mvn\settings.xml spring-boot:run
```

- Maven phải dùng Java 21. Thay mật khẩu mẫu bằng thông tin máy mình; không gửi mật khẩu lên Git.
- Chờ khởi động thành công, mở http://localhost:8080/login. Nếu đang chạy bản cũ, Ctrl+C rồi chạy lại.
- Tài khoản web khác tài khoản MySQL. Nếu chưa có admin, xem phần tạo quản trị viên đầu tiên trong [README](../README.md).
- Dùng dữ liệu thử có tiền tố `THU_`. Thao tác sẽ lưu vào database đang kết nối; không xóa bảng hoặc import lại SQL để test.
- Dùng profile trình duyệt khác nhau khi thử nhiều người cùng lúc. Hai tab cùng profile dùng chung đăng nhập.

### Tài khoản và tệp thử

Các tên dưới đây là gợi ý **cần tạo**, không phải tài khoản có sẵn. Admin tạo tài khoản trước, hồ sơ SV/GV sau; tất cả đang hoạt động.

| Tài khoản | Vai trò / nhiệm vụ |
|---|---|
| Admin hiện có | Quản lý tài khoản, bộ môn, hồ sơ |
| `thu_truongkhoa` | TRUONG_KHOA: tạo và triển khai đợt |
| `thu_canbokhoa` | CAN_BO_KHOA: công bố đề tài, duyệt đăng ký, tổ chức đánh giá |
| `thu_truongbomon` | GIANG_VIEN + TRUONG_BO_MON; hồ sơ GV thuộc bộ môn thử: duyệt đề tài và phân công GVHD |
| `thu_gv1` | GIANG_VIEN: đề xuất, làm GVHD |
| `thu_gv2` | GIANG_VIEN: GVPB, có thể kiêm ủy viên |
| `thu_gv3`, `thu_gv4` | GIANG_VIEN: chủ tịch và thư ký hội đồng |
| `thu_sv1`–`thu_sv6` | SINH_VIEN: nhóm A gồm SV1–3, nhóm B gồm SV4–6; SV1 và SV4 làm trưởng |

Tạo bộ môn `THU_CNTT`, gán hồ sơ GV vào đó. Khi kiểm tra quyền khác bộ môn, tạo thêm một bộ môn và trưởng bộ môn tương ứng. Mỗi tài khoản dùng email riêng.

Chuẩn bị **PDF báo cáo V1/V2, slide PDF hoặc PPTX, mã nguồn ZIP**; mỗi tệp tối đa 50 MiB. Dùng tệp thực, không đổi đuôi TXT thành PDF để làm tệp hợp lệ. Có thể dùng một bản đổi đuôi riêng để kiểm tra bị từ chối.

### Lịch chạy thử

Chuẩn bị tài khoản/tệp trước khi tạo đợt. Ví dụ bắt đầu thử lúc 14:00:

| Mốc | Giờ ví dụ |
|---|---|
| GV đăng ký | 14:00–14:20 |
| SV đăng ký | 14:20–14:50 |
| Hạn nộp báo cáo | 15:10 |
| Phản biện | 15:15 |
| Hạn điểm PB (TLCN/KLTN) | 15:35 |
| Hội đồng (NCKH/KLTN) | 15:40–16:00 |

Điều chỉnh theo giờ thực tế và dành đủ thời gian thao tác. **GV kết thúc không muộn hơn SV bắt đầu.** Phải chờ đúng giai đoạn để thao tác; đợt đã triển khai không sửa lịch trên web. Không đổi đồng hồ máy hoặc sửa database để vượt hạn.

| Loại | Hình thức đánh giá | Trường bổ sung khi tạo đợt |
|---|---|---|
| MON_HOC | GVPB riêng | Không có hạn điểm PB/ngày hội đồng |
| TLCN | GVPB riêng | Hạn điểm PB |
| NCKH | GVPB rồi hội đồng | Không có hạn điểm PB/ngày hội đồng cấp đợt; vẫn xếp lịch hội đồng cụ thể |
| KLTN | GVPB rồi hội đồng | Hạn điểm PB và ngày hội đồng khớp lịch đánh giá |

## 2. Chạy luồng chính trước

Bắt đầu bằng đợt **MON_HOC**. Làm lần lượt, chỉ sang bước tiếp theo khi bước trước thành công.

| Bước | Người thực hiện | Việc làm → kết quả cần thấy |
|---|---|---|
| 1 | Trưởng khoa | Tạo đợt, kiểm tra lịch rồi triển khai |
| 2 | GV1, trong hạn GV | Tạo đề tài thuộc THU_CNTT, gửi duyệt → trạng thái chờ duyệt |
| 3 | Trưởng bộ môn | Duyệt đề tài và phân công GV1 hướng dẫn → có 1 GVHD |
| 4 | Cán bộ khoa | Công bố đề tài → SV xem được trong danh sách |
| 5 | SV1, trong hạn SV | Tạo nhóm A, mời SV2 và SV3; hai bạn nhận lời → nhóm đủ 3 người, SV1 là trưởng |
| 6 | SV1 | Đăng ký đề tài đã công bố → chờ duyệt và được giữ chỗ |
| 7 | Cán bộ khoa | Chấp thuận đăng ký → nhóm đang thực hiện, khóa thay đổi thành viên và đề tài |
| 8 | SV1, trước hạn báo cáo | Nộp đủ PDF + slide + ZIP; nộp lại bộ V2 → có 2 phiên bản, tải đúng nội dung từng bản |
| 9 | Cán bộ khoa | Lập kế hoạch với GVPB là GV2; tới giờ PB và qua hạn báo cáo thì chốt bản cuối, mở phản biện → bản V2 dùng chấm |
| 10 | GV2 | Nhập 8.0 và nhận xét, lưu nháp rồi nộp chính thức → PB hoàn tất |
| 11 | Cán bộ khoa | Tổng hợp, xác nhận, công bố → điểm 8.0, đạt |
| 12 | SV1–3 | Xem Kết quả, lọc đúng đợt → thấy điểm/nhận xét; SV ngoài nhóm không xem được |

Trước bước 11 công bố, kiểm tra SV **chưa thấy điểm**. Nếu thử email, bật Mailpit trước khi chạy ứng dụng theo mục 4.

### Thử thêm hội đồng

Tạo đợt **KLTN** và lặp lại bước 1–8. Khoa lập hội đồng gồm **GV3 chủ tịch, GV4 thư ký, GV2 ủy viên**, chốt rồi gán vào kế hoạch; GVPB vẫn là GV2.

1. GV2 nộp điểm PB **8.0**.
2. Sau khi PB xong và tới lịch, khoa mở chấm hội đồng.
3. GV3 nộp **7.0**, GV4 nộp **9.0**. GV2 kiêm nhiệm chỉ tính một điểm, không nhập phiếu thứ hai.
4. **GV3/chủ tịch tổng hợp**, khoa xác nhận và công bố.
5. Kết quả phải là **(8 + 7 + 9) / 3 = 8.0**, đạt; không tính điểm GVHD.

Kiểm tra thêm TLCN và NCKH theo bảng khác biệt ở mục 1; không cần học lại luồng mới.

### Thử sửa điểm

1. GV2 xin sửa, ghi lý do → khoa duyệt → GV2 nộp điểm mới **9.0**.
2. Trong lúc điều chỉnh, SV vẫn thấy kết quả đã công bố cũ kèm nhãn **Đang điều chỉnh**.
3. Tổng hợp lại (chủ tịch nếu có hội đồng), khoa xác nhận và công bố lại.
4. SV thấy kết quả mới, hết nhãn điều chỉnh; lịch sử điểm/kết quả cũ còn nguyên. Với MON_HOC chỉ GV2 chấm, điểm mới là **9.0**.

## 3. Kiểm tra các trường hợp dễ lỗi

Dùng dữ liệu phụ cho thao tác khóa, xóa, từ chối, rời nhóm; tránh làm hỏng luồng demo. Mỗi dòng có nhiều tình huống thì kiểm tra từng tình huống. **Bị từ chối đúng quy tắc là Đạt.**

| Mã | Thử thao tác | Kết quả mong đợi |
|---|---|---|
| TK01 | Đăng nhập sai; đăng xuất rồi mở lại trang riêng | Không truy cập được |
| TK02 | Tạo trùng username/email/mã SV/GV; thiếu trường; hồ sơ sai vai trò | Báo lỗi, không tạo dữ liệu không hợp lệ |
| TK03 | Đổi mật khẩu; khóa/mở khóa hoặc đổi vai trò tài khoản phụ | Mật khẩu/quyền mới có hiệu lực; phiên cũ kết thúc khi bị thay đổi; tài khoản khóa không đăng nhập được |
| TK04 | Admin tự khóa/tự bỏ ADMIN; gán TRUONG_BO_MON thiếu GIANG_VIEN | Bị chặn |
| DT01 | Lịch bắt đầu >= kết thúc; GV/SV chồng nhau; thiếu hạn/ngày bắt buộc | Không lưu được đợt |
| DT02 | Cán bộ khoa tạo đợt; trưởng bộ môn duyệt/phân công đề tài khác bộ môn | Bị chặn; chỉ trưởng khoa tạo đợt, trưởng bộ môn xử lý đúng bộ môn mình |
| DT03 | GV đề xuất ngoài hạn; SV xem đề tài chưa công bố | Bị chặn |
| DT04 | Phân công 0 hoặc 3 GVHD; công bố chưa có GVHD | Bị chặn; 1–2 GVHD hợp lệ được chấp nhận |
| NH01 | Nhận lời mời; từ chối/thu hồi; tham gia hai nhóm cùng đợt | Chỉ nhận lời mới thành thành viên; không thuộc hai nhóm cùng đợt |
| NH02 | Nhóm 1–2 người đăng ký; nhóm 3 hoặc 5 người đăng ký; thêm người thứ 6 | Thiếu 3 bị chặn; 3–5 hợp lệ; không vượt 5 |
| NH03 | Đổi trưởng/rời nhóm trước duyệt; sửa thành viên sau duyệt | Trước duyệt và còn hạn được xử lý đúng; sau duyệt bị khóa |
| DK01 | Thành viên thường đăng ký; nhóm khác chọn đề tài đang giữ chỗ | Bị chặn |
| DK02 | Đổi sang đề tài đã bị giữ; đăng ký lại sau bị từ chối khi còn hạn | Đổi thất bại vẫn giữ đăng ký cũ; bị từ chối được chọn lại |
| BC01 | Thiếu một tệp, tệp rỗng/sai định dạng hoặc >50 MiB | Bị chặn, không tạo bộ báo cáo thiếu |
| BC02 | Thành viên thường nộp; trưởng nộp sau hạn | Bị chặn; chỉ trưởng được nộp trước hạn |
| BC03 | Thành viên/GVHD/người chấm được giao tải; người ngoài dùng cùng link | Đúng quyền tải được, người ngoài bị chặn |
| HD01 | Hội đồng 2/6 GV, trùng GV, thiếu chủ tịch/thư ký; thử 3/5 GV hợp lệ | Cấu hình sai bị chặn; cấu hình hợp lệ được chấp nhận |
| HD02 | Gán GVHD làm GVPB/thành viên hội đồng; đổi GVHD thành người đang chấm | Cả hai chiều đều bị chặn |
| HD03 | Dùng hội đồng nháp/khác đợt; mở PB hoặc hội đồng trước điều kiện cho phép | Bị chặn; hội đồng cần PB hoàn tất và tới giờ |
| HD04 | Phân công cùng GV trùng lịch ở hai đề tài; trùng địa điểm hội đồng cùng đợt | Bị chặn |
| CD01 | Điểm -1 hoặc >10, thiếu nhận xét; thử điểm 0 và 10 | Dữ liệu sai bị chặn; hai biên hợp lệ được chấp nhận |
| CD02 | Thiếu phiếu/phiếu còn nháp mà tổng hợp; GV không được giao chấm | Bị chặn |
| KQ01 | GVPB riêng ngoài hội đồng 3 người | Cần đủ 4 người chấm; trung bình tính mỗi GV một lần |
| KQ02 | Tất cả điểm 3.95; bộ khác tất cả điểm 3.94 | Lần lượt 4.0 đạt và 3.9 không đạt sau làm tròn |
| KQ03 | SV lọc đợt mình tham gia; đổi sang ID đợt/nhóm người khác | Chỉ thấy kết quả của mình; không lộ dữ liệu khác |
| SD01 | Sửa điểm chưa được duyệt; tự duyệt yêu cầu của mình; công bố khi còn điều chỉnh dang dở | Bị chặn; kết quả đã công bố cũ giữ nguyên |
| TB01 | Tạo thông báo nháp + tệp → công bố → thu hồi | SV chỉ xem/tải lúc công bố; chưa đăng nhập không xem được |
| XOA01 | Xóa đợt/đề tài nháp chưa dùng và đúng quyền; xóa hồ sơ/bộ môn có liên kết | Bản nháp đủ điều kiện xóa được; dữ liệu có liên kết bị chặn |
| XOA02 | Xóa thông báo nháp không tệp; thử bài có tệp hoặc đã công bố | Chỉ nháp không tệp xóa được; bài công bố dùng thu hồi |
| Q01 | SV gõ URL quản trị; đổi ID nhóm/báo cáo/phiếu sang bản ghi có thật của người khác | Backend chặn, không chỉ ẩn menu |
| Q02 | Xóa input `_csrf` bằng DevTools trên form thử rồi gửi | Bị chặn, dữ liệu không thay đổi; tải lại form sau khi thử |

**Thử đồng thời bằng hai profile độc lập:**

- Hai nhóm đủ 3–5 người cùng chọn một đề tài → chỉ một nhóm giữ được.
- Một SV đồng ý lời mời hai nhóm cùng đợt → chỉ vào một nhóm.
- Nhóm đang 4 người, hai SV cùng nhận lời → chỉ thêm một người, tổng tối đa 5.

Bấm tay gần nhau chỉ kiểm tra sơ bộ; kiểm thử transaction đồng thời còn được thực hiện bằng test tự động.

## 4. Kiểm tra email theo Lab 6

Làm theo [Hướng dẫn email](HUONG_DAN_EMAIL.md) để chạy Mailpit và bật `MAIL_ENABLED=true` trước khi khởi động ứng dụng.

- [ ] Công bố kết quả → http://localhost:8025 nhận một thư riêng cho mỗi SV trong nhóm.
- [ ] Thư đúng tên đề tài, đợt, phiên bản; tiếng Việt hiển thị đúng.
- [ ] Link mở trang kết quả, yêu cầu đăng nhập và chỉ xem dữ liệu đúng quyền.
- [ ] Công bố lại sau sửa điểm → nhận thư cho phiên bản mới.
- [ ] Dừng Mailpit, công bố kết quả thử khác → kết quả vẫn lưu; log báo lỗi gửi mail.

Mailpit là hộp thư giả lập, **không gửi đến Gmail**. Ứng dụng chưa có chức năng tự gửi lại thư thất bại.

## 5. Ghi kết quả và kết thúc kiểm thử

Mỗi bước/ca ghi **Đạt / Lỗi / Chưa chạy / Bị chặn do thiếu điều kiện**. Với dòng nhiều tình huống, thêm hậu tố a, b… để ghi riêng.

| Bước/mã ca | Người test | Mã đợt/nhóm | Kết quả thực tế | Trạng thái |
|---|---|---|---|---|
| Ví dụ: NH02a | … | … | … | … |

Khi báo lỗi, gửi: **vai trò → các bước thao tác → mong đợi → thực tế → ảnh/log**, kèm mã dữ liệu và thời gian nếu lỗi liên quan hạn. Không gửi mật khẩu.

Trước khi kết luận lỗi, kiểm tra: đúng tài khoản/hồ sơ chưa, đúng hạn chưa, bản ghi đã được duyệt/công bố chưa, ứng dụng có đang chạy bản code mới không.

### Test tự động bổ trợ

Tại PowerShell trong project, chạy bộ không kết nối MySQL/SMTP thật:

```powershell
Remove-Item Env:TEST_DB_URL, Env:TEST_TV4_DB_URL, Env:TEST_SMTP_PORT -ErrorAction SilentlyContinue
.\mvnw.cmd --settings .mvn\settings.xml clean test
```

Mong đợi `BUILD SUCCESS`; các test cần MySQL/Mailpit có thể `Skipped` do chưa bật môi trường. Xem `target/surefire-reports`. Muốn chạy tích hợp dùng **schema thử riêng**, theo [Hướng dẫn backend](HUONG_DAN_LAM_BACKEND_NHOM.md); không dùng database chính.

Kết quả tự động đã ghi nhận nằm trong [Biên bản kiểm thử](KIEM_THU_BAN_RUBRIC.md), không thay thế việc test giao diện bản đang chạy.

### Đủ điều kiện chuyển sang deploy khi

- [ ] Luồng PB riêng và luồng có hội đồng chạy đến công bố; kiểm tra đủ khác biệt của bốn loại.
- [ ] Sửa điểm, giới hạn nhóm, giữ chỗ, tệp, phân quyền và email hoạt động đúng.
- [ ] Lỗi đã phát hiện được sửa và kiểm tra lại; ca chưa chạy được ghi rõ.
- [ ] Nhóm lưu kết quả/ảnh demo và chạy test tự động phù hợp.

Phân công gợi ý: **TV1** tài khoản/quyền; **TV2** đợt/đề tài; **TV3** nhóm/đăng ký; **TV4** báo cáo/chấm điểm/thông báo/email. Đổi người kiểm tra chéo một luồng. Khi deploy xong, chạy lại luồng chính trên URL online theo [Hướng dẫn triển khai](TRIEN_KHAI.md).
