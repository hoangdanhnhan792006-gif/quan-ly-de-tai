package com.khoacntt.quanlydetai.dotdangky.service;

import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dotdangky.dto.DotDangKyDto;
import com.khoacntt.quanlydetai.dotdangky.form.DotDangKyForm;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.dotdangky.repository.DotDangKyRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.time.Clock;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
public class DotDangKyService {
    private final DotDangKyRepository repository;
    private final NguoiDungHienTai nguoiDung;
    private final Clock clock;

    public DotDangKyService(DotDangKyRepository repository, NguoiDungHienTai nguoiDung, Clock clock) {
        this.repository = repository;
        this.nguoiDung = nguoiDung;
        this.clock = clock;
    }

    public List<DotDangKyDto> danhSach() {
        return repository.findAll(Sort.by("id").descending()).stream().map(DotDangKyDto::tu).toList();
    }

    public DotDangKyDto chiTiet(Integer id) {
        return DotDangKyDto.tu(repository.findById(id)
                .orElseThrow(() -> new KhongTimThayException("Không tìm thấy đợt đăng ký")));
    }

    @PreAuthorize("hasRole('GIANG_VIEN')")
    public List<DotDangKyDto> dangMoDeXuat() {
        var now = ThoiGian.hienTai(clock);
        return repository.dangMoDeXuat(now).stream().map(DotDangKyDto::tu).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public Integer tao(@Valid DotDangKyForm form) {
        kiemTraThoiGian(form);
        String ma = form.getMaDot().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByMaDot(ma)) throw new NghiepVuException("Mã đợt đã tồn tại");
        DotDangKy dot = new DotDangKy(nguoiDung.batBuoc());
        capNhat(dot, form, ma);
        luu(dot);
        return dot.getId();
    }

    @Transactional
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public void sua(Integer id, @Valid DotDangKyForm form) {
        DotDangKy dot = timVaKhoa(id);
        if (dot.getTrangThai() != TrangThaiDotDangKy.NHAP) {
            throw new NghiepVuException("Chỉ sửa thông tin đợt khi còn nháp");
        }
        kiemTraThoiGian(form);
        String ma = form.getMaDot().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByMaDotAndIdNot(ma, id)) throw new NghiepVuException("Mã đợt đã tồn tại");
        capNhat(dot, form, ma);
        luu(dot);
    }

    @Transactional
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public void trienKhai(Integer id) {
        DotDangKy dot = timVaKhoa(id);
        if (dot.getTrangThai() != TrangThaiDotDangKy.NHAP) throw new NghiepVuException("Chỉ triển khai đợt đang nháp");
        if (dot.getGvKetThuc().isAfter(dot.getSvBatDau())) {
            throw new NghiepVuException("Hai giai đoạn không được chồng thời gian; sửa lịch đợt nháp trước khi triển khai");
        }
        var now = ThoiGian.hienTai(clock);
        if (!now.isBefore(dot.getGvKetThuc()) || !now.isBefore(dot.getSvKetThuc())) {
            throw new NghiepVuException("Thời gian đăng ký đã kết thúc. Hãy sửa lịch trước khi triển khai.");
        }
        dot.chuyenTrangThai(TrangThaiDotDangKy.TRIEN_KHAI);
        luu(dot);
    }

    @Transactional
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public void ketThuc(Integer id) {
        DotDangKy dot = timVaKhoa(id);
        if (dot.getTrangThai() != TrangThaiDotDangKy.TRIEN_KHAI) throw new NghiepVuException("Đợt chưa triển khai hoặc đã đóng");
        dot.chuyenTrangThai(TrangThaiDotDangKy.KET_THUC);
        luu(dot);
    }

    @Transactional
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public void huy(Integer id) {
        DotDangKy dot = timVaKhoa(id);
        if (dot.getTrangThai() != TrangThaiDotDangKy.NHAP && dot.getTrangThai() != TrangThaiDotDangKy.TRIEN_KHAI) {
            throw new NghiepVuException("Đợt đã đóng, không thể hủy");
        }
        if (repository.demDangKyGiuCho(id) > 0) {
            throw new NghiepVuException("Không hủy đợt đang có nhóm chờ duyệt hoặc được chấp thuận");
        }
        dot.chuyenTrangThai(TrangThaiDotDangKy.HUY);
        luu(dot);
    }

    @Transactional
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public void xoa(Integer id) {
        var d = timVaKhoa(id);
        if (d.getTrangThai() != TrangThaiDotDangKy.NHAP)
            throw new NghiepVuException("Chỉ xóa đợt nháp chưa được sử dụng");
        try { repository.delete(d); repository.flush(); }
        catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Đợt đã có dữ liệu liên quan; không được xóa");
        }
    }

    private DotDangKy timVaKhoa(Integer id) {
        return repository.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy đợt đăng ký"));
    }

    private void kiemTraThoiGian(DotDangKyForm f) {
        if (!f.getGvBatDau().isBefore(f.getGvKetThuc()) || !f.getSvBatDau().isBefore(f.getSvKetThuc())) {
            throw new NghiepVuException("Thời gian bắt đầu phải trước thời gian kết thúc của từng khoảng đăng ký");
        }
        if (f.getGvKetThuc().isAfter(f.getSvBatDau())) {
            throw new NghiepVuException("Thời gian SV đăng ký phải bắt đầu từ khi kết thúc giai đoạn GV đề xuất");
        }
        if (f.getHanNopBaoCao().isBefore(f.getSvKetThuc())) {
            throw new NghiepVuException("Hạn nộp báo cáo không được trước khi đóng đăng ký sinh viên");
        }
        boolean canHanDiem = f.getLoai() == LoaiDeTai.TLCN || f.getLoai() == LoaiDeTai.KLTN;
        if (canHanDiem != (f.getHanNopDiemPb() != null)) {
            throw new NghiepVuException("Hạn nộp điểm phản biện chỉ bắt buộc cho TLCN/KLTN; loại khác phải để trống");
        }
        if (canHanDiem && f.getHanNopDiemPb().isBefore(f.getHanNopBaoCao())) {
            throw new NghiepVuException("Hạn nộp điểm không được trước hạn nộp báo cáo");
        }
        if ((f.getLoai() == LoaiDeTai.KLTN) != (f.getNgayBaoCaoHd() != null)) {
            throw new NghiepVuException("Ngày báo cáo hội đồng cấp đợt chỉ bắt buộc cho KLTN; loại khác phải để trống");
        }
        if (f.getNgayBaoCaoHd() != null && f.getNgayBaoCaoHd().isBefore(f.getHanNopDiemPb().toLocalDate())) {
            throw new NghiepVuException("Ngày hội đồng không được trước ngày hạn nộp điểm phản biện");
        }
    }

    private void capNhat(DotDangKy dot, DotDangKyForm f, String ma) {
        dot.capNhat(ma, f.getTenDot().trim(), f.getLoai(),
                ThoiGian.sangUtc(f.getGvBatDau()), ThoiGian.sangUtc(f.getGvKetThuc()),
                ThoiGian.sangUtc(f.getSvBatDau()), ThoiGian.sangUtc(f.getSvKetThuc()),
                ThoiGian.sangUtc(f.getHanNopBaoCao()), ThoiGian.sangUtc(f.getHanNopDiemPb()), f.getNgayBaoCaoHd());
    }

    private void luu(DotDangKy dot) {
        try {
            repository.saveAndFlush(dot);
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Mã đợt bị trùng hoặc dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
        }
    }
}
