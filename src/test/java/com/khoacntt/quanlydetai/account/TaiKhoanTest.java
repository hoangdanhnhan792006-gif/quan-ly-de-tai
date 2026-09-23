package com.khoacntt.quanlydetai.account;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaiKhoanTest {

    @Test
    void taoMoiChuanHoaDuLieuVaGanTrangThaiHoatDong() {
        TaiKhoan taiKhoan = TaiKhoan.taoMoi(
                "  ADMIN01  ",
                "$2a$10$hash",
                "  Nguyễn Văn A  ",
                "  ADMIN@EXAMPLE.EDU.VN  ",
                Set.of(VaiTro.ADMIN));

        assertThat(taiKhoan.getTenDangNhap()).isEqualTo("admin01");
        assertThat(taiKhoan.getHoTen()).isEqualTo("Nguyễn Văn A");
        assertThat(taiKhoan.getEmail()).isEqualTo("admin@example.edu.vn");
        assertThat(taiKhoan.getTrangThai()).isEqualTo(TrangThaiTaiKhoan.HOAT_DONG);
        assertThat(taiKhoan.coVaiTro(VaiTro.ADMIN)).isTrue();
    }

    @Test
    void taiKhoanBatBuocCoItNhatMotVaiTro() {
        assertThatThrownBy(() -> TaiKhoan.taoMoi(
                "admin",
                "$2a$10$hash",
                "Quản trị viên",
                "admin@example.edu.vn",
                Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ít nhất một vai trò");
    }
}
