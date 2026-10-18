package com.khoacntt.quanlydetai.danhgia.model;

public enum TrangThaiYeuCauSuaDiem {
    CHO_DUYET("Chờ duyệt"), CHAP_THUAN("Được sửa điểm"), DA_NOP_DIEU_CHINH("Đã nộp điều chỉnh"), HOAN_TAT(
            "Hoàn tất"), TU_CHOI("Từ chối"), HUY("Đã hủy");

    private final String nhan;
    TrangThaiYeuCauSuaDiem(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
