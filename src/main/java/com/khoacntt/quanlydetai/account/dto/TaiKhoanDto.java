package com.khoacntt.quanlydetai.account.dto;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;

import java.util.Set;
public record TaiKhoanDto(Integer id, String tenDangNhap, String hoTen, String email,
                         TrangThaiTaiKhoan trangThai, Set<VaiTro> vaiTros) {
    public static TaiKhoanDto tu(TaiKhoan tk) {
        return new TaiKhoanDto(tk.getId(), tk.getTenDangNhap(), tk.getHoTen(), tk.getEmail(),
                tk.getTrangThai(), Set.copyOf(tk.getVaiTros()));
    }
}
