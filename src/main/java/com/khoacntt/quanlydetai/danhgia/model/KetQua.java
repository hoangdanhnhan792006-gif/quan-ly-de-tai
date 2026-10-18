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
@Table(name = "ket_qua")
public class KetQua {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "danh_gia_id")
    private DanhGia danhGia;
    @Column(name = "phien_ban", nullable = false)
    private Integer phienBan;
    @Column(name = "so_nguoi_cham", nullable = false)
    private Byte soNguoiCham;
    @Column(name = "tong_diem", precision = 6, scale = 2, nullable = false)
    private BigDecimal tongDiem;
    @Column(name = "diem_cuoi", precision = 3, scale = 1, insertable = false, updatable = false)
    private BigDecimal diemCuoi;
    @Column(insertable = false, updatable = false)
    private Byte dat;
    @Column(name = "nhan_xet_tong_hop", nullable = false, columnDefinition = "TEXT")
    private String nhanXetTongHop;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 32, nullable = false)
    private TrangThaiKetQua trangThai;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tong_hop_id")
    private TaiKhoan nguoiTongHop;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xac_nhan_id")
    private TaiKhoan nguoiXacNhan;
    @Column(name = "xac_nhan_luc")
    private LocalDateTime xacNhanLuc;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_cong_bo_id")
    private TaiKhoan nguoiCongBo;
    @Column(name = "cong_bo_luc")
    private LocalDateTime congBoLuc;
    protected KetQua() {
    }
    public KetQua(DanhGia dg, int version, int soNguoi, BigDecimal tong, String nx, TaiKhoan actor) {
        this.danhGia = dg;
        this.phienBan = version;
        this.soNguoiCham = (byte) soNguoi;
        this.tongDiem = tong;
        this.nhanXetTongHop = nx;
        this.nguoiTongHop = actor;
        this.trangThai = TrangThaiKetQua.CHO_XAC_NHAN;
    }
    public void xacNhan(TaiKhoan actor, LocalDateTime now) {
        this.trangThai = TrangThaiKetQua.DA_XAC_NHAN;
        this.nguoiXacNhan = actor;
        this.xacNhanLuc = now;
    }
    public void congBo(TaiKhoan actor, LocalDateTime now) {
        this.trangThai = TrangThaiKetQua.DA_CONG_BO;
        this.nguoiCongBo = actor;
        this.congBoLuc = now;
    }
    public void huy() {
        this.trangThai = TrangThaiKetQua.HUY;
    }
    public Integer getId() {
        return id;
    }
    public DanhGia getDanhGia() {
        return danhGia;
    }
    public Integer getPhienBan() {
        return phienBan;
    }
    public Byte getSoNguoiCham() {
        return soNguoiCham;
    }
    public BigDecimal getTongDiem() {
        return tongDiem;
    }
    public BigDecimal getDiemCuoi() {
        return diemCuoi;
    }
    public Byte getDat() {
        return dat;
    }
    public String getNhanXetTongHop() {
        return nhanXetTongHop;
    }
    public TrangThaiKetQua getTrangThai() {
        return trangThai;
    }
    public TaiKhoan getNguoiTongHop() {
        return nguoiTongHop;
    }
    public TaiKhoan getNguoiXacNhan() {
        return nguoiXacNhan;
    }
    public LocalDateTime getXacNhanLuc() {
        return xacNhanLuc;
    }
    public TaiKhoan getNguoiCongBo() {
        return nguoiCongBo;
    }
    public LocalDateTime getCongBoLuc() {
        return congBoLuc;
    }
}
