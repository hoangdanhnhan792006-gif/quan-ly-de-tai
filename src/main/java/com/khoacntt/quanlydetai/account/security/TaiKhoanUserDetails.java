package com.khoacntt.quanlydetai.account.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;

/** Giữ dấu kiểm tra để phát hiện đổi mật khẩu, kể cả sau khi Spring xóa credentials khỏi phiên. */
public class TaiKhoanUserDetails extends User {
    private static final long serialVersionUID = 1L;
    private final String dauKiemTraMatKhau;
    private final String hoTen;

    public TaiKhoanUserDetails(String username, String password, boolean enabled,
                              Collection<? extends GrantedAuthority> authorities) {
        this(username, password, enabled, authorities, username);
    }

    public TaiKhoanUserDetails(String username, String password, boolean enabled,
                              Collection<? extends GrantedAuthority> authorities, String hoTen) {
        super(username, password, enabled, true, true, true, authorities);
        this.dauKiemTraMatKhau = dauKiemTra(password);
        this.hoTen = hoTen;
    }

    public String getHoTen() {
        return hoTen == null || hoTen.isBlank() ? getUsername() : hoTen;
    }

    public boolean matKhauDaThayDoi(String matKhauBamHienTai) {
        return !dauKiemTraMatKhau.equals(dauKiemTra(matKhauBamHienTai));
    }

    private static String dauKiemTra(String matKhauBam) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(matKhauBam.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Java cần hỗ trợ SHA-256", ex);
        }
    }
}
