package com.khoacntt.quanlydetai.hoso.service;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.repository.TaiKhoanRepository;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.hoso.form.GiangVienForm;
import com.khoacntt.quanlydetai.hoso.form.SinhVienForm;
import com.khoacntt.quanlydetai.hoso.model.BoMon;
import com.khoacntt.quanlydetai.hoso.model.SinhVien;
import com.khoacntt.quanlydetai.hoso.repository.BoMonRepository;
import com.khoacntt.quanlydetai.hoso.repository.GiangVienRepository;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoSoServiceTest {
    @Mock SinhVienRepository sv;
    @Mock GiangVienRepository gv;
    @Mock BoMonRepository bm;
    @Mock TaiKhoanRepository tk;
    @Mock NguoiDungHienTai current;
    @InjectMocks HoSoService service;
    TaiKhoan account(int id,VaiTro role) {
        var a=TaiKhoan.taoMoi("user"+id,"hash","Họ tên","u"+id+"@example.edu.vn",Set.of(role));
        ReflectionTestUtils.setField(a,"id",id);return a;
    }
    SinhVienForm formSv() { var f=new SinhVienForm(); f.setTaiKhoanId(2); f.setMaSv("sv01"); f.setLop("CNTT1"); f.setKhoaHoc("2024"); return f; }
    GiangVienForm formGv() { var f=new GiangVienForm(); f.setTaiKhoanId(3); f.setMaGv("gv01"); f.setBoMonId(1); return f; }
    @Test void taoSinhVienVoiTaiKhoanDungVaiTro() {
        when(tk.timVaKhoa(2)).thenReturn(Optional.of(account(2,VaiTro.SINH_VIEN)));
        service.taoSinhVien(formSv());
        var cap=ArgumentCaptor.forClass(SinhVien.class);verify(sv).saveAndFlush(cap.capture());
        assertThat(cap.getValue().getMaSv()).isEqualTo("SV01");
    }
    @Test void taoSinhVienSaiVaiTroBiChan() {
        when(tk.timVaKhoa(2)).thenReturn(Optional.of(account(2,VaiTro.GIANG_VIEN)));
        assertThatThrownBy(()->service.taoSinhVien(formSv())).isInstanceOf(NghiepVuException.class);
    }
    @Test void moiTaiKhoanChiCoMotHoSoSinhVien() {
        when(tk.timVaKhoa(2)).thenReturn(Optional.of(account(2,VaiTro.SINH_VIEN)));when(sv.existsByTaiKhoanId(2)).thenReturn(true);
        assertThatThrownBy(()->service.taoSinhVien(formSv())).isInstanceOf(NghiepVuException.class);
    }
    @Test void maSinhVienTrungBiChan() {
        when(tk.timVaKhoa(2)).thenReturn(Optional.of(account(2,VaiTro.SINH_VIEN)));when(sv.existsByMaSv("SV01")).thenReturn(true);
        assertThatThrownBy(()->service.taoSinhVien(formSv())).isInstanceOf(NghiepVuException.class);
    }
    @Test void khongDoiChuHoSo() {
        when(tk.timVaKhoa(2)).thenReturn(Optional.of(account(2,VaiTro.SINH_VIEN)));
        when(sv.findById(8)).thenReturn(Optional.of(new SinhVien(account(9,VaiTro.SINH_VIEN),"SV09",null,null)));
        assertThatThrownBy(()->service.suaSinhVien(8,formSv())).isInstanceOf(NghiepVuException.class);
    }
    @Test void taoGiangVienVoiBoMonHoatDong() {
        when(tk.timVaKhoa(3)).thenReturn(Optional.of(account(3,VaiTro.GIANG_VIEN)));
        when(bm.findById(1)).thenReturn(Optional.of(new BoMon("CNPM","Công nghệ phần mềm",(byte)1)));
        service.taoGiangVien(formGv());verify(gv).saveAndFlush(any());
    }
    @Test void boMonNgungHoatDongKhongNhanGiangVienMoi() {
        when(tk.timVaKhoa(3)).thenReturn(Optional.of(account(3,VaiTro.GIANG_VIEN)));
        when(bm.findById(1)).thenReturn(Optional.of(new BoMon("CNPM","Công nghệ phần mềm",(byte)0)));
        assertThatThrownBy(()->service.taoGiangVien(formGv())).isInstanceOf(NghiepVuException.class);
    }
    @Test void hoSoCaNhanLayIdTuPhienDangNhap() {
        var a=account(2,VaiTro.SINH_VIEN); when(current.batBuoc()).thenReturn(a);
        when(sv.findByTaiKhoanId(2)).thenReturn(Optional.of(new SinhVien(a,"SV01","CNTT1",null)));
        var dto=service.caNhan();assertThat(dto.sinhVien().maSv()).isEqualTo("SV01");
        verify(gv).findByTaiKhoanId(2);verify(sv,never()).findByTaiKhoanId(3);
    }
}
