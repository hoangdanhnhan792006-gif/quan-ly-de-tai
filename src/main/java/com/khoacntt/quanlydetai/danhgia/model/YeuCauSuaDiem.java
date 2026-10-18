package com.khoacntt.quanlydetai.danhgia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.hoso.model.*;
import com.khoacntt.quanlydetai.dotdangky.model.DotDangKy;
import com.khoacntt.quanlydetai.dangky.model.DangKy;
import com.khoacntt.quanlydetai.baocao.model.*;
import com.khoacntt.quanlydetai.common.storage.TepDaLuu;

@Entity
@Table(name = "yeu_cau_sua_diem")
public class YeuCauSuaDiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phan_cong_cham_id")
    private PhanCongCham phanCongCham;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phieu_cham_goc_id")
    private PhieuCham phieuChamGoc;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_yeu_cau_id")
    private TaiKhoan nguoiYeuCau;
    @Column(name = "ly_do", nullable = false, columnDefinition = "TEXT")
    private String lyDo;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 32, nullable = false)
    private TrangThaiYeuCauSuaDiem trangThai;
    @Column(name = "yeu_cau_luc", nullable = false)
    private LocalDateTime yeuCauLuc;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private TaiKhoan nguoiDuyet;
    @Column(name = "duyet_luc")
    private LocalDateTime duyetLuc;
    @Column(name = "nhan_xet_duyet", columnDefinition = "TEXT")
    private String nhanXetDuyet;
    @Column(name = "hoan_tat_luc")
    private LocalDateTime hoanTatLuc;
    protected YeuCauSuaDiem() {
    }
    public YeuCauSuaDiem(PhieuCham goc, String lyDo, TaiKhoan actor, LocalDateTime now) {
        this.phieuChamGoc = goc;
        this.phanCongCham = goc.getPhanCongCham();
        this.lyDo = lyDo;
        this.nguoiYeuCau = actor;
        this.yeuCauLuc = now;
        this.trangThai = TrangThaiYeuCauSuaDiem.CHO_DUYET;
    }
    public void duyet(boolean yes, TaiKhoan actor, String nx, LocalDateTime now) {
        this.trangThai = yes ? TrangThaiYeuCauSuaDiem.CHAP_THUAN : TrangThaiYeuCauSuaDiem.TU_CHOI;
        this.nguoiDuyet = actor;
        this.nhanXetDuyet = nx;
        this.duyetLuc = now;
    }
    public void daNop() {
        this.trangThai = TrangThaiYeuCauSuaDiem.DA_NOP_DIEU_CHINH;
    }
    public void hoanTat(LocalDateTime now) {
        this.trangThai = TrangThaiYeuCauSuaDiem.HOAN_TAT;
        this.hoanTatLuc = now;
    }
    public void huy() {
        this.trangThai = TrangThaiYeuCauSuaDiem.HUY;
    }
    public Integer getId() {
        return id;
    }
    public PhanCongCham getPhanCongCham() {
        return phanCongCham;
    }
    public PhieuCham getPhieuChamGoc() {
        return phieuChamGoc;
    }
    public TaiKhoan getNguoiYeuCau() {
        return nguoiYeuCau;
    }
    public String getLyDo() {
        return lyDo;
    }
    public TrangThaiYeuCauSuaDiem getTrangThai() {
        return trangThai;
    }
    public LocalDateTime getYeuCauLuc() {
        return yeuCauLuc;
    }
    public TaiKhoan getNguoiDuyet() {
        return nguoiDuyet;
    }
    public LocalDateTime getDuyetLuc() {
        return duyetLuc;
    }
    public String getNhanXetDuyet() {
        return nhanXetDuyet;
    }
    public LocalDateTime getHoanTatLuc() {
        return hoanTatLuc;
    }
}
