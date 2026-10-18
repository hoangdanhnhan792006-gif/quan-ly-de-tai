package com.khoacntt.quanlydetai.nhom.form;

import jakarta.validation.constraints.*;

public class MoiThanhVienForm {
    @NotBlank @Size(max = 20)
    private String maSv;
    public String getMaSv() { return maSv; }
    public void setMaSv(String maSv) { this.maSv = maSv; }
    @Size(max = 500)
    private String loiNhan;
    public String getLoiNhan() { return loiNhan; }
    public void setLoiNhan(String loiNhan) { this.loiNhan = loiNhan; }
}
