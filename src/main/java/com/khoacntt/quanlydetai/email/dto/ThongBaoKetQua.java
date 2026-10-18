package com.khoacntt.quanlydetai.email.dto;

import java.util.List;

/** Dữ liệu độc lập với JPA để dùng an toàn ở luồng gửi email nền. */
public record ThongBaoKetQua(Integer ketQuaId, int phienBan, String tenDeTai,
                            String tenDot, List<String> nguoiNhans) {
    public ThongBaoKetQua {
        nguoiNhans = List.copyOf(nguoiNhans);
    }
}
