package com.khoacntt.quanlydetai.nhom.controller;

import com.khoacntt.quanlydetai.config.SecurityConfig;
import com.khoacntt.quanlydetai.nhom.dto.*;
import com.khoacntt.quanlydetai.nhom.model.*;
import com.khoacntt.quanlydetai.nhom.service.NhomService;
import com.khoacntt.quanlydetai.dangky.controller.*;
import com.khoacntt.quanlydetai.dangky.dto.*;
import com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy;
import com.khoacntt.quanlydetai.dangky.service.DangKyService;
import com.khoacntt.quanlydetai.dotdangky.dto.DotDangKyDto;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {NhomController.class, DangKyController.class, DuyetDangKyController.class})
@Import(SecurityConfig.class)
class NhomWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean NhomService nhomService;
    @MockitoBean DangKyService dangKyService;
    @MockitoBean UserDetailsService userDetailsService;
    @BeforeEach void setup() {
        when(userDetailsService.loadUserByUsername(anyString())).thenAnswer(i -> User.withUsername(i.getArgument(0))
                .password("hash").authorities(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).build());
    }
    private DangKyDto dk() {
        return new DangKyDto(1, 10, "N1", "Đợt", 20, "Đề tài", TrangThaiDangKy.CHO_DUYET, null, LocalDateTime.now(), true);
    }
    private NhomDto nhom(boolean leader, boolean sua) {
        return new NhomDto(10, 1, "Đợt", "N1", "Nhóm", TrangThaiNhom.DA_CHOT,
                List.of(new ThanhVienDto(1, "SV01", "Người 1", true), new ThanhVienDto(2, "SV02", "Người 2", false)),
                List.of(new LoiMoiDto(40, 10, "N1", "Đợt", "Người 1", "SV03", "Mời", TrangThaiLoiMoiNhom.CHO_PHAN_HOI, null, true)),
                dk(), leader, sua, leader ? 1 : 2);
    }
    @Test void chuaDangNhapChuyenDenLogin() throws Exception {
        mvc.perform(get("/sinh-vien/nhom")).andExpect(status().is3xxRedirection());
    }
    @Test @WithMockUser(roles = "ADMIN")
    void adminKhongCoQuyenSinhVienHoacDuyet() throws Exception {
        mvc.perform(get("/sinh-vien/nhom")).andExpect(status().isForbidden());
        mvc.perform(get("/khoa/dang-ky")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles = "SINH_VIEN")
    void sinhVienKhongDuocDuyet() throws Exception {
        mvc.perform(post("/khoa/dang-ky/1/duyet").with(csrf()).param("chapThuan", "true").param("deTaiId", "20"))
                .andExpect(status().isForbidden());
        verify(dangKyService, never()).duyet(any(), any());
    }
    @Test @WithMockUser(roles = "SINH_VIEN")
    void postBatBuocCsrf() throws Exception {
        mvc.perform(post("/sinh-vien/nhom/10/giai-the")).andExpect(status().isForbidden());
        verify(nhomService, never()).giaiThe(any());
    }
    @Test @WithMockUser(roles = "SINH_VIEN")
    void renderDanhSachTaoChiTietVaChonDeTai() throws Exception {
        when(nhomService.cuaToi()).thenReturn(List.of(nhom(true, true)));
        when(nhomService.chiTiet(10)).thenReturn(nhom(true, true));
        when(nhomService.loiMoiCuaToi()).thenReturn(nhom(true, true).loiMois());
        var now = LocalDateTime.now();
        when(nhomService.cacDotDangMo()).thenReturn(List.of(new DotDangKyDto(1, "D1", "Đợt",
                LoaiDeTai.MON_HOC, now, now.plusDays(1), now, now.plusDays(2), now.plusDays(10), null, null, TrangThaiDotDangKy.TRIEN_KHAI)));
        when(dangKyService.luaChon(10)).thenReturn(List.of(new LuaChonDeTaiDto(20, "DT1", "Đề tài", false),
                new LuaChonDeTaiDto(21, "DT2", "Hết chỗ", true)));
        for (String p : List.of("/sinh-vien/nhom", "/sinh-vien/nhom/tao", "/sinh-vien/nhom/10", "/sinh-vien/dang-ky/10"))
            mvc.perform(get(p)).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles = "SINH_VIEN")
    void nhomKhoaKhongHienNutThayDoi() throws Exception {
        when(nhomService.chiTiet(10)).thenReturn(nhom(true, false));
        mvc.perform(get("/sinh-vien/nhom/10")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Chuyển nhóm trưởng"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Gửi lời mời"))));
    }
    @Test @WithMockUser(roles = "SINH_VIEN")
    void formSaiKhongGoiService() throws Exception {
        mvc.perform(post("/sinh-vien/nhom/tao").with(csrf())).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"));
        verify(nhomService, never()).tao(any());
    }
    @Test @WithMockUser(roles = "SINH_VIEN")
    void taoHopLeChuyenVeNhom() throws Exception {
        when(nhomService.tao(any())).thenReturn(10);
        mvc.perform(post("/sinh-vien/nhom/tao").with(csrf()).param("dotId", "1").param("maNhom", "N1"))
                .andExpect(redirectedUrl("/sinh-vien/nhom/10"));
    }
    @Test @WithMockUser(roles = "CAN_BO_KHOA")
    void khoaXemNhomVaDuyetDuoc() throws Exception {
        when(dangKyService.quanLy()).thenReturn(List.of(dk()));
        when(nhomService.chiTiet(10)).thenReturn(nhom(false, true));
        mvc.perform(get("/khoa/dang-ky")).andExpect(status().isOk());
        mvc.perform(get("/khoa/dang-ky/nhom/10")).andExpect(status().isOk());
        mvc.perform(post("/khoa/dang-ky/1/duyet").with(csrf()).param("chapThuan", "true").param("deTaiId", "20"))
                .andExpect(redirectedUrl("/khoa/dang-ky"));
        verify(dangKyService).duyet(eq(1), any());
    }
}
