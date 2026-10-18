package com.khoacntt.quanlydetai.detai.form;

import jakarta.validation.constraints.*;

public class DeTaiForm {
    @NotNull(message = "Chọn đợt và bộ môn") @Positive
    private Integer dotId;

    @NotNull(message = "Chọn đợt và bộ môn") @Positive
    private Integer boMonId;

    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,30}", message = "Mã đề tài tối đa 30 chữ, số, gạch ngang hoặc gạch dưới")
    private String maDeTai;

    @NotBlank(message = "Nhập tên đề tài") @Size(max = 255)
    private String tenDeTai;

    @NotBlank(message = "Nhập nội dung đề tài") @Size(max = 10000)
    private String noiDung;

    @Size(max = 10000)
    private String yeuCau;

    @Size(max = 10000)
    private String sanPhamDuKien;

    public Integer getDotId() {
        return dotId;
    }

    public Integer getBoMonId() {
        return boMonId;
    }

    public String getMaDeTai() {
        return maDeTai;
    }

    public String getTenDeTai() {
        return tenDeTai;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public String getYeuCau() {
        return yeuCau;
    }

    public String getSanPhamDuKien() {
        return sanPhamDuKien;
    }

    public void setDotId(Integer dotId) {
        this.dotId = dotId;
    }

    public void setBoMonId(Integer boMonId) {
        this.boMonId = boMonId;
    }

    public void setMaDeTai(String maDeTai) {
        this.maDeTai = maDeTai;
    }

    public void setTenDeTai(String tenDeTai) {
        this.tenDeTai = tenDeTai;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public void setYeuCau(String yeuCau) {
        this.yeuCau = yeuCau;
    }

    public void setSanPhamDuKien(String sanPhamDuKien) {
        this.sanPhamDuKien = sanPhamDuKien;
    }
}
