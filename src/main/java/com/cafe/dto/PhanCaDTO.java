package com.cafe.dto;

import java.time.LocalDate;

public class PhanCaDTO {

    private Integer maNV;
    private Integer maCa;
    private LocalDate ngayLam;
    private String ghiChu;

    public PhanCaDTO() {
    }

    public PhanCaDTO(Integer maNV, Integer maCa, LocalDate ngayLam, String ghiChu) {
        this.maNV = maNV;
        this.maCa = maCa;
        this.ngayLam = ngayLam;
        this.ghiChu = ghiChu;
    }

    public Integer getMaNV() {
        return maNV;
    }

    public void setMaNV(Integer maNV) {
        this.maNV = maNV;
    }

    public Integer getMaCa() {
        return maCa;
    }

    public void setMaCa(Integer maCa) {
        this.maCa = maCa;
    }

    public LocalDate getNgayLam() {
        return ngayLam;
    }

    public void setNgayLam(LocalDate ngayLam) {
        this.ngayLam = ngayLam;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
