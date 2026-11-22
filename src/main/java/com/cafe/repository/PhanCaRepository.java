package com.cafe.repository;

import com.cafe.entity.PhanCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhanCaRepository extends JpaRepository<PhanCa, Integer> {
    List<PhanCa> findByNgayLam(LocalDate ngayLam);
    List<PhanCa> findByNhanVien_MaNVAndNgayLam(Integer maNV, LocalDate ngayLam);
    List<PhanCa> findByNgayLamBetweenOrderByNgayLamAsc(LocalDate tuNgay, LocalDate denNgay);
    List<PhanCa> findByNhanVien_MaNVAndNgayLamBetweenOrderByNgayLamAsc(Integer maNV, LocalDate tuNgay, LocalDate denNgay);
    Optional<PhanCa> findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(Integer maNV, Integer maCa, LocalDate ngayLam);
    List<PhanCa> findByNgayLamLessThanEqualOrderByNgayLamAsc(LocalDate ngayLam);
}
