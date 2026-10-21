package com.khoacntt.quanlydetai.baocao.service;

import com.khoacntt.quanlydetai.baocao.dto.*;
import com.khoacntt.quanlydetai.baocao.model.*;
import com.khoacntt.quanlydetai.baocao.repository.*;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.storage.*;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dangky.model.*;
import com.khoacntt.quanlydetai.dangky.repository.DangKyRepository;
import com.khoacntt.quanlydetai.danhgia.repository.DanhGiaRepository;
import com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy;
import com.khoacntt.quanlydetai.nhom.service.QuyTacNhom;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.Clock;
import java.util.*;

@Service
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class BaoCaoService {
    private final BaoCaoRepository baoCaos;
    private final BaoCaoTepRepository teps;
    private final DangKyRepository dangKys;
    private final DanhGiaRepository danhGias;
    private final QuyenBaoCao quyen;
    private final QuyTacNhom nhom;
    private final Clock clock;
    public BaoCaoService(BaoCaoRepository baoCaos, BaoCaoTepRepository teps, DangKyRepository dangKys,
            DanhGiaRepository danhGias, QuyenBaoCao quyen, QuyTacNhom nhom, Clock clock) {
        this.baoCaos = baoCaos;
        this.teps = teps;
        this.dangKys = dangKys;
        this.danhGias = danhGias;
        this.quyen = quyen;
        this.nhom = nhom;
        this.clock = clock;
    }
    public List<HoSoBaoCaoDto> danhSach() {
        var actor = quyen.nguoiDung();
        var ds = dangKys.duocXemBaoCao(actor.getId(),
                actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.TRUONG_KHOA)
                        || actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.CAN_BO_KHOA),
                actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.SINH_VIEN),
                actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.GIANG_VIEN));
        if (ds.isEmpty()) return List.of();
        Set<Integer> duocNop = actor.coVaiTro(com.khoacntt.quanlydetai.account.model.VaiTro.SINH_VIEN)
                ? dangKys.duocNopBaoCao(ds.stream().map(DangKy::getId).toList(), actor.getId(), ThoiGian.hienTai(clock))
                : Set.of();
        return ds.stream().map(d -> new HoSoBaoCaoDto(d.getId(), d.getNhom().getMaNhom(),
                d.getDeTai().getTenDeTai(), ThoiGian.sangGioVietNam(d.getNhom().getDot().getHanNopBaoCao()),
                duocNop.contains(d.getId()), List.of())).toList();
    }
    public HoSoBaoCaoDto chiTiet(Integer dkId) {
        DangKy dk = tim(dkId);
        quyen.kiemTraXem(dk);
        return dto(dk, true);
    }
    @PreAuthorize("hasRole('SINH_VIEN')")
    public void kiemTraNop(Integer dkId) {
        kiemTra(tim(dkId));
    }
    private void kiemTra(DangKy dk) {
        nhom.truongNhom(dk.getNhom());
        if (dk.getTrangThai() != TrangThaiDangKy.CHAP_THUAN)
            throw new NghiepVuException("Nhóm chưa được chấp thuận đăng ký");
        if (dk.getNhom().getDot().getTrangThai() != TrangThaiDotDangKy.TRIEN_KHAI
                || !ThoiGian.hienTai(clock).isBefore(dk.getNhom().getDot().getHanNopBaoCao()))
            throw new NghiepVuException("Đã hết hạn nộp báo cáo hoặc đợt đã đóng");
        if (danhGias.findByDangKyId(dk.getId()).map(d -> d.getBaoCaoChot() != null).orElse(false))
            throw new NghiepVuException("Báo cáo đã được chốt để đánh giá");
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('SINH_VIEN')")
    public Integer nop(Integer dkId, String ghiChu, Map<LoaiTaiLieuBaoCao, TepDaLuu> files) {
        Integer nhomId = dangKys.timNhomId(dkId).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đăng ký"));
        nhom.khoaNhom(nhomId, null);
        DangKy dk = tim(dkId);
        kiemTra(dk);
        if (files == null || !files.keySet().equals(EnumSet.allOf(LoaiTaiLieuBaoCao.class))
                || files.values().stream().anyMatch(Objects::isNull))
            throw new NghiepVuException("Cần đủ báo cáo PDF, slide và mã nguồn");
        if (ghiChu != null && ghiChu.length() > 5000)
            throw new NghiepVuException("Ghi chú tối đa 5000 ký tự");
        var sv = nhom.sinhVienHienTai();
        BaoCao bc = baoCaos
                .saveAndFlush(new BaoCao(dk, baoCaos.phienBanCuoi(dkId) + 1, sv, ghiChu, ThoiGian.hienTai(clock)));
        for (var entry : files.entrySet())
            teps.save(new BaoCaoTep(bc, entry.getKey(), entry.getValue(), sv.getTaiKhoan()));
        teps.flush();
        return bc.getId();
    }
    public TaiTepDto taiTep(Integer id) {
        BaoCaoTep t = teps.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy tệp"));
        quyen.kiemTraXem(t.getBaoCao().getDangKy());
        return new TaiTepDto(t.getKhoaLuuTru(), t.getTenTepGoc(), t.getLoaiMime(), t.getKichThuocByte());
    }
    private DangKy tim(Integer id) {
        return dangKys.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đăng ký"));
    }
    private HoSoBaoCaoDto dto(DangKy dk, boolean dayDu) {
        boolean nop = false;
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SINH_VIEN"))) {
            try {
                kiemTra(dk);
                nop = true;
            } catch (NghiepVuException | org.springframework.security.access.AccessDeniedException ignored) {
            }
        }
        var cacBaoCao = dayDu ? baoCaos.findByDangKyIdOrderByPhienBanDesc(dk.getId()) : List.<BaoCao>of();
        var tepTheoBaoCao = cacBaoCao.isEmpty() ? Map.<Integer, List<BaoCaoTep>>of()
                : teps.findByBaoCaoIdInOrderByIdAsc(cacBaoCao.stream().map(BaoCao::getId).toList()).stream()
                        .collect(java.util.stream.Collectors.groupingBy(t -> t.getBaoCao().getId()));
        List<BaoCaoDto> ds = dayDu
                ? cacBaoCao.stream()
                        .map(b -> new BaoCaoDto(b.getId(), b.getPhienBan(), b.getGhiChu(),
                                ThoiGian.sangGioVietNam(b.getNopLuc()),
                                tepTheoBaoCao.getOrDefault(b.getId(), List.of()).stream()
                                        .map(t -> new BaoCaoDto.TepDto(t.getId(), t.getLoaiTaiLieu().getNhan(),
                                                t.getTenTepGoc(), t.getKichThuocByte()))
                                        .toList()))
                        .toList()
                : List.of();
        return new HoSoBaoCaoDto(dk.getId(), dk.getNhom().getMaNhom(), dk.getDeTai().getTenDeTai(),
                ThoiGian.sangGioVietNam(dk.getNhom().getDot().getHanNopBaoCao()), nop, ds);
    }
}
