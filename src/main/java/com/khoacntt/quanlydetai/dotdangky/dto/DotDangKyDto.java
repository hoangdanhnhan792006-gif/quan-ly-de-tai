package com.khoacntt.quanlydetai.dotdangky.dto;

import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import java.time.*;

public record DotDangKyDto(Integer id, String maDot, String tenDot, LoaiDeTai loai,
                          LocalDateTime gvBatDau, LocalDateTime gvKetThuc,
                          LocalDateTime svBatDau, LocalDateTime svKetThuc,
                          LocalDateTime hanNopBaoCao, LocalDateTime hanNopDiemPb,
                          LocalDate ngayBaoCaoHd, TrangThaiDotDangKy trangThai) {
    public static DotDangKyDto tu(DotDangKy d) {
        return new DotDangKyDto(d.getId(), d.getMaDot(), d.getTenDot(), d.getLoai(),
                ThoiGian.sangGioVietNam(d.getGvBatDau()), ThoiGian.sangGioVietNam(d.getGvKetThuc()),
                ThoiGian.sangGioVietNam(d.getSvBatDau()), ThoiGian.sangGioVietNam(d.getSvKetThuc()),
                ThoiGian.sangGioVietNam(d.getHanNopBaoCao()), ThoiGian.sangGioVietNam(d.getHanNopDiemPb()),
                d.getNgayBaoCaoHd(), d.getTrangThai());
    }
}
