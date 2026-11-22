package com.cafe.dto;

public class TopMonBanChayDTO {
    private Integer maMon;
    private String tenMon;
    private String danhMuc;
    private Integer tongSoLuong;
    private Double tongDoanhThu;

    public TopMonBanChayDTO() {
    }

    public TopMonBanChayDTO(Integer maMon, String tenMon, String danhMuc, Integer tongSoLuong, Double tongDoanhThu) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.danhMuc = danhMuc;
        this.tongSoLuong = tongSoLuong;
        this.tongDoanhThu = tongDoanhThu;
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

    public Integer getTongSoLuong() {
        return tongSoLuong;
    }

    public void setTongSoLuong(Integer tongSoLuong) {
        this.tongSoLuong = tongSoLuong;
    }

    public Double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(Double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }
}
