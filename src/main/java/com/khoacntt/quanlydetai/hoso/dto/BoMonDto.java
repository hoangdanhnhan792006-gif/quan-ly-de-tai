package com.khoacntt.quanlydetai.hoso.dto;

import com.khoacntt.quanlydetai.hoso.model.BoMon;

public record BoMonDto(Integer id, String maBoMon, String tenBoMon, boolean dangHoatDong) {
    public static BoMonDto tu(BoMon b) {
        return new BoMonDto(b.getId(), b.getMaBoMon(), b.getTenBoMon(), b.getDangHoatDong() == 1);
    }
}
