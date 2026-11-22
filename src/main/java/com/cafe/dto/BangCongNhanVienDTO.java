package com.cafe.dto;

public class BangCongNhanVienDTO {

    private Integer maNV;
    private String hoTen;
    private String tenDangNhap;
    private String vaiTro;
    private Integer tongSoCa = 0;
    private Integer soCaDungGio = 0;
    private Integer soCaDiTre = 0;
    private Integer soCaVeSom = 0;
    private Integer soCaVang = 0;
    private Integer soCaQuenCheckOut = 0;
    private Double tongGioThucTe = 0.0;
    private Double tongGioQuyDoi = 0.0;

    public BangCongNhanVienDTO() {
    }

    public Integer getMaNV() {
        return maNV;
    }

    public void setMaNV(Integer maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public Integer getTongSoCa() {
        return tongSoCa;
    }

    public void setTongSoCa(Integer tongSoCa) {
        this.tongSoCa = tongSoCa;
    }

    public Integer getSoCaDungGio() {
        return soCaDungGio;
    }

    public void setSoCaDungGio(Integer soCaDungGio) {
        this.soCaDungGio = soCaDungGio;
    }

    public Integer getSoCaDiTre() {
        return soCaDiTre;
    }

    public void setSoCaDiTre(Integer soCaDiTre) {
        this.soCaDiTre = soCaDiTre;
    }

    public Integer getSoCaVeSom() {
        return soCaVeSom;
    }

    public void setSoCaVeSom(Integer soCaVeSom) {
        this.soCaVeSom = soCaVeSom;
    }

    public Integer getSoCaVang() {
        return soCaVang;
    }

    public void setSoCaVang(Integer soCaVang) {
        this.soCaVang = soCaVang;
    }

    public Integer getSoCaQuenCheckOut() {
        return soCaQuenCheckOut;
    }

    public void setSoCaQuenCheckOut(Integer soCaQuenCheckOut) {
        this.soCaQuenCheckOut = soCaQuenCheckOut;
    }

    public Double getTongGioThucTe() {
        return tongGioThucTe;
    }

    public void setTongGioThucTe(Double tongGioThucTe) {
        this.tongGioThucTe = tongGioThucTe;
    }

    public Double getTongGioQuyDoi() {
        return tongGioQuyDoi;
    }

    public void setTongGioQuyDoi(Double tongGioQuyDoi) {
        this.tongGioQuyDoi = tongGioQuyDoi;
    }
}
