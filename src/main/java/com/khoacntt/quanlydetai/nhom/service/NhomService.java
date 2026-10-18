package com.khoacntt.quanlydetai.nhom.service;

import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dangky.repository.DangKyRepository;
import com.khoacntt.quanlydetai.dotdangky.dto.DotDangKyDto;
import com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy;
import com.khoacntt.quanlydetai.dotdangky.repository.DotDangKyRepository;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;
import com.khoacntt.quanlydetai.nhom.dto.*;
import com.khoacntt.quanlydetai.nhom.form.*;
import com.khoacntt.quanlydetai.nhom.model.*;
import com.khoacntt.quanlydetai.nhom.repository.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.time.Clock;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasRole('SINH_VIEN')")
public class NhomService {
    private final NhomRepository nhomRepository;
    private final ThanhVienNhomRepository thanhVienRepository;
    private final LoiMoiNhomRepository loiMoiRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DotDangKyRepository dotRepository;
    private final DangKyRepository dangKyRepository;
    private final QuyTacNhom quyTac;
    private final NguoiDungHienTai nguoiDung;
    private final Clock clock;
    public NhomService(NhomRepository nhomRepository, ThanhVienNhomRepository thanhVienRepository,
            LoiMoiNhomRepository loiMoiRepository, SinhVienRepository sinhVienRepository,
            DotDangKyRepository dotRepository, DangKyRepository dangKyRepository,
            QuyTacNhom quyTac, NguoiDungHienTai nguoiDung, Clock clock) {
        this.nhomRepository = nhomRepository; this.thanhVienRepository = thanhVienRepository;
        this.loiMoiRepository = loiMoiRepository; this.sinhVienRepository = sinhVienRepository;
        this.dotRepository = dotRepository; this.dangKyRepository = dangKyRepository;
        this.quyTac = quyTac; this.nguoiDung = nguoiDung; this.clock = clock;
    }
    public List<NhomDto> cuaToi() {
        Integer svId = quyTac.sinhVienHienTai().getId();
        return nhomRepository.cuaSinhVien(svId).stream().map(n -> dto(n, svId)).toList();
    }
    public List<DotDangKyDto> cacDotDangMo() {
        return dotRepository.findByTrangThaiOrderByIdDesc(TrangThaiDotDangKy.TRIEN_KHAI).stream()
                .filter(quyTac::trongHan).map(DotDangKyDto::tu).toList();
    }
    public List<LoiMoiDto> loiMoiCuaToi() {
        return loiMoiRepository.findBySinhVienDuocMoiIdOrderByIdDesc(quyTac.sinhVienHienTai().getId())
                .stream().map(this::loiMoiDto).toList();
    }
    @PreAuthorize("hasAnyRole('SINH_VIEN','TRUONG_KHOA','CAN_BO_KHOA')")
    public NhomDto chiTiet(Integer id) {
        Nhom nhom = nhomRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy nhóm"));
        TaiKhoan actor = nguoiDung.batBuoc();
        boolean khoa = actor.coVaiTro(VaiTro.TRUONG_KHOA) || actor.coVaiTro(VaiTro.CAN_BO_KHOA);
        Integer svId = sinhVienRepository.findByTaiKhoanId(actor.getId()).map(s -> s.getId()).orElse(null);
        if (!khoa) quyTac.thanhVien(id, svId);
        return dto(nhom, svId);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Integer tao(@Valid NhomForm f) {
        var dot = quyTac.khoaDot(f.getDotId());
        quyTac.kiemTraTrongHan(dot);
        var sv = quyTac.sinhVienHienTai();
        if (thanhVienRepository.existsBySinhVienIdAndDotId(sv.getId(), dot.getId()))
            throw new NghiepVuException("Bạn đã có nhóm trong đợt này");
        String ma = f.getMaNhom().trim().toUpperCase(Locale.ROOT);
        if (nhomRepository.existsByDotIdAndMaNhom(dot.getId(), ma))
            throw new NghiepVuException("Mã nhóm đã được dùng trong đợt");
        try {
            Nhom nhom = nhomRepository.saveAndFlush(new Nhom(dot, sv, ma, gon(f.getTenNhom())));
            thanhVienRepository.saveAndFlush(new ThanhVienNhom(nhom, sv, true));
            huyLoiMoiCuaSinhVien(sv.getId(), dot.getId());
            return nhom.getId();
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Mã nhóm trùng hoặc bạn vừa tham gia một nhóm khác; hãy tải lại trang");
        }
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void doiTen(Integer id, @Valid DoiTenNhomForm f) {
        Nhom n = khoaVaKiemTraTruong(id);
        n.doiTen(gon(f.getTenNhom()));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void moi(Integer id, @Valid MoiThanhVienForm f) {
        Nhom n = khoaVaKiemTraTruong(id);
        var truong = quyTac.truongNhom(n).getSinhVien();
        if (thanhVienRepository.countByNhomId(id) >= 5) throw new NghiepVuException("Nhóm đã đủ 5 thành viên");
        var sv = sinhVienRepository.findByMaSv(f.getMaSv().trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new NghiepVuException("Không tìm thấy mã sinh viên; hãy kiểm tra lại"));
        quyTac.kiemTraSinhVien(sv);
        if (thanhVienRepository.existsBySinhVienIdAndDotId(sv.getId(), n.getDot().getId()))
            throw new NghiepVuException("Sinh viên đã có nhóm trong đợt này");
        if (loiMoiRepository.existsByNhomIdAndSinhVienDuocMoiIdAndTrangThai(id, sv.getId(), TrangThaiLoiMoiNhom.CHO_PHAN_HOI))
            throw new NghiepVuException("Đã có lời mời đang chờ sinh viên này phản hồi");
        try {
            loiMoiRepository.saveAndFlush(new LoiMoiNhom(n, sv, truong, gon(f.getLoiNhan()), ThoiGian.hienTai(clock)));
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Lời mời đang chờ đã tồn tại; hãy tải lại trang");
        }
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void phanHoi(Integer loiMoiId, boolean chapNhan) {
        LoiMoiNhom l = khoaLoiMoi(loiMoiId);
        var sv = quyTac.sinhVienHienTai();
        if (!l.getSinhVienDuocMoi().getId().equals(sv.getId()))
            throw new AccessDeniedException("Chỉ người nhận được phản hồi lời mời");
        kiemTraDangCho(l);
        if (!chapNhan) {
            l.ketThuc(TrangThaiLoiMoiNhom.TU_CHOI, "Sinh viên từ chối", ThoiGian.hienTai(clock));
            return;
        }
        Nhom n = l.getNhom();
        quyTac.kiemTraDuocSua(n);
        if (thanhVienRepository.existsBySinhVienIdAndDotId(sv.getId(), n.getDot().getId()))
            throw new NghiepVuException("Bạn đã có nhóm trong đợt này");
        if (thanhVienRepository.countByNhomId(n.getId()) >= 5) throw new NghiepVuException("Nhóm đã đủ 5 thành viên");
        try {
            thanhVienRepository.saveAndFlush(new ThanhVienNhom(n, sv, false));
            l.ketThuc(TrangThaiLoiMoiNhom.DA_CHAP_NHAN, "Sinh viên đồng ý tham gia", ThoiGian.hienTai(clock));
            huyLoiMoiCuaSinhVien(sv.getId(), n.getDot().getId());
            if (thanhVienRepository.countByNhomId(n.getId()) == 5) huyLoiMoiNhom(n.getId(), "Nhóm đã đủ 5 thành viên");
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Bạn vừa tham gia nhóm khác; hãy tải lại trang");
        }
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void thuHoi(Integer loiMoiId) {
        LoiMoiNhom l = khoaLoiMoi(loiMoiId);
        quyTac.truongNhom(l.getNhom());
        quyTac.kiemTraDuocSua(l.getNhom());
        kiemTraDangCho(l);
        l.ketThuc(TrangThaiLoiMoiNhom.DA_THU_HOI, "Nhóm trưởng thu hồi", ThoiGian.hienTai(clock));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void doiTruong(Integer id, Integer svId) {
        Nhom n = khoaVaKiemTraTruong(id);
        ThanhVienNhom cu = quyTac.truongNhom(n);
        ThanhVienNhom moi = thanhVienRepository.findByNhomIdAndSinhVienId(id, svId)
                .orElseThrow(() -> new NghiepVuException("Nhóm trưởng mới phải là thành viên của nhóm"));
        if (moi.isTruongNhom()) throw new NghiepVuException("Sinh viên đã là nhóm trưởng");
        quyTac.kiemTraSinhVien(moi.getSinhVien());
        // Flush bỏ cờ cũ trước khi đặt cờ mới để không vi phạm UNIQUE trưởng nhóm.
        cu.datTruongNhom(false);
        thanhVienRepository.flush();
        moi.datTruongNhom(true);
        thanhVienRepository.flush();
        huyLoiMoiNhom(id, "Nhóm đã đổi trưởng nhóm; trưởng nhóm mới có thể gửi lời mời lại");
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void xoaThanhVien(Integer id, Integer svId) {
        Nhom n = khoaVaKiemTraTruong(id);
        ThanhVienNhom tv = thanhVienRepository.findByNhomIdAndSinhVienId(id, svId)
                .orElseThrow(() -> new NghiepVuException("Sinh viên không thuộc nhóm"));
        if (tv.isTruongNhom()) throw new NghiepVuException("Hãy chuyển trưởng nhóm trước khi rời nhóm");
        thanhVienRepository.delete(tv);
        thanhVienRepository.flush();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void roiNhom(Integer id) {
        Nhom n = quyTac.khoaNhom(id, null);
        ThanhVienNhom tv = quyTac.thanhVien(id, quyTac.sinhVienHienTai().getId());
        quyTac.kiemTraDuocSua(n);
        if (tv.isTruongNhom()) throw new NghiepVuException("Hãy chuyển trưởng nhóm hoặc giải thể nhóm trước");
        thanhVienRepository.delete(tv);
        thanhVienRepository.flush();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void giaiThe(Integer id) {
        Nhom n = khoaVaKiemTraTruong(id);
        dangKyRepository.findByNhomId(id).ifPresent(d -> d.huy("Nhóm đã giải thể"));
        huyLoiMoiNhom(id, "Nhóm đã giải thể");
        thanhVienRepository.deleteAll(thanhVienRepository.findByNhomIdOrderBySinhVienMaSvAsc(id));
        thanhVienRepository.flush();
        n.chuyenTrangThai(TrangThaiNhom.GIAI_THE);
    }
    private Nhom khoaVaKiemTraTruong(Integer id) {
        Nhom n = quyTac.khoaNhom(id, null);
        quyTac.truongNhom(n);
        quyTac.kiemTraDuocSua(n);
        return n;
    }
    private LoiMoiNhom khoaLoiMoi(Integer id) {
        Integer nhomId = loiMoiRepository.timNhomId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy lời mời"));
        quyTac.khoaNhom(nhomId, null);
        return loiMoiRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy lời mời"));
    }
    private void kiemTraDangCho(LoiMoiNhom l) {
        if (l.getTrangThai() != TrangThaiLoiMoiNhom.CHO_PHAN_HOI)
            throw new NghiepVuException("Lời mời đã được xử lý hoặc không còn hiệu lực");
    }
    private void huyLoiMoiCuaSinhVien(Integer svId, Integer dotId) {
        loiMoiRepository.findBySinhVienDuocMoiIdAndNhomDotIdAndTrangThai(svId, dotId, TrangThaiLoiMoiNhom.CHO_PHAN_HOI)
                .forEach(l -> l.ketThuc(TrangThaiLoiMoiNhom.KHONG_CON_HIEU_LUC, "Sinh viên đã có nhóm trong đợt", ThoiGian.hienTai(clock)));
    }
    private void huyLoiMoiNhom(Integer id, String lyDo) {
        loiMoiRepository.findByNhomIdAndTrangThai(id, TrangThaiLoiMoiNhom.CHO_PHAN_HOI)
                .forEach(l -> l.ketThuc(TrangThaiLoiMoiNhom.KHONG_CON_HIEU_LUC, lyDo, ThoiGian.hienTai(clock)));
    }
    private LoiMoiDto loiMoiDto(LoiMoiNhom l) {
        boolean cho = l.getTrangThai() == TrangThaiLoiMoiNhom.CHO_PHAN_HOI && quyTac.coTheSua(l.getNhom());
        return new LoiMoiDto(l.getId(), l.getNhom().getId(), l.getNhom().getMaNhom(), l.getNhom().getDot().getTenDot(),
                l.getNguoiMoi().getTaiKhoan().getHoTen(), l.getSinhVienDuocMoi().getMaSv(),
                l.getLoiNhan(), l.getTrangThai(), l.getLyDoKetThuc(), cho);
    }
    private NhomDto dto(Nhom n, Integer svId) {
        var tvs = thanhVienRepository.findByNhomIdOrderBySinhVienMaSvAsc(n.getId());
        boolean truong = tvs.stream().anyMatch(t -> t.isTruongNhom() && Objects.equals(t.getSinhVien().getId(), svId));
        var thanhViens = tvs.stream().map(t -> new ThanhVienDto(t.getSinhVien().getId(), t.getSinhVien().getMaSv(),
                t.getSinhVien().getTaiKhoan().getHoTen(), t.isTruongNhom())).toList();
        return new NhomDto(n.getId(), n.getDot().getId(), n.getDot().getTenDot(), n.getMaNhom(), n.getTenNhom(),
                n.getTrangThai(), thanhViens, truong ? loiMoiRepository.findByNhomIdOrderByIdDesc(n.getId()).stream()
                .map(this::loiMoiDto).toList() : List.of(),
                dangKyRepository.findByNhomId(n.getId()).map(quyTac::dangKyDto).orElse(null),
                truong, quyTac.coTheSua(n), svId);
    }
    private String gon(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
