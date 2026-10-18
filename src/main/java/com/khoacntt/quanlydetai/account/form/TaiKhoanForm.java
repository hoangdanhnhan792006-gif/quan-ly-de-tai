package com.khoacntt.quanlydetai.account.form;

import com.khoacntt.quanlydetai.account.model.VaiTro;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.HashSet;

public class TaiKhoanForm {
    @NotBlank(message = "Nhập tên đăng nhập")
    @Pattern(regexp = "[A-Za-z0-9_.-]{3,50}", message = "Tên đăng nhập gồm 3–50 chữ, số, dấu gạch hoặc dấu chấm")
    private String tenDangNhap;

    @NotBlank(message = "Nhập mật khẩu")
    @Size(min = 8, max = 72, message = "Mật khẩu gồm 8–72 ký tự")
    private String matKhau;

    @NotBlank(message = "Nhập họ tên")
    @Size(max = 150)
    private String hoTen;

    @NotBlank(message = "Nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 254)
    private String email;

    @NotEmpty(message = "Chọn ít nhất một vai trò")
    private Set<VaiTro> vaiTros = new HashSet<>();

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<VaiTro> getVaiTros() {
        return vaiTros;
    }

    public void setVaiTros(Set<VaiTro> vaiTros) {
        this.vaiTros = vaiTros;
    }

}
