# HTTPS local — tên miền thật và localhost

Cập nhật 20/10/2026. Cách đang dùng trên máy chính là **local-domain** với chứng chỉ Let's Encrypt và tên miền qldt.nhom4qldt.id.vn. Render dùng cấu hình riêng, không bị thay đổi khi chạy local.

## 1. Phân biệt các bản chạy

| Bản | URL | Profile/chứng chỉ | Dữ liệu |
|---|---|---|---|
| HTTP local | http://localhost:8080 | Mặc định, không TLS | MySQL local |
| HTTPS tên miền local | https://qldt.nhom4qldt.id.vn:8443 | local-domain, PFX win-acme | MySQL local |
| HTTPS localhost tùy chọn | https://localhost:8443 | local-https, P12 tự ký | MySQL local |
| Online | https://nhom4qldt.id.vn | prod, TLS do Render | Aiven |

Chỉ bật một profile cho mỗi tiến trình. Hai profile HTTPS dùng cổng 8443, không chạy cùng cổng. Profile local chỉ bind 127.0.0.1; không phải tunnel/public hosting. Người khác không tự truy cập được tên miền local của máy bạn; gửi URL online cho họ.

## 2. Điều kiện trước khi chạy tên miền local

- Java 21, MySQL local và tài khoản database đã dùng được theo README.
- Có `.local-https/qldt.nhom4qldt.id.vn.pfx` và biết PfxPassword do mình đặt trong win-acme.
- Hosts của máy có ánh xạ đúng ở mục 3.
- Đã dừng tiến trình cũ dùng cổng 8443.

PFX chứa khóa riêng, không gửi cho nhóm hoặc đưa lên Git/Render. Thành viên mới có thể dùng HTTP; không bắt buộc mỗi máy phải cấu hình tên miền này. Hướng dẫn không có mật khẩu mẫu dùng chung.

## 3. Ánh xạ hosts, làm một lần trên máy

Mở Notepad bằng Run as administrator → File/Open → All files → mở:

```text
C:\Windows\System32\drivers\etc\hosts
```

Giữ nội dung cũ, thêm đúng một dòng, hoặc sửa dòng tên miền này nếu đã có:

```text
127.0.0.1 qldt.nhom4qldt.id.vn
```

Không thêm @/www hay tên miền root nhom4qldt.id.vn vào hosts; root dành cho bản online. Lưu đúng tên hosts, không phải hosts.txt. Không cần tạo bản ghi A công khai về IP nhà hoặc mở cổng router.

Kiểm tra trong PowerShell:

```powershell
ipconfig /flushdns
[System.Net.Dns]::GetHostAddresses("qldt.nhom4qldt.id.vn")
```

Kết quả có 127.0.0.1. Nếu flushdns yêu cầu quyền cao hơn, dùng terminal Administrator cho riêng lệnh đó; nếu tên miền đã ra đúng loopback thì không cần coi lỗi flush cache là lỗi ứng dụng.

## 4. Chạy HTTPS — nhập mật khẩu một lần mỗi terminal

Trong **PowerShell** tại thư mục chứa pom.xml:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
$env:DB_USERNAME = "doan_dev"
& .\scripts\Chay-HttpsLocal.ps1 -TenMien
```

ExecutionPolicy ở Scope Process chỉ có tác dụng với phiên hiện tại. Nếu JAVA_HOME chưa đúng, đặt nó tới JDK 21 của máy. DB_USERNAME có thể thay bằng user ứng dụng của máy mình, không dùng avnadmin cloud.

Script hỏi mật khẩu PFX, và mật khẩu MySQL nếu DB_PASSWORD chưa đặt. Khi Started, mở **https://qldt.nhom4qldt.id.vn:8443/login**. Tài khoản web là tài khoản đã có trong DB local, không cần tạo lại admin.

Dừng bằng Ctrl+C rồi chạy lại **trong cùng terminal**:

```powershell
& .\scripts\Chay-HttpsLocal.ps1 -TenMien
```

Không hỏi lại hai mật khẩu đã nhớ. Script lưu SecureString trong bộ nhớ PowerShell theo project/chứng chỉ/user DB, khôi phục env ban đầu khi dừng. Đóng terminal/VS Code thì mất cache. Không dùng powershell.exe -File vì sẽ tạo một phiên con mới mỗi lần.

Nếu vừa nhập sai/đổi mật khẩu:

```powershell
Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
& .\scripts\Chay-HttpsLocal.ps1 -TenMien -NhapLaiMatKhau
```

Nếu DB_PASSWORD đã có từ trước, script dùng biến đó nên cần xóa khi muốn được hỏi lại. Muốn xóa toàn bộ mật khẩu nhớ trong phiên, dừng ứng dụng rồi chạy `$global:QldtMatKhauPhien.Clear()` hoặc đóng terminal.

## 5. Kiểm tra kết nối

- Giữ :8443 trong URL. Không dùng localhost/IP cho chứng chỉ chỉ cấp qldt.nhom4qldt.id.vn.
- Chrome/Edge không có cảnh báo TLS; phần thông tin kết nối hiển thị đúng tên miền và thời hạn thực tế.
- Đăng nhập/đăng xuất, thử chức năng tạo/sửa, logo/CSS và con mắt mật khẩu.
- Profile local-domain dùng cookie QLDT_LOCAL_DOMAIN_SESSION có Secure, HttpOnly, SameSite=Lax; không ghi đè cookie phiên HTTP/localhost của profile khác.
- Link trong email local dùng tên miền này; muốn kiểm tra thư vẫn cần bật Mailpit như [hướng dẫn email](HUONG_DAN_EMAIL.md).
- Có thể dùng `curl.exe -I https://qldt.nhom4qldt.id.vn:8443/login`. Không dùng -k/bỏ qua cảnh báo để kết luận HTTPS đã đúng.

