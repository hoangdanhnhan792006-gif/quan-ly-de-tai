package com.khoacntt.quanlydetai.hoso.model;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;

import jakarta.persistence.*;

@Entity
@Table(name = "giang_vien")
public class GiangVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tai_khoan_id", nullable = false, unique = true)
    private TaiKhoan taiKhoan;
    @Column(name = "ma_gv", nullable = false, length = 20)
    private String maGv;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bo_mon_id", nullable = false)
    private BoMon boMon;
    @Column(name = "hoc_vi", length = 50)
    private String hocVi;
    protected GiangVien() {
    }

    public GiangVien(TaiKhoan taiKhoan, String maGv, BoMon boMon, String hocVi) {
        this.taiKhoan = taiKhoan;
        this.maGv = maGv;
        this.boMon = boMon;
        this.hocVi = hocVi;
    }

    public void capNhat(String maGv, BoMon boMon, String hocVi) {
        this.maGv = maGv;
        this.boMon = boMon;
        this.hocVi = hocVi;
    }

    public Integer getId() {
        return id;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public String getMaGv() {
        return maGv;
    }

    public BoMon getBoMon() {
        return boMon;
    }

    public String getHocVi() {
        return hocVi;
    }
}
