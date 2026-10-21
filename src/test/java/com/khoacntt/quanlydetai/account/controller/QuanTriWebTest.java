package com.khoacntt.quanlydetai.account.controller;

import com.khoacntt.quanlydetai.account.dto.TaiKhoanDto;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.service.TaiKhoanService;
import com.khoacntt.quanlydetai.account.security.TaiKhoanUserDetails;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.config.SecurityConfig;
import com.khoacntt.quanlydetai.hoso.controller.BoMonController;
import com.khoacntt.quanlydetai.hoso.controller.GiangVienController;
import com.khoacntt.quanlydetai.hoso.controller.HoSoCaNhanController;
import com.khoacntt.quanlydetai.hoso.controller.SinhVienController;
import com.khoacntt.quanlydetai.hoso.dto.BoMonDto;
import com.khoacntt.quanlydetai.hoso.dto.GiangVienDto;
import com.khoacntt.quanlydetai.hoso.dto.HoSoCaNhanDto;
import com.khoacntt.quanlydetai.hoso.dto.SinhVienDto;
import com.khoacntt.quanlydetai.hoso.service.BoMonService;
import com.khoacntt.quanlydetai.hoso.service.HoSoService;
import com.khoacntt.quanlydetai.web.controller.HomeController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.test.context.support.WithMockUser;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers={TaiKhoanController.class,MatKhauController.class,BoMonController.class,
        SinhVienController.class,GiangVienController.class,HoSoCaNhanController.class,HomeController.class})
