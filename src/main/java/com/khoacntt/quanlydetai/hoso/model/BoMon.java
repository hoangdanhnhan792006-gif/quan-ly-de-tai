package com.khoacntt.quanlydetai.hoso.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bo_mon")
public class BoMon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "ma_bo_mon", nullable = false, length = 20)
    private String maBoMon;
    @Column(name = "ten_bo_mon", nullable = false, length = 150)
    private String tenBoMon;
    @Column(name = "dang_hoat_dong", nullable = false)
    private Byte dangHoatDong;
    protected BoMon() {
    }

    public BoMon(String maBoMon, String tenBoMon, Byte dangHoatDong) {
        this.maBoMon = maBoMon;
        this.tenBoMon = tenBoMon;
        this.dangHoatDong = dangHoatDong;
    }

    public void capNhat(String maBoMon, String tenBoMon, Byte dangHoatDong) {
        this.maBoMon = maBoMon;
        this.tenBoMon = tenBoMon;
        this.dangHoatDong = dangHoatDong;
    }

    public Integer getId() {
        return id;
    }

    public String getMaBoMon() {
        return maBoMon;
    }

    public String getTenBoMon() {
        return tenBoMon;
    }

    public Byte getDangHoatDong() {
        return dangHoatDong;
    }
}
