package com.khoacntt.quanlydetai.dotdangky.model;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dot_dang_ky")
public class DotDangKy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "ma_dot", nullable = false, length = 30)
    private String maDot;
    @Column(name = "ten_dot", nullable = false, length = 200)
    private String tenDot;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32)
    private LoaiDeTai loai;
    @Column(name = "gv_bat_dau", nullable = false)
    private LocalDateTime gvBatDau;
    @Column(name = "gv_ket_thuc", nullable = false)
    private LocalDateTime gvKetThuc;
    @Column(name = "sv_bat_dau", nullable = false)
    private LocalDateTime svBatDau;
    @Column(name = "sv_ket_thuc", nullable = false)
    private LocalDateTime svKetThuc;
    @Column(name = "han_nop_bao_cao", nullable = false)
    private LocalDateTime hanNopBaoCao;
    @Column(name = "han_nop_diem_pb")
    private LocalDateTime hanNopDiemPb;
    @Column(name = "ngay_bao_cao_hd")
    private LocalDate ngayBaoCaoHd;
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false, length = 32)
    private TrangThaiDotDangKy trangThai = TrangThaiDotDangKy.NHAP;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tao_id", nullable = false)
    private TaiKhoan nguoiTao;

    protected DotDangKy() {}

    public DotDangKy(TaiKhoan nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public void capNhat(String maDot, String tenDot, LoaiDeTai loai,
                       LocalDateTime gvBatDau, LocalDateTime gvKetThuc,
                       LocalDateTime svBatDau, LocalDateTime svKetThuc,
                       LocalDateTime hanNopBaoCao, LocalDateTime hanNopDiemPb, LocalDate ngayBaoCaoHd) {
        this.maDot = maDot;
        this.tenDot = tenDot;
        this.loai = loai;
        this.gvBatDau = gvBatDau;
        this.gvKetThuc = gvKetThuc;
        this.svBatDau = svBatDau;
        this.svKetThuc = svKetThuc;
        this.hanNopBaoCao = hanNopBaoCao;
        this.hanNopDiemPb = hanNopDiemPb;
        this.ngayBaoCaoHd = ngayBaoCaoHd;
    }

    public void chuyenTrangThai(TrangThaiDotDangKy trangThai) {
        this.trangThai = trangThai;
    }

    public boolean dangMoDeXuat(LocalDateTime now) {
        return trangThai == TrangThaiDotDangKy.TRIEN_KHAI && !now.isBefore(gvBatDau) && now.isBefore(gvKetThuc);
    }

    public Integer getId() {
        return id;
    }

    public String getMaDot() {
        return maDot;
    }

    public String getTenDot() {
        return tenDot;
    }

    public LoaiDeTai getLoai() {
        return loai;
    }

    public LocalDateTime getGvBatDau() {
        return gvBatDau;
    }

    public LocalDateTime getGvKetThuc() {
        return gvKetThuc;
    }

    public LocalDateTime getSvBatDau() {
        return svBatDau;
    }

    public LocalDateTime getSvKetThuc() {
        return svKetThuc;
    }

    public LocalDateTime getHanNopBaoCao() {
        return hanNopBaoCao;
    }

    public LocalDateTime getHanNopDiemPb() {
        return hanNopDiemPb;
    }

    public LocalDate getNgayBaoCaoHd() {
        return ngayBaoCaoHd;
    }

    public TrangThaiDotDangKy getTrangThai() {
        return trangThai;
    }

    public TaiKhoan getNguoiTao() {
        return nguoiTao;
    }
}
