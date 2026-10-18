package com.khoacntt.quanlydetai.danhgia.dto;
import com.khoacntt.quanlydetai.danhgia.model.TrangThaiKetQua;
import java.math.BigDecimal;
import java.util.List;
public record KetQuaDto(Integer id, int phienBan, BigDecimal diemCuoi, boolean dat, String nhanXet,
        TrangThaiKetQua trangThai, boolean dangDieuChinh, List<DiemDto> diems) {
    public record DiemDto(String giangVien, int phienBanPhieu, BigDecimal diem, String nhanXet) {
    }
}
