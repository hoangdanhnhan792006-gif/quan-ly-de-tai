package com.khoacntt.quanlydetai.thongbao.model;

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
@Table(name = "thong_bao_tep")
public class ThongBaoTep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thong_bao_id")
    private ThongBao thongBao;
    @Column(name = "ten_tep_goc", length = 255, nullable = false)
    private String tenTepGoc;
    @Column(name = "khoa_luu_tru", length = 255, nullable = false)
    private String khoaLuuTru;
    @Column(name = "loai_mime", length = 127, nullable = false)
    private String loaiMime;
    @Column(name = "kich_thuoc_byte", nullable = false)
    private Long kichThuocByte;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tai_len_id")
    private TaiKhoan nguoiTaiLen;
    protected ThongBaoTep() {
    }
    public ThongBaoTep(ThongBao tb, TepDaLuu tep, TaiKhoan actor) {
        this.thongBao = tb;
        this.tenTepGoc = tep.tenGoc();
        this.khoaLuuTru = tep.khoa();
        this.loaiMime = tep.mime();
        this.kichThuocByte = tep.kichThuoc();
        this.nguoiTaiLen = actor;
    }
    public Integer getId() {
        return id;
    }
    public ThongBao getThongBao() {
        return thongBao;
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
    public TaiKhoan getNguoiTaiLen() {
        return nguoiTaiLen;
    }
}
