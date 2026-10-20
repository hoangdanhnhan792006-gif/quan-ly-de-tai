package com.khoacntt.quanlydetai.detai.controller;

import com.khoacntt.quanlydetai.config.SecurityConfig;
import com.khoacntt.quanlydetai.detai.dto.DeTaiDto;
import com.khoacntt.quanlydetai.detai.dto.BoLocDeTaiDto;
import com.khoacntt.quanlydetai.detai.model.TrangThaiDeTai;
import com.khoacntt.quanlydetai.detai.service.DeTaiService;
import com.khoacntt.quanlydetai.dotdangky.controller.DotDangKyController;
import com.khoacntt.quanlydetai.dotdangky.dto.DotDangKyDto;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.dotdangky.service.DotDangKyService;
import com.khoacntt.quanlydetai.web.controller.HomeController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {DeTaiController.class, DotDangKyController.class, HomeController.class})
@Import(SecurityConfig.class)
class DeTaiWebTest {
    @Autowired MockMvc mvc;
    @MockitoBean DeTaiService deTaiService;
    @MockitoBean DotDangKyService dotService;
    @MockitoBean UserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        when(userDetailsService.loadUserByUsername(anyString())).thenAnswer(i -> User.withUsername(i.getArgument(0))
                .password("hash").authorities(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).build());
    }

    private DotDangKyDto dot() {
        var now = LocalDateTime.of(2026, 10, 1, 9, 0);
        return new DotDangKyDto(1, "DOT01", "Đợt 1", LoaiDeTai.MON_HOC,
                now, now.plusDays(1), now, now.plusDays(2), now.plusDays(20), null, null, TrangThaiDotDangKy.NHAP);
    }

    private DeTaiDto deTai(boolean khoa) {
        return new DeTaiDto(10, 1, "Đợt 1", LoaiDeTai.MON_HOC, 1, "CNPM", "DT01", "Đề tài 1",
                "Nội dung", null, null, "Giảng viên 1", TrangThaiDeTai.DA_DUYET, null,
                List.of(), false, false, khoa, khoa, khoa);
    }

    @Test
    void khachChuaDangNhapBiChuyenVeLogin() throws Exception {
        mvc.perform(get("/de-tai")).andExpect(status().is3xxRedirection());
    }

    @Test @WithMockUser(roles = "SINH_VIEN")
    void svChiXemDanhSachVaKhongCoNutDeXuat() throws Exception {
        when(deTaiService.daCongBo(null, null)).thenReturn(List.of(deTai(false)));
        when(deTaiService.boLocCongBo()).thenReturn(new BoLocDeTaiDto(Map.of(1, "Đợt 1"), Map.of(1, "CNPM")));
        mvc.perform(get("/de-tai")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đề tài 1")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("href=\"/de-tai/tao\""))));
        when(deTaiService.daCongBo(1, 1)).thenReturn(List.of(deTai(false)));
        mvc.perform(get("/de-tai").param("dotId", "1").param("boMonId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<option value=\"1\" selected=\"selected\">Đợt 1</option>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<option value=\"1\" selected=\"selected\">CNPM</option>")));
        verify(deTaiService).daCongBo(1, 1);
        mvc.perform(get("/de-tai/tao")).andExpect(status().isForbidden());
        mvc.perform(get("/de-tai/quan-ly")).andExpect(status().isForbidden());
        mvc.perform(get("/khoa/dot-dang-ky")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminKhongMacNhienCoQuyenKhoa() throws Exception {
        mvc.perform(get("/de-tai/quan-ly")).andExpect(status().isForbidden());
        mvc.perform(post("/de-tai/10/cong-bo").with(csrf())).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "CAN_BO_KHOA")
    void canBoDuocXemDotNhungKhongTaoHoacKetThuc() throws Exception {
        when(dotService.danhSach()).thenReturn(List.of(dot()));
        mvc.perform(get("/khoa/dot-dang-ky")).andExpect(status().isOk());
        mvc.perform(get("/khoa/dot-dang-ky/tao")).andExpect(status().isForbidden());
        mvc.perform(post("/khoa/dot-dang-ky/1/ket-thuc").with(csrf())).andExpect(status().isForbidden());
        verify(dotService, never()).ketThuc(any());
    }

    @Test @WithMockUser(roles = "TRUONG_KHOA")
    void trangTaoSuaChiTietDotRenderDuoc() throws Exception {
        when(dotService.chiTiet(1)).thenReturn(dot());
        for (String path : List.of("/khoa/dot-dang-ky/tao", "/khoa/dot-dang-ky/1", "/khoa/dot-dang-ky/1/sua")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test @WithMockUser(roles = "TRUONG_KHOA")
    void formDotThieuThongTinHienLoiKhongGoiService() throws Exception {
        mvc.perform(post("/khoa/dot-dang-ky/tao").with(csrf())).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"));
        verify(dotService, never()).tao(any());
    }

    @Test @WithMockUser(roles = "GIANG_VIEN")
    void formDeXuatRenderKhiChuaCoDot() throws Exception {
        mvc.perform(get("/de-tai/tao")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hiện không có đợt mở đề xuất")));
    }

    @Test @WithMockUser(roles = "GIANG_VIEN")
    void taoDeTaiHopLeChuyenVeChiTiet() throws Exception {
        when(deTaiService.tao(any())).thenReturn(10);
        mvc.perform(post("/de-tai/tao").with(csrf()).param("dotId", "1").param("boMonId", "1")
                .param("maDeTai", "DT01").param("tenDeTai", "Đề tài").param("noiDung", "Nội dung").param("yeuCau", "Yêu cầu").param("sanPhamDuKien", "Website"))
                .andExpect(redirectedUrl("/de-tai/10"));
    }

    @Test @WithMockUser(roles="GIANG_VIEN") void deTaiThieuYeuCauSanPhamKhongDuocTao() throws Exception {
        mvc.perform(post("/de-tai/tao").with(csrf()).param("dotId","1").param("boMonId","1")
                .param("maDeTai","DT01").param("tenDeTai","Tên đề tài").param("noiDung","Nội dung")
                .param("yeuCau","   ").param("sanPhamDuKien",""))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","yeuCau","sanPhamDuKien"));
        verify(deTaiService,never()).tao(any());
    }

    @Test @WithMockUser(roles = "GIANG_VIEN")
    void giangVienKhongTuDuyetDeTai() throws Exception {
        mvc.perform(post("/de-tai/10/duyet").with(csrf()).param("chapThuan", "true"))
                .andExpect(status().isForbidden());
        verify(deTaiService, never()).duyet(any(), any());
    }

    @Test @WithMockUser(roles = "CAN_BO_KHOA")
    void chiTietCacFormDuyetVaPhanCongRenderDuoc() throws Exception {
        when(deTaiService.chiTiet(10)).thenReturn(deTai(true));
        mvc.perform(get("/de-tai/10")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Phân công GVHD")));
    }

    @Test @WithMockUser(roles = "TRUONG_BO_MON")
    void phanCongBaGiangVienBiChanTaiForm() throws Exception {
        when(deTaiService.chiTiet(10)).thenReturn(deTai(true));
        mvc.perform(post("/de-tai/10/huong-dan").with(csrf()).param("giangVienIds", "1", "2", "3"))
                .andExpect(status().isOk()).andExpect(model().attributeHasErrors("huongDanForm"));
        verify(deTaiService, never()).phanCong(any(), any());
    }

    @Test @WithMockUser(roles = "CAN_BO_KHOA")
    void csrfVanBatBuocKhiCongBo() throws Exception {
        mvc.perform(post("/de-tai/10/cong-bo")).andExpect(status().isForbidden());
        verify(deTaiService, never()).congBo(any());
    }

    @Test @WithMockUser(roles = "SINH_VIEN")
    void tuChoiXemDeTaiTraiQuyenTra403() throws Exception {
        when(deTaiService.chiTiet(10)).thenThrow(new AccessDeniedException("Không được xem"));
        mvc.perform(get("/de-tai/10")).andExpect(status().isForbidden());
    }
}
