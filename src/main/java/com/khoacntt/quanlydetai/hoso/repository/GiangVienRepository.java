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

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"taiKhoan", "taiKhoan.vaiTros", "boMon"})
    @org.springframework.data.jpa.repository.Query("select distinct g from GiangVien g join fetch g.boMon "
            + "join fetch g.taiKhoan tk left join fetch tk.vaiTros where g.taiKhoan.trangThai = "
            + "com.khoacntt.quanlydetai.account.model.TrangThaiTaiKhoan.HOAT_DONG and "
            + "com.khoacntt.quanlydetai.account.model.VaiTro.GIANG_VIEN member of g.taiKhoan.vaiTros order by g.maGv")
    java.util.List<GiangVien> dangHoatDong();

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"taiKhoan.vaiTros", "boMon"})
    @org.springframework.data.jpa.repository.Query("select distinct g from GiangVien g join fetch g.boMon join fetch g.taiKhoan tk left join fetch tk.vaiTros")
    java.util.List<GiangVien> findAll(org.springframework.data.domain.Sort sort);

}
