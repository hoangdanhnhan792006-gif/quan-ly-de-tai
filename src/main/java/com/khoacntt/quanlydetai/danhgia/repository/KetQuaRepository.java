package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface KetQuaRepository extends JpaRepository<KetQua, Integer> {
    List<KetQua> findByDanhGiaIdOrderByPhienBanDesc(Integer id);
    Optional<KetQua> findFirstByDanhGiaIdAndTrangThaiOrderByPhienBanDesc(Integer id, TrangThaiKetQua state);
    List<KetQua> findByDanhGiaIdAndTrangThaiIn(Integer id, Collection<TrangThaiKetQua> states);
    @Query("select coalesce(max(k.phienBan),0) from KetQua k where k.danhGia.id=:id")
    int phienBanCuoi(@Param("id") Integer id);
    @Query("select k.danhGia.id from KetQua k where k.id=:id")
    Optional<Integer> timDanhGiaId(@Param("id") Integer id);

    @EntityGraph(attributePaths = "danhGia.dangKy")
    @Query("select k from KetQua k where k.danhGia.dangKy.id in :ids and "
            + "k.trangThai = com.khoacntt.quanlydetai.danhgia.model.TrangThaiKetQua.DA_CONG_BO and not exists "
            + "(select newer from KetQua newer where newer.danhGia = k.danhGia and newer.trangThai = k.trangThai "
            + "and newer.phienBan > k.phienBan)")
    List<KetQua> congBoMoiNhat(@Param("ids") Collection<Integer> dangKyIds);

}
