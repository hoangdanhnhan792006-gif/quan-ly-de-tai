package com.khoacntt.quanlydetai.baocao.dto;
import java.time.LocalDateTime;
import java.util.List;
public record HoSoBaoCaoDto(Integer dangKyId, String maNhom, String tenDeTai, LocalDateTime hanNop, boolean coTheNop,
        List<BaoCaoDto> baoCaos) {
}
