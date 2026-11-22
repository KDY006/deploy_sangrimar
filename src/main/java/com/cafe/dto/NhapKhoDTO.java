package com.cafe.dto;

public class NhapKhoDTO {
    private Integer maNL;
    private Double soLuongNhap;
    private Double donGiaNhapMoi;
    private String nhaCungCap;
    private String ghiChu;

    public NhapKhoDTO() {
    }

    public NhapKhoDTO(Integer maNL, Double soLuongNhap) {
        this.maNL = maNL;
        this.soLuongNhap = soLuongNhap;
    }

    public NhapKhoDTO(Integer maNL, Double soLuongNhap, Double donGiaNhapMoi, String nhaCungCap, String ghiChu) {
        this.maNL = maNL;
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhapMoi = donGiaNhapMoi;
        this.nhaCungCap = nhaCungCap;
        this.ghiChu = ghiChu;
    }

    public Integer getMaNL() {
        return maNL;
    }

    public void setMaNL(Integer maNL) {
        this.maNL = maNL;
    }

    public Double getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(Double soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    public Double getDonGiaNhapMoi() {
        return donGiaNhapMoi;
    }

    public void setDonGiaNhapMoi(Double donGiaNhapMoi) {
        this.donGiaNhapMoi = donGiaNhapMoi;
    }

    public String getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(String nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
