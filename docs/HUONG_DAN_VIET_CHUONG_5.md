# Hướng dẫn thực hiện Chương 5 — Giao diện người dùng

Cập nhật: 20/10/2026. Áp dụng cho Hệ thống Quản lý Đề tài Sinh viên hiện tại.

Đây là kế hoạch thao tác, chụp minh chứng và viết Word. Những ảnh/tình huống được đề xuất bên dưới **chưa được tạo chỉ bằng việc viết tài liệu này**. Chỉ ghi “đã thực hiện/đạt” sau khi tự thao tác và có kết quả thật.

## 1. Yêu cầu của chương và sản phẩm cần hoàn thành

Theo đề cương giảng viên cung cấp: Chương 5 trình bày giao diện người dùng, chụp màn hình sản phẩm ở các giao diện/flow quan trọng và có mô tả ở mỗi màn hình.

Mục tiêu của chương là giúp người đọc thấy ai dùng hệ thống, thao tác gì, dữ liệu đi qua các bước nào và kết quả hiển thị ra sao. Không cần chép code, giải thích toàn bộ annotation hoặc đưa mọi ca kiểm thử vào chương này.

Sản phẩm cần bàn giao:

- Nội dung Chương 5 trong Word, có tiêu đề mục, ảnh, chú thích và mô tả từng ảnh.
- Thư mục ảnh gốc theo đúng thứ tự; kế hoạch gợi ý 25 ảnh chính, có thể gộp/tách theo khả năng đọc. **Số lượng này là đề xuất, không phải định mức của thầy.**
- Danh sách ghi nhận ảnh: số hình, tên file, môi trường, tài khoản/vai trò, trạng thái dữ liệu và nhận xét.
- Minh chứng triển khai online/HTTPS để đưa phụ lục nếu cần; phân biệt với các ảnh chức năng local.

Tài liệu liên quan:

- [README — cách chạy và mục lục](../README.md).
- [Hướng dẫn test web — thao tác nghiệp vụ chi tiết](HUONG_DAN_TEST_CHUC_NANG_WEB.md).
- [Phạm vi chức năng và FR/NFR](PHAM_VI_CHUC_NANG.md).
- [HTTPS local](HTTPS_LOCAL.md), [email/Mailpit](HUONG_DAN_EMAIL.md), [triển khai online](TRIEN_KHAI.md).

## 2. Chốt môi trường và giao diện trước khi chụp

### 2.1. Chọn môi trường

| Môi trường | Cách sử dụng trong Chương 5 |
|---|---|
| Local HTTPS: `https://qldt.nhom4qldt.id.vn:8443` | Ưu tiên chụp toàn bộ luồng nếu máy đã có hosts/PFX và chạy được |
| Local HTTP: `http://localhost:8080` | Có thể dùng cho luồng chức năng nếu chưa cấu hình HTTPS; mô tả đúng môi trường |
| Online: `https://nhom4qldt.id.vn` | Chụp minh chứng truy cập từ Internet, tên miền và giao diện đã deploy |
| Mailpit: `http://localhost:8025` | Chụp thư thử của kết quả vừa công bố ở local |

Local và online dùng database riêng. Không đổi môi trường giữa một luồng rồi coi các ảnh thuộc cùng một bộ dữ liệu. Bản online đang tắt mail, tệp upload không được lưu bền vững; không dùng ảnh local để khẳng định online đã gửi email/lưu file lâu dài.

Tên miền `qldt.nhom4qldt.id.vn:8443` hiện trỏ loopback trên máy phát triển; không mô tả link này là địa chỉ Internet cho mọi người.

### 2.2. Chốt phiên bản

1. Chạy giao diện cuối và kiểm tra logo, font, nút, bảng, thông báo lỗi, menu theo vai trò.
2. Nếu sửa code, chạy lại ứng dụng và tải lại trang trước khi chụp.
3. Ghi commit hoặc trạng thái source đang dùng; ghi riêng nếu còn thay đổi chưa commit.
4. Nếu cần ảnh online cùng phiên bản, hoàn tất tích hợp/push/deploy theo hướng dẫn trước, rồi kiểm tra commit Render. Tài liệu này không tự push/deploy.
5. Trong một buổi chụp, giữ cùng phiên bản, cùng bộ dữ liệu và cách trình bày.

## 3. Dữ liệu và công cụ cần chuẩn bị

### 3.1. Tài khoản và hồ sơ

Các tên dưới đây chỉ là gợi ý tự tạo, không phải tài khoản/mật khẩu mặc định. Có thể dùng bộ dữ liệu demo đang có nếu đáp ứng điều kiện.

