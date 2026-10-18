package com.khoacntt.quanlydetai.hoso.dto;

import com.khoacntt.quanlydetai.hoso.model.SinhVien;

public record SinhVienDto(Integer id, Integer taiKhoanId, String tenDangNhap, String hoTen, String maSv, String lop, String khoaHoc) {
    public static SinhVienDto tu(SinhVien s) {
        return new SinhVienDto(s.getId(), s.getTaiKhoan().getId(), s.getTaiKhoan().getTenDangNhap(), s.getTaiKhoan().getHoTen(), s.getMaSv(), s.getLop(), s.getKhoaHoc());
    }
}
