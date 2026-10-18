package com.khoacntt.quanlydetai.danhgia.model;

public enum TrangThaiPhanBien {
    CHUA_BAT_DAU("Chưa bắt đầu"), DANG_CHAM("Đang chấm"), HOAN_TAT("Hoàn tất");

    private final String nhan;
    TrangThaiPhanBien(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
