package com.cafe.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "mon_nuoc")
public class MonNuoc implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_mon")
    private Integer maMon;

    @Column(name = "ten_mon", nullable = false, length = 100)
    private String tenMon;

    @Column(name = "danh_muc", nullable = false, length = 50)
    private String danhMuc;

    @Column(name = "don_gia", nullable = false)
    private Double donGia;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "DANG_BAN";

    public MonNuoc() {
    }

    public MonNuoc(Integer maMon, String tenMon, String danhMuc, Double donGia, String hinhAnh, String trangThai) {
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
}
