package com.khoacntt.quanlydetai.hoso.model;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;

import jakarta.persistence.*;

@Entity
@Table(name = "sinh_vien")
public class SinhVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tai_khoan_id", nullable = false, unique = true)
    private TaiKhoan taiKhoan;
    @Column(name = "ma_sv", nullable = false, length = 20)
    private String maSv;
    @Column(length = 30)
    private String lop;
    @Column(name = "khoa_hoc", length = 30)
    private String khoaHoc;
    protected SinhVien() {
    }

    public SinhVien(TaiKhoan taiKhoan, String maSv, String lop, String khoaHoc) {
        this.taiKhoan = taiKhoan;
        this.maSv = maSv;
        this.lop = lop;
        this.khoaHoc = khoaHoc;
    }

    public void capNhat(String maSv, String lop, String khoaHoc) {
        this.maSv = maSv;
        this.lop = lop;
        this.khoaHoc = khoaHoc;
    }

    public Integer getId() {
        return id;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public String getMaSv() {
        return maSv;
    }

    public String getLop() {
        return lop;
    }

    public String getKhoaHoc() {
        return khoaHoc;
    }
}
