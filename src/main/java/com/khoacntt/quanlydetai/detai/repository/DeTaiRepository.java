package com.khoacntt.quanlydetai.detai.repository;

import com.khoacntt.quanlydetai.detai.model.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface DeTaiRepository extends JpaRepository<DeTai, Integer> {
    boolean existsByMaDeTai(String ma);
    boolean existsByMaDeTaiAndIdNot(String ma, Integer id);
    List<DeTai> findByTrangThaiOrderByIdDesc(TrangThaiDeTai trangThai);

    @Query("select d from DeTai d where d.gvDeXuat.taiKhoan.id = :taiKhoanId or exists "
            + "(select h from HuongDan h where h.deTai = d and h.giangVien.taiKhoan.id = :taiKhoanId) order by d.id desc")
    List<DeTai> cuaGiangVien(@Param("taiKhoanId") Integer taiKhoanId);

    @Query("select d.dot.id from DeTai d where d.id = :id")
    Optional<Integer> timDotId(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DeTai d where d.id = :id")
    Optional<DeTai> timVaKhoa(@Param("id") Integer id);

    // Đọc bảng liên quan mà không phải triển khai Entity của thành viên 3/4.
    @Query(value = "select count(*) from dang_ky dk join danh_gia dg on dg.dang_ky_id = dk.id "
            + "join phan_cong_cham pc on pc.danh_gia_id = dg.id "
            + "where dk.de_tai_id = :id and pc.giang_vien_id = :gvId", nativeQuery = true)
    long demPhanCongCham(@Param("id") Integer id, @Param("gvId") Integer gvId);

    @Query(value = "select count(*) from dang_ky dk join danh_gia dg on dg.dang_ky_id = dk.id "
            + "join thanh_vien_hoi_dong tv on tv.hoi_dong_id = dg.hoi_dong_id "
            + "where dk.de_tai_id = :id and tv.giang_vien_id = :gvId", nativeQuery = true)
    long demThanhVienHoiDong(@Param("id") Integer id, @Param("gvId") Integer gvId);
}
