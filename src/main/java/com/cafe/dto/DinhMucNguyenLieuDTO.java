package com.cafe.dto;

public class DinhMucNguyenLieuDTO {
    private Integer maCongThuc;
    private Integer maNL;
    private String tenNL;
    private String donViTinh;
    private Double dinhMuc;
    private Double donGiaNhap;
    private Double chiPhi;
    private Double soLuongTon;
    private String ghiChu;

    public DinhMucNguyenLieuDTO() {
    }

    public DinhMucNguyenLieuDTO(Integer maCongThuc, Integer maNL, String tenNL, String donViTinh,
                                Double dinhMuc, Double donGiaNhap, Double chiPhi, Double soLuongTon, String ghiChu) {
        this.maCongThuc = maCongThuc;
        this.maNL = maNL;
        this.tenNL = tenNL;
        this.donViTinh = donViTinh;
        this.dinhMuc = dinhMuc;
        this.donGiaNhap = donGiaNhap;
        this.chiPhi = chiPhi;
        this.soLuongTon = soLuongTon;
        this.ghiChu = ghiChu;
    }

    public Integer getMaCongThuc() {
        return maCongThuc;
    }

    public void setMaCongThuc(Integer maCongThuc) {
        this.maCongThuc = maCongThuc;
    }

    public Integer getMaNL() {
        return maNL;
    }

    public void setMaNL(Integer maNL) {
        this.maNL = maNL;
    }

    public String getTenNL() {
        return tenNL;
    }

    public void setTenNL(String tenNL) {
        this.tenNL = tenNL;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public Double getDinhMuc() {
        return dinhMuc;
    }

    public void setDinhMuc(Double dinhMuc) {
        this.dinhMuc = dinhMuc;
    }

    public Double getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(Double donGiaNhap) {
        this.donGiaNhap = donGiaNhap;
    }

    public Double getChiPhi() {
        return chiPhi;
    }

    public void setChiPhi(Double chiPhi) {
        this.chiPhi = chiPhi;
    }

    public Double getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Double soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
