package com.khoacntt.quanlydetai.thongbao.controller;
import com.khoacntt.quanlydetai.thongbao.form.ThongBaoForm;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
import com.khoacntt.quanlydetai.thongbao.service.*;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.storage.*;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/thong-bao")
public class ThongBaoController {
    private final ThongBaoService service;
    private final TepThongBaoService upload;
    private final LuuTepService storage;
    public ThongBaoController(ThongBaoService service, TepThongBaoService upload, LuuTepService storage) {
        this.service = service;
        this.upload = upload;
        this.storage = storage;
    }
    @GetMapping
    public String danhSach(Model m) {
        m.addAttribute("danhSach", service.congKhai());
        return "thongbao/danh-sach";
    }
    @GetMapping("/quan-ly")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String quanLy(Model m) {
        m.addAttribute("danhSach", service.quanLy());
        return "thongbao/danh-sach";
    }
    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Integer id, Model m) {
        m.addAttribute("thongBao", service.chiTiet(id));
        return "thongbao/chi-tiet";
    }
    @GetMapping("/tao")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String tao(Model m) {
        m.addAttribute("form", new ThongBaoForm());
        m.addAttribute("donVis", DonViBanHanh.values());
        return "thongbao/form";
    }
    @GetMapping("/{id}/sua")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String sua(@PathVariable Integer id, Model m) {
        m.addAttribute("form", service.formSua(id));
        m.addAttribute("id", id);
        m.addAttribute("donVis", DonViBanHanh.values());
        return "thongbao/form";
    }
    @PostMapping("/tao")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String tao(@Valid @ModelAttribute("form") ThongBaoForm f, BindingResult e, Model m) {
        if (e.hasErrors()) {
            m.addAttribute("donVis", DonViBanHanh.values());
            return "thongbao/form";
        }
        return "redirect:/thong-bao/" + service.tao(f);
    }
    @PostMapping("/{id}/sua")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") ThongBaoForm f, BindingResult e, Model m,
            RedirectAttributes r) {
        if (!e.hasErrors())
            try {
                service.sua(id, f);
                r.addFlashAttribute("thanhCong", "Đã lưu thông báo");
                return ve(id);
            } catch (NghiepVuException ex) {
                e.reject("nghiepVu", ex.getMessage());
            }
        m.addAttribute("id", id);
        m.addAttribute("donVis", DonViBanHanh.values());
        return "thongbao/form";
    }
    @PostMapping("/{id}/tep")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String themTep(@PathVariable Integer id, @RequestParam MultipartFile tep, RedirectAttributes r) {
        try {
            upload.them(id, tep);
            r.addFlashAttribute("thanhCong", "Đã thêm tệp đính kèm");
        } catch (NghiepVuException ex) {
            r.addFlashAttribute("loi", ex.getMessage());
        }
        return ve(id);
    }
    @PostMapping("/{id}/cong-bo")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String congBo(@PathVariable Integer id, RedirectAttributes r) {
        try {
            service.congBo(id);
            r.addFlashAttribute("thanhCong", "Đã công bố thông báo");
        } catch (NghiepVuException ex) {
            r.addFlashAttribute("loi", ex.getMessage());
        }
        return ve(id);
    }
    @PostMapping("/{id}/thu-hoi")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String thuHoi(@PathVariable Integer id, RedirectAttributes r) {
        try {
            service.thuHoi(id);
            r.addFlashAttribute("thanhCong", "Đã thu hồi thông báo");
        } catch (NghiepVuException ex) {
            r.addFlashAttribute("loi", ex.getMessage());
        }
        return ve(id);
    }
    @GetMapping("/tep/{id}")
    public ResponseEntity<Resource> taiTep(@PathVariable Integer id) {
        var t = service.taiTep(id);
        return TaiTepResponse.tao(t, storage.doc(t.khoa()));
    }
    private String ve(Integer id) {
        return "redirect:/thong-bao/" + id;
    }
    @PostMapping("/{id}/xoa")
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirect) {
        try { service.xoa(id); redirect.addFlashAttribute("thanhCong", "Đã xóa dữ liệu chưa sử dụng"); }
        catch (NghiepVuException ex) { redirect.addFlashAttribute("loi", ex.getMessage()); }
        return "redirect:/thong-bao/quan-ly";
    }
}
