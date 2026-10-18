package com.khoacntt.quanlydetai.account.form;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.HashSet;

public class DoiMatKhauForm {
    @NotBlank(message = "Nhập mật khẩu hiện tại")
    private String matKhauCu;

    @NotBlank(message = "Nhập mật khẩu mới")
    @Size(min = 8, max = 72, message = "Mật khẩu mới gồm 8–72 ký tự")
    private String matKhauMoi;

    @NotBlank(message = "Nhập lại mật khẩu mới")
    private String xacNhanMatKhau;

    public String getMatKhauCu() {
        return matKhauCu;
    }

    public void setMatKhauCu(String matKhauCu) {
        this.matKhauCu = matKhauCu;
    }

    public String getMatKhauMoi() {
        return matKhauMoi;
    }

    public void setMatKhauMoi(String matKhauMoi) {
        this.matKhauMoi = matKhauMoi;
    }

    public String getXacNhanMatKhau() {
        return xacNhanMatKhau;
    }

    public void setXacNhanMatKhau(String xacNhanMatKhau) {
        this.xacNhanMatKhau = xacNhanMatKhau;
    }

}
