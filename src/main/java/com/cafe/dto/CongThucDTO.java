package com.cafe.dto;

public class CongThucDTO {
    private Integer maMon;
    private Integer maNL;
    private Double dinhMuc;
    private String ghiChu;

    public CongThucDTO() {
    }

    public CongThucDTO(Integer maMon, Integer maNL, Double dinhMuc, String ghiChu) {
        this.maMon = maMon;
        this.maNL = maNL;
        this.dinhMuc = dinhMuc;
        this.ghiChu = ghiChu;
    }

    public Integer getMaMon() {
        return maMon;
    }

    public void setMaMon(Integer maMon) {
        this.maMon = maMon;
    }

    public Integer getMaNL() {
        return maNL;
    }

    public void setMaNL(Integer maNL) {
        this.maNL = maNL;
    }

    public Double getDinhMuc() {
        return dinhMuc;
    }

    public void setDinhMuc(Double dinhMuc) {
        this.dinhMuc = dinhMuc;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
