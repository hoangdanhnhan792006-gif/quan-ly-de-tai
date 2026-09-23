package com.khoacntt.quanlydetai.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KhoiTaoQuanTriVienTest {

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments applicationArguments;

    @Test
    void taoQuanTriVienKhiChuaTonTai() {
        KhoiTaoQuanTriVienProperties properties = new KhoiTaoQuanTriVienProperties(
                true,
                "Admin",
                "MatKhau123",
                "Quản trị viên",
                "ADMIN@EXAMPLE.EDU.VN");
        when(passwordEncoder.encode("MatKhau123")).thenReturn("bcrypt-hash");

        KhoiTaoQuanTriVien initializer = new KhoiTaoQuanTriVien(
                properties, taiKhoanRepository, passwordEncoder);
        initializer.run(applicationArguments);

        ArgumentCaptor<TaiKhoan> captor = ArgumentCaptor.forClass(TaiKhoan.class);
        verify(taiKhoanRepository).save(captor.capture());
        TaiKhoan saved = captor.getValue();
        assertThat(saved.getTenDangNhap()).isEqualTo("admin");
        assertThat(saved.getEmail()).isEqualTo("admin@example.edu.vn");
        assertThat(saved.getMatKhauBam()).isEqualTo("bcrypt-hash");
        assertThat(saved.coVaiTro(VaiTro.ADMIN)).isTrue();
    }

    @Test
    void khongTaoTrungQuanTriVien() {
        KhoiTaoQuanTriVienProperties properties = new KhoiTaoQuanTriVienProperties(
                true,
                "admin",
                "MatKhau123",
                "Quản trị viên",
                "admin@example.edu.vn");
        when(taiKhoanRepository.existsByTenDangNhap("admin")).thenReturn(true);

        KhoiTaoQuanTriVien initializer = new KhoiTaoQuanTriVien(
                properties, taiKhoanRepository, passwordEncoder);
        initializer.run(applicationArguments);

        verify(taiKhoanRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
