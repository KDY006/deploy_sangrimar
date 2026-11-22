package com.cafe.dto;

public class KiemKeKhoDTO {
    private Integer maNL;
    private Double soLuongThucTe;
    private String ghiChu;

    public KiemKeKhoDTO() {
    }

    public KiemKeKhoDTO(Integer maNL, Double soLuongThucTe, String ghiChu) {
        this.maNL = maNL;
        this.soLuongThucTe = soLuongThucTe;
        this.ghiChu = ghiChu;
    }

    public Integer getMaNL() {
        return maNL;
    }

    public void setMaNL(Integer maNL) {
        this.maNL = maNL;
    }

    public Double getSoLuongThucTe() {
        return soLuongThucTe;
    }

    public void setSoLuongThucTe(Double soLuongThucTe) {
        this.soLuongThucTe = soLuongThucTe;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
