package com.khoacntt.quanlydetai.danhgia.model;

public enum TrangThaiHoiDong {
    NHAP("Nháp"), DA_CHOT("Đã chốt"), HOAN_THANH("Hoàn thành");

    private final String nhan;
    TrangThaiHoiDong(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
