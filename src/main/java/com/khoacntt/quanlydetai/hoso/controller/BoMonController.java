package com.khoacntt.quanlydetai.hoso.controller;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.hoso.form.BoMonForm;
import com.khoacntt.quanlydetai.hoso.service.BoMonService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bo-mon")
@PreAuthorize("hasRole('ADMIN')")
public class BoMonController {
    private final BoMonService service;

    public BoMonController(BoMonService service) {
        this.service = service;
    }

    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", service.danhSach());
        return "hoso/bo-mon-danh-sach";
    }

    @GetMapping("/tao")
    public String tao(Model model) {
        model.addAttribute("form", new BoMonForm());
        return "hoso/bo-mon-form";
    }

    @PostMapping("/tao")
    public String tao(@Valid @ModelAttribute("form") BoMonForm form, BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "hoso/bo-mon-form";
        }
        try {
            service.tao(form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "hoso/bo-mon-form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã tạo bộ môn");
        return "redirect:/admin/bo-mon";
    }

    @GetMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, Model model) {
        model.addAttribute("id", id);
        var bm = service.chiTiet(id);
        var f = new BoMonForm();
        f.setMaBoMon(bm.maBoMon());
        f.setTenBoMon(bm.tenBoMon());
        f.setDangHoatDong(bm.dangHoatDong());
        model.addAttribute("form", f);
        return "hoso/bo-mon-form";
    }

    @PostMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") BoMonForm form,
                      BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (errors.hasErrors()) {
            return "hoso/bo-mon-form";
        }
        try {
            service.sua(id, form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "hoso/bo-mon-form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã cập nhật bộ môn");
        return "redirect:/admin/bo-mon";
    }
    @PostMapping("/{id}/xoa")
    @PreAuthorize("hasRole('ADMIN')")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirect) {
        try { service.xoa(id); redirect.addFlashAttribute("thanhCong", "Đã xóa dữ liệu chưa sử dụng"); }
        catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/admin/bo-mon";
    }
}
