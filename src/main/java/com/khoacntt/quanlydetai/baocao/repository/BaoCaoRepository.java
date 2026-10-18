package com.khoacntt.quanlydetai.baocao.repository;
import com.khoacntt.quanlydetai.baocao.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface BaoCaoRepository extends JpaRepository<BaoCao, Integer> {
    List<BaoCao> findByDangKyIdOrderByPhienBanDesc(Integer id);
    Optional<BaoCao> findFirstByDangKyIdAndTrangThaiOrderByPhienBanDesc(Integer id, TrangThaiBaoCao state);
    @Query("select coalesce(max(b.phienBan),0) from BaoCao b where b.dangKy.id=:id")
    int phienBanCuoi(@Param("id") Integer id);
}
