package com.khoacntt.quanlydetai.hoso.form;

import jakarta.validation.constraints.*;

public class GiangVienForm {
    @NotNull(message = "Chọn tài khoản giảng viên")
    @Positive
    private Integer taiKhoanId;

    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_-]{1,20}", message = "Mã giảng viên gồm tối đa 20 chữ, số hoặc dấu gạch")
    private String maGv;

    @NotNull(message = "Chọn bộ môn")
    @Positive
    private Integer boMonId;

    @Size(max = 50)
    private String hocVi;

    public Integer getTaiKhoanId() {
        return taiKhoanId;
    }

    public void setTaiKhoanId(Integer taiKhoanId) {
        this.taiKhoanId = taiKhoanId;
    }

    public String getMaGv() {
        return maGv;
    }

    public void setMaGv(String maGv) {
        this.maGv = maGv;
    }

    public Integer getBoMonId() {
        return boMonId;
    }

    public void setBoMonId(Integer boMonId) {
        this.boMonId = boMonId;
    }

    public String getHocVi() {
        return hocVi;
    }

    public void setHocVi(String hocVi) {
        this.hocVi = hocVi;
    }

}
