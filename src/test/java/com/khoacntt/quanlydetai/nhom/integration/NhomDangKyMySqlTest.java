package com.khoacntt.quanlydetai.nhom.integration;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.nhom.form.*;
import com.khoacntt.quanlydetai.nhom.service.NhomService;
import com.khoacntt.quanlydetai.dangky.form.DangKyForm;
import com.khoacntt.quanlydetai.dangky.service.DangKyService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test tùy chọn với MySQL thật, không dùng database chính.
 * Xem mục 12 trong hướng dẫn backend để tạo schema thử và cấu hình biến môi trường.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class NhomDangKyMySqlTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired NhomService nhomService;
    @Autowired DangKyService dangKyService;
    int dotId;
    int topicId;
    int seq;
    String prefix;
    record Student(int id, String username) {}
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        String url = System.getenv("TEST_DB_URL");
        if (url == null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/quan_ly_de_tai_thu_tv3_[a-zA-Z0-9_]+(\\?.*)?"))
            throw new IllegalArgumentException("Chỉ cho phép MySQL local và schema quan_ly_de_tai_thu_tv3_*");
        String password = System.getenv("TEST_DB_PASSWORD");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Thiếu TEST_DB_PASSWORD");
        r.add("spring.datasource.url", () -> url);
        r.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USERNAME", "doan_dev"));
        r.add("spring.datasource.password", () -> password);
        r.add("app.bootstrap-admin.enabled", () -> false);
    }
    @BeforeEach void fixture() {
        assertTrue(jdbc.queryForObject("SELECT DATABASE()", String.class).startsWith("quan_ly_de_tai_thu_tv3_"));
        prefix = "T" + Long.toUnsignedString(System.nanoTime(), 36);
        int admin = account(prefix + "admin", "TRUONG_KHOA");
        int bm = insert("INSERT INTO bo_mon(ma_bo_mon,ten_bo_mon) VALUES(?,?)", prefix, "Bộ môn thử");
        int teacher = account(prefix + "gv", "GIANG_VIEN");
        int gv = insert("INSERT INTO giang_vien(tai_khoan_id,ma_gv,bo_mon_id) VALUES(?,?,?)", teacher, prefix, bm);
        dotId = insert("INSERT INTO dot_dang_ky(ma_dot,ten_dot,loai,gv_bat_dau,gv_ket_thuc,sv_bat_dau,sv_ket_thuc,han_nop_bao_cao,trang_thai,nguoi_tao_id) "
                + "VALUES(?,?,'MON_HOC',UTC_TIMESTAMP()-INTERVAL 2 DAY,UTC_TIMESTAMP()-INTERVAL 1 DAY,UTC_TIMESTAMP()-INTERVAL 1 HOUR,UTC_TIMESTAMP()+INTERVAL 1 DAY,UTC_TIMESTAMP()+INTERVAL 20 DAY,'TRIEN_KHAI',?)",
                prefix, "Đợt thử", admin);
        topicId = insert("INSERT INTO de_tai(dot_id,bo_mon_id,ma_de_tai,ten_de_tai,noi_dung,gv_de_xuat_id,trang_thai,nguoi_duyet_id,duyet_luc,nguoi_cong_bo_id,cong_bo_luc) "
                + "VALUES(?,?,?,?,'Nội dung',?,'DA_CONG_BO',?,UTC_TIMESTAMP(),?,UTC_TIMESTAMP())", dotId, bm, prefix, "Đề tài thử", gv, admin, admin);
        jdbc.update("INSERT INTO huong_dan(de_tai_id,giang_vien_id,nguoi_phan_cong_id) VALUES(?,?,?)", topicId, gv, admin);
    }
    int insert(String sql, Object... args) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps;
        }, keys);
        return Objects.requireNonNull(keys.getKey()).intValue();
    }
    int account(String login, String role) {
        int id = insert("INSERT INTO tai_khoan(ten_dang_nhap,mat_khau_bam,ho_ten,email) VALUES(?,'test-khong-dang-nhap',?,?)",
                login.toLowerCase(Locale.ROOT), login, login + "@test.example");
        jdbc.update("INSERT INTO tai_khoan_vai_tro(tai_khoan_id,vai_tro) VALUES(?,?)", id, role);
        return id;
    }
    Student student() {
        String login = (prefix + "sv" + (++seq)).toLowerCase(Locale.ROOT);
        int accountId = account(login, "SINH_VIEN");
        int id = insert("INSERT INTO sinh_vien(tai_khoan_id,ma_sv) VALUES(?,?)", accountId, login.toUpperCase(Locale.ROOT));
        return new Student(id, login);
    }
    <T> T as(Student s, Supplier<T> action) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(s.username(), null, List.of(new SimpleGrantedAuthority("ROLE_SINH_VIEN"))));
        SecurityContextHolder.setContext(context);
        try { return action.get(); } finally { SecurityContextHolder.clearContext(); }
    }
    int group(Student s) {
        return as(s, () -> {
            var f = new NhomForm(); f.setDotId(dotId); f.setMaNhom("N" + s.id()); f.setTenNhom("Nhóm thử " + s.id());
            return nhomService.tao(f);
        });
    }
    int invite(Student sender, int groupId, Student receiver) {
        as(sender, () -> {
            var f = new MoiThanhVienForm(); f.setMaSv(receiver.username().toUpperCase(Locale.ROOT));
            nhomService.moi(groupId, f); return null;
        });
        return jdbc.queryForObject("SELECT id FROM loi_moi_nhom WHERE nhom_id=? AND sinh_vien_duoc_moi_id=? AND trang_thai='CHO_PHAN_HOI'",
                Integer.class, groupId, receiver.id());
    }
    boolean accept(Student s, int invitation) {
        return as(s, () -> { nhomService.phanHoi(invitation, true); return true; });
    }
    void duBa(Student leader, int groupId) {
        for (int i=0;i<2;i++) { var member=student(); accept(member,invite(leader,groupId,member)); }
    }
    boolean register(Student s, int groupId, int topic) {
        return as(s, () -> {
            var f = new DangKyForm(); f.setDeTaiId(topic);
            dangKyService.dangKy(groupId, f); return true;
        });
    }
    List<Boolean> race(List<Callable<Boolean>> actions) throws Exception {
        var ready = new CountDownLatch(actions.size());
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(actions.size());
        try {
            var futures = new ArrayList<Future<Boolean>>();
            for (var action : actions) futures.add(pool.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Không đồng bộ được test");
                try { return action.call(); } catch (NghiepVuException expected) { return false; }
            }));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            var results = new ArrayList<Boolean>();
            for (var f : futures) results.add(f.get(30, TimeUnit.SECONDS));
            return results;
        } finally { start.countDown(); pool.shutdownNow(); }
    }
    @Test void haiNhomTranhDeTaiChiMotNhomGiuCho() throws Exception {
        var a = student(); var b = student(); int na = group(a), nb = group(b);
        duBa(a,na); duBa(b,nb);
        var results = race(List.of(() -> register(a, na, topicId), () -> register(b, nb, topicId)));
        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM dang_ky WHERE de_tai_id=? AND co_giu_cho=1", Integer.class, topicId));
    }
    @Test void haiLoiMoiCungSinhVienChiChapNhanMot() throws Exception {
        var a = student(); var b = student(); var c = student();
        int na = group(a), nb = group(b);
        int la = invite(a, na, c), lb = invite(b, nb, c);
        var results = race(List.of(() -> accept(c, la), () -> accept(c, lb)));
        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM thanh_vien_nhom WHERE sinh_vien_id=? AND dot_id=?", Integer.class, c.id(), dotId));
    }
    @Test void nhomKhongVuotNamKhiChapNhanDongThoi() throws Exception {
        var leader = student(); int n = group(leader); duBa(leader,n);
        var a = student(); var b = student(); var c = student();
        int la = invite(leader, n, a), lb = invite(leader, n, b), lc = invite(leader, n, c);
        var results = race(List.of(() -> accept(a, la), () -> accept(b, lb), () -> accept(c, lc)));
        assertEquals(2, results.stream().filter(Boolean::booleanValue).count());
        assertEquals(5, jdbc.queryForObject("SELECT COUNT(*) FROM thanh_vien_nhom WHERE nhom_id=?", Integer.class, n));
        assertEquals(1, jdbc.queryForObject("SELECT SUM(la_nhom_truong) FROM thanh_vien_nhom WHERE nhom_id=?", Integer.class, n));
    }
    @Test void doiDeTaiThatBaiGiuNguyenDangKyCu() {
        var a = student(); var b = student(); int na = group(a), nb = group(b);
        int other = insert("INSERT INTO de_tai(dot_id,bo_mon_id,ma_de_tai,ten_de_tai,noi_dung,gv_de_xuat_id,trang_thai,nguoi_duyet_id,duyet_luc,nguoi_cong_bo_id,cong_bo_luc) "
                + "SELECT dot_id,bo_mon_id,CONCAT(ma_de_tai,'X'),ten_de_tai,noi_dung,gv_de_xuat_id,trang_thai,nguoi_duyet_id,duyet_luc,nguoi_cong_bo_id,cong_bo_luc FROM de_tai WHERE id=?", topicId);
        jdbc.update("INSERT INTO huong_dan(de_tai_id,giang_vien_id,nguoi_phan_cong_id) SELECT ?,giang_vien_id,nguoi_phan_cong_id FROM huong_dan WHERE de_tai_id=?", other, topicId);
        duBa(a,na); duBa(b,nb);
        register(a, na, topicId); register(b, nb, other);
        assertThrows(NghiepVuException.class, () -> register(a, na, other));
        assertEquals(topicId, jdbc.queryForObject("SELECT de_tai_id FROM dang_ky WHERE nhom_id=? AND co_giu_cho=1", Integer.class, na));
    }
}
