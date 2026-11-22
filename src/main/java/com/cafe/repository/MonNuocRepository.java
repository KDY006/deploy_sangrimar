package com.cafe.repository;

import com.cafe.entity.MonNuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonNuocRepository extends JpaRepository<MonNuoc, Integer> {
    List<MonNuoc> findByTrangThai(String trangThai);
    List<MonNuoc> findByDanhMuc(String danhMuc);
    List<MonNuoc> findByTenMonContainingIgnoreCase(String tenMon);
}
