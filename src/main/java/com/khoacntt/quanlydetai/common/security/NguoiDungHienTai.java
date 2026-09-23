package com.khoacntt.quanlydetai.common.security;

import com.khoacntt.quanlydetai.account.TaiKhoan;
import com.khoacntt.quanlydetai.account.TaiKhoanRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NguoiDungHienTai {

    private final TaiKhoanRepository taiKhoanRepository;

    public NguoiDungHienTai(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Transactional(readOnly = true)
    public TaiKhoan batBuoc() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Bạn cần đăng nhập để thực hiện thao tác này");
        }

        return taiKhoanRepository.findByTenDangNhap(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Tài khoản đăng nhập không còn tồn tại"));
    }
}
