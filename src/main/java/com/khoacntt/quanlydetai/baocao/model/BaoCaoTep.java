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
@Table(name = "bao_cao_tep")
public class BaoCaoTep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bao_cao_id")
    private BaoCao baoCao;
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_tai_lieu", length = 32, nullable = false)
    private LoaiTaiLieuBaoCao loaiTaiLieu;
    @Column(name = "ten_tep_goc", length = 255, nullable = false)
    private String tenTepGoc;
    @Column(name = "khoa_luu_tru", length = 255, nullable = false)
    private String khoaLuuTru;
    @Column(name = "loai_mime", length = 127, nullable = false)
    private String loaiMime;
    @Column(name = "kich_thuoc_byte", nullable = false)
    private Long kichThuocByte;
    @Column(name = "ma_kiem_tra", length = 64, columnDefinition = "char(64)")
    private String maKiemTra;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tai_len_id")
    private TaiKhoan nguoiTaiLen;
    protected BaoCaoTep() {
    }
    public BaoCaoTep(BaoCao baoCao, LoaiTaiLieuBaoCao loai, TepDaLuu tep, TaiKhoan actor) {
        this.baoCao = baoCao;
        this.loaiTaiLieu = loai;
        this.tenTepGoc = tep.tenGoc();
        this.khoaLuuTru = tep.khoa();
        this.loaiMime = tep.mime();
        this.kichThuocByte = tep.kichThuoc();
        this.maKiemTra = tep.sha256();
        this.nguoiTaiLen = actor;
    }
    public Integer getId() {
        return id;
    }
    public BaoCao getBaoCao() {
        return baoCao;
    }
    public LoaiTaiLieuBaoCao getLoaiTaiLieu() {
        return loaiTaiLieu;
    }
    public String getTenTepGoc() {
        return tenTepGoc;
    }
    public String getKhoaLuuTru() {
        return khoaLuuTru;
    }
    public String getLoaiMime() {
        return loaiMime;
    }
    public Long getKichThuocByte() {
        return kichThuocByte;
    }
    public String getMaKiemTra() {
        return maKiemTra;
    }
    public TaiKhoan getNguoiTaiLen() {
        return nguoiTaiLen;
    }
}
