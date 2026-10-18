package com.khoacntt.quanlydetai.hoso.controller;

import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.service.TaiKhoanService;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.hoso.form.GiangVienForm;
import com.khoacntt.quanlydetai.hoso.service.BoMonService;
import com.khoacntt.quanlydetai.hoso.service.HoSoService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/giang-vien")
@PreAuthorize("hasRole('ADMIN')")
public class GiangVienController {
    private final HoSoService service;
    private final TaiKhoanService taiKhoanService;
    private final BoMonService boMonService;

    public GiangVienController(HoSoService service, TaiKhoanService taiKhoanService, BoMonService boMonService) {
        this.service = service;
        this.taiKhoanService = taiKhoanService;
        this.boMonService = boMonService;
    }

    @ModelAttribute
    public void duLieuForm(Model model) {
        model.addAttribute("taiKhoans", taiKhoanService.theoVaiTro(VaiTro.GIANG_VIEN));
        model.addAttribute("boMons", boMonService.danhSach());
    }

    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", service.giangVien());
        return "hoso/giang-vien-danh-sach";
    }

    @GetMapping("/tao")
    public String tao(Model model) {
        model.addAttribute("form", new GiangVienForm());
        return "hoso/giang-vien-form";
    }

    @PostMapping("/tao")
    public String tao(@Valid @ModelAttribute("form") GiangVienForm form, BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "hoso/giang-vien-form";
        }
        try {
            service.taoGiangVien(form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "hoso/giang-vien-form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã tạo hồ sơ");
        return "redirect:/admin/giang-vien";
    }

    @GetMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, Model model) {
        model.addAttribute("id", id);
        var hs = service.giangVien(id);
        var f = new GiangVienForm();
        f.setTaiKhoanId(hs.taiKhoanId());
        f.setMaGv(hs.maGv());
        f.setBoMonId(hs.boMonId());
        f.setHocVi(hs.hocVi());
        model.addAttribute("form", f);
        return "hoso/giang-vien-form";
    }

    @PostMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") GiangVienForm form,
                      BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (errors.hasErrors()) {
            return "hoso/giang-vien-form";
        }
        try {
            service.suaGiangVien(id, form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "hoso/giang-vien-form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã cập nhật hồ sơ");
        return "redirect:/admin/giang-vien";
    }
    @PostMapping("/{id}/xoa")
    @PreAuthorize("hasRole('ADMIN')")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirect) {
        try { service.xoaGiangVien(id); redirect.addFlashAttribute("thanhCong", "Đã xóa dữ liệu chưa sử dụng"); }
        catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/admin/giang-vien";
    }
}
