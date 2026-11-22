package com.cafe.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;

@Entity
@Table(name = "ca_lam")
public class CaLam implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ca")
    private Integer maCa;

    @Column(name = "ten_ca", nullable = false, length = 50)
    private String tenCa;

    @Column(name = "gio_bat_dau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "he_so_luong", nullable = false)
    private Double heSoLuong = 1.0;

    public CaLam() {
    }

    public CaLam(Integer maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc, Double heSoLuong) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.heSoLuong = heSoLuong;
    }

    public Integer getMaCa() {
        return maCa;
    }

    public void setMaCa(Integer maCa) {
        this.maCa = maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public Double getHeSoLuong() {
        return heSoLuong;
    }

    public void setHeSoLuong(Double heSoLuong) {
        this.heSoLuong = heSoLuong;
    }
}
