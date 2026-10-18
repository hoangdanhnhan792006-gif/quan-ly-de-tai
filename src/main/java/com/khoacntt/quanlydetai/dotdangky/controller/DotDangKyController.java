package com.khoacntt.quanlydetai.dotdangky.controller;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.dotdangky.dto.DotDangKyDto;
import com.khoacntt.quanlydetai.dotdangky.form.DotDangKyForm;
import com.khoacntt.quanlydetai.dotdangky.model.LoaiDeTai;
import com.khoacntt.quanlydetai.dotdangky.service.DotDangKyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/khoa/dot-dang-ky")
@PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
public class DotDangKyController {
    private final DotDangKyService service;

    public DotDangKyController(DotDangKyService service) {
        this.service = service;
    }

    @ModelAttribute("cacLoai")
    public LoaiDeTai[] cacLoai() {
        return LoaiDeTai.values();
    }

    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", service.danhSach());
        return "dotdangky/danh-sach";
    }

    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        model.addAttribute("dot", service.chiTiet(id));
        return "dotdangky/chi-tiet";
    }

    @GetMapping("/tao")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String tao(Model model) {
        model.addAttribute("form", new DotDangKyForm());
        return "dotdangky/form";
    }

    @PostMapping("/tao")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String tao(@Valid @ModelAttribute("form") DotDangKyForm form, BindingResult errors,
                      RedirectAttributes redirect) {
        if (errors.hasErrors()) return "dotdangky/form";
        try {
            Integer id = service.tao(form);
            redirect.addFlashAttribute("thanhCong", "Đã lưu đợt nháp. Kiểm tra lịch trước khi triển khai.");
            return "redirect:/khoa/dot-dang-ky/" + id;
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "dotdangky/form";
        }
    }

    @GetMapping("/{id}/sua")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String sua(@PathVariable Integer id, Model model) {
        DotDangKyDto d = service.chiTiet(id);
        DotDangKyForm f = new DotDangKyForm();
        f.setMaDot(d.maDot());
        f.setTenDot(d.tenDot());
        f.setLoai(d.loai());
        f.setGvBatDau(d.gvBatDau());
        f.setGvKetThuc(d.gvKetThuc());
        f.setSvBatDau(d.svBatDau());
        f.setSvKetThuc(d.svKetThuc());
        f.setHanNopBaoCao(d.hanNopBaoCao());
        f.setHanNopDiemPb(d.hanNopDiemPb());
        f.setNgayBaoCaoHd(d.ngayBaoCaoHd());
        model.addAttribute("form", f);
        model.addAttribute("id", id);
        return "dotdangky/form";
    }

    @PostMapping("/{id}/sua")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") DotDangKyForm form,
                      BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (errors.hasErrors()) return "dotdangky/form";
        try {
            service.sua(id, form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "dotdangky/form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã cập nhật đợt");
        return "redirect:/khoa/dot-dang-ky/" + id;
    }

    @PostMapping("/{id}/trien-khai")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String trienKhai(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.trienKhai(id);
            redirect.addFlashAttribute("thanhCong", "Đã triển khai. Cổng đăng ký mở theo các mốc thời gian đã đặt.");
        } catch (NghiepVuException ex) {
            redirect.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/khoa/dot-dang-ky/" + id;
    }

    @PostMapping("/{id}/ket-thuc")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String ketThuc(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.ketThuc(id);
            redirect.addFlashAttribute("thanhCong", "Đã kết thúc đợt");
        } catch (NghiepVuException ex) {
            redirect.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/khoa/dot-dang-ky/" + id;
    }

    @PostMapping("/{id}/huy")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String huy(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.huy(id);
            redirect.addFlashAttribute("thanhCong", "Đã hủy đợt");
        } catch (NghiepVuException ex) {
            redirect.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/khoa/dot-dang-ky/" + id;
    }
    @PostMapping("/{id}/xoa")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirect) {
        try { service.xoa(id); redirect.addFlashAttribute("thanhCong", "Đã xóa dữ liệu chưa sử dụng"); }
        catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/khoa/dot-dang-ky";
    }
}
