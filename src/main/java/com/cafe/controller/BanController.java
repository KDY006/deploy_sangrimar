package com.cafe.controller;

import com.cafe.dto.BanHienThiDTO;
import com.cafe.service.DonHangService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ban")
public class BanController {

    private final DonHangService donHangService;
    private final PhanQuyenSession access;

    public BanController(DonHangService donHangService, PhanQuyenSession access) {
        this.donHangService = donHangService;
        this.access = access;
    }

    @GetMapping
    public ResponseEntity<List<BanHienThiDTO>> layDanhSachBan(HttpSession session) {
        access.requireDangNhap(session);
        return ResponseEntity.ok(donHangService.layDanhSachBanHienThi());
    }

    @GetMapping("/so-do")
    public ResponseEntity<List<BanHienThiDTO>> laySoDoBan(HttpSession session) {
        access.requireDangNhap(session);
        return ResponseEntity.ok(donHangService.layDanhSachBanHienThi());
    }
}
