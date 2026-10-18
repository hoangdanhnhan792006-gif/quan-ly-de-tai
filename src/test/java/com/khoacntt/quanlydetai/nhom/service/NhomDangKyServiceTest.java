package com.khoacntt.quanlydetai.nhom.service;

import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.dangky.form.*;
import com.khoacntt.quanlydetai.dangky.model.*;
import com.khoacntt.quanlydetai.dangky.repository.DangKyRepository;
import com.khoacntt.quanlydetai.dangky.service.DangKyService;
import com.khoacntt.quanlydetai.detai.model.*;
import com.khoacntt.quanlydetai.detai.repository.*;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.dotdangky.repository.DotDangKyRepository;
import com.khoacntt.quanlydetai.hoso.model.*;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;
import com.khoacntt.quanlydetai.nhom.form.*;
import com.khoacntt.quanlydetai.nhom.model.*;
import com.khoacntt.quanlydetai.nhom.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NhomDangKyServiceTest {
    NhomRepository nr;
    ThanhVienNhomRepository tr;
    LoiMoiNhomRepository lr;
    SinhVienRepository sr;
    DotDangKyRepository dr;
    DeTaiRepository der;
    HuongDanRepository hr;
    DangKyRepository dkr;
    NguoiDungHienTai actor;
    NhomService service;
    DangKyService dangKyService;
    QuyTacNhom rules;
    DotDangKy dot;
    Nhom nhom;
    SinhVien sv1, sv2, sv3;
    ThanhVienNhom leader, member;
    DeTai topic;
    TaiKhoan tk;
    LocalDateTime now = LocalDateTime.of(2026, 10, 1, 3, 0);

    @BeforeEach void setup() {
        nr = mock(NhomRepository.class); tr = mock(ThanhVienNhomRepository.class);
        lr = mock(LoiMoiNhomRepository.class); sr = mock(SinhVienRepository.class);
        dr = mock(DotDangKyRepository.class); der = mock(DeTaiRepository.class);
        hr = mock(HuongDanRepository.class); dkr = mock(DangKyRepository.class);
        actor = mock(NguoiDungHienTai.class);
        Clock clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        rules = new QuyTacNhom(nr, tr, dkr, dr, der, sr, actor, clock);
        service = new NhomService(nr, tr, lr, sr, dr, dkr, rules, actor, clock);
        dangKyService = new DangKyService(dkr, nr, der, hr, lr, rules, actor, clock);
        tk = account(1, VaiTro.SINH_VIEN);
        sv1 = withId(new SinhVien(tk, "SV01", null, null), 1);
        sv2 = withId(new SinhVien(account(2, VaiTro.SINH_VIEN), "SV02", null, null), 2);
        sv3 = withId(new SinhVien(account(4, VaiTro.SINH_VIEN), "SV03", null, null), 4);
        when(actor.batBuoc()).thenReturn(tk);
        when(sr.findByTaiKhoanId(1)).thenReturn(Optional.of(sv1));
        when(sr.findByTaiKhoanId(2)).thenReturn(Optional.of(sv2));
        when(sr.findByMaSv("SV02")).thenReturn(Optional.of(sv2));
        dot = withId(new DotDangKy(tk), 1);
        dot.capNhat("D1", "Đợt 1", LoaiDeTai.MON_HOC, now.minusDays(1), now,
                now, now.plusDays(2), now.plusDays(20), null, null);
        dot.chuyenTrangThai(TrangThaiDotDangKy.TRIEN_KHAI);
        when(dr.timVaKhoa(1)).thenReturn(Optional.of(dot));
        nhom = withId(new Nhom(dot, sv1, "N1", "Nhóm 1"), 10);
        when(nr.timDotId(10)).thenReturn(Optional.of(1));
        when(nr.timVaKhoa(10)).thenReturn(Optional.of(nhom));
        when(nr.findById(10)).thenReturn(Optional.of(nhom));
        leader = new ThanhVienNhom(nhom, sv1, true);
        member = new ThanhVienNhom(nhom, sv2, false);
        when(tr.findByNhomIdAndSinhVienId(10, 1)).thenReturn(Optional.of(leader));
        when(tr.findByNhomIdAndSinhVienId(10, 2)).thenReturn(Optional.of(member));
        when(tr.findByNhomIdOrderBySinhVienMaSvAsc(10)).thenReturn(List.of(leader, member, new ThanhVienNhom(nhom, sv3, false)));
        when(tr.countByNhomId(10)).thenReturn(2L);
        BoMon bm = withId(new BoMon("BM", "Bộ môn", (byte) 1), 1);
        GiangVien gv = withId(new GiangVien(account(3, VaiTro.GIANG_VIEN), "GV01", bm, null), 1);
        topic = withId(new DeTai(dot, gv), 20);
        topic.capNhat(bm, "DT1", "Đề tài", "Nội dung", null, null);
        topic.guiDuyet(); topic.duyet(true, tk, null, now); topic.congBo(tk, now);
        when(der.timDotId(20)).thenReturn(Optional.of(1));
        when(der.timVaKhoa(20)).thenReturn(Optional.of(topic));
        when(der.findById(20)).thenReturn(Optional.of(topic));
        when(hr.findByDeTaiIdOrderByGiangVienMaGvAsc(20)).thenReturn(List.of(new HuongDan(topic, gv, tk, now)));
        when(dkr.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }
    private <T> T withId(T obj, int id) { ReflectionTestUtils.setField(obj, "id", id); return obj; }
    private TaiKhoan account(int id, VaiTro role) {
        return withId(TaiKhoan.taoMoi("u" + id, "hash", "Người " + id, "u" + id + "@test.example", Set.of(role)), id);
    }
    private DangKyForm dangKyForm(int topicId) { var f = new DangKyForm(); f.setDeTaiId(topicId); return f; }
    private DuyetDangKyForm duyetForm(boolean yes) {
        var f = new DuyetDangKyForm(); f.setChapThuan(yes); f.setDeTaiId(20); return f;
    }
    private DangKy pending() {
        DangKy d = withId(new DangKy(nhom), 30); d.dangKy(topic, sv1, now);
        when(dkr.findByNhomId(10)).thenReturn(Optional.of(d));
        when(dkr.timNhomId(30)).thenReturn(Optional.of(10)); return d;
    }
    private LoiMoiNhom invitation() {
        LoiMoiNhom l = withId(new LoiMoiNhom(nhom, sv2, sv1, "Mời bạn", now), 40);
        when(lr.timNhomId(40)).thenReturn(Optional.of(10)); when(lr.findById(40)).thenReturn(Optional.of(l)); return l;
    }
    private MoiThanhVienForm moiForm() { var f = new MoiThanhVienForm(); f.setMaSv("sv02"); return f; }

    @Test void nhomHaiNguoiChuaDuocDangKy() {
        when(tr.findByNhomIdOrderBySinhVienMaSvAsc(10)).thenReturn(List.of(leader,member));
        assertThrows(NghiepVuException.class, () -> dangKyService.dangKy(10,dangKyForm(20)));
        verify(dkr,never()).saveAndFlush(any());
    }
    @Test void nhomNamNguoiDuocDangKySauNguoiBiChan() {
        var sv4=withId(new SinhVien(account(5,VaiTro.SINH_VIEN),"SV04",null,null),5);
        var sv5=withId(new SinhVien(account(6,VaiTro.SINH_VIEN),"SV05",null,null),6);
        var ds=new ArrayList<>(List.of(leader,member,new ThanhVienNhom(nhom,sv3,false),
            new ThanhVienNhom(nhom,sv4,false),new ThanhVienNhom(nhom,sv5,false)));
        when(tr.findByNhomIdOrderBySinhVienMaSvAsc(10)).thenReturn(ds);
        assertDoesNotThrow(() -> rules.kiemTraThanhVienHopLe(nhom));
        ds.add(new ThanhVienNhom(nhom,withId(new SinhVien(account(7,VaiTro.SINH_VIEN),"SV06",null,null),7),false));
        assertThrows(NghiepVuException.class, () -> rules.kiemTraThanhVienHopLe(nhom));
    }
    @Test void taoNhomTaoDungMotTruongVaChuanHoaMa() {
        var f = new NhomForm(); f.setDotId(1); f.setMaNhom("abc"); f.setTenNhom(" Nhóm ");
        when(nr.saveAndFlush(any())).thenAnswer(i -> withId(i.getArgument(0), 11));
        assertEquals(11, service.tao(f));
        verify(tr).saveAndFlush(argThat(t -> t.isTruongNhom() && t.getSinhVien() == sv1
                && t.getNhom().getMaNhom().equals("ABC")));
    }
    @Test void motSinhVienKhongTaoHaiNhomCungDot() {
        when(tr.existsBySinhVienIdAndDotId(1, 1)).thenReturn(true);
        var f = new NhomForm(); f.setDotId(1); f.setMaNhom("N2");
        assertThrows(NghiepVuException.class, () -> service.tao(f));
        verify(nr, never()).saveAndFlush(any());
    }
    @Test void canHoSoSinhVien() {
        when(sr.findByTaiKhoanId(1)).thenReturn(Optional.empty());
        assertThrows(NghiepVuException.class, () -> service.cuaToi());
    }
    @Test void mocMoBaoGomMocDongKhongBaoGom() {
        assertTrue(rules.trongHan(dot));
        ReflectionTestUtils.setField(dot, "svKetThuc", now);
        assertFalse(rules.trongHan(dot));
    }
    @Test void khongMoiKhiNhomDuNamNguoi() {
        when(tr.countByNhomId(10)).thenReturn(5L);
        assertThrows(NghiepVuException.class, () -> service.moi(10, moiForm()));
        verify(lr, never()).saveAndFlush(any());
    }
    @Test void khongMoiSinhVienDaCoNhom() {
        when(tr.existsBySinhVienIdAndDotId(2, 1)).thenReturn(true);
        assertThrows(NghiepVuException.class, () -> service.moi(10, moiForm()));
    }
    @Test void khongGuiTrungLoiMoiDangCho() {
        when(lr.existsByNhomIdAndSinhVienDuocMoiIdAndTrangThai(10, 2, TrangThaiLoiMoiNhom.CHO_PHAN_HOI)).thenReturn(true);
        assertThrows(NghiepVuException.class, () -> service.moi(10, moiForm()));
    }
    @Test void chiNguoiNhanDuocChapNhan() {
        invitation();
        assertThrows(AccessDeniedException.class, () -> service.phanHoi(40, true));
        verify(tr, never()).saveAndFlush(any());
    }
    @Test void chapNhanMoiThemThanhVien() {
        var l = invitation(); when(actor.batBuoc()).thenReturn(sv2.getTaiKhoan());
        service.phanHoi(40, true);
        assertEquals(TrangThaiLoiMoiNhom.DA_CHAP_NHAN, l.getTrangThai());
        verify(tr).saveAndFlush(argThat(t -> t.getSinhVien() == sv2 && !t.isTruongNhom()));
    }
    @Test void tuChoiKhongThemThanhVien() {
        var l = invitation(); when(actor.batBuoc()).thenReturn(sv2.getTaiKhoan());
        service.phanHoi(40, false);
        assertEquals(TrangThaiLoiMoiNhom.TU_CHOI, l.getTrangThai());
        verify(tr, never()).saveAndFlush(any());
    }
    @Test void khongChapNhanLoiMoiDaThuHoi() {
        var l = invitation(); l.ketThuc(TrangThaiLoiMoiNhom.DA_THU_HOI, null, now);
        when(actor.batBuoc()).thenReturn(sv2.getTaiKhoan());
        assertThrows(NghiepVuException.class, () -> service.phanHoi(40, true));
    }
    @Test void doiTruongBoCoCuTruocKhiDatCoMoi() {
        doAnswer(i -> { assertFalse(leader.isTruongNhom() && member.isTruongNhom()); return null; }).when(tr).flush();
        service.doiTruong(10, 2);
        assertFalse(leader.isTruongNhom()); assertTrue(member.isTruongNhom());
        verify(tr, times(2)).flush();
    }
    @Test void truongKhongDuocTuRoiNhom() {
        assertThrows(NghiepVuException.class, () -> service.roiNhom(10));
        verify(tr, never()).delete(any());
    }
    @Test void thanhVienDuocRoiTruocChapThuan() {
        when(actor.batBuoc()).thenReturn(sv2.getTaiKhoan());
        service.roiNhom(10); verify(tr).delete(member);
    }
    @Test void chiTruongDuocDangKy() {
        when(actor.batBuoc()).thenReturn(sv2.getTaiKhoan());
        assertThrows(AccessDeniedException.class, () -> dangKyService.dangKy(10, dangKyForm(20)));
        verify(dkr, never()).saveAndFlush(any());
    }
    @Test void dangKyChoDuyetVaKhoaTheoThuTu() {
        dangKyService.dangKy(10, dangKyForm(20));
        var order = inOrder(dr, der, nr);
        order.verify(dr).timVaKhoa(1); order.verify(der).timVaKhoa(20); order.verify(nr).timVaKhoa(10);
        verify(dkr).saveAndFlush(argThat(d -> d.getTrangThai() == TrangThaiDangKy.CHO_DUYET));
        assertEquals(TrangThaiNhom.DA_CHOT, nhom.getTrangThai());
    }
    @Test void doiSangDeTaiHetChoKhongSuaDangKyCu() {
        DangKy old = pending();
        DeTai other = withId(new DeTai(dot, topic.getGvDeXuat()), 21);
        other.congBo(tk, now);
        when(der.timDotId(21)).thenReturn(Optional.of(1)); when(der.timVaKhoa(21)).thenReturn(Optional.of(other));
        when(der.findById(21)).thenReturn(Optional.of(other));
        var guides = hr.findByDeTaiIdOrderByGiangVienMaGvAsc(20);
        when(hr.findByDeTaiIdOrderByGiangVienMaGvAsc(21)).thenReturn(guides);
        when(dkr.existsByDeTaiIdAndTrangThaiInAndNhomIdNot(eq(21), any(), eq(10))).thenReturn(true);
        assertThrows(NghiepVuException.class, () -> dangKyService.dangKy(10, dangKyForm(21)));
        assertSame(topic, old.getDeTai()); assertEquals(TrangThaiDangKy.CHO_DUYET, old.getTrangThai());
    }
    @Test void khongDangKyDeTaiKhacDot() {
        when(der.timDotId(20)).thenReturn(Optional.of(2));
        assertThrows(NghiepVuException.class, () -> dangKyService.dangKy(10, dangKyForm(20)));
        verify(der, never()).timVaKhoa(any());
    }
    @Test void khongDangKyDeTaiChuaCongBo() {
        ReflectionTestUtils.setField(topic, "trangThai", TrangThaiDeTai.DA_DUYET);
        assertThrows(NghiepVuException.class, () -> dangKyService.dangKy(10, dangKyForm(20)));
    }
    @Test void khongDangKyKhiThieuHuongDan() {
        when(hr.findByDeTaiIdOrderByGiangVienMaGvAsc(20)).thenReturn(List.of());
        assertThrows(NghiepVuException.class, () -> dangKyService.dangKy(10, dangKyForm(20)));
    }
    @Test void tuChoiPhaiCoLyDoVaNhomDuocChonLai() {
        DangKy d = pending();
        assertThrows(NghiepVuException.class, () -> dangKyService.duyet(30, duyetForm(false)));
        var f = duyetForm(false); f.setLyDo("Bổ sung nhóm"); dangKyService.duyet(30, f);
        assertEquals(TrangThaiDangKy.TU_CHOI, d.getTrangThai());
        dangKyService.dangKy(10, dangKyForm(20));
        assertEquals(TrangThaiDangKy.CHO_DUYET, d.getTrangThai());
    }
    @Test void khoaKhongDuyetDeTaiCuTrenTrangDaMo() {
        pending(); var f = duyetForm(true); f.setDeTaiId(99);
        assertThrows(NghiepVuException.class, () -> dangKyService.duyet(30, f));
        verify(dkr, never()).saveAndFlush(any());
    }
    @Test void chapThuanKhoaTatCaThayDoi() {
        DangKy d = pending(); dangKyService.duyet(30, duyetForm(true));
        assertEquals(TrangThaiDangKy.CHAP_THUAN, d.getTrangThai());
        assertEquals(TrangThaiNhom.DANG_THUC_HIEN, nhom.getTrangThai());
        assertThrows(NghiepVuException.class, () -> service.doiTruong(10, 2));
        assertThrows(NghiepVuException.class, () -> service.moi(10, moiForm()));
        assertThrows(NghiepVuException.class, () -> service.xoaThanhVien(10, 2));
        assertThrows(NghiepVuException.class, () -> service.giaiThe(10));
        assertThrows(NghiepVuException.class, () -> dangKyService.huy(10));
        assertThrows(NghiepVuException.class, () -> dangKyService.dangKy(10, dangKyForm(20)));
    }
    @Test void khoaDuocDuyetSauHanSinhVienNeuDotConTrienKhai() {
        pending(); ReflectionTestUtils.setField(dot, "svKetThuc", now.minusHours(1));
        assertDoesNotThrow(() -> dangKyService.duyet(30, duyetForm(true)));
    }
    @Test void huyDangKyTraCho() {
        var d = pending(); dangKyService.huy(10);
        assertEquals(TrangThaiDangKy.HUY, d.getTrangThai());
        assertEquals(TrangThaiNhom.DANG_LAP, nhom.getTrangThai());
    }
    @Test void giaiTheHuyDangKyVaXoaLienKetThanhVien() {
        var d = pending(); service.giaiThe(10);
        assertEquals(TrangThaiDangKy.HUY, d.getTrangThai());
        assertEquals(TrangThaiNhom.GIAI_THE, nhom.getTrangThai());
        verify(tr).deleteAll(argThat(ds -> ((java.util.Collection<?>) ds).size() == 3));
    }
}
