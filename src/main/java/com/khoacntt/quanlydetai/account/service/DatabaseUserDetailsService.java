package com.khoacntt.quanlydetai.account.service;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import com.khoacntt.quanlydetai.account.repository.TaiKhoanRepository;
import com.khoacntt.quanlydetai.account.security.TaiKhoanUserDetails;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

        return new TaiKhoanUserDetails(account.getTenDangNhap(), account.getMatKhauBam(),
                account.getTrangThai() == TrangThaiTaiKhoan.HOAT_DONG, authorities);
    }
}
