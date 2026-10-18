package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface HoiDongRepository extends JpaRepository<HoiDong, Integer> {
    List<HoiDong> findAllByOrderByIdDesc();
    List<HoiDong> findByDotIdAndTrangThai(Integer id, TrangThaiHoiDong state);
    boolean existsByDotIdAndMaHoiDong(Integer dotId, String ma);
    @Query("select h.dot.id from HoiDong h where h.id=:id")
    Optional<Integer> timDotId(@Param("id") Integer id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from HoiDong h where h.id=:id")
    Optional<HoiDong> timVaKhoa(@Param("id") Integer id);
}
