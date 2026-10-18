package com.khoacntt.quanlydetai.baocao.model;

public enum TrangThaiBaoCao {
    NHAP("Nháp"), DA_NOP("Đã nộp"), HUY("Đã hủy");

    private final String nhan;
    TrangThaiBaoCao(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
