package com.cafe.repository;

import com.cafe.entity.NguyenLieu;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguyenLieuRepository extends JpaRepository<NguyenLieu, Integer> {

    
    @Query("SELECT nl FROM NguyenLieu nl WHERE nl.soLuongTon <= nl.mucTonToiThieu")
    List<NguyenLieu> findNguyenLieuSapHet();

    List<NguyenLieu> findAllByOrderByMaNLAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT x FROM NguyenLieu x WHERE x.maNL = :id")
    Optional<NguyenLieu> khoaNguyenLieu(@Param("id") Integer id);
}
