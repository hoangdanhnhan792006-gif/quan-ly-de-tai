package com.khoacntt.quanlydetai.baocao.repository;
import com.khoacntt.quanlydetai.baocao.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface BaoCaoTepRepository extends JpaRepository<BaoCaoTep, Integer> {
    List<BaoCaoTep> findByBaoCaoIdOrderByIdAsc(Integer id);

    List<BaoCaoTep> findByBaoCaoIdInOrderByIdAsc(Collection<Integer> ids);

}
