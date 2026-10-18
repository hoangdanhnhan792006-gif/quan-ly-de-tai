package com.khoacntt.quanlydetai.nhom.repository;

import com.khoacntt.quanlydetai.nhom.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface LoiMoiNhomRepository extends JpaRepository<LoiMoiNhom, Integer> {
    List<LoiMoiNhom> findBySinhVienDuocMoiIdOrderByIdDesc(Integer svId);
    List<LoiMoiNhom> findByNhomIdOrderByIdDesc(Integer nhomId);
    List<LoiMoiNhom> findByNhomIdAndTrangThai(Integer nhomId, TrangThaiLoiMoiNhom trangThai);
    List<LoiMoiNhom> findBySinhVienDuocMoiIdAndNhomDotIdAndTrangThai(Integer svId, Integer dotId, TrangThaiLoiMoiNhom trangThai);
    boolean existsByNhomIdAndSinhVienDuocMoiIdAndTrangThai(Integer nhomId, Integer svId, TrangThaiLoiMoiNhom trangThai);
    @Query("select l.nhom.id from LoiMoiNhom l where l.id = :id")
    Optional<Integer> timNhomId(@Param("id") Integer id);
}
