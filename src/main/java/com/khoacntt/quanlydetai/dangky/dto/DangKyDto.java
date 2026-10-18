package com.khoacntt.quanlydetai.dangky.dto;
import com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy;
import java.time.LocalDateTime;
public record DangKyDto(Integer id, Integer nhomId, String maNhom, String tenDot,
        Integer deTaiId, String tenDeTai, TrangThaiDangKy trangThai, String lyDo,
        LocalDateTime dangKyLuc, boolean coTheDuyet) {}
