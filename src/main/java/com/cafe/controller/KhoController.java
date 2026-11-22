package com.cafe.controller;

import com.cafe.dto.BangCongThucMonDTO;
import com.cafe.dto.CongThucDTO;
import com.cafe.dto.DashboardKpiDTO;
import com.cafe.dto.KiemKeKhoDTO;
import com.cafe.dto.NhapKhoDTO;
import com.cafe.entity.CongThuc;
import com.cafe.entity.LichSuKho;
import com.cafe.entity.NguyenLieu;
import com.cafe.service.KhoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kho")
public class KhoController {

    private final KhoService khoService;
    private final PhanQuyenSession access;

    public KhoController(KhoService khoService, PhanQuyenSession access) {
        this.khoService = khoService;
        this.access = access;
    }

    @GetMapping("/nguyen-lieu")
    public ResponseEntity<List<NguyenLieu>> layDanhSachNguyenLieu(HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.ok(khoService.layTatCaNguyenLieu());
    }

    @GetMapping("/sap-het")
    public ResponseEntity<List<NguyenLieu>> layDanhSachSapHet(HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.ok(khoService.layDanhSachSapHet());
    }

    @GetMapping("/bang-cong-thuc")
    public ResponseEntity<List<BangCongThucMonDTO>> layBangCongThuc(HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.ok(khoService.layBangCongThuc());
    }

    @PostMapping("/nhap-kho")
    public ResponseEntity<NguyenLieu> nhapKho(@RequestBody NhapKhoDTO dto, HttpSession session) {
        Integer maNV = access.requireQuanLy(session);
        NguyenLieu nl = khoService.nhapKho(dto, maNV);
        return ResponseEntity.ok(nl);
    }

    @PostMapping("/kiem-ke")
    public ResponseEntity<NguyenLieu> kiemKeKho(@RequestBody KiemKeKhoDTO dto, HttpSession session) {
        Integer maNV = access.requireQuanLy(session);
        NguyenLieu nl = khoService.kiemKeKho(dto, maNV);
        return ResponseEntity.ok(nl);
    }

    @PostMapping("/cong-thuc")
    public ResponseEntity<CongThuc> thietLapCongThuc(@RequestBody CongThucDTO dto, HttpSession session) {
        access.requireQuanLy(session);
        CongThuc ct = khoService.thietLapCongThuc(dto);
        return ResponseEntity.ok(ct);
    }

    @GetMapping("/cong-thuc/{maMon}")
    public ResponseEntity<List<CongThuc>> layCongThucTheoMon(@PathVariable Integer maMon, HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.ok(khoService.layCongThucTheoMon(maMon));
    }

    @GetMapping("/lich-su")
    public ResponseEntity<List<LichSuKho>> layLichSuKho(@RequestParam(required = false) String loaiGD,
                                                        @RequestParam(required = false) Integer maNL,
                                                        @RequestParam(required = false, defaultValue = "HOM_NAY") String phamVi,
                                                        HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.ok(khoService.layLichSuKho(loaiGD, maNL, phamVi));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardKpiDTO> getDashboardKPI(@RequestParam(required = false, defaultValue = "HOM_NAY") String phamVi,
                                                          HttpSession session) {
        access.requireQuanLy(session);
        return ResponseEntity.ok(khoService.getDashboardKPIDTO(phamVi));
    }
}
