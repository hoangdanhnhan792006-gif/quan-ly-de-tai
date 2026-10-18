package com.khoacntt.quanlydetai.nhom.repository;

import com.khoacntt.quanlydetai.nhom.model.Nhom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface NhomRepository extends JpaRepository<Nhom, Integer> {
    boolean existsByDotIdAndMaNhom(Integer dotId, String maNhom);
    @Query("select n.dot.id from Nhom n where n.id = :id")
    Optional<Integer> timDotId(@Param("id") Integer id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Nhom n where n.id = :id")
    Optional<Nhom> timVaKhoa(@Param("id") Integer id);
    @Query("select n from Nhom n where exists (select t from ThanhVienNhom t where t.nhom = n and t.sinhVien.id = :svId) order by n.id desc")
    List<Nhom> cuaSinhVien(@Param("svId") Integer svId);
}
