package com.khoacntt.quanlydetai.danhgia.model;
import jakarta.persistence.*;
import com.khoacntt.quanlydetai.hoso.model.GiangVien;
@Entity
@Table(name = "chi_tiet_ket_qua")
public class ChiTietKetQua {
    @EmbeddedId
    private ChiTietKetQuaId id;
    @MapsId("ketQuaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ket_qua_id")
    private KetQua ketQua;
    @MapsId("phanCongChamId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phan_cong_cham_id")
    private PhanCongCham phanCongCham;
    @Column(name = "danh_gia_id", nullable = false)
    private Integer danhGiaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phieu_cham_id")
    private PhieuCham phieuCham;
    protected ChiTietKetQua() {
    }
    public ChiTietKetQua(KetQua kq, PhieuCham phieu) {
        this.ketQua = kq;
        this.phanCongCham = phieu.getPhanCongCham();
        this.phieuCham = phieu;
        this.danhGiaId = kq.getDanhGia().getId();
        this.id = new ChiTietKetQuaId(kq.getId(), phanCongCham.getId());
    }
    public PhieuCham getPhieuCham() {
        return phieuCham;
    }
    public KetQua getKetQua() {
        return ketQua;
    }
    public PhanCongCham getPhanCongCham() {
        return phanCongCham;
    }
}
