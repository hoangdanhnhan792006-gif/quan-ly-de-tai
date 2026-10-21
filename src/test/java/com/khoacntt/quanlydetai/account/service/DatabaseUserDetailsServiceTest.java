package com.khoacntt.quanlydetai.account.service;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.repository.TaiKhoanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @InjectMocks
    private DatabaseUserDetailsService service;

    @Test
    void napTaiKhoanTuDatabaseVaChuyenVaiTroThanhAuthority() {
        TaiKhoan taiKhoan = TaiKhoan.taoMoi(
                "gv01",
                "$2a$10$hash",
                "Giảng viên 01",
                "gv01@example.edu.vn",
                Set.of(VaiTro.GIANG_VIEN));
        when(taiKhoanRepository.findByTenDangNhap("gv01")).thenReturn(Optional.of(taiKhoan));

        var userDetails = service.loadUserByUsername("  GV01 ");

        assertThat(userDetails.getUsername()).isEqualTo("gv01");
        assertThat(((com.khoacntt.quanlydetai.account.security.TaiKhoanUserDetails) userDetails).getHoTen())
                .isEqualTo("Giảng viên 01");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_GIANG_VIEN");
    }

    @Test
    void tuChoiTenDangNhapRong() {
        assertThatThrownBy(() -> service.loadUserByUsername("  "))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Tên đăng nhập hoặc mật khẩu không đúng");
    }
}
