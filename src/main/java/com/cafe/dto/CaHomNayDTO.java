package com.cafe.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class CaHomNayDTO {

    private Integer maPhanCa;
    private Integer maCa;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private Double heSoLuong;
    private String trangThai;
    private LocalDateTime gioCheckIn;
    private LocalDateTime gioCheckOut;
    private Integer soPhutTre;
    private Integer soPhutVeSom;
    private Double soGioLam;
    private Double gioCongQuyDoi;
    private boolean coTheCheckIn;
    private boolean coTheCheckOut;
    private String thongBaoCuaSo;
    private Integer maChamCong;
    private String ghiChu;

    public CaHomNayDTO() {
    }

    public Integer getMaPhanCa() {
        return maPhanCa;
    }

    public void setMaPhanCa(Integer maPhanCa) {
        this.maPhanCa = maPhanCa;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getGioCheckIn() {
        return gioCheckIn;
    }

    public void setGioCheckIn(LocalDateTime gioCheckIn) {
        this.gioCheckIn = gioCheckIn;
    }

    public LocalDateTime getGioCheckOut() {
        return gioCheckOut;
    }

    public void setGioCheckOut(LocalDateTime gioCheckOut) {
        this.gioCheckOut = gioCheckOut;
    }

    public Integer getSoPhutTre() {
        return soPhutTre;
    }

    public void setSoPhutTre(Integer soPhutTre) {
        this.soPhutTre = soPhutTre;
    }

    public Integer getSoPhutVeSom() {
        return soPhutVeSom;
    }

    public void setSoPhutVeSom(Integer soPhutVeSom) {
        this.soPhutVeSom = soPhutVeSom;
    }

    public Double getSoGioLam() {
        return soGioLam;
    }

    public void setSoGioLam(Double soGioLam) {
        this.soGioLam = soGioLam;
    }

    public Double getGioCongQuyDoi() {
        return gioCongQuyDoi;
    }

    public void setGioCongQuyDoi(Double gioCongQuyDoi) {
        this.gioCongQuyDoi = gioCongQuyDoi;
    }

    public boolean isCoTheCheckIn() {
        return coTheCheckIn;
    }

    public void setCoTheCheckIn(boolean coTheCheckIn) {
        this.coTheCheckIn = coTheCheckIn;
    }

    public boolean isCoTheCheckOut() {
        return coTheCheckOut;
    }

    public void setCoTheCheckOut(boolean coTheCheckOut) {
        this.coTheCheckOut = coTheCheckOut;
    }

    public String getThongBaoCuaSo() {
        return thongBaoCuaSo;
    }

    public void setThongBaoCuaSo(String thongBaoCuaSo) {
        this.thongBaoCuaSo = thongBaoCuaSo;
    }

    public Integer getMaChamCong() {
        return maChamCong;
    }

    public void setMaChamCong(Integer maChamCong) {
        this.maChamCong = maChamCong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
