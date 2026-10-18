package com.khoacntt.quanlydetai.account.controller;

import com.khoacntt.quanlydetai.account.form.SuaTaiKhoanForm;
import com.khoacntt.quanlydetai.account.form.DatLaiMatKhauForm;
import com.khoacntt.quanlydetai.account.form.TaiKhoanForm;
import com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.service.TaiKhoanService;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashSet;

@Controller
@RequestMapping("/admin/tai-khoan")
@PreAuthorize("hasRole('ADMIN')")
public class TaiKhoanController {
    private final TaiKhoanService service;

    public TaiKhoanController(TaiKhoanService service) {
        this.service = service;
    }

    @ModelAttribute("cacVaiTro")
    public VaiTro[] cacVaiTro() {
        return VaiTro.values();
    }

    @GetMapping
    public String danhSach(Model model, Authentication authentication) {
        model.addAttribute("danhSach", service.danhSach());
        model.addAttribute("tenDangNhapHienTai", authentication.getName());
        return "account/danh-sach";
    }

    @GetMapping("/tao")
    public String tao(Model model) {
        model.addAttribute("form", new TaiKhoanForm());
        return "account/tao";
    }

    @PostMapping("/tao")
    public String tao(@Valid @ModelAttribute("form") TaiKhoanForm form, BindingResult errors,
                      RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "account/tao";
        }
        try {
            service.tao(form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "account/tao";
        }
        redirect.addFlashAttribute("thanhCong", "Đã tạo tài khoản. Nếu là SV/GV, hãy tạo hồ sơ tương ứng.");
        return "redirect:/admin/tai-khoan";
    }

    @GetMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, Model model) {
        var tk = service.chiTiet(id);
        var form = new SuaTaiKhoanForm();
        form.setHoTen(tk.hoTen());
        form.setEmail(tk.email());
        form.setVaiTros(new HashSet<>(tk.vaiTros()));
        model.addAttribute("form", form);
        model.addAttribute("taiKhoan", tk);
        return "account/sua";
    }

    @PostMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") SuaTaiKhoanForm form,
                      BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("taiKhoan", service.chiTiet(id));
        if (errors.hasErrors()) {
            return "account/sua";
        }
        try {
            service.sua(id, form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "account/sua";
        }
        redirect.addFlashAttribute("thanhCong", "Đã cập nhật tài khoản");
        return "redirect:/admin/tai-khoan";
    }

    @PostMapping("/{id}/xoa")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.xoa(id);
            redirect.addFlashAttribute("thanhCong", "Đã xóa tài khoản");
        } catch (NghiepVuException ex) {
            redirect.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/admin/tai-khoan";
    }

    @GetMapping("/{id}/dat-lai-mat-khau")
    public String datLaiMatKhau(@PathVariable Integer id, Model model) {
        model.addAttribute("taiKhoan", service.chiTiet(id));
        model.addAttribute("form", new DatLaiMatKhauForm());
        return "account/dat-lai-mat-khau";
    }

    @PostMapping("/{id}/dat-lai-mat-khau")
    public String datLaiMatKhau(@PathVariable Integer id,
                              @Valid @ModelAttribute("form") DatLaiMatKhauForm form,
                              BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("taiKhoan", service.chiTiet(id));
        if (!errors.hasErrors()) {
            try {
                service.datLaiMatKhau(id, form);
                redirect.addFlashAttribute("thanhCong", "Đã đặt lại mật khẩu. Người dùng cần đăng nhập lại bằng mật khẩu mới.");
                return "redirect:/admin/tai-khoan";
            } catch (NghiepVuException ex) {
                errors.reject("nghiepVu", ex.getMessage());
            }
        }
        // Không đưa mật khẩu vừa nhập trở lại HTML khi form có lỗi.
        form.setMatKhauMoi(null);
        form.setXacNhanMatKhau(null);
        return "account/dat-lai-mat-khau";
    }

    @PostMapping("/{id}/trang-thai")
    public String trangThai(@PathVariable Integer id, @RequestParam TrangThaiTaiKhoan trangThai,
                            RedirectAttributes redirect) {
        service.doiTrangThai(id, trangThai);
        redirect.addFlashAttribute("thanhCong", "Đã cập nhật trạng thái tài khoản");
        return "redirect:/admin/tai-khoan";
    }
}
