package com.khoacntt.quanlydetai.hoso.repository;

import com.khoacntt.quanlydetai.hoso.model.GiangVien;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface GiangVienRepository extends JpaRepository<GiangVien, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select g from GiangVien g where g.id=:id")
    Optional<GiangVien> timVaKhoa(@org.springframework.data.repository.query.Param("id") Integer id);
    Optional<GiangVien> findByTaiKhoanId(Integer taiKhoanId);
    boolean existsByTaiKhoanId(Integer taiKhoanId);
    boolean existsByMaGv(String ma);
    boolean existsByMaGvAndIdNot(String ma, Integer id);
}
