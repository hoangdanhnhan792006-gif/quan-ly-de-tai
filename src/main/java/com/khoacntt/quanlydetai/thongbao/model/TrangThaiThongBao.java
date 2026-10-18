package com.khoacntt.quanlydetai.thongbao.model;

public enum TrangThaiThongBao {
    NHAP("Nháp"), DA_CONG_BO("Đã công bố"), THU_HOI("Đã thu hồi");

    private final String nhan;
    TrangThaiThongBao(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
