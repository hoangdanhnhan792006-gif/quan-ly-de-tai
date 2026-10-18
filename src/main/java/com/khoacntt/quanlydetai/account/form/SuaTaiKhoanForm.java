package com.khoacntt.quanlydetai.account.form;

import com.khoacntt.quanlydetai.account.model.VaiTro;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.HashSet;

public class SuaTaiKhoanForm {
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
