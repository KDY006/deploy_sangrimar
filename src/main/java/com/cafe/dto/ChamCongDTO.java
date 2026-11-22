package com.cafe.dto;

public class ChamCongDTO {

    private Integer maPhanCa;
    private Integer maNV;
    private Integer maCa;
    private String ghiChu;

    public ChamCongDTO() {
    }

    public ChamCongDTO(Integer maNV, Integer maCa) {
        this.maNV = maNV;
        this.maCa = maCa;
    }

    public ChamCongDTO(Integer maPhanCa, Integer maNV, Integer maCa, String ghiChu) {
        this.maPhanCa = maPhanCa;
        this.maNV = maNV;
        this.maCa = maCa;
        this.ghiChu = ghiChu;
    }

    public Integer getMaPhanCa() {
        return maPhanCa;
    }

    public void setMaPhanCa(Integer maPhanCa) {
        this.maPhanCa = maPhanCa;
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

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
