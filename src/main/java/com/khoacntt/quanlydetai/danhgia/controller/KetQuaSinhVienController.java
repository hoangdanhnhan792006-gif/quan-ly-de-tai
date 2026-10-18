package com.khoacntt.quanlydetai.danhgia.controller;
import com.khoacntt.quanlydetai.danhgia.service.KetQuaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.AccessDeniedException;
import java.util.LinkedHashMap;
@Controller
@PreAuthorize("hasRole('SINH_VIEN')")
public class KetQuaSinhVienController {
    private final KetQuaService service;
    public KetQuaSinhVienController(KetQuaService service) {
        this.service = service;
    }
    @GetMapping("/sinh-vien/ket-qua")
    public String ketQua(@RequestParam(required = false) Integer dotId, Model m) {
        m.addAllAttributes(service.traCuuSinhVien(dotId));
        m.addAttribute("dotId", dotId);
        return "danhgia/ket-qua-sinh-vien";
    }
}
