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
@Table(name = "phan_cong_cham")
public class PhanCongCham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "danh_gia_id")
    private DanhGia danhGia;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giang_vien_id")
    private GiangVien giangVien;
    @Column(name = "la_phan_bien", nullable = false)
    private Byte laPhanBien;
    @Column(name = "la_thanh_vien_hoi_dong", nullable = false)
    private Byte laThanhVienHoiDong;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_phan_cong_id")
    private TaiKhoan nguoiPhanCong;
    protected PhanCongCham() {
    }
    public PhanCongCham(DanhGia dg, GiangVien gv, boolean pb, boolean hd, TaiKhoan actor) {
        this.danhGia = dg;
        this.giangVien = gv;
        this.laPhanBien = (byte) (pb ? 1 : 0);
        this.laThanhVienHoiDong = (byte) (hd ? 1 : 0);
        this.nguoiPhanCong = actor;
    }
    public boolean isPhanBien() {
        return laPhanBien == 1;
    }
    public boolean isThanhVienHoiDong() {
        return laThanhVienHoiDong == 1;
    }
    public Integer getId() {
        return id;
    }
    public DanhGia getDanhGia() {
        return danhGia;
    }
    public GiangVien getGiangVien() {
        return giangVien;
    }
    public Byte getLaPhanBien() {
        return laPhanBien;
    }
    public Byte getLaThanhVienHoiDong() {
        return laThanhVienHoiDong;
    }
    public TaiKhoan getNguoiPhanCong() {
        return nguoiPhanCong;
    }
}
