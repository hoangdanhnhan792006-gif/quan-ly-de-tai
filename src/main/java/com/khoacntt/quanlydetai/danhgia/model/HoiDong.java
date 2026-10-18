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
@Table(name = "hoi_dong")
public class HoiDong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dot_id")
    private DotDangKy dot;
    @Column(name = "ma_hoi_dong", length = 30, nullable = false)
    private String maHoiDong;
    @Column(name = "ten_hoi_dong", length = 200, nullable = false)
    private String tenHoiDong;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 32, nullable = false)
    private TrangThaiHoiDong trangThai;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tao_id")
    private TaiKhoan nguoiTao;
    protected HoiDong() {
    }
    public HoiDong(DotDangKy dot, String ma, String ten, TaiKhoan actor) {
        this.dot = dot;
        this.maHoiDong = ma;
        this.tenHoiDong = ten;
        this.nguoiTao = actor;
        this.trangThai = TrangThaiHoiDong.NHAP;
    }
    public void doiTen(String ten) {
        this.tenHoiDong = ten;
    }
    public void chot() {
        this.trangThai = TrangThaiHoiDong.DA_CHOT;
    }
    public void hoanThanh() {
        this.trangThai = TrangThaiHoiDong.HOAN_THANH;
    }
    public Integer getId() {
        return id;
    }
    public DotDangKy getDot() {
        return dot;
    }
    public String getMaHoiDong() {
        return maHoiDong;
    }
    public String getTenHoiDong() {
        return tenHoiDong;
    }
    public TrangThaiHoiDong getTrangThai() {
        return trangThai;
    }
    public TaiKhoan getNguoiTao() {
        return nguoiTao;
    }
}
