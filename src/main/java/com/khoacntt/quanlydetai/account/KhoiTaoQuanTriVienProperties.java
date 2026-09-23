package com.khoacntt.quanlydetai.account;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record KhoiTaoQuanTriVienProperties(
        boolean enabled,
        String username,
        String password,
        String fullName,
        String email) {
}
