package com.khoacntt.quanlydetai.account.form;

import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.HashSet;

public class SuaTaiKhoanForm {
    @NotBlank(message = "Nhập tên đăng nhập")
    @Pattern(regexp = "[A-Za-z0-9_.-]{3,50}", message = "Tên đăng nhập gồm 3–50 chữ, số, dấu gạch hoặc dấu chấm")
    private String tenDangNhap;

    @NotNull(message = "Chọn trạng thái tài khoản")
    private TrangThaiTaiKhoan trangThai;

    @Size(max = 72, message = "Mật khẩu mới tối đa 72 ký tự và 72 byte UTF-8")
    @Pattern(regexp = "(?:|[\\s\\S]{8,72})", message = "Để trống để giữ mật khẩu, hoặc nhập mật khẩu mới từ 8 ký tự")
    private String matKhauMoi;

    @Size(max = 72, message = "Xác nhận mật khẩu tối đa 72 ký tự")
    private String xacNhanMatKhau;

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public TrangThaiTaiKhoan getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiTaiKhoan trangThai) { this.trangThai = trangThai; }
    public String getMatKhauMoi() { return matKhauMoi; }
    public void setMatKhauMoi(String matKhauMoi) { this.matKhauMoi = matKhauMoi; }
    public String getXacNhanMatKhau() { return xacNhanMatKhau; }
    public void setXacNhanMatKhau(String xacNhanMatKhau) { this.xacNhanMatKhau = xacNhanMatKhau; }

    @NotBlank(message = "Nhập họ tên")
    @Size(max = 150)
    private String hoTen;

    @NotBlank(message = "Nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 254)
    private String email;

    @NotEmpty(message = "Chọn ít nhất một vai trò")
    private Set<VaiTro> vaiTros = new HashSet<>();

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
