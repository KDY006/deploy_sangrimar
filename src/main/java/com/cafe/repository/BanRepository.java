package com.cafe.repository;

import com.cafe.entity.Ban;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BanRepository extends JpaRepository<Ban, Integer> {

    List<Ban> findByTrangThai(String trangThai);

    List<Ban> findAllByOrderByMaBanAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT x FROM Ban x WHERE x.maBan = :id")
    Optional<Ban> khoaBan(@Param("id") Integer id);
}
