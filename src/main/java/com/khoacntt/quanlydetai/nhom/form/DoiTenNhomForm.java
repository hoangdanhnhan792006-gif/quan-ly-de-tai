package com.khoacntt.quanlydetai.nhom.form;

import jakarta.validation.constraints.*;

public class DoiTenNhomForm {
    @NotBlank(message = "Nhập tên nhóm")
    @Size(max = 150, message = "Tên nhóm tối đa 150 ký tự")
    private String tenNhom;
    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }
}
