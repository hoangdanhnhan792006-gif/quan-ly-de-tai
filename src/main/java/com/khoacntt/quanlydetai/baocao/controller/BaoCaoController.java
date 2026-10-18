package com.khoacntt.quanlydetai.baocao.controller;
import com.khoacntt.quanlydetai.baocao.form.NopBaoCaoForm;
import com.khoacntt.quanlydetai.baocao.service.*;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/bao-cao")
public class BaoCaoController {
    private final BaoCaoService service;
    private final NopBaoCaoService nopService;
    private final LuuTepService storage;
    public BaoCaoController(BaoCaoService service, NopBaoCaoService nopService, LuuTepService storage) {
        this.service = service;
        this.nopService = nopService;
        this.storage = storage;
    }
    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("danhSach", service.danhSach());
        return "baocao/danh-sach";
    }
    @GetMapping("/dang-ky/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        model.addAttribute("hoSo", service.chiTiet(id));
        model.addAttribute("form", new NopBaoCaoForm());
        return "baocao/chi-tiet";
    }
    @PostMapping("/dang-ky/{id}/nop")
    @PreAuthorize("hasRole('SINH_VIEN')")
    public String nop(@PathVariable Integer id, @Valid @ModelAttribute("form") NopBaoCaoForm f, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                nopService.nop(id, f);
                redirect.addFlashAttribute("thanhCong", "Đã nộp phiên bản báo cáo mới");
                return "redirect:/bao-cao/dang-ky/" + id;
            } catch (NghiepVuException ex) {
                errors.reject("nghiepVu", ex.getMessage());
            }
        }
        model.addAttribute("hoSo", service.chiTiet(id));
        return "baocao/chi-tiet";
    }
    @GetMapping("/tep/{id}")
    public ResponseEntity<Resource> tep(@PathVariable Integer id) {
        var t = service.taiTep(id);
        return TaiTepResponse.tao(t, storage.doc(t.khoa()));
    }
}
