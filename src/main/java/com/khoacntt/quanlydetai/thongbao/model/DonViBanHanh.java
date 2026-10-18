package com.khoacntt.quanlydetai.thongbao.model;

public enum DonViBanHanh {
    NHA_TRUONG("Nhà trường"), KHOA_CNTT("Khoa CNTT");

    private final String nhan;
    DonViBanHanh(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
