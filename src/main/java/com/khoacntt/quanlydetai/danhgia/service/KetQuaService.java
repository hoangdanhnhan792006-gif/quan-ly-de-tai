package com.khoacntt.quanlydetai.danhgia.service;

import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy;
import com.khoacntt.quanlydetai.dangky.repository.DangKyRepository;
import com.khoacntt.quanlydetai.danhgia.dto.*;
import com.khoacntt.quanlydetai.danhgia.form.TongHopForm;
import com.khoacntt.quanlydetai.danhgia.model.*;
import com.khoacntt.quanlydetai.danhgia.repository.*;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;
import com.khoacntt.quanlydetai.nhom.model.TrangThaiNhom;
import com.khoacntt.quanlydetai.nhom.repository.ThanhVienNhomRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class KetQuaService {
    private final KetQuaRepository ketQuas;
    private final ChiTietKetQuaRepository chiTiets;
    private final PhanCongChamRepository phanCongs;
    private final PhieuChamRepository phieus;
    private final YeuCauSuaDiemRepository yeuCaus;
    private final DanhGiaRepository danhGias;
    private final DangKyRepository dangKys;
    private final SinhVienRepository sinhViens;
    private final ThanhVienNhomRepository thanhViens;
    private final QuyTacDanhGia rules;
    private final Clock clock;
    private final org.springframework.context.ApplicationEventPublisher events;
    public KetQuaService(KetQuaRepository ketQuas, ChiTietKetQuaRepository chiTiets, PhanCongChamRepository phanCongs,
            PhieuChamRepository phieus, YeuCauSuaDiemRepository yeuCaus, DanhGiaRepository danhGias,
            DangKyRepository dangKys, SinhVienRepository sinhViens, ThanhVienNhomRepository thanhViens,
            QuyTacDanhGia rules, Clock clock, org.springframework.context.ApplicationEventPublisher events) {
        this.events = events;
        this.ketQuas = ketQuas;
        this.chiTiets = chiTiets;
        this.phanCongs = phanCongs;
        this.phieus = phieus;
        this.yeuCaus = yeuCaus;
        this.danhGias = danhGias;
        this.dangKys = dangKys;
        this.sinhViens = sinhViens;
        this.thanhViens = thanhViens;
        this.rules = rules;
        this.clock = clock;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Integer tongHop(Integer dgId, @Valid TongHopForm f) {
        DanhGia d = rules.khoa(dgId);
        kiemTraNguoiTongHop(d);
        rules.duocDieuChinh(d);
        var latest = phieuHopLe(d);
        if (!ketQuas
                .findByDanhGiaIdAndTrangThaiIn(dgId, List.of(TrangThaiKetQua.CHO_XAC_NHAN, TrangThaiKetQua.DA_XAC_NHAN))
                .isEmpty())
            throw new NghiepVuException("Đã có kết quả đang chờ xử lý; xác nhận/công bố hoặc hủy bản đó trước");
        var old = ketQuas.findFirstByDanhGiaIdAndTrangThaiOrderByPhienBanDesc(dgId, TrangThaiKetQua.DA_CONG_BO);
        if (old.isPresent() && nguonSnapshot(old.get()).equals(nguon(latest)))
            throw new NghiepVuException("Chưa có phiên bản điểm mới để tổng hợp lại");
        BigDecimal sum = latest.values().stream().map(PhieuCham::getDiem).reduce(BigDecimal.ZERO, BigDecimal::add);
        KetQua k = ketQuas.saveAndFlush(new KetQua(d, ketQuas.phienBanCuoi(dgId) + 1, latest.size(), sum,
                f.getNhanXet().trim(), rules.actor()));
        for (var p : latest.values())
            chiTiets.save(new ChiTietKetQua(k, p));
        chiTiets.flush();
        return k.getId();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public void xacNhan(Integer id) {
        KetQua k = khoa(id);
        if (k.getTrangThai() != TrangThaiKetQua.CHO_XAC_NHAN)
            throw new NghiepVuException("Kết quả không còn chờ xác nhận");
        kiemTraMoiNhat(k);
        k.xacNhan(rules.actor(), ThoiGian.hienTai(clock));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public void congBo(Integer id) {
        KetQua k = khoa(id);
        if (k.getTrangThai() != TrangThaiKetQua.DA_XAC_NHAN)
            throw new NghiepVuException("Chỉ công bố kết quả đã xác nhận");
        kiemTraMoiNhat(k);
        k.congBo(rules.actor(), ThoiGian.hienTai(clock));
        ketQuas.flush();
        var nhom = k.getDanhGia().getDangKy().getNhom();
        var nguoiNhans = thanhViens.findByNhomIdOrderBySinhVienMaSvAsc(nhom.getId()).stream()
                .map(tv -> tv.getSinhVien().getTaiKhoan().getEmail()).distinct().toList();
        events.publishEvent(new com.khoacntt.quanlydetai.email.dto.ThongBaoKetQua(
                k.getId(), k.getPhienBan(), k.getDanhGia().getDangKy().getDeTai().getTenDeTai(),
                nhom.getDot().getTenDot(), nguoiNhans));
        k.getDanhGia().getDangKy().getNhom().chuyenTrangThai(TrangThaiNhom.HOAN_THANH);
        yeuCaus.findByPhanCongChamDanhGiaIdAndTrangThaiIn(k.getDanhGia().getId(),
                List.of(TrangThaiYeuCauSuaDiem.DA_NOP_DIEU_CHINH)).forEach(y -> y.hoanTat(ThoiGian.hienTai(clock)));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void huy(Integer id) {
        KetQua k = khoa(id);
        if (!rules.laKhoa() && !rules.laChuTich(k.getDanhGia()))
            throw new AccessDeniedException("Chỉ khoa hoặc chủ tịch được hủy bản chưa công bố");
        if (k.getTrangThai() != TrangThaiKetQua.CHO_XAC_NHAN && k.getTrangThai() != TrangThaiKetQua.DA_XAC_NHAN)
            throw new NghiepVuException("Không hủy kết quả đã công bố hoặc đã hủy");
        k.huy();
    }
    private void kiemTraNguoiTongHop(DanhGia d) {
        if (d.getHoiDong() != null) {
            if (!rules.laChuTich(d))
                throw new AccessDeniedException("Chỉ chủ tịch hội đồng của đề tài được tổng hợp");
        } else if (!rules.laKhoa())
            throw new AccessDeniedException("Khoa tổng hợp đề tài chỉ có phản biện riêng");
    }
    private Map<Integer, PhieuCham> phieuHopLe(DanhGia d) {
        if (d.getTrangThaiPhanBien() != TrangThaiPhanBien.HOAN_TAT
                || (d.getTrangThaiHoiDong() != TrangThaiDanhGiaHoiDong.KHONG_AP_DUNG
                        && d.getTrangThaiHoiDong() != TrangThaiDanhGiaHoiDong.HOAN_TAT))
            throw new NghiepVuException("Phản biện/hội đồng chưa hoàn tất");
        if (!yeuCaus.findByPhanCongChamDanhGiaIdAndTrangThaiIn(d.getId(),
                List.of(TrangThaiYeuCauSuaDiem.CHO_DUYET, TrangThaiYeuCauSuaDiem.CHAP_THUAN)).isEmpty())
            throw new NghiepVuException("Còn yêu cầu sửa điểm chưa xử lý hoặc chưa nộp điều chỉnh");
        Map<Integer, PhieuCham> result = new LinkedHashMap<>();
        for (PhanCongCham pc : phanCongs.findByDanhGiaIdOrderByIdAsc(d.getId())) {
            rules.khongLaHuongDan(d.getDangKy().getDeTai().getId(), pc.getGiangVien());
            var p = phieus
                    .findFirstByPhanCongChamIdAndTrangThaiOrderByPhienBanDesc(pc.getId(), TrangThaiPhieuCham.DA_NOP)
                    .orElseThrow(() -> new NghiepVuException("Chưa đủ phiếu chấm bắt buộc"));
            result.put(pc.getId(), p);
        }
        if (result.isEmpty() || result.size() > 6)
            throw new NghiepVuException("Số người chấm không hợp lệ");
        return result;
    }
    private void kiemTraMoiNhat(KetQua k) {
        rules.duocDieuChinh(k.getDanhGia());
        var latest = phieuHopLe(k.getDanhGia());
        BigDecimal sum = latest.values().stream().map(PhieuCham::getDiem).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!nguonSnapshot(k).equals(nguon(latest)) || k.getSoNguoiCham() != latest.size()
                || sum.compareTo(k.getTongDiem()) != 0)
            throw new NghiepVuException("Điểm đã thay đổi; hủy bản chưa công bố và tổng hợp lại");
    }
    private Map<Integer, Integer> nguon(Map<Integer, PhieuCham> phieus) {
        Map<Integer, Integer> map = new HashMap<>();
        phieus.forEach((pc, p) -> map.put(pc, p.getId()));
        return map;
    }
    private Map<Integer, Integer> nguonSnapshot(KetQua k) {
        Map<Integer, Integer> map = new HashMap<>();
        chiTiets.findByKetQuaIdOrderByPhanCongChamIdAsc(k.getId())
                .forEach(c -> map.put(c.getPhanCongCham().getId(), c.getPhieuCham().getId()));
        return map;
    }
    private KetQua khoa(Integer id) {
        Integer dg = ketQuas.timDanhGiaId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy kết quả"));
        rules.khoa(dg);
        return ketQuas.findById(id).orElseThrow();
    }
    public List<KetQuaDto> cuaDanhGia(Integer dgId) {
        rules.kiemTraXem(rules.tim(dgId));
        return danhSachDto(ketQuas.findByDanhGiaIdOrderByPhienBanDesc(dgId));
    }
    public Integer danhGiaCuaKetQua(Integer id) {
        Integer dg = ketQuas.timDanhGiaId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy kết quả"));
        rules.kiemTraXem(rules.tim(dg));
        return dg;
    }
    @PreAuthorize("hasRole('SINH_VIEN')")
    public List<KetQuaSinhVienDto> cuaSinhVien() {
        var sv = sinhViens.findByTaiKhoanId(rules.actor().getId())
                .orElseThrow(() -> new NghiepVuException("Bạn cần có hồ sơ sinh viên"));
        return cuaSinhVien(sv.getId(), null);
    }
    private List<KetQuaSinhVienDto> cuaSinhVien(Integer svId, Integer dotId) {
        var ds = dangKys.cuaSinhVienTrongDot(svId, dotId);
        if (ds.isEmpty()) return List.of();
        var congBo = ketQuas.congBoMoiNhat(ds.stream().map(d -> d.getId()).toList());
        var dtos = danhSachDto(congBo).stream().collect(java.util.stream.Collectors.toMap(KetQuaDto::id, k -> k));
        Map<Integer, KetQuaDto> theoDangKy = new HashMap<>();
        congBo.forEach(k -> theoDangKy.put(k.getDanhGia().getDangKy().getId(), dtos.get(k.getId())));
        return ds.stream().map(d -> new KetQuaSinhVienDto(d.getId(), d.getNhom().getDot().getId(),
                d.getNhom().getDot().getTenDot(), d.getNhom().getMaNhom(), d.getDeTai().getTenDeTai(),
                theoDangKy.get(d.getId()))).toList();
    }
    @PreAuthorize("hasRole('SINH_VIEN')")
    public Map<String, Object> traCuuSinhVien(Integer dotId) {
        var sv = sinhViens.findByTaiKhoanId(rules.actor().getId())
                .orElseThrow(() -> new NghiepVuException("Bạn cần có hồ sơ sinh viên"));
        Map<Integer, String> dots = new LinkedHashMap<>();
        dangKys.dotCuaSinhVien(sv.getId()).forEach(d -> dots.put(d.getId(), d.getTenDot()));
        if (dotId != null && !dots.containsKey(dotId))
            throw new AccessDeniedException("Bạn không có đăng ký được chấp thuận trong đợt này");
        return Map.of("cacDot", dots, "danhSach", cuaSinhVien(sv.getId(), dotId));
    }

    private List<KetQuaDto> danhSachDto(List<KetQua> ds) {
        if (ds.isEmpty()) return List.of();
        var dangDieuChinh = yeuCaus.danhGiaDangDieuChinh(ds.stream().map(k -> k.getDanhGia().getId()).distinct().toList(),
                QuyTacDanhGia.DANG_XU_LY);
        var theoKetQua = chiTiets.findByKetQuaIdInOrderByPhanCongChamIdAsc(ds.stream().map(KetQua::getId).toList()).stream()
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getKetQua().getId()));
        return ds.stream().map(k -> dto(k, dangDieuChinh.contains(k.getDanhGia().getId()),
                theoKetQua.getOrDefault(k.getId(), List.of()))).toList();
    }
    private KetQuaDto dto(KetQua k, boolean dieuChinh, List<ChiTietKetQua> ds) {
        return new KetQuaDto(k.getId(), k.getPhienBan(), k.getDiemCuoi(), k.getDat() != null && k.getDat() == 1,
                k.getNhanXetTongHop(), k.getTrangThai(), dieuChinh,
                ds.stream()
                        .map(c -> new KetQuaDto.DiemDto(c.getPhanCongCham().getGiangVien().getTaiKhoan().getHoTen(),
                                c.getPhieuCham().getPhienBan(), c.getPhieuCham().getDiem(),
                                c.getPhieuCham().getNhanXet()))
                        .toList());
    }
}
