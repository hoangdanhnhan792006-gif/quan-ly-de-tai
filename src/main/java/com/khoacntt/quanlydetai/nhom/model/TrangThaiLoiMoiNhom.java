package com.khoacntt.quanlydetai.nhom.model;

public enum TrangThaiLoiMoiNhom {
    CHO_PHAN_HOI("Chờ phản hồi"),
    DA_CHAP_NHAN("Đã chấp nhận"),
    TU_CHOI("Từ chối"),
    DA_THU_HOI("Đã thu hồi"),
    KHONG_CON_HIEU_LUC("Không còn hiệu lực");
    private final String nhan;
    TrangThaiLoiMoiNhom(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }
}
