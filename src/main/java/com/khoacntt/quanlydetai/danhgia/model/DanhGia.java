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
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dang_ky_id")
    private DangKy dangKy;
    @Column(name = "dot_id", nullable = false)
    private Integer dotId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bao_cao_chot_id")
    private BaoCao baoCaoChot;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoi_dong_id")
    private HoiDong hoiDong;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_phan_bien", length = 32, nullable = false)
    private TrangThaiPhanBien trangThaiPhanBien;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_hoi_dong", length = 32, nullable = false)
    private TrangThaiDanhGiaHoiDong trangThaiHoiDong;
    @Column(name = "lich_phan_bien_luc")
    private LocalDateTime lichPhanBienLuc;
    @Column(name = "dia_diem_phan_bien", length = 255)
    private String diaDiemPhanBien;
    @Column(name = "phan_bien_hoan_tat_luc")
    private LocalDateTime phanBienHoanTatLuc;
    @Column(name = "hoi_dong_bat_dau")
    private LocalDateTime hoiDongBatDau;
    @Column(name = "hoi_dong_ket_thuc")
    private LocalDateTime hoiDongKetThuc;
    @Column(name = "dia_diem_hoi_dong", length = 255)
    private String diaDiemHoiDong;
    @Column(name = "hoi_dong_hoan_tat_luc")
    private LocalDateTime hoiDongHoanTatLuc;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tao_id")
    private TaiKhoan nguoiTao;
    protected DanhGia() {
    }
    public DanhGia(DangKy dk, boolean coHoiDong, TaiKhoan actor) {
        this.dangKy = dk;
        this.dotId = dk.getDotId();
        this.nguoiTao = actor;
        this.trangThaiPhanBien = TrangThaiPhanBien.CHUA_BAT_DAU;
        this.trangThaiHoiDong = coHoiDong
                ? TrangThaiDanhGiaHoiDong.CHUA_BAT_DAU
                : TrangThaiDanhGiaHoiDong.KHONG_AP_DUNG;
    }
    public void xepLich(HoiDong hd, LocalDateTime pb, String noiPb, LocalDateTime batDau, LocalDateTime ketThuc,
            String noiHd) {
        this.hoiDong = hd;
        this.lichPhanBienLuc = pb;
        this.diaDiemPhanBien = noiPb;
        this.hoiDongBatDau = batDau;
        this.hoiDongKetThuc = ketThuc;
        this.diaDiemHoiDong = noiHd;
    }
    public void batDauPhanBien(BaoCao bc) {
        this.baoCaoChot = bc;
        this.trangThaiPhanBien = TrangThaiPhanBien.DANG_CHAM;
    }
    public void xongPhanBien(LocalDateTime now) {
        this.trangThaiPhanBien = TrangThaiPhanBien.HOAN_TAT;
        this.phanBienHoanTatLuc = now;
    }
    public void batDauHoiDong() {
        this.trangThaiHoiDong = TrangThaiDanhGiaHoiDong.DANG_CHAM;
    }
    public void xongHoiDong(LocalDateTime now) {
        this.trangThaiHoiDong = TrangThaiDanhGiaHoiDong.HOAN_TAT;
        this.hoiDongHoanTatLuc = now;
    }
    public Integer getId() {
        return id;
    }
    public DangKy getDangKy() {
        return dangKy;
    }
    public Integer getDotId() {
        return dotId;
    }
    public BaoCao getBaoCaoChot() {
        return baoCaoChot;
    }
    public HoiDong getHoiDong() {
        return hoiDong;
    }
    public TrangThaiPhanBien getTrangThaiPhanBien() {
        return trangThaiPhanBien;
    }
    public TrangThaiDanhGiaHoiDong getTrangThaiHoiDong() {
        return trangThaiHoiDong;
    }
    public LocalDateTime getLichPhanBienLuc() {
        return lichPhanBienLuc;
    }
    public String getDiaDiemPhanBien() {
        return diaDiemPhanBien;
    }
    public LocalDateTime getPhanBienHoanTatLuc() {
        return phanBienHoanTatLuc;
    }
    public LocalDateTime getHoiDongBatDau() {
        return hoiDongBatDau;
    }
    public LocalDateTime getHoiDongKetThuc() {
        return hoiDongKetThuc;
    }
    public String getDiaDiemHoiDong() {
        return diaDiemHoiDong;
    }
    public LocalDateTime getHoiDongHoanTatLuc() {
        return hoiDongHoanTatLuc;
    }
    public TaiKhoan getNguoiTao() {
        return nguoiTao;
    }
}
