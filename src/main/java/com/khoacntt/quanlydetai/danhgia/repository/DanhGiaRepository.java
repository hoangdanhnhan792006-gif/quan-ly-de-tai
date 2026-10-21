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

    @EntityGraph(attributePaths = {"dangKy.nhom.dot", "dangKy.deTai", "hoiDong", "baoCaoChot"})
    @Query("select d from DanhGia d join fetch d.dangKy dk join fetch dk.nhom n join fetch n.dot "
            + "join fetch dk.deTai left join fetch d.hoiDong left join fetch d.baoCaoChot "
            + "where :laKhoa = true or (:laGiangVien = true and exists "
            + "(select p from PhanCongCham p where p.danhGia = d and p.giangVien.taiKhoan.id = :actorId)) order by d.id desc")
    List<DanhGia> duocXem(@Param("actorId") Integer actorId, @Param("laKhoa") boolean laKhoa,
            @Param("laGiangVien") boolean laGiangVien);

    // Lọc cả khoảng thời gian và người/địa điểm có khả năng xung đột tại DB.
    // Service vẫn kiểm tra chính xác loại phân công và biên thời gian như nghiệp vụ cũ.
    @Query("select d from DanhGia d where d.id <> :id and d.dangKy.nhom.dot.trangThai <> "
            + "com.khoacntt.quanlydetai.dotdangky.model.TrangThaiDotDangKy.HUY and "
            + "(d.lichPhanBienLuc = :pb or (d.hoiDongBatDau <= :pb and d.hoiDongKetThuc > :pb) "
            + "or (:batDau is not null and d.lichPhanBienLuc >= :batDau and d.lichPhanBienLuc < :ketThuc) "
            + "or (:batDau is not null and d.hoiDongBatDau < :ketThuc and d.hoiDongKetThuc > :batDau)) and "
            + "(exists (select p from PhanCongCham p where p.danhGia = d and p.giangVien.id in :giangVienIds) "
            + "or (d.dotId = :dotId and lower(trim(d.diaDiemHoiDong)) = lower(trim(:diaDiem))))")
    List<DanhGia> lichCoTheTrung(@Param("id") Integer id, @Param("dotId") Integer dotId,
            @Param("pb") LocalDateTime pb, @Param("batDau") LocalDateTime batDau,
            @Param("ketThuc") LocalDateTime ketThuc, @Param("giangVienIds") Collection<Integer> giangVienIds,
            @Param("diaDiem") String diaDiem);

}
