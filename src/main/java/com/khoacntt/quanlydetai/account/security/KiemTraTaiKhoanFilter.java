package com.khoacntt.quanlydetai.account.security;

import com.khoacntt.quanlydetai.account.dto.NguoiDangNhapDto;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashSet;

/** Kiểm tra tài khoản mỗi request để khóa/xóa/đổi quyền/đổi mật khẩu có hiệu lực với phiên cũ. */
public class KiemTraTaiKhoanFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;

    public KiemTraTaiKhoanFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            try {
                UserDetails user = userDetailsService.loadUserByUsername(auth.getName());
                boolean matKhauThayDoi = auth.getPrincipal() instanceof TaiKhoanUserDetails taiKhoan
                        && taiKhoan.matKhauDaThayDoi(user.getPassword());
                if (!user.isEnabled() || matKhauThayDoi
                        || !new HashSet<>(user.getAuthorities()).equals(new HashSet<>(auth.getAuthorities()))) {
                    ketThucPhien(request, response, auth);
                    return;
                }
                // Tận dụng tài khoản vừa kiểm tra: tên mới có hiệu lực ở request kế tiếp,
                // không truy vấn DB thêm chỉ để hiển thị menu.
                String hoTen = user instanceof TaiKhoanUserDetails tk ? tk.getHoTen() : user.getUsername();
                request.setAttribute("nguoiDangNhap", new NguoiDangNhapDto(hoTen, user.getUsername()));
            } catch (UsernameNotFoundException ex) {
                ketThucPhien(request, response, auth);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void ketThucPhien(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException {
        new SecurityContextLogoutHandler().logout(request, response, auth);
        response.sendRedirect(request.getContextPath() + "/login?sessionChanged");
    }
}
