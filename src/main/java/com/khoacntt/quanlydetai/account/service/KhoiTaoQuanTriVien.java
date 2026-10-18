package com.khoacntt.quanlydetai.account.service;

import com.khoacntt.quanlydetai.account.config.KhoiTaoQuanTriVienProperties;
import com.khoacntt.quanlydetai.account.model.TaiKhoan;
import com.khoacntt.quanlydetai.account.model.VaiTro;
import com.khoacntt.quanlydetai.account.repository.TaiKhoanRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class KhoiTaoQuanTriVien implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(KhoiTaoQuanTriVien.class);

    private final KhoiTaoQuanTriVienProperties properties;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    public KhoiTaoQuanTriVien(
            KhoiTaoQuanTriVienProperties properties,
            TaiKhoanRepository taiKhoanRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String username = batBuocCoGiaTri(properties.username(), "APP_BOOTSTRAP_ADMIN_USERNAME")
                .toLowerCase(Locale.ROOT);
        String password = batBuocCoGiaTri(properties.password(), "APP_BOOTSTRAP_ADMIN_PASSWORD");
        String fullName = batBuocCoGiaTri(properties.fullName(), "APP_BOOTSTRAP_ADMIN_FULL_NAME");
        String email = batBuocCoGiaTri(properties.email(), "APP_BOOTSTRAP_ADMIN_EMAIL")
                .toLowerCase(Locale.ROOT);

        if (password.length() < 8) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD phải có ít nhất 8 ký tự");
        }
        if (taiKhoanRepository.existsByTenDangNhap(username)) {
            LOGGER.info("Tài khoản quản trị '{}' đã tồn tại; bỏ qua bước khởi tạo.", username);
            return;
        }
        if (taiKhoanRepository.existsByEmail(email)) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_EMAIL đã được sử dụng");
        }

        TaiKhoan admin = TaiKhoan.taoMoi(
                username,
                passwordEncoder.encode(password),
                fullName,
                email,
                Set.of(VaiTro.ADMIN));
        taiKhoanRepository.save(admin);
        LOGGER.info("Đã tạo tài khoản quản trị đầu tiên '{}'. Hãy tắt APP_BOOTSTRAP_ADMIN_ENABLED.", username);
    }

    private String batBuocCoGiaTri(String value, String variableName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(variableName + " chưa được cấu hình");
        }
        return value.trim();
    }
}