| Tài khoản gợi ý | Vai trò/hồ sơ | Nhiệm vụ |
|---|---|---|
| Admin hiện có | ADMIN | Tạo tài khoản, bộ môn, hồ sơ |
| `demo_truongkhoa` | TRUONG_KHOA | Tạo và triển khai đợt |
| `demo_canbokhoa` | CAN_BO_KHOA | Công bố đề tài, duyệt đăng ký, kế hoạch, xác nhận/công bố điểm |
| `demo_truongbomon` | GIANG_VIEN + TRUONG_BO_MON; hồ sơ GV thuộc bộ môn demo | Duyệt đề tài, phân công hướng dẫn |
| `demo_gv1` | GIANG_VIEN; hồ sơ cùng bộ môn | Đề xuất và hướng dẫn đề tài |
| `demo_gv2` | GIANG_VIEN | Phản biện, kiêm ủy viên hội đồng |
| `demo_gv3` | GIANG_VIEN | Chủ tịch hội đồng |
| `demo_gv4` | GIANG_VIEN | Thư ký hội đồng |
| `demo_sv1`–`demo_sv3` | SINH_VIEN; đủ hồ sơ | Một nhóm có ba thành viên, SV1 làm trưởng |
| Tài khoản phụ | Tài khoản riêng chưa dùng cho luồng chính | Thử sửa username/mật khẩu, tránh làm mất phiên đang chụp |

Trưởng khoa có thể thực hiện các thao tác thuộc quyền khoa. Không suy ra ADMIN có tất cả quyền học thuật. GVHD không được chấm chính đề tài mình hướng dẫn.

Mỗi sinh viên có mã SV, lớp hành chính, khóa học và email thử riêng, ví dụ `sv01@example.test`. Mỗi giảng viên có hồ sơ và bộ môn phù hợp. Không để lộ mật khẩu khi chụp nút hiện/ẩn; ảnh có thể để ô rỗng hoặc dùng dữ liệu thử không sử dụng lại.

### 3.2. Bộ dữ liệu minh họa

- Bộ môn: Công nghệ phần mềm, mã demo riêng không trùng dữ liệu hiện có.
- Đề tài: “Xây dựng hệ thống quản lý thư viện”; điền đủ nội dung, yêu cầu, sản phẩm dự kiến.
- Nhóm: “Nhóm Thư viện 01”, ba sinh viên đã đồng ý tham gia, đúng một trưởng.
- Luồng phụ MON_HOC: dùng để hiểu nhanh chu trình phản biện, công bố và sửa điểm nếu cần.
- Luồng chính KLTN: dùng cho bộ ảnh có hội đồng. Nếu dùng hai đợt, ghi rõ loại và tên đợt ở ảnh/mô tả; không trộn điểm của hai đề tài.
- Điểm KLTN gợi ý: GV2 phản biện 8.0, GV3 chủ tịch 7.0, GV4 thư ký 9.0 → kết quả 8.0. GV2 kiêm ủy viên chỉ có một phiếu, không cộng hai lần.
- Khi sửa điểm GV4 từ 9.0 thành 9.5 theo quy trình hợp lệ: kết quả mới là 8.2 sau làm tròn một chữ số. Dùng chính giá trị hệ thống hiển thị để viết báo cáo.

Tệp chuẩn bị: một PDF báo cáo có nội dung thật, một PDF/PPTX slide và một ZIP source hợp lệ. Nếu minh họa phiên bản, chuẩn bị hai bộ có nội dung khác nhau. Mỗi lần nộp đủ ba tệp; tối đa 50 MiB/tệp. Không tạo file rỗng rồi đổi đuôi.

Thông báo mẫu: “Lịch bảo vệ đề tài tốt nghiệp”, có nội dung và tệp đính kèm hợp lệ nếu muốn minh họa tải xuống.

### 3.3. Phiên đăng nhập và chụp ảnh

Hai tab trong cùng profile trình duyệt dùng chung phiên đăng nhập. Chuyển vai trò bằng đăng xuất/đăng nhập lần lượt, hoặc dùng profile trình duyệt khác nhau. Không giả định mở thêm tab là có tài khoản độc lập.

Trên Windows dùng `Win + Shift + S` hoặc công cụ chụp tương đương. Giữ kích thước cửa sổ và zoom thống nhất; ưu tiên 100% nếu nội dung vừa khung. Thu gọn phần trống nhưng giữ tiêu đề, dữ liệu và thao tác liên quan. Không chụp cả màn hình rất dài rồi thu nhỏ đến mức không đọc được.

## 4. Lịch thực hiện để chụp đúng trạng thái

### 4.1. Trình tự tổng quát

