package com.khoacntt.quanlydetai.detai.model;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.dotdangky.model.DotDangKy;
import com.khoacntt.quanlydetai.hoso.model.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "de_tai")
public class DeTai {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "dot_id", nullable = false)
    private DotDangKy dot;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "bo_mon_id", nullable = false)
    private BoMon boMon;
    @Column(name = "ma_de_tai", nullable = false, length = 30)
    private String maDeTai;
    @Column(name = "ten_de_tai", nullable = false, length = 255)
    private String tenDeTai;
    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;
    @Column(name = "yeu_cau", columnDefinition = "TEXT")
    private String yeuCau;
    @Column(name = "san_pham_du_kien", columnDefinition = "TEXT")
    private String sanPhamDuKien;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "gv_de_xuat_id", nullable = false)
    private GiangVien gvDeXuat;
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false, length = 32)
    private TrangThaiDeTai trangThai = TrangThaiDeTai.NHAP;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "nguoi_duyet_id")
    private TaiKhoan nguoiDuyet;
    @Column(name = "duyet_luc")
    private LocalDateTime duyetLuc;
    @Column(name = "nhan_xet_duyet", columnDefinition = "TEXT")
    private String nhanXetDuyet;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "nguoi_cong_bo_id")
    private TaiKhoan nguoiCongBo;
    @Column(name = "cong_bo_luc")
    private LocalDateTime congBoLuc;

    protected DeTai() {}

    public DeTai(DotDangKy dot, GiangVien gvDeXuat) {
        this.dot = dot;
        this.gvDeXuat = gvDeXuat;
    }

    public void capNhat(BoMon boMon, String maDeTai, String tenDeTai, String noiDung,
                       String yeuCau, String sanPhamDuKien) {
        this.boMon = boMon;
        this.maDeTai = maDeTai;
        this.tenDeTai = tenDeTai;
        this.noiDung = noiDung;
        this.yeuCau = yeuCau;
        this.sanPhamDuKien = sanPhamDuKien;
    }

    public void guiDuyet() {
        trangThai = TrangThaiDeTai.CHO_DUYET;
        nguoiDuyet = null;
        duyetLuc = null;
        nhanXetDuyet = null;
    }

    public void duyet(boolean chapThuan, TaiKhoan nguoiDuyet, String nhanXet, LocalDateTime now) {
        trangThai = chapThuan ? TrangThaiDeTai.DA_DUYET : TrangThaiDeTai.TU_CHOI;
        this.nguoiDuyet = nguoiDuyet;
        duyetLuc = now;
        nhanXetDuyet = nhanXet;
    }

    public void congBo(TaiKhoan nguoiCongBo, LocalDateTime now) {
        trangThai = TrangThaiDeTai.DA_CONG_BO;
        this.nguoiCongBo = nguoiCongBo;
        congBoLuc = now;
    }

    public Integer getId() {
        return id;
    }

    public DotDangKy getDot() {
        return dot;
    }

    public BoMon getBoMon() {
        return boMon;
    }

    public String getMaDeTai() {
        return maDeTai;
    }

    public String getTenDeTai() {
        return tenDeTai;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public String getYeuCau() {
        return yeuCau;
    }

    public String getSanPhamDuKien() {
        return sanPhamDuKien;
    }

    public GiangVien getGvDeXuat() {
        return gvDeXuat;
    }

    public TrangThaiDeTai getTrangThai() {
        return trangThai;
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

    public TaiKhoan getNguoiCongBo() {
        return nguoiCongBo;
    }

    public LocalDateTime getCongBoLuc() {
        return congBoLuc;
    }
}
