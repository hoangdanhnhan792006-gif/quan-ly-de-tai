package com.khoacntt.quanlydetai.account.service;

import com.khoacntt.quanlydetai.account.form.DoiMatKhauForm;
import com.khoacntt.quanlydetai.account.form.SuaTaiKhoanForm;
import com.khoacntt.quanlydetai.account.form.TaiKhoanForm;
import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.repository.TaiKhoanRepository;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.hoso.repository.GiangVienRepository;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaiKhoanServiceTest {
    @Mock TaiKhoanRepository repository;
    @Mock SinhVienRepository sv;
    @Mock GiangVienRepository gv;
    @Mock NguoiDungHienTai current;
    TaiKhoanService service;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    @BeforeEach void setup() { service = new TaiKhoanService(repository, sv, gv, encoder, current); }
    TaiKhoan tk(int id, VaiTro role) {
        var tk = TaiKhoan.taoMoi("user"+id, encoder.encode("OldPass123"), "Người dùng", "u"+id+"@example.edu.vn", Set.of(role));
        ReflectionTestUtils.setField(tk, "id", id); return tk;
    }
    TaiKhoanForm taoForm() {
        var f = new TaiKhoanForm(); f.setTenDangNhap("SV01"); f.setMatKhau("NewPass123");
        f.setHoTen("Sinh viên"); f.setEmail("SV01@EXAMPLE.EDU.VN"); f.setVaiTros(Set.of(VaiTro.SINH_VIEN)); return f;
    }
    SuaTaiKhoanForm suaForm(Set<VaiTro> roles) {
        var f = new SuaTaiKhoanForm(); f.setTenDangNhap("user2"); f.setTrangThai(TrangThaiTaiKhoan.HOAT_DONG); f.setHoTen("Họ tên mới"); f.setEmail("moi@example.edu.vn"); f.setVaiTros(roles); return f;
    }
    DoiMatKhauForm mkForm(String old, String confirm) {
        var f = new DoiMatKhauForm(); f.setMatKhauCu(old); f.setMatKhauMoi("NewPass123"); f.setXacNhanMatKhau(confirm); return f;
    }
    @Test void taoChuanHoaVaBamMatKhau() {
        service.tao(taoForm());
        ArgumentCaptor<TaiKhoan> cap = ArgumentCaptor.forClass(TaiKhoan.class);
        verify(repository).saveAndFlush(cap.capture());
        assertThat(cap.getValue().getTenDangNhap()).isEqualTo("sv01");
        assertThat(cap.getValue().getEmail()).isEqualTo("sv01@example.edu.vn");
        assertThat(encoder.matches("NewPass123", cap.getValue().getMatKhauBam())).isTrue();
    }
    @Test void trungTenDangNhapBiTuChoi() {
        when(repository.existsByTenDangNhap("sv01")).thenReturn(true);
        assertThatThrownBy(() -> service.tao(taoForm())).isInstanceOf(NghiepVuException.class);
        verify(repository, never()).saveAndFlush(any());
    }
    @Test void xungDotUniqueDuocChuyenThanhLoiNghiepVu() {
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        assertThatThrownBy(() -> service.tao(taoForm())).isInstanceOf(NghiepVuException.class);
    }
    @Test void matKhauUnicodeQua72ByteBiTuChoi() {
        var f = taoForm(); f.setMatKhau("ế".repeat(25));
        assertThatThrownBy(() -> service.tao(f)).isInstanceOf(NghiepVuException.class);
    }
    @Test void khongTuKhoaAdmin() {
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        assertThatThrownBy(() -> service.doiTrangThai(1, TrangThaiTaiKhoan.BI_KHOA)).isInstanceOf(NghiepVuException.class);
        verify(repository, never()).saveAndFlush(any());
    }
    @Test void khoaTaiKhoanKhac() {
        var target = tk(2, VaiTro.SINH_VIEN);
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(target));
        service.doiTrangThai(2, TrangThaiTaiKhoan.BI_KHOA);
        assertThat(target.getTrangThai()).isEqualTo(TrangThaiTaiKhoan.BI_KHOA);
    }
    @Test void khongTuBoQuyenAdmin() {
        var admin = tk(1, VaiTro.ADMIN);
        when(repository.timVaKhoa(1)).thenReturn(Optional.of(admin)); when(current.batBuoc()).thenReturn(admin);
        assertThatThrownBy(() -> service.sua(1, suaForm(Set.of(VaiTro.GIANG_VIEN)))).isInstanceOf(NghiepVuException.class);
    }
    @Test void khongBoVaiTroKhiDaCoHoSo() {
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk(2,VaiTro.SINH_VIEN)));
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN)); when(sv.existsByTaiKhoanId(2)).thenReturn(true);
        assertThatThrownBy(() -> service.sua(2, suaForm(Set.of(VaiTro.ADMIN)))).isInstanceOf(NghiepVuException.class);
    }
    @Test void suaEmailTrungBiTuChoi() {
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk(2,VaiTro.SINH_VIEN)));
        when(current.batBuoc()).thenReturn(tk(1,VaiTro.ADMIN)); when(repository.existsByEmailAndIdNot("moi@example.edu.vn",2)).thenReturn(true);
        assertThatThrownBy(() -> service.sua(2,suaForm(Set.of(VaiTro.SINH_VIEN)))).isInstanceOf(NghiepVuException.class);
    }
    @Test void doiMatKhauChiChoTaiKhoanHienTai() {
        var tk = tk(2,VaiTro.SINH_VIEN); when(current.batBuoc()).thenReturn(tk); when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk));
        service.doiMatKhau(mkForm("OldPass123", "NewPass123"));
        assertThat(encoder.matches("NewPass123",tk.getMatKhauBam())).isTrue();
    }
    @Test void saiMatKhauCuKhongLuu() {
        var tk = tk(2,VaiTro.SINH_VIEN); when(current.batBuoc()).thenReturn(tk); when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk));
        assertThatThrownBy(() -> service.doiMatKhau(mkForm("sai", "NewPass123"))).isInstanceOf(NghiepVuException.class);
        verify(repository,never()).saveAndFlush(any());
    }
    @Test void xacNhanMatKhauKhongKhop() {
        var tk = tk(2,VaiTro.SINH_VIEN); when(current.batBuoc()).thenReturn(tk); when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk));
        assertThatThrownBy(() -> service.doiMatKhau(mkForm("OldPass123", "khac"))).isInstanceOf(NghiepVuException.class);
    }

    SuaTaiKhoanForm datLaiForm(String password, String confirm) {
        var f = suaForm(Set.of(VaiTro.SINH_VIEN));
        f.setMatKhauMoi(password); f.setXacNhanMatKhau(confirm); return f;
    }

    @Test void khongTuXoaTaiKhoan() {
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        assertThatThrownBy(() -> service.xoa(1)).isInstanceOf(NghiepVuException.class);
        verify(repository, never()).delete(any());
    }

    @Test void xoaTaiKhoanChuaCoHoSo() {
        var target = tk(2, VaiTro.SINH_VIEN);
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(target));
        service.xoa(2);
        verify(repository).delete(target); verify(repository).flush();
    }

    @Test void khongXoaTaiKhoanCoHoSoSinhVien() {
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk(2, VaiTro.SINH_VIEN)));
        when(sv.existsByTaiKhoanId(2)).thenReturn(true);
        assertThatThrownBy(() -> service.xoa(2)).isInstanceOf(NghiepVuException.class).hasMessageContaining("hồ sơ");
        verify(repository, never()).delete(any());
    }

    @Test void khongXoaTaiKhoanCoHoSoGiangVien() {
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk(2, VaiTro.GIANG_VIEN)));
        when(gv.existsByTaiKhoanId(2)).thenReturn(true);
        assertThatThrownBy(() -> service.xoa(2)).isInstanceOf(NghiepVuException.class);
        verify(repository, never()).delete(any());
    }

    @Test void fkChanXoaDuocBaoLoiDeHieu() {
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(tk(2, VaiTro.CAN_BO_KHOA)));
        doThrow(new DataIntegrityViolationException("FK RESTRICT")).when(repository).flush();
        assertThatThrownBy(() -> service.xoa(2)).isInstanceOf(NghiepVuException.class).hasMessageContaining("khóa tài khoản");
    }

    @Test void adminDatLaiMatKhauKhongCanMatKhauCuVaKhongMoKhoaTaiKhoan() {
        var target = tk(2, VaiTro.SINH_VIEN);
        target.doiTrangThai(TrangThaiTaiKhoan.BI_KHOA);
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(target));
        var form = datLaiForm("NewPass123", "NewPass123"); form.setTrangThai(TrangThaiTaiKhoan.BI_KHOA);
        service.sua(2, form);
        assertThat(encoder.matches("NewPass123", target.getMatKhauBam())).isTrue();
        assertThat(encoder.matches("OldPass123", target.getMatKhauBam())).isFalse();
        assertThat(target.getTrangThai()).isEqualTo(TrangThaiTaiKhoan.BI_KHOA);
        verify(repository).saveAndFlush(target);
    }

    @Test void adminKhongTuDatLaiBoQuaMatKhauCu() {
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(1)).thenReturn(Optional.of(tk(1, VaiTro.ADMIN)));
        var form = datLaiForm("NewPass123", "NewPass123"); form.setVaiTros(Set.of(VaiTro.ADMIN));
        assertThatThrownBy(() -> service.sua(1, form))
                .isInstanceOf(NghiepVuException.class).hasMessageContaining("Đổi mật khẩu");
        verify(repository, never()).saveAndFlush(any());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"NewPass123,khac", "OldPass123,OldPass123", "123,123", "ếếếếếếếếếếếếếếếếếếếếếếếếế,ếếếếếếếếếếếếếếếếếếếếếếếếế"})
    void tuChoiMatKhauKhongHopLeKhiAdminDatLai(String password, String confirm) {
        var target = tk(2, VaiTro.SINH_VIEN);
        String oldHash = target.getMatKhauBam();
        when(current.batBuoc()).thenReturn(tk(1, VaiTro.ADMIN));
        when(repository.timVaKhoa(2)).thenReturn(Optional.of(target));
        assertThatThrownBy(() -> service.sua(2, datLaiForm(password, confirm))).isInstanceOf(NghiepVuException.class);
        assertThat(target.getMatKhauBam()).isEqualTo(oldHash);
        verify(repository, never()).saveAndFlush(any());
    }
}
