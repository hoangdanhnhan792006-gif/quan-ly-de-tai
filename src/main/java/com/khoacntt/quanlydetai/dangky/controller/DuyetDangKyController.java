package com.khoacntt.quanlydetai.dangky.controller;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.dangky.form.DuyetDangKyForm;
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
@RequestMapping("/khoa/dang-ky")
@PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
public class DuyetDangKyController {
    private final DangKyService service;
    private final NhomService nhomService;
    public DuyetDangKyController(DangKyService service, NhomService nhomService) {
        this.service = service; this.nhomService = nhomService;
    }
    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", service.quanLy());
        return "dangky/quan-ly";
    }
    @GetMapping("/nhom/{id}")
    public String nhom(@PathVariable Integer id, Model model) {
        model.addAttribute("nhom", nhomService.chiTiet(id));
        return "dangky/nhom";
    }
    @PostMapping("/{id}/duyet")
    public String duyet(@PathVariable Integer id, @Valid @ModelAttribute("form") DuyetDangKyForm f,
            BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            redirect.addFlashAttribute("loi", "Vui lòng chọn chấp thuận/từ chối, kiểm tra đề tài và lý do tối đa 2000 ký tự");
        } else {
            try {
                service.duyet(id, f);
                redirect.addFlashAttribute("thanhCong", "Đã xử lý đăng ký của nhóm");
            } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        }
        return "redirect:/khoa/dang-ky";
    }
}
