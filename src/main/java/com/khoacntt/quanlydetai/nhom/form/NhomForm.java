package com.khoacntt.quanlydetai.nhom.form;

import jakarta.validation.constraints.*;

public class NhomForm {
    @NotNull @Positive
    private Integer dotId;
    public Integer getDotId() { return dotId; }
    public void setDotId(Integer dotId) { this.dotId = dotId; }
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,30}", message = "Mã nhóm gồm 1–30 chữ, số, dấu gạch ngang/gạch dưới")
    private String maNhom;
    public String getMaNhom() { return maNhom; }
    public void setMaNhom(String maNhom) { this.maNhom = maNhom; }
    @Size(max = 150)
    private String tenNhom;
    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }
}
