package com.khoacntt.quanlydetai.hoso.repository;

import com.khoacntt.quanlydetai.hoso.model.SinhVien;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SinhVienRepository extends JpaRepository<SinhVien, Integer> {
    Optional<SinhVien> findByTaiKhoanId(Integer taiKhoanId);
    Optional<SinhVien> findByMaSv(String maSv);
    boolean existsByTaiKhoanId(Integer taiKhoanId);
    boolean existsByMaSv(String ma);
    boolean existsByMaSvAndIdNot(String ma, Integer id);
}
