package com.cafe.dto;

import java.util.List;

public class DonHangDTO {

    private Integer maBan;
    private Integer maNV;
    private String loaiDon;
    private List<ChiTietDonHangDTO> danhSachMon;
    private String yeuCauTao;
    private Long phienBan;

    public DonHangDTO() {
    }

    public DonHangDTO(Integer maBan, Integer maNV, String loaiDon, List<ChiTietDonHangDTO> danhSachMon, String yeuCauTao, Long phienBan) {
        this.maBan = maBan;
        this.maNV = maNV;
        this.loaiDon = loaiDon;
        this.danhSachMon = danhSachMon;
        this.yeuCauTao = yeuCauTao;
        this.phienBan = phienBan;
    }

    public Integer getMaBan() {
        return maBan;
    }

    public void setMaBan(Integer maBan) {
        this.maBan = maBan;
    }

    public Integer getMaNV() {
        return maNV;
    }

    public void setMaNV(Integer maNV) {
        this.maNV = maNV;
    }

    public String getLoaiDon() {
        return loaiDon;
    }

    public void setLoaiDon(String loaiDon) {
        this.loaiDon = loaiDon;
    }

    public List<ChiTietDonHangDTO> getDanhSachMon() {
        return danhSachMon;
    }

    public void setDanhSachMon(List<ChiTietDonHangDTO> danhSachMon) {
        this.danhSachMon = danhSachMon;
    }

    public String getYeuCauTao() {
        return yeuCauTao;
    }

    public void setYeuCauTao(String yeuCauTao) {
        this.yeuCauTao = yeuCauTao;
    }

    public Long getPhienBan() {
        return phienBan;
    }

    public void setPhienBan(Long phienBan) {
        this.phienBan = phienBan;
    }
}
