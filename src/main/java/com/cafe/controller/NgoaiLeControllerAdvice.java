package com.cafe.controller;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class NgoaiLeControllerAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException e) {
        String message = e.getReason() == null ? "Không thực hiện được" : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", message));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
    public ResponseEntity<?> handleConflict(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Dữ liệu vừa thay đổi hoặc mã yêu cầu/tham chiếu đã dùng. Tải lại đơn để kiểm tra trước khi thử lại."
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        String message = e.getMessage() == null ? "Không thực hiện được yêu cầu" : e.getMessage();
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
