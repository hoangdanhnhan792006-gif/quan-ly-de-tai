# Bối cảnh và đặc tả hiện hành — Hệ thống Quản lý Đề tài Sinh viên

**Cập nhật 20/10/2026.** Có thể sao chép toàn bộ tài liệu này vào đầu chat mới để bàn giao bối cảnh. Đây là ảnh chụp trạng thái tại ngày cập nhật, không thay cho việc kiểm tra code/Git mới nhất.

## 1. Dự án và nguyên tắc

- Đồ án Lập trình Web, nhóm 4 SV, Khoa CNTT. Người trao đổi hiện là thành viên 1.
- Người dùng ưu tiên Spring Boot, code theo kiến thức đã học, dễ đọc và giải thích khi bảo vệ. Business logic phải ở Service, không đặt trong Controller/View.
- Hệ thống quản lý đề tài MON_HOC, NCKH, TLCN, KLTN; không phải hệ thống tín chỉ, không có môn tiên quyết, tích lũy tín chỉ, lớp học phần hay thời khóa biểu.
- Phiên bản đang dùng là 24 bảng, thay bản 27 bảng cũ. Quy tắc rubric mới nhất là nhóm 3–5 SV khi đăng ký; không dùng lại giới hạn 3 SV hoặc cho GV/SV chồng giai đoạn của thảo luận ban đầu.
- Không tự thay đổi quy tắc nghiệp vụ, reset DB, sửa migration cũ, xóa file hoặc push/deploy khi nhiệm vụ chỉ là review. Không ghi bí mật trong tài liệu/chat/Git.

## 2. Vị trí, Git và môi trường

| Mục | Giá trị |
|---|---|
| Project đang làm | D:\MonHoc\lap_trinh_web\cuoi_ki\quan_ly_de_tai |
| Repo | https://github.com/hoangdanhnhan792006-gif/quan-ly-de-tai.git |
| Nhánh tích hợp | develop |
| Nhánh local từng sử dụng | feature/tai-khoan-ho-so; không mặc định người làm đang đứng develop |
| FE đã ghép | feature/giao-dien, mốc 9172a47, ghép với backend có Aiven SSL |
| Online chính | https://nhom4qldt.id.vn |
| Online dự phòng | https://quan-ly-de-tai.onrender.com |
| Local HTTP | http://localhost:8080 |
| Local tên miền HTTPS | https://qldt.nhom4qldt.id.vn:8443 |
| Tài liệu thầy, ngoài repo | D:\MonHoc\lap_trinh_web\cuoi_ki: Rubric.pdf, Deployment.pdf, Lab 6 - Sending emails.pdf |

Các thư mục C:\Users\Admin\Documents\Codex và bản D:\...\2026-09-17 là vị trí cũ; không tự sửa nhầm bản copy. Trước khi làm phải kiểm tra đúng pom.xml, git status và nhánh. Đường dẫn JDK/tài khoản MySQL mỗi máy có thể khác.

## 3. Kiến trúc đang có

Java 21, Spring Boot 3.5.16, Spring MVC @Controller, Thymeleaf, HTML/CSS/JS thuần. Không có FE SPA tách riêng, REST API riêng, JWT hoặc microservices.

Spring Data JPA/Hibernate, repository, entity mapping, transaction tại Service. Spring Security form login/session/BCrypt, CSRF và @PreAuthorize; Service kiểm tra quyền theo nhóm, bộ môn, đề tài, phân công chấm. Bean Validation tại Form/Service. Constructor injection.

Module: account, hoso, dotdangky, detai, nhom, dangky, baocao, danhgia, thongbao, email, common, config, web. Mỗi module có model/repository/service/controller/form/dto khi cần; view nằm trong resources/templates, tài nguyên công khai trong resources/static.

Phân công module: TV1 account/hoso; TV2 dotdangky/detai; TV3 nhom/dangky; TV4 baocao/danhgia/thongbao/email. Đây là phân chia trách nhiệm, bảng Word phải phản ánh đúng đóng góp thực tế của nhóm.

## 4. Actors và quyền

