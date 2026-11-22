package com.cafe.repository;

import com.cafe.entity.ChiTietHoaDon;
import com.cafe.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    List<ChiTietHoaDon> findByHoaDon(HoaDon hoaDon);
}
