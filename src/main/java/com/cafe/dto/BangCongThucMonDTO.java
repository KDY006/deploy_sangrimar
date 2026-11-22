package com.cafe.dto;

import java.util.ArrayList;
import java.util.List;

public class BangCongThucMonDTO {
    private Integer maMon;
    private String tenMon;
    private String danhMuc;
    private Double donGia;
    private String hinhAnh;
    private String trangThai;
    private Double giaVonLy = 0.0;
    private Double bienLoiNhuan = 0.0;
    private Double loiNhuanGopLy = 0.0;
    private Integer soLyCoTheLam = 0;
    private List<DinhMucNguyenLieuDTO> danhSachNguyenLieu = new ArrayList<>();

    public BangCongThucMonDTO() {
    }

    public BangCongThucMonDTO(Integer maMon, String tenMon, String danhMuc, Double donGia, String hinhAnh, String trangThai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.danhMuc = danhMuc;
        this.donGia = donGia;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
    }

    public Integer getMaMon() {
        return maMon;
    }

    public void setMaMon(Integer maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(String danhMuc) {
        this.danhMuc = danhMuc;
    }

    public Double getDonGia() {
        return donGia;
    }

    public void setDonGia(Double donGia) {
        this.donGia = donGia;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Double getGiaVonLy() {
        return giaVonLy;
    }

    public void setGiaVonLy(Double giaVonLy) {
        this.giaVonLy = giaVonLy;
    }

    public Double getBienLoiNhuan() {
        return bienLoiNhuan;
    }

    public void setBienLoiNhuan(Double bienLoiNhuan) {
        this.bienLoiNhuan = bienLoiNhuan;
    }

    public Double getLoiNhuanGopLy() {
        return loiNhuanGopLy;
    }

    public void setLoiNhuanGopLy(Double loiNhuanGopLy) {
        this.loiNhuanGopLy = loiNhuanGopLy;
    }

    public Integer getSoLyCoTheLam() {
        return soLyCoTheLam;
    }

    public void setSoLyCoTheLam(Integer soLyCoTheLam) {
        this.soLyCoTheLam = soLyCoTheLam;
    }

    public List<DinhMucNguyenLieuDTO> getDanhSachNguyenLieu() {
        return danhSachNguyenLieu;
    }

    public void setDanhSachNguyenLieu(List<DinhMucNguyenLieuDTO> danhSachNguyenLieu) {
        this.danhSachNguyenLieu = danhSachNguyenLieu;
    }
}
