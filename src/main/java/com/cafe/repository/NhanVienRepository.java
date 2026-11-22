package com.cafe.repository;

import com.cafe.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);
    Optional<NhanVien> findByTenDangNhapAndMatKhau(String tenDangNhap, String matKhau);
    List<NhanVien> findByTrangThai(String trangThai);
    List<NhanVien> findByVaiTro(String vaiTro);
}
