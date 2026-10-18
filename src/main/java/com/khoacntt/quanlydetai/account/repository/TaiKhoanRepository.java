package com.khoacntt.quanlydetai.account.repository;

import com.khoacntt.quanlydetai.account.model.TaiKhoan;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {

    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TaiKhoan t where t.id = :id")
    Optional<TaiKhoan> timVaKhoa(@Param("id") Integer id);
}
