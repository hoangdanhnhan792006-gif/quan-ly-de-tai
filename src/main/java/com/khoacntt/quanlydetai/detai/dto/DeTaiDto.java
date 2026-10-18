package com.khoacntt.quanlydetai.detai.dto;

import com.khoacntt.quanlydetai.detai.model.TrangThaiDeTai;
import com.khoacntt.quanlydetai.dotdangky.model.LoaiDeTai;
import com.khoacntt.quanlydetai.hoso.dto.GiangVienDto;
import java.util.List;

public record DeTaiDto(Integer id, Integer dotId, String tenDot, LoaiDeTai loai,
                       Integer boMonId, String tenBoMon, String maDeTai, String tenDeTai,
                       String noiDung, String yeuCau, String sanPhamDuKien,
                       String nguoiDeXuat, TrangThaiDeTai trangThai, String nhanXetDuyet,
                       List<GiangVienDto> huongDans, boolean coTheSua, boolean coTheGui,
                       boolean coTheDuyet, boolean coThePhanCong, boolean coTheCongBo) {}
