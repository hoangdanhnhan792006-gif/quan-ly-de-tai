package com.khoacntt.quanlydetai.baocao.model;

public enum LoaiTaiLieuBaoCao {
    BAO_CAO_PDF("Báo cáo PDF"), SLIDE("Slide"), MA_NGUON("Mã nguồn");

    private final String nhan;
    LoaiTaiLieuBaoCao(String nhan) {
        this.nhan = nhan;
    }
    public String getNhan() {
        return nhan;
    }
}
