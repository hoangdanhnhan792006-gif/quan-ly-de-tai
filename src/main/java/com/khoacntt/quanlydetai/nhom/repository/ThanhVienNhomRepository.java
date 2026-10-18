package com.khoacntt.quanlydetai.nhom.repository;

import com.khoacntt.quanlydetai.nhom.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ThanhVienNhomRepository extends JpaRepository<ThanhVienNhom, ThanhVienNhomId> {
    List<ThanhVienNhom> findByNhomIdOrderBySinhVienMaSvAsc(Integer nhomId);
    Optional<ThanhVienNhom> findByNhomIdAndSinhVienId(Integer nhomId, Integer sinhVienId);
    boolean existsBySinhVienIdAndDotId(Integer sinhVienId, Integer dotId);
    long countByNhomId(Integer nhomId);
}
