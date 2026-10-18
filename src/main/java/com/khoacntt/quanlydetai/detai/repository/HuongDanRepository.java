package com.khoacntt.quanlydetai.detai.repository;

import com.khoacntt.quanlydetai.detai.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HuongDanRepository extends JpaRepository<HuongDan, HuongDanId> {
    List<HuongDan> findByDeTaiIdOrderByGiangVienMaGvAsc(Integer deTaiId);
    boolean existsByDeTaiIdAndGiangVienTaiKhoanId(Integer deTaiId, Integer taiKhoanId);
}
