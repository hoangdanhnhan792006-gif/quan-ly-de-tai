package com.khoacntt.quanlydetai.account;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    public DatabaseUserDetailsService(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername;
        try {
            normalizedUsername = TaiKhoan.chuanHoaTenDangNhap(username);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        TaiKhoan account = taiKhoanRepository.findByTenDangNhap(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Tên đăng nhập hoặc mật khẩu không đúng"));

        var authorities = account.getVaiTros().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();

        return User.withUsername(account.getTenDangNhap())
                .password(account.getMatKhauBam())
                .authorities(authorities)
                .disabled(account.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG)
                .build();
    }
}
