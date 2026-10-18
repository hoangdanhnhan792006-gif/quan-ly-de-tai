package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    Optional<DanhGia> findByDangKyId(Integer id);
    List<DanhGia> findAllByOrderByIdDesc();
    List<DanhGia> findByHoiDongId(Integer id);
    @Query("select d.dangKy.nhom.id from DanhGia d where d.id=:id")
    Optional<Integer> timNhomId(@Param("id") Integer id);
    @Query("select d from DanhGia d where d.hoiDongBatDau < :ketThuc and d.hoiDongKetThuc > :batDau and d.id <> :id")
    List<DanhGia> lichTrung(@Param("id") Integer id, @Param("batDau") LocalDateTime batDau,
            @Param("ketThuc") LocalDateTime ketThuc);
}
