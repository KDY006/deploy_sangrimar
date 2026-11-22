package com.cafe.repository;

import com.cafe.entity.ChamCong;
import com.cafe.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChamCongRepository extends JpaRepository<ChamCong, Integer> {
    Optional<ChamCong> findByPhanCa_MaPhanCa(Integer maPhanCa);
    Optional<ChamCong> findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(Integer maNV, Integer maCa, LocalDate ngayLam);
    List<ChamCong> findByNgayLam(LocalDate ngayLam);
    List<ChamCong> findByNhanVienAndNgayLam(NhanVien nhanVien, LocalDate ngayLam);
    List<ChamCong> findByNhanVienOrderByNgayLamDesc(NhanVien nhanVien);
    List<ChamCong> findByNhanVien_MaNVAndNgayLam(Integer maNV, LocalDate ngayLam);
    List<ChamCong> findByNgayLamBetweenOrderByNgayLamDescGioCheckInDesc(LocalDate tuNgay, LocalDate denNgay);
    List<ChamCong> findByNhanVien_MaNVAndNgayLamBetweenOrderByNgayLamDescGioCheckInDesc(Integer maNV, LocalDate tuNgay, LocalDate denNgay);
}
