package com.khoacntt.quanlydetai.danhgia.service;

import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.danhgia.form.*;
import com.khoacntt.quanlydetai.danhgia.model.*;
import com.khoacntt.quanlydetai.danhgia.repository.*;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.time.Clock;
import java.math.BigDecimal;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasRole('GIANG_VIEN')")
public class ChamDiemService {
    private final PhieuChamRepository phieus;
    private final PhanCongChamRepository phanCongs;
    private final YeuCauSuaDiemRepository yeuCaus;
    private final KetQuaRepository ketQuas;
    private final QuyTacDanhGia rules;
    private final Clock clock;
    public ChamDiemService(PhieuChamRepository phieus, PhanCongChamRepository phanCongs,
            YeuCauSuaDiemRepository yeuCaus, KetQuaRepository ketQuas, QuyTacDanhGia rules, Clock clock) {
        this.phieus = phieus;
        this.phanCongs = phanCongs;
        this.yeuCaus = yeuCaus;
        this.ketQuas = ketQuas;
        this.rules = rules;
        this.clock = clock;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void luu(Integer pcId, @Valid PhieuChamForm f) {
        PhanCongCham pc = rules.khoaPhanCong(pcId);
        rules.chuPhanCong(pc);
        rules.duocDieuChinh(pc.getDanhGia());
        var yc = yeuCaus.findFirstByPhanCongChamIdAndTrangThaiInOrderByIdDesc(pcId,
                List.of(TrangThaiYeuCauSuaDiem.CHAP_THUAN));
        var daNop = phieus.findFirstByPhanCongChamIdAndTrangThaiOrderByPhienBanDesc(pcId, TrangThaiPhieuCham.DA_NOP);
        if (yc.isEmpty()) {
            if (daNop.isPresent())
                throw new NghiepVuException("Phiếu đã nộp; cần yêu cầu sửa điểm được khoa chấp thuận");
            kiemTraGiaiDoan(pc);
        }
        if (Boolean.TRUE.equals(f.getNop())
                && (f.getDiem() == null || f.getNhanXet() == null || f.getNhanXet().isBlank()))
            throw new NghiepVuException("Nộp điểm cần đủ điểm 0–10 và nhận xét");
        PhieuCham p = phieus.findFirstByPhanCongChamIdAndTrangThaiOrderByPhienBanDesc(pcId, TrangThaiPhieuCham.NHAP)
                .orElseGet(() -> new PhieuCham(pc, phieus.phienBanCuoi(pcId) + 1, yc.orElse(null), rules.actor()));
        if (!Objects.equals(p.getYeuCauSuaDiem() == null ? null : p.getYeuCauSuaDiem().getId(),
                yc.map(YeuCauSuaDiem::getId).orElse(null)))
            throw new NghiepVuException("Phiếu nháp không khớp yêu cầu sửa hiện tại");
        p.luuNhap(f.getDiem(), f.getNhanXet() == null ? null : f.getNhanXet().trim());
        if (Boolean.TRUE.equals(f.getNop()))
            p.nop(ThoiGian.hienTai(clock));
        phieus.saveAndFlush(p);
        if (Boolean.TRUE.equals(f.getNop())) {
            if (yc.isPresent())
                yc.get().daNop();
            else {
                if (pc.isPhanBien())
                    pc.getDanhGia().xongPhanBien(ThoiGian.hienTai(clock));
                if (pc.getDanhGia().getTrangThaiHoiDong() == TrangThaiDanhGiaHoiDong.DANG_CHAM) {
                    boolean du = phanCongs.findByDanhGiaIdOrderByIdAsc(pc.getDanhGia().getId()).stream()
                            .filter(PhanCongCham::isThanhVienHoiDong)
                            .allMatch(t -> phieus.findFirstByPhanCongChamIdAndTrangThaiOrderByPhienBanDesc(t.getId(),
                                    TrangThaiPhieuCham.DA_NOP).isPresent());
                    if (du)
                        pc.getDanhGia().xongHoiDong(ThoiGian.hienTai(clock));
                }
            }
        }
    }
    private void kiemTraGiaiDoan(PhanCongCham pc) {
        DanhGia d = pc.getDanhGia();
        var dot = d.getDangKy().getNhom().getDot();
        rules.dangTrienKhai(dot);
        if (pc.isPhanBien()) {
            if (d.getTrangThaiPhanBien() != TrangThaiPhanBien.DANG_CHAM)
                throw new NghiepVuException("Chưa mở chấm phản biện hoặc đã hoàn tất");
            if (dot.getHanNopDiemPb() != null && !ThoiGian.hienTai(clock).isBefore(dot.getHanNopDiemPb()))
                throw new NghiepVuException("Đã hết hạn nộp điểm phản biện");
        } else if (!pc.isThanhVienHoiDong() || d.getTrangThaiHoiDong() != TrangThaiDanhGiaHoiDong.DANG_CHAM)
            throw new NghiepVuException("Chưa mở chấm hội đồng hoặc đã hoàn tất");
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void yeuCauSua(Integer pcId, @Valid YeuCauSuaDiemForm f) {
        PhanCongCham pc = rules.khoaPhanCong(pcId);
        rules.chuPhanCong(pc);
        rules.duocDieuChinh(pc.getDanhGia());
        if (yeuCaus.findFirstByPhanCongChamIdAndTrangThaiInOrderByIdDesc(pcId, QuyTacDanhGia.DANG_XU_LY).isPresent())
            throw new NghiepVuException("Phân công đã có yêu cầu sửa đang xử lý");
        PhieuCham goc = phieus.findFirstByPhanCongChamIdAndTrangThaiOrderByPhienBanDesc(pcId, TrangThaiPhieuCham.DA_NOP)
                .orElseThrow(() -> new NghiepVuException("Chưa có phiếu đã nộp để xin sửa"));
        yeuCaus.saveAndFlush(new YeuCauSuaDiem(goc, f.getLyDo().trim(), rules.actor(), ThoiGian.hienTai(clock)));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public void duyetYeuCau(Integer id, @Valid DuyetSuaDiemForm f) {
        YeuCauSuaDiem y = khoaYeuCau(id);
        rules.duocDieuChinh(y.getPhanCongCham().getDanhGia());
        if (y.getNguoiYeuCau().getId().equals(rules.actor().getId()))
            throw new AccessDeniedException("Không được tự duyệt yêu cầu sửa điểm");
        if (y.getTrangThai() != TrangThaiYeuCauSuaDiem.CHO_DUYET)
            throw new NghiepVuException("Yêu cầu không còn chờ duyệt");
        if (!f.getChapThuan() && (f.getNhanXet() == null || f.getNhanXet().isBlank()))
            throw new NghiepVuException("Cần nhận xét khi từ chối");
        y.duyet(f.getChapThuan(), rules.actor(), f.getNhanXet(), ThoiGian.hienTai(clock));
        if (f.getChapThuan()) {
            var pc = y.getPhanCongCham();
            PhieuCham p = new PhieuCham(pc, phieus.phienBanCuoi(pc.getId()) + 1, y, y.getNguoiYeuCau());
            p.luuNhap(y.getPhieuChamGoc().getDiem(), y.getPhieuChamGoc().getNhanXet());
            phieus.saveAndFlush(p);
            ketQuas.findByDanhGiaIdAndTrangThaiIn(pc.getDanhGia().getId(),
                    List.of(TrangThaiKetQua.CHO_XAC_NHAN, TrangThaiKetQua.DA_XAC_NHAN)).forEach(KetQua::huy);
        }
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void huyYeuCau(Integer id) {
        YeuCauSuaDiem y = khoaYeuCau(id);
        rules.chuPhanCong(y.getPhanCongCham());
        if (y.getTrangThai() != TrangThaiYeuCauSuaDiem.CHO_DUYET
                && y.getTrangThai() != TrangThaiYeuCauSuaDiem.CHAP_THUAN)
            throw new NghiepVuException("Chỉ hủy yêu cầu chưa nộp điểm điều chỉnh");
        phieus.findByYeuCauSuaDiemId(id).ifPresent(PhieuCham::huyNhap);
        y.huy();
    }
    private YeuCauSuaDiem khoaYeuCau(Integer id) {
        Integer dgId = yeuCaus.timDanhGiaId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy yêu cầu"));
        rules.khoa(dgId);
        return yeuCaus.findById(id).orElseThrow();
    }
}
