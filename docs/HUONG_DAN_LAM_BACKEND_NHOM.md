# Hướng dẫn phát triển backend cho nhóm 4 thành viên

## 1. Kiến trúc thống nhất

Ứng dụng là Spring Boot monolith, tổ chức package theo tính năng. Mỗi tính năng đi theo một luồng hoàn chỉnh:

```text
<module>/
├── <Entity>.java
├── <Repository>.java
├── <Service>.java
├── <Controller>.java
├── dto/
└── form/
```

- Controller chỉ nhận request, kiểm tra form và gọi service.
- Quy tắc nghiệp vụ và `@Transactional` đặt trong service.
- Repository chỉ truy cập dữ liệu, không chứa nghiệp vụ.
- Entity bảo vệ trạng thái bằng phương thức có tên nghiệp vụ; không tạo public setter cho mọi trường.
- Form nhập liệu dùng Bean Validation. Không bind trực tiếp request vào entity.
- Thymeleaf nhận DTO hoặc dữ liệu chỉ đọc từ controller.

## 2. Phân công không chồng chéo

| Thành viên | Package sở hữu | Bảng chính | URL sở hữu |
|---|---|---|---|
| 1 | `account`, `hoso` | `tai_khoan`, `tai_khoan_vai_tro`, `bo_mon`, `sinh_vien`, `giang_vien` | `/admin/**`, `/ho-so/**` |
| 2 | `dotdangky`, `detai` | `dot_dang_ky`, `de_tai`, `huong_dan` | `/khoa/dot-dang-ky/**`, `/de-tai/**` |
| 3 | `nhom`, `dangky` | `nhom`, `thanh_vien_nhom`, `loi_moi_nhom`, `dang_ky` | `/sinh-vien/nhom/**`, `/sinh-vien/dang-ky/**` |
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
