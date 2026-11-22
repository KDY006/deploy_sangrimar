package com.cafe.controller;

import jakarta.servlet.http.HttpSession;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PhanQuyenSession {

    public static final String QUAN_LY = "QUAN_LY";
    public static final String THU_NGAN = "THU_NGAN";
    public static final String PHUC_VU = "PHUC_VU";

    public Integer userId(HttpSession session) {
        Object id = session.getAttribute("maNV");
        if (!(id instanceof Integer)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập lại");
        }
        return (Integer) id;
    }

    public String role(HttpSession session) {
        userId(session);
        return String.valueOf(session.getAttribute("vaiTro"));
    }

    public Integer requireAny(HttpSession session, String... roles) {
        String currentRole = role(session);
        Set<String> allowed = Set.of(roles);
        if (!allowed.contains(currentRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
        }
        return userId(session);
    }

    
    public Integer requireDangNhap(HttpSession session) {
        return userId(session);
    }

    
    public Integer requireThuNganHoacQuanLy(HttpSession session) {
        return requireAny(session, THU_NGAN, QUAN_LY, "MANAGER", "ADMIN");
    }

    
    public Integer requireQuanLy(HttpSession session) {
        return requireAny(session, QUAN_LY, "MANAGER", "ADMIN");
    }

    
    public Integer requireManager(HttpSession session) {
        return requireQuanLy(session);
    }

    public Integer requireAdmin(HttpSession session) {
        return requireQuanLy(session);
    }
}
