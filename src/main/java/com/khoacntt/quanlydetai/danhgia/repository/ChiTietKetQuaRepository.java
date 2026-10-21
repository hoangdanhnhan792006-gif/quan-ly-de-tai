package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface ChiTietKetQuaRepository extends JpaRepository<ChiTietKetQua, ChiTietKetQuaId> {
    List<ChiTietKetQua> findByKetQuaIdOrderByPhanCongChamIdAsc(Integer id);

    @EntityGraph(attributePaths = {"phanCongCham.giangVien.taiKhoan.vaiTros", "phieuCham"})
    @Query("select distinct c from ChiTietKetQua c join fetch c.phieuCham join fetch c.phanCongCham p "
            + "join fetch p.giangVien g join fetch g.taiKhoan tk left join fetch tk.vaiTros "
            + "where c.ketQua.id in :ids order by p.id")
    List<ChiTietKetQua> findByKetQuaIdInOrderByPhanCongChamIdAsc(@Param("ids") Collection<Integer> ids);

}
