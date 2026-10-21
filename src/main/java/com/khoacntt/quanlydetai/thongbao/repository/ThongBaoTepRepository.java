package com.khoacntt.quanlydetai.thongbao.repository;
import com.khoacntt.quanlydetai.thongbao.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface ThongBaoTepRepository extends JpaRepository<ThongBaoTep, Integer> {
    List<ThongBaoTep> findByThongBaoIdOrderByIdAsc(Integer id);

    List<ThongBaoTep> findByThongBaoIdInOrderByIdAsc(Collection<Integer> ids);

}
