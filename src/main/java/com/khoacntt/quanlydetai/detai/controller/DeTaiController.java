package com.khoacntt.quanlydetai.detai.controller;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.detai.dto.DeTaiDto;
import com.khoacntt.quanlydetai.detai.form.*;
import com.khoacntt.quanlydetai.detai.service.DeTaiService;
import com.khoacntt.quanlydetai.dotdangky.service.DotDangKyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/de-tai")
public class DeTaiController {
    private final DeTaiService service;
    private final DotDangKyService dotService;

    public DeTaiController(DeTaiService service, DotDangKyService dotService) {
        this.service = service;
        this.dotService = dotService;
    }

    @GetMapping
    public String daCongBo(@RequestParam(required = false) Integer dotId,
                          @RequestParam(required = false) Integer boMonId, Model model) {
        model.addAttribute("tieuDe", "Đề tài đã công bố");
        model.addAttribute("danhSach", service.daCongBo(dotId, boMonId));
        model.addAttribute("boLoc", service.boLocCongBo());
        model.addAttribute("dotId", dotId);
        model.addAttribute("boMonId", boMonId);
        return "detai/danh-sach";
    }

    @GetMapping("/cua-toi")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String cuaToi(Model model) {
        model.addAttribute("tieuDe", "Đề tài tôi đề xuất hoặc hướng dẫn");
        model.addAttribute("danhSach", service.cuaToi());
        return "detai/danh-sach";
    }

    @GetMapping("/quan-ly")
    @PreAuthorize("hasAnyRole('TRUONG_BO_MON','TRUONG_KHOA','CAN_BO_KHOA')")
    public String quanLy(Model model) {
        model.addAttribute("tieuDe", "Duyệt và quản lý đề tài");
        model.addAttribute("danhSach", service.quanLy());
        return "detai/danh-sach";
    }

    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        napChiTiet(id, model);
        return "detai/chi-tiet";
    }

    @GetMapping("/tao")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String tao(Model model) {
        model.addAttribute("form", new DeTaiForm());
        napLuaChon(model);
        return "detai/form";
    }

    @PostMapping("/tao")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String tao(@Valid @ModelAttribute("form") DeTaiForm form, BindingResult errors,
                      Model model, RedirectAttributes redirect) {
        napLuaChon(model);
        if (errors.hasErrors()) return "detai/form";
        try {
            Integer id = service.tao(form);
            redirect.addFlashAttribute("thanhCong", "Đã lưu đề tài nháp. Bạn có thể kiểm tra và gửi duyệt.");
            return "redirect:/de-tai/" + id;
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "detai/form";
        }
    }

    @GetMapping("/{id}/sua")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String sua(@PathVariable Integer id, Model model) {
        model.addAttribute("form", service.formSua(id));
        model.addAttribute("id", id);
        napLuaChon(model);
        return "detai/form";
    }

    @PostMapping("/{id}/sua")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") DeTaiForm form,
                      BindingResult errors, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        napLuaChon(model);
        if (errors.hasErrors()) return "detai/form";
        try {
            service.sua(id, form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "detai/form";
        }
        redirect.addFlashAttribute("thanhCong", "Đã cập nhật đề tài");
        return "redirect:/de-tai/" + id;
    }

    @PostMapping("/{id}/gui-duyet")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String guiDuyet(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.guiDuyet(id);
            redirect.addFlashAttribute("thanhCong", "Đã gửi đề tài đến Trưởng bộ môn");
        } catch (NghiepVuException ex) {
            redirect.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/de-tai/" + id;
    }

    @PostMapping("/{id}/duyet")
    @PreAuthorize("hasRole('TRUONG_BO_MON')")
    public String duyet(@PathVariable Integer id, @Valid @ModelAttribute("duyetForm") DuyetDeTaiForm form,
                        BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.duyet(id, form);
                redirect.addFlashAttribute("thanhCong", "Đã xử lý đề xuất");
                return "redirect:/de-tai/" + id;
            } catch (NghiepVuException ex) {
                errors.reject("nghiepVu", ex.getMessage());
            }
        }
        napChiTiet(id, model);
        return "detai/chi-tiet";
    }

    @PostMapping("/{id}/huong-dan")
    @PreAuthorize("hasRole('TRUONG_BO_MON')")
    public String huongDan(@PathVariable Integer id, @Valid @ModelAttribute("huongDanForm") HuongDanForm form,
                           BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.phanCong(id, form);
                redirect.addFlashAttribute("thanhCong", "Đã lưu phân công hướng dẫn");
                return "redirect:/de-tai/" + id;
            } catch (NghiepVuException ex) {
                errors.reject("nghiepVu", ex.getMessage());
            }
        }
        napChiTiet(id, model);
        return "detai/chi-tiet";
    }

    @PostMapping("/{id}/cong-bo")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String congBo(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.congBo(id);
            redirect.addFlashAttribute("thanhCong", "Đã công bố đề tài cho sinh viên xem");
        } catch (NghiepVuException ex) {
            redirect.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/de-tai/" + id;
    }

    private void napLuaChon(Model model) {
        model.addAttribute("cacDot", dotService.dangMoDeXuat());
        model.addAttribute("cacBoMon", service.boMonDeXuat());
    }

    private void napChiTiet(Integer id, Model model) {
        DeTaiDto d = service.chiTiet(id);
        model.addAttribute("deTai", d);
        if (!model.containsAttribute("duyetForm")) model.addAttribute("duyetForm", new DuyetDeTaiForm());
        if (!model.containsAttribute("huongDanForm")) {
            HuongDanForm f = new HuongDanForm();
            f.setGiangVienIds(d.huongDans().stream().map(g -> g.id()).collect(Collectors.toSet()));
            model.addAttribute("huongDanForm", f);
        }
        if (d.coThePhanCong()) model.addAttribute("cacGiangVien", service.giangVienHuongDan());
    }
    @PostMapping("/{id}/xoa")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirect) {
        try { service.xoa(id); redirect.addFlashAttribute("thanhCong", "Đã xóa dữ liệu chưa sử dụng"); }
        catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/de-tai/cua-toi";
    }
}
