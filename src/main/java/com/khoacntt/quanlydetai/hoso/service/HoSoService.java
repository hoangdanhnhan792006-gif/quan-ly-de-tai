package com.khoacntt.quanlydetai.hoso.service;

import com.khoacntt.quanlydetai.account.dto.TaiKhoanDto;
import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.repository.TaiKhoanRepository;
import com.khoacntt.quanlydetai.common.exception.KhongTimThayException;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.hoso.dto.GiangVienDto;
import com.khoacntt.quanlydetai.hoso.dto.HoSoCaNhanDto;
import com.khoacntt.quanlydetai.hoso.dto.SinhVienDto;
import com.khoacntt.quanlydetai.hoso.form.GiangVienForm;
import com.khoacntt.quanlydetai.hoso.form.SinhVienForm;
import com.khoacntt.quanlydetai.hoso.model.BoMon;
import com.khoacntt.quanlydetai.hoso.model.GiangVien;
import com.khoacntt.quanlydetai.hoso.model.SinhVien;
import com.khoacntt.quanlydetai.hoso.repository.BoMonRepository;
import com.khoacntt.quanlydetai.hoso.repository.GiangVienRepository;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
public class HoSoService {
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final BoMonRepository boMonRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final NguoiDungHienTai nguoiDungHienTai;

    public HoSoService(SinhVienRepository sinhVien, GiangVienRepository giangVien, BoMonRepository boMon,
                       TaiKhoanRepository taiKhoan, NguoiDungHienTai nguoiDungHienTai) {
        sinhVienRepository = sinhVien;
        giangVienRepository = giangVien;
        boMonRepository = boMon;
        taiKhoanRepository = taiKhoan;
        this.nguoiDungHienTai = nguoiDungHienTai;
    }