1. Chụp đăng nhập, trang chính, quản trị và dữ liệu hồ sơ trước.
2. Tạo đợt, kiểm tra lịch rồi triển khai; chụp phần tạo đợt trước khi lưu.
3. Trong hạn GV: đề xuất → gửi duyệt → trưởng bộ môn duyệt/phân công → khoa công bố.
4. Trong hạn SV: tạo nhóm → mời/đồng ý → đủ thành viên → đăng ký → khoa chấp thuận.
5. Trước hạn báo cáo: nộp V1, có thể nộp V2; chụp danh sách phiên bản và tải kiểm tra.
6. Khoa chốt hội đồng/lưu kế hoạch; đến hạn và lịch thì mở phản biện, sau đó mở hội đồng đúng điều kiện.
7. GV nộp chính thức → chủ tịch tổng hợp → khoa xác nhận/công bố → SV xem → Mailpit nhận thư.
8. Xin sửa → khoa duyệt → nộp phiếu mới → tổng hợp/xác nhận/công bố lại; chụp trước và sau.
9. Chụp thông báo, responsive và online; ghép Word, soát chú thích.

### 4.2. Lưu ý thời gian

Nên chia thành hai buổi: buổi đầu chuẩn bị tài khoản/tệp và chụp giao diện chung; buổi sau chạy luồng có lịch. Chừa thời gian chuyển tài khoản và chụp, không ép mọi giai đoạn vào vài phút.

Ví dụ cho buổi nghiệp vụ bắt đầu lúc 08:00; **đổi toàn bộ theo ngày/giờ thực tế**, không nhập nguyên lịch đã qua:

| Mốc | Ví dụ | Việc cần hoàn thành |
|---|---|---|
| GV đăng ký | 08:00–08:40 | Đề xuất, duyệt, phân công, công bố |
| SV đăng ký | 08:40–09:20 | Nhóm đủ thành viên, đăng ký và chấp thuận |
| Hạn báo cáo | 09:40 | Nộp đủ bộ tài liệu trước hạn |
| Lịch phản biện | 09:45 | Khoa mở phản biện sau hạn, GV2 nộp điểm |
| Hạn điểm PB của KLTN | 10:05 | Hoàn tất phản biện trước hạn |
| Hội đồng | 10:10–10:40 | Mở hội đồng khi PB hoàn tất, các thành viên nộp điểm |

Ngày hội đồng KLTN cấp đợt phải khớp ngày kế hoạch. Giai đoạn GV/SV không chồng nhau. Đợt đã triển khai không sửa lịch trên web; kiểm tra kỹ trước khi triển khai. Không đổi đồng hồ máy hoặc sửa SQL để vượt trạng thái/hạn.

Code hiện cho phép hoàn tất/tổng hợp/công bố trước giờ kết thúc hội đồng nếu đã đủ phiếu và đúng trạng thái/quyền. Không viết “bắt buộc đợi hết giờ hội đồng” hoặc “chỉ cần ba giảng viên là luôn công bố được”; hội đồng có thể có 3–5 người và phải đủ phiếu của phân công thực tế, còn bước tổng hợp/xác nhận/công bố.

## 5. Bố cục Word và danh mục 25 ảnh chính

Tiêu đề chương: **CHƯƠNG 5. GIAO DIỆN NGƯỜI DÙNG**.

Đoạn mở đầu gợi ý, chỉnh lại sau khi có ảnh:

> Chương này trình bày các giao diện và luồng sử dụng chính của hệ thống Quản lý Đề tài Sinh viên. Các màn hình được sắp xếp theo vai trò và tiến trình nghiệp vụ, từ quản trị dữ liệu, đăng ký đề tài, nộp báo cáo đến đánh giá và công bố kết quả. Mỗi hình minh họa được kèm mô tả về mục đích, thao tác và kết quả hiển thị.

Các bảng dưới là kế hoạch chụp và mô tả gợi ý. Sau khi chụp phải sửa đúng tên nút, tên đợt, trạng thái và dữ liệu thực tế; không giữ nguyên câu mô tả nếu màn hình không thể hiện nội dung đó.

### 5.1. Giao diện đăng nhập và điều hướng

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.1 | Giao diện đăng nhập | Chụp logo, tên hệ thống, hai ô đăng nhập, biểu tượng hiện/ẩn mật khẩu và nút đăng nhập | Người dùng đăng nhập bằng tài khoản do quản trị viên cấp. Biểu tượng hình con mắt hỗ trợ hiện hoặc ẩn mật khẩu đang nhập; hệ thống không cung cấp chức năng xem mật khẩu đã lưu. |
| 5.2 | Trang chính và menu theo vai trò | Đăng nhập một vai trò rõ ràng, ví dụ sinh viên; chụp menu và nội dung chính | Sau khi đăng nhập, người dùng được điều hướng tới giao diện của hệ thống. Các mục chức năng hiển thị phù hợp với vai trò của tài khoản; quyền thao tác còn được kiểm tra tại backend. |

