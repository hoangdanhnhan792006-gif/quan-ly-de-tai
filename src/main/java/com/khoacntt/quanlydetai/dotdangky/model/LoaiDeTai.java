package com.khoacntt.quanlydetai.dotdangky.model;

public enum LoaiDeTai {
    MON_HOC,
    NCKH,
    TLCN,
    KLTN;

    public String getNhan() {
        return switch (this) {
            case MON_HOC -> "Môn học";
            case NCKH -> "Nghiên cứu khoa học";
            case TLCN -> "Tiểu luận chuyên ngành";
            case KLTN -> "Khóa luận tốt nghiệp";
        };
    }
}
