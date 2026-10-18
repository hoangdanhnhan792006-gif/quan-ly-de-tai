package com.khoacntt.quanlydetai.danhgia.model;

public enum TrangThaiPhieuCham {
    NHAP("Nháp"), DA_NOP("Đã nộp"), HUY("Đã hủy");

    private final String nhan;
    TrangThaiPhieuCham(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
