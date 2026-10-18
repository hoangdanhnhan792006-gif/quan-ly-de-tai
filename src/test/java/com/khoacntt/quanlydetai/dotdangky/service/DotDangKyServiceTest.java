package com.khoacntt.quanlydetai.dotdangky.service;

import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dotdangky.form.DotDangKyForm;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.dotdangky.repository.DotDangKyRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DotDangKyServiceTest {
    private DotDangKyRepository repository;
    private DotDangKyService service;
    private TaiKhoan truongKhoa;
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-01T03:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        repository = mock(DotDangKyRepository.class);
        NguoiDungHienTai nguoiDung = mock(NguoiDungHienTai.class);
        truongKhoa = TaiKhoan.taoMoi("truongkhoa", "hash", "Trưởng khoa", "tk@example.edu", Set.of(VaiTro.TRUONG_KHOA));
        when(nguoiDung.batBuoc()).thenReturn(truongKhoa);
        service = new DotDangKyService(repository, nguoiDung, clock);
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            DotDangKy d = i.getArgument(0);
            ReflectionTestUtils.setField(d, "id", 1);
            return d;
        });
    }

    private DotDangKyForm form() {
        DotDangKyForm f = new DotDangKyForm();
        f.setMaDot("dot01"); f.setTenDot("Đợt 1"); f.setLoai(LoaiDeTai.MON_HOC);
        f.setGvBatDau(LocalDateTime.of(2026, 10, 1, 9, 0));
        f.setGvKetThuc(LocalDateTime.of(2026, 10, 5, 17, 0));
        f.setSvBatDau(LocalDateTime.of(2026, 10, 5, 17, 0));
        f.setSvKetThuc(LocalDateTime.of(2026, 10, 6, 17, 0));
        f.setHanNopBaoCao(LocalDateTime.of(2026, 10, 20, 17, 0));
        return f;
    }

    private DotDangKy dot(TrangThaiDotDangKy trangThai) {
        DotDangKy d = new DotDangKy(truongKhoa);
        DotDangKyForm f = form();
        d.capNhat("DOT01", f.getTenDot(), f.getLoai(), ThoiGian.sangUtc(f.getGvBatDau()),
                ThoiGian.sangUtc(f.getGvKetThuc()), ThoiGian.sangUtc(f.getSvBatDau()),
                ThoiGian.sangUtc(f.getSvKetThuc()), ThoiGian.sangUtc(f.getHanNopBaoCao()), null, null);
        d.chuyenTrangThai(trangThai);
        when(repository.timVaKhoa(1)).thenReturn(Optional.of(d));
        return d;
    }

    @Test
    void choPhepHaiKhoangTiepNhauVaLuuUtc() {
        assertEquals(1, service.tao(form()));
        var captor = org.mockito.ArgumentCaptor.forClass(DotDangKy.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals("DOT01", captor.getValue().getMaDot());
        assertEquals(LocalDateTime.of(2026, 10, 1, 2, 0), captor.getValue().getGvBatDau());
        assertEquals(TrangThaiDotDangKy.NHAP, captor.getValue().getTrangThai());
    }

    @Test
    void chanHaiGiaiDoanChongNhau() {
        var f = form(); f.setSvBatDau(f.getGvKetThuc().minusMinutes(1));
        assertThrows(NghiepVuException.class, () -> service.tao(f));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void chanKhoangThoiGianDaoNguoc() {
        var f = form(); f.setGvKetThuc(f.getGvBatDau());
        assertThrows(NghiepVuException.class, () -> service.tao(f));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void tlcnBatBuocHanDiem() {
        var f = form(); f.setLoai(LoaiDeTai.TLCN);
        assertThrows(NghiepVuException.class, () -> service.tao(f));
        f.setHanNopDiemPb(f.getHanNopBaoCao().plusDays(1));
        assertEquals(1, service.tao(f));
    }

    @Test
    void kltnBatBuocNgayHoiDongVaKhongTruocPhanBien() {
        var f = form(); f.setLoai(LoaiDeTai.KLTN); f.setHanNopDiemPb(f.getHanNopBaoCao().plusDays(1));
        assertThrows(NghiepVuException.class, () -> service.tao(f));
        f.setNgayBaoCaoHd(f.getHanNopBaoCao().toLocalDate());
        assertThrows(NghiepVuException.class, () -> service.tao(f));
        f.setNgayBaoCaoHd(f.getHanNopDiemPb().toLocalDate().plusDays(1));
        assertEquals(1, service.tao(f));
    }

    @Test
    void nckhKhongDungNgayHoiDongCapDot() {
        var f = form(); f.setLoai(LoaiDeTai.NCKH); f.setNgayBaoCaoHd(LocalDate.of(2026, 10, 25));
        assertThrows(NghiepVuException.class, () -> service.tao(f));
    }

    @Test
    void monHocKhongNhanHanDiemPb() {
        var f = form(); f.setHanNopDiemPb(f.getHanNopBaoCao().plusDays(1));
        assertThrows(NghiepVuException.class, () -> service.tao(f));
    }

    @Test
    void chanBaoCaoTruocDongDangKyVaDiemTruocBaoCao() {
        var f = form(); f.setHanNopBaoCao(f.getSvKetThuc().minusMinutes(1));
        assertThrows(NghiepVuException.class, () -> service.tao(f));
        var invalid = form(); invalid.setLoai(LoaiDeTai.TLCN); invalid.setHanNopDiemPb(invalid.getHanNopBaoCao().minusMinutes(1));
        assertThrows(NghiepVuException.class, () -> service.tao(invalid));
    }

    @Test
    void khongSuaDotDaTrienKhai() {
        dot(TrangThaiDotDangKy.TRIEN_KHAI);
        assertThrows(NghiepVuException.class, () -> service.sua(1, form()));
    }

    @Test
    void trienKhaiRoiKetThucKhongMoLai() {
        DotDangKy d = dot(TrangThaiDotDangKy.NHAP);
        service.trienKhai(1);
        assertEquals(TrangThaiDotDangKy.TRIEN_KHAI, d.getTrangThai());
        service.ketThuc(1);
        assertEquals(TrangThaiDotDangKy.KET_THUC, d.getTrangThai());
        assertThrows(NghiepVuException.class, () -> service.trienKhai(1));
    }

    @Test
    void huyDotKhongCoDangKyVaChanDotDaCoNhom() {
        DotDangKy d = dot(TrangThaiDotDangKy.TRIEN_KHAI);
        when(repository.demDangKyGiuCho(1)).thenReturn(1L);
        assertThrows(NghiepVuException.class, () -> service.huy(1));
        assertEquals(TrangThaiDotDangKy.TRIEN_KHAI, d.getTrangThai());
        when(repository.demDangKyGiuCho(1)).thenReturn(0L);
        service.huy(1);
        assertEquals(TrangThaiDotDangKy.HUY, d.getTrangThai());
    }

    @Test
    void bienThoiGianMoBaoGomBatDauKhongBaoGomKetThuc() {
        DotDangKy d = dot(TrangThaiDotDangKy.TRIEN_KHAI);
        assertFalse(d.dangMoDeXuat(d.getGvBatDau().minusNanos(1)));
        assertTrue(d.dangMoDeXuat(d.getGvBatDau()));
        assertFalse(d.dangMoDeXuat(d.getGvKetThuc()));
    }

    @Test
    void chanMaTrung() {
        when(repository.existsByMaDot("DOT01")).thenReturn(true);
        assertThrows(NghiepVuException.class, () -> service.tao(form()));
    }
}
