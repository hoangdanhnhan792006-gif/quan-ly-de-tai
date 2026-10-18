package com.khoacntt.quanlydetai.danhgia.model;

public enum TrangThaiKetQua {
    CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_CONG_BO("Đã công bố"), HUY("Đã hủy");

    private final String nhan;
    TrangThaiKetQua(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
