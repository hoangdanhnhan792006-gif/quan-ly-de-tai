package com.khoacntt.quanlydetai.account.controller;

import com.khoacntt.quanlydetai.account.form.DoiMatKhauForm;
import com.khoacntt.quanlydetai.account.service.TaiKhoanService;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ho-so/doi-mat-khau")
public class MatKhauController {
    private final TaiKhoanService service;

    public MatKhauController(TaiKhoanService service) {
        this.service = service;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("form", new DoiMatKhauForm());
        return "account/doi-mat-khau";
    }

    @PostMapping
    public String doi(@Valid @ModelAttribute("form") DoiMatKhauForm form, BindingResult errors,
                      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (errors.hasErrors()) {
            return "account/doi-mat-khau";
        }
        try {
            service.doiMatKhau(form);
        } catch (NghiepVuException ex) {
            errors.reject("nghiepVu", ex.getMessage());
            return "account/doi-mat-khau";
        }
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?passwordChanged";
    }
}
