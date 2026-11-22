package com.cafe.dto;

import java.time.LocalDateTime;

public class BanHienThiDTO {

    private Integer maBan;
    private String tenBan;
    private String viTri;
    private Integer soGhe = 4;
    private String trangThai;
    private Integer maHoaDonHienTai;
    private Double tongTienTamTinh = 0.0;
    private LocalDateTime thoiGianMoBan;

    public BanHienThiDTO() {
    }

    public BanHienThiDTO(Integer maBan, String tenBan, String viTri, Integer soGhe, String trangThai,
                         Integer maHoaDonHienTai, Double tongTienTamTinh, LocalDateTime thoiGianMoBan) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.viTri = viTri;
        this.soGhe = soGhe;
        this.trangThai = trangThai;
        this.maHoaDonHienTai = maHoaDonHienTai;
        this.tongTienTamTinh = tongTienTamTinh;
        this.thoiGianMoBan = thoiGianMoBan;
    }

    public Integer getMaBan() {
        return maBan;
    }

    public void setMaBan(Integer maBan) {
        this.maBan = maBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public void setTenBan(String tenBan) {
        this.tenBan = tenBan;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public Integer getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(Integer soGhe) {
        this.soGhe = soGhe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getMaHoaDonHienTai() {
        return maHoaDonHienTai;
    }

    public void setMaHoaDonHienTai(Integer maHoaDonHienTai) {
        this.maHoaDonHienTai = maHoaDonHienTai;
    }

    public Double getTongTienTamTinh() {
        return tongTienTamTinh;
    }

    public void setTongTienTamTinh(Double tongTienTamTinh) {
        this.tongTienTamTinh = tongTienTamTinh;
    }

    public LocalDateTime getThoiGianMoBan() {
        return thoiGianMoBan;
    }

    public void setThoiGianMoBan(LocalDateTime thoiGianMoBan) {
        this.thoiGianMoBan = thoiGianMoBan;
    }
}
