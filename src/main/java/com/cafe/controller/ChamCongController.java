package com.cafe.controller;

import com.cafe.dto.BangCongNhanVienDTO;
import com.cafe.dto.CaHomNayDTO;
import com.cafe.dto.ChamCongDTO;
import com.cafe.dto.ChotCaResponseDTO;
import com.cafe.dto.DieuChinhChamCongDTO;
import com.cafe.entity.ChamCong;
import com.cafe.service.NhanSuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cham-cong")
/** Điều khiển API chấm công và bảng công */
public class ChamCongController {

    private final NhanSuService nhanSuService;
    private final PhanQuyenSession access;

    public ChamCongController(NhanSuService nhanSuService, PhanQuyenSession access) {
        this.nhanSuService = nhanSuService;
        this.access = access;
    }

    private boolean isQuanLy(HttpSession session) {
        String role = access.role(session);
        return "QUAN_LY".equals(role) || "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    @GetMapping("/ca-hom-nay")
    public List<CaHomNayDTO> layCaHomNay(HttpSession session) {
        Integer userId = access.requireDangNhap(session);
        return nhanSuService.layCaHomNay(userId);
    }

    @PostMapping("/check-in")
    public ChamCong checkIn(@RequestBody ChamCongDTO dto, HttpSession session) {
        Integer userId = access.requireDangNhap(session);
        return nhanSuService.checkIn(userId, dto);
    }

    @PostMapping("/check-out/{id}")
    public ChamCong checkOut(@PathVariable Integer id, HttpSession session) {
        Integer userId = access.requireDangNhap(session);
        return nhanSuService.checkOut(userId, isQuanLy(session), id);
    }

    @GetMapping("/lich-su")
    public List<ChamCong> layLichSuChamCong(
            @RequestParam(required = false) LocalDate tuNgay,
            @RequestParam(required = false) LocalDate denNgay,
            @RequestParam(required = false) Integer maNV,
            @RequestParam(required = false) String trangThai,
            HttpSession session) {
        Integer userId = access.requireDangNhap(session);
        return nhanSuService.layLichSuChamCong(userId, isQuanLy(session), tuNgay, denNgay, maNV, trangThai);
    }

    @PostMapping("/chot-ca")
    public ChotCaResponseDTO chotCa(HttpSession session) {
        access.requireQuanLy(session);
        return nhanSuService.chotCaQuaHan();
    }

    @PutMapping("/{id}/dieu-chinh")
    public ChamCong dieuChinhChamCong(
            @PathVariable Integer id,
            @RequestBody DieuChinhChamCongDTO dto,
            HttpSession session) {
        Integer userId = access.requireQuanLy(session);
        return nhanSuService.dieuChinhChamCong(id, userId, dto);
    }

    @GetMapping("/bang-cong")
    public List<BangCongNhanVienDTO> layBangCong(
            @RequestParam(required = false) LocalDate tuNgay,
            @RequestParam(required = false) LocalDate denNgay,
            HttpSession session) {
        Integer userId = access.requireDangNhap(session);
        return nhanSuService.layBangCong(userId, isQuanLy(session), tuNgay, denNgay);
    }
}
