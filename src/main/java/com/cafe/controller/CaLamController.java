package com.cafe.controller;

import com.cafe.entity.CaLam;
import com.cafe.service.NhanSuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ca-lam")
public class CaLamController {

    private final NhanSuService nhanSuService;
    private final PhanQuyenSession access;

    public CaLamController(NhanSuService nhanSuService, PhanQuyenSession access) {
        this.nhanSuService = nhanSuService;
        this.access = access;
    }

    @GetMapping
    public List<CaLam> layDanhSach(HttpSession session) {
        access.userId(session);
        return nhanSuService.layDanhSachCaLam();
    }
}
