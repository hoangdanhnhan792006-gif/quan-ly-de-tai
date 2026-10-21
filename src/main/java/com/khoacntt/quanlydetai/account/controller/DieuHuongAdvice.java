package com.khoacntt.quanlydetai.account.controller;

import com.khoacntt.quanlydetai.account.dto.NguoiDangNhapDto;
import com.khoacntt.quanlydetai.account.security.TaiKhoanUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
@ControllerAdvice
public class DieuHuongAdvice {
    @ModelAttribute("nguoiDangNhap")
    public NguoiDangNhapDto nguoiDangNhap(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return null;
        if (request.getAttribute("nguoiDangNhap") instanceof NguoiDangNhapDto dto) return dto;
        String hoTen = authentication.getPrincipal() instanceof TaiKhoanUserDetails tk
                ? tk.getHoTen() : authentication.getName();
        return new NguoiDangNhapDto(hoTen, authentication.getName());
    }

    @ModelAttribute("laQuanTriVien")
    public boolean laQuanTriVien(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @ModelAttribute("laTruongBoMon")
    public boolean laTruongBoMon(Authentication authentication) {
        return coQuyen(authentication, "ROLE_TRUONG_BO_MON");
    }

    @ModelAttribute("laTruongKhoa")
    public boolean laTruongKhoa(Authentication authentication) {
        return coQuyen(authentication, "ROLE_TRUONG_KHOA");
    }

    @ModelAttribute("laCanBoKhoa")
    public boolean laCanBoKhoa(Authentication authentication) {
        return laTruongKhoa(authentication) || coQuyen(authentication, "ROLE_CAN_BO_KHOA");
    }

    @ModelAttribute("laGiangVien")
    public boolean laGiangVien(Authentication authentication) {
        return coQuyen(authentication, "ROLE_GIANG_VIEN");
    }

    @ModelAttribute("laSinhVien")
    public boolean laSinhVien(Authentication authentication) {
        return coQuyen(authentication, "ROLE_SINH_VIEN");
    }

    private boolean coQuyen(Authentication authentication, String quyen) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(quyen));
    }
}
