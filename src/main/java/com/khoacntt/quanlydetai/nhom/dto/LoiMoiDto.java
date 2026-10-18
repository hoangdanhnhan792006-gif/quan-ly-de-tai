package com.khoacntt.quanlydetai.nhom.dto;
import com.khoacntt.quanlydetai.nhom.model.TrangThaiLoiMoiNhom;
public record LoiMoiDto(Integer id, Integer nhomId, String maNhom, String tenDot, String nguoiMoi,
        String nguoiNhan, String loiNhan, TrangThaiLoiMoiNhom trangThai, String lyDo, boolean coThePhanHoi) {}
