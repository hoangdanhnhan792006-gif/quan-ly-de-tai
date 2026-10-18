package com.khoacntt.quanlydetai.nhom.model;

public enum TrangThaiNhom {
    DANG_LAP("Đang lập nhóm"),
    DA_CHOT("Chờ duyệt đăng ký"),
    DANG_THUC_HIEN("Đang thực hiện"),
    HOAN_THANH("Hoàn thành"),
    GIAI_THE("Đã giải thể");
    private final String nhan;
    TrangThaiNhom(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }
}
