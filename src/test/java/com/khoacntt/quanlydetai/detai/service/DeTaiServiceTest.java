package com.khoacntt.quanlydetai.detai.service;

import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.detai.form.*;
import com.khoacntt.quanlydetai.detai.model.*;
import com.khoacntt.quanlydetai.detai.repository.*;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.dotdangky.repository.DotDangKyRepository;
import com.khoacntt.quanlydetai.hoso.model.*;
import com.khoacntt.quanlydetai.hoso.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeTaiServiceTest {
    private DeTaiRepository repository;
    private HuongDanRepository huongDanRepository;
    private DotDangKyRepository dotRepository;
    private GiangVienRepository giangVienRepository;
    private NguoiDungHienTai nguoiDung;
    private DeTaiService service;
    private DotDangKy dot;
    private DeTai deTai;
    private GiangVien gv;
    private TaiKhoan taiKhoan;
    private BoMon boMon;
    private final LocalDateTime now = LocalDateTime.of(2026, 10, 1, 3, 0);

    @BeforeEach
    void setup() {
        repository = mock(DeTaiRepository.class);
        huongDanRepository = mock(HuongDanRepository.class);
        dotRepository = mock(DotDangKyRepository.class);
        giangVienRepository = mock(GiangVienRepository.class);
        BoMonRepository boMonRepository = mock(BoMonRepository.class);
        nguoiDung = mock(NguoiDungHienTai.class);
        Clock clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new DeTaiService(repository, huongDanRepository, dotRepository, boMonRepository,
                giangVienRepository, nguoiDung, clock);
        taiKhoan = taiKhoan(1, VaiTro.GIANG_VIEN);
        taiKhoan.capNhatThongTin(taiKhoan.getHoTen(), taiKhoan.getEmail(), Set.of(VaiTro.GIANG_VIEN, VaiTro.TRUONG_BO_MON));
        when(nguoiDung.batBuoc()).thenReturn(taiKhoan);
        boMon = new BoMon("CNPM", "Công nghệ phần mềm", (byte) 1);
        ReflectionTestUtils.setField(boMon, "id", 1);
        gv = new GiangVien(taiKhoan, "GV01", boMon, "Thạc sĩ");
        ReflectionTestUtils.setField(gv, "id", 1);
        when(boMonRepository.findById(1)).thenReturn(Optional.of(boMon));
        when(giangVienRepository.findByTaiKhoanId(1)).thenReturn(Optional.of(gv));
        when(giangVienRepository.findById(1)).thenReturn(Optional.of(gv));
        dot = new DotDangKy(taiKhoan);
        ReflectionTestUtils.setField(dot, "id", 1);
        dot.capNhat("DOT01", "Đợt 1", LoaiDeTai.MON_HOC, now.minusDays(1), now.plusDays(1),
                now, now.plusDays(2), now.plusDays(20), null, null);
        dot.chuyenTrangThai(TrangThaiDotDangKy.TRIEN_KHAI);
        when(dotRepository.timVaKhoa(1)).thenReturn(Optional.of(dot));
        deTai = new DeTai(dot, gv);
        ReflectionTestUtils.setField(deTai, "id", 10);
        deTai.capNhat(boMon, "DT01", "Đề tài", "Nội dung", null, null);
        when(repository.findById(10)).thenReturn(Optional.of(deTai));
        when(repository.timDotId(10)).thenReturn(Optional.of(1));
        when(repository.timVaKhoa(10)).thenReturn(Optional.of(deTai));
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            DeTai d = i.getArgument(0); ReflectionTestUtils.setField(d, "id", 10); return d;
        });
    }

    private TaiKhoan taiKhoan(int id, VaiTro role) {
        TaiKhoan t = TaiKhoan.taoMoi("user" + id, "hash", "Người " + id, "u" + id + "@example.edu", Set.of(role));
        ReflectionTestUtils.setField(t, "id", id);
        return t;
    }

    @Test
    void truongBoMonKhongDuyetDeTaiBoMonKhac() {
        var other = new BoMon("KHAC", "Bộ môn khác", (byte)1);
        ReflectionTestUtils.setField(other,"id",2);
        gv.capNhat("GV01",other,null);
        deTai.guiDuyet(); var f = new DuyetDeTaiForm(); f.setChapThuan(true);
        assertThrows(AccessDeniedException.class, () -> service.duyet(10,f));
        assertThrows(AccessDeniedException.class, () -> service.phanCong(10,huongDan(1)));
        assertFalse(service.chiTiet(10).coTheDuyet());
    }
    @Test
    void chiXoaNhapCuaChinhMinh() {
        service.xoa(10); verify(repository).delete(deTai);
        deTai.guiDuyet();
        assertThrows(NghiepVuException.class, () -> service.xoa(10));
    }
    @Test
    void khoaKhongTuDuyetThayBoMon() {
        when(nguoiDung.batBuoc()).thenReturn(taiKhoan(9,VaiTro.TRUONG_KHOA));
        deTai.guiDuyet(); var f = new DuyetDeTaiForm(); f.setChapThuan(true);
        assertThrows(AccessDeniedException.class, () -> service.duyet(10,f));
    }

    private DeTaiForm form() {
        DeTaiForm f = new DeTaiForm(); f.setDotId(1); f.setBoMonId(1);
        f.setMaDeTai("dt01"); f.setTenDeTai("Đề tài"); f.setNoiDung("Nội dung");
        return f;
    }

    private void daDuyet() {
        deTai.guiDuyet(); deTai.duyet(true, taiKhoan(9, VaiTro.CAN_BO_KHOA), "Đạt", now);
    }

    private HuongDanForm huongDan(Integer... ids) {
        HuongDanForm f = new HuongDanForm(); f.setGiangVienIds(new HashSet<>(Arrays.asList(ids))); return f;
    }

    @Test
    void taoNhapDungNguoiDeXuatKhongTuThemGvhd() {
        assertEquals(10, service.tao(form()));
        var captor = org.mockito.ArgumentCaptor.forClass(DeTai.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(gv, captor.getValue().getGvDeXuat());
        assertEquals("DT01", captor.getValue().getMaDeTai());
        assertEquals(TrangThaiDeTai.NHAP, captor.getValue().getTrangThai());
        verifyNoInteractions(huongDanRepository);
    }

    @Test
    void thieuHoSoGiangVienKhongDeXuatDuoc() {
        when(giangVienRepository.findByTaiKhoanId(1)).thenReturn(Optional.empty());
        assertThrows(NghiepVuException.class, () -> service.tao(form()));
    }

    @Test
    void hetHanVaDotDongKhongNhanDeXuat() {
        ReflectionTestUtils.setField(dot, "gvKetThuc", now);
        assertThrows(NghiepVuException.class, () -> service.tao(form()));
        dot.chuyenTrangThai(TrangThaiDotDangKy.KET_THUC);
        assertThrows(NghiepVuException.class, () -> service.guiDuyet(10));
    }

    @Test
    void boMonNgungHoatDongKhongDeXuatDuoc() {
        boMon.capNhat("CNPM", "CNPM", (byte) 0);
        assertThrows(NghiepVuException.class, () -> service.tao(form()));
    }

    @Test
    void maTrungBiChan() {
        when(repository.existsByMaDeTai("DT01")).thenReturn(true);
        assertThrows(NghiepVuException.class, () -> service.tao(form()));
    }

    @Test
    void nguoiKhacKhongSuaHoacGuiDeTai() {
        when(nguoiDung.batBuoc()).thenReturn(taiKhoan(2, VaiTro.GIANG_VIEN));
        assertThrows(AccessDeniedException.class, () -> service.sua(10, form()));
        assertThrows(AccessDeniedException.class, () -> service.guiDuyet(10));
    }

    @Test
    void svKhongXemBanNhapBangCachDoanId() {
        when(nguoiDung.batBuoc()).thenReturn(taiKhoan(3, VaiTro.SINH_VIEN));
        assertThrows(AccessDeniedException.class, () -> service.chiTiet(10));
    }

    @Test
    void guiDuyetKhoaNoiDungVaKhongChoGuiLanHai() {
        service.guiDuyet(10);
        assertEquals(TrangThaiDeTai.CHO_DUYET, deTai.getTrangThai());
        assertThrows(NghiepVuException.class, () -> service.sua(10, form()));
        assertThrows(NghiepVuException.class, () -> service.guiDuyet(10));
    }

    @Test
    void tuChoiCanLyDoVaDuocSuaGuiLai() {
        deTai.guiDuyet();
        DuyetDeTaiForm f = new DuyetDeTaiForm(); f.setChapThuan(false);
        assertThrows(NghiepVuException.class, () -> service.duyet(10, f));
        f.setNhanXet("Cần bổ sung"); service.duyet(10, f);
        assertEquals(TrangThaiDeTai.TU_CHOI, deTai.getTrangThai());
        service.sua(10, form()); service.guiDuyet(10);
        assertNull(deTai.getNhanXetDuyet()); assertNull(deTai.getNguoiDuyet());
    }

    @Test
    void khongChuyenDotKhiSua() {
        var f = form(); f.setDotId(2);
        assertThrows(NghiepVuException.class, () -> service.sua(10, f));
    }

    @Test
    void chiDuyetDeTaiChoDuyet() {
        DuyetDeTaiForm f = new DuyetDeTaiForm(); f.setChapThuan(true);
        assertThrows(NghiepVuException.class, () -> service.duyet(10, f));
        deTai.guiDuyet(); service.duyet(10, f);
        assertEquals(TrangThaiDeTai.DA_DUYET, deTai.getTrangThai());
    }

    @Test
    void chuaCoHuongDanKhongCongBo() {
        daDuyet();
        assertThrows(NghiepVuException.class, () -> service.congBo(10));
        assertEquals(TrangThaiDeTai.DA_DUYET, deTai.getTrangThai());
    }

    @Test
    void phanCongTuMotDenHaiNguoi() {
        daDuyet();
        assertThrows(NghiepVuException.class, () -> service.phanCong(10, huongDan()));
        assertThrows(NghiepVuException.class, () -> service.phanCong(10, huongDan(1, 2, 3)));
        service.phanCong(10, huongDan(1));
        verify(huongDanRepository).save(any(HuongDan.class));
    }

    @Test
    void gvDangChamKhongDuocHuongDan() {
        daDuyet(); when(repository.demPhanCongCham(10, 1)).thenReturn(1L);
        assertThrows(NghiepVuException.class, () -> service.phanCong(10, huongDan(1)));
        verify(huongDanRepository, never()).save(any());
    }

    @Test
    void thanhVienHoiDongKhongDuocHuongDan() {
        daDuyet(); when(repository.demThanhVienHoiDong(10, 1)).thenReturn(1L);
        assertThrows(NghiepVuException.class, () -> service.phanCong(10, huongDan(1)));
    }

    @Test
    void gvBiKhoaKhongDuocPhanCong() {
        daDuyet(); taiKhoan.doiTrangThai(TrangThaiTaiKhoan.BI_KHOA);
        assertThrows(NghiepVuException.class, () -> service.phanCong(10, huongDan(1)));
    }

    @Test
    void congBoSauKhiDuyetVaDuHuongDanChoSvXem() {
        daDuyet();
        when(huongDanRepository.findByDeTaiIdOrderByGiangVienMaGvAsc(10))
                .thenReturn(List.of(new HuongDan(deTai, gv, taiKhoan, now)));
        service.congBo(10);
        assertEquals(TrangThaiDeTai.DA_CONG_BO, deTai.getTrangThai());
        assertNotNull(deTai.getCongBoLuc());
        when(nguoiDung.batBuoc()).thenReturn(taiKhoan(3, VaiTro.SINH_VIEN));
        assertEquals("DT01", service.chiTiet(10).maDeTai());
        assertFalse(service.chiTiet(10).coThePhanCong());
    }

    @Test
    void dotDaHuyKhongConTrongDanhSachCongBo() {
        daDuyet(); deTai.congBo(taiKhoan, now); dot.chuyenTrangThai(TrangThaiDotDangKy.HUY);
        when(repository.daCongBo(null, null)).thenReturn(List.of());
        assertTrue(service.daCongBo(null, null).isEmpty());
        verify(repository).daCongBo(null, null);
    }
}
