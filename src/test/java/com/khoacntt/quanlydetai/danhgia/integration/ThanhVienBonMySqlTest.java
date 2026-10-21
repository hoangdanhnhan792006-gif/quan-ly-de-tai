package com.khoacntt.quanlydetai.danhgia.integration;

import com.khoacntt.quanlydetai.baocao.form.NopBaoCaoForm;
import com.khoacntt.quanlydetai.baocao.service.*;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.danhgia.form.*;
import com.khoacntt.quanlydetai.danhgia.service.*;
import com.khoacntt.quanlydetai.thongbao.form.ThongBaoForm;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
import com.khoacntt.quanlydetai.thongbao.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.Statement;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Chỉ chạy khi chỉ định schema thử MySQL riêng; không bao giờ dùng database
 * chính.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ThanhVienBonMySqlTest.TestClockConfig.class)
@EnabledIfEnvironmentVariable(named = "TEST_TV4_DB_URL", matches = ".+")
class ThanhVienBonMySqlTest {
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    NopBaoCaoService nopBaoCao;
    @Autowired
    BaoCaoService baoCao;
    @Autowired
    HoiDongService hoiDong;
    @Autowired
    DanhGiaService danhGia;
    @Autowired
    DanhGiaTruyVanService truyVan;
    @Autowired
    ChamDiemService chamDiem;
    @Autowired
    KetQuaService ketQua;
    @Autowired
    ThongBaoService thongBao;
    @Autowired
    TepThongBaoService tepThongBao;
    @Autowired
    MutableClock clock;
    @Autowired
    MockMvc mvc;
    @Autowired
    com.khoacntt.quanlydetai.detai.service.DeTaiService deTai;

