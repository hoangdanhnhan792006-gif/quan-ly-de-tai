package com.khoacntt.quanlydetai.baocao.model;

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
@Table(name = "bao_cao")
public class BaoCao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dang_ky_id")
    private DangKy dangKy;
    @Column(name = "phien_ban", nullable = false)
    private Integer phienBan;
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 32, nullable = false)
    private TrangThaiBaoCao trangThai;
    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tao_id")
    private SinhVien nguoiTao;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nop_id")
    private SinhVien nguoiNop;
    @Column(name = "nop_luc")
    private LocalDateTime nopLuc;
    protected BaoCao() {
    }
    public BaoCao(DangKy dk, int version, SinhVien sv, String ghiChu, LocalDateTime now) {
        this.dangKy = dk;
        this.phienBan = version;
        this.nguoiTao = sv;
        this.nguoiNop = sv;
        this.ghiChu = ghiChu;
        this.nopLuc = now;
        this.trangThai = TrangThaiBaoCao.DA_NOP;
    }
    public Integer getId() {
        return id;
    }
    public DangKy getDangKy() {
        return dangKy;
    }
    public Integer getPhienBan() {
        return phienBan;
    }
    public TrangThaiBaoCao getTrangThai() {
        return trangThai;
    }
    public String getGhiChu() {
        return ghiChu;
    }
    public SinhVien getNguoiTao() {
        return nguoiTao;
    }
    public SinhVien getNguoiNop() {
        return nguoiNop;
    }
    public LocalDateTime getNopLuc() {
        return nopLuc;
    }
}
