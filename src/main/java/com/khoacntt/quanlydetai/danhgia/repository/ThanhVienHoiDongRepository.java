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
}
