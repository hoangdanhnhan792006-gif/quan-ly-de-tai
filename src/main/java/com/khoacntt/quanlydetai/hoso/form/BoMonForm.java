package com.khoacntt.quanlydetai.hoso.form;

import jakarta.validation.constraints.*;

public class BoMonForm {
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_-]{1,20}", message = "Mã gồm tối đa 20 chữ, số hoặc dấu gạch")
    private String maBoMon;

    @NotBlank(message = "Nhập tên bộ môn")
    @Size(max = 150)
    private String tenBoMon;

    @NotNull
    private Boolean dangHoatDong = true;

    public String getMaBoMon() {
        return maBoMon;
    }

    public void setMaBoMon(String maBoMon) {
        this.maBoMon = maBoMon;
    }

    public String getTenBoMon() {
        return tenBoMon;
    }

    public void setTenBoMon(String tenBoMon) {
        this.tenBoMon = tenBoMon;
    }

    public Boolean getDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(Boolean dangHoatDong) {
        this.dangHoatDong = dangHoatDong;
    }

}