- ADMIN: tài khoản, bộ môn, hồ sơ; quản lý thông báo theo quyền.
- TRUONG_KHOA: quyền khoa và tạo/sửa/triển khai/kết thúc/hủy đợt.
- CAN_BO_KHOA: công bố đề tài, duyệt nhóm đăng ký, hội đồng/kế hoạch/phân công, duyệt sửa, xác nhận/công bố kết quả.
- TRUONG_BO_MON: cần thêm GIANG_VIEN và hồ sơ GV có bộ môn; duyệt đề tài và gán 1–2 GVHD trong bộ môn mình.
- GIANG_VIEN: đề xuất và xem các đề tài hướng dẫn/chấm được phân công.
- SINH_VIEN: hồ sơ, nhóm, lời mời, đề tài đã công bố, kết quả của mình.
- Nhóm trưởng, GVHD, GVPB, chủ tịch/thư ký/ủy viên không phải quyền đăng nhập riêng; kiểm tra bằng bản ghi nghiệp vụ. ADMIN không mặc nhiên có các quyền học thuật khác.

## 5. Nghiệp vụ bất biến

### Đợt và đề tài

- Trưởng khoa tạo đợt. GV đăng ký trước, sau đó SV đăng ký: gv_ket_thuc <= sv_bat_dau. Thao tác phải đúng trạng thái và thời gian server.
- Hạn báo cáo chung mỗi đợt. TLCN/KLTN có hạn nộp điểm PB; KLTN có ngày hội đồng cấp đợt. Không nhập các mốc không áp dụng.
- GV đề xuất; SV không tự đề xuất. Mỗi đề tài thuộc một bộ môn và đợt. Bộ môn duyệt/phân công, khoa công bố. SV chỉ chọn đề tài đã công bố.
- Người đề xuất không mặc nhiên là GVHD; có 1–2 GVHD. GVHD không chấm hoặc tham gia chấm trong hội đồng của đề tài đó, kể cả được gán hướng dẫn sau khi đã phân công chấm.

### Nhóm và đăng ký

- SV tạo nhóm trở thành trưởng nhóm, mời người khác và phải có chấp nhận.
- Mỗi SV tối đa một nhóm trong một đợt; có thể tham gia đợt khác. Nhóm đang lập có thể 1–2 SV; đăng ký và chấp thuận phải đủ 3–5 SV, đúng một trưởng.
- Chỉ trưởng nhóm đăng ký/nộp báo cáo. Mỗi nhóm một đăng ký hiện hành, mỗi đề tài một nhóm giữ chỗ.
- First-come: CHO_DUYET cũng giữ chỗ; nhóm khác không đăng ký được. Đổi đề tài trong một transaction; thất bại giữ đăng ký cũ.
- Thành viên/trưởng/đề tài đổi khi còn hạn và chưa được chấp thuận; sau chấp thuận khóa. Từ chối có thể chọn lại khi còn hạn.
- Pessimistic lock + UNIQUE; QuyTacNhom khóa đợt → đề tài cũ/mới tăng dần ID → nhóm. Không thay bằng kiểm tra exists rồi save không khóa.

### Báo cáo và đánh giá

- Mỗi phiên bản báo cáo đủ PDF + slide PDF/PPTX + ZIP source. Tối đa 50 MiB mỗi tệp, kiểm tra nội dung và cấu trúc; ZIP giải nén tối đa 200 MiB. Không dùng tệp chỉ đổi đuôi.
- Chỉ trưởng nộp trước hạn, được nộp lại đủ ba tệp. Bản cuối được chốt khi mở phản biện, giữ phiên bản cũ. Download qua controller kiểm tra quyền, không public uploads.
- MON_HOC/TLCN: GVPB riêng. NCKH/KLTN: PB trước rồi hội đồng; 3–5 GV, đúng một chủ tịch và thư ký.
- Lập kế hoạch khác mở chấm. Mở PB sau hạn báo cáo, tới lịch và có báo cáo hợp lệ; mở hội đồng sau PB hoàn tất và tới lịch.
- Mỗi GV chấm một điểm tổng 0–10 và nhận xét; lưu nháp khác nộp chính thức. GVPB kiêm thành viên hội đồng dùng một phân công và một điểm.
- Điểm cuối = ROUND(tổng điểm các GV hợp lệ khác nhau / số GV đó, 1). Xét đạt theo điểm đã làm tròn >=4.0. Không có điểm GVHD.
- Đủ phiếu chính thức và hoàn tất đánh giá mới tổng hợp; chủ tịch tổng hợp nếu có hội đồng, khoa tổng hợp nếu chỉ PB. Sau đó khoa xác nhận rồi công bố.
- Code hiện xét hoàn tất theo đủ phiếu/trạng thái, không có điều kiện riêng bắt đợi giờ kết thúc hội đồng mới công bố. Đây là hành vi hiện tại; chỉ sửa nếu có quyết định nghiệp vụ mới.
- SV chỉ thấy kết quả đã công bố của chính nhóm/đợt tham gia; không truy cập đợt khác bằng đổi URL/filter.

