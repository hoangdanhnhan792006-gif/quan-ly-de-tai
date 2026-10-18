package com.khoacntt.quanlydetai.account.controller;

import com.khoacntt.quanlydetai.account.dto.TaiKhoanDto;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.service.TaiKhoanService;
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
}
