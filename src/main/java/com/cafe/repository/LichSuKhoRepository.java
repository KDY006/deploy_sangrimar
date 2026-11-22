package com.cafe.repository;

import com.cafe.entity.LichSuKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LichSuKhoRepository extends JpaRepository<LichSuKho, Integer> {

    @Query("SELECT l FROM LichSuKho l ORDER BY l.thoiGian DESC, l.maGD DESC")
    List<LichSuKho> findAllSapXepMoiNhat();

    @Query("SELECT l FROM LichSuKho l WHERE (:loaiGD IS NULL OR :loaiGD = '' OR l.loaiGD = :loaiGD) " +
           "AND (:maNL IS NULL OR l.nguyenLieu.maNL = :maNL) " +
           "AND (:tuNgay IS NULL OR l.thoiGian >= :tuNgay) " +
           "ORDER BY l.thoiGian DESC, l.maGD DESC")
    List<LichSuKho> traCuuLichSu(@Param("loaiGD") String loaiGD,
                                @Param("maNL") Integer maNL,
                                @Param("tuNgay") LocalDateTime tuNgay);

    List<LichSuKho> findByThoiGianGreaterThanEqual(LocalDateTime tuNgay);

    List<LichSuKho> findByThoiGianBetween(LocalDateTime start, LocalDateTime end);
}
