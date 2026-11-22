package com.cafe.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "nguyen_lieu")
public class NguyenLieu implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nl")
    private Integer maNL;

    @Column(name = "ten_nl", nullable = false, length = 100)
    private String tenNL;

    @Column(name = "don_vi_tinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "so_luong_ton", nullable = false)
    private Double soLuongTon = 0.0;

    @Column(name = "muc_ton_toi_thieu", nullable = false)
    private Double mucTonToiThieu = 100.0;

    @Column(name = "don_gia_nhap", nullable = false)
    private Double donGiaNhap = 0.0;

    public NguyenLieu() {
    }

    public NguyenLieu(Integer maNL, String tenNL, String donViTinh, Double soLuongTon, Double mucTonToiThieu, Double donGiaNhap) {
        this.maNL = maNL;
        this.tenNL = tenNL;
        this.donViTinh = donViTinh;
        this.soLuongTon = soLuongTon;
        this.mucTonToiThieu = mucTonToiThieu;
        this.donGiaNhap = donGiaNhap;
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

    public Double getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Double soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public Double getMucTonToiThieu() {
        return mucTonToiThieu;
    }

    public void setMucTonToiThieu(Double mucTonToiThieu) {
        this.mucTonToiThieu = mucTonToiThieu;
    }

    public Double getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(Double donGiaNhap) {
        this.donGiaNhap = donGiaNhap;
    }

    public boolean isSapHet() {
        return this.soLuongTon <= this.mucTonToiThieu;
    }

    public Double getGiaTriTon() {
        return (this.soLuongTon != null && this.donGiaNhap != null) ? this.soLuongTon * this.donGiaNhap : 0.0;
    }
}
