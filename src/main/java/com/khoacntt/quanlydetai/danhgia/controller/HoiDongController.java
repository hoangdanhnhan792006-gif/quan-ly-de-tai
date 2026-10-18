package com.khoacntt.quanlydetai.danhgia.controller;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.danhgia.form.HoiDongForm;
import com.khoacntt.quanlydetai.danhgia.model.VaiTroHoiDong;
import com.khoacntt.quanlydetai.danhgia.service.HoiDongService;
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
@RequestMapping("/khoa/hoi-dong")
@PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
public class HoiDongController {
    private final HoiDongService service;
    private final DotDangKyService dots;
    private final DeTaiService deTais;
    public HoiDongController(HoiDongService service, DotDangKyService dots, DeTaiService deTais) {
        this.service = service;
        this.dots = dots;
        this.deTais = deTais;
    }
    @GetMapping
    public String danhSach(Model m) {
        m.addAttribute("danhSach", service.danhSach());
        return "danhgia/hoi-dong";
    }
    @GetMapping("/tao")
    public String tao(Model m) {
        m.addAttribute("form", new HoiDongForm());
        luaChon(m);
        return "danhgia/hoi-dong-form";
    }
    @GetMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, Model m) {
        var d = service.chiTiet(id);
        var f = new HoiDongForm();
        f.setDotId(d.dotId());
        f.setMaHoiDong(d.maHoiDong());
        f.setTenHoiDong(d.tenHoiDong());
        d.thanhViens().stream().filter(t -> t.vaiTro() == VaiTroHoiDong.CHU_TICH).findFirst()
                .ifPresent(t -> f.setChuTichId(t.id()));
        d.thanhViens().stream().filter(t -> t.vaiTro() == VaiTroHoiDong.THU_KY).findFirst()
                .ifPresent(t -> f.setThuKyId(t.id()));
        f.setUyVienIds(d.thanhViens().stream().filter(t -> t.vaiTro() == VaiTroHoiDong.UY_VIEN).map(t -> t.id())
                .collect(Collectors.toSet()));
        m.addAttribute("form", f);
        m.addAttribute("id", id);
        luaChon(m);
        return "danhgia/hoi-dong-form";
    }
    @PostMapping("/tao")
    public String tao(@Valid @ModelAttribute("form") HoiDongForm f, BindingResult e, Model m, RedirectAttributes r) {
        if (!e.hasErrors())
            try {
                service.tao(f);
                r.addFlashAttribute("thanhCong", "Đã tạo hội đồng nháp");
                return "redirect:/khoa/hoi-dong";
            } catch (NghiepVuException ex) {
                e.reject("nghiepVu", ex.getMessage());
            }
        luaChon(m);
        return "danhgia/hoi-dong-form";
    }
    @PostMapping("/{id}/sua")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") HoiDongForm f, BindingResult e, Model m,
            RedirectAttributes r) {
        if (!e.hasErrors())
            try {
                service.sua(id, f);
                r.addFlashAttribute("thanhCong", "Đã sửa hội đồng");
                return "redirect:/khoa/hoi-dong";
            } catch (NghiepVuException ex) {
                e.reject("nghiepVu", ex.getMessage());
            }
        m.addAttribute("id", id);
        luaChon(m);
        return "danhgia/hoi-dong-form";
    }
    @PostMapping("/{id}/chot")
    public String chot(@PathVariable Integer id, RedirectAttributes r) {
        try {
            service.chot(id);
            r.addFlashAttribute("thanhCong", "Đã chốt thành phần hội đồng");
        } catch (NghiepVuException ex) {
            r.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/khoa/hoi-dong";
    }
    @PostMapping("/{id}/hoan-thanh")
    public String hoanThanh(@PathVariable Integer id, RedirectAttributes r) {
        try {
            service.hoanThanh(id);
            r.addFlashAttribute("thanhCong", "Hội đồng đã hoàn thành");
        } catch (NghiepVuException ex) {
            r.addFlashAttribute("loi", ex.getMessage());
        }
        return "redirect:/khoa/hoi-dong";
    }
    private void luaChon(Model m) {
        m.addAttribute("cacDot", dots.danhSach());
        m.addAttribute("giangViens", deTais.giangVienHuongDan());
    }
}
