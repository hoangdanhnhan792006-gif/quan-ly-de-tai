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
@Table(name = "phieu_cham")
public class PhieuCham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phan_cong_cham_id")
    private PhanCongCham phanCongCham;
    @Column(name = "phien_ban", nullable = false)
    private Integer phienBan;
    @Column(precision = 4, scale = 2)
    private BigDecimal diem;
    @Column(name = "nhan_xet", columnDefinition = "TEXT")
    private String nhanXet;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 32, nullable = false)
    private TrangThaiPhieuCham trangThai;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yeu_cau_sua_diem_id")
    private YeuCauSuaDiem yeuCauSuaDiem;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tao_id")
    private TaiKhoan nguoiTao;
    @Column(name = "nop_luc")
    private LocalDateTime nopLuc;
    protected PhieuCham() {
    }
    public PhieuCham(PhanCongCham pc, int version, YeuCauSuaDiem yc, TaiKhoan actor) {
        this.phanCongCham = pc;
        this.phienBan = version;
        this.yeuCauSuaDiem = yc;
        this.nguoiTao = actor;
        this.trangThai = TrangThaiPhieuCham.NHAP;
    }
    public void luuNhap(BigDecimal diem, String nhanXet) {
        this.diem = diem;
        this.nhanXet = nhanXet;
    }
    public void nop(LocalDateTime now) {
        this.trangThai = TrangThaiPhieuCham.DA_NOP;
        this.nopLuc = now;
    }
    public void huyNhap() {
        this.trangThai = TrangThaiPhieuCham.HUY;
    }
    public Integer getId() {
        return id;
    }
    public PhanCongCham getPhanCongCham() {
        return phanCongCham;
    }
    public Integer getPhienBan() {
        return phienBan;
    }
    public BigDecimal getDiem() {
        return diem;
    }
    public String getNhanXet() {
        return nhanXet;
    }
    public TrangThaiPhieuCham getTrangThai() {
        return trangThai;
    }
    public YeuCauSuaDiem getYeuCauSuaDiem() {
        return yeuCauSuaDiem;
    }
    public TaiKhoan getNguoiTao() {
        return nguoiTao;
    }
    public LocalDateTime getNopLuc() {
        return nopLuc;
    }
}