Không cần nhập chứng chỉ Let's Encrypt vào Trusted Root Windows. Nếu không tin cậy, kiểm tra hostname/chain/ngày giờ, không tự thêm các CA không rõ nguồn vào máy.

## 6. Cấp và gia hạn bằng win-acme

Lần đầu đã cấp thành công bằng win-acme, không cần tạo lại mỗi lần chạy. Mốc Next renewal due after ghi nhận là **14/12/2026**, không phải ngày hết hạn. Xem hạn thực tế trong trình duyệt hoặc certificate trước buổi demo.

Khi cần gia hạn, mở win-acme ở thư mục công cụ riêng, quản lý renewal hiện có; làm theo mã mới công cụ đưa ra. Không tạo hàng loạt certificate mới để thử lỗi DNS.

Tóm tắt phương án đã dùng:

1. Full options → Manual input, host qldt.nhom4qldt.id.vn → Single certificate.
2. DNS validation manual → RSA → PFX archive vào `.local-https/`, đặt mật khẩu và lưu riêng.
3. Không cài IIS. Đọc và chấp nhận điều khoản nếu đồng ý, nhập email liên hệ.
4. Tạo TXT ở Tenten: tên `_acme-challenge.qldt`, giá trị đúng mã win-acme sinh cho lần đó. Không dùng lại mã cũ trong chat/tài liệu.
5. Kiểm tra DNS công cộng bằng Resolve-DnsName với 1.1.1.1/8.8.8.8, rồi cho công cụ xác minh. Chỉ bỏ qua kiểm tra local resolver nếu đã xác nhận DNS công cộng/authoritative đúng, không bỏ qua mù.
6. Khi Authorization valid, xóa đúng TXT thử và tiếp tục nhận PFX. Không xóa DNS web/mail khác.
7. Sau PFX mới được ghi, khởi động lại Spring Boot và kiểm tra thời hạn.

DNS manual không tự hoàn tất gia hạn. Lần đầu win-acme báo không tạo được Scheduled Task do quyền; không coi có lịch tự động đang bảo đảm gia hạn. Khóa/PFX chỉ ở máy riêng, tên chứng chỉ có thể xuất hiện trong Certificate Transparency.

## 7. Phương án riêng: localhost tự ký

Chỉ dùng nếu muốn https://localhost:8443, không cần khi đã chạy tên miền thật:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\Tao-ChungChiLocal.ps1
```

Script tìm keytool từ JAVA_HOME/PATH, tạo `.local-https/localhost.p12` và localhost.cer, hạn 365 ngày, SAN localhost/127.0.0.1/::1. Không ghi đè P12 đã có và không tự sửa kho tin cậy Windows.

Để Chrome/Edge tin chứng chỉ tự ký này, chạy trên máy đã tạo:

```powershell
certutil.exe -user -addstore Root .\.local-https\localhost.cer
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
& .\scripts\Chay-HttpsLocal.ps1
```

Lệnh certutil thay đổi Trusted Root của tài khoản Windows hiện tại; chỉ chấp nhận đúng chứng chỉ localhost mình vừa tạo. Không dùng bước này cho PFX Let's Encrypt và không chia sẻ private key. Firefox có thể cần thiết lập kho tin cậy riêng. Khi gỡ, đối chiếu đúng chứng chỉ trong certmgr.msc, không xóa hàng loạt localhost của phần mềm khác.

## 8. Lỗi thường gặp và quay lại HTTP

| Lỗi | Kiểm tra |
|---|---|
| MySQL 1045 doan_dev | Mật khẩu DB local, không phải mật khẩu PFX/root; xóa env cũ và nhập lại như mục 4 |
| keystore password incorrect | PfxPassword đã đặt trong win-acme; không đổi mật khẩu MySQL để chữa |
| Không tìm thấy PFX/P12 | Đúng file, đúng thư mục .local-https; mỗi máy không tự có cert của máy khác |
| Cảnh báo hostname | Đúng URL và SAN của chứng chỉ; hosts chỉ giúp phân giải, không sửa SAN |
| Connection refused/cổng đã dùng | Spring Boot đã Started chưa, có tiến trình cũ trên 8443 không |
| Scripts disabled | Đúng PowerShell và Scope Process như mục 4; không đổi chính sách Windows lâu dài |

Muốn về HTTP: Ctrl+C, đặt DB_USERNAME/DB_PASSWORD theo README rồi chạy Maven bình thường, không truyền profile. Kiểm tra SPRING_PROFILES_ACTIVE không còn local-domain/local-https/prod. Script không tự đặt profile ở cấp Windows.

Nguồn: [Spring Boot HTTPS](https://docs.spring.io/spring-boot/3.5/how-to/webserver.html), [win-acme DNS manual](https://www.win-acme.com/reference/plugins/validation/dns/manual), [win-acme PFX](https://www.win-acme.com/reference/plugins/store/pfxfile).
