package com.khoacntt.quanlydetai.dangky.repository;

import com.khoacntt.quanlydetai.dangky.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface DangKyRepository extends JpaRepository<DangKy, Integer> {
    @Query("select d from DangKy d where d.trangThai = com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy.CHAP_THUAN "
            + "and exists (select t from ThanhVienNhom t where t.nhom = d.nhom and t.sinhVien.id = :svId) "
            + "order by d.dangKyLuc desc")
    List<DangKy> cuaSinhVien(@Param("svId") Integer svId);
    Optional<DangKy> findByNhomId(Integer nhomId);
    List<DangKy> findAllByOrderByDangKyLucDesc();
    boolean existsByDeTaiIdAndTrangThaiInAndNhomIdNot(Integer deTaiId, Collection<TrangThaiDangKy> states, Integer nhomId);
    @Query("select d.nhom.id from DangKy d where d.id = :id")
    Optional<Integer> timNhomId(@Param("id") Integer id);
}
