package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface YeuCauSuaDiemRepository extends JpaRepository<YeuCauSuaDiem, Integer> {
    List<YeuCauSuaDiem> findByPhanCongChamIdOrderByIdDesc(Integer id);
    List<YeuCauSuaDiem> findByPhanCongChamDanhGiaIdAndTrangThaiIn(Integer id,
            Collection<TrangThaiYeuCauSuaDiem> states);
    Optional<YeuCauSuaDiem> findFirstByPhanCongChamIdAndTrangThaiInOrderByIdDesc(Integer id,
            Collection<TrangThaiYeuCauSuaDiem> states);
    @Query("select y.phanCongCham.danhGia.id from YeuCauSuaDiem y where y.id=:id")
    Optional<Integer> timDanhGiaId(@Param("id") Integer id);
}
