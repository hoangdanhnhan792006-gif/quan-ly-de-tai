package com.khoacntt.quanlydetai.dangky.controller;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.dangky.form.DangKyForm;
import com.khoacntt.quanlydetai.dangky.service.DangKyService;
import com.khoacntt.quanlydetai.nhom.service.NhomService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sinh-vien/dang-ky")
@PreAuthorize("hasRole('SINH_VIEN')")
public class DangKyController {
    private final DangKyService service;
    private final NhomService nhomService;
    public DangKyController(DangKyService service, NhomService nhomService) {
        this.service = service; this.nhomService = nhomService;
    }
    @GetMapping("/{nhomId}")
    public String form(@PathVariable Integer nhomId, Model model) {
        model.addAttribute("form", new DangKyForm());
        nap(nhomId, model);
        return "dangky/form";
    }
    @PostMapping("/{nhomId}")
    public String dangKy(@PathVariable Integer nhomId, @Valid @ModelAttribute("form") DangKyForm f,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.dangKy(nhomId, f);
                redirect.addFlashAttribute("thanhCong", "Đã giữ chỗ đề tài, chờ khoa duyệt");
                return "redirect:/sinh-vien/nhom/" + nhomId;
            } catch (NghiepVuException ex) {
                // Trả về nhóm để vẫn xem được đăng ký cũ nếu thời hạn/trạng thái vừa thay đổi.
                redirect.addFlashAttribute("loi", ex.getMessage());
                return "redirect:/sinh-vien/nhom/" + nhomId;
            }
        }
        nap(nhomId, model);
        return "dangky/form";
    }
    @PostMapping("/{nhomId}/huy")
    public String huy(@PathVariable Integer nhomId, RedirectAttributes redirect) {
        try {
            service.huy(nhomId);
            redirect.addFlashAttribute("thanhCong", "Đã hủy đăng ký và trả chỗ đề tài");
        } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/sinh-vien/nhom/" + nhomId;
    }
    private void nap(Integer nhomId, Model model) {
        model.addAttribute("nhom", nhomService.chiTiet(nhomId));
        model.addAttribute("deTais", service.luaChon(nhomId));
    }
}
