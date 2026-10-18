package com.khoacntt.quanlydetai.nhom.model;

import com.khoacntt.quanlydetai.dotdangky.model.DotDangKy;
import com.khoacntt.quanlydetai.hoso.model.SinhVien;
import jakarta.persistence.*;

@Entity
@Table(name = "nhom")
public class Nhom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dot_id")
    private DotDangKy dot;
    @Column(name = "ma_nhom", nullable = false, length = 30)
    private String maNhom;
    @Column(name = "ten_nhom", length = 150)
    private String tenNhom;
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false, length = 32)
    private TrangThaiNhom trangThai = TrangThaiNhom.DANG_LAP;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tao_id")
    private SinhVien nguoiTao;
    protected Nhom() {}
    public Nhom(DotDangKy dot, SinhVien nguoiTao, String maNhom, String tenNhom) {
        this.dot = dot; this.nguoiTao = nguoiTao; this.maNhom = maNhom; this.tenNhom = tenNhom;
    }
    public void doiTen(String tenNhom) { this.tenNhom = tenNhom; }
    public void chuyenTrangThai(TrangThaiNhom trangThai) { this.trangThai = trangThai; }
    public Integer getId() { return id; }
    public DotDangKy getDot() { return dot; }
    public String getMaNhom() { return maNhom; }
    public String getTenNhom() { return tenNhom; }
    public TrangThaiNhom getTrangThai() { return trangThai; }
    public SinhVien getNguoiTao() { return nguoiTao; }
}
