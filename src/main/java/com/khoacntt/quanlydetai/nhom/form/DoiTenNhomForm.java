package com.khoacntt.quanlydetai.nhom.form;

import jakarta.validation.constraints.*;

public class DoiTenNhomForm {
    @Size(max = 150)
    private String tenNhom;
    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }
}
