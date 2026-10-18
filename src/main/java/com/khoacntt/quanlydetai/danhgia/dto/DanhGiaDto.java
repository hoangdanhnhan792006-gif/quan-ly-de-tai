package com.khoacntt.quanlydetai.danhgia.dto;
import com.khoacntt.quanlydetai.danhgia.model.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record DanhGiaDto(Integer id, Integer dangKyId, String maNhom, String tenDeTai, String tenDot,
        TrangThaiPhanBien phanBien, TrangThaiDanhGiaHoiDong hoiDong, String tenHoiDong, LocalDateTime lichPhanBien,
        String noiPhanBien, LocalDateTime batDauHoiDong, LocalDateTime ketThucHoiDong, String noiHoiDong,
        Integer phienBanBaoCao, boolean laKhoa, boolean laChuTich, List<PhanCongDto> phanCongs,
        List<KetQuaDto> ketQuas) {
    public record PhanCongDto(Integer id, String giangVien, boolean phanBien, boolean hoiDong, boolean cuaToi,
            boolean duocCham, boolean duocXinSua, List<PhieuDto> phieus, List<YeuCauDto> yeuCaus) {
    }
    public record PhieuDto(Integer id, int phienBan, BigDecimal diem, String nhanXet, TrangThaiPhieuCham trangThai) {
    }
    public record YeuCauDto(Integer id, String lyDo, TrangThaiYeuCauSuaDiem trangThai, String nhanXetDuyet) {
    }
}
