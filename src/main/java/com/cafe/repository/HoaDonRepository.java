package com.cafe.repository;

import com.cafe.entity.Ban;
import com.cafe.entity.HoaDon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    List<HoaDon> findByTrangThai(String trangThai);

    Optional<HoaDon> findFirstByBanAndTrangThaiOrderByMaHoaDonDesc(Ban ban, String trangThai);

    Optional<HoaDon> findFirstByBanAndTrangThaiInOrderByMaHoaDonDesc(Ban ban, Collection<String> trangThais);

    List<HoaDon> findByThoiGianTaoBetween(LocalDateTime start, LocalDateTime end);

    List<HoaDon> findAllByOrderByMaHoaDonDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT x FROM HoaDon x WHERE x.maHoaDon = :id")
    Optional<HoaDon> khoaHoaDon(@Param("id") Integer id);

    Optional<HoaDon> findByYeuCauTao(String key);

    boolean existsByMaThamChieu(String ref);
}
