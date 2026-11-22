package com.cafe.controller;

import com.cafe.dto.DonHangDTO;
import com.cafe.dto.ThanhToanDTO;
import com.cafe.entity.HoaDon;
import com.cafe.service.DonHangService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/order", "/api/don-hang"})
public class DonHangController {

    private final DonHangService donHangService;
    private final PhanQuyenSession access;

    public DonHangController(DonHangService donHangService, PhanQuyenSession access) {
        this.donHangService = donHangService;
        this.access = access;
    }

    @GetMapping("/danh-sach")
    public List<HoaDon> layDanhSach(HttpSession s) {
        access.requireDangNhap(s);
        return donHangService.layDanhSachHoaDon();
    }

    @GetMapping("/{id}")
    public HoaDon layTheoId(@PathVariable Integer id, HttpSession s) {
        access.requireDangNhap(s);
        return donHangService.layHoaDonTheoId(id);
    }

    @GetMapping("/theo-ban/{id}")
    public ResponseEntity<?> layTheoBan(@PathVariable Integer id, HttpSession s) {
        access.requireDangNhap(s);
        HoaDon h = donHangService.layDonTheoBan(id);
        return h == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(h);
    }

    @GetMapping("/theo-yeu-cau/{key}")
    public ResponseEntity<?> layTheoYeuCau(@PathVariable String key, HttpSession s) {
        access.requireDangNhap(s);
        HoaDon h = donHangService.layDonTheoYeuCau(key);
        return h == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(h);
    }

    @PostMapping("/tao-don")
    public ResponseEntity<HoaDon> taoDonHang(@RequestBody DonHangDTO dto, HttpSession s) {
        Integer nvId = access.requireDangNhap(s);
        dto.setMaNV(nvId);
        return ResponseEntity.status(HttpStatus.CREATED).body(donHangService.taoDonHang(dto));
    }

    @PutMapping("/{id}/mon")
    public HoaDon capNhatDon(@PathVariable Integer id, @RequestBody DonHangDTO dto, HttpSession s) {
        access.requireDangNhap(s);
        return donHangService.capNhatDon(id, dto);
    }

    @PostMapping("/{id}/yeu-cau-thanh-toan")
    public HoaDon yeuCauThanhToan(@PathVariable Integer id, @RequestBody Map<String, Long> body, HttpSession s) {
        access.requireDangNhap(s);
        Long phienBan = body != null ? body.get("phienBan") : null;
        return donHangService.yeuCauThanhToan(id, phienBan);
    }

    @PostMapping("/{id}/quay-lai-phuc-vu")
    public HoaDon quayLaiPhucVu(@PathVariable Integer id, @RequestBody Map<String, Long> body, HttpSession s) {
        access.requireDangNhap(s);
        Long phienBan = body != null ? body.get("phienBan") : null;
        return donHangService.quayLaiPhucVu(id, phienBan);
    }

    @PostMapping("/{id}/thanh-toan")
    public HoaDon thanhToan(@PathVariable Integer id, @RequestBody ThanhToanDTO dto, HttpSession s) {
        access.requireThuNganHoacQuanLy(s);
        String role = access.role(s);
        if (dto.getChietKhau() != null && dto.getChietKhau() > 0) {
            if (!PhanQuyenSession.QUAN_LY.equals(role) && !"MANAGER".equals(role) && !"ADMIN".equals(role)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chiết khấu chỉ dành cho Quản lý cửa hàng!");
            }
        }
        Integer cashierId = access.userId(s);
        return donHangService.thanhToanDonHang(id, dto, cashierId);
    }

    @PostMapping("/{id}/huy")
    public HoaDon huyDonHang(@PathVariable Integer id, @RequestBody Map<String, Long> body, HttpSession s) {
        access.requireThuNganHoacQuanLy(s);
        return donHangService.huyDonHang(id, body != null ? body.get("phienBan") : null);
    }
}