### Sửa điểm, tài khoản, thông báo

- GV xin sửa → khoa duyệt → GV nộp phiếu phiên bản mới → người có quyền tổng hợp lại → khoa xác nhận/công bố. Giữ snapshot cũ và nhãn Đang điều chỉnh cho SV đến khi có bản công bố mới.
- Admin tạo tài khoản, không tự đăng ký. Sửa tên đăng nhập/thông tin/vai trò/trạng thái/mật khẩu trong một form, một transaction. Cả hai ô mật khẩu rỗng thì giữ; nhập mới phải xác nhận, 8 ký tự tối thiểu, giới hạn BCrypt 72 byte UTF-8. Không xem mật khẩu cũ.
- Có nút hiện/ẩn từng ô mật khẩu. Không tự xóa/khóa/bỏ ADMIN; đổi mật khẩu cá nhân cần mật khẩu cũ. Thu hồi phiên cũ khi thông tin xác thực/quyền thay đổi.
- Hồ sơ SV bắt buộc lớp/khóa học, nhóm bắt buộc tên, đề tài bắt buộc yêu cầu/sản phẩm dự kiến. Backend chặn rỗng/chỉ dấu cách.
- Thông báo có nháp/công bố/thu hồi, tệp; chỉ người đăng nhập xem. Xóa chỉ đối tượng chưa dùng; dữ liệu học thuật có FK RESTRICT được giữ.

## 6. Database và lưu tệp

24 bảng: bo_mon, tai_khoan, tai_khoan_vai_tro, sinh_vien, giang_vien, dot_dang_ky, de_tai, huong_dan, nhom, thanh_vien_nhom, loi_moi_nhom, dang_ky, bao_cao, bao_cao_tep, hoi_dong, thanh_vien_hoi_dong, danh_gia, phan_cong_cham, phieu_cham, yeu_cau_sua_diem, ket_qua, chi_tiet_ket_qua, thong_bao, thong_bao_tep.

Flyway V1 tạo 24 bảng; V2 thêm role trưởng bộ môn. Không có trigger nghiệp vụ, bảng audit hoặc hàng đợi email. Cột generated/UNIQUE/CHECK bảo vệ quy tắc một dòng và tính duy nhất; service chịu quy tắc liên bảng. FK RESTRICT; không cascade xóa lịch sử. Hibernate validate; không sửa V1/V2 đã áp dụng. Đặc tả đầy đủ trong CO_SO_DU_LIEU.md.

Local schema quan_ly_de_tai, user thường doan_dev; mỗi máy một mật khẩu và dữ liệu riêng. Online Aiven defaultdb, user avnadmin; không dùng mật khẩu root local cho cloud. Datetime lưu UTC, hiển thị/nhập giờ Việt Nam. ID entity hiện dùng Integer; điểm BigDecimal.

Tệp vật lý local trong uploads hoặc UPLOAD_DIR; MySQL chỉ giữ metadata/khóa. Phải sao lưu cả DB và tệp tương ứng, không coi backup DB là backup đủ báo cáo.

## 7. Email và môi trường chạy

Email theo Lab 6: KetQuaService phát ThongBaoKetQua; AFTER_COMMIT → @Async EmailService → JavaMailSender + Thymeleaf → mỗi SV một thư HTML. Không gửi trước commit; lỗi một thư không hủy kết quả và không chặn thử người khác. Không có retry bền vững.

Mailpit local SMTP localhost:1025, UI http://localhost:8025, không gửi tới Gmail. MAIL_ENABLED mặc định false; bật trước khi khởi động để demo.

- Mặc định local HTTP: 8080.
- local-https: localhost:8443 với chứng chỉ tự ký riêng, alias local-https.
- local-domain: qldt.nhom4qldt.id.vn:8443, win-acme PFX trong .local-https, hosts trỏ 127.0.0.1. Chứng chỉ chỉ có một private key, không ép alias localhost. Chỉ bind loopback.
- Script Chay-HttpsLocal.ps1 -TenMien nhớ mật khẩu bằng SecureString trong phiên PowerShell; & chạy cùng tiến trình, không gọi powershell.exe -File nếu muốn giữ cache. -NhapLaiMatKhau để nhập lại; env có sẵn có thể cần xóa DB_PASSWORD cũ.
- Không commit PFX/P12/key hoặc mật khẩu; Git và Docker đã ignore. Không cấu hình chứng chỉ local trên Render.