Nếu cần chứng minh menu giữa hai vai trò, có thể thêm hình phụ hoặc đặt hai ảnh cạnh nhau. Không ghi menu “đúng mọi vai trò” chỉ từ một ảnh SV.

### 5.2. Quản trị tài khoản và hồ sơ

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.3 | Danh sách tài khoản | Admin → Quản lý tài khoản; chụp hàng dữ liệu có vai trò, trạng thái và nút sửa/khóa/xóa | Quản trị viên theo dõi tên đăng nhập, thông tin liên hệ, vai trò và trạng thái tài khoản. Màn hình cung cấp thao tác tạo, sửa, khóa/mở khóa và xóa trong phạm vi điều kiện cho phép. |
| 5.4 | Chỉnh sửa tài khoản tích hợp đặt lại mật khẩu | Mở sửa tài khoản phụ; chụp username, thông tin, vai trò/trạng thái và cặp mật khẩu mới | Quản trị viên cập nhật thông tin tài khoản trên cùng biểu mẫu. Hai ô mật khẩu để trống sẽ giữ mật khẩu hiện tại; khi đặt mật khẩu mới, người dùng phải nhập xác nhận phù hợp. |
| 5.5 | Biểu mẫu hồ sơ sinh viên và kiểm tra bắt buộc | Thử bỏ trống lớp/khóa học rồi gửi; chụp lỗi validation cùng các trường liên quan | Hồ sơ sinh viên được liên kết với tài khoản và lưu mã sinh viên, lớp hành chính, khóa học. Khi thiếu thông tin bắt buộc, hệ thống hiển thị lỗi và yêu cầu bổ sung trước khi lưu. |

Sau hình lỗi phải nhập đủ và tạo thành công để dùng ở các bước sau. Có thể thêm ảnh danh sách hồ sơ đã lưu nếu cần, không coi ảnh validation là minh chứng đã tạo thành công.

### 5.3. Quản lý đợt và đề tài

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.6 | Thiết lập đợt đăng ký | Trưởng khoa → tạo đợt; chụp loại đợt và các mốc thời gian đã nhập đúng | Trưởng khoa thiết lập đợt đăng ký theo loại đề tài và các mốc thời gian. Giai đoạn giảng viên và sinh viên được tổ chức liên tiếp, cùng các hạn báo cáo/đánh giá áp dụng cho loại đợt. |
| 5.7 | Duyệt đề tài và phân công hướng dẫn | Sau GV gửi duyệt, trưởng bộ môn mở đề tài; chụp trạng thái và GVHD đã phân công. Nếu hai màn hình riêng, dùng hình 5.7a/5.7b | Trưởng bộ môn xét duyệt đề tài thuộc bộ môn và phân công giảng viên hướng dẫn. Người đề xuất không tự động trở thành người hướng dẫn; đề tài được khoa công bố trước khi sinh viên lựa chọn. |
| 5.8 | Danh sách đề tài đã công bố | Khoa công bố xong → SV mở danh sách; chọn đúng đợt | Sinh viên tra cứu các đề tài đã công bố và thông tin liên quan để lựa chọn. Danh sách thể hiện trạng thái khả dụng theo dữ liệu thực tế của từng đợt. |

Đề xuất đề tài là thao tác cần làm để có dữ liệu hình 5.7. Nếu muốn thể hiện rõ phần GV, bổ sung ảnh form đề xuất với đủ nội dung/yêu cầu/sản phẩm dự kiến và đánh lại số hình trong Word.

### 5.4. Nhóm và đăng ký đề tài

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.9 | Nhóm sinh viên sau khi nhận lời mời | SV1 tạo nhóm, SV2/SV3 đồng ý; chụp danh sách thành viên và trưởng nhóm | Sinh viên tạo nhóm và mời các thành viên tham gia. Sau khi chấp nhận lời mời, nhóm có ba thành viên và đúng một nhóm trưởng; lời mời chưa được đồng ý chưa được tính vào thành viên chính thức. |
| 5.10 | Đăng ký đề tài ở trạng thái chờ duyệt | Trưởng nhóm chọn đề tài; chụp tên nhóm, đề tài và trạng thái chờ duyệt | Nhóm trưởng đăng ký đề tài đã công bố khi nhóm đủ điều kiện. Đăng ký chờ duyệt giữ chỗ đề tài, tránh để nhóm khác đồng thời đăng ký cùng đề tài. |
| 5.11 | Đăng ký được khoa chấp thuận | Khoa duyệt rồi mở lại chi tiết nhóm/đăng ký | Khoa xem thông tin nhóm và đề tài trước khi chấp thuận đăng ký. Sau chấp thuận, nhóm chuyển sang giai đoạn thực hiện; các thay đổi thành viên, trưởng nhóm và đề tài bị khóa theo nghiệp vụ. |

