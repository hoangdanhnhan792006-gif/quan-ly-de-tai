package com.khoacntt.quanlydetai.detai.model;

public enum TrangThaiDeTai {
    NHAP,
    CHO_DUYET,
    TU_CHOI,
    DA_DUYET,
    DA_CONG_BO;

    public String getNhan() {
        return switch (this) {
            case NHAP -> "Nháp";
            case CHO_DUYET -> "Chờ duyệt";
            case TU_CHOI -> "Bị từ chối";
            case DA_DUYET -> "Đã duyệt";
            case DA_CONG_BO -> "Đã công bố";
        };
    }
}
