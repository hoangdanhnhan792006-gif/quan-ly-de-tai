package com.khoacntt.quanlydetai.nhom.dto;
import com.khoacntt.quanlydetai.nhom.model.TrangThaiNhom;
import com.khoacntt.quanlydetai.dangky.dto.DangKyDto;
import java.util.List;
public record NhomDto(Integer id, Integer dotId, String tenDot, String maNhom, String tenNhom,
        TrangThaiNhom trangThai, List<ThanhVienDto> thanhViens, List<LoiMoiDto> loiMois,
        DangKyDto dangKy, boolean truongNhom, boolean coTheSua, Integer sinhVienId) {}