    static final Path FILES = Path.of("target", "tep-kiem-thu-tv4").toAbsolutePath();
    static final Instant START = Instant.parse("2030-04-01T01:00:00Z");
    static class MutableClock extends Clock {
        volatile Instant now = START;
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }
        public Clock withZone(ZoneId zone) {
            return this;
        }
        public Instant instant() {
            return now;
        }
    }
    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock();
        }
    }
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        String url = System.getenv("TEST_TV4_DB_URL");
        if (url == null
                || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/quan_ly_de_tai_thu_tv4_[a-zA-Z0-9_]+(\\?.*)?"))
            throw new IllegalArgumentException("Chỉ dùng schema local quan_ly_de_tai_thu_tv4_*");
        String password = System.getenv("TEST_DB_PASSWORD");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Thiếu TEST_DB_PASSWORD");
        r.add("spring.datasource.url", () -> url);
        r.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USERNAME", "doan_dev"));
        r.add("spring.datasource.password", () -> password);
        r.add("app.bootstrap-admin.enabled", () -> false);
        r.add("app.storage.root", () -> FILES.toString());
    }
    record Actor(int account, int profile, String login, String role) {
    }
    Actor khoa, leader, member, stranger, supervisor, chair, secretary, examiner, otherTeacher;
    String prefix;
    int seq, bm, dot, topic, group, registration;
    @BeforeEach
    void fixture() {
        assertTrue(jdbc.queryForObject("SELECT DATABASE()", String.class).startsWith("quan_ly_de_tai_thu_tv4_"));
        clock.now = START;
        prefix = "T" + Long.toUnsignedString(System.nanoTime(), 36);
        bm = insert("INSERT INTO bo_mon(ma_bo_mon,ten_bo_mon) VALUES(?,?)", prefix, "Bộ môn thử");
        khoa = actor("TRUONG_KHOA");
        leader = actor("SINH_VIEN");
        member = actor("SINH_VIEN");
        stranger = actor("SINH_VIEN");
        supervisor = actor("GIANG_VIEN");
        chair = actor("GIANG_VIEN");
        secretary = actor("GIANG_VIEN");
        examiner = actor("GIANG_VIEN");
        otherTeacher = actor("GIANG_VIEN");
    }
    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }
    @AfterAll
    static void cleanupFiles() throws Exception {
        // Chỉ xóa thư mục tệp thử có tên cố định trong target của project.
        Path expected = Path.of("target").toAbsolutePath().resolve("tep-kiem-thu-tv4").normalize();
        if (!FILES.normalize().equals(expected))
            throw new IllegalStateException("Đường dẫn dọn tệp không hợp lệ");
        if (Files.exists(FILES))
            try (var paths = Files.walk(FILES)) {
                for (Path p : paths.sorted(Comparator.reverseOrder()).toList())
                    Files.delete(p);
            }
    }
    int insert(String sql, Object... args) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(c -> {
            var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++)
                ps.setObject(i + 1, args[i]);
            return ps;
        }, keys);
        return Objects.requireNonNull(keys.getKey()).intValue();
    }
    Actor actor(String role) {
        String login = (prefix + "u" + (++seq)).toLowerCase(Locale.ROOT);
        int a = insert("INSERT INTO tai_khoan(ten_dang_nhap,mat_khau_bam,ho_ten,email) VALUES(?,'test-no-login',?,?)",
                login, login, login + "@test.example");
        jdbc.update("INSERT INTO tai_khoan_vai_tro(tai_khoan_id,vai_tro) VALUES(?,?)", a, role);
        int p = role.equals("SINH_VIEN")
                ? insert("INSERT INTO sinh_vien(tai_khoan_id,ma_sv) VALUES(?,?)", a, login)
                : role.equals("GIANG_VIEN")
                        ? insert("INSERT INTO giang_vien(tai_khoan_id,ma_gv,bo_mon_id) VALUES(?,?,?)", a, login, bm)
                        : 0;
        return new Actor(a, p, login, role);
    }
    <T> T as(Actor a, Supplier<T> work) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(a.login(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + a.role()))));
        SecurityContextHolder.setContext(context);
        try {
            return work.get();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
    void run(Actor a, Runnable work) {
        as(a, () -> {
            work.run();
            return null;
        });
    }
    void project(String type) {
        var now = LocalDateTime.ofInstant(START, ZoneOffset.UTC);
        dot = insert(
                "INSERT INTO dot_dang_ky(ma_dot,ten_dot,loai,gv_bat_dau,gv_ket_thuc,sv_bat_dau,sv_ket_thuc,han_nop_bao_cao,han_nop_diem_pb,ngay_bao_cao_hd,trang_thai,nguoi_tao_id) VALUES(?,?,?,?,?,?,?,?,?,?,'TRIEN_KHAI',?)",
                prefix, "Đợt thử", type, now.minusDays(6), now.minusDays(4), now.minusDays(4), now.minusDays(2),
                now.plusHours(1), Set.of("TLCN", "KLTN").contains(type) ? now.plusHours(4) : null,
                type.equals("KLTN") ? now.toLocalDate() : null, khoa.account());
        topic = insert(
                "INSERT INTO de_tai(dot_id,bo_mon_id,ma_de_tai,ten_de_tai,noi_dung,gv_de_xuat_id,trang_thai,nguoi_duyet_id,duyet_luc,nguoi_cong_bo_id,cong_bo_luc) VALUES(?,?,?,?,'Nội dung',?,'DA_CONG_BO',?,?,?,?)",
                dot, bm, prefix, "Đề tài thử", supervisor.profile(), khoa.account(), now.minusDays(4), khoa.account(),
                now.minusDays(4));
        jdbc.update("INSERT INTO huong_dan(de_tai_id,giang_vien_id,nguoi_phan_cong_id) VALUES(?,?,?)", topic,
                supervisor.profile(), khoa.account());
        group = insert("INSERT INTO nhom(dot_id,ma_nhom,trang_thai,nguoi_tao_id) VALUES(?,?,'DANG_THUC_HIEN',?)", dot,
                prefix, leader.profile());
        jdbc.update("INSERT INTO thanh_vien_nhom(nhom_id,sinh_vien_id,dot_id,la_nhom_truong) VALUES(?,?,?,1),(?,?,?,0)",
                group, leader.profile(), dot, group, member.profile(), dot);
        var third = actor("SINH_VIEN");
        jdbc.update("INSERT INTO thanh_vien_nhom(nhom_id,sinh_vien_id,dot_id,la_nhom_truong) VALUES(?,?,?,0)", group,third.profile(),dot);
        registration = insert(
                "INSERT INTO dang_ky(nhom_id,de_tai_id,dot_id,nguoi_dang_ky_id,dang_ky_luc,trang_thai,nguoi_duyet_id,duyet_luc) VALUES(?,?,?,?,?,'CHAP_THUAN',?,?)",
                group, topic, dot, leader.profile(), now.minusDays(3), khoa.account(), now.minusDays(2));
    }
    static MockMultipartFile pdf() {
        return new MockMultipartFile("pdf", "bao-cao.pdf", "application/pdf", "%PDF-1.4\n%%EOF".getBytes());
    }
    static MockMultipartFile zip() {
        try (var bytes = new ByteArrayOutputStream(); var z = new ZipOutputStream(bytes)) {
            z.putNextEntry(new ZipEntry("src/Main.java"));
            z.write("class Main {}".getBytes());
            z.closeEntry();
            z.finish();
            return new MockMultipartFile("source", "source.zip", "application/zip", bytes.toByteArray());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    NopBaoCaoForm reportForm() {
        var f = new NopBaoCaoForm();
        f.setPdf(pdf());
        f.setSlide(pdf());
        f.setSource(zip());
        f.setGhiChu("Phiên bản thử");
        return f;
    }
    int report() {
        return as(leader, () -> nopBaoCao.nop(registration, reportForm()));
    }
    HoiDongForm councilForm() {
        var f = new HoiDongForm();
        f.setDotId(dot);
        f.setMaHoiDong("HD1");
        f.setTenHoiDong("Hội đồng thử");
        f.setChuTichId(chair.profile());
        f.setThuKyId(secretary.profile());
        f.setUyVienIds(Set.of(examiner.profile()));
        return f;
    }
    int council() {
        int id = as(khoa, () -> hoiDong.tao(councilForm()));
        run(khoa, () -> hoiDong.chot(id));
        return id;
    }
    KeHoachDanhGiaForm planForm(Integer council, Actor pb) {
        var start = ThoiGian.sangGioVietNam(LocalDateTime.ofInstant(START, ZoneOffset.UTC));
        var f = new KeHoachDanhGiaForm();
        f.setDangKyId(registration);
        f.setPhanBienId(pb.profile());
        f.setLichPhanBienLuc(start.plusHours(1));
        f.setDiaDiemPhanBien("P1");
        if (council != null) {
            f.setHoiDongId(council);
            f.setHoiDongBatDau(start.plusHours(2));
            f.setHoiDongKetThuc(start.plusHours(3));
            f.setDiaDiemHoiDong("P2");
        }
        return f;
    }
    int plan(Integer council, Actor pb) {
        return as(khoa, () -> danhGia.lap(planForm(council, pb)));
    }
    int assignment(int dg, Actor a) {
        return jdbc.queryForObject("SELECT id FROM phan_cong_cham WHERE danh_gia_id=? AND giang_vien_id=?",
                Integer.class, dg, a.profile());
    }
    void score(Actor a, int pc, String point, boolean submit) {
        var f = new PhieuChamForm();
        f.setDiem(point == null ? null : new BigDecimal(point));
        f.setNhanXet("Nhận xét của " + a.login());
        f.setNop(submit);
        run(a, () -> chamDiem.luu(pc, f));
    }
    int aggregate(Actor a, int dg) {
        var f = new TongHopForm();
        f.setNhanXet("Đã tổng hợp đầy đủ");
        return as(a, () -> ketQua.tongHop(dg, f));
    }
    void publish(int id) {
        run(khoa, () -> ketQua.xacNhan(id));
        run(khoa, () -> ketQua.congBo(id));
    }
    void start(int dg) {
        clock.now = START.plusSeconds(3600);
        run(khoa, () -> danhGia.batDauPhanBien(dg));
    }
    void page(Actor a, String url) throws Exception {
        mvc.perform(get(url).with(user(a.login()).roles(a.role()))).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MON_HOC", "TLCN", "NCKH", "KLTN"})
    void tronQuyTrinhBonLoaiVaGiaoDien(String type) throws Exception {
        project(type);
        report();
        int last = report();
        boolean hd = Set.of("NCKH", "KLTN").contains(type);
        Integer council = hd ? council() : null;
        int dg = plan(council, examiner), pb = assignment(dg, examiner);
        page(khoa, "/khoa/danh-gia/tao");
        page(khoa, "/khoa/danh-gia/" + dg + "/sua");
        page(leader, "/bao-cao");
        page(leader, "/bao-cao/dang-ky/" + registration);
        page(khoa, "/khoa/hoi-dong");
        page(khoa, "/khoa/hoi-dong/tao");
        if (hd)
            page(khoa, "/khoa/hoi-dong/" + council + "/sua");
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> danhGia.batDauPhanBien(dg)));
        start(dg);
        assertEquals(last, jdbc.queryForObject("SELECT bao_cao_chot_id FROM danh_gia WHERE id=?", Integer.class, dg));
        assertThrows(NghiepVuException.class, this::report);
        assertThrows(NghiepVuException.class, () -> aggregate(hd ? chair : khoa, dg));
        assertThrows(AccessDeniedException.class, () -> score(otherTeacher, pb, "8", true));
        score(examiner, pb, "3.95", false);
        assertThrows(NghiepVuException.class, () -> aggregate(hd ? chair : khoa, dg));
        score(examiner, pb, "3.95", true);
        assertThrows(NghiepVuException.class, () -> score(examiner, pb, "9", true));
        if (hd) {
            assertEquals(3,
                    jdbc.queryForObject("SELECT COUNT(*) FROM phan_cong_cham WHERE danh_gia_id=?", Integer.class, dg));
            assertThrows(NghiepVuException.class, () -> score(chair, assignment(dg, chair), "3.95", true));
            clock.now = START.plusSeconds(7200);
            run(khoa, () -> danhGia.batDauHoiDong(dg));
            score(chair, assignment(dg, chair), "3.95", true);
            score(secretary, assignment(dg, secretary), "3.95", true);
            assertThrows(AccessDeniedException.class, () -> aggregate(khoa, dg));
            run(khoa, () -> hoiDong.hoanThanh(council));
        }
        int result = aggregate(hd ? chair : khoa, dg);
        assertNull(as(leader, () -> ketQua.cuaSinhVien()).getFirst().ketQua());
        publish(result);
        var visible = as(leader, () -> ketQua.cuaSinhVien()).getFirst().ketQua();
        assertEquals(1, ((java.util.List<?>) as(leader, () -> ketQua.traCuuSinhVien(dot)).get("danhSach")).size());
        assertThrows(AccessDeniedException.class, () -> as(stranger, () -> ketQua.traCuuSinhVien(dot)));
        assertThrows(AccessDeniedException.class, () -> as(leader, () -> ketQua.traCuuSinhVien(Integer.MAX_VALUE)));

        assertEquals(new BigDecimal("4.0"), visible.diemCuoi());
        assertTrue(visible.dat());
        assertEquals(hd ? 3 : 1, visible.diems().size());
        assertTrue(as(stranger, () -> ketQua.cuaSinhVien()).isEmpty());
        page(examiner, "/danh-gia");
        page(examiner, "/danh-gia/" + dg);
        page(khoa, "/danh-gia/" + dg);
        page(leader, "/sinh-vien/ket-qua");
        mvc.perform(get("/danh-gia/" + dg).with(user(leader.login()).roles("SINH_VIEN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/khoa/ket-qua/" + result + "/cong-bo").with(user(khoa.login()).roles(khoa.role())))
                .andExpect(status().isForbidden());
    }

    @Test
    void suaDiemGiuSnapshotCuDenKhiCongBoLai() throws Exception {
        project("KLTN");
        report();
        int hd = council(), dg = plan(hd, examiner), pc = assignment(dg, examiner);
        start(dg);
        score(examiner, pc, "7", true);
        clock.now = START.plusSeconds(7200);
        run(khoa, () -> danhGia.batDauHoiDong(dg));
        score(chair, assignment(dg, chair), "8", true);
        score(secretary, assignment(dg, secretary), "9", true);
        int first = aggregate(chair, dg);
        publish(first);
        var f = new YeuCauSuaDiemForm();
        f.setLyDo("Kiểm tra lại tiêu chí");
        run(examiner, () -> chamDiem.yeuCauSua(pc, f));
        int yc = jdbc.queryForObject("SELECT id FROM yeu_cau_sua_diem WHERE phan_cong_cham_id=?", Integer.class, pc);
        assertThrows(NghiepVuException.class, () -> run(examiner, () -> chamDiem.yeuCauSua(pc, f)));
        assertThrows(NghiepVuException.class, () -> score(examiner, pc, "10", true));
        var old = as(member, () -> ketQua.cuaSinhVien()).getFirst().ketQua();
        assertTrue(old.dangDieuChinh());
        assertEquals(new BigDecimal("8.0"), old.diemCuoi());
        var approval = new DuyetSuaDiemForm();
        approval.setChapThuan(true);
        jdbc.update("INSERT INTO tai_khoan_vai_tro(tai_khoan_id,vai_tro) VALUES(?,'CAN_BO_KHOA')", examiner.account());
        var dual = new Actor(examiner.account(), examiner.profile(), examiner.login(), "CAN_BO_KHOA");
        assertThrows(AccessDeniedException.class, () -> run(dual, () -> chamDiem.duyetYeuCau(yc, approval)));
        jdbc.update("DELETE FROM tai_khoan_vai_tro WHERE tai_khoan_id=? AND vai_tro='CAN_BO_KHOA'", examiner.account());
        run(khoa, () -> chamDiem.duyetYeuCau(yc, approval));
        assertThrows(NghiepVuException.class, () -> aggregate(chair, dg));
        page(examiner, "/danh-gia/" + dg);
        page(member, "/sinh-vien/ket-qua");
        // Sửa điểm sau khi đợt đã kết thúc vẫn phải qua quy trình duyệt.
        jdbc.update("UPDATE dot_dang_ky SET trang_thai='KET_THUC' WHERE id=?", dot);
        score(examiner, pc, "10", true);
        int second = aggregate(chair, dg);
        assertEquals(new BigDecimal("8.0"), as(member, () -> ketQua.cuaSinhVien()).getFirst().ketQua().diemCuoi());
        publish(second);
        var current = as(member, () -> ketQua.cuaSinhVien()).getFirst().ketQua();
        assertEquals(new BigDecimal("9.0"), current.diemCuoi());
        assertFalse(current.dangDieuChinh());
        assertEquals(2, current.phienBan());
        assertEquals(new BigDecimal("8.0"),
                jdbc.queryForObject("SELECT diem_cuoi FROM ket_qua WHERE id=?", BigDecimal.class, first));
        assertEquals(2,
                jdbc.queryForObject("SELECT COUNT(*) FROM phieu_cham WHERE phan_cong_cham_id=? AND trang_thai='DA_NOP'",
                        Integer.class, pc));
    }

    @Test
    void baoCaoPhanQuyenThieuTepVaTaiXuong() throws Exception {
        project("MON_HOC");
        assertThrows(AccessDeniedException.class, () -> as(member, () -> nopBaoCao.nop(registration, reportForm())));
        assertThrows(AccessDeniedException.class, () -> as(stranger, () -> baoCao.chiTiet(registration)));
        var invalid = reportForm();
        invalid.setSource(null);
        assertThrows(NghiepVuException.class, () -> as(leader, () -> nopBaoCao.nop(registration, invalid)));
        assertEquals(0,
                jdbc.queryForObject("SELECT COUNT(*) FROM bao_cao WHERE dang_ky_id=?", Integer.class, registration));
        int r = report();
        int file = jdbc.queryForObject("SELECT MIN(id) FROM bao_cao_tep WHERE bao_cao_id=?", Integer.class, r);
        mvc.perform(get("/bao-cao/tep/" + file).with(user(member.login()).roles("SINH_VIEN")))
                .andExpect(status().isOk()).andExpect(header().string("X-Content-Type-Options", "nosniff"));
        mvc.perform(get("/bao-cao/tep/" + file).with(user(stranger.login()).roles("SINH_VIEN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/bao-cao/tep/" + file)).andExpect(status().is3xxRedirection());
        assertNotNull(as(supervisor, () -> baoCao.taiTep(file)));
        assertThrows(AccessDeniedException.class, () -> as(otherTeacher, () -> baoCao.taiTep(file)));
    }

    @Test
    void rangBuocHoiDongVaKhongChamDeTaiHuongDan() {
        project("NCKH");
        report();
        var bad = councilForm();
        bad.setThuKyId(chair.profile());
        assertThrows(NghiepVuException.class, () -> as(khoa, () -> hoiDong.tao(bad)));
        int hd = council();
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> hoiDong.sua(hd, councilForm())));
        assertThrows(NghiepVuException.class, () -> plan(hd, supervisor));
        assertEquals(0,
                jdbc.queryForObject("SELECT COUNT(*) FROM danh_gia WHERE dang_ky_id=?", Integer.class, registration));
        // GVPB ngoài hội đồng: phải có 4 người chấm duy nhất.
        int dg = plan(hd, otherTeacher);
        assertEquals(4,
                jdbc.queryForObject("SELECT COUNT(*) FROM phan_cong_cham WHERE danh_gia_id=?", Integer.class, dg));
        start(dg);
        score(otherTeacher, assignment(dg, otherTeacher), "8", true);
        clock.now = START.plusSeconds(7200);
        run(khoa, () -> danhGia.batDauHoiDong(dg));
        score(chair, assignment(dg, chair), "8", true);
        score(secretary, assignment(dg, secretary), "8", true);
        assertThrows(NghiepVuException.class, () -> aggregate(chair, dg));
        score(examiner, assignment(dg, examiner), "4", true);
        int k = aggregate(chair, dg);
        publish(k);
        assertEquals(new BigDecimal("7.0"), as(leader, () -> ketQua.cuaSinhVien()).getFirst().ketQua().diemCuoi());
    }

    @Test
    void thongBaoNhapCongBoThuHoiVaTepRiengTu() throws Exception {
        var f = new ThongBaoForm();
        f.setTieuDe("Lịch báo cáo");
        f.setNoiDung("<script>alert(1)</script> Nội dung");
        f.setDonViBanHanh(DonViBanHanh.KHOA_CNTT);
        int id = as(khoa, () -> thongBao.tao(f));
        run(khoa, () -> tepThongBao.them(id, pdf()));
        int file = jdbc.queryForObject("SELECT id FROM thong_bao_tep WHERE thong_bao_id=?", Integer.class, id);
        assertThrows(AccessDeniedException.class, () -> as(leader, () -> thongBao.chiTiet(id)));
        assertThrows(AccessDeniedException.class, () -> as(leader, () -> thongBao.taiTep(file)));
        page(khoa, "/thong-bao/tao");
        page(khoa, "/thong-bao/" + id + "/sua");
        page(khoa, "/thong-bao/quan-ly");
        page(khoa, "/thong-bao/" + id);
        run(khoa, () -> thongBao.congBo(id));
        page(leader, "/thong-bao");
        mvc.perform(get("/thong-bao/" + id).with(user(leader.login()).roles("SINH_VIEN"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;"))).andExpect(
                        content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>"))));
        mvc.perform(get("/thong-bao/tep/" + file).with(user(leader.login()).roles("SINH_VIEN")))
                .andExpect(status().isOk());
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> thongBao.sua(id, f)));
        Actor another = actor("CAN_BO_KHOA");
        assertThrows(AccessDeniedException.class, () -> run(another, () -> thongBao.thuHoi(id)));
        run(khoa, () -> thongBao.thuHoi(id));
        assertThrows(AccessDeniedException.class, () -> as(leader, () -> thongBao.taiTep(file)));
        run(khoa, () -> thongBao.sua(id, f));
        run(khoa, () -> thongBao.congBo(id));
        assertNotNull(as(leader, () -> thongBao.chiTiet(id)));
    }

    @Test
    void nopDiemDongThoiChiCoMotPhieuChinhThuc() throws Exception {
        project("MON_HOC");
        report();
        int dg = plan(null, examiner), pc = assignment(dg, examiner);
        start(dg);
        var barrier = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> task = () -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    score(examiner, pc, "8", true);
                    return true;
                } catch (NghiepVuException expected) {
                    return false;
                }
            };
            var a = pool.submit(task);
            var b = pool.submit(task);
            assertNotEquals(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));
        }
        assertEquals(1,
                jdbc.queryForObject("SELECT COUNT(*) FROM phieu_cham WHERE phan_cong_cham_id=? AND trang_thai='DA_NOP'",
                        Integer.class, pc));
    }

    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> diaDiemHoiDongBoTrong() {
        return java.util.stream.Stream.of("MON_HOC", "TLCN").flatMap(type ->
                java.util.stream.Stream.of("", "   ", "\t").map(blank ->
                        org.junit.jupiter.params.provider.Arguments.of(type, blank)));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("diaDiemHoiDongBoTrong")
    void luuVaSuaKeHoachKhongHoiDongTuFormTrinhDuyet(String type, String blank) throws Exception {
        project(type);
        // Trình duyệt gửi cả các input bỏ trống, khác test cũ vốn không gửi tham số này.
        mvc.perform(post("/khoa/danh-gia/tao").with(user(khoa.login()).roles(khoa.role())).with(csrf())
                .param("dangKyId", String.valueOf(registration)).param("phanBienId", String.valueOf(examiner.profile()))
                .param("lichPhanBienLuc", "2030-04-01T09:00").param("diaDiemPhanBien", "P1")
                .param("hoiDongId", "").param("hoiDongBatDau", "").param("hoiDongKetThuc", "")
                .param("diaDiemHoiDong", blank))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("thanhCong"));
        int dg = jdbc.queryForObject("SELECT id FROM danh_gia WHERE dang_ky_id=?", Integer.class, registration);
        assertNull(jdbc.queryForObject("SELECT dia_diem_hoi_dong FROM danh_gia WHERE id=?", String.class, dg));
        mvc.perform(post("/khoa/danh-gia/" + dg + "/sua").with(user(khoa.login()).roles(khoa.role())).with(csrf())
                .param("dangKyId", String.valueOf(registration)).param("phanBienId", String.valueOf(examiner.profile()))
                .param("lichPhanBienLuc", "2030-04-01T09:10").param("diaDiemPhanBien", "P3")
                .param("hoiDongId", "").param("hoiDongBatDau", "").param("hoiDongKetThuc", "")
                .param("diaDiemHoiDong", blank))
                .andExpect(redirectedUrl("/danh-gia/" + dg)).andExpect(flash().attributeExists("thanhCong"));
        assertNull(jdbc.queryForObject("SELECT dia_diem_hoi_dong FROM danh_gia WHERE id=?", String.class, dg));
        assertEquals("P3", jdbc.queryForObject("SELECT dia_diem_phan_bien FROM danh_gia WHERE id=?", String.class, dg));
    }

    @Test
    void postFormVaUploadQuaMvc() throws Exception {
        project("MON_HOC");
        mvc.perform(multipart("/bao-cao/dang-ky/" + registration + "/nop").file(pdf())
                .file(new MockMultipartFile("slide", "slide.pdf", "application/pdf", pdf().getBytes())).file(zip())
                .with(user(leader.login()).roles("SINH_VIEN")).with(csrf()))
                .andExpect(redirectedUrl("/bao-cao/dang-ky/" + registration));
        mvc.perform(post("/khoa/danh-gia/tao").with(user(khoa.login()).roles(khoa.role())).with(csrf())
                .param("dangKyId", String.valueOf(registration)).param("phanBienId", String.valueOf(examiner.profile()))
                .param("lichPhanBienLuc", "2030-04-01T09:00").param("diaDiemPhanBien", "P1"))
                .andExpect(status().is3xxRedirection());
        int dg = jdbc.queryForObject("SELECT id FROM danh_gia WHERE dang_ky_id=?", Integer.class, registration),
                pc = assignment(dg, examiner);
        start(dg);
        for (String bad : List.of("-1", "10.01", "7.123", "abc")) {
            mvc.perform(post("/giang-vien/cham-diem/" + pc).with(user(examiner.login()).roles("GIANG_VIEN"))
                    .with(csrf()).param("diem", bad).param("nhanXet", "Nhận xét").param("nop", "true"))
                    .andExpect(redirectedUrl("/danh-gia/" + dg)).andExpect(flash().attributeExists("loi"));
        }
        assertEquals(0,
                jdbc.queryForObject("SELECT COUNT(*) FROM phieu_cham WHERE phan_cong_cham_id=?", Integer.class, pc));
        mvc.perform(post("/giang-vien/cham-diem/" + pc).with(user(examiner.login()).roles("GIANG_VIEN")).with(csrf())
                .param("diem", "0").param("nhanXet", "Không hoàn thành").param("nop", "true"))
                .andExpect(redirectedUrl("/danh-gia/" + dg)).andExpect(flash().attributeExists("thanhCong"));
        mvc.perform(post("/danh-gia/" + dg + "/tong-hop").with(user(khoa.login()).roles(khoa.role())).with(csrf())
                .param("nhanXet", "Tổng hợp")).andExpect(redirectedUrl("/danh-gia/" + dg));
        int k = jdbc.queryForObject("SELECT id FROM ket_qua WHERE danh_gia_id=?", Integer.class, dg);
        for (String action : List.of("xac-nhan", "cong-bo")) {
            mvc.perform(
                    post("/khoa/ket-qua/" + k + "/" + action).with(user(khoa.login()).roles(khoa.role())).with(csrf()))
                    .andExpect(redirectedUrl("/danh-gia/" + dg)).andExpect(flash().attributeExists("thanhCong"));
        }
        assertFalse(as(leader, () -> ketQua.cuaSinhVien()).getFirst().ketQua().dat());
    }

    @Test
    void tuChoiHuyYeuCauVaVoHieuKetQuaChuaCongBo() {
        project("MON_HOC");
        report();
        int dg = plan(null, examiner), pc = assignment(dg, examiner);
        start(dg);
        score(examiner, pc, "8", true);
        int k = aggregate(khoa, dg);
        run(khoa, () -> ketQua.xacNhan(k));
        var request = new YeuCauSuaDiemForm();
        request.setLyDo("Cần điều chỉnh");
        run(examiner, () -> chamDiem.yeuCauSua(pc, request));
        int yc = jdbc.queryForObject("SELECT MAX(id) FROM yeu_cau_sua_diem WHERE phan_cong_cham_id=?", Integer.class,
                pc);
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> ketQua.congBo(k)));
        var reject = new DuyetSuaDiemForm();
        reject.setChapThuan(false);
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> chamDiem.duyetYeuCau(yc, reject)));
        reject.setNhanXet("Không đủ căn cứ");
        run(khoa, () -> chamDiem.duyetYeuCau(yc, reject));
        run(examiner, () -> chamDiem.yeuCauSua(pc, request));
        int next = jdbc.queryForObject("SELECT MAX(id) FROM yeu_cau_sua_diem WHERE phan_cong_cham_id=?", Integer.class,
                pc);
        var approve = new DuyetSuaDiemForm();
        approve.setChapThuan(true);
        run(khoa, () -> chamDiem.duyetYeuCau(next, approve));
        assertEquals("HUY", jdbc.queryForObject("SELECT trang_thai FROM ket_qua WHERE id=?", String.class, k));
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> ketQua.congBo(k)));
        run(examiner, () -> chamDiem.huyYeuCau(next));
        assertEquals("HUY", jdbc.queryForObject("SELECT trang_thai FROM phieu_cham WHERE yeu_cau_sua_diem_id=?",
                String.class, next));
        assertThrows(NghiepVuException.class, () -> score(examiner, pc, "9", true));
        publish(aggregate(khoa, dg));
        assertEquals(new BigDecimal("8.0"), as(leader, () -> ketQua.cuaSinhVien()).getFirst().ketQua().diemCuoi());
    }

    @Test
    void chanGvChamThanhHuongDanVaDungHanDiemPb() {
        project("TLCN");
        report();
        int dg = plan(null, examiner), pc = assignment(dg, examiner);
        var f = new com.khoacntt.quanlydetai.detai.form.HuongDanForm();
        f.setGiangVienIds(Set.of(examiner.profile()));
        jdbc.update("INSERT INTO tai_khoan_vai_tro(tai_khoan_id,vai_tro) VALUES(?,'TRUONG_BO_MON')", supervisor.account());
        assertThrows(NghiepVuException.class, () -> run(new Actor(supervisor.account(), supervisor.profile(), supervisor.login(), "TRUONG_BO_MON"), () -> deTai.phanCong(topic, f)));
        start(dg);
        clock.now = START.plusSeconds(4 * 3600);
        assertThrows(NghiepVuException.class, () -> score(examiner, pc, "8", true));
        assertEquals(0,
                jdbc.queryForObject("SELECT COUNT(*) FROM phieu_cham WHERE phan_cong_cham_id=?", Integer.class, pc));
    }

    @Test
    void haiPhienBanBaoCaoDongThoiKhongTrungSo() throws Exception {
        project("MON_HOC");
        var barrier = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Integer> task = () -> {
                barrier.await(10, TimeUnit.SECONDS);
                return report();
            };
            var a = pool.submit(task);
            var b = pool.submit(task);
            assertNotEquals(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));
        }
        assertEquals(List.of(1, 2), jdbc.queryForList(
                "SELECT phien_ban FROM bao_cao WHERE dang_ky_id=? ORDER BY phien_ban", Integer.class, registration));
        assertEquals(6,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM bao_cao_tep t JOIN bao_cao b ON b.id=t.bao_cao_id WHERE b.dang_ky_id=?",
                        Integer.class, registration));
    }

    @Test void haiDotDatCungGvCungGioChiMotKeHoachThanhCong() throws Exception {
        project("NCKH");
        var first=planForm(council(),examiner);
        prefix=prefix+"B";
        project("NCKH");
        var second=planForm(council(),examiner);
        var barrier=new CyclicBarrier(2);
        try(var pool=Executors.newFixedThreadPool(2)) {
            java.util.function.Function<KeHoachDanhGiaForm,Callable<Boolean>> task=f->()->{
                barrier.await(10,TimeUnit.SECONDS);
                try {as(khoa,()->danhGia.lap(f));return true;} catch(NghiepVuException expected) {return false;}
            };
            var a=pool.submit(task.apply(first));var b=pool.submit(task.apply(second));
            assertNotEquals(a.get(30,TimeUnit.SECONDS),b.get(30,TimeUnit.SECONDS));
        }
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM danh_gia WHERE dang_ky_id IN (?,?)",Integer.class,first.getDangKyId(),second.getDangKyId()));
    }

    @Test void hoiDongDuNamVaTuChoiVuotGioiHan() {
        project("NCKH");
        var f=councilForm();f.setUyVienIds(Set.of(examiner.profile(),otherTeacher.profile(),supervisor.profile()));
        int hd=as(khoa,()->hoiDong.tao(f));
        assertEquals(5,as(khoa,()->hoiDong.chiTiet(hd)).thanhViens().size());
        run(khoa,()->hoiDong.chot(hd));
        // Hội đồng có GVHD của đề tài: không được phân công chấm đề tài đó.
        assertThrows(NghiepVuException.class,()->plan(hd,examiner));
        var extra=actor("GIANG_VIEN");f.setMaHoiDong("HD2");
        f.setUyVienIds(Set.of(examiner.profile(),otherTeacher.profile(),supervisor.profile(),extra.profile()));
        assertThrows(jakarta.validation.ConstraintViolationException.class,()->as(khoa,()->hoiDong.tao(f)));
        f.setUyVienIds(Set.of());
        assertThrows(jakarta.validation.ConstraintViolationException.class,()->as(khoa,()->hoiDong.tao(f)));
    }

    @Autowired jakarta.persistence.EntityManagerFactory entityManagerFactory;

    // Đếm SQL thật trên MySQL, không chỉ kiểm tra Mockito gọi đúng tên hàm.
    long demTruyVan(Runnable work) {
        var stats = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        boolean old = stats.isStatisticsEnabled();
        stats.setStatisticsEnabled(true);
        stats.clear();
        try { work.run(); return stats.getPrepareStatementCount(); }
        finally { stats.setStatisticsEnabled(old); }
    }

    int themDeTai(int boMon, Actor gv, String suffix) {
        var now = LocalDateTime.ofInstant(START, ZoneOffset.UTC);
        int id = insert("INSERT INTO de_tai(dot_id,bo_mon_id,ma_de_tai,ten_de_tai,noi_dung,gv_de_xuat_id,trang_thai,nguoi_duyet_id,duyet_luc,nguoi_cong_bo_id,cong_bo_luc) "
                + "VALUES(?,?,?,?,'Nội dung',?,'DA_CONG_BO',?,?,?,?)", dot, boMon, prefix + suffix,
                "Đề tài " + suffix, gv.profile(), khoa.account(), now.minusDays(4), khoa.account(), now.minusDays(4));
        jdbc.update("INSERT INTO huong_dan(de_tai_id,giang_vien_id,nguoi_phan_cong_id) VALUES(?,?,?)", id, gv.profile(), khoa.account());
        return id;
    }

    @Test void locDeTaiDungDotBoMonVaQuyenTruongBoMon() {
        project("MON_HOC");
        int otherBm = insert("INSERT INTO bo_mon(ma_bo_mon,ten_bo_mon) VALUES(?,?)", prefix + "X", "Bộ môn khác");
        int other = themDeTai(otherBm, supervisor, "other");
        assertEquals(Set.of(topic, other), as(leader, () -> deTai.daCongBo(dot, null)).stream()
                .map(d -> d.id()).collect(java.util.stream.Collectors.toSet()));
        assertEquals(List.of(topic), as(leader, () -> deTai.daCongBo(dot, bm)).stream().map(d -> d.id()).toList());
        jdbc.update("INSERT INTO tai_khoan_vai_tro VALUES(?,'TRUONG_BO_MON')", supervisor.account());
        var tbm = new Actor(supervisor.account(), supervisor.profile(), supervisor.login(), "TRUONG_BO_MON");
        var ds = as(tbm, () -> deTai.quanLy());
        assertFalse(ds.isEmpty());
        assertTrue(ds.stream().allMatch(d -> d.boMonId() == bm));
        assertTrue(as(leader, () -> deTai.boLocCongBo()).cacDot().containsKey(dot));
        jdbc.update("UPDATE dot_dang_ky SET trang_thai='HUY' WHERE id=?", dot);
        assertTrue(as(leader, () -> deTai.daCongBo(dot, null)).isEmpty());
        assertFalse(as(leader, () -> deTai.boLocCongBo()).cacDot().containsKey(dot));
    }

    @Test void danhSachBaoCaoDanhGiaLocDungNguoiVaGiuQuyenNop() {
        project("MON_HOC");
        int dg = plan(null, examiner);
        assertEquals(List.of(registration), as(leader, () -> baoCao.danhSach()).stream().map(d -> d.dangKyId()).toList());
        assertTrue(as(leader, () -> baoCao.danhSach()).getFirst().coTheNop());
        assertFalse(as(member, () -> baoCao.danhSach()).getFirst().coTheNop());
        assertTrue(as(stranger, () -> baoCao.danhSach()).isEmpty());
        assertTrue(as(otherTeacher, () -> baoCao.danhSach()).isEmpty());
        assertEquals(1, as(supervisor, () -> baoCao.danhSach()).size());
        assertTrue(as(supervisor, () -> truyVan.danhSach()).isEmpty());
        assertEquals(List.of(dg), as(examiner, () -> truyVan.danhSach()).stream().map(d -> d.id()).toList());
        assertTrue(as(otherTeacher, () -> truyVan.danhSach()).isEmpty());
        report(); start(dg);
        assertFalse(as(leader, () -> baoCao.danhSach()).getFirst().coTheNop());
    }

    @Test void danhSachDeTaiKhongTruyVanThemChoTungDong() {
        project("MON_HOC");
        long before = demTruyVan(() -> assertEquals(1, as(khoa, () -> deTai.daCongBo(dot, null)).size()));
        for (int n = 0; n < 20; n++) themDeTai(bm, actor("GIANG_VIEN"), "batch" + n);
        long after = demTruyVan(() -> assertEquals(21, as(khoa, () -> deTai.daCongBo(dot, null)).size()));
        assertTrue(after <= before + 1, "SQL tăng theo dòng: " + before + " -> " + after);
        System.out.println("DeTai SQL: 1 dòng=" + before + ", 21 dòng=" + after);
    }

    @Test void truyVanTrungLichBoQuaChinhNoVaDotHuy() {
        project("MON_HOC");
        int first = plan(null, examiner), firstDot = dot;
        prefix += "B"; project("MON_HOC");
        var f = planForm(null, examiner);
        assertThrows(NghiepVuException.class, () -> as(khoa, () -> danhGia.lap(f)));
        int second = plan(null, otherTeacher);
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> danhGia.sua(second, f)));
        jdbc.update("UPDATE dot_dang_ky SET trang_thai='HUY' WHERE id=?", firstDot);
        run(khoa, () -> danhGia.sua(second, f));
        run(khoa, () -> danhGia.sua(second, f)); // Chính lần đánh giá đang sửa không tự xung đột.
        assertNotEquals(first, second);
    }

    @Test void phanBienChamBienDauHoiDongBiChanNhungBienCuoiDuocPhep() {
        project("NCKH");
        var first = planForm(council(), examiner);
        as(khoa, () -> danhGia.lap(first));
        prefix += "B"; project("MON_HOC");
        var f = planForm(null, chair);
        f.setLichPhanBienLuc(first.getHoiDongBatDau());
        assertThrows(NghiepVuException.class, () -> as(khoa, () -> danhGia.lap(f)));
        f.setLichPhanBienLuc(first.getHoiDongKetThuc());
        assertNotNull(as(khoa, () -> danhGia.lap(f)));
    }

    @Test void thongBaoLocNguoiDangVaKhongLoQuyenSauKhiDoiVaiTro() {
        var f = new ThongBaoForm(); f.setTieuDe("Thông báo thử"); f.setNoiDung("Nội dung");
        f.setDonViBanHanh(DonViBanHanh.KHOA_CNTT);
        int id = as(khoa, () -> thongBao.tao(f));
        run(khoa, () -> thongBao.congBo(id));
        var other = actor("CAN_BO_KHOA");
        assertTrue(as(other, () -> thongBao.quanLy()).isEmpty());
        assertEquals(List.of(id), as(khoa, () -> thongBao.quanLy()).stream().map(t -> t.id()).toList());
        jdbc.update("UPDATE tai_khoan_vai_tro SET vai_tro='SINH_VIEN' WHERE tai_khoan_id=?", khoa.account());
        var former = new Actor(khoa.account(), 0, khoa.login(), "SINH_VIEN");
        assertFalse(as(former, () -> thongBao.congKhai()).stream().filter(t -> t.id() == id).findFirst().orElseThrow().coTheSua());
    }

    Map<String, Long> demCacDanhSach() {
        return Map.of(
                "baoCao", demTruyVan(() -> as(khoa, () -> baoCao.danhSach())),
                "danhGia", demTruyVan(() -> as(khoa, () -> truyVan.danhSach())),
                "hoiDong", demTruyVan(() -> as(khoa, () -> hoiDong.danhSach())),
                "thongBao", demTruyVan(() -> as(khoa, () -> thongBao.congKhai())));
    }

    void themBoDuLieuDanhSach() {
        project("NCKH");
        plan(council(), examiner);
        var author = actor("CAN_BO_KHOA");
        var f = new ThongBaoForm(); f.setTieuDe("Thông báo " + prefix); f.setNoiDung("Nội dung");
        f.setDonViBanHanh(DonViBanHanh.KHOA_CNTT);
        int id = as(author, () -> thongBao.tao(f));
        run(author, () -> thongBao.congBo(id));
    }

    @Test void cacDanhSachBaoCaoDanhGiaHoiDongThongBaoKhongBiNPlusOne() {
        themBoDuLieuDanhSach();
        var before = demCacDanhSach();
        for (int n = 0; n < 4; n++) {
            prefix += "B";
            examiner = actor("GIANG_VIEN"); chair = actor("GIANG_VIEN"); secretary = actor("GIANG_VIEN");
            themBoDuLieuDanhSach();
        }
        var after = demCacDanhSach();
        before.forEach((key, count) -> assertTrue(after.get(key) <= count + 1,
                key + " SQL tăng theo dòng: " + count + " -> " + after.get(key)));
        System.out.println("DanhSach SQL before=" + before + ", after=" + after);
    }

    @Test void trungDiaDiemHoiDongTrongDotDuKhacGiangVienVaChapNhanGioLienKe() {
        project("NCKH");
        var first = planForm(council(), examiner);
        as(khoa, () -> danhGia.lap(first));
        int secondTopic = themDeTai(bm, supervisor, "room");
        int secondGroup = insert("INSERT INTO nhom(dot_id,ma_nhom,trang_thai,nguoi_tao_id) VALUES(?,?,'DANG_THUC_HIEN',?)",
                dot, prefix + "room", stranger.profile());
        jdbc.update("INSERT INTO thanh_vien_nhom(nhom_id,sinh_vien_id,dot_id,la_nhom_truong) VALUES(?,?,?,1)",
                secondGroup, stranger.profile(), dot);
        for (int n = 0; n < 2; n++) {
            var sv = actor("SINH_VIEN");
            jdbc.update("INSERT INTO thanh_vien_nhom(nhom_id,sinh_vien_id,dot_id,la_nhom_truong) VALUES(?,?,?,0)",
                    secondGroup, sv.profile(), dot);
        }
        int secondRegistration = insert("INSERT INTO dang_ky(nhom_id,de_tai_id,dot_id,nguoi_dang_ky_id,dang_ky_luc,trang_thai,nguoi_duyet_id,duyet_luc) "
                + "VALUES(?,?,?,?,?,'CHAP_THUAN',?,?)", secondGroup, secondTopic, dot, stranger.profile(),
                LocalDateTime.ofInstant(START, ZoneOffset.UTC).minusDays(3), khoa.account(),
                LocalDateTime.ofInstant(START, ZoneOffset.UTC).minusDays(2));
        var pb = actor("GIANG_VIEN"); var ct = actor("GIANG_VIEN"); var tk = actor("GIANG_VIEN");
        var hdForm = councilForm(); hdForm.setMaHoiDong("HD2");
        hdForm.setChuTichId(ct.profile()); hdForm.setThuKyId(tk.profile()); hdForm.setUyVienIds(Set.of(pb.profile()));
        int hd = as(khoa, () -> hoiDong.tao(hdForm)); run(khoa, () -> hoiDong.chot(hd));
        var second = planForm(hd, pb); second.setDangKyId(secondRegistration);
        second.setDiaDiemHoiDong("\tP2\t");
        assertThrows(NghiepVuException.class, () -> as(khoa, () -> danhGia.lap(second)));
        second.setDiaDiemHoiDong("P3");
        int dg = as(khoa, () -> danhGia.lap(second));
        second.setDiaDiemHoiDong("P2");
        assertThrows(NghiepVuException.class, () -> run(khoa, () -> danhGia.sua(dg, second)));
        second.setHoiDongBatDau(first.getHoiDongKetThuc());
        second.setHoiDongKetThuc(first.getHoiDongKetThuc().plusHours(1));
        run(khoa, () -> danhGia.sua(dg, second));
    }
}
