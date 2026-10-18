package com.khoacntt.quanlydetai.danhgia.dto;
import com.khoacntt.quanlydetai.danhgia.model.*;
import java.util.List;
public record HoiDongDto(Integer id, Integer dotId, String tenDot, String maHoiDong, String tenHoiDong,
        TrangThaiHoiDong trangThai, List<ThanhVienDto> thanhViens) {
    public record ThanhVienDto(Integer id, String maGv, String hoTen, VaiTroHoiDong vaiTro) {
    }
}
