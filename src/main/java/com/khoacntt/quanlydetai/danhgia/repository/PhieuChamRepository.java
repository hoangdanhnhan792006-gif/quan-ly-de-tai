package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface PhieuChamRepository extends JpaRepository<PhieuCham, Integer> {
    List<PhieuCham> findByPhanCongChamIdOrderByPhienBanDesc(Integer id);
    Optional<PhieuCham> findFirstByPhanCongChamIdAndTrangThaiOrderByPhienBanDesc(Integer id, TrangThaiPhieuCham state);
    Optional<PhieuCham> findByYeuCauSuaDiemId(Integer id);
    boolean existsByPhanCongChamDanhGiaId(Integer id);
    @Query("select coalesce(max(p.phienBan),0) from PhieuCham p where p.phanCongCham.id=:id")
    int phienBanCuoi(@Param("id") Integer id);
}
