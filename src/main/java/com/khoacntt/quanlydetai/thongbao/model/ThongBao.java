package com.khoacntt.quanlydetai.thongbao.model;

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
@Table(name = "thong_bao")
public class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "tieu_de", length = 255, nullable = false)
    private String tieuDe;
    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;
    @Enumerated(EnumType.STRING)
    @Column(name = "don_vi_ban_hanh", length = 32, nullable = false)
    private DonViBanHanh donViBanHanh;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_dang_id")
    private TaiKhoan nguoiDang;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 32, nullable = false)
    private TrangThaiThongBao trangThai;
    @Column(name = "cong_bo_luc")
    private LocalDateTime congBoLuc;
    protected ThongBao() {
    }
    public ThongBao(TaiKhoan actor) {
        this.nguoiDang = actor;
        this.trangThai = TrangThaiThongBao.NHAP;
    }
    public void sua(String title, String text, DonViBanHanh donVi) {
        this.tieuDe = title;
        this.noiDung = text;
        this.donViBanHanh = donVi;
    }
    public void congBo(LocalDateTime now) {
        this.trangThai = TrangThaiThongBao.DA_CONG_BO;
        this.congBoLuc = now;
    }
    public void thuHoi() {
        this.trangThai = TrangThaiThongBao.THU_HOI;
    }
    public Integer getId() {
        return id;
    }
    public String getTieuDe() {
        return tieuDe;
    }
    public String getNoiDung() {
        return noiDung;
    }
    public DonViBanHanh getDonViBanHanh() {
        return donViBanHanh;
    }
    public TaiKhoan getNguoiDang() {
        return nguoiDang;
    }
    public TrangThaiThongBao getTrangThai() {
        return trangThai;
    }
    public LocalDateTime getCongBoLuc() {
        return congBoLuc;
    }
}
