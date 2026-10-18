package com.khoacntt.quanlydetai.hoso.controller;

import com.khoacntt.quanlydetai.hoso.service.HoSoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HoSoCaNhanController {
    private final HoSoService service;

    public HoSoCaNhanController(HoSoService service) {
        this.service = service;
    }

    @GetMapping("/ho-so")
    public String xem(Model model) {
        model.addAttribute("hoSo", service.caNhan());
        return "hoso/ca-nhan";
    }
}
