package com.cafe.dto;

public class ChiTietDonHangDTO {

    private Integer maMon;
    private Integer soLuong;
    private String ghiChu;

    public ChiTietDonHangDTO() {
    }

    public ChiTietDonHangDTO(Integer maMon, Integer soLuong, String ghiChu) {
        this.maMon = maMon;
        this.soLuong = soLuong;
        this.ghiChu = ghiChu;
    }

    public Integer getMaMon() {
        return maMon;
    }

    public void setMaMon(Integer maMon) {
        this.maMon = maMon;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
