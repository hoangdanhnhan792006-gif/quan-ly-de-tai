package com.khoacntt.quanlydetai.thongbao.dto;
import com.khoacntt.quanlydetai.thongbao.model.*;
import java.util.List;
import java.time.LocalDateTime;
public record ThongBaoDto(Integer id, String tieuDe, String noiDung, DonViBanHanh donViBanHanh, String nguoiDang,
        TrangThaiThongBao trangThai, LocalDateTime congBoLuc, boolean coTheSua, List<TepDto> teps) {
    public record TepDto(Integer id, String ten, long kichThuoc) {
    }
}
