package com.khoacntt.quanlydetai.account.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_dang_nhap", nullable = false, length = 50, unique = true)
    private String tenDangNhap;

    @Column(name = "mat_khau_bam", nullable = false, length = 255)
    private String matKhauBam;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "email", nullable = false, length = 254, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 32)
    private TrangThaiTaiKhoan trangThai;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tai_khoan_vai_tro", joinColumns = @JoinColumn(name = "tai_khoan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false, length = 32)
    private Set<VaiTro> vaiTros = new HashSet<>();

    @Column(name = "tao_luc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime taoLuc;

    @Column(name = "cap_nhat_luc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime capNhatLuc;

    protected TaiKhoan() {
    }

    public static TaiKhoan taoMoi(
            String tenDangNhap,
            String matKhauBam,
            String hoTen,
            String email,
            Set<VaiTro> vaiTros) {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.tenDangNhap = chuanHoaTenDangNhap(tenDangNhap);
        taiKhoan.matKhauBam = batBuocCoGiaTri(matKhauBam, "Mật khẩu băm");
        taiKhoan.hoTen = batBuocCoGiaTri(hoTen, "Họ tên");
        taiKhoan.email = batBuocCoGiaTri(email, "Email").toLowerCase(Locale.ROOT);
        taiKhoan.trangThai = TrangThaiTaiKhoan.HOAT_DONG;
        taiKhoan.vaiTros = new HashSet<>(Objects.requireNonNull(vaiTros, "Vai trò không được để trống"));
        if (taiKhoan.vaiTros.isEmpty()) {
            throw new IllegalArgumentException("Tài khoản phải có ít nhất một vai trò");
        }
        return taiKhoan;
    }

    public static String chuanHoaTenDangNhap(String tenDangNhap) {
        return batBuocCoGiaTri(tenDangNhap, "Tên đăng nhập").toLowerCase(Locale.ROOT);
    }

    private static String batBuocCoGiaTri(String giaTri, String tenTruong) {
        if (giaTri == null || giaTri.isBlank()) {
            throw new IllegalArgumentException(tenTruong + " không được để trống");
        }
        return giaTri.trim();
    }

    public void capNhatThongTin(String hoTen, String email, Set<VaiTro> vaiTros) {
        if (vaiTros == null || vaiTros.isEmpty() || vaiTros.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Tài khoản phải có vai trò hợp lệ");
        }
        this.hoTen = batBuocCoGiaTri(hoTen, "Họ tên");
        this.email = batBuocCoGiaTri(email, "Email").toLowerCase(Locale.ROOT);
        this.vaiTros.clear();
        this.vaiTros.addAll(vaiTros);
    }

    public void doiTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = chuanHoaTenDangNhap(tenDangNhap);
    }

    public void doiTrangThai(TrangThaiTaiKhoan trangThai) {
        this.trangThai = Objects.requireNonNull(trangThai);
    }

    public void doiMatKhau(String matKhauBam) {
        this.matKhauBam = batBuocCoGiaTri(matKhauBam, "Mật khẩu băm");
    }

    public Integer getId() {
        return id;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getMatKhauBam() {
        return matKhauBam;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getEmail() {
        return email;
    }

    public TrangThaiTaiKhoan getTrangThai() {
        return trangThai;
    }

    public Set<VaiTro> getVaiTros() {
        return Collections.unmodifiableSet(vaiTros);
    }

    public boolean coVaiTro(VaiTro vaiTro) {
        return vaiTros.contains(vaiTro);
    }

    public LocalDateTime getTaoLuc() {
        return taoLuc;
    }

    public LocalDateTime getCapNhatLuc() {
        return capNhatLuc;
    }
}
