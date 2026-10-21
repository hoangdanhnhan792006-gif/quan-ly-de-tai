package com.khoacntt.quanlydetai.danhgia.service;
import com.khoacntt.quanlydetai.danhgia.dto.DanhGiaDto;
import com.khoacntt.quanlydetai.danhgia.model.*;
import com.khoacntt.quanlydetai.danhgia.repository.*;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.*;
@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_KHOA','CAN_BO_KHOA')")
public class DanhGiaTruyVanService {
    private final DanhGiaRepository danhGias;
    private final PhanCongChamRepository phanCongs;
    private final PhieuChamRepository phieus;
    private final YeuCauSuaDiemRepository yeuCaus;
    private final QuyTacDanhGia rules;
    private final KetQuaService ketQua;
    private final Clock clock;
    public DanhGiaTruyVanService(DanhGiaRepository danhGias, PhanCongChamRepository phanCongs,
            PhieuChamRepository phieus, YeuCauSuaDiemRepository yeuCaus, QuyTacDanhGia rules, KetQuaService ketQua,
            Clock clock) {
        this.danhGias = danhGias;
        this.phanCongs = phanCongs;
        this.phieus = phieus;
        this.yeuCaus = yeuCaus;
        this.rules = rules;
        this.ketQua = ketQua;
        this.clock = clock;
    }
    public List<DanhGiaDto> danhSach() {
        var actor = rules.actor();
        boolean laKhoa = actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.TRUONG_KHOA)
                || actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.CAN_BO_KHOA);
        var ds = danhGias.duocXem(actor.getId(), laKhoa,
                actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.GIANG_VIEN));
        var chuTich = rules.hoiDongChuTich(ds.stream().filter(d -> d.getHoiDong() != null)
                .map(d -> d.getHoiDong().getId()).distinct().toList(), actor);
        return ds.stream().map(d -> dto(d, false, laKhoa,
                d.getHoiDong() != null && chuTich.contains(d.getHoiDong().getId()))).toList();
    }
    public DanhGiaDto chiTiet(Integer id) {
        var d = rules.tim(id);
        rules.kiemTraXem(d);
        return dto(d, true);
    }
    public Integer danhGiaCuaPhanCong(Integer id) {
        Integer dg = phanCongs.timDanhGiaId(id).orElseThrow(
                () -> new com.khoacntt.quanlydetai.common.exception.KhongTimThayException("Không tìm thấy phân công"));
        rules.kiemTraXem(rules.tim(dg));
        return dg;
    }
    public Integer danhGiaCuaYeuCau(Integer id) {
        Integer dg = yeuCaus.timDanhGiaId(id).orElseThrow(
                () -> new com.khoacntt.quanlydetai.common.exception.KhongTimThayException("Không tìm thấy yêu cầu"));
        rules.kiemTraXem(rules.tim(dg));
        return dg;
    }
    private DanhGiaDto dto(DanhGia d, boolean dayDu) {
        return dto(d, dayDu, rules.laKhoa(), rules.laChuTich(d));
    }
    private DanhGiaDto dto(DanhGia d, boolean dayDu, boolean laKhoa, boolean laChuTich) {
        List<DanhGiaDto.PhanCongDto> pcs = new ArrayList<>();
        var dsPhanCong = dayDu ? phanCongs.findByDanhGiaIdOrderByIdAsc(d.getId()) : List.<PhanCongCham>of();
        var ids = dsPhanCong.stream().map(PhanCongCham::getId).toList();
        var phieuTheoPhanCong = ids.isEmpty() ? Map.<Integer, List<PhieuCham>>of()
                : phieus.findByPhanCongChamIdInOrderByPhienBanDesc(ids).stream()
                        .collect(java.util.stream.Collectors.groupingBy(p -> p.getPhanCongCham().getId()));
        var yeuCauTheoPhanCong = ids.isEmpty() ? Map.<Integer, List<YeuCauSuaDiem>>of()
                : yeuCaus.findByPhanCongChamIdInOrderByIdDesc(ids).stream()
                        .collect(java.util.stream.Collectors.groupingBy(y -> y.getPhanCongCham().getId()));
        var actorId = dayDu ? rules.actor().getId() : null;
        if (dayDu)
            for (var p : dsPhanCong) {
                var phs = phieuTheoPhanCong.getOrDefault(p.getId(), List.of());
                var ycs = yeuCauTheoPhanCong.getOrDefault(p.getId(), List.of());
                boolean own = p.getGiangVien().getTaiKhoan().getId().equals(actorId);
                boolean nop = phs.stream().anyMatch(x -> x.getTrangThai() == TrangThaiPhieuCham.DA_NOP);
                boolean approved = ycs.stream().anyMatch(y -> y.getTrangThai() == TrangThaiYeuCauSuaDiem.CHAP_THUAN);
                boolean active = ycs.stream().anyMatch(y -> QuyTacDanhGia.DANG_XU_LY.contains(y.getTrangThai()));
                var dot = d.getDangKy().getNhom().getDot();
                boolean phase = p.isPhanBien()
                        ? d.getTrangThaiPhanBien() == TrangThaiPhanBien.DANG_CHAM && (dot.getHanNopDiemPb() == null
                                || ThoiGian.hienTai(clock).isBefore(dot.getHanNopDiemPb()))
                        : d.getTrangThaiHoiDong() == TrangThaiDanhGiaHoiDong.DANG_CHAM;
                boolean dangTrienKhai = dot
                        .getTrangThai() == com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy.TRIEN_KHAI;
                boolean xuLyDiem = dangTrienKhai
                        || dot.getTrangThai() == com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy.KET_THUC;
                pcs.add(new DanhGiaDto.PhanCongDto(p.getId(), p.getGiangVien().getTaiKhoan().getHoTen(), p.isPhanBien(),
                        p.isThanhVienHoiDong(), own, own && xuLyDiem && (approved || (!nop && phase && dangTrienKhai)),
                        own && xuLyDiem && nop && !active,
                        phs.stream()
                                .map(x -> new DanhGiaDto.PhieuDto(x.getId(), x.getPhienBan(), x.getDiem(),
                                        x.getNhanXet(), x.getTrangThai()))
                                .toList(),
                        ycs.stream().map(y -> new DanhGiaDto.YeuCauDto(y.getId(), y.getLyDo(), y.getTrangThai(),
                                y.getNhanXetDuyet())).toList()));
            }
        return new DanhGiaDto(d.getId(), d.getDangKy().getId(), d.getDangKy().getNhom().getMaNhom(),
                d.getDangKy().getDeTai().getTenDeTai(), d.getDangKy().getNhom().getDot().getTenDot(),
                d.getTrangThaiPhanBien(), d.getTrangThaiHoiDong(),
                d.getHoiDong() == null ? null : d.getHoiDong().getTenHoiDong(),
                ThoiGian.sangGioVietNam(d.getLichPhanBienLuc()), d.getDiaDiemPhanBien(),
                ThoiGian.sangGioVietNam(d.getHoiDongBatDau()), ThoiGian.sangGioVietNam(d.getHoiDongKetThuc()),
                d.getDiaDiemHoiDong(), d.getBaoCaoChot() == null ? null : d.getBaoCaoChot().getPhienBan(),
                laKhoa, laChuTich, pcs, dayDu ? ketQua.cuaDanhGia(d.getId()) : List.of());
    }
}
