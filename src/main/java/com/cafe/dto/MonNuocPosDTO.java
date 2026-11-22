package com.cafe.dto;

public class MonNuocPosDTO {
    private Integer maMon;
    private String tenMon;
    private String danhMuc;
    private Double donGia;
    private String hinhAnh;
    private String trangThai;
    private Integer soLyCoTheLam;

    public MonNuocPosDTO() {
    }

    public MonNuocPosDTO(Integer maMon, String tenMon, String danhMuc, Double donGia, String hinhAnh, String trangThai, Integer soLyCoTheLam) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.danhMuc = danhMuc;
        this.donGia = donGia;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.soLyCoTheLam = soLyCoTheLam;
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

    public Integer getSoLyCoTheLam() {
        return soLyCoTheLam;
    }

    public void setSoLyCoTheLam(Integer soLyCoTheLam) {
        this.soLyCoTheLam = soLyCoTheLam;
    }
}
