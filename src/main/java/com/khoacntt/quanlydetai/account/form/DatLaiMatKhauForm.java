package com.khoacntt.quanlydetai.account.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DatLaiMatKhauForm {
    @NotBlank(message = "Nhập mật khẩu mới")
    @Size(min = 8, max = 72, message = "Mật khẩu mới gồm 8–72 ký tự")
    private String matKhauMoi;

    @NotBlank(message = "Nhập lại mật khẩu mới")
    private String xacNhanMatKhau;

    public String getMatKhauMoi() { return matKhauMoi; }
    public void setMatKhauMoi(String matKhauMoi) { this.matKhauMoi = matKhauMoi; }
    public String getXacNhanMatKhau() { return xacNhanMatKhau; }
    public void setXacNhanMatKhau(String xacNhanMatKhau) { this.xacNhanMatKhau = xacNhanMatKhau; }
}
