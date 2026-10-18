package com.khoacntt.quanlydetai.nhom.model;

import com.khoacntt.quanlydetai.hoso.model.SinhVien;
import jakarta.persistence.*;

@Entity
@Table(name = "thanh_vien_nhom")
public class ThanhVienNhom {
    @EmbeddedId private ThanhVienNhomId id;
    @MapsId("nhomId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nhom_id") private Nhom nhom;
    @MapsId("sinhVienId") @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sinh_vien_id") private SinhVien sinhVien;
    @Column(name = "dot_id", nullable = false) private Integer dotId;
    @Column(name = "la_nhom_truong", nullable = false) private Byte laNhomTruong;
    protected ThanhVienNhom() {}
    public ThanhVienNhom(Nhom nhom, SinhVien sinhVien, boolean truong) {
        this.id = new ThanhVienNhomId(nhom.getId(), sinhVien.getId());
        this.nhom = nhom; this.sinhVien = sinhVien; this.dotId = nhom.getDot().getId();
        datTruongNhom(truong);
    }
    public void datTruongNhom(boolean truong) { this.laNhomTruong = (byte) (truong ? 1 : 0); }
    public Nhom getNhom() { return nhom; }
    public SinhVien getSinhVien() { return sinhVien; }
    public boolean isTruongNhom() { return laNhomTruong == 1; }
}
