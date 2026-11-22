package com.cafe.controller;

import com.cafe.entity.CaLam;
import com.cafe.entity.NhanVien;
import com.cafe.service.NhanSuService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nhan-su")
public class NhanSuController {

    private final NhanSuService nhanSuService;
    private final PhanQuyenSession access;

    public NhanSuController(NhanSuService nhanSuService, PhanQuyenSession access) {
        this.nhanSuService = nhanSuService;
        this.access = access;
    }

    @GetMapping("/nhan-vien")
    public List<NhanVien> layDanhSachNhanVien(HttpSession session) {
        access.requireQuanLy(session);
        return nhanSuService.layDanhSachNhanVien();
    }

    @PostMapping("/nhan-vien")
    public ResponseEntity<NhanVien> themNhanVien(@RequestBody NhanVien nv, HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.status(201).body(nhanSuService.themNhanVien(nv));
    }

    @PutMapping("/nhan-vien/{id}")
    public NhanVien capNhatNhanVien(@PathVariable Integer id, @RequestBody NhanVien nv, HttpSession session) {
        Integer caller = access.requireQuanLy(session);
        return nhanSuService.capNhatNhanVien(id, nv, caller);
    }

    @DeleteMapping("/nhan-vien/{id}")
    public NhanVien ngungLamViec(@PathVariable Integer id, HttpSession session) {
        Integer caller = access.requireQuanLy(session);
        return nhanSuService.ngungLamViec(id, caller);
    }

    @GetMapping("/ca-lam")
    public List<CaLam> layDanhSachCaLam(HttpSession session) {
        access.requireDangNhap(session);
        return nhanSuService.layDanhSachCaLam();
    }
}
