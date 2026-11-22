package com.cafe.repository;

import com.cafe.entity.CongThuc;
import com.cafe.entity.MonNuoc;
import com.cafe.entity.NguyenLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CongThucRepository extends JpaRepository<CongThuc, Integer> {
    List<CongThuc> findByMonNuoc(MonNuoc monNuoc);
    Optional<CongThuc> findByMonNuocAndNguyenLieu(MonNuoc monNuoc, NguyenLieu nguyenLieu);
    void deleteByMonNuoc(MonNuoc monNuoc);
}
