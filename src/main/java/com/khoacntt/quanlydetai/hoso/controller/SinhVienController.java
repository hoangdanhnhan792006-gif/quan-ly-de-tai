package com.khoacntt.quanlydetai.hoso.controller;

import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.service.TaiKhoanService;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.hoso.form.SinhVienForm;
import com.khoacntt.quanlydetai.hoso.service.HoSoService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sinh-vien")
@PreAuthorize("hasRole('ADMIN')")
public class SinhVienController {
    private final HoSoService service;
    private final TaiKhoanService taiKhoanService;

    public SinhVienController(HoSoService service, TaiKhoanService taiKhoanService) {
        this.service = service;
        this.taiKhoanService = taiKhoanService;
    }

    @ModelAttribute
    public void duLieuForm(Model model) {
        model.addAttribute("taiKhoans", taiKhoanService.theoVaiTro(VaiTro.SINH_VIEN));

    }

    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", service.sinhVien());
        return "hoso/sinh-vien-danh-sach";
    }

    @GetMapping("/tao")
    public String tao(Model model) {
        model.addAttribute("form", new SinhVienForm());
        return "hoso/sinh-vien-form";
    }

    @PostMapping("/tao")
    public String tao(@Valid @ModelAttribute("form") SinhVienForm form, BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "hoso/sinh-vien-form";
        }
        try {
            service.taoSinhVien(form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "hoso/sinh-vien-form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã tạo hồ sơ");
        return "redirect:/admin/sinh-vien";
    }

    @GetMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, Model model) {
        model.addAttribute("id", id);
        var hs = service.sinhVien(id);
        var f = new SinhVienForm();
        f.setTaiKhoanId(hs.taiKhoanId());
        f.setMaSv(hs.maSv());
        f.setLop(hs.lop());
        f.setKhoaHoc(hs.khoaHoc());
        model.addAttribute("form", f);
        return "hoso/sinh-vien-form";
    }

    @PostMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") SinhVienForm form,
                      BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (errors.hasErrors()) {
            return "hoso/sinh-vien-form";
        }
        try {
            service.suaSinhVien(id, form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "hoso/sinh-vien-form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã cập nhật hồ sơ");
        return "redirect:/admin/sinh-vien";
    }
    @PostMapping("/{id}/xoa")
    @PreAuthorize("hasRole('ADMIN')")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirect) {
        try { service.xoaSinhVien(id); redirect.addFlashAttribute("thanhCong", "Đã xóa dữ liệu chưa sử dụng"); }
        catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/admin/sinh-vien";
    }
}
