package com.khoacntt.quanlydetai.dangky.model;

public enum TrangThaiDangKy {
    CHO_DUYET("Chờ duyệt"),
    CHAP_THUAN("Đã chấp thuận"),
    TU_CHOI("Từ chối"),
    HUY("Đã hủy");
    private final String nhan;
    TrangThaiDangKy(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }
}
