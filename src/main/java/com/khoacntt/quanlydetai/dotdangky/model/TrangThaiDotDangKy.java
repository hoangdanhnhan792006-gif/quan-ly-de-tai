package com.khoacntt.quanlydetai.dotdangky.model;

public enum TrangThaiDotDangKy {
    NHAP,
    TRIEN_KHAI,
    KET_THUC,
    HUY;

    public String getNhan() {
        return switch (this) {
            case NHAP -> "Nháp";
            case TRIEN_KHAI -> "Đang triển khai";
            case KET_THUC -> "Đã kết thúc";
            case HUY -> "Đã hủy";
        };
    }
}
