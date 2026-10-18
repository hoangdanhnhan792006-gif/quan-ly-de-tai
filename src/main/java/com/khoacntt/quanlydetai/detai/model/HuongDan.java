package com.khoacntt.quanlydetai.detai.model;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.hoso.model.GiangVien;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "huong_dan")
public class HuongDan {
    @EmbeddedId
    private HuongDanId id;
    @MapsId("deTaiId") @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "de_tai_id")
    private DeTai deTai;
    @MapsId("giangVienId") @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "giang_vien_id")
    private GiangVien giangVien;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "nguoi_phan_cong_id", nullable = false)
    private TaiKhoan nguoiPhanCong;
    @Column(name = "phan_cong_luc", nullable = false)
    private LocalDateTime phanCongLuc;

    protected HuongDan() {}

    public HuongDan(DeTai deTai, GiangVien giangVien, TaiKhoan nguoiPhanCong, LocalDateTime now) {
        this.id = new HuongDanId(deTai.getId(), giangVien.getId());
        this.deTai = deTai;
        this.giangVien = giangVien;
        this.nguoiPhanCong = nguoiPhanCong;
        this.phanCongLuc = now;
    }

    public GiangVien getGiangVien() {
        return giangVien;
    }
}
