package com.khoacntt.quanlydetai.danhgia.model;

public enum TrangThaiDanhGiaHoiDong {
    KHONG_AP_DUNG("Không áp dụng"), CHUA_BAT_DAU("Chưa bắt đầu"), DANG_CHAM("Đang chấm"), HOAN_TAT("Hoàn tất");

    private final String nhan;
    TrangThaiDanhGiaHoiDong(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