## 8. Deploy và tên miền đã thực hiện

Render Free chạy Docker Java 21, branch develop, profile prod, health /login, cổng PORT. Aiven đã kết nối qua Connector/J sslMode=VERIFY_IDENTITY; Docker nhập deploy/aiven-ca.pem vào bản truststore có các CA mặc định.

Đã deploy, người dùng xác nhận đăng nhập admin, tạo tài khoản/hồ sơ và xem FE online. Có lần timeout lúc khởi động; sau thêm JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1, log ghi Started trong khoảng 83 giây và người dùng truy cập được.

Tên miền nhom4qldt.id.vn đăng ký chương trình miễn phí 2 năm, ảnh quản lý ghi hết hạn 20/10/2028. DNS online: A @ → 216.24.57.1, CNAME www → quan-ly-de-tai.onrender.com. Render hiển thị Verified + Certificate Issued cho cả hai, www chuyển về root. APP_PUBLIC_URL nên đặt https://nhom4qldt.id.vn; chưa có truy cập dashboard để kiểm chứng biến thực tế đã lưu hay chưa.

Tên miền local qldt.nhom4qldt.id.vn đã cấp Let's Encrypt bằng DNS-01 manual. Đã xóa TXT xác minh sau cấp. PFX có trên máy, người dùng xác nhận chạy HTTPS local được. Win-acme không tạo được scheduled task vì quyền; DNS thủ công cũng không tự gia hạn được. Mốc nhắc gia hạn sau 14/12/2026 không phải ngày hết hạn chứng chỉ.

Giới hạn đã thống nhất: Render upload là ổ tạm; online tắt mail và chưa xác nhận SMTP Internet. Nhóm chấp nhận local demo đầy đủ, online minh chứng deploy; không làm Supabase/Brevo/tunnel lúc này. Không mô tả bản online đã lưu tệp bền vững hoặc gửi email thật đầy đủ.

## 9. Kiểm thử và tiến trình đã làm

- 20/10 sau ghép FE: verify liệt kê 209 ca; 208 đạt, 0 failure/error, 1 SMTP thật bỏ qua. Ba bộ MySQL chạy trên schema riêng và được dọn.
- 18/10 Mailpit: đã nhận 3 email HTML riêng, nội dung UTF-8, gửi sau commit và không gửi rollback; đây là lần kiểm thử khác với mốc SMTP skipped nêu trên.
- Chrome sau ghép: đăng nhập, con mắt, sửa username/password và đăng nhập lại giữ hồ sơ, trường bắt buộc, menu mobile, thông báo/tệp và trang theo vai trò đạt.
- Lỗi kế hoạch MON_HOC/TLCN gửi địa điểm hội đồng rỗng đã sửa tại service thành NULL, giữ CHECK ck_danh_gia_khong_hd.
- 20/10 thêm logo FIT ở trang chủ/login/menu/favicon; kiểm tra desktop/mobile.
- 20/10 HTTPS: kiểm tra TLS và hostname bằng chứng chỉ thử/schema riêng; người dùng đã tự nhập PFX thật và xác nhận mở web local được.
- 20/10 chỉnh bảng quản trị: đồng nhất chữ, căn giữa hàng, nút 36px, table-actions; cố định cột Khóa/Mở khóa. Chrome kiểm tra form, khóa/mở, password, mobile/table scroll; build thành công. Người dùng đã xác nhận giao diện local ổn; lượt tổng hợp tài liệu không tự coi bản chỉnh này đã được kiểm tra hết trên online.
- Script nhớ mật khẩu đã thử bằng prompt/Maven giả lập: lần 1 hỏi, lần 2 tái sử dụng, reset hỏi lại, env khôi phục; không ghi mật khẩu thật vào test.

Các log/ảnh thử trong .mvn-repository là cache cục bộ, không nộp hoặc commit toàn thư mục. Test không thay cho demo và vấn đáp; không bảo đảm tuyệt đối mọi lỗi đã hết.

## 10. Còn phải làm và đối chiếu rubric

