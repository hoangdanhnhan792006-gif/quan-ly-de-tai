-- HE THONG QUAN LY DE TAI SINH VIEN - PHIEN BAN NHOM 4 NGUOI, 24 BANG
-- Target: MySQL Server 8.0.46 / InnoDB / utf8mb4.
-- Select a NEW EMPTY database before running. This file does NOT create/use/drop a database.
-- No triggers: cross-table workflow, authorization and immutability must be enforced by Spring services.
-- Initial schema: run ONCE. No IF NOT EXISTS masking schema differences.
-- MySQL DDL may commit implicitly. This entire file is NOT one rollbackable transaction.
-- Do not disable STRICT_TRANS_TABLES, ONLY_FULL_GROUP_BY or FOREIGN_KEY_CHECKS.
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET time_zone = '+00:00';

-- 01. bo_mon: Bộ môn
CREATE TABLE `bo_mon` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `ma_bo_mon` VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Mã bộ môn.',
  `ten_bo_mon` VARCHAR(150) NOT NULL COMMENT 'Tên bộ môn.',
  `dang_hoat_dong` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1: hoạt động; 0: ngừng hoạt động.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bo_mon_ma` (`ma_bo_mon`),
  CONSTRAINT `ck_bo_mon_dang_hoat_dong` CHECK (dang_hoat_dong IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Bộ môn';

-- 02. tai_khoan: Tài khoản
CREATE TABLE `tai_khoan` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `ten_dang_nhap` VARCHAR(50) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Tên đăng nhập; backend chuẩn hóa chữ thường.',
  `mat_khau_bam` VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Hash mật khẩu, không lưu plaintext.',
  `ho_ten` VARCHAR(150) NOT NULL COMMENT 'Họ tên.',
  `email` VARCHAR(254) COLLATE utf8mb4_0900_as_ci NOT NULL COMMENT 'Email được chuẩn hóa nhất quán.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'HOAT_DONG' COMMENT 'Giá trị: HOAT_DONG, BI_KHOA.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo UTC.',
  `cap_nhat_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm cập nhật UTC.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tai_khoan_login` (`ten_dang_nhap`),
  UNIQUE KEY `uq_tai_khoan_email` (`email`),
  CONSTRAINT `ck_tai_khoan_trang_thai` CHECK (trang_thai IN ('HOAT_DONG', 'BI_KHOA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tài khoản';

-- 03. tai_khoan_vai_tro: Vai trò của tài khoản
CREATE TABLE `tai_khoan_vai_tro` (
  `tai_khoan_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản được cấp quyền.',
  `vai_tro` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Giá trị: ADMIN, TRUONG_KHOA, CAN_BO_KHOA, GIANG_VIEN, SINH_VIEN.',
  PRIMARY KEY (`tai_khoan_id`, `vai_tro`),
  KEY `ix_tai_khoan_vai_tro_vai_tro` (`vai_tro`, `tai_khoan_id`),
  CONSTRAINT `ck_tai_khoan_vai_tro_vai_tro` CHECK (vai_tro IN ('ADMIN', 'TRUONG_KHOA', 'CAN_BO_KHOA', 'GIANG_VIEN', 'SINH_VIEN')),
  CONSTRAINT `fk_tai_khoan_vai_tro_tai_khoan` FOREIGN KEY (`tai_khoan_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Vai trò của tài khoản';

-- 04. sinh_vien: Hồ sơ sinh viên
CREATE TABLE `sinh_vien` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `tai_khoan_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản của SV.',
  `ma_sv` VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Mã sinh viên.',
  `lop` VARCHAR(30) NULL DEFAULT NULL COMMENT 'Lớp hành chính, không phải lớp học phần.',
  `khoa_hoc` VARCHAR(30) NULL DEFAULT NULL COMMENT 'Khóa học.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sinh_vien_tai_khoan` (`tai_khoan_id`),
  UNIQUE KEY `uq_sinh_vien_ma` (`ma_sv`),
  CONSTRAINT `fk_sinh_vien_tai_khoan` FOREIGN KEY (`tai_khoan_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Hồ sơ sinh viên';

-- 05. giang_vien: Hồ sơ giảng viên
CREATE TABLE `giang_vien` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `tai_khoan_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản của GV.',
  `ma_gv` VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Mã giảng viên.',
  `bo_mon_id` INT UNSIGNED NOT NULL COMMENT 'Bộ môn công tác.',
  `hoc_vi` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Học vị.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_giang_vien_tai_khoan` (`tai_khoan_id`),
  UNIQUE KEY `uq_giang_vien_ma` (`ma_gv`),
  KEY `ix_giang_vien_fk_bo_mon` (`bo_mon_id`),
  CONSTRAINT `fk_giang_vien_tai_khoan` FOREIGN KEY (`tai_khoan_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_giang_vien_bo_mon` FOREIGN KEY (`bo_mon_id`) REFERENCES `bo_mon` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Hồ sơ giảng viên';

-- 06. dot_dang_ky: Đợt đăng ký
CREATE TABLE `dot_dang_ky` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `ma_dot` VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Mã đợt.',
  `ten_dot` VARCHAR(200) NOT NULL COMMENT 'Tên đợt.',
  `loai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Giá trị: MON_HOC, NCKH, TLCN, KLTN.',
  `gv_bat_dau` DATETIME(6) NOT NULL COMMENT 'Mở đề xuất của GV.',
  `gv_ket_thuc` DATETIME(6) NOT NULL COMMENT 'Đóng đề xuất của GV.',
  `sv_bat_dau` DATETIME(6) NOT NULL COMMENT 'Mở đăng ký của SV.',
  `sv_ket_thuc` DATETIME(6) NOT NULL COMMENT 'Đóng đăng ký của SV.',
  `han_nop_bao_cao` DATETIME(6) NOT NULL COMMENT 'Hạn nộp báo cáo.',
  `han_nop_diem_pb` DATETIME(6) NULL DEFAULT NULL COMMENT 'Hạn phản biện cấp đợt, chỉ TLCN/KLTN.',
  `ngay_bao_cao_hd` DATE NULL DEFAULT NULL COMMENT 'Ngày địa phương của hội đồng KLTN.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'NHAP' COMMENT 'Giá trị: NHAP, TRIEN_KHAI, KET_THUC, HUY.',
  `nguoi_tao_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản trưởng khoa tạo đợt.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dot_dang_ky_ma` (`ma_dot`),
  KEY `ix_dot_dang_ky_loai_tt` (`loai`, `trang_thai`),
  KEY `ix_dot_dang_ky_fk_nguoi_tao` (`nguoi_tao_id`),
  CONSTRAINT `ck_dot_dang_ky_loai` CHECK (loai IN ('MON_HOC', 'NCKH', 'TLCN', 'KLTN')),
  CONSTRAINT `ck_dot_dang_ky_trang_thai` CHECK (trang_thai IN ('NHAP', 'TRIEN_KHAI', 'KET_THUC', 'HUY')),
  CONSTRAINT `ck_dot_dang_ky_khoang_gv` CHECK (gv_bat_dau < gv_ket_thuc),
  CONSTRAINT `ck_dot_dang_ky_khoang_sv` CHECK (sv_bat_dau < sv_ket_thuc),
  CONSTRAINT `ck_dot_dang_ky_han_pb` CHECK (((loai IN ('TLCN','KLTN') AND han_nop_diem_pb IS NOT NULL) OR (loai IN ('MON_HOC','NCKH') AND han_nop_diem_pb IS NULL))),
  CONSTRAINT `ck_dot_dang_ky_ngay_hd` CHECK (((loai = 'KLTN' AND ngay_bao_cao_hd IS NOT NULL) OR (loai <> 'KLTN' AND ngay_bao_cao_hd IS NULL))),
  CONSTRAINT `fk_dot_dang_ky_nguoi_tao` FOREIGN KEY (`nguoi_tao_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Đợt đăng ký';

-- 07. de_tai: Đề tài trong một đợt
CREATE TABLE `de_tai` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `dot_id` INT UNSIGNED NOT NULL COMMENT 'Đợt của đề tài.',
  `bo_mon_id` INT UNSIGNED NOT NULL COMMENT 'Bộ môn quản lý.',
  `ma_de_tai` VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Mã đề tài.',
  `ten_de_tai` VARCHAR(255) NOT NULL COMMENT 'Tên đề tài.',
  `noi_dung` TEXT NOT NULL COMMENT 'Nội dung đề tài.',
  `yeu_cau` TEXT NULL DEFAULT NULL COMMENT 'Yêu cầu thực hiện.',
  `san_pham_du_kien` TEXT NULL DEFAULT NULL COMMENT 'Sản phẩm dự kiến.',
  `gv_de_xuat_id` INT UNSIGNED NOT NULL COMMENT 'GV đề xuất, không mặc nhiên là GVHD.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'NHAP' COMMENT 'Giá trị: NHAP, CHO_DUYET, TU_CHOI, DA_DUYET, DA_CONG_BO.',
  `nguoi_duyet_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Tài khoản duyệt.',
  `duyet_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm duyệt.',
  `nhan_xet_duyet` TEXT NULL DEFAULT NULL COMMENT 'Nhận xét duyệt.',
  `nguoi_cong_bo_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Tài khoản công bố.',
  `cong_bo_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm công bố.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_de_tai_ma` (`ma_de_tai`),
  UNIQUE KEY `uq_de_tai_id_dot` (`id`, `dot_id`),
  KEY `ix_de_tai_dot_tt_bm` (`dot_id`, `trang_thai`, `bo_mon_id`),
  KEY `ix_de_tai_gv_dot` (`gv_de_xuat_id`, `dot_id`),
  KEY `ix_de_tai_fk_bo_mon_id` (`bo_mon_id`),
  KEY `ix_de_tai_fk_nguoi_duyet_id` (`nguoi_duyet_id`),
  KEY `ix_de_tai_fk_nguoi_cong_bo_id` (`nguoi_cong_bo_id`),
  CONSTRAINT `ck_de_tai_trang_thai` CHECK (trang_thai IN ('NHAP', 'CHO_DUYET', 'TU_CHOI', 'DA_DUYET', 'DA_CONG_BO')),
  CONSTRAINT `ck_de_tai_nguoi_duyet_id_pair` CHECK (((nguoi_duyet_id IS NULL AND duyet_luc IS NULL) OR (nguoi_duyet_id IS NOT NULL AND duyet_luc IS NOT NULL))),
  CONSTRAINT `ck_de_tai_nguoi_cong_bo_id_pair` CHECK (((nguoi_cong_bo_id IS NULL AND cong_bo_luc IS NULL) OR (nguoi_cong_bo_id IS NOT NULL AND cong_bo_luc IS NOT NULL))),
  CONSTRAINT `ck_de_tai_duyet_du` CHECK (trang_thai NOT IN ('TU_CHOI','DA_DUYET','DA_CONG_BO') OR nguoi_duyet_id IS NOT NULL),
  CONSTRAINT `ck_de_tai_cong_bo_du` CHECK (trang_thai <> 'DA_CONG_BO' OR nguoi_cong_bo_id IS NOT NULL),
  CONSTRAINT `fk_de_tai_dot_id` FOREIGN KEY (`dot_id`) REFERENCES `dot_dang_ky` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_de_tai_bo_mon_id` FOREIGN KEY (`bo_mon_id`) REFERENCES `bo_mon` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_de_tai_gv_de_xuat_id` FOREIGN KEY (`gv_de_xuat_id`) REFERENCES `giang_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_de_tai_nguoi_duyet_id` FOREIGN KEY (`nguoi_duyet_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_de_tai_nguoi_cong_bo_id` FOREIGN KEY (`nguoi_cong_bo_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Đề tài trong một đợt';

-- 08. huong_dan: Phân công hướng dẫn
CREATE TABLE `huong_dan` (
  `de_tai_id` INT UNSIGNED NOT NULL COMMENT 'Đề tài.',
  `giang_vien_id` INT UNSIGNED NOT NULL COMMENT 'GVHD.',
  `nguoi_phan_cong_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản phân công.',
  `phan_cong_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm phân công.',
  PRIMARY KEY (`de_tai_id`, `giang_vien_id`),
  KEY `ix_huong_dan_gv_dt` (`giang_vien_id`, `de_tai_id`),
  KEY `ix_huong_dan_fk_nguoi_pc` (`nguoi_phan_cong_id`),
  CONSTRAINT `fk_huong_dan_de_tai` FOREIGN KEY (`de_tai_id`) REFERENCES `de_tai` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_huong_dan_gv` FOREIGN KEY (`giang_vien_id`) REFERENCES `giang_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_huong_dan_nguoi_pc` FOREIGN KEY (`nguoi_phan_cong_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Phân công hướng dẫn';

-- 09. nhom: Nhóm sinh viên
CREATE TABLE `nhom` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `dot_id` INT UNSIGNED NOT NULL COMMENT 'Đợt của nhóm.',
  `ma_nhom` VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Mã nhóm trong đợt.',
  `ten_nhom` VARCHAR(150) NULL DEFAULT NULL COMMENT 'Tên nhóm.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'DANG_LAP' COMMENT 'Giá trị: DANG_LAP, DA_CHOT, DANG_THUC_HIEN, HOAN_THANH, GIAI_THE.',
  `nguoi_tao_id` INT UNSIGNED NOT NULL COMMENT 'SV tạo nhóm và làm trưởng nhóm ban đầu.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nhom_dot_ma` (`dot_id`, `ma_nhom`),
  UNIQUE KEY `uq_nhom_id_dot` (`id`, `dot_id`),
  KEY `ix_nhom_dot_tt` (`dot_id`, `trang_thai`),
  KEY `ix_nhom_fk_nguoi_tao` (`nguoi_tao_id`),
  CONSTRAINT `ck_nhom_trang_thai` CHECK (trang_thai IN ('DANG_LAP', 'DA_CHOT', 'DANG_THUC_HIEN', 'HOAN_THANH', 'GIAI_THE')),
  CONSTRAINT `fk_nhom_dot` FOREIGN KEY (`dot_id`) REFERENCES `dot_dang_ky` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_nhom_nguoi_tao` FOREIGN KEY (`nguoi_tao_id`) REFERENCES `sinh_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Nhóm sinh viên';

-- 10. thanh_vien_nhom: Thành viên chính thức
CREATE TABLE `thanh_vien_nhom` (
  `nhom_id` INT UNSIGNED NOT NULL COMMENT 'Nhóm.',
  `sinh_vien_id` INT UNSIGNED NOT NULL COMMENT 'Sinh viên.',
  `dot_id` INT UNSIGNED NOT NULL COMMENT 'Đợt, nhất quán với nhóm qua FK kép.',
  `la_nhom_truong` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 nếu là nhóm trưởng.',
  `tham_gia_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm trở thành thành viên.',
  `co_truong_nhom` TINYINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN la_nhom_truong = 1 THEN 1 ELSE NULL END) VIRTUAL COMMENT 'Cột kỹ thuật bảo vệ tối đa một trưởng nhóm.',
  PRIMARY KEY (`nhom_id`, `sinh_vien_id`),
  UNIQUE KEY `uq_thanh_vien_nhom_sv_dot` (`sinh_vien_id`, `dot_id`),
  UNIQUE KEY `uq_thanh_vien_nhom_truong` (`nhom_id`, `co_truong_nhom`),
  KEY `ix_thanh_vien_nhom_fk_nhom_dot` (`nhom_id`, `dot_id`),
  CONSTRAINT `ck_thanh_vien_nhom_la_nhom_truong` CHECK (la_nhom_truong IN (0,1)),
  CONSTRAINT `fk_thanh_vien_nhom_nhom_dot` FOREIGN KEY (`nhom_id`, `dot_id`) REFERENCES `nhom` (`id`, `dot_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_thanh_vien_nhom_sv` FOREIGN KEY (`sinh_vien_id`) REFERENCES `sinh_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Thành viên chính thức';

-- 11. loi_moi_nhom: Lời mời vào nhóm
CREATE TABLE `loi_moi_nhom` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `nhom_id` INT UNSIGNED NOT NULL COMMENT 'Nhóm mời.',
  `sinh_vien_duoc_moi_id` INT UNSIGNED NOT NULL COMMENT 'SV nhận lời mời.',
  `nguoi_moi_id` INT UNSIGNED NOT NULL COMMENT 'SV đang là trưởng nhóm gửi lời mời.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'CHO_PHAN_HOI' COMMENT 'Giá trị: CHO_PHAN_HOI, DA_CHAP_NHAN, TU_CHOI, DA_THU_HOI, KHONG_CON_HIEU_LUC.',
  `loi_nhan` VARCHAR(500) NULL DEFAULT NULL COMMENT 'Lời nhắn.',
  `gui_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm gửi.',
  `xu_ly_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm kết thúc lời mời.',
  `ly_do_ket_thuc` VARCHAR(500) NULL DEFAULT NULL COMMENT 'Lý do kết thúc.',
  `co_dang_cho` TINYINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN trang_thai = 'CHO_PHAN_HOI' THEN 1 ELSE NULL END) VIRTUAL COMMENT 'Cờ bảo vệ không có hai lời mời chờ giống nhau.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_loi_moi_nhom_cho` (`nhom_id`, `sinh_vien_duoc_moi_id`, `co_dang_cho`),
  KEY `ix_loi_moi_nhom_sv_tt` (`sinh_vien_duoc_moi_id`, `trang_thai`, `gui_luc`),
  KEY `ix_loi_moi_nhom_fk_nguoi_moi_id` (`nguoi_moi_id`),
  CONSTRAINT `ck_loi_moi_nhom_trang_thai` CHECK (trang_thai IN ('CHO_PHAN_HOI', 'DA_CHAP_NHAN', 'TU_CHOI', 'DA_THU_HOI', 'KHONG_CON_HIEU_LUC')),
  CONSTRAINT `ck_loi_moi_nhom_khac_nguoi` CHECK (nguoi_moi_id <> sinh_vien_duoc_moi_id),
  CONSTRAINT `ck_loi_moi_nhom_xu_ly` CHECK (((trang_thai = 'CHO_PHAN_HOI' AND xu_ly_luc IS NULL) OR (trang_thai <> 'CHO_PHAN_HOI' AND xu_ly_luc IS NOT NULL))),
  CONSTRAINT `fk_loi_moi_nhom_nhom_id` FOREIGN KEY (`nhom_id`) REFERENCES `nhom` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_loi_moi_nhom_sinh_vien_duoc_moi_id` FOREIGN KEY (`sinh_vien_duoc_moi_id`) REFERENCES `sinh_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_loi_moi_nhom_nguoi_moi_id` FOREIGN KEY (`nguoi_moi_id`) REFERENCES `sinh_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Lời mời vào nhóm';

-- 12. dang_ky: Đăng ký hiện hành và giữ chỗ
CREATE TABLE `dang_ky` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `nhom_id` INT UNSIGNED NOT NULL COMMENT 'Nhóm đăng ký, chỉ một dòng hiện hành.',
  `de_tai_id` INT UNSIGNED NOT NULL COMMENT 'Đề tài được chọn.',
  `dot_id` INT UNSIGNED NOT NULL COMMENT 'Đợt chung của nhóm và đề tài.',
  `nguoi_dang_ky_id` INT UNSIGNED NOT NULL COMMENT 'Nhóm trưởng thực hiện đăng ký.',
  `dang_ky_luc` DATETIME(6) NOT NULL COMMENT 'Thời điểm gửi do máy chủ ghi.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'CHO_DUYET' COMMENT 'Giá trị: CHO_DUYET, CHAP_THUAN, TU_CHOI, HUY.',
  `nguoi_duyet_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Tài khoản duyệt.',
  `duyet_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm duyệt.',
  `ly_do` TEXT NULL DEFAULT NULL COMMENT 'Lý do từ chối/hủy.',
  `co_giu_cho` TINYINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN trang_thai IN ('CHO_DUYET','CHAP_THUAN') THEN 1 ELSE NULL END) VIRTUAL COMMENT 'Cờ giữ chỗ: 1 hoặc NULL.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dang_ky_nhom` (`nhom_id`),
  UNIQUE KEY `uq_dang_ky_cho_de_tai` (`de_tai_id`, `co_giu_cho`),
  UNIQUE KEY `uq_dang_ky_id_dot` (`id`, `dot_id`),
  KEY `ix_dang_ky_dot_tt` (`dot_id`, `trang_thai`, `dang_ky_luc`),
  KEY `ix_dang_ky_fk_nhom_dot` (`nhom_id`, `dot_id`),
  KEY `ix_dang_ky_fk_dt_dot` (`de_tai_id`, `dot_id`),
  KEY `ix_dang_ky_fk_nguoi_dk` (`nguoi_dang_ky_id`),
  KEY `ix_dang_ky_fk_nguoi_duyet` (`nguoi_duyet_id`),
  CONSTRAINT `ck_dang_ky_trang_thai` CHECK (trang_thai IN ('CHO_DUYET', 'CHAP_THUAN', 'TU_CHOI', 'HUY')),
  CONSTRAINT `ck_dang_ky_nguoi_duyet_id_pair` CHECK (((nguoi_duyet_id IS NULL AND duyet_luc IS NULL) OR (nguoi_duyet_id IS NOT NULL AND duyet_luc IS NOT NULL))),
  CONSTRAINT `ck_dang_ky_duyet_du` CHECK (trang_thai NOT IN ('CHAP_THUAN','TU_CHOI') OR nguoi_duyet_id IS NOT NULL),
  CONSTRAINT `fk_dang_ky_nhom_dot` FOREIGN KEY (`nhom_id`, `dot_id`) REFERENCES `nhom` (`id`, `dot_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_dang_ky_dt_dot` FOREIGN KEY (`de_tai_id`, `dot_id`) REFERENCES `de_tai` (`id`, `dot_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_dang_ky_nguoi_dk` FOREIGN KEY (`nguoi_dang_ky_id`) REFERENCES `sinh_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_dang_ky_nguoi_duyet` FOREIGN KEY (`nguoi_duyet_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Đăng ký hiện hành và giữ chỗ';

-- 13. bao_cao: Phiên bản bộ báo cáo
CREATE TABLE `bao_cao` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `dang_ky_id` INT UNSIGNED NOT NULL COMMENT 'Đăng ký được chấp thuận.',
  `phien_ban` INT UNSIGNED NOT NULL COMMENT 'Số phiên bản >0.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'NHAP' COMMENT 'Giá trị: NHAP, DA_NOP, HUY.',
  `ghi_chu` TEXT NULL DEFAULT NULL COMMENT 'Ghi chú.',
  `nguoi_tao_id` INT UNSIGNED NOT NULL COMMENT 'SV tạo bản nháp.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  `nguoi_nop_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Nhóm trưởng nộp chính thức.',
  `nop_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm nộp chính thức.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bao_cao_version` (`dang_ky_id`, `phien_ban`),
  UNIQUE KEY `uq_bao_cao_id_dk` (`id`, `dang_ky_id`),
  KEY `ix_bao_cao_ban_cuoi` (`dang_ky_id`, `trang_thai`, `nop_luc`, `id`),
  KEY `ix_bao_cao_fk_nguoi_tao` (`nguoi_tao_id`),
  KEY `ix_bao_cao_fk_nguoi_nop` (`nguoi_nop_id`),
  CONSTRAINT `ck_bao_cao_version` CHECK (phien_ban > 0),
  CONSTRAINT `ck_bao_cao_trang_thai` CHECK (trang_thai IN ('NHAP', 'DA_NOP', 'HUY')),
  CONSTRAINT `ck_bao_cao_nop_du` CHECK (((trang_thai = 'DA_NOP' AND nguoi_nop_id IS NOT NULL AND nop_luc IS NOT NULL) OR (trang_thai IN ('NHAP','HUY') AND nguoi_nop_id IS NULL AND nop_luc IS NULL))),
  CONSTRAINT `fk_bao_cao_dk` FOREIGN KEY (`dang_ky_id`) REFERENCES `dang_ky` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_bao_cao_nguoi_tao` FOREIGN KEY (`nguoi_tao_id`) REFERENCES `sinh_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_bao_cao_nguoi_nop` FOREIGN KEY (`nguoi_nop_id`) REFERENCES `sinh_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Phiên bản bộ báo cáo';

-- 14. bao_cao_tep: Ba tài liệu của một bộ báo cáo
CREATE TABLE `bao_cao_tep` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `bao_cao_id` INT UNSIGNED NOT NULL COMMENT 'Bộ báo cáo.',
  `loai_tai_lieu` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Giá trị: BAO_CAO_PDF, SLIDE, MA_NGUON.',
  `ten_tep_goc` VARCHAR(255) NOT NULL COMMENT 'Tên tệp người dùng tải lên.',
  `khoa_luu_tru` VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Khóa nội bộ, không phải URL công khai.',
  `loai_mime` VARCHAR(127) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Loại nội dung đã được kiểm tra.',
  `kich_thuoc_byte` BIGINT UNSIGNED NOT NULL COMMENT 'Kích thước tệp >0.',
  `ma_kiem_tra` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL DEFAULT NULL COMMENT 'SHA-256 hex nếu dùng.',
  `nguoi_tai_len_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản upload.',
  `tai_len_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tải.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bao_cao_tep_loai` (`bao_cao_id`, `loai_tai_lieu`),
  UNIQUE KEY `uq_bao_cao_tep_storage` (`khoa_luu_tru`),
  KEY `ix_bao_cao_tep_fk_nguoi_tai` (`nguoi_tai_len_id`),
  CONSTRAINT `ck_bao_cao_tep_loai_tai_lieu` CHECK (loai_tai_lieu IN ('BAO_CAO_PDF', 'SLIDE', 'MA_NGUON')),
  CONSTRAINT `ck_bao_cao_tep_size` CHECK (kich_thuoc_byte > 0),
  CONSTRAINT `fk_bao_cao_tep_bao_cao` FOREIGN KEY (`bao_cao_id`) REFERENCES `bao_cao` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_bao_cao_tep_nguoi_tai` FOREIGN KEY (`nguoi_tai_len_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Ba tài liệu của một bộ báo cáo';

-- 15. hoi_dong: Hội đồng theo đợt
CREATE TABLE `hoi_dong` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `dot_id` INT UNSIGNED NOT NULL COMMENT 'Đợt.',
  `ma_hoi_dong` VARCHAR(30) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Mã hội đồng trong đợt.',
  `ten_hoi_dong` VARCHAR(200) NOT NULL COMMENT 'Tên hội đồng.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'NHAP' COMMENT 'Giá trị: NHAP, DA_CHOT, HOAN_THANH.',
  `nguoi_tao_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản tạo.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_hoi_dong_dot_ma` (`dot_id`, `ma_hoi_dong`),
  UNIQUE KEY `uq_hoi_dong_id_dot` (`id`, `dot_id`),
  KEY `ix_hoi_dong_dot_tt` (`dot_id`, `trang_thai`),
  KEY `ix_hoi_dong_fk_nguoi_tao` (`nguoi_tao_id`),
  CONSTRAINT `ck_hoi_dong_trang_thai` CHECK (trang_thai IN ('NHAP', 'DA_CHOT', 'HOAN_THANH')),
  CONSTRAINT `fk_hoi_dong_dot` FOREIGN KEY (`dot_id`) REFERENCES `dot_dang_ky` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_hoi_dong_nguoi_tao` FOREIGN KEY (`nguoi_tao_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Hội đồng theo đợt';

-- 16. thanh_vien_hoi_dong: Thành phần hội đồng
CREATE TABLE `thanh_vien_hoi_dong` (
  `hoi_dong_id` INT UNSIGNED NOT NULL COMMENT 'Hội đồng.',
  `giang_vien_id` INT UNSIGNED NOT NULL COMMENT 'Thành viên GV.',
  `vai_tro` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Giá trị: CHU_TICH, THU_KY, UY_VIEN.',
  `chuc_danh_doc_quyen` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin GENERATED ALWAYS AS (CASE WHEN vai_tro IN ('CHU_TICH','THU_KY') THEN vai_tro ELSE NULL END) VIRTUAL COMMENT 'Bảo vệ tối đa một chủ tịch và một thư ký.',
  PRIMARY KEY (`hoi_dong_id`, `giang_vien_id`),
  UNIQUE KEY `uq_thanh_vien_hoi_dong_chuc_danh` (`hoi_dong_id`, `chuc_danh_doc_quyen`),
  KEY `ix_thanh_vien_hoi_dong_gv_hd` (`giang_vien_id`, `hoi_dong_id`),
  CONSTRAINT `ck_thanh_vien_hoi_dong_vai_tro` CHECK (vai_tro IN ('CHU_TICH', 'THU_KY', 'UY_VIEN')),
  CONSTRAINT `fk_thanh_vien_hoi_dong_hd` FOREIGN KEY (`hoi_dong_id`) REFERENCES `hoi_dong` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_thanh_vien_hoi_dong_gv` FOREIGN KEY (`giang_vien_id`) REFERENCES `giang_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Thành phần hội đồng';

-- 17. danh_gia: Một lần đánh giá, gộp phản biện và hội đồng
CREATE TABLE `danh_gia` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `dang_ky_id` INT UNSIGNED NOT NULL COMMENT 'Một đăng ký có một lần đánh giá.',
  `dot_id` INT UNSIGNED NOT NULL COMMENT 'Đợt chung.',
  `bao_cao_chot_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Bộ báo cáo chính thức.',
  `hoi_dong_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Hội đồng nếu áp dụng.',
  `trang_thai_phan_bien` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'CHUA_BAT_DAU' COMMENT 'Giá trị: CHUA_BAT_DAU, DANG_CHAM, HOAN_TAT.',
  `lich_phan_bien_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Lịch phản biện nếu tổ chức buổi riêng.',
  `dia_diem_phan_bien` VARCHAR(255) NULL DEFAULT NULL COMMENT 'Địa điểm phản biện nếu có.',
  `phan_bien_hoan_tat_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Hoàn tất phản biện.',
  `trang_thai_hoi_dong` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'KHONG_AP_DUNG' COMMENT 'Giá trị: KHONG_AP_DUNG, CHUA_BAT_DAU, DANG_CHAM, HOAN_TAT.',
  `hoi_dong_bat_dau` DATETIME(6) NULL DEFAULT NULL COMMENT 'Giờ bắt đầu hội đồng.',
  `hoi_dong_ket_thuc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Giờ kết thúc hội đồng.',
  `dia_diem_hoi_dong` VARCHAR(255) NULL DEFAULT NULL COMMENT 'Địa điểm hội đồng.',
  `hoi_dong_hoan_tat_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Hoàn tất hội đồng.',
  `nguoi_tao_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản lập đánh giá.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_danh_gia_dk` (`dang_ky_id`),
  UNIQUE KEY `uq_danh_gia_id_dot` (`id`, `dot_id`),
  KEY `ix_danh_gia_hd_lich` (`hoi_dong_id`, `hoi_dong_bat_dau`),
  KEY `ix_danh_gia_dot_pb` (`dot_id`, `trang_thai_phan_bien`),
  KEY `ix_danh_gia_fk_dk_dot` (`dang_ky_id`, `dot_id`),
  KEY `ix_danh_gia_fk_bao_cao_dk` (`bao_cao_chot_id`, `dang_ky_id`),
  KEY `ix_danh_gia_fk_hd_dot` (`hoi_dong_id`, `dot_id`),
  KEY `ix_danh_gia_fk_nguoi_tao` (`nguoi_tao_id`),
  CONSTRAINT `ck_danh_gia_trang_thai_phan_bien` CHECK (trang_thai_phan_bien IN ('CHUA_BAT_DAU', 'DANG_CHAM', 'HOAN_TAT')),
  CONSTRAINT `ck_danh_gia_trang_thai_hoi_dong` CHECK (trang_thai_hoi_dong IN ('KHONG_AP_DUNG', 'CHUA_BAT_DAU', 'DANG_CHAM', 'HOAN_TAT')),
  CONSTRAINT `ck_danh_gia_pb_xong` CHECK (((trang_thai_phan_bien = 'HOAN_TAT' AND phan_bien_hoan_tat_luc IS NOT NULL) OR (trang_thai_phan_bien <> 'HOAN_TAT' AND phan_bien_hoan_tat_luc IS NULL))),
  CONSTRAINT `ck_danh_gia_hd_xong` CHECK (((trang_thai_hoi_dong = 'HOAN_TAT' AND hoi_dong_hoan_tat_luc IS NOT NULL) OR (trang_thai_hoi_dong <> 'HOAN_TAT' AND hoi_dong_hoan_tat_luc IS NULL))),
  CONSTRAINT `ck_danh_gia_pb_truoc_hd` CHECK (trang_thai_hoi_dong NOT IN ('DANG_CHAM','HOAN_TAT') OR trang_thai_phan_bien = 'HOAN_TAT'),
  CONSTRAINT `ck_danh_gia_bao_cao_khi_cham` CHECK (trang_thai_phan_bien = 'CHUA_BAT_DAU' OR bao_cao_chot_id IS NOT NULL),
  CONSTRAINT `ck_danh_gia_lich_hd` CHECK (((hoi_dong_bat_dau IS NULL AND hoi_dong_ket_thuc IS NULL) OR (hoi_dong_bat_dau IS NOT NULL AND hoi_dong_ket_thuc IS NOT NULL AND hoi_dong_bat_dau < hoi_dong_ket_thuc))),
  CONSTRAINT `ck_danh_gia_khong_hd` CHECK (trang_thai_hoi_dong <> 'KHONG_AP_DUNG' OR (hoi_dong_id IS NULL AND hoi_dong_bat_dau IS NULL AND hoi_dong_ket_thuc IS NULL AND dia_diem_hoi_dong IS NULL)),
  CONSTRAINT `ck_danh_gia_du_hd` CHECK (trang_thai_hoi_dong NOT IN ('DANG_CHAM','HOAN_TAT') OR (hoi_dong_id IS NOT NULL AND hoi_dong_bat_dau IS NOT NULL AND hoi_dong_ket_thuc IS NOT NULL AND dia_diem_hoi_dong IS NOT NULL)),
  CONSTRAINT `fk_danh_gia_dk_dot` FOREIGN KEY (`dang_ky_id`, `dot_id`) REFERENCES `dang_ky` (`id`, `dot_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_danh_gia_bao_cao_dk` FOREIGN KEY (`bao_cao_chot_id`, `dang_ky_id`) REFERENCES `bao_cao` (`id`, `dang_ky_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_danh_gia_hd_dot` FOREIGN KEY (`hoi_dong_id`, `dot_id`) REFERENCES `hoi_dong` (`id`, `dot_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_danh_gia_nguoi_tao` FOREIGN KEY (`nguoi_tao_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Một lần đánh giá, gộp phản biện và hội đồng';

-- 18. phan_cong_cham: Mỗi GV một phân công trong lần đánh giá
CREATE TABLE `phan_cong_cham` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `danh_gia_id` INT UNSIGNED NOT NULL COMMENT 'Lần đánh giá.',
  `giang_vien_id` INT UNSIGNED NOT NULL COMMENT 'GV chấm.',
  `la_phan_bien` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 nếu là GVPB.',
  `la_thanh_vien_hoi_dong` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '1 nếu chấm với tư cách thành viên hội đồng.',
  `nguoi_phan_cong_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản phân công.',
  `phan_cong_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm phân công.',
  `co_phan_bien` TINYINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN la_phan_bien = 1 THEN 1 ELSE NULL END) VIRTUAL COMMENT 'Bảo vệ tối đa một GVPB/lần đánh giá.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_phan_cong_cham_dg_gv` (`danh_gia_id`, `giang_vien_id`),
  UNIQUE KEY `uq_phan_cong_cham_id_dg` (`id`, `danh_gia_id`),
  UNIQUE KEY `uq_phan_cong_cham_mot_pb` (`danh_gia_id`, `co_phan_bien`),
  KEY `ix_phan_cong_cham_gv_dg` (`giang_vien_id`, `danh_gia_id`),
  KEY `ix_phan_cong_cham_fk_nguoi_pc` (`nguoi_phan_cong_id`),
  CONSTRAINT `ck_phan_cong_cham_la_phan_bien` CHECK (la_phan_bien IN (0,1)),
  CONSTRAINT `ck_phan_cong_cham_la_thanh_vien_hoi_dong` CHECK (la_thanh_vien_hoi_dong IN (0,1)),
  CONSTRAINT `ck_phan_cong_cham_it_nhat_mot` CHECK (la_phan_bien = 1 OR la_thanh_vien_hoi_dong = 1),
  CONSTRAINT `fk_phan_cong_cham_dg` FOREIGN KEY (`danh_gia_id`) REFERENCES `danh_gia` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_phan_cong_cham_gv` FOREIGN KEY (`giang_vien_id`) REFERENCES `giang_vien` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_phan_cong_cham_nguoi_pc` FOREIGN KEY (`nguoi_phan_cong_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Mỗi GV một phân công trong lần đánh giá';

-- 19. phieu_cham: Phiên bản điểm và nhận xét
CREATE TABLE `phieu_cham` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `phan_cong_cham_id` INT UNSIGNED NOT NULL COMMENT 'Phân công sở hữu điểm.',
  `phien_ban` INT UNSIGNED NOT NULL COMMENT 'Số phiên bản >0.',
  `diem` DECIMAL(4,2) NULL DEFAULT NULL COMMENT 'Điểm 0–10, có thể trống khi nháp.',
  `nhan_xet` TEXT NULL DEFAULT NULL COMMENT 'Nhận xét; bắt buộc khi nộp.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'NHAP' COMMENT 'Giá trị: NHAP, DA_NOP, HUY.',
  `yeu_cau_sua_diem_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Yêu cầu được duyệt dẫn tới phiên bản điều chỉnh.',
  `nguoi_tao_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản GV tạo phiếu.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  `nop_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm nộp.',
  `co_ban_nhap` TINYINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN trang_thai = 'NHAP' THEN 1 ELSE NULL END) VIRTUAL COMMENT 'Tối đa một bản nháp/phân công.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_phieu_cham_version` (`phan_cong_cham_id`, `phien_ban`),
  UNIQUE KEY `uq_phieu_cham_id_pc` (`id`, `phan_cong_cham_id`),
  UNIQUE KEY `uq_phieu_cham_request` (`yeu_cau_sua_diem_id`),
  UNIQUE KEY `uq_phieu_cham_nhap` (`phan_cong_cham_id`, `co_ban_nhap`),
  KEY `ix_phieu_cham_pc_tt_ver` (`phan_cong_cham_id`, `trang_thai`, `phien_ban`),
  KEY `ix_phieu_cham_fk_request_pc` (`yeu_cau_sua_diem_id`, `phan_cong_cham_id`),
  KEY `ix_phieu_cham_fk_nguoi_tao` (`nguoi_tao_id`),
  CONSTRAINT `ck_phieu_cham_version` CHECK (phien_ban > 0),
  CONSTRAINT `ck_phieu_cham_diem` CHECK (diem IS NULL OR (diem >= 0 AND diem <= 10)),
  CONSTRAINT `ck_phieu_cham_trang_thai` CHECK (trang_thai IN ('NHAP', 'DA_NOP', 'HUY')),
  CONSTRAINT `ck_phieu_cham_nop_du` CHECK (((trang_thai = 'DA_NOP' AND diem IS NOT NULL AND nhan_xet IS NOT NULL AND CHAR_LENGTH(TRIM(nhan_xet)) > 0 AND nop_luc IS NOT NULL) OR (trang_thai IN ('NHAP','HUY') AND nop_luc IS NULL))),
  CONSTRAINT `fk_phieu_cham_pc` FOREIGN KEY (`phan_cong_cham_id`) REFERENCES `phan_cong_cham` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_phieu_cham_nguoi_tao` FOREIGN KEY (`nguoi_tao_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Phiên bản điểm và nhận xét';

-- 20. yeu_cau_sua_diem: Xin và duyệt điều chỉnh điểm
CREATE TABLE `yeu_cau_sua_diem` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `phan_cong_cham_id` INT UNSIGNED NOT NULL COMMENT 'Phân công cần sửa.',
  `phieu_cham_goc_id` INT UNSIGNED NOT NULL COMMENT 'Phiếu gốc đã nộp.',
  `nguoi_yeu_cau_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản GV sở hữu phân công.',
  `ly_do` TEXT NOT NULL COMMENT 'Lý do bắt buộc.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'CHO_DUYET' COMMENT 'Giá trị: CHO_DUYET, CHAP_THUAN, DA_NOP_DIEU_CHINH, HOAN_TAT, TU_CHOI, HUY.',
  `yeu_cau_luc` DATETIME(6) NOT NULL COMMENT 'Thời điểm gửi yêu cầu.',
  `nguoi_duyet_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Trưởng khoa/cán bộ khoa duyệt.',
  `duyet_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm duyệt.',
  `nhan_xet_duyet` TEXT NULL DEFAULT NULL COMMENT 'Nhận xét duyệt.',
  `hoan_tat_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Kết quả sử dụng phiếu mới đã công bố.',
  `co_dang_xu_ly` TINYINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN trang_thai IN ('CHO_DUYET','CHAP_THUAN','DA_NOP_DIEU_CHINH') THEN 1 ELSE NULL END) VIRTUAL COMMENT 'Tối đa một yêu cầu đang xử lý/phân công.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_yeu_cau_sua_diem_id_pc` (`id`, `phan_cong_cham_id`),
  UNIQUE KEY `uq_yeu_cau_sua_diem_dang_xu_ly` (`phan_cong_cham_id`, `co_dang_xu_ly`),
  KEY `ix_yeu_cau_sua_diem_tt_luc` (`trang_thai`, `yeu_cau_luc`),
  KEY `ix_yeu_cau_sua_diem_fk_goc_pc` (`phieu_cham_goc_id`, `phan_cong_cham_id`),
  KEY `ix_yeu_cau_sua_diem_fk_nguoi_yc` (`nguoi_yeu_cau_id`),
  KEY `ix_yeu_cau_sua_diem_fk_nguoi_duyet` (`nguoi_duyet_id`),
  CONSTRAINT `ck_yeu_cau_sua_diem_trang_thai` CHECK (trang_thai IN ('CHO_DUYET', 'CHAP_THUAN', 'DA_NOP_DIEU_CHINH', 'HOAN_TAT', 'TU_CHOI', 'HUY')),
  CONSTRAINT `ck_yeu_cau_sua_diem_nguoi_duyet_id_pair` CHECK (((nguoi_duyet_id IS NULL AND duyet_luc IS NULL) OR (nguoi_duyet_id IS NOT NULL AND duyet_luc IS NOT NULL))),
  CONSTRAINT `ck_yeu_cau_sua_diem_khong_tu_duyet` CHECK (nguoi_duyet_id IS NULL OR nguoi_duyet_id <> nguoi_yeu_cau_id),
  CONSTRAINT `ck_yeu_cau_sua_diem_ly_do` CHECK (CHAR_LENGTH(TRIM(ly_do)) > 0),
  CONSTRAINT `ck_yeu_cau_sua_diem_duyet_du` CHECK (trang_thai NOT IN ('CHAP_THUAN','DA_NOP_DIEU_CHINH','HOAN_TAT','TU_CHOI') OR nguoi_duyet_id IS NOT NULL),
  CONSTRAINT `ck_yeu_cau_sua_diem_hoan_tat` CHECK (((trang_thai = 'HOAN_TAT' AND hoan_tat_luc IS NOT NULL) OR (trang_thai <> 'HOAN_TAT' AND hoan_tat_luc IS NULL))),
  CONSTRAINT `fk_yeu_cau_sua_diem_pc` FOREIGN KEY (`phan_cong_cham_id`) REFERENCES `phan_cong_cham` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_yeu_cau_sua_diem_goc_pc` FOREIGN KEY (`phieu_cham_goc_id`, `phan_cong_cham_id`) REFERENCES `phieu_cham` (`id`, `phan_cong_cham_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_yeu_cau_sua_diem_nguoi_yc` FOREIGN KEY (`nguoi_yeu_cau_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_yeu_cau_sua_diem_nguoi_duyet` FOREIGN KEY (`nguoi_duyet_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Xin và duyệt điều chỉnh điểm';

-- 21. ket_qua: Phiên bản kết quả tổng hợp/công bố
CREATE TABLE `ket_qua` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `danh_gia_id` INT UNSIGNED NOT NULL COMMENT 'Lần đánh giá.',
  `phien_ban` INT UNSIGNED NOT NULL COMMENT 'Số phiên bản >0.',
  `so_nguoi_cham` TINYINT UNSIGNED NOT NULL COMMENT 'Số người khác nhau dùng để tính điểm, 1–6.',
  `tong_diem` DECIMAL(6,2) NOT NULL COMMENT 'Tổng điểm từ chi tiết snapshot.',
  `diem_cuoi` DECIMAL(3,1) GENERATED ALWAYS AS (ROUND(tong_diem / NULLIF(so_nguoi_cham, 0), 1)) STORED COMMENT 'Trung bình làm tròn 1 chữ số; DB tự tính.',
  `dat` TINYINT UNSIGNED GENERATED ALWAYS AS (diem_cuoi >= 4.0) STORED COMMENT 'DB tự xét điểm đã làm tròn >=4.0.',
  `nhan_xet_tong_hop` TEXT NOT NULL COMMENT 'Nhận xét tổng hợp.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'CHO_XAC_NHAN' COMMENT 'Giá trị: CHO_XAC_NHAN, DA_XAC_NHAN, DA_CONG_BO, HUY.',
  `nguoi_tong_hop_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản tổng hợp.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  `nguoi_xac_nhan_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Tài khoản xác nhận.',
  `xac_nhan_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm xác nhận.',
  `nguoi_cong_bo_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Tài khoản khoa công bố.',
  `cong_bo_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm công bố.',
  `co_dang_xu_ly` TINYINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN trang_thai IN ('CHO_XAC_NHAN','DA_XAC_NHAN') THEN 1 ELSE NULL END) VIRTUAL COMMENT 'Tối đa một snapshot đang chuẩn bị/xác nhận.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ket_qua_version` (`danh_gia_id`, `phien_ban`),
  UNIQUE KEY `uq_ket_qua_id_dg` (`id`, `danh_gia_id`),
  UNIQUE KEY `uq_ket_qua_dang_xu_ly` (`danh_gia_id`, `co_dang_xu_ly`),
  KEY `ix_ket_qua_dg_tt_ver` (`danh_gia_id`, `trang_thai`, `phien_ban`),
  KEY `ix_ket_qua_fk_nguoi_tong_hop_id` (`nguoi_tong_hop_id`),
  KEY `ix_ket_qua_fk_nguoi_xac_nhan_id` (`nguoi_xac_nhan_id`),
  KEY `ix_ket_qua_fk_nguoi_cong_bo_id` (`nguoi_cong_bo_id`),
  CONSTRAINT `ck_ket_qua_version` CHECK (phien_ban > 0),
  CONSTRAINT `ck_ket_qua_so_nguoi` CHECK (so_nguoi_cham BETWEEN 1 AND 6),
  CONSTRAINT `ck_ket_qua_tong` CHECK (tong_diem >= 0 AND tong_diem <= 10 * so_nguoi_cham),
  CONSTRAINT `ck_ket_qua_trang_thai` CHECK (trang_thai IN ('CHO_XAC_NHAN', 'DA_XAC_NHAN', 'DA_CONG_BO', 'HUY')),
  CONSTRAINT `ck_ket_qua_nguoi_xac_nhan_id_pair` CHECK (((nguoi_xac_nhan_id IS NULL AND xac_nhan_luc IS NULL) OR (nguoi_xac_nhan_id IS NOT NULL AND xac_nhan_luc IS NOT NULL))),
  CONSTRAINT `ck_ket_qua_nguoi_cong_bo_id_pair` CHECK (((nguoi_cong_bo_id IS NULL AND cong_bo_luc IS NULL) OR (nguoi_cong_bo_id IS NOT NULL AND cong_bo_luc IS NOT NULL))),
  CONSTRAINT `ck_ket_qua_xac_nhan_du` CHECK (trang_thai NOT IN ('DA_XAC_NHAN','DA_CONG_BO') OR nguoi_xac_nhan_id IS NOT NULL),
  CONSTRAINT `ck_ket_qua_cong_bo_du` CHECK (((trang_thai = 'DA_CONG_BO' AND nguoi_cong_bo_id IS NOT NULL) OR (trang_thai <> 'DA_CONG_BO' AND nguoi_cong_bo_id IS NULL))),
  CONSTRAINT `fk_ket_qua_dg` FOREIGN KEY (`danh_gia_id`) REFERENCES `danh_gia` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ket_qua_nguoi_tong_hop_id` FOREIGN KEY (`nguoi_tong_hop_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ket_qua_nguoi_xac_nhan_id` FOREIGN KEY (`nguoi_xac_nhan_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ket_qua_nguoi_cong_bo_id` FOREIGN KEY (`nguoi_cong_bo_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Phiên bản kết quả tổng hợp/công bố';

-- 22. chi_tiet_ket_qua: Phiếu nguồn của từng snapshot
CREATE TABLE `chi_tiet_ket_qua` (
  `ket_qua_id` INT UNSIGNED NOT NULL COMMENT 'Snapshot kết quả.',
  `phan_cong_cham_id` INT UNSIGNED NOT NULL COMMENT 'Người chấm thông qua phân công.',
  `danh_gia_id` INT UNSIGNED NOT NULL COMMENT 'Lần đánh giá chung.',
  `phieu_cham_id` INT UNSIGNED NOT NULL COMMENT 'Chính xác phiên bản phiếu được dùng.',
  PRIMARY KEY (`ket_qua_id`, `phan_cong_cham_id`),
  KEY `ix_chi_tiet_ket_qua_fk_kq_dg` (`ket_qua_id`, `danh_gia_id`),
  KEY `ix_chi_tiet_ket_qua_fk_pc_dg` (`phan_cong_cham_id`, `danh_gia_id`),
  KEY `ix_chi_tiet_ket_qua_fk_phieu_pc` (`phieu_cham_id`, `phan_cong_cham_id`),
  CONSTRAINT `fk_chi_tiet_ket_qua_kq_dg` FOREIGN KEY (`ket_qua_id`, `danh_gia_id`) REFERENCES `ket_qua` (`id`, `danh_gia_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chi_tiet_ket_qua_pc_dg` FOREIGN KEY (`phan_cong_cham_id`, `danh_gia_id`) REFERENCES `phan_cong_cham` (`id`, `danh_gia_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chi_tiet_ket_qua_phieu_pc` FOREIGN KEY (`phieu_cham_id`, `phan_cong_cham_id`) REFERENCES `phieu_cham` (`id`, `phan_cong_cham_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Phiếu nguồn của từng snapshot';

-- 23. thong_bao: Thông báo cho người đăng nhập
CREATE TABLE `thong_bao` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `tieu_de` VARCHAR(255) NOT NULL COMMENT 'Tiêu đề.',
  `noi_dung` TEXT NOT NULL COMMENT 'Nội dung.',
  `don_vi_ban_hanh` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Giá trị: NHA_TRUONG, KHOA_CNTT.',
  `nguoi_dang_id` INT UNSIGNED NOT NULL COMMENT 'Admin/trưởng khoa/cán bộ khoa đăng.',
  `trang_thai` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'NHAP' COMMENT 'Giá trị: NHAP, DA_CONG_BO, THU_HOI.',
  `tao_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tạo.',
  `cong_bo_luc` DATETIME(6) NULL DEFAULT NULL COMMENT 'Thời điểm đã công bố.',
  PRIMARY KEY (`id`),
  KEY `ix_thong_bao_tt_luc` (`trang_thai`, `cong_bo_luc`),
  KEY `ix_thong_bao_fk_nguoi_dang` (`nguoi_dang_id`),
  CONSTRAINT `ck_thong_bao_don_vi_ban_hanh` CHECK (don_vi_ban_hanh IN ('NHA_TRUONG', 'KHOA_CNTT')),
  CONSTRAINT `ck_thong_bao_trang_thai` CHECK (trang_thai IN ('NHAP', 'DA_CONG_BO', 'THU_HOI')),
  CONSTRAINT `ck_thong_bao_cong_bo` CHECK (((trang_thai = 'NHAP' AND cong_bo_luc IS NULL) OR (trang_thai IN ('DA_CONG_BO','THU_HOI') AND cong_bo_luc IS NOT NULL))),
  CONSTRAINT `fk_thong_bao_nguoi_dang` FOREIGN KEY (`nguoi_dang_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Thông báo cho người đăng nhập';

-- 24. thong_bao_tep: Tệp của thông báo
CREATE TABLE `thong_bao_tep` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Khóa chính tự tăng.',
  `thong_bao_id` INT UNSIGNED NOT NULL COMMENT 'Thông báo.',
  `ten_tep_goc` VARCHAR(255) NOT NULL COMMENT 'Tên tệp gốc.',
  `khoa_luu_tru` VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Khóa nội bộ, namespace riêng so với báo cáo.',
  `loai_mime` VARCHAR(127) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'Loại nội dung tệp.',
  `kich_thuoc_byte` BIGINT UNSIGNED NOT NULL COMMENT 'Kích thước >0.',
  `nguoi_tai_len_id` INT UNSIGNED NOT NULL COMMENT 'Tài khoản upload.',
  `tai_len_luc` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thời điểm tải.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_thong_bao_tep_storage` (`khoa_luu_tru`),
  KEY `ix_thong_bao_tep_fk_thong_bao` (`thong_bao_id`),
  KEY `ix_thong_bao_tep_fk_nguoi_tai` (`nguoi_tai_len_id`),
  CONSTRAINT `ck_thong_bao_tep_size` CHECK (kich_thuoc_byte > 0),
  CONSTRAINT `fk_thong_bao_tep_thong_bao` FOREIGN KEY (`thong_bao_id`) REFERENCES `thong_bao` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_thong_bao_tep_nguoi_tai` FOREIGN KEY (`nguoi_tai_len_id`) REFERENCES `tai_khoan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tệp của thông báo';

-- Add the deliberate circular foreign key after both tables exist.
ALTER TABLE `phieu_cham` ADD CONSTRAINT `fk_phieu_cham_request_pc` FOREIGN KEY (`yeu_cau_sua_diem_id`, `phan_cong_cham_id`) REFERENCES `yeu_cau_sua_diem` (`id`, `phan_cong_cham_id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- End: 24 application tables. No data or application users are created.
