package com.khoacntt.quanlydetai.baocao.dto;
import java.time.LocalDateTime;
import java.util.List;
public record BaoCaoDto(Integer id, int phienBan, String ghiChu, LocalDateTime nopLuc, List<TepDto> teps) {
    public record TepDto(Integer id, String loai, String ten, long kichThuoc) {
    }
}
