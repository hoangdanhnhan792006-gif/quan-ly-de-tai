package com.khoacntt.quanlydetai.dangky.repository;

import com.khoacntt.quanlydetai.dangky.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface DangKyRepository extends JpaRepository<DangKy, Integer> {
    @EntityGraph(attributePaths = {"nhom.dot", "deTai"})
    @Query("select d from DangKy d where d.trangThai = com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy.CHAP_THUAN "
            + "and exists (select t from ThanhVienNhom t where t.nhom = d.nhom and t.sinhVien.id = :svId) "
            + "order by d.dangKyLuc desc")
    List<DangKy> cuaSinhVien(@Param("svId") Integer svId);
    Optional<DangKy> findByNhomId(Integer nhomId);
    List<DangKy> findAllByOrderByDangKyLucDesc();
    boolean existsByDeTaiIdAndTrangThaiInAndNhomIdNot(Integer deTaiId, Collection<TrangThaiDangKy> states, Integer nhomId);
    @Query("select d.nhom.id from DangKy d where d.id = :id")
    Optional<Integer> timNhomId(@Param("id") Integer id);

    @Query("select d.deTai.id from DangKy d where d.deTai.id in :ids and d.trangThai in :states and d.nhom.id <> :nhomId")
    Set<Integer> deTaiDaGiuCho(@Param("ids") Collection<Integer> ids,
            @Param("states") Collection<TrangThaiDangKy> states, @Param("nhomId") Integer nhomId);

    @EntityGraph(attributePaths = {"nhom.dot", "deTai"})
    @Query("select d from DangKy d where d.trangThai = com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy.CHAP_THUAN "
            + "and (:laKhoa = true "
            + "or (:laSinhVien = true and exists (select t from ThanhVienNhom t where t.nhom = d.nhom and t.sinhVien.taiKhoan.id = :actorId)) "
            + "or (:laGiangVien = true and (exists (select h from HuongDan h where h.deTai = d.deTai and h.giangVien.taiKhoan.id = :actorId) "
            + "or exists (select p from PhanCongCham p where p.danhGia.dangKy = d and p.giangVien.taiKhoan.id = :actorId)))) "
            + "order by d.dangKyLuc desc")
    List<DangKy> duocXemBaoCao(@Param("actorId") Integer actorId, @Param("laKhoa") boolean laKhoa,
            @Param("laSinhVien") boolean laSinhVien, @Param("laGiangVien") boolean laGiangVien);

    @Query("select d.id from DangKy d where d.id in :ids and "
            + "d.trangThai = com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy.CHAP_THUAN and "
            + "d.nhom.dot.trangThai = com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy.TRIEN_KHAI and "
            + "d.nhom.dot.hanNopBaoCao > :now and exists (select t from ThanhVienNhom t "
            + "where t.nhom = d.nhom and t.sinhVien.taiKhoan.id = :actorId and t.laNhomTruong = 1) and "
            + "not exists (select g from DanhGia g where g.dangKy = d and g.baoCaoChot is not null)")
    Set<Integer> duocNopBaoCao(@Param("ids") Collection<Integer> ids, @Param("actorId") Integer actorId,
            @Param("now") java.time.LocalDateTime now);


    @EntityGraph(attributePaths = {"nhom.dot", "deTai"})
    @Query("select d from DangKy d where d.trangThai = com.khoacntt.quanlydetai.dangky.model.TrangThaiDangKy.CHAP_THUAN "
            + "and (:dotId is null or d.dotId = :dotId) and exists (select t from ThanhVienNhom t "
            + "where t.nhom = d.nhom and t.sinhVien.id = :svId) order by d.dangKyLuc desc")
    List<DangKy> cuaSinhVienTrongDot(@Param("svId") Integer svId, @Param("dotId") Integer dotId);

    interface DotCuaSinhVien {
        Integer getId();
        String getTenDot();
    }
    @Query(value = "select d.dot_id as id, dot.ten_dot as tenDot from dang_ky d "
            + "join dot_dang_ky dot on dot.id = d.dot_id where d.trang_thai = 'CHAP_THUAN' "
            + "and exists (select 1 from thanh_vien_nhom t where t.nhom_id = d.nhom_id and t.sinh_vien_id = :svId) "
            + "group by d.dot_id, dot.ten_dot order by max(d.dang_ky_luc) desc", nativeQuery = true)
    List<DotCuaSinhVien> dotCuaSinhVien(@Param("svId") Integer svId);
}
