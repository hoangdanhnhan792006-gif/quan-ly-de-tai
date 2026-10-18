package com.khoacntt.quanlydetai.common.time;

import java.time.*;

/** Form dùng giờ Việt Nam; các DATETIME trong database lưu giờ UTC. */
public final class ThoiGian {
    private static final ZoneId VIET_NAM = ZoneId.of("Asia/Ho_Chi_Minh");

    private ThoiGian() {}

    public static LocalDateTime hienTai(Clock clock) {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    public static LocalDateTime sangUtc(LocalDateTime value) {
        return value == null ? null : value.atZone(VIET_NAM).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static LocalDateTime sangGioVietNam(LocalDateTime value) {
        return value == null ? null : value.atZone(ZoneOffset.UTC).withZoneSameInstant(VIET_NAM).toLocalDateTime();
    }
}
