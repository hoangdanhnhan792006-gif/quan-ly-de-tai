package com.khoacntt.quanlydetai.danhgia.service;

import com.khoacntt.quanlydetai.baocao.model.TrangThaiBaoCao;
import com.khoacntt.quanlydetai.baocao.repository.*;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dangky.model.*;
import com.khoacntt.quanlydetai.dangky.repository.DangKyRepository;
import com.khoacntt.quanlydetai.danhgia.form.KeHoachDanhGiaForm;
import com.khoacntt.quanlydetai.danhgia.model.*;
import com.khoacntt.quanlydetai.danhgia.repository.*;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.hoso.repository.GiangVienRepository;
import com.khoacntt.quanlydetai.nhom.service.QuyTacNhom;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.time.*;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
public class DanhGiaService {
    private final DanhGiaRepository danhGias;
    private final DangKyRepository dangKys;
    private final HoiDongRepository hoiDongs;
    private final ThanhVienHoiDongRepository thanhViens;
    private final PhanCongChamRepository phanCongs;
    private final PhieuChamRepository phieus;
    private final BaoCaoRepository baoCaos;
    private final BaoCaoTepRepository teps;
    private final GiangVienRepository giangViens;
    private final QuyTacDanhGia rules;
    private final QuyTacNhom nhom;
    private final Clock clock;
    public DanhGiaService(DanhGiaRepository danhGias, DangKyRepository dangKys, HoiDongRepository hoiDongs,
            ThanhVienHoiDongRepository thanhViens, PhanCongChamRepository phanCongs, PhieuChamRepository phieus,
            BaoCaoRepository baoCaos, BaoCaoTepRepository teps, GiangVienRepository giangViens, QuyTacDanhGia rules,
            QuyTacNhom nhom, Clock clock) {
        this.danhGias = danhGias;
        this.dangKys = dangKys;
        this.hoiDongs = hoiDongs;
        this.thanhViens = thanhViens;
        this.phanCongs = phanCongs;
        this.phieus = phieus;
        this.baoCaos = baoCaos;
        this.teps = teps;
        this.giangViens = giangViens;
        this.rules = rules;
        this.nhom = nhom;
        this.clock = clock;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Integer lap(@Valid KeHoachDanhGiaForm f) {
        Integer nId = dangKys.timNhomId(f.getDangKyId())
                .orElseThrow(() -> new KhongTimThayException("Không tìm thấy đăng ký"));
        nhom.khoaNhom(nId, null);
        DangKy dk = dangKys.findById(f.getDangKyId()).orElseThrow();
        if (dk.getTrangThai() != TrangThaiDangKy.CHAP_THUAN)
            throw new NghiepVuException("Chỉ lập đánh giá cho đăng ký đã chấp thuận");
        if (danhGias.findByDangKyId(dk.getId()).isPresent())
            throw new NghiepVuException("Đăng ký đã có lần đánh giá; không hỗ trợ đánh giá lại");
        rules.dangTrienKhai(dk.getNhom().getDot());
        DanhGia dg = danhGias
                .saveAndFlush(new DanhGia(dk, rules.canHoiDong(dk.getNhom().getDot().getLoai()), rules.actor()));
        lapKeHoach(dg, f);
        return dg.getId();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void sua(Integer id, @Valid KeHoachDanhGiaForm f) {
        DanhGia dg = rules.khoa(id);
        rules.dangTrienKhai(dg.getDangKy().getNhom().getDot());
        if (!dg.getDangKy().getId().equals(f.getDangKyId()))
            throw new NghiepVuException("Không chuyển đánh giá sang đăng ký khác");
        if (dg.getTrangThaiPhanBien() != TrangThaiPhanBien.CHUA_BAT_DAU || phieus.existsByPhanCongChamDanhGiaId(id))
            throw new NghiepVuException("Chỉ sửa kế hoạch trước khi bắt đầu chấm");
        lapKeHoach(dg, f);
    }
    public KeHoachDanhGiaForm formSua(Integer id) {
        DanhGia d = rules.tim(id);
        var f = new KeHoachDanhGiaForm();
        f.setDangKyId(d.getDangKy().getId());
        phanCongs.findByDanhGiaIdOrderByIdAsc(id).stream().filter(PhanCongCham::isPhanBien).findFirst()
                .ifPresent(p -> f.setPhanBienId(p.getGiangVien().getId()));
        f.setHoiDongId(d.getHoiDong() == null ? null : d.getHoiDong().getId());
        f.setLichPhanBienLuc(ThoiGian.sangGioVietNam(d.getLichPhanBienLuc()));
        f.setDiaDiemPhanBien(d.getDiaDiemPhanBien());
        f.setHoiDongBatDau(ThoiGian.sangGioVietNam(d.getHoiDongBatDau()));
        f.setHoiDongKetThuc(ThoiGian.sangGioVietNam(d.getHoiDongKetThuc()));
        f.setDiaDiemHoiDong(d.getDiaDiemHoiDong());
        return f;
    }
    private void lapKeHoach(DanhGia dg, KeHoachDanhGiaForm f) {
        var dot = dg.getDangKy().getNhom().getDot();
        boolean coHd = rules.canHoiDong(dot.getLoai());
        LocalDateTime pb = ThoiGian.sangUtc(f.getLichPhanBienLuc());
        LocalDateTime start = ThoiGian.sangUtc(f.getHoiDongBatDau()), end = ThoiGian.sangUtc(f.getHoiDongKetThuc());
        if (pb.isBefore(dot.getHanNopBaoCao()))
            throw new NghiepVuException("Lịch phản biện phải từ hạn nộp báo cáo trở đi");
        if (dot.getHanNopDiemPb() != null && !pb.isBefore(dot.getHanNopDiemPb()))
            throw new NghiepVuException("Lịch phản biện phải trước hạn nộp điểm PB");
        HoiDong hd = null;
        List<ThanhVienHoiDong> tvs = List.of();
        if (coHd) {
            if (f.getHoiDongId() == null || start == null || end == null || !start.isBefore(end)
                    || f.getDiaDiemHoiDong() == null || f.getDiaDiemHoiDong().isBlank())
                throw new NghiepVuException("Cần chọn hội đồng, khoảng giờ hợp lệ và địa điểm");
            if (!pb.isBefore(start))
                throw new NghiepVuException("Hội đồng phải diễn ra sau lịch phản biện");
            if (dot.getLoai() == LoaiDeTai.KLTN && !f.getHoiDongBatDau().toLocalDate().equals(dot.getNgayBaoCaoHd()))
                throw new NghiepVuException("Lịch hội đồng KLTN phải đúng ngày báo cáo của đợt");
            hd = hoiDongs.findById(f.getHoiDongId())
                    .orElseThrow(() -> new KhongTimThayException("Không tìm thấy hội đồng"));
            if (!hd.getDot().getId().equals(dot.getId()) || hd.getTrangThai() != TrangThaiHoiDong.DA_CHOT)
                throw new NghiepVuException("Chọn hội đồng đã chốt thuộc cùng đợt");
            tvs = thanhViens.findByHoiDongIdOrderByGiangVienMaGvAsc(hd.getId());
            if (tvs.size() < 3 || tvs.size() > 5)
                throw new NghiepVuException("Hội đồng phải có 3–5 giảng viên");
        } else if (f.getHoiDongId() != null || start != null || end != null
                || (f.getDiaDiemHoiDong() != null && !f.getDiaDiemHoiDong().isBlank()))
            throw new NghiepVuException("Môn học/TLCN chỉ dùng GVPB riêng, để trống thông tin hội đồng");
        Set<Integer> ids = new TreeSet<>();
        ids.add(f.getPhanBienId());
        tvs.forEach(t -> ids.add(t.getGiangVien().getId()));
        // Cùng GV ở nhiều đợt: khóa GV theo ID trước khi kiểm tra lịch, tránh đặt trùng
        // đồng thời.
        for (Integer id : ids)
            giangViens.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy giảng viên"));
        for (Integer id : ids)
            rules.khongLaHuongDan(dg.getDangKy().getDeTai().getId(), rules.giangVien(id));
        kiemTraLich(dg.getId(), dot.getId(), f.getPhanBienId(), pb, tvs, start, end, f.getDiaDiemHoiDong());
        phanCongs.deleteAll(phanCongs.findByDanhGiaIdOrderByIdAsc(dg.getId()));
        phanCongs.flush();
        Set<Integer> hdIds = new HashSet<>();
        tvs.forEach(t -> hdIds.add(t.getGiangVien().getId()));
        for (Integer id : ids)
            phanCongs.save(new PhanCongCham(dg, rules.giangVien(id), id.equals(f.getPhanBienId()), hdIds.contains(id),
                    rules.actor()));
        dg.xepLich(hd, pb, f.getDiaDiemPhanBien().trim(), start, end,
                // Môn học/TLCN không có hội đồng: CHECK yêu cầu NULL, không phải chuỗi rỗng từ form.
                coHd ? f.getDiaDiemHoiDong().trim() : null);
        phanCongs.flush();
        danhGias.flush();
    }
    private void kiemTraLich(Integer dgId, Integer dotId, Integer pbId, LocalDateTime pb, List<ThanhVienHoiDong> tvs,
            LocalDateTime start, LocalDateTime end, String place) {
        Set<Integer> hdIds = new HashSet<>();
        tvs.forEach(t -> hdIds.add(t.getGiangVien().getId()));
        Set<Integer> lienQuan = new HashSet<>(hdIds);
        lienQuan.add(pbId);
        var lichs = danhGias.lichCoTheTrung(dgId, dotId, pb, start, end, lienQuan, place == null ? null : place.trim());
        if (lichs.isEmpty()) return;
        var phanCongTheoLich = phanCongs.findByDanhGiaIdInOrderByIdAsc(
                lichs.stream().map(DanhGia::getId).toList()).stream()
                .collect(java.util.stream.Collectors.groupingBy(p -> p.getDanhGia().getId()));
        for (DanhGia other : lichs) {
            var pcs = phanCongTheoLich.getOrDefault(other.getId(), List.of());
            Set<Integer> otherHd = new HashSet<>();
            pcs.stream().filter(PhanCongCham::isThanhVienHoiDong).forEach(p -> otherHd.add(p.getGiangVien().getId()));
            Integer otherPb = pcs.stream().filter(PhanCongCham::isPhanBien).map(p -> p.getGiangVien().getId())
                    .findFirst().orElse(null);
            if ((pb.equals(other.getLichPhanBienLuc()) && pbId.equals(otherPb))
                    || (trongKhoang(pb, other.getHoiDongBatDau(), other.getHoiDongKetThuc()) && otherHd.contains(pbId))
                    || (trongKhoang(other.getLichPhanBienLuc(), start, end) && hdIds.contains(otherPb)))
                throw new NghiepVuException("Giảng viên bị trùng lịch phản biện/hội đồng");
            if (start != null && other.getHoiDongBatDau() != null && start.isBefore(other.getHoiDongKetThuc())
                    && end.isAfter(other.getHoiDongBatDau())) {
                if (hdIds.stream().anyMatch(otherHd::contains))
                    throw new NghiepVuException("Giảng viên bị trùng giờ hội đồng");
                if (dotId.equals(other.getDotId()) && place.trim().equalsIgnoreCase(other.getDiaDiemHoiDong()))
                    throw new NghiepVuException("Địa điểm hội đồng bị trùng giờ trong đợt");
            }
        }
    }
    private boolean trongKhoang(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        return value != null && start != null && end != null && !value.isBefore(start) && value.isBefore(end);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void batDauPhanBien(Integer id) {
        DanhGia d = rules.khoa(id);
        var dot = d.getDangKy().getNhom().getDot();
        rules.dangTrienKhai(dot);
        var now = ThoiGian.hienTai(clock);
        if (d.getTrangThaiPhanBien() != TrangThaiPhanBien.CHUA_BAT_DAU)
            throw new NghiepVuException("Phản biện đã bắt đầu");
        if (now.isBefore(dot.getHanNopBaoCao()) || d.getLichPhanBienLuc() == null
                || now.isBefore(d.getLichPhanBienLuc()))
            throw new NghiepVuException("Chỉ bắt đầu phản biện sau hạn báo cáo và đến giờ trong kế hoạch");
        if (dot.getHanNopDiemPb() != null && !now.isBefore(dot.getHanNopDiemPb()))
            throw new NghiepVuException("Đã hết hạn nộp điểm phản biện");
        var pcs = phanCongs.findByDanhGiaIdOrderByIdAsc(id);
        if (pcs.stream().filter(PhanCongCham::isPhanBien).count() != 1)
            throw new NghiepVuException("Cần đúng một GVPB");
        pcs.forEach(p -> {
            rules.kiemTraGv(p.getGiangVien());
            rules.khongLaHuongDan(d.getDangKy().getDeTai().getId(), p.getGiangVien());
        });
        var bc = baoCaos
                .findFirstByDangKyIdAndTrangThaiOrderByPhienBanDesc(d.getDangKy().getId(), TrangThaiBaoCao.DA_NOP)
                .orElseThrow(() -> new NghiepVuException("Nhóm chưa nộp báo cáo hợp lệ"));
        if (teps.findByBaoCaoIdOrderByIdAsc(bc.getId()).size() != 3)
            throw new NghiepVuException("Bộ báo cáo thiếu ba tài liệu bắt buộc");
        d.batDauPhanBien(bc);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void batDauHoiDong(Integer id) {
        DanhGia d = rules.khoa(id);
        rules.dangTrienKhai(d.getDangKy().getNhom().getDot());
        if (d.getTrangThaiPhanBien() != TrangThaiPhanBien.HOAN_TAT
                || d.getTrangThaiHoiDong() != TrangThaiDanhGiaHoiDong.CHUA_BAT_DAU)
            throw new NghiepVuException("Chỉ mở hội đồng sau khi phản biện hoàn tất");
        if (d.getHoiDong() == null || d.getHoiDongBatDau() == null
                || ThoiGian.hienTai(clock).isBefore(d.getHoiDongBatDau()))
            throw new NghiepVuException("Chưa đến lịch hội đồng");
        phanCongs.findByDanhGiaIdOrderByIdAsc(id).forEach(p -> rules.kiemTraGv(p.getGiangVien()));
        d.batDauHoiDong();
    }
}
