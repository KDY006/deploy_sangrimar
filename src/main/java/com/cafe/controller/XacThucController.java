package com.cafe.controller;

import com.cafe.dto.DangNhapDTO;
import com.cafe.entity.NhanVien;
import com.cafe.service.NhanSuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/api/xac-thuc"})
public class XacThucController {

    private final NhanSuService nhanSuService;

    public XacThucController(NhanSuService nhanSuService) {
        this.nhanSuService = nhanSuService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DangNhapDTO dto, HttpSession session) {
        
        Long khoaDen = (Long) session.getAttribute("khoaDangNhapDen");
        if (khoaDen != null) {
            long conLaiMs = khoaDen - System.currentTimeMillis();
            if (conLaiMs > 0) {
                long conLaiGiay = (conLaiMs / 1000) + 1;
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Đăng nhập sai quá 5 lần liên tiếp. Vui lòng thử lại sau " + conLaiGiay + " giây."));
            } else {
                session.removeAttribute("khoaDangNhapDen");
                session.removeAttribute("soLanDangNhapSai");
            }
        }

        try {
            NhanVien nv = nhanSuService.dangNhap(dto);
            if (!"DANG_LAM".equals(nv.getTrangThai())) {
                throw new RuntimeException("Tài khoản đã ngừng làm việc");
            }

            
            session.removeAttribute("soLanDangNhapSai");
            session.removeAttribute("khoaDangNhapDen");

            session.setAttribute("maNV", nv.getMaNV());
            session.setAttribute("vaiTro", nv.getVaiTro());
            session.setAttribute("hoTen", nv.getHoTen());
            session.setAttribute("tenDangNhap", nv.getTenDangNhap());

            return ResponseEntity.ok(nv);
        } catch (Exception e) {
            Integer soLanSai = (Integer) session.getAttribute("soLanDangNhapSai");
            if (soLanSai == null) {
                soLanSai = 0;
            }
            soLanSai++;

            if (soLanSai >= 5) {
                session.setAttribute("khoaDangNhapDen", System.currentTimeMillis() + 60_000L); 
                session.removeAttribute("soLanDangNhapSai");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Đăng nhập sai 5 lần liên tiếp. Tài khoản bị tạm khóa trong 60 giây."));
            } else {
                session.setAttribute("soLanDangNhapSai", soLanSai);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Sai tên đăng nhập hoặc mật khẩu!"));
            }
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object id = session.getAttribute("maNV");
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập lại"));
        }
        return ResponseEntity.ok(Map.of(
                "maNV", id,
                "vaiTro", session.getAttribute("vaiTro"),
                "hoTen", session.getAttribute("hoTen") != null ? session.getAttribute("hoTen") : "",
                "tenDangNhap", session.getAttribute("tenDangNhap") != null ? session.getAttribute("tenDangNhap") : ""
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Đã đăng xuất"));
    }
}
