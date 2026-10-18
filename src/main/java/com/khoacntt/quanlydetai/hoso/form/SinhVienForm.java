package com.khoacntt.quanlydetai.hoso.form;

import jakarta.validation.constraints.*;

public class SinhVienForm {
    @NotNull(message = "Chọn tài khoản sinh viên")
    @Positive
    private Integer taiKhoanId;

    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_-]{1,20}", message = "Mã sinh viên gồm tối đa 20 chữ, số hoặc dấu gạch")
    private String maSv;

    @Size(max = 30)
    private String lop;

    @Size(max = 30)
    private String khoaHoc;

    public Integer getTaiKhoanId() {
        return taiKhoanId;
    }

    public void setTaiKhoanId(Integer taiKhoanId) {
        this.taiKhoanId = taiKhoanId;
    }

    public String getMaSv() {
        return maSv;
    }

    public void setMaSv(String maSv) {
        this.maSv = maSv;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public String getKhoaHoc() {
        return khoaHoc;
    }

    public void setKhoaHoc(String khoaHoc) {
        this.khoaHoc = khoaHoc;
    }

}