Ảnh 5.10 chỉ chứng minh trạng thái giữ chỗ được hiển thị. Muốn khẳng định đã kiểm thử tranh chấp đồng thời phải có bài test riêng, không suy ra từ một ảnh tĩnh.

### 5.5. Nộp và quản lý báo cáo

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.12 | Biểu mẫu nộp bộ báo cáo | Trưởng nhóm → Báo cáo, chọn đủ PDF/slide/source trước khi nộp | Nhóm trưởng nộp một bộ tài liệu gồm báo cáo PDF, slide PDF/PPTX và mã nguồn ZIP. Hệ thống kiểm tra quyền, thời hạn, loại và dung lượng tệp trước khi tiếp nhận. |
| 5.13 | Các phiên bản báo cáo đã nộp | Nộp V1 và V2 hợp lệ; chụp danh sách có phiên bản, thời gian, tệp tải | Mỗi lần nộp hợp lệ tạo một phiên bản bộ báo cáo và giữ các bản trước. Khi mở phản biện, hệ thống chốt bản cuối hợp lệ để phục vụ đánh giá. |

Tải thử tệp sau khi nộp. Ảnh danh sách metadata chưa chứng minh tệp vật lý luôn tải được, nhất là bản Render dùng ổ tạm.

### 5.6. Đánh giá và công bố kết quả

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.14 | Hội đồng và thành phần chấm | Khoa tạo/chốt hội đồng KLTN: GV3 chủ tịch, GV4 thư ký, GV2 ủy viên; chụp danh sách | Khoa tổ chức hội đồng gồm các giảng viên có chức danh cụ thể. Bộ dữ liệu minh họa có ba người khác nhau, đủ chủ tịch và thư ký, không bao gồm GVHD của đề tài. |
| 5.15 | Kế hoạch đánh giá đề tài | Khoa lưu kế hoạch có GVPB, hội đồng, giờ và địa điểm; chụp chi tiết | Kế hoạch đánh giá gắn nhóm/đề tài với người phản biện, hội đồng và lịch thực hiện. Lưu kế hoạch chưa tự mở chấm; thao tác mở từng giai đoạn chỉ thực hiện khi đủ điều kiện. |
| 5.16 | Phiếu chấm của giảng viên được phân công | Mở phản biện/hội đồng đúng lịch; đăng nhập người chấm, chụp điểm, nhận xét và thao tác nháp/nộp | Giảng viên được phân công nhập điểm và nhận xét cho đề tài. Lưu nháp giữ dữ liệu để hoàn thiện, còn nộp chính thức xác lập phiếu tham gia tính kết quả. |
| 5.17 | Tổng hợp và công bố kết quả | Sau đủ phiếu, chủ tịch tổng hợp; khoa xác nhận/công bố. Chụp điểm nguồn, kết quả và trạng thái; dùng ảnh a/b nếu cần thể hiện hai bước | Kết quả được tổng hợp từ các giảng viên hợp lệ khác nhau và làm tròn một chữ số. Với đề tài có hội đồng, chủ tịch tổng hợp; khoa xác nhận và công bố kết quả cho sinh viên. |
| 5.18 | Kết quả công bố của sinh viên | SV1 → Kết quả, lọc đúng đợt; chụp điểm, kết luận và nhận xét | Sinh viên xem kết quả đã công bố của nhóm trong đợt tham gia. Với dữ liệu minh họa 8.0, 7.0 và 9.0, điểm tổng hợp là 8.0 và được kết luận đạt. |

Cần chụp trước/sau đúng thời điểm nếu muốn chứng minh SV chưa xem được điểm trước công bố. Không chỉ chụp form nhập điểm rồi mô tả đã nộp thành công; kiểm tra trạng thái sau thao tác.

### 5.7. Điều chỉnh điểm

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.19 | Yêu cầu điều chỉnh điểm | GV4 xin sửa với lý do; khoa mở yêu cầu. Có thể ghép thêm trạng thái Đang điều chỉnh của SV | Giảng viên gửi yêu cầu kèm lý do khi cần điều chỉnh điểm chính thức. Yêu cầu được khoa xem xét trước khi cho phép nộp phiếu sửa; kết quả công khai cũ vẫn được giữ trong thời gian xử lý. |
| 5.20 | Kết quả sau công bố phiên bản điều chỉnh | Khoa duyệt → GV4 nộp 9.5 → chủ tịch tổng hợp → khoa xác nhận/công bố → SV xem | Điều chỉnh điểm tạo phiếu và kết quả phiên bản mới thay vì ghi đè lịch sử. Sau khi công bố lại, sinh viên thấy kết quả mới; với bộ điểm 8.0, 7.0 và 9.5, kết quả làm tròn là 8.2. |