| Hạng mục | Trạng thái/việc tiếp theo |
|---|---|
| Chức năng và kiến trúc | Các module chính đã có; không cần tự mở rộng kiến trúc lớn |
| FE | Đã ghép; các chỉnh sau đó cần đồng bộ develop và Manual Deploy khi nhóm chốt |
| Word, báo cáo kết quả nhóm | Chưa thấy bản cuối trong thư mục đã rà; cần nhận bản thành viên viết và review |
| Use case/ERD | Có nguồn; xuất ảnh rõ và đối chiếu code để đưa Word |
| FR/NFR | Đã bổ sung trong PHAM_VI_CHUC_NANG.md; không trình bày chỉ như danh sách trang |
| Minh chứng | Chụp UI cuối, nghiệp vụ, Mailpit, Render Live + domain/HTTPS; tránh mật khẩu và dữ liệu cá nhân |
| Demo | Chuẩn bị dữ liệu và thứ tự chuyển vai trò; dự phòng local khi hosting ngủ |
| Vấn đáp | Chiếm 3/10 rubric; từng người hiểu luồng, annotation, file/hàm phụ trách và sửa nhanh |
| Đóng gói | Word + source + báo cáo kết quả nhóm; tên ZIP theo mẫu GroupXX và MSSV thực tế. Link hosting tùy chọn theo xác nhận người dùng; không bắt buộc PPT |

Nên ghi mục tính năng mở rộng riêng trong Word: giữ chỗ an toàn khi đồng thời, phiên bản báo cáo/điểm, quy trình xin sửa, thu hồi phiên, giao diện responsive. Không cần tự làm Excel/PDF, SPA, cloud storage, email API hoặc tunnel chỉ vì rubric có ví dụ.

## 11. Bộ tài liệu cần giữ và file cũ

Giữ README, TONG_HOP_DU_AN, PHAM_VI_CHUC_NANG, CO_SO_DU_LIEU, HUONG_DAN_LAM_BACKEND_NHOM, HUONG_DAN_LAM_FRONTEND, HUONG_DAN_TEST_CHUC_NANG_WEB, HUONG_DAN_EMAIL, HTTPS_LOCAL, TRIEN_KHAI, HUONG_DAN_VIET_CHUONG_5 và nguồn USE_CASE.puml.

KIEM_THU_BAN_RUBRIC.md đã được hợp nhất vào phần kiểm thử/rubric ở đây, có thể bỏ sau khi nhóm review. Không xóa các tài liệu thầy PDF. Các bản Markdown cũ trong thư mục tailieu hoặc bản project copy theo ngày không phải nguồn hiện hành; đối chiếu trước khi bỏ. Tài liệu ngoài repo chưa tự được xóa.

| File/vị trí cũ | Đề xuất |
|---|---|
| `docs/KIEM_THU_BAN_RUBRIC.md` | Có thể bỏ: đã hợp nhất kết quả và rubric vào mục 9–10 của tài liệu này |
| `../tailieu/TONG_HOP_DU_AN.md`, `PHAM_VI_CHUC_NANG.md`, `HUONG_DAN_LAM_BACKEND_NHOM.md`, `HUONG_DAN_TEST_CHUC_NANG_WEB.md` | Có thể bỏ các bản cũ sau review; dùng bản trong `docs/` của repo |
| `../tailieu/KE_HOACH_DO_AN_NHOM_4_SPRING_BOOT.md` | Chỉ giữ nếu cần lịch sử lập kế hoạch; không dùng làm đặc tả hiện hành |
| `../tailieu/08_BAO_CAO_KIEM_THU.md` | Có thể lưu trữ riêng làm minh chứng lịch sử; trạng thái hiện tại ở mục 9, thao tác test ở hướng dẫn test web |
| `../tailieu/database_24_bang/` | Bản tham khảo cũ; dùng migration V1/V2 và `docs/CO_SO_DU_LIEU.md` cho đặc tả hiện hành. Đối chiếu file SQL/ERD riêng trước khi bỏ cả thư mục |

Không xóa PDF của giảng viên, migration SQL, `USE_CASE.puml`, script chạy, CA Aiven hoặc chứng chỉ/private key đang dùng chỉ vì đã có tài liệu mới. Bản ZIP/source copy cũ phải kiểm tra có thay đổi riêng hay không trước khi dọn.

Nếu tiếp tục ở chat mới: đọc tài liệu này, README và đúng source trong project D:, kiểm tra Git/trạng thái file, rồi làm nhiệm vụ cụ thể được giao. Không yêu cầu người dùng gửi lại mật khẩu hoặc toàn bộ lịch sử chat.
