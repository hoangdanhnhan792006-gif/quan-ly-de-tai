package com.khoacntt.quanlydetai.nhom.controller;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.nhom.form.*;
import com.khoacntt.quanlydetai.nhom.service.NhomService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sinh-vien/nhom")
@PreAuthorize("hasRole('SINH_VIEN')")
public class NhomController {
    private final NhomService service;
    public NhomController(NhomService service) { this.service = service; }
    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", service.cuaToi());
        model.addAttribute("loiMois", service.loiMoiCuaToi());
        model.addAttribute("cacDot", service.cacDotDangMo());
        return "nhom/danh-sach";
    }
    @GetMapping("/tao")
    public String tao(Model model) {
        model.addAttribute("form", new NhomForm());
        model.addAttribute("cacDot", service.cacDotDangMo());
        return "nhom/form";
    }
    @PostMapping("/tao")
    public String tao(@Valid @ModelAttribute("form") NhomForm f, BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("cacDot", service.cacDotDangMo());
        if (errors.hasErrors()) return "nhom/form";
        try {
            Integer id = service.tao(f);
            redirect.addFlashAttribute("thanhCong", "Đã tạo nhóm; bạn là nhóm trưởng");
            return "redirect:/sinh-vien/nhom/" + id;
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "nhom/form";
        }
    }
    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        nap(id, model);
        return "nhom/chi-tiet";
    }
    @PostMapping("/{id}/doi-ten")
    public String doiTen(@PathVariable Integer id, @Valid @ModelAttribute("tenForm") DoiTenNhomForm f,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.doiTen(id, f);
                redirect.addFlashAttribute("thanhCong", "Đã đổi tên nhóm");
                return veNhom(id);
            } catch (NghiepVuException ex) { errors.reject("nghiepVu", ex.getMessage()); }
        }
        nap(id, model);
        return "nhom/chi-tiet";
    }
    @PostMapping("/{id}/moi")
    public String moi(@PathVariable Integer id, @Valid @ModelAttribute("moiForm") MoiThanhVienForm f,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.moi(id, f);
                redirect.addFlashAttribute("thanhCong", "Đã gửi lời mời; sinh viên cần đồng ý mới trở thành thành viên");
                return veNhom(id);
            } catch (NghiepVuException ex) { errors.reject("nghiepVu", ex.getMessage()); }
        }
        nap(id, model);
        return "nhom/chi-tiet";
    }
    @PostMapping("/loi-moi/{id}/phan-hoi")
    public String phanHoi(@PathVariable Integer id, @RequestParam boolean chapNhan, RedirectAttributes redirect) {
        try {
            service.phanHoi(id, chapNhan);
            redirect.addFlashAttribute("thanhCong", "Đã phản hồi lời mời");
        } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/sinh-vien/nhom";
    }
    @PostMapping("/loi-moi/{id}/thu-hoi")
    public String thuHoi(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.thuHoi(id);
            redirect.addFlashAttribute("thanhCong", "Đã thu hồi lời mời");
        } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/sinh-vien/nhom";
    }
    @PostMapping("/{id}/doi-truong")
    public String doiTruong(@PathVariable Integer id, @RequestParam Integer sinhVienId, RedirectAttributes redirect) {
        try {
            service.doiTruong(id, sinhVienId);
            redirect.addFlashAttribute("thanhCong", "Đã chuyển quyền nhóm trưởng");
        } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return veNhom(id);
    }
    @PostMapping("/{id}/xoa-thanh-vien")
    public String xoaThanhVien(@PathVariable Integer id, @RequestParam Integer sinhVienId, RedirectAttributes redirect) {
        try {
            service.xoaThanhVien(id, sinhVienId);
            redirect.addFlashAttribute("thanhCong", "Đã đưa thành viên ra khỏi nhóm");
        } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return veNhom(id);
    }
    @PostMapping("/{id}/roi")
    public String roi(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.roiNhom(id);
            redirect.addFlashAttribute("thanhCong", "Đã rời nhóm");
        } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/sinh-vien/nhom";
    }
    @PostMapping("/{id}/giai-the")
    public String giaiThe(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.giaiThe(id);
            redirect.addFlashAttribute("thanhCong", "Đã giải thể nhóm và giải phóng đăng ký đang giữ chỗ");
        } catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/sinh-vien/nhom";
    }
    private String veNhom(Integer id) { return "redirect:/sinh-vien/nhom/" + id; }
    private void nap(Integer id, Model model) {
        var n = service.chiTiet(id);
        model.addAttribute("nhom", n);
        if (!model.containsAttribute("moiForm")) model.addAttribute("moiForm", new MoiThanhVienForm());
        if (!model.containsAttribute("tenForm")) {
            var f = new DoiTenNhomForm(); f.setTenNhom(n.tenNhom()); model.addAttribute("tenForm", f);
        }
    }
}
