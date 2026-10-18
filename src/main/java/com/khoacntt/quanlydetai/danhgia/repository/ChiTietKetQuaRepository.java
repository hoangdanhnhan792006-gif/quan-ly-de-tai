package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface ChiTietKetQuaRepository extends JpaRepository<ChiTietKetQua, ChiTietKetQuaId> {
    List<ChiTietKetQua> findByKetQuaIdOrderByPhanCongChamIdAsc(Integer id);
}
