package com.cafe.dto;

public class ChotCaResponseDTO {

    private int soCaDanhDauVang;
    private int soCaQuenCheckOut;

    public ChotCaResponseDTO() {
    }

    public ChotCaResponseDTO(int soCaDanhDauVang, int soCaQuenCheckOut) {
        this.soCaDanhDauVang = soCaDanhDauVang;
        this.soCaQuenCheckOut = soCaQuenCheckOut;
    }

    public int getSoCaDanhDauVang() {
        return soCaDanhDauVang;
    }

    public void setSoCaDanhDauVang(int soCaDanhDauVang) {
        this.soCaDanhDauVang = soCaDanhDauVang;
    }

    public int getSoCaQuenCheckOut() {
        return soCaQuenCheckOut;
    }

    public void setSoCaQuenCheckOut(int soCaQuenCheckOut) {
        this.soCaQuenCheckOut = soCaQuenCheckOut;
    }
}
