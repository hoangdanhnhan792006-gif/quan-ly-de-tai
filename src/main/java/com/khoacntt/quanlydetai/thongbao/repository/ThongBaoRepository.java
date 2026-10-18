package com.khoacntt.quanlydetai.thongbao.repository;
import com.khoacntt.quanlydetai.thongbao.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    List<ThongBao> findAllByOrderByIdDesc();
    List<ThongBao> findByTrangThaiOrderByCongBoLucDesc(TrangThaiThongBao state);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ThongBao t where t.id=:id")
    Optional<ThongBao> timVaKhoa(@Param("id") Integer id);
}
