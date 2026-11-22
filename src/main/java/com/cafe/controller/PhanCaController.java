package com.cafe.controller;

import com.cafe.dto.DoiCaDTO;
import com.cafe.dto.PhanCaDTO;
import com.cafe.dto.PhanCaTuanItemDTO;
import com.cafe.entity.PhanCa;
import com.cafe.service.NhanSuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/phan-ca")
/** Điều khiển API phân ca làm việc */
public class PhanCaController {

    private final NhanSuService nhanSuService;
    private final PhanQuyenSession access;

    public PhanCaController(NhanSuService nhanSuService, PhanQuyenSession access) {
        this.nhanSuService = nhanSuService;
        this.access = access;
    }

    private boolean isQuanLy(HttpSession session) {
        String role = access.role(session);
        return "QUAN_LY".equals(role) || "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    @PostMapping
    public ResponseEntity<PhanCa> phanCa(@RequestBody PhanCaDTO dto, HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.status(201).body(nhanSuService.phanCa(dto));
    }

    @GetMapping("/tuan")
    public List<PhanCaTuanItemDTO> lichTuan(
            @RequestParam(required = false) LocalDate ngayBatDau,
            @RequestParam(required = false) LocalDate ngay,
            HttpSession session) {
        Integer userId = access.requireDangNhap(session);
        LocalDate targetDate = ngayBatDau != null ? ngayBatDau : ngay;
        return nhanSuService.layLichTuan(userId, isQuanLy(session), targetDate);
    }

    @PutMapping("/{maPhanCa}/doi-ca")
    public PhanCa doiCa(
            @PathVariable Integer maPhanCa,
            @RequestBody DoiCaDTO dto,
            HttpSession session) {
        access.requireQuanLy(session);
        return nhanSuService.doiCa(maPhanCa, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> xoaPhanCa(@PathVariable Integer id, HttpSession session) {
        access.requireQuanLy(session);
        nhanSuService.xoaPhanCa(id);
        return ResponseEntity.noContent().build();
    }
}
