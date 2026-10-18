package com.khoacntt.quanlydetai.dangky.model;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.detai.model.DeTai;
import com.khoacntt.quanlydetai.hoso.model.SinhVien;
import com.khoacntt.quanlydetai.nhom.model.Nhom;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dang_ky")
public class DangKy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "nhom_id")
    private Nhom nhom;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "de_tai_id")
    private DeTai deTai;
    @Column(name = "dot_id", nullable = false) private Integer dotId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "nguoi_dang_ky_id")
    private SinhVien nguoiDangKy;
    @Column(name = "dang_ky_luc", nullable = false) private LocalDateTime dangKyLuc;
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false, length = 32)
    private TrangThaiDangKy trangThai;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "nguoi_duyet_id")
    private TaiKhoan nguoiDuyet;
    @Column(name = "duyet_luc") private LocalDateTime duyetLuc;
    @Column(name = "ly_do", columnDefinition = "TEXT") private String lyDo;
    protected DangKy() {}
    public DangKy(Nhom nhom) { this.nhom = nhom; this.dotId = nhom.getDot().getId(); }
    public void dangKy(DeTai deTai, SinhVien truong, LocalDateTime now) {
        this.deTai = deTai; this.nguoiDangKy = truong; this.dangKyLuc = now;
        this.trangThai = TrangThaiDangKy.CHO_DUYET;
        this.nguoiDuyet = null; this.duyetLuc = null; this.lyDo = null;
    }
    public void duyet(boolean chapThuan, TaiKhoan actor, String lyDo, LocalDateTime now) {
        this.trangThai = chapThuan ? TrangThaiDangKy.CHAP_THUAN : TrangThaiDangKy.TU_CHOI;
        this.nguoiDuyet = actor; this.duyetLuc = now; this.lyDo = lyDo;
    }
    public void huy(String lyDo) { this.trangThai = TrangThaiDangKy.HUY; this.lyDo = lyDo; }
    public Integer getId() { return id; }
    public Nhom getNhom() { return nhom; }
    public DeTai getDeTai() { return deTai; }
    public Integer getDotId() { return dotId; }
    public TrangThaiDangKy getTrangThai() { return trangThai; }
    public String getLyDo() { return lyDo; }
    public LocalDateTime getDangKyLuc() { return dangKyLuc; }
}