Chụp/lưu hình 5.18 trước khi sửa để còn bằng chứng điểm 8.0. Nếu dùng bộ điểm khác, tính lại và đối chiếu hệ thống trước khi viết.

### 5.8. Thông báo và email

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.21 | Chi tiết thông báo trên hệ thống | Tạo và công bố thông báo → đăng nhập người nhận, chụp nội dung/tệp | Người dùng đăng nhập đọc thông báo đã công bố và tải tệp đính kèm theo quyền. Người có thẩm quyền quản lý trạng thái nháp, công bố hoặc thu hồi thông báo. |
| 5.22 | Email thông báo kết quả trong Mailpit | Sau công bố local, mở thư đúng đề tài/phiên bản; chụp người nhận và nội dung HTML | Sau giao dịch công bố thành công, ứng dụng gửi thông báo HTML riêng tới từng sinh viên trong nhóm. Mailpit tiếp nhận thư thử tại local; hình này không chứng minh thư đã được chuyển đến Gmail hoặc hộp thư Internet. |

Nếu hình 5.22 chụp sau sửa điểm thì mô tả rõ phiên bản mới, không ghép điểm 8.0 trong Word với thư chứa 8.2 mà không giải thích.

### 5.9. Khả năng hiển thị và phản hồi nhập liệu

| Hình | Tên ảnh/chú thích | Cách chụp | Mô tả gợi ý dưới ảnh |
|---|---|---|---|
| 5.23 | Menu trên màn hình hẹp | Bật chế độ thiết bị trình duyệt, chọn chiều rộng cố định, mở menu | Giao diện điều chỉnh cách hiển thị điều hướng khi chiều rộng màn hình giảm. Người dùng mở menu để truy cập các chức năng phù hợp với vai trò. |
| 5.24 | Bảng dữ liệu trên màn hình hẹp | Chụp bảng quản trị tại cùng kích thước, thể hiện vùng cuộn nếu có | Bảng dữ liệu giữ bố cục các cột và cung cấp cuộn ngang trong vùng bảng khi cần. Các thao tác được bố trí nhất quán, hạn chế việc tràn ngang toàn trang. |
| 5.25 | Phản hồi khi xác nhận mật khẩu không khớp | Dùng tài khoản phụ, nhập mật khẩu mới/xác nhận khác nhau và gửi; chụp lỗi, giữ nội dung mật khẩu được che | Hệ thống kiểm tra tính nhất quán của mật khẩu mới và giá trị xác nhận. Khi không khớp, biểu mẫu hiển thị lỗi và chưa cập nhật tài khoản theo yêu cầu đó. |

Ghi rõ “mô phỏng kích thước thiết bị bằng trình duyệt” nếu chưa thử trên điện thoại thật. Đừng mô tả đã hỗ trợ mọi thiết bị chỉ từ hai ảnh.

## 6. Minh chứng online và ảnh bổ sung

Nên đưa các ảnh sau vào phụ lục triển khai để Chương 5 tập trung vào giao diện nghiệp vụ. Nếu yêu cầu báo cáo đặt ngay Chương 5, có thể thêm mục 5.10 rồi đánh số tiếp.

| Mã ảnh gốc gợi ý | Nội dung | Điều ảnh có thể chứng minh |
|---|---|---|
| PL_01_Online_Ten_mien.png | Trang login hoặc trang chính, thanh địa chỉ `https://nhom4qldt.id.vn` | Website truy cập qua tên miền online tại thời điểm chụp |
| PL_02_Render_Live.png | Render có service/commit và Status Live | Lượt triển khai được Render báo chạy; không tự chứng minh mọi nghiệp vụ đúng |
| PL_03_HTTPS_Online.png | Thông tin kết nối/chứng chỉ tên miền online hoặc Certificate Issued | HTTPS/chứng chỉ của tên miền online tại thời điểm kiểm tra |
| PL_04_HTTPS_Local.png | URL local-domain và thông tin chứng chỉ tương ứng | HTTPS trên máy local đã cấu hình; không chứng minh truy cập Internet |

Ảnh tùy chọn nếu Word cần thể hiện rõ hơn: biểu mẫu đề xuất đề tài; lời mời nhóm; GVHD bị chặn phân công chấm; nhóm chưa đủ người bị chặn đăng ký; SV chưa thấy điểm trước công bố; trang từ chối truy cập sai quyền. Chỉ bổ sung khi đã tự chạy thành công tình huống tương ứng.

