package com.khoacntt.quanlydetai.danhgia.model;

public enum VaiTroHoiDong {
    CHU_TICH("Chủ tịch"), THU_KY("Thư ký"), UY_VIEN("Ủy viên");

    private final String nhan;
    VaiTroHoiDong(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
