package com.khoacntt.quanlydetai.thongbao.repository;
import com.khoacntt.quanlydetai.thongbao.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    List<ThongBao> findAllByOrderByIdDesc();
    @EntityGraph(attributePaths = "nguoiDang.vaiTros")
    @Query("select distinct t from ThongBao t join fetch t.nguoiDang tk left join fetch tk.vaiTros "
            + "where t.trangThai = :state order by t.congBoLuc desc")
    List<ThongBao> findByTrangThaiOrderByCongBoLucDesc(@Param("state") TrangThaiThongBao state);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ThongBao t where t.id=:id")
    Optional<ThongBao> timVaKhoa(@Param("id") Integer id);

    @EntityGraph(attributePaths = "nguoiDang.vaiTros")
    @Query("select distinct t from ThongBao t join fetch t.nguoiDang tk left join fetch tk.vaiTros "
            + "where :laAdmin = true or t.nguoiDang.id = :actorId order by t.id desc")
    List<ThongBao> quanLy(@Param("laAdmin") boolean laAdmin, @Param("actorId") Integer actorId);

}