@Import(SecurityConfig.class)
class QuanTriWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean TaiKhoanService taiKhoanService;
    @MockitoBean BoMonService boMonService;
    @MockitoBean HoSoService hoSoService;
    @MockitoBean UserDetailsService userDetailsService;
    @BeforeEach void users() {
        when(userDetailsService.loadUserByUsername(anyString())).thenAnswer(inv -> User.withUsername(inv.getArgument(0))
                .password("hash").authorities(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).build());
    }
    @Test void chuaDangNhapKhongVaoAdmin() throws Exception {
        mvc.perform(get("/admin/tai-khoan")).andExpect(status().is3xxRedirection());
    }
    @Test @WithMockUser(roles="SINH_VIEN") void sinhVienKhongVaoAdmin() throws Exception {
        mvc.perform(get("/admin/tai-khoan")).andExpect(status().isForbidden());verifyNoInteractions(taiKhoanService);
    }
    @Test @WithMockUser(roles="ADMIN") void postThieuCsrfBiChan() throws Exception {
        mvc.perform(post("/admin/tai-khoan/tao")).andExpect(status().isForbidden());verifyNoInteractions(taiKhoanService);
    }
    @Test @WithMockUser(roles="ADMIN") void trang403NhanDuocPostChuyenTiep() throws Exception {
        mvc.perform(post("/khong-co-quyen").with(csrf())).andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
    }
    @Test @WithMockUser(roles="ADMIN") void formKhongHopLeKhongGoiService() throws Exception {
        mvc.perform(post("/admin/tai-khoan/tao").with(csrf()).param("tenDangNhap","!").param("email","sai"))
                .andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        verify(taiKhoanService,never()).tao(any());
    }
    @Test @WithMockUser(roles="ADMIN") void taoTaiKhoanChuyenHuong() throws Exception {
        mvc.perform(post("/admin/tai-khoan/tao").with(csrf()).param("tenDangNhap","sv01").param("matKhau","MatKhau123")
                .param("hoTen","Sinh viên 01").param("email","sv01@example.edu.vn").param("vaiTros","SINH_VIEN"))
                .andExpect(redirectedUrl("/admin/tai-khoan"));verify(taiKhoanService).tao(any());
    }
    @Test @WithMockUser(roles="ADMIN") void cacTrangQuanTriRenderDuoc() throws Exception {
        for (String url:List.of("/dashboard","/admin/tai-khoan","/admin/tai-khoan/tao","/admin/bo-mon","/admin/bo-mon/tao",
                "/admin/sinh-vien","/admin/sinh-vien/tao","/admin/giang-vien","/admin/giang-vien/tao","/ho-so/doi-mat-khau")) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }
    @Test @WithMockUser(roles="ADMIN") void trangSuaRenderDungFormAction() throws Exception {
        when(taiKhoanService.chiTiet(1)).thenReturn(new TaiKhoanDto(1,"admin","Admin","admin@example.edu.vn",TrangThaiTaiKhoan.HOAT_DONG,Set.of(VaiTro.ADMIN)));
        when(boMonService.chiTiet(1)).thenReturn(new BoMonDto(1,"CNPM","Công nghệ phần mềm",true));
        when(hoSoService.sinhVien(1)).thenReturn(new SinhVienDto(1,2,"sv01","SV 01","SV01","CNTT1","2024"));
        when(hoSoService.giangVien(1)).thenReturn(new GiangVienDto(1,3,"gv01","GV 01","GV01",1,"CNPM","Thạc sĩ"));
        for(String module:List.of("tai-khoan","bo-mon","sinh-vien","giang-vien")) {
            mvc.perform(get("/admin/"+module+"/1/sua")).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/admin/"+module+"/1/sua\"")));
        }
    }
    @Test @WithMockUser(roles="SINH_VIEN") void caNhanChiGoiServiceCuaNguoiDangNhap() throws Exception {
        when(hoSoService.caNhan()).thenReturn(new HoSoCaNhanDto(new TaiKhoanDto(2,"sv01","SV 01","sv01@example.edu.vn",TrangThaiTaiKhoan.HOAT_DONG,Set.of(VaiTro.SINH_VIEN)),null,null));
        mvc.perform(get("/ho-so").param("id","999")).andExpect(status().isOk());verify(hoSoService).caNhan();
    }
    @Test @WithMockUser(roles="ADMIN") void taiKhoanBiKhoaMatQuyenTrongPhienDangMo() throws Exception {
        when(userDetailsService.loadUserByUsername("user")).thenReturn(User.withUsername("user").password("hash").roles("ADMIN").disabled(true).build());
        mvc.perform(get("/admin/tai-khoan")).andExpect(redirectedUrl("/login?sessionChanged"));verifyNoInteractions(taiKhoanService);
    }
    @Test @WithMockUser(roles="ADMIN") void doiVaiTroBatBuocDangNhapLai() throws Exception {
        when(userDetailsService.loadUserByUsername("user")).thenReturn(User.withUsername("user").password("hash").roles("SINH_VIEN").build());
        mvc.perform(get("/admin/tai-khoan")).andExpect(redirectedUrl("/login?sessionChanged"));verifyNoInteractions(taiKhoanService);
    }

    @Test @WithMockUser(roles="ADMIN") void xoaTaiKhoanBangPostVaBaoKetQua() throws Exception {
        mvc.perform(post("/admin/tai-khoan/2/xoa").with(csrf()))
                .andExpect(redirectedUrl("/admin/tai-khoan")).andExpect(flash().attributeExists("thanhCong"));
        verify(taiKhoanService).xoa(2);
    }

    @Test @WithMockUser(roles="ADMIN") void xoaTaiKhoanCoLienKetBaoLoiTaiDanhSach() throws Exception {
        doThrow(new NghiepVuException("Tài khoản đã có dữ liệu")).when(taiKhoanService).xoa(2);
        mvc.perform(post("/admin/tai-khoan/2/xoa").with(csrf()))
                .andExpect(redirectedUrl("/admin/tai-khoan"))
                .andExpect(flash().attribute("loi", "Tài khoản đã có dữ liệu"));
    }

    @Test @WithMockUser(roles="SINH_VIEN") void sinhVienKhongDuocXoaHoacDatLaiMatKhauNguoiKhac() throws Exception {
        mvc.perform(post("/admin/tai-khoan/2/xoa").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/admin/tai-khoan/2/sua").with(csrf())).andExpect(status().isForbidden());
        verifyNoInteractions(taiKhoanService);
    }

    @Test @WithMockUser(roles="ADMIN") void thaoTacMoiVanBatBuocCsrf() throws Exception {
        mvc.perform(post("/admin/tai-khoan/2/xoa")).andExpect(status().isForbidden());
        mvc.perform(post("/admin/tai-khoan/2/sua")).andExpect(status().isForbidden());
        verifyNoInteractions(taiKhoanService);
    }

    void targetAccount() {
        when(taiKhoanService.chiTiet(2)).thenReturn(new TaiKhoanDto(2,"sv01","SV 01",
                "sv01@example.edu.vn",TrangThaiTaiKhoan.HOAT_DONG,Set.of(VaiTro.SINH_VIEN)));
    }

    @Test @WithMockUser(roles="ADMIN") void formDatLaiMatKhauRenderVaGuiDuoc() throws Exception {
        targetAccount();
        mvc.perform(get("/admin/tai-khoan/2/sua")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/admin/tai-khoan/2/sua\"")));
        mvc.perform(post("/admin/tai-khoan/2/sua").with(csrf())
                .param("matKhauMoi","NewPass123").param("xacNhanMatKhau","NewPass123")
                .param("tenDangNhap","sv01").param("hoTen","SV 01").param("email","sv01@example.edu.vn")
                .param("vaiTros","SINH_VIEN").param("trangThai","HOAT_DONG"))
                .andExpect(redirectedUrl("/admin/tai-khoan"));
        verify(taiKhoanService).sua(eq(2), any());
    }

    @Test @WithMockUser(roles="ADMIN") void formDatLaiMatKhauSaiKhongHienLaiMatKhau() throws Exception {
        targetAccount();
        doThrow(new NghiepVuException("Xác nhận không khớp")).when(taiKhoanService).sua(eq(2),any());
        mvc.perform(post("/admin/tai-khoan/2/sua").with(csrf())
                .param("matKhauMoi","SecretNew123").param("xacNhanMatKhau","SecretWrong123")
                .param("tenDangNhap","sv01").param("hoTen","SV 01").param("email","sv01@example.edu.vn")
                .param("vaiTros","SINH_VIEN").param("trangThai","HOAT_DONG"))
                .andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SecretNew123"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SecretWrong123"))));
    }

    @Test @WithMockUser(roles="ADMIN") void matKhauNganBiValidationChan() throws Exception {
        targetAccount();
        mvc.perform(post("/admin/tai-khoan/2/sua").with(csrf())
                .param("matKhauMoi","123").param("xacNhanMatKhau","123"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","matKhauMoi"));
        verify(taiKhoanService,never()).sua(anyInt(),any());
    }

    @Test @WithMockUser(roles="ADMIN") void suaKhongNhapMatKhauVanLuuDuoc() throws Exception {
        targetAccount();
        mvc.perform(post("/admin/tai-khoan/2/sua").with(csrf()).param("tenDangNhap","sv_moi")
                .param("hoTen","Sinh viên").param("email","sv_moi@example.test").param("vaiTros","SINH_VIEN")
                .param("trangThai","HOAT_DONG").param("matKhauMoi","").param("xacNhanMatKhau",""))
                .andExpect(redirectedUrl("/admin/tai-khoan"));
        verify(taiKhoanService).sua(eq(2), any());
    }

    @Test @WithMockUser(username="admin",roles="ADMIN") void suaTaiKhoanChinhMinhKhongHienOBoQuaMatKhauCu() throws Exception {
        when(taiKhoanService.chiTiet(1)).thenReturn(new TaiKhoanDto(1,"admin","Admin","admin@example.test",TrangThaiTaiKhoan.HOAT_DONG,Set.of(VaiTro.ADMIN)));
        mvc.perform(get("/admin/tai-khoan/1/sua")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("name=\"matKhauMoi\""))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/ho-so/doi-mat-khau")));
    }

    @Test @WithMockUser(roles="ADMIN") void hoSoSinhVienThieuLopKhoaHocHienLoiGanTruong() throws Exception {
        mvc.perform(post("/admin/sinh-vien/tao").with(csrf()).param("taiKhoanId","2")
                .param("maSv","SV01").param("lop","   ").param("khoaHoc",""))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","lop","khoaHoc"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nhập lớp hành chính")));
        verify(hoSoService,never()).taoSinhVien(any());
    }

    @Test void trangDangNhapTaiDuocScriptHienMatKhauKhiChuaDangNhap() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/js/form.js")));
        mvc.perform(get("/js/form.js")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("password-toggle")));
    }

    @Test void doiMatKhauKetThucPhienCuSauKhiCredentialsDaDuocXoa() throws Exception {
        var principal = new TaiKhoanUserDetails("sv01", "old-bcrypt-hash", true,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SINH_VIEN")));
        principal.eraseCredentials();
        org.junit.jupiter.api.Assertions.assertNull(principal.getPassword());
        doReturn(User.withUsername("sv01").password("new-bcrypt-hash").roles("SINH_VIEN").build())
                .when(userDetailsService).loadUserByUsername("sv01");
        mvc.perform(get("/ho-so").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(redirectedUrl("/login?sessionChanged"));
        verifyNoInteractions(hoSoService);
    }

    @Test void matKhauKhongDoiThiPhienVanHoatDong() throws Exception {
        var principal = new TaiKhoanUserDetails("admin", "bcrypt-hash", true,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        principal.eraseCredentials();
        doReturn(User.withUsername("admin").password("bcrypt-hash").roles("ADMIN").build())
                .when(userDetailsService).loadUserByUsername("admin");
        when(taiKhoanService.danhSach()).thenReturn(List.of(new TaiKhoanDto(1,"admin","Admin",
                "admin@example.edu.vn",TrangThaiTaiKhoan.HOAT_DONG,Set.of(VaiTro.ADMIN)),
                new TaiKhoanDto(2,"sv01","SV","sv01@example.edu.vn",TrangThaiTaiKhoan.HOAT_DONG,Set.of(VaiTro.SINH_VIEN))));
        mvc.perform(get("/admin/tai-khoan").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/admin/tai-khoan/2/xoa\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("action=\"/admin/tai-khoan/1/xoa\""))));
    }

    @Test void menuHienDungTaiKhoanMoiRequestVaEscapeHoTen() throws Exception {
        var roles = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(new TaiKhoanUserDetails("demo_admin", "hash", true, roles, "Nguyễn Hoàng Minh"))
                .when(userDetailsService).loadUserByUsername("demo_admin");
        var first = mvc.perform(get("/dashboard").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("demo_admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("nguoiDangNhap", new com.khoacntt.quanlydetai.account.dto.NguoiDangNhapDto("Nguyễn Hoàng Minh", "demo_admin")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nguyễn Hoàng Minh")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("@demo_admin")))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        var preview = java.nio.file.Path.of("target", "identity-preview");
        java.nio.file.Files.createDirectories(preview);
        java.nio.file.Files.writeString(preview.resolve("index.html"), first, java.nio.charset.StandardCharsets.UTF_8);
        doReturn(new TaiKhoanUserDetails("demo_admin", "hash", true, roles, "Tên mới <script>alert(1)</script>"))
                .when(userDetailsService).loadUserByUsername("demo_admin");
        mvc.perform(get("/dashboard").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("demo_admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("nguoiDangNhap", new com.khoacntt.quanlydetai.account.dto.NguoiDangNhapDto("Tên mới <script>alert(1)</script>", "demo_admin")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>alert(1)</script>"))));
        mvc.perform(get("/dashboard").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("demo_other").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("nguoiDangNhap", new com.khoacntt.quanlydetai.account.dto.NguoiDangNhapDto("demo_other", "demo_other")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("@demo_other")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Nguyễn Hoàng Minh"))));
    }
}
