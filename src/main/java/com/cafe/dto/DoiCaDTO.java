package com.cafe.dto;

public class DoiCaDTO {

    private Integer maNVMoi;
    private String lyDoDoiCa;

    public DoiCaDTO() {
    }

    public DoiCaDTO(Integer maNVMoi, String lyDoDoiCa) {
        this.maNVMoi = maNVMoi;
        this.lyDoDoiCa = lyDoDoiCa;
    }

    public Integer getMaNVMoi() {
        return maNVMoi;
    }

    public void setMaNVMoi(Integer maNVMoi) {
        this.maNVMoi = maNVMoi;
    }

    public String getLyDoDoiCa() {
        return lyDoDoiCa;
    }

    public void setLyDoDoiCa(String lyDoDoiCa) {
        this.lyDoDoiCa = lyDoDoiCa;
    }
}
