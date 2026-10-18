-- Giữ nguyên dữ liệu và V1 đã phát hành.
ALTER TABLE tai_khoan_vai_tro DROP CHECK ck_tai_khoan_vai_tro_vai_tro;
ALTER TABLE tai_khoan_vai_tro ADD CONSTRAINT ck_tai_khoan_vai_tro_vai_tro
 CHECK (vai_tro IN ('ADMIN','TRUONG_KHOA','CAN_BO_KHOA','TRUONG_BO_MON','GIANG_VIEN','SINH_VIEN'));
