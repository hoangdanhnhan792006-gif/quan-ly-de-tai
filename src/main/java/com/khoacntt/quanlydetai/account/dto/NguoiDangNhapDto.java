package com.khoacntt.quanlydetai.account.dto;

/** Chỉ đưa tên hiển thị ra view, không đưa UserDetails hay thông tin xác thực. */
public record NguoiDangNhapDto(String hoTen, String tenDangNhap) {
}
