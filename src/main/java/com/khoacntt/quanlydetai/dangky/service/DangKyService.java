package com.khoacntt.quanlydetai.dangky.service;

import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dangky.dto.*;
import com.khoacntt.quanlydetai.dangky.form.*;
import com.khoacntt.quanlydetai.dangky.model.*;
import com.khoacntt.quanlydetai.dangky.repository.DangKyRepository;
import com.khoacntt.quanlydetai.detai.model.*;
import com.khoacntt.quanlydetai.detai.repository.*;
import com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy;
import com.khoacntt.quanlydetai.nhom.model.*;
import com.khoacntt.quanlydetai.nhom.repository.*;
import com.khoacntt.quanlydetai.nhom.service.QuyTacNhom;
import com.khoacntt.quanlydetai.account.model.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
public class DangKyService {
    private static final List<TrangThaiDangKy> GIU_CHO = List.of(TrangThaiDangKy.CHO_DUYET, TrangThaiDangKy.CHAP_THUAN);
    private final DangKyRepository repository;
    private final NhomRepository nhomRepository;
    private final DeTaiRepository deTaiRepository;
    private final HuongDanRepository huongDanRepository;
    private final LoiMoiNhomRepository loiMoiRepository;
    private final QuyTacNhom quyTac;
    private final NguoiDungHienTai nguoiDung;
    private final Clock clock;
    public DangKyService(DangKyRepository repository, NhomRepository nhomRepository, DeTaiRepository deTaiRepository,
            HuongDanRepository huongDanRepository, LoiMoiNhomRepository loiMoiRepository,
            QuyTacNhom quyTac, NguoiDungHienTai nguoiDung, Clock clock) {
        this.repository = repository; this.nhomRepository = nhomRepository; this.deTaiRepository = deTaiRepository;
        this.huongDanRepository = huongDanRepository; this.loiMoiRepository = loiMoiRepository;
        this.quyTac = quyTac; this.nguoiDung = nguoiDung; this.clock = clock;
    }
    public List<LuaChonDeTaiDto> luaChon(Integer nhomId) {
        Nhom n = nhomRepository.findById(nhomId).orElseThrow(() -> new KhongTimThayException("Không tìm thấy nhóm"));
        quyTac.truongNhom(n);
        quyTac.kiemTraDuocSua(n);
        return deTaiRepository.findByTrangThaiOrderByIdDesc(TrangThaiDeTai.DA_CONG_BO).stream()
                .filter(d -> d.getDot().getId().equals(n.getDot().getId()))
                .map(d -> new LuaChonDeTaiDto(d.getId(), d.getMaDeTai(), d.getTenDeTai(),
                        repository.existsByDeTaiIdAndTrangThaiInAndNhomIdNot(d.getId(), GIU_CHO, nhomId))).toList();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void dangKy(Integer nhomId, @Valid DangKyForm f) {
        Nhom n = quyTac.khoaNhom(nhomId, f.getDeTaiId());
        var truong = quyTac.truongNhom(n).getSinhVien();
        quyTac.kiemTraDuocSua(n);
        quyTac.kiemTraThanhVienHopLe(n);
        DeTai d = deTaiRepository.findById(f.getDeTaiId()).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đề tài"));
        kiemTraDeTai(n, d);
        if (repository.existsByDeTaiIdAndTrangThaiInAndNhomIdNot(d.getId(), GIU_CHO, nhomId))
            throw new NghiepVuException("Đề tài đã có nhóm giữ chỗ. Đăng ký cũ của bạn được giữ nguyên");
        DangKy dk = repository.findByNhomId(nhomId).orElseGet(() -> new DangKy(n));
        if (dk.getTrangThai() == TrangThaiDangKy.CHO_DUYET && dk.getDeTai().getId().equals(d.getId()))
            throw new NghiepVuException("Nhóm đang chờ duyệt chính đề tài này");
        // Chỉ thay đề tài sau khi kiểm tra chỗ; UNIQUE + rollback bảo vệ đăng ký cũ.
        dk.dangKy(d, truong, ThoiGian.hienTai(clock));
        n.chuyenTrangThai(TrangThaiNhom.DA_CHOT);
        luu(dk);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void huy(Integer nhomId) {
        Nhom n = quyTac.khoaNhom(nhomId, null);
        quyTac.truongNhom(n);
        quyTac.kiemTraDuocSua(n);
        DangKy dk = repository.findByNhomId(nhomId).orElseThrow(() -> new NghiepVuException("Nhóm chưa đăng ký"));
        if (dk.getTrangThai() != TrangThaiDangKy.CHO_DUYET) throw new NghiepVuException("Chỉ hủy đăng ký đang chờ duyệt");
        dk.huy("Nhóm trưởng hủy đăng ký");
        n.chuyenTrangThai(TrangThaiNhom.DANG_LAP);
        luu(dk);
    }
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public List<DangKyDto> quanLy() {
        return repository.findAllByOrderByDangKyLucDesc().stream().map(quyTac::dangKyDto).toList();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public void duyet(Integer id, @Valid DuyetDangKyForm f) {
        Integer nhomId = repository.timNhomId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đăng ký"));
        Nhom n = quyTac.khoaNhom(nhomId, null);
        DangKy dk = repository.findByNhomId(nhomId).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đăng ký"));
        if (n.getDot().getTrangThai() != TrangThaiDotDangKy.TRIEN_KHAI)
            throw new NghiepVuException("Đợt không còn triển khai");
        if (dk.getTrangThai() != TrangThaiDangKy.CHO_DUYET)
            throw new NghiepVuException("Đăng ký không còn chờ duyệt");
        if (!dk.getDeTai().getId().equals(f.getDeTaiId()))
            throw new NghiepVuException("Nhóm vừa đổi đề tài; hãy tải lại và xem đăng ký mới trước khi duyệt");
        if (!f.getChapThuan() && (f.getLyDo() == null || f.getLyDo().isBlank()))
            throw new NghiepVuException("Cần nêu lý do từ chối đăng ký");
        if (f.getChapThuan()) {
            quyTac.kiemTraThanhVienHopLe(n);
            kiemTraDeTai(n, dk.getDeTai());
        }
        dk.duyet(f.getChapThuan(), nguoiDung.batBuoc(), f.getLyDo() == null ? null : f.getLyDo().trim(), ThoiGian.hienTai(clock));
        n.chuyenTrangThai(f.getChapThuan() ? TrangThaiNhom.DANG_THUC_HIEN : TrangThaiNhom.DANG_LAP);
        if (f.getChapThuan()) {
            loiMoiRepository.findByNhomIdAndTrangThai(nhomId, TrangThaiLoiMoiNhom.CHO_PHAN_HOI)
                    .forEach(l -> l.ketThuc(TrangThaiLoiMoiNhom.KHONG_CON_HIEU_LUC, "Đăng ký nhóm đã được chấp thuận", ThoiGian.hienTai(clock)));
        }
        luu(dk);
    }
    private void kiemTraDeTai(Nhom n, DeTai d) {
        if (!n.getDot().getId().equals(d.getDot().getId())) throw new NghiepVuException("Nhóm và đề tài phải cùng đợt");
        if (d.getTrangThai() != TrangThaiDeTai.DA_CONG_BO) throw new NghiepVuException("Chỉ đăng ký đề tài đã công bố");
        var ds = huongDanRepository.findByDeTaiIdOrderByGiangVienMaGvAsc(d.getId());
        if (ds.isEmpty() || ds.size() > 2) throw new NghiepVuException("Đề tài cần được khoa phân công đủ 1–2 GVHD");
        for (var h : ds) {
            var tk = h.getGiangVien().getTaiKhoan();
            if (tk.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG || !tk.coVaiTro(VaiTro.GIANG_VIEN))
                throw new NghiepVuException("Có GVHD không còn hoạt động; liên hệ khoa để phân công lại");
        }
    }
    private void luu(DangKy d) {
        try { repository.saveAndFlush(d); }
        catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Đề tài vừa được nhóm khác giữ chỗ hoặc đăng ký bị trùng. Hãy tải lại; đăng ký cũ được giữ nguyên");
        }
    }
}
