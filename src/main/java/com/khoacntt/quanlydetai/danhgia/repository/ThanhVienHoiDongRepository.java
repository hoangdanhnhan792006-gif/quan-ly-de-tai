package com.khoacntt.quanlydetai.danhgia.repository;
import com.khoacntt.quanlydetai.danhgia.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface ThanhVienHoiDongRepository extends JpaRepository<ThanhVienHoiDong, ThanhVienHoiDongId> {
    List<ThanhVienHoiDong> findByHoiDongIdOrderByGiangVienMaGvAsc(Integer id);
    boolean existsByHoiDongIdAndGiangVienTaiKhoanIdAndVaiTro(Integer id, Integer taiKhoanId, VaiTroHoiDong role);

    @EntityGraph(attributePaths = "giangVien.taiKhoan.vaiTros")
    @Query("select distinct t from ThanhVienHoiDong t join fetch t.giangVien g "
            + "join fetch g.taiKhoan tk left join fetch tk.vaiTros where t.hoiDong.id in :ids order by g.maGv")
    List<ThanhVienHoiDong> findByHoiDongIdInOrderByGiangVienMaGvAsc(@Param("ids") Collection<Integer> ids);

    @Query("select t.hoiDong.id from ThanhVienHoiDong t where t.hoiDong.id in :ids "
            + "and t.giangVien.taiKhoan.id = :actorId and t.vaiTro = com.khoacntt.quanlydetai.danhgia.model.VaiTroHoiDong.CHU_TICH")
    Set<Integer> hoiDongChuTich(@Param("ids") Collection<Integer> ids, @Param("actorId") Integer actorId);

}
