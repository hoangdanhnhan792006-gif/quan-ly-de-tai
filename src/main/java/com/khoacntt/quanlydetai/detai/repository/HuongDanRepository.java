package com.khoacntt.quanlydetai.detai.repository;

import com.khoacntt.quanlydetai.detai.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HuongDanRepository extends JpaRepository<HuongDan, HuongDanId> {
    List<HuongDan> findByDeTaiIdOrderByGiangVienMaGvAsc(Integer deTaiId);
    boolean existsByDeTaiIdAndGiangVienTaiKhoanId(Integer deTaiId, Integer taiKhoanId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"giangVien.taiKhoan.vaiTros", "giangVien.boMon"})
    @org.springframework.data.jpa.repository.Query("select distinct h from HuongDan h join fetch h.giangVien g "
            + "join fetch g.boMon join fetch g.taiKhoan tk left join fetch tk.vaiTros "
            + "where h.deTai.id in :ids order by g.maGv")
    List<HuongDan> findByDeTaiIdInOrderByGiangVienMaGvAsc(@org.springframework.data.repository.query.Param("ids") java.util.Collection<Integer> deTaiIds);

}
