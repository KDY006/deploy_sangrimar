package com.cafe.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "chi_tiet_hoa_don")
public class ChiTietHoaDon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cthd")
    private Integer maCTHD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoa_don", nullable = false)
    @JsonBackReference
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_mon", nullable = false)
    private MonNuoc monNuoc;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong = 1;

    @Column(name = "so_luong_da_in_bar", nullable = false)
    private Integer soLuongDaInBar = 0;

    @Column(name = "don_gia_ban", nullable = false)
    private Double donGiaBan;

    @Column(name = "thanh_tien", nullable = false)
    private Double thanhTien;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(Integer maCTHD, HoaDon hoaDon, MonNuoc monNuoc, Integer soLuong, Double donGiaBan, Double thanhTien, String ghiChu) {
        this.maCTHD = maCTHD;
        this.hoaDon = hoaDon;
        this.monNuoc = monNuoc;
        this.soLuong = soLuong;
        this.soLuongDaInBar = soLuong;
        this.donGiaBan = donGiaBan;
        this.thanhTien = thanhTien;
        this.ghiChu = ghiChu;
    }

    public ChiTietHoaDon(Integer maCTHD, HoaDon hoaDon, MonNuoc monNuoc, Integer soLuong, Integer soLuongDaInBar, Double donGiaBan, Double thanhTien, String ghiChu) {
        this.maCTHD = maCTHD;
        this.hoaDon = hoaDon;
        this.monNuoc = monNuoc;
        this.soLuong = soLuong;
        this.soLuongDaInBar = soLuongDaInBar;
        this.donGiaBan = donGiaBan;
        this.thanhTien = thanhTien;
        this.ghiChu = ghiChu;
    }

    public Integer getMaCTHD() {
        return maCTHD;
    }

    public void setMaCTHD(Integer maCTHD) {
        this.maCTHD = maCTHD;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public MonNuoc getMonNuoc() {
        return monNuoc;
    }

    public void setMonNuoc(MonNuoc monNuoc) {
        this.monNuoc = monNuoc;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getSoLuongDaInBar() {
        return soLuongDaInBar;
    }

    public void setSoLuongDaInBar(Integer soLuongDaInBar) {
        this.soLuongDaInBar = soLuongDaInBar;
    }

    public Double getDonGiaBan() {
        return donGiaBan;
    }

    public void setDonGiaBan(Double donGiaBan) {
        this.donGiaBan = donGiaBan;
    }

    public Double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
