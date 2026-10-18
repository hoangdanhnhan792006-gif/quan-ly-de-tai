package com.khoacntt.quanlydetai.nhom.service;

import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dangky.dto.DangKyDto;
import com.khoacntt.quanlydetai.dangky.model.*;
import com.khoacntt.quanlydetai.dangky.repository.DangKyRepository;
import com.khoacntt.quanlydetai.detai.repository.DeTaiRepository;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.dotdangky.repository.DotDangKyRepository;
import com.khoacntt.quanlydetai.hoso.model.SinhVien;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;
import com.khoacntt.quanlydetai.nhom.model.*;
import com.khoacntt.quanlydetai.nhom.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.util.*;

/** Quy tắc dùng chung, được gọi bên trong transaction của NhomService/DangKyService. */
@Component
public class QuyTacNhom {
    private final NhomRepository nhomRepository;
    private final ThanhVienNhomRepository thanhVienRepository;
    private final DangKyRepository dangKyRepository;
    private final DotDangKyRepository dotRepository;
    private final DeTaiRepository deTaiRepository;
    private final SinhVienRepository sinhVienRepository;
    private final NguoiDungHienTai nguoiDung;
    private final Clock clock;
    public QuyTacNhom(NhomRepository nhomRepository, ThanhVienNhomRepository thanhVienRepository,
            DangKyRepository dangKyRepository, DotDangKyRepository dotRepository, DeTaiRepository deTaiRepository,
            SinhVienRepository sinhVienRepository, NguoiDungHienTai nguoiDung, Clock clock) {
        this.nhomRepository = nhomRepository; this.thanhVienRepository = thanhVienRepository;
        this.dangKyRepository = dangKyRepository; this.dotRepository = dotRepository;
        this.deTaiRepository = deTaiRepository; this.sinhVienRepository = sinhVienRepository;
        this.nguoiDung = nguoiDung; this.clock = clock;
    }
    public SinhVien sinhVienHienTai() {
        SinhVien sv = sinhVienRepository.findByTaiKhoanId(nguoiDung.batBuoc().getId())
                .orElseThrow(() -> new NghiepVuException("Bạn cần có hồ sơ sinh viên trước khi sử dụng chức năng nhóm"));
        kiemTraSinhVien(sv);
        return sv;
    }
    public void kiemTraSinhVien(SinhVien sv) {
        if (sv.getTaiKhoan().getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG
                || !sv.getTaiKhoan().coVaiTro(VaiTro.SINH_VIEN)) {
            throw new NghiepVuException("Sinh viên phải có tài khoản hoạt động và vai trò SINH_VIEN");
        }
    }
    public DotDangKy khoaDot(Integer id) {
        return dotRepository.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đợt"));
    }
    public Nhom khoaNhom(Integer id, Integer deTaiMoiId) {
        Integer dotId = nhomRepository.timDotId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy nhóm"));
        khoaDot(dotId);
        // Khóa đợt trước; đọc lại đăng ký, khóa đề tài cũ/mới theo ID rồi mới khóa nhóm.
        Set<Integer> deTaiIds = new TreeSet<>();
        dangKyRepository.findByNhomId(id).ifPresent(d -> deTaiIds.add(d.getDeTai().getId()));
        if (deTaiMoiId != null) {
            Integer dotDeTai = deTaiRepository.timDotId(deTaiMoiId)
                    .orElseThrow(() -> new KhongTimThayException("Không tìm thấy đề tài"));
            if (!dotId.equals(dotDeTai)) throw new NghiepVuException("Nhóm và đề tài phải cùng đợt");
            deTaiIds.add(deTaiMoiId);
        }
        for (Integer deTaiId : deTaiIds) {
            deTaiRepository.timVaKhoa(deTaiId).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đề tài"));
        }
        return nhomRepository.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy nhóm"));
    }
    public boolean trongHan(DotDangKy dot) {
        var now = ThoiGian.hienTai(clock);
        return dot.getTrangThai() == TrangThaiDotDangKy.TRIEN_KHAI
                && !now.isBefore(dot.getGvKetThuc()) && !now.isBefore(dot.getSvBatDau()) && now.isBefore(dot.getSvKetThuc());
    }
    public void kiemTraTrongHan(DotDangKy dot) {
        if (!trongHan(dot)) throw new NghiepVuException("Không trong thời gian sinh viên đăng ký của đợt");
    }
    public boolean coTheSua(Nhom nhom) {
        return trongHan(nhom.getDot())
                && (nhom.getTrangThai() == TrangThaiNhom.DANG_LAP || nhom.getTrangThai() == TrangThaiNhom.DA_CHOT)
                && dangKyRepository.findByNhomId(nhom.getId())
                    .map(d -> d.getTrangThai() != TrangThaiDangKy.CHAP_THUAN).orElse(true);
    }
    public void kiemTraDuocSua(Nhom nhom) {
        if (!coTheSua(nhom)) throw new NghiepVuException("Nhóm đã được chấp thuận, đã đóng hoặc ngoài thời gian đăng ký; không được thay đổi");
    }
    public ThanhVienNhom thanhVien(Integer nhomId, Integer svId) {
        return thanhVienRepository.findByNhomIdAndSinhVienId(nhomId, svId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc nhóm này"));
    }
    public ThanhVienNhom truongNhom(Nhom nhom) {
        ThanhVienNhom tv = thanhVien(nhom.getId(), sinhVienHienTai().getId());
        if (!tv.isTruongNhom()) throw new AccessDeniedException("Chỉ nhóm trưởng được thực hiện thao tác này");
        return tv;
    }
    public void kiemTraThanhVienHopLe(Nhom nhom) {
        var ds = thanhVienRepository.findByNhomIdOrderBySinhVienMaSvAsc(nhom.getId());
        if (ds.size() < 3 || ds.size() > 5 || ds.stream().filter(ThanhVienNhom::isTruongNhom).count() != 1)
            throw new NghiepVuException("Nhóm cần 3–5 thành viên và đúng một nhóm trưởng");
        ds.forEach(t -> kiemTraSinhVien(t.getSinhVien()));
    }
    public DangKyDto dangKyDto(DangKy d) {
        return new DangKyDto(d.getId(), d.getNhom().getId(), d.getNhom().getMaNhom(), d.getNhom().getDot().getTenDot(),
                d.getDeTai().getId(), d.getDeTai().getTenDeTai(), d.getTrangThai(), d.getLyDo(),
                ThoiGian.sangGioVietNam(d.getDangKyLuc()),
                d.getTrangThai() == TrangThaiDangKy.CHO_DUYET
                    && d.getNhom().getDot().getTrangThai() == TrangThaiDotDangKy.TRIEN_KHAI);
    }
}
