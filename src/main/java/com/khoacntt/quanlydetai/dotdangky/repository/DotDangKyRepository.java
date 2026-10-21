package com.khoacntt.quanlydetai.dotdangky.repository;

import com.khoacntt.quanlydetai.dotdangky.model.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface DotDangKyRepository extends JpaRepository<DotDangKy, Integer> {
    boolean existsByMaDotAndIdNot(String maDot, Integer id);
    boolean existsByMaDot(String maDot);
    List<DotDangKy> findByTrangThaiOrderByIdDesc(TrangThaiDotDangKy trangThai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DotDangKy d where d.id = :id")
    Optional<DotDangKy> timVaKhoa(@Param("id") Integer id);

    @Query(value = "select count(*) from dang_ky where dot_id = :id and trang_thai in ('CHO_DUYET','CHAP_THUAN')", nativeQuery = true)
    long demDangKyGiuCho(@Param("id") Integer id);

    @Query("select d from DotDangKy d where d.trangThai = com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy.TRIEN_KHAI "
            + "and d.gvBatDau <= :now and d.gvKetThuc > :now order by d.id desc")
    List<DotDangKy> dangMoDeXuat(@Param("now") java.time.LocalDateTime now);

    @Query("select d from DotDangKy d where d.trangThai = com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy.TRIEN_KHAI "
            + "and d.gvKetThuc <= :now and d.svBatDau <= :now and d.svKetThuc > :now order by d.id desc")
    List<DotDangKy> dangMoSinhVien(@Param("now") java.time.LocalDateTime now);

}