    @PreAuthorize("isAuthenticated()")
    public HoSoCaNhanDto caNhan() {
        TaiKhoan taiKhoan = nguoiDungHienTai.batBuoc();
        return new HoSoCaNhanDto(TaiKhoanDto.tu(taiKhoan),
                sinhVienRepository.findByTaiKhoanId(taiKhoan.getId()).map(SinhVienDto::tu).orElse(null),
                giangVienRepository.findByTaiKhoanId(taiKhoan.getId()).map(GiangVienDto::tu).orElse(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<SinhVienDto> sinhVien() {
        return sinhVienRepository.findAll(Sort.by("maSv")).stream().map(SinhVienDto::tu).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<GiangVienDto> giangVien() {
        return giangVienRepository.findAll(Sort.by("maGv")).stream().map(GiangVienDto::tu).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public SinhVienDto sinhVien(Integer id) {
        return SinhVienDto.tu(timSv(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public GiangVienDto giangVien(Integer id) {
        return GiangVienDto.tu(timGv(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void taoSinhVien(@Valid SinhVienForm form) {
        // Cùng khóa tài khoản với thao tác sửa vai trò để hồ sơ luôn khớp vai trò.
        TaiKhoan taiKhoan = taiKhoanTheoVaiTro(form.getTaiKhoanId(), VaiTro.SINH_VIEN);
        if (sinhVienRepository.existsByTaiKhoanId(taiKhoan.getId())) {
            throw new NghiepVuException("Tài khoản đã có hồ sơ sinh viên");
        }
        String ma = chuanHoaMa(form.getMaSv());
        if (sinhVienRepository.existsByMaSv(ma)) {
            throw new NghiepVuException("Mã sinh viên đã tồn tại");
        }
        luuSinhVien(new SinhVien(taiKhoan, ma, gon(form.getLop()), gon(form.getKhoaHoc())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void suaSinhVien(Integer id, @Valid SinhVienForm form) {
        taiKhoanTheoVaiTro(form.getTaiKhoanId(), VaiTro.SINH_VIEN);
        SinhVien sinhVien = timSv(id);
        if (!Objects.equals(sinhVien.getTaiKhoan().getId(), form.getTaiKhoanId())) {
            throw new NghiepVuException("Không được chuyển hồ sơ sang tài khoản khác");
        }
        String ma = chuanHoaMa(form.getMaSv());
        if (sinhVienRepository.existsByMaSvAndIdNot(ma, id)) {
            throw new NghiepVuException("Mã sinh viên đã tồn tại");
        }
        sinhVien.capNhat(ma, gon(form.getLop()), gon(form.getKhoaHoc()));
        luuSinhVien(sinhVien);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void taoGiangVien(@Valid GiangVienForm form) {
        TaiKhoan taiKhoan = taiKhoanTheoVaiTro(form.getTaiKhoanId(), VaiTro.GIANG_VIEN);
        if (giangVienRepository.existsByTaiKhoanId(taiKhoan.getId())) {
            throw new NghiepVuException("Tài khoản đã có hồ sơ giảng viên");
        }
        String ma = chuanHoaMa(form.getMaGv());
        if (giangVienRepository.existsByMaGv(ma)) {
            throw new NghiepVuException("Mã giảng viên đã tồn tại");
        }
        luuGiangVien(new GiangVien(taiKhoan, ma, boMonDeGan(form.getBoMonId(), null), gon(form.getHocVi())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void suaGiangVien(Integer id, @Valid GiangVienForm form) {
        taiKhoanTheoVaiTro(form.getTaiKhoanId(), VaiTro.GIANG_VIEN);
        GiangVien giangVien = timGv(id);
        if (!Objects.equals(giangVien.getTaiKhoan().getId(), form.getTaiKhoanId())) {
            throw new NghiepVuException("Không được chuyển hồ sơ sang tài khoản khác");
        }
        String ma = chuanHoaMa(form.getMaGv());
        if (giangVienRepository.existsByMaGvAndIdNot(ma, id)) {
            throw new NghiepVuException("Mã giảng viên đã tồn tại");
        }
        giangVien.capNhat(ma, boMonDeGan(form.getBoMonId(), giangVien.getBoMon().getId()), gon(form.getHocVi()));
        luuGiangVien(giangVien);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void xoaSinhVien(Integer id) {
        var h = timSv(id);
        taiKhoanRepository.timVaKhoa(h.getTaiKhoan().getId()).orElseThrow();
        try { sinhVienRepository.delete(h); sinhVienRepository.flush(); }
        catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Hồ sơ đã được sử dụng trong nghiệp vụ, không thể xóa; có thể khóa tài khoản");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void xoaGiangVien(Integer id) {
        var h = timGv(id);
        if (h.getTaiKhoan().coVaiTro(VaiTro.TRUONG_BO_MON))
            throw new NghiepVuException("Bỏ vai trò Trưởng bộ môn trước khi xóa hồ sơ");
        taiKhoanRepository.timVaKhoa(h.getTaiKhoan().getId()).orElseThrow();
        try { giangVienRepository.delete(h); giangVienRepository.flush(); }
        catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Hồ sơ đã được sử dụng trong nghiệp vụ, không thể xóa; có thể khóa tài khoản");
        }
    }

    private TaiKhoan taiKhoanTheoVaiTro(Integer id, VaiTro vaiTro) {
        TaiKhoan taiKhoan = taiKhoanRepository.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy tài khoản"));
        if (!taiKhoan.coVaiTro(vaiTro)) {
            throw new NghiepVuException("Tài khoản chưa có vai trò " + vaiTro);
        }
        return taiKhoan;
    }

    private BoMon boMonDeGan(Integer id, Integer boMonCu) {
        BoMon boMon = boMonRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy bộ môn"));
        if (boMon.getDangHoatDong() != 1 && !Objects.equals(id, boMonCu)) {
            throw new NghiepVuException("Không thể phân công mới vào bộ môn đã ngừng hoạt động");
        }
        return boMon;
    }

    private SinhVien timSv(Integer id) {
        return sinhVienRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy sinh viên"));
    }

    private GiangVien timGv(Integer id) {
        return giangVienRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy giảng viên"));
    }

    private String chuanHoaMa(String ma) {
        return ma.trim().toUpperCase(Locale.ROOT);
    }

    private String gon(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private void luuSinhVien(SinhVien sinhVien) {
        try {
            sinhVienRepository.saveAndFlush(sinhVien);
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Mã sinh viên hoặc tài khoản đã có hồ sơ. Vui lòng kiểm tra lại.");
        }
    }

    private void luuGiangVien(GiangVien giangVien) {
        try {
            giangVienRepository.saveAndFlush(giangVien);
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Mã giảng viên hoặc tài khoản đã có hồ sơ. Vui lòng kiểm tra lại.");
        }
    }
}
