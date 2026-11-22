package com.cafe.repository;

import com.cafe.entity.CaLam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaLamRepository extends JpaRepository<CaLam, Integer> {
    Optional<CaLam> findByTenCa(String tenCa);
    List<CaLam> findAllByOrderByGioBatDauAsc();
}
