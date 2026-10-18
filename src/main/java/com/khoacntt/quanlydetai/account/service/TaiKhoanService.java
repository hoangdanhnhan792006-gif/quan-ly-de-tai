package com.khoacntt.quanlydetai.account.service;

import com.khoacntt.quanlydetai.account.dto.TaiKhoanDto;
import com.khoacntt.quanlydetai.account.form.DoiMatKhauForm;
import com.khoacntt.quanlydetai.account.form.DatLaiMatKhauForm;
import com.khoacntt.quanlydetai.account.form.SuaTaiKhoanForm;
import com.khoacntt.quanlydetai.account.form.TaiKhoanForm;
import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.repository.TaiKhoanRepository;
import com.khoacntt.quanlydetai.common.exception.KhongTimThayException;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.hoso.repository.GiangVienRepository;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
public class TaiKhoanService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final NguoiDungHienTai nguoiDungHienTai;

    public TaiKhoanService(TaiKhoanRepository taiKhoanRepository, SinhVienRepository sinhVienRepository,
                          GiangVienRepository giangVienRepository, PasswordEncoder passwordEncoder,
                          NguoiDungHienTai nguoiDungHienTai) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.giangVienRepository = giangVienRepository;
        this.passwordEncoder = passwordEncoder;
        this.nguoiDungHienTai = nguoiDungHienTai;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TaiKhoanDto> danhSach() {
        return taiKhoanRepository.findAll(Sort.by("id").descending()).stream().map(TaiKhoanDto::tu).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TaiKhoanDto> theoVaiTro(VaiTro vaiTro) {
        return taiKhoanRepository.findAll(Sort.by("tenDangNhap")).stream()
                .filter(t -> t.coVaiTro(vaiTro)).map(TaiKhoanDto::tu).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public TaiKhoanDto chiTiet(Integer id) {
        return TaiKhoanDto.tu(tim(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Integer tao(@Valid TaiKhoanForm form) {
        String ten = TaiKhoan.chuanHoaTenDangNhap(form.getTenDangNhap());
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (taiKhoanRepository.existsByTenDangNhap(ten)) {
            throw new NghiepVuException("Tên đăng nhập đã được sử dụng");
        }
        if (taiKhoanRepository.existsByEmail(email)) {
            throw new NghiepVuException("Email đã được sử dụng");
        }
        kiemTraMatKhau(form.getMatKhau());
        kiemTraVaiTro(form.getVaiTros());
        TaiKhoan tk = TaiKhoan.taoMoi(ten, passwordEncoder.encode(form.getMatKhau()),
                form.getHoTen(), email, form.getVaiTros());
        luu(tk);
        return tk.getId();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void sua(Integer id, @Valid SuaTaiKhoanForm form) {
        TaiKhoan tk = timVaKhoa(id);
        kiemTraVaiTro(form.getVaiTros());
        if (Objects.equals(nguoiDungHienTai.batBuoc().getId(), id) && !form.getVaiTros().contains(VaiTro.ADMIN)) {
            throw new NghiepVuException("Bạn không được tự bỏ quyền quản trị của mình");
        }
        if (sinhVienRepository.existsByTaiKhoanId(id) && !form.getVaiTros().contains(VaiTro.SINH_VIEN)) {
            throw new NghiepVuException("Tài khoản đã có hồ sơ sinh viên, cần giữ vai trò SINH_VIEN");
        }
        if (giangVienRepository.existsByTaiKhoanId(id) && !form.getVaiTros().contains(VaiTro.GIANG_VIEN)) {
            throw new NghiepVuException("Tài khoản đã có hồ sơ giảng viên, cần giữ vai trò GIANG_VIEN");
        }
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (taiKhoanRepository.existsByEmailAndIdNot(email, id)) {
            throw new NghiepVuException("Email đã được sử dụng");
        }
        tk.capNhatThongTin(form.getHoTen(), email, form.getVaiTros());
        luu(tk);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void doiTrangThai(Integer id, TrangThaiTaiKhoan trangThai) {
        if (trangThai == null) {
            throw new NghiepVuException("Chọn trạng thái hợp lệ");
        }
        if (Objects.equals(nguoiDungHienTai.batBuoc().getId(), id) && trangThai == TrangThaiTaiKhoan.BI_KHOA) {
            throw new NghiepVuException("Bạn không được tự khóa tài khoản đang sử dụng");
        }
        TaiKhoan tk = timVaKhoa(id);
        tk.doiTrangThai(trangThai);
        luu(tk);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void xoa(Integer id) {
        if (Objects.equals(nguoiDungHienTai.batBuoc().getId(), id)) {
            throw new NghiepVuException("Bạn không được tự xóa tài khoản đang sử dụng");
        }
        TaiKhoan tk = timVaKhoa(id);
        if (sinhVienRepository.existsByTaiKhoanId(id) || giangVienRepository.existsByTaiKhoanId(id)) {
            throw new NghiepVuException("Tài khoản đã có hồ sơ SV/GV. Chỉ xóa hồ sơ chưa sử dụng trước, hoặc khóa tài khoản.");
        }
        try {
            // JPA xóa collection vai trò cùng tài khoản; FK RESTRICT giữ các dữ liệu nghiệp vụ khác.
            taiKhoanRepository.delete(tk);
            taiKhoanRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Tài khoản đã có dữ liệu liên quan, không thể xóa. Hãy khóa tài khoản.");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void datLaiMatKhau(Integer id, @Valid DatLaiMatKhauForm form) {
        if (Objects.equals(nguoiDungHienTai.batBuoc().getId(), id)) {
            throw new NghiepVuException("Để đổi mật khẩu của mình, hãy dùng mục Đổi mật khẩu và nhập mật khẩu hiện tại.");
        }
        TaiKhoan tk = timVaKhoa(id);
        kiemTraMatKhau(form.getMatKhauMoi());
        if (!Objects.equals(form.getMatKhauMoi(), form.getXacNhanMatKhau())) {
            throw new NghiepVuException("Xác nhận mật khẩu mới không khớp");
        }
        if (passwordEncoder.matches(form.getMatKhauMoi(), tk.getMatKhauBam())) {
            throw new NghiepVuException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        tk.doiMatKhau(passwordEncoder.encode(form.getMatKhauMoi()));
        luu(tk);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void doiMatKhau(@Valid DoiMatKhauForm form) {
        // Lấy ID từ phiên đăng nhập, không nhận ID tùy ý từ trình duyệt.
        TaiKhoan tk = timVaKhoa(nguoiDungHienTai.batBuoc().getId());
        if (!passwordEncoder.matches(form.getMatKhauCu(), tk.getMatKhauBam())) {
            throw new NghiepVuException("Mật khẩu hiện tại không đúng");
        }
        kiemTraMatKhau(form.getMatKhauMoi());
        if (!form.getMatKhauMoi().equals(form.getXacNhanMatKhau())) {
            throw new NghiepVuException("Xác nhận mật khẩu mới không khớp");
        }
        if (passwordEncoder.matches(form.getMatKhauMoi(), tk.getMatKhauBam())) {
            throw new NghiepVuException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        tk.doiMatKhau(passwordEncoder.encode(form.getMatKhauMoi()));
        luu(tk);
    }

    private TaiKhoan tim(Integer id) {
        return taiKhoanRepository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy tài khoản"));
    }

    private TaiKhoan timVaKhoa(Integer id) {
        return taiKhoanRepository.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy tài khoản"));
    }

    private void kiemTraVaiTro(Set<VaiTro> roles) {
        if (roles != null && roles.contains(VaiTro.TRUONG_BO_MON) && !roles.contains(VaiTro.GIANG_VIEN)) {
            throw new NghiepVuException("Trưởng bộ môn phải đồng thời có vai trò GIANG_VIEN và hồ sơ thuộc bộ môn phụ trách");
        }
        if (roles == null || roles.isEmpty() || roles.stream().anyMatch(Objects::isNull)) {
            throw new NghiepVuException("Chọn ít nhất một vai trò hợp lệ");
        }
    }

    private void kiemTraMatKhau(String password) {
        if (password == null || password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new NghiepVuException("Mật khẩu tối thiểu 8 ký tự và tối đa 72 byte UTF-8");
        }
    }

    private void luu(TaiKhoan tk) {
        try {
            taiKhoanRepository.saveAndFlush(tk);
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Tên đăng nhập hoặc email đã được sử dụng. Vui lòng kiểm tra lại.");
        }
    }
}
