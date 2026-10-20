package com.khoacntt.quanlydetai.account.integration;

import com.khoacntt.quanlydetai.account.form.SuaTaiKhoanForm;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import java.util.Set;
import com.khoacntt.quanlydetai.account.service.TaiKhoanService;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Chỉ chạy trên schema local riêng quan_ly_de_tai_thu_taikhoan_*. */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "TEST_ACCOUNT_DB_URL", matches = ".+")
class TaiKhoanMySqlTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired TaiKhoanService service;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;
    @Autowired com.khoacntt.quanlydetai.hoso.service.HoSoService hoSo;
    @Autowired com.khoacntt.quanlydetai.nhom.service.NhomService nhoms;
    @Autowired com.khoacntt.quanlydetai.detai.service.DeTaiService deTais;
    int adminId;
    int targetId;
    String adminName;
    String targetName;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        String url = System.getenv("TEST_ACCOUNT_DB_URL");
        if (url == null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/quan_ly_de_tai_thu_taikhoan_[a-zA-Z0-9_]+(\\?.*)?")) {
            throw new IllegalArgumentException("Chỉ cho phép schema local quan_ly_de_tai_thu_taikhoan_*");
        }
        r.add("spring.datasource.url", () -> url);
        r.add("spring.datasource.username", () -> System.getenv("TEST_DB_USERNAME"));
        r.add("spring.datasource.password", () -> System.getenv("TEST_DB_PASSWORD"));
        r.add("app.bootstrap-admin.enabled", () -> false);
        r.add("app.mail.enabled", () -> false);
    }

    @BeforeEach void fixture() {
        assertThat(jdbc.queryForObject("SELECT DATABASE()", String.class)).startsWith("quan_ly_de_tai_thu_taikhoan_");
        String prefix = "tk" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        adminName = prefix + "admin";
        targetName = prefix + "sv";
        adminId = account(adminName, "ADMIN");
        targetId = account(targetName, "SINH_VIEN");
        loginContext(adminName, "ADMIN");
    }

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    int account(String name, String role) {
        jdbc.update("INSERT INTO tai_khoan(ten_dang_nhap,mat_khau_bam,ho_ten,email) VALUES(?,?,?,?)",
                name, encoder.encode("OldPass123"), name, name + "@example.test");
        int id = jdbc.queryForObject("SELECT id FROM tai_khoan WHERE ten_dang_nhap=?", Integer.class, name);
        jdbc.update("INSERT INTO tai_khoan_vai_tro(tai_khoan_id,vai_tro) VALUES(?,?)", id, role);
        return id;
    }

    void loginContext(String name, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(name, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    MockHttpSession webLogin(String name) throws Exception {
        SecurityContextHolder.clearContext();
        return (MockHttpSession) mvc.perform(formLogin().user(name).password("OldPass123"))
                .andExpect(authenticated().withUsername(name)).andReturn().getRequest().getSession(false);
    }

    SuaTaiKhoanForm newPassword() {
        var form = new SuaTaiKhoanForm();
        form.setTenDangNhap(targetName); form.setHoTen("Sinh viên"); form.setEmail(targetName+"@example.test");
        form.setVaiTros(Set.of(VaiTro.SINH_VIEN)); form.setTrangThai(TrangThaiTaiKhoan.HOAT_DONG);
        form.setMatKhauMoi("NewPass123"); form.setXacNhanMatKhau("NewPass123"); return form;
    }

    @Test void xoaTaiKhoanVaVaiTroKhongDeDuLieuMoCoi() {
        jdbc.update("INSERT INTO tai_khoan_vai_tro(tai_khoan_id,vai_tro) VALUES(?,'CAN_BO_KHOA')", targetId);
        service.xoa(targetId);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tai_khoan WHERE id=?", Integer.class, targetId)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tai_khoan_vai_tro WHERE tai_khoan_id=?", Integer.class, targetId)).isZero();
    }

    @Test void fkChanXoaVaRollbackCaVaiTro() {
        jdbc.update("INSERT INTO thong_bao(tieu_de,noi_dung,don_vi_ban_hanh,nguoi_dang_id) VALUES('THU','Thu','KHOA_CNTT',?)", targetId);
        assertThatThrownBy(() -> service.xoa(targetId)).isInstanceOf(NghiepVuException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tai_khoan WHERE id=?", Integer.class, targetId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tai_khoan_vai_tro WHERE tai_khoan_id=?", Integer.class, targetId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM thong_bao WHERE nguoi_dang_id=?", Integer.class, targetId)).isEqualTo(1);
    }

    @Test void taiKhoanCoHoSoVaTuXoaBiChan() {
        jdbc.update("INSERT INTO sinh_vien(tai_khoan_id,ma_sv) VALUES(?,?)", targetId, targetName.toUpperCase());
        assertThatThrownBy(() -> service.xoa(targetId)).isInstanceOf(NghiepVuException.class);
        assertThatThrownBy(() -> service.xoa(adminId)).isInstanceOf(NghiepVuException.class);
        assertThatThrownBy(() -> service.sua(adminId, newPassword())).isInstanceOf(NghiepVuException.class);
    }

    @Test void methodSecurityChanNguoiKhongPhaiAdmin() {
        loginContext(targetName, "SINH_VIEN");
        assertThatThrownBy(() -> service.xoa(adminId)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.sua(adminId, newPassword())).isInstanceOf(AccessDeniedException.class);
    }

    @Test void datLaiMatKhauDoiDangNhapVaKetThucTatCaPhienCu() throws Exception {
        MockHttpSession first = webLogin(targetName);
        MockHttpSession second = webLogin(targetName);
        MockHttpSession admin = webLogin(adminName);
        mvc.perform(post("/admin/tai-khoan/" + targetId + "/sua").session(admin).with(csrf())
                .param("matKhauMoi", "NewPass123").param("xacNhanMatKhau", "NewPass123")
                .param("tenDangNhap",targetName).param("hoTen","Sinh viên").param("email",targetName+"@example.test")
                .param("vaiTros","SINH_VIEN").param("trangThai","HOAT_DONG"))
                .andExpect(redirectedUrl("/admin/tai-khoan"));
        String stored = jdbc.queryForObject("SELECT mat_khau_bam FROM tai_khoan WHERE id=?", String.class, targetId);
        assertThat(stored).isNotEqualTo("NewPass123");
        assertThat(encoder.matches("NewPass123", stored)).isTrue();
        for (var session : List.of(first, second)) {
            mvc.perform(get("/dashboard").session(session)).andExpect(redirectedUrl("/login?sessionChanged"));
            assertThat(session.isInvalid()).isTrue();
        }
        mvc.perform(formLogin().user(targetName).password("OldPass123")).andExpect(unauthenticated());
        mvc.perform(formLogin().user(targetName).password("NewPass123")).andExpect(authenticated().withUsername(targetName));
    }

    @Test void xoaTaiKhoanKetThucPhienDangMo() throws Exception {
        MockHttpSession target = webLogin(targetName);
        MockHttpSession admin = webLogin(adminName);
        mvc.perform(post("/admin/tai-khoan/" + targetId + "/xoa").session(admin).with(csrf()))
                .andExpect(redirectedUrl("/admin/tai-khoan"));
        mvc.perform(get("/dashboard").session(target)).andExpect(redirectedUrl("/login?sessionChanged"));
        mvc.perform(formLogin().user(targetName).password("OldPass123")).andExpect(unauthenticated());
    }

    @Test void doiTenDangNhapGiuIdHoSoVaThuHoiPhienCu() throws Exception {
        jdbc.update("INSERT INTO sinh_vien(tai_khoan_id,ma_sv,lop,khoa_hoc) VALUES(?,?,'CNTT1','2024')", targetId, targetName.toUpperCase());
        MockHttpSession old = webLogin(targetName);
        loginContext(adminName, "ADMIN");
        var f = newPassword(); f.setTenDangNhap(targetName + "moi");
        f.setMatKhauMoi(""); f.setXacNhanMatKhau("");
        String hash = jdbc.queryForObject("SELECT mat_khau_bam FROM tai_khoan WHERE id=?", String.class, targetId);
        service.sua(targetId, f);
        assertThat(jdbc.queryForObject("SELECT id FROM tai_khoan WHERE ten_dang_nhap=?", Integer.class, f.getTenDangNhap())).isEqualTo(targetId);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sinh_vien WHERE tai_khoan_id=?", Integer.class, targetId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT mat_khau_bam FROM tai_khoan WHERE id=?", String.class, targetId)).isEqualTo(hash);
        SecurityContextHolder.clearContext();
        mvc.perform(get("/dashboard").session(old)).andExpect(redirectedUrl("/login?sessionChanged"));
        mvc.perform(formLogin().user(targetName).password("OldPass123")).andExpect(unauthenticated());
        mvc.perform(formLogin().user(f.getTenDangNhap()).password("OldPass123")).andExpect(authenticated().withUsername(f.getTenDangNhap()));
    }

    @Test void doiThongTinVaMatKhauSaiKhongLuuMotPhan() {
        var f = newPassword(); f.setTenDangNhap(targetName + "moi"); f.setHoTen("Tên khác");
        f.setEmail(targetName+"moi@example.test"); f.setTrangThai(TrangThaiTaiKhoan.BI_KHOA);
        f.setXacNhanMatKhau("KhongKhop123");
        assertThatThrownBy(() -> service.sua(targetId, f)).isInstanceOf(NghiepVuException.class);
        assertThat(jdbc.queryForObject("SELECT ten_dang_nhap FROM tai_khoan WHERE id=?", String.class, targetId)).isEqualTo(targetName);
        assertThat(jdbc.queryForObject("SELECT ho_ten FROM tai_khoan WHERE id=?", String.class, targetId)).isEqualTo(targetName);
        assertThat(jdbc.queryForObject("SELECT email FROM tai_khoan WHERE id=?", String.class, targetId)).isEqualTo(targetName+"@example.test");
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM tai_khoan WHERE id=?", String.class, targetId)).isEqualTo("HOAT_DONG");
        assertThat(encoder.matches("OldPass123", jdbc.queryForObject("SELECT mat_khau_bam FROM tai_khoan WHERE id=?",String.class,targetId))).isTrue();
    }

    @Test void khongDoiTenSangTaiKhoanKhacHoacEmailTrung() {
        var f = newPassword(); f.setTenDangNhap(adminName);
        assertThatThrownBy(() -> service.sua(targetId, f)).isInstanceOf(NghiepVuException.class).hasMessageContaining("Tên đăng nhập");
        f.setTenDangNhap(targetName + "moi"); f.setEmail(adminName+"@example.test");
        assertThatThrownBy(() -> service.sua(targetId, f)).isInstanceOf(NghiepVuException.class).hasMessageContaining("Email");
        assertThat(jdbc.queryForObject("SELECT ten_dang_nhap FROM tai_khoan WHERE id=?",String.class,targetId)).isEqualTo(targetName);
    }

    @Test void suaDongThoiTenMatKhauVaiTroVaTrangThai() {
        var f = newPassword(); f.setTenDangNhap(targetName + "moi");
        f.setVaiTros(Set.of(VaiTro.SINH_VIEN, VaiTro.CAN_BO_KHOA)); f.setTrangThai(TrangThaiTaiKhoan.BI_KHOA);
        service.sua(targetId,f);
        assertThat(jdbc.queryForObject("SELECT ten_dang_nhap FROM tai_khoan WHERE id=?",String.class,targetId)).isEqualTo(f.getTenDangNhap());
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM tai_khoan WHERE id=?",String.class,targetId)).isEqualTo("BI_KHOA");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tai_khoan_vai_tro WHERE tai_khoan_id=?",Integer.class,targetId)).isEqualTo(2);
        assertThat(encoder.matches("NewPass123",jdbc.queryForObject("SELECT mat_khau_bam FROM tai_khoan WHERE id=?",String.class,targetId))).isTrue();
    }

    @Test void adminTuDoiTenDangNhapPhaiDangNhapLai() throws Exception {
        MockHttpSession admin = webLogin(adminName);
        mvc.perform(post("/admin/tai-khoan/"+adminId+"/sua").session(admin).with(csrf())
                .param("tenDangNhap",adminName+"moi").param("hoTen","Admin")
                .param("email",adminName+"@example.test").param("vaiTros","ADMIN").param("trangThai","HOAT_DONG")
                .param("matKhauMoi", "").param("xacNhanMatKhau", ""))
                .andExpect(redirectedUrl("/admin/tai-khoan"));
        mvc.perform(get("/admin/tai-khoan").session(admin)).andExpect(redirectedUrl("/login?sessionChanged"));
        mvc.perform(formLogin().user(adminName+"moi").password("OldPass123")).andExpect(authenticated());
    }

    @Test void tuKhoaVaTuDoiMatKhauBoQuaMatKhauCuBiChan() {
        var f = newPassword(); f.setTenDangNhap(adminName); f.setVaiTros(Set.of(VaiTro.ADMIN));
        f.setEmail(adminName+"@example.test"); f.setTrangThai(TrangThaiTaiKhoan.BI_KHOA);
        assertThatThrownBy(() -> service.sua(adminId,f)).isInstanceOf(NghiepVuException.class).hasMessageContaining("tự khóa");
        f.setTrangThai(TrangThaiTaiKhoan.HOAT_DONG);
        assertThatThrownBy(() -> service.sua(adminId,f)).isInstanceOf(NghiepVuException.class).hasMessageContaining("Đổi mật khẩu");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"", "   "})
    void backendChanHoSoSinhVienThieuLopKhoaHoc(String blank) {
        var f = new com.khoacntt.quanlydetai.hoso.form.SinhVienForm();
        f.setTaiKhoanId(targetId); f.setMaSv(targetName); f.setLop(blank); f.setKhoaHoc(blank);
        assertThatThrownBy(() -> hoSo.taoSinhVien(f)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sinh_vien WHERE tai_khoan_id=?",Integer.class,targetId)).isZero();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"", "   "})
    void backendChanTaoVaDoiTenNhomTrong(String blank) {
        loginContext(targetName,"SINH_VIEN");
        var f = new com.khoacntt.quanlydetai.nhom.form.NhomForm(); f.setDotId(1); f.setMaNhom("N1"); f.setTenNhom(blank);
        var doi = new com.khoacntt.quanlydetai.nhom.form.DoiTenNhomForm(); doi.setTenNhom(blank);
        assertThatThrownBy(() -> nhoms.tao(f)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        assertThatThrownBy(() -> nhoms.doiTen(1,doi)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
    }

    @Test void backendChanDeTaiThieuYeuCauSanPham() {
        loginContext(targetName,"GIANG_VIEN");
        var f = new com.khoacntt.quanlydetai.detai.form.DeTaiForm(); f.setDotId(1); f.setBoMonId(1);
        f.setMaDeTai("DT1"); f.setTenDeTai("Đề tài"); f.setNoiDung("Nội dung"); f.setYeuCau("   "); f.setSanPhamDuKien("");
        assertThatThrownBy(() -> deTais.tao(f)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
    }
}