Không cần đưa log Maven dài, toàn bộ bảng database hoặc đoạn code vào Chương 5 để tăng số trang. Kiến trúc ở Chương 2, yêu cầu ở Chương 3, database ở Chương 4; phụ lục chứa cách cài/chạy.

## 7. Quy cách lưu ảnh và ghi nhận minh chứng

Tạo thư mục riêng ở máy làm Word, ví dụ `MinhChung_Chuong5/`. Không lưu vào `src/main/resources/static` hoặc thư mục upload nghiệp vụ chỉ để ghép báo cáo.

```text
MinhChung_Chuong5/
  01_Giao_dien_chung/
    Hinh_5_01_Dang_nhap.png
    Hinh_5_02_Trang_chinh.png
  02_Tai_khoan_ho_so/
  03_Dot_de_tai/
  04_Nhom_dang_ky/
  05_Bao_cao/
  06_Danh_gia_ket_qua/
  07_Sua_diem/
  08_Thong_bao_email/
  09_Responsive_validation/
  Phu_luc_trien_khai/
```

Giữ ảnh gốc PNG, không dùng ảnh đã gửi qua chat bị nén làm bản duy nhất. Nếu số hình thay đổi khi ghép Word, cập nhật đồng bộ tên file/danh sách hoặc giữ bảng ánh xạ rõ ràng.

Mẫu danh sách tự điền khi chụp:

| Số hình | Tên file | Môi trường | Vai trò | Đợt/đề tài/phiên bản | Đã kiểm tra | Ngày chụp |
|---|---|---|---|---|---|---|
| 5.1 | Hinh_5_01_Dang_nhap.png | Local HTTPS | Chưa đăng nhập | Không áp dụng | Chưa thực hiện | Điền sau |
| 5.18 | Hinh_5_18_Ket_qua_SV.png | Local HTTPS | SV1 | Điền bộ dữ liệu thực tế | Chưa thực hiện | Điền sau |
| 5.22 | Hinh_5_22_Email.png | Mailpit local | Người nhận SV1 | Điền đúng phiên bản kết quả | Chưa thực hiện | Điền sau |

Không chụp mật khẩu đang hiện, Environment chứa bí mật, private key/PFX password, thông tin CCCD hoặc dữ liệu cá nhân thật. Nếu ảnh có dữ liệu nhạy cảm, chụp lại bằng dữ liệu demo hoặc che rõ ràng trước khi đưa Word; không chỉnh sửa điểm/trạng thái trong ảnh để tạo kết quả giả.

## 8. Cách đưa vào Word và viết mô tả

1. Dùng định dạng heading của mẫu báo cáo cho chương và các mục 5.1–5.9.
2. Chèn ảnh đúng tỉ lệ, không kéo méo. Cắt phần trống nhưng giữ thông tin cần hiểu bối cảnh.
3. Căn giữa ảnh; đặt chú thích bên dưới bằng chức năng Caption nếu mẫu Word hỗ trợ. Thực hiện đánh số theo chương theo quy định chung của nhóm.
4. Sau chú thích, viết một đoạn 3–5 câu phù hợp độ phức tạp: **vai trò → mục đích → thao tác → kết quả/ràng buộc**. Dùng mô tả gợi ý mục 5 làm khung, bổ sung chi tiết thật.
5. Nếu một luồng có hai trạng thái, đặt ảnh trước/sau hoặc hình a/b và giải thích rõ; không dùng một ảnh form để mô tả toàn bộ luồng đã hoàn tất.
6. Cỡ chữ, font, giãn dòng, lề và khoảng cách theo mẫu Word/thống nhất của nhóm. Không tự chọn một bộ định dạng khác các chương còn lại.
7. Xem lại Word ở 100% và bản PDF xuất ra: chữ trong ảnh phải đọc được, chú thích không bị tách xa ảnh, bảng/ảnh không tràn lề.

Mẫu đoạn đầy đủ cho hình sửa tài khoản:

> Quản trị viên sử dụng màn hình chỉnh sửa để cập nhật tên đăng nhập, thông tin cá nhân, vai trò và trạng thái tài khoản. Chức năng đặt lại mật khẩu được tích hợp trong cùng biểu mẫu. Nếu để trống cả hai trường mật khẩu, hệ thống giữ mật khẩu hiện tại; nếu nhập mới, hai giá trị phải khớp nhau. Biểu tượng hình con mắt cho phép hiện hoặc ẩn nội dung đang nhập. Hệ thống không cung cấp lại mật khẩu cũ đã được băm.

Mẫu đoạn mô tả luồng nhiều màn hình:

> Nhóm trưởng lựa chọn đề tài đã công bố khi nhóm đủ số thành viên và còn thời gian đăng ký. Sau khi gửi, đăng ký chuyển sang chờ duyệt và giữ chỗ đề tài. Khoa kiểm tra thông tin nhóm trước khi chấp thuận. Sau chấp thuận, nhóm thực hiện đề tài và nộp bộ báo cáo theo hạn quy định.

