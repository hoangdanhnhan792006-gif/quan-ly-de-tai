package com.khoacntt.quanlydetai.nhom.model;

import com.khoacntt.quanlydetai.hoso.model.SinhVien;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loi_moi_nhom")
public class LoiMoiNhom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "nhom_id")
    private Nhom nhom;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "sinh_vien_duoc_moi_id")
    private SinhVien sinhVienDuocMoi;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "nguoi_moi_id")
    private SinhVien nguoiMoi;
    @Enumerated(EnumType.STRING) @Column(name = "trang_thai", nullable = false, length = 32)
    private TrangThaiLoiMoiNhom trangThai = TrangThaiLoiMoiNhom.CHO_PHAN_HOI;
    @Column(name = "loi_nhan", length = 500) private String loiNhan;
    @Column(name = "gui_luc", nullable = false) private LocalDateTime guiLuc;
    @Column(name = "xu_ly_luc") private LocalDateTime xuLyLuc;
    @Column(name = "ly_do_ket_thuc", length = 500) private String lyDoKetThuc;
    protected LoiMoiNhom() {}
    public LoiMoiNhom(Nhom nhom, SinhVien nhan, SinhVien gui, String loiNhan, LocalDateTime now) {
        this.nhom = nhom; this.sinhVienDuocMoi = nhan; this.nguoiMoi = gui;
        this.loiNhan = loiNhan; this.guiLuc = now;
    }
    public void ketThuc(TrangThaiLoiMoiNhom trangThai, String lyDo, LocalDateTime now) {
        this.trangThai = trangThai; this.lyDoKetThuc = lyDo; this.xuLyLuc = now;
    }
    public Integer getId() { return id; }
    public Nhom getNhom() { return nhom; }
    public SinhVien getSinhVienDuocMoi() { return sinhVienDuocMoi; }
    public SinhVien getNguoiMoi() { return nguoiMoi; }
    public TrangThaiLoiMoiNhom getTrangThai() { return trangThai; }
    public String getLoiNhan() { return loiNhan; }
    public String getLyDoKetThuc() { return lyDoKetThuc; }
}
