package com.khoacntt.quanlydetai.detai.service;

import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.detai.dto.DeTaiDto;
import com.khoacntt.quanlydetai.detai.dto.BoLocDeTaiDto;
import com.khoacntt.quanlydetai.detai.form.*;
import com.khoacntt.quanlydetai.detai.model.*;
import com.khoacntt.quanlydetai.detai.repository.*;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.dotdangky.repository.DotDangKyRepository;
import com.khoacntt.quanlydetai.hoso.dto.*;
import com.khoacntt.quanlydetai.hoso.model.*;
import com.khoacntt.quanlydetai.hoso.repository.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.validation.annotation.Validated;
import java.time.Clock;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class DeTaiService {
    private final DeTaiRepository deTaiRepository;
    private final HuongDanRepository huongDanRepository;
    private final DotDangKyRepository dotRepository;
    private final BoMonRepository boMonRepository;
    private final GiangVienRepository giangVienRepository;
    private final NguoiDungHienTai nguoiDung;
    private final Clock clock;

    public DeTaiService(DeTaiRepository deTaiRepository, HuongDanRepository huongDanRepository,
                       DotDangKyRepository dotRepository, BoMonRepository boMonRepository,
                       GiangVienRepository giangVienRepository, NguoiDungHienTai nguoiDung, Clock clock) {
        this.deTaiRepository = deTaiRepository;
        this.huongDanRepository = huongDanRepository;
        this.dotRepository = dotRepository;
        this.boMonRepository = boMonRepository;
        this.giangVienRepository = giangVienRepository;
        this.nguoiDung = nguoiDung;
        this.clock = clock;
    }

    public List<DeTaiDto> daCongBo(Integer dotId, Integer boMonId) {
        TaiKhoan actor = nguoiDung.batBuoc();
        return deTaiRepository.findByTrangThaiOrderByIdDesc(TrangThaiDeTai.DA_CONG_BO).stream()
                .filter(this::duocXemCongKhai)
                .filter(d -> dotId == null || Objects.equals(d.getDot().getId(), dotId))
                .filter(d -> boMonId == null || Objects.equals(d.getBoMon().getId(), boMonId))
                .map(d -> dto(d, actor)).toList();
    }

    public BoLocDeTaiDto boLocCongBo() {
        Map<Integer, String> cacDot = new LinkedHashMap<>();
        Map<Integer, String> cacBoMon = new LinkedHashMap<>();
        for (DeTai d : deTaiRepository.findByTrangThaiOrderByIdDesc(TrangThaiDeTai.DA_CONG_BO)) {
            if (duocXemCongKhai(d)) {
                cacDot.put(d.getDot().getId(), d.getDot().getTenDot());
                cacBoMon.put(d.getBoMon().getId(), d.getBoMon().getTenBoMon());
            }
        }
        return new BoLocDeTaiDto(cacDot, cacBoMon);
    }

    @PreAuthorize("hasRole('GIANG_VIEN')")
    public List<DeTaiDto> cuaToi() {
        TaiKhoan actor = nguoiDung.batBuoc();
        return deTaiRepository.cuaGiangVien(actor.getId()).stream().map(d -> dto(d, actor)).toList();
    }

    @PreAuthorize("hasAnyRole('TRUONG_BO_MON','TRUONG_KHOA','CAN_BO_KHOA')")
    public List<DeTaiDto> quanLy() {
        TaiKhoan actor = nguoiDung.batBuoc();
        return deTaiRepository.findAll(Sort.by("id").descending()).stream()
                .filter(d -> laKhoa(actor) || quanLyBoMon(d, actor)).map(d -> dto(d, actor)).toList();
    }

    public DeTaiDto chiTiet(Integer id) {
        DeTai deTai = tim(id);
        TaiKhoan actor = nguoiDung.batBuoc();
        if (!laKhoa(actor) && !quanLyBoMon(deTai, actor) && !duocXemCongKhai(deTai) && !laChuDeXuat(deTai, actor)
                && !huongDanRepository.existsByDeTaiIdAndGiangVienTaiKhoanId(id, actor.getId())) {
            throw new AccessDeniedException("Bạn không được xem đề tài chưa công bố này");
        }
        return dto(deTai, actor);
    }

    @PreAuthorize("hasRole('GIANG_VIEN')")
    public DeTaiForm formSua(Integer id) {
        DeTai d = tim(id);
        kiemTraChuVaDuocSua(d);
        DeTaiForm f = new DeTaiForm();
        f.setDotId(d.getDot().getId());
        f.setBoMonId(d.getBoMon().getId());
        f.setMaDeTai(d.getMaDeTai());
        f.setTenDeTai(d.getTenDeTai());
        f.setNoiDung(d.getNoiDung());
        f.setYeuCau(d.getYeuCau());
        f.setSanPhamDuKien(d.getSanPhamDuKien());
        return f;
    }

    @PreAuthorize("hasRole('GIANG_VIEN')")
    public List<BoMonDto> boMonDeXuat() {
        return boMonRepository.findAll(Sort.by("maBoMon")).stream()
                .filter(b -> b.getDangHoatDong() == 1).map(BoMonDto::tu).toList();
    }

    @PreAuthorize("hasAnyRole('TRUONG_BO_MON','TRUONG_KHOA','CAN_BO_KHOA')")
    public List<GiangVienDto> giangVienHuongDan() {
        return giangVienRepository.findAll(Sort.by("maGv")).stream()
                .filter(this::gvHoatDong).map(GiangVienDto::tu).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public Integer tao(@Valid DeTaiForm f) {
        TaiKhoan actor = nguoiDung.batBuoc();
        GiangVien gv = giangVienRepository.findByTaiKhoanId(actor.getId())
                .orElseThrow(() -> new NghiepVuException("Bạn cần có hồ sơ giảng viên trước khi đề xuất"));
        DotDangKy dot = khoaDot(f.getDotId());
        kiemTraMoDeXuat(dot);
        String ma = chuanHoaMa(f.getMaDeTai());
        if (deTaiRepository.existsByMaDeTai(ma)) throw new NghiepVuException("Mã đề tài đã tồn tại");
        DeTai deTai = new DeTai(dot, gv);
        capNhat(deTai, f, ma);
        luu(deTai);
        return deTai.getId();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public void sua(Integer id, @Valid DeTaiForm f) {
        DeTai deTai = khoaDeTai(id);
        kiemTraChuVaDuocSua(deTai);
        if (!Objects.equals(f.getDotId(), deTai.getDot().getId())) {
            throw new NghiepVuException("Không chuyển đề tài sang đợt khác");
        }
        String ma = chuanHoaMa(f.getMaDeTai());
        if (deTaiRepository.existsByMaDeTaiAndIdNot(ma, id)) throw new NghiepVuException("Mã đề tài đã tồn tại");
        capNhat(deTai, f, ma);
        luu(deTai);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public void guiDuyet(Integer id) {
        DeTai deTai = khoaDeTai(id);
        kiemTraChuVaDuocSua(deTai);
        kiemTraBoMon(deTai.getBoMon().getId());
        deTai.guiDuyet();
        luu(deTai);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('TRUONG_BO_MON')")
    public void duyet(Integer id, @Valid DuyetDeTaiForm f) {
        DeTai deTai = khoaDeTai(id);
        kiemTraBoMonPhuTrach(deTai);
        kiemTraDotTrienKhai(deTai.getDot());
        if (deTai.getTrangThai() != TrangThaiDeTai.CHO_DUYET) throw new NghiepVuException("Đề tài không ở trạng thái chờ duyệt");
        if (!f.getChapThuan() && (f.getNhanXet() == null || f.getNhanXet().isBlank())) {
            throw new NghiepVuException("Cần nêu lý do khi từ chối đề tài");
        }
        deTai.duyet(f.getChapThuan(), nguoiDung.batBuoc(), gon(f.getNhanXet()), ThoiGian.hienTai(clock));
        luu(deTai);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('TRUONG_BO_MON')")
    public void phanCong(Integer id, @Valid HuongDanForm f) {
        DeTai deTai = khoaDeTai(id);
        kiemTraBoMonPhuTrach(deTai);
        kiemTraDotTrienKhai(deTai.getDot());
        if (deTai.getTrangThai() != TrangThaiDeTai.DA_DUYET && deTai.getTrangThai() != TrangThaiDeTai.DA_CONG_BO) {
            throw new NghiepVuException("Phân công hướng dẫn sau khi đề tài được duyệt");
        }
        Set<Integer> ids = f.getGiangVienIds();
        if (ids == null || ids.isEmpty() || ids.size() > 2 || ids.stream().anyMatch(Objects::isNull)) {
            throw new NghiepVuException("Mỗi đề tài phải có từ một đến hai giảng viên hướng dẫn");
        }
        List<GiangVien> giangViens = ids.stream().sorted().map(gvId -> {
            GiangVien gv = giangVienRepository.findById(gvId)
                    .orElseThrow(() -> new KhongTimThayException("Không tìm thấy giảng viên hướng dẫn"));
            kiemTraGvHuongDan(id, gv);
            return gv;
        }).toList();
        // Cả thay GVHD và module chấm điểm phải khóa cùng đề tài để tránh xung đột hai chiều.
        List<HuongDan> hienTai = huongDanRepository.findByDeTaiIdOrderByGiangVienMaGvAsc(id);
        for (HuongDan h : hienTai) {
            if (!ids.contains(h.getGiangVien().getId())) huongDanRepository.delete(h);
        }
        huongDanRepository.flush();
        TaiKhoan actor = nguoiDung.batBuoc();
        for (GiangVien gv : giangViens) {
            boolean daCo = hienTai.stream().anyMatch(h -> Objects.equals(h.getGiangVien().getId(), gv.getId()));
            if (!daCo) huongDanRepository.save(new HuongDan(deTai, gv, actor, ThoiGian.hienTai(clock)));
        }
        huongDanRepository.flush();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public void congBo(Integer id) {
        DeTai deTai = khoaDeTai(id);
        kiemTraDotTrienKhai(deTai.getDot());
        if (deTai.getTrangThai() != TrangThaiDeTai.DA_DUYET) throw new NghiepVuException("Chỉ công bố đề tài đã được duyệt");
        kiemTraBoMon(deTai.getBoMon().getId());
        List<HuongDan> danhSach = huongDanRepository.findByDeTaiIdOrderByGiangVienMaGvAsc(id);
        if (danhSach.isEmpty() || danhSach.size() > 2) throw new NghiepVuException("Cần phân công từ một đến hai GVHD trước khi công bố");
        for (HuongDan h : danhSach) kiemTraGvHuongDan(id, h.getGiangVien());
        deTai.congBo(nguoiDung.batBuoc(), ThoiGian.hienTai(clock));
        luu(deTai);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public void xoa(Integer id) {
        var d = khoaDeTai(id);
        kiemTraChuVaDuocSua(d);
        if (d.getTrangThai() != TrangThaiDeTai.NHAP)
            throw new NghiepVuException("Chỉ xóa đề tài nháp chưa gửi duyệt");
        try { deTaiRepository.delete(d); deTaiRepository.flush(); }
        catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Đề tài đã có dữ liệu liên quan; không thể xóa");
        }
    }

    private void kiemTraGvHuongDan(Integer deTaiId, GiangVien gv) {
        if (!gvHoatDong(gv)) throw new NghiepVuException("Giảng viên hướng dẫn phải có tài khoản hoạt động và vai trò GIANG_VIEN");
        if (deTaiRepository.demPhanCongCham(deTaiId, gv.getId()) > 0
                || deTaiRepository.demThanhVienHoiDong(deTaiId, gv.getId()) > 0) {
            throw new NghiepVuException("Giảng viên đã được phân công chấm hoặc thuộc hội đồng của đề tài này");
        }
    }

    private boolean gvHoatDong(GiangVien gv) {
        return gv.getTaiKhoan().getTrangThai() == TrangThaiTaiKhoan.HOAT_DONG && gv.getTaiKhoan().coVaiTro(VaiTro.GIANG_VIEN);
    }

    private DeTai tim(Integer id) {
        return deTaiRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đề tài"));
    }

    private DotDangKy khoaDot(Integer id) {
        return dotRepository.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đợt đăng ký"));
    }

    private DeTai khoaDeTai(Integer id) {
        Integer dotId = deTaiRepository.timDotId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đề tài"));
        khoaDot(dotId); // Thứ tự dùng chung: đợt trước, đề tài sau.
        DeTai deTai = deTaiRepository.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đề tài"));
        if (!Objects.equals(dotId, deTai.getDot().getId())) throw new NghiepVuException("Dữ liệu đã thay đổi, hãy tải lại trang");
        return deTai;
    }

    private void kiemTraChuVaDuocSua(DeTai deTai) {
        if (!laChuDeXuat(deTai, nguoiDung.batBuoc())) throw new AccessDeniedException("Chỉ giảng viên đề xuất được sửa/gửi đề tài này");
        if (deTai.getTrangThai() != TrangThaiDeTai.NHAP && deTai.getTrangThai() != TrangThaiDeTai.TU_CHOI) {
            throw new NghiepVuException("Chỉ sửa hoặc gửi đề tài nháp/bị từ chối");
        }
        kiemTraMoDeXuat(deTai.getDot());
    }

    private void kiemTraMoDeXuat(DotDangKy dot) {
        if (!dot.dangMoDeXuat(ThoiGian.hienTai(clock))) throw new NghiepVuException("Đợt không trong thời gian giảng viên được đề xuất");
    }

    private void kiemTraDotTrienKhai(DotDangKy dot) {
        if (dot.getTrangThai() != TrangThaiDotDangKy.TRIEN_KHAI) throw new NghiepVuException("Đợt không còn triển khai");
    }

    private BoMon kiemTraBoMon(Integer id) {
        BoMon boMon = boMonRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy bộ môn"));
        if (boMon.getDangHoatDong() != 1) throw new NghiepVuException("Bộ môn đã ngừng hoạt động");
        return boMon;
    }

    private void capNhat(DeTai d, DeTaiForm f, String ma) {
        d.capNhat(kiemTraBoMon(f.getBoMonId()), ma, f.getTenDeTai().trim(), f.getNoiDung().trim(),
                gon(f.getYeuCau()), gon(f.getSanPhamDuKien()));
    }

    private boolean quanLyBoMon(DeTai d, TaiKhoan actor) {
        if (!actor.coVaiTro(VaiTro.TRUONG_BO_MON)) return false;
        return giangVienRepository.findByTaiKhoanId(actor.getId())
                .map(g -> Objects.equals(g.getBoMon().getId(), d.getBoMon().getId())).orElse(false);
    }

    private void kiemTraBoMonPhuTrach(DeTai d) {
        if (!quanLyBoMon(d, nguoiDung.batBuoc()))
            throw new AccessDeniedException("Chỉ Trưởng bộ môn phụ trách bộ môn của đề tài được duyệt/phân công GVHD");
    }

    private boolean laKhoa(TaiKhoan actor) {
        return actor.coVaiTro(VaiTro.TRUONG_KHOA) || actor.coVaiTro(VaiTro.CAN_BO_KHOA);
    }

    private boolean laChuDeXuat(DeTai d, TaiKhoan actor) {
        return actor.coVaiTro(VaiTro.GIANG_VIEN) && Objects.equals(d.getGvDeXuat().getTaiKhoan().getId(), actor.getId());
    }

    private boolean duocXemCongKhai(DeTai d) {
        return d.getTrangThai() == TrangThaiDeTai.DA_CONG_BO
                && d.getDot().getTrangThai() != TrangThaiDotDangKy.HUY
                && d.getDot().getTrangThai() != TrangThaiDotDangKy.NHAP;
    }

    private DeTaiDto dto(DeTai d, TaiKhoan actor) {
        boolean khoa = laKhoa(actor) && d.getDot().getTrangThai() == TrangThaiDotDangKy.TRIEN_KHAI;
        boolean boMon = quanLyBoMon(d, actor) && d.getDot().getTrangThai() == TrangThaiDotDangKy.TRIEN_KHAI;
        boolean sua = laChuDeXuat(d, actor) && d.getDot().dangMoDeXuat(ThoiGian.hienTai(clock))
                && (d.getTrangThai() == TrangThaiDeTai.NHAP || d.getTrangThai() == TrangThaiDeTai.TU_CHOI);
        List<GiangVienDto> huongDans = huongDanRepository.findByDeTaiIdOrderByGiangVienMaGvAsc(d.getId()).stream()
                .map(h -> GiangVienDto.tu(h.getGiangVien())).toList();
        return new DeTaiDto(d.getId(), d.getDot().getId(), d.getDot().getTenDot(), d.getDot().getLoai(),
                d.getBoMon().getId(), d.getBoMon().getTenBoMon(), d.getMaDeTai(), d.getTenDeTai(), d.getNoiDung(),
                d.getYeuCau(), d.getSanPhamDuKien(), d.getGvDeXuat().getTaiKhoan().getHoTen(), d.getTrangThai(),
                d.getNhanXetDuyet(), huongDans, sua, sua, boMon && d.getTrangThai() == TrangThaiDeTai.CHO_DUYET,
                boMon && (d.getTrangThai() == TrangThaiDeTai.DA_DUYET || d.getTrangThai() == TrangThaiDeTai.DA_CONG_BO),
                khoa && d.getTrangThai() == TrangThaiDeTai.DA_DUYET);
    }

    private String chuanHoaMa(String ma) {
        return ma.trim().toUpperCase(Locale.ROOT);
    }

    private String gon(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private void luu(DeTai d) {
        try {
            deTaiRepository.saveAndFlush(d);
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Mã đề tài bị trùng hoặc dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
        }
    }
}
