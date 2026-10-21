package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface PhanCongChamRepository extends JpaRepository<PhanCongCham, Integer> {
    @Query("select distinct p from PhanCongCham p join fetch p.giangVien g join fetch g.taiKhoan tk "
            + "left join fetch tk.vaiTros where p.danhGia.id = :id order by p.id")
    List<PhanCongCham> findByDanhGiaIdOrderByIdAsc(@Param("id") Integer id);
    List<PhanCongCham> findByGiangVienTaiKhoanIdOrderByIdDesc(Integer id);
    boolean existsByDanhGiaDangKyIdAndGiangVienTaiKhoanId(Integer dkId, Integer actorId);
    @Query("select p.danhGia.id from PhanCongCham p where p.id=:id")
    Optional<Integer> timDanhGiaId(@Param("id") Integer id);

    List<PhanCongCham> findByDanhGiaIdInOrderByIdAsc(Collection<Integer> ids);

}
