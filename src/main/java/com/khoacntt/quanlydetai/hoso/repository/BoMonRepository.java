package com.khoacntt.quanlydetai.hoso.repository;

import com.khoacntt.quanlydetai.hoso.model.BoMon;

import org.springframework.data.jpa.repository.JpaRepository;
public interface BoMonRepository extends JpaRepository<BoMon, Integer> {
    boolean existsByMaBoMon(String maBoMon);
    boolean existsByMaBoMonAndIdNot(String maBoMon, Integer id);

    java.util.List<BoMon> findByDangHoatDongOrderByMaBoMonAsc(byte dangHoatDong);

}
