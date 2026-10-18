package com.khoacntt.quanlydetai.danhgia.model;
import jakarta.persistence.*;
import com.khoacntt.quanlydetai.hoso.model.GiangVien;
@Entity
@Table(name = "thanh_vien_hoi_dong")
public class ThanhVienHoiDong {
    @EmbeddedId
    private ThanhVienHoiDongId id;
    @MapsId("hoiDongId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hoi_dong_id")
    private HoiDong hoiDong;
    @MapsId("giangVienId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giang_vien_id")
    private GiangVien giangVien;
    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", length = 32, nullable = false)
    private VaiTroHoiDong vaiTro;
    protected ThanhVienHoiDong() {
    }
    public ThanhVienHoiDong(HoiDong hd, GiangVien gv, VaiTroHoiDong vaiTro) {
        this.id = new ThanhVienHoiDongId(hd.getId(), gv.getId());
        this.hoiDong = hd;
        this.giangVien = gv;
        this.vaiTro = vaiTro;
    }
    public VaiTroHoiDong getVaiTro() {
        return vaiTro;
    }
    public HoiDong getHoiDong() {
        return hoiDong;
    }
    public GiangVien getGiangVien() {
        return giangVien;
    }
}
