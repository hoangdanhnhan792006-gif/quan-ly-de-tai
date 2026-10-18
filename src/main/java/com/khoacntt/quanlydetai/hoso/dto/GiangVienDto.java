package com.khoacntt.quanlydetai.hoso.dto;

import com.khoacntt.quanlydetai.hoso.model.GiangVien;

public record GiangVienDto(Integer id, Integer taiKhoanId, String tenDangNhap, String hoTen, String maGv, Integer boMonId, String tenBoMon, String hocVi) {
    public static GiangVienDto tu(GiangVien g) {
        return new GiangVienDto(g.getId(), g.getTaiKhoan().getId(), g.getTaiKhoan().getTenDangNhap(), g.getTaiKhoan().getHoTen(), g.getMaGv(), g.getBoMon().getId(), g.getBoMon().getTenBoMon(), g.getHocVi());
    }
}