Chỉ dùng những câu có ảnh hoặc kết quả thao tác hỗ trợ. Không viết “bảo mật tuyệt đối”, “không còn lỗi”, “chịu tải lớn”, “gửi Gmail thành công” khi chưa có bằng chứng tương ứng.

## 9. Các lỗi thường gặp trong quá trình thực hiện

| Hiện tượng | Hướng xử lý |
|---|---|
| Không có dữ liệu sau khi chuyển online/local | Kiểm tra đúng database/môi trường; dữ liệu không tự đồng bộ |
| Chuyển tài khoản ở tab A làm tab B đổi theo | Cùng phiên trình duyệt; dùng profile khác hoặc đăng nhập lần lượt |
| Chưa tới giai đoạn SV | Đọc lịch đợt và chờ đúng giờ; không sửa SQL/đồng hồ |
| Đăng ký bị chặn do số thành viên | Kiểm tra thành viên đã đồng ý, đủ 3–5 người, đúng một trưởng |
| Không phân công được người chấm | Kiểm tra GVHD, vai trò/hồ sơ và hội đồng có đúng đợt/chức danh |
| Chưa mở được phản biện | Kiểm tra quá hạn báo cáo, tới lịch, có bộ báo cáo hợp lệ và đăng ký chấp thuận |
| Chưa mở được hội đồng | Kiểm tra loại đề tài, kế hoạch, PB đã hoàn tất và tới giờ bắt đầu |
| Chưa tổng hợp/công bố được | Kiểm tra đủ phiếu chính thức, đúng người tổng hợp, trạng thái xác nhận/công bố và yêu cầu sửa đang xử lý |
| Mailpit trống | Bật Mailpit/MAIL_ENABLED trước khởi động; kiểm tra đã Công bố, không chỉ Xác nhận; xem log SMTP |
| Render có bản cũ | Kiểm tra branch/commit deploy rồi tải lại; không suy ra local mới là online đã mới |
| Ảnh đầu/cuối khác điểm hoặc phiên bản | Kiểm tra có vừa sửa điểm/công bố lại không; chụp và mô tả theo đúng tiến trình |

## 10. Checklist hoàn tất và bàn giao cho nhóm

### Nội dung

- [ ] Có đoạn giới thiệu chương, các mục theo vai trò/luồng và thứ tự dễ theo dõi.
- [ ] Có đăng nhập, quản trị, đợt/đề tài, nhóm/đăng ký, báo cáo, chấm/công bố, sửa điểm, thông báo/email.
- [ ] Các ảnh có số hình, tên hình và mô tả; không để ảnh đứng một mình.
- [ ] Mô tả đúng quy tắc 3–5 SV, GVHD không chấm, PB trước hội đồng và công bố sau tổng hợp/xác nhận.
- [ ] Không nhầm duyệt đề tài với duyệt đăng ký, nháp với nộp, xác nhận với công bố.
- [ ] Phân biệt local, online, Mailpit và thử trên thiết bị thật/mô phỏng.

### Minh chứng

- [ ] Ảnh lấy từ giao diện chạy thật; đã kiểm tra thao tác và trạng thái sau lưu.
- [ ] Bộ dữ liệu, tên đề tài, điểm và phiên bản nhất quán xuyên suốt.
- [ ] Điểm trước/sau điều chỉnh và email khớp phiên bản được mô tả.
- [ ] Có ít nhất ví dụ phản hồi nhập liệu bắt buộc và mật khẩu xác nhận không khớp nếu giữ các hình đã đề xuất.
- [ ] Ảnh rõ tại kích thước Word; không méo, không tràn lề, không lộ bí mật.
- [ ] Giữ ảnh gốc và danh sách ghi nhận, chưa đánh dấu đạt cho phần chưa tự làm.

### Bàn giao

- [ ] Gửi Chương 5 cùng ảnh gốc và thông tin phiên bản source/môi trường/ngày chụp.
- [ ] Thống nhất font, chú thích, số hình với người ghép Word.
- [ ] Bổ sung tên/hình vào danh mục hình của báo cáo, kiểm tra tham chiếu chéo.
- [ ] Xuất PDF và rà lại từng trang sau khi ghép với các chương khác.
- [ ] Ghi riêng phần chưa chụp/chưa hoàn tất để thành viên ghép báo cáo biết; không tự ghi là đã xong.

Kết thúc Chương 5 có thể dùng một đoạn chuyển tiếp ngắn sang Chương 6; phần ưu/nhược điểm và hướng phát triển để ở Chương 6, tránh lặp lại toàn bộ trong chương giao diện.
