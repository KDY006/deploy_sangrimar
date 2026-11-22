package com.cafe.dto;

import com.cafe.entity.NguyenLieu;
import java.util.ArrayList;
import java.util.List;

public class DashboardKpiDTO {
    private String phamVi;
    private Double tongDoanhThu = 0.0;
    private Integer tongDonHang = 0;
    private Double tongChietKhau = 0.0;
    private Double doanhThuTienMat = 0.0;
    private Integer soDonTienMat = 0;
    private Double doanhThuChuyenKhoan = 0.0;
    private Integer soDonChuyenKhoan = 0;
    private Double chiPhiNhapKho = 0.0;
    private Double giaVonBanHang = 0.0;
    private Double giaTriHaoHut = 0.0;
    private Double loiNhuanGop = 0.0;
    private Double tongGiaTriTonKho = 0.0;
    private Integer tongNguyenLieu = 0;
    private Integer soNguyenLieuSapHet = 0;
    private List<NguyenLieu> danhSachSapHet = new ArrayList<>();
    private List<TopMonBanChayDTO> topMonBanChay = new ArrayList<>();

    public DashboardKpiDTO() {
    }

    public String getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(String phamVi) {
        this.phamVi = phamVi;
    }

    public Double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(Double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public Integer getTongDonHang() {
        return tongDonHang;
    }

    public void setTongDonHang(Integer tongDonHang) {
        this.tongDonHang = tongDonHang;
    }

    public Double getTongChietKhau() {
        return tongChietKhau;
    }

    public void setTongChietKhau(Double tongChietKhau) {
        this.tongChietKhau = tongChietKhau;
    }

    public Double getDoanhThuTienMat() {
        return doanhThuTienMat;
    }

    public void setDoanhThuTienMat(Double doanhThuTienMat) {
        this.doanhThuTienMat = doanhThuTienMat;
    }

    public Integer getSoDonTienMat() {
        return soDonTienMat;
    }

    public void setSoDonTienMat(Integer soDonTienMat) {
        this.soDonTienMat = soDonTienMat;
    }

    public Double getDoanhThuChuyenKhoan() {
        return doanhThuChuyenKhoan;
    }

    public void setDoanhThuChuyenKhoan(Double doanhThuChuyenKhoan) {
        this.doanhThuChuyenKhoan = doanhThuChuyenKhoan;
    }

    public Integer getSoDonChuyenKhoan() {
        return soDonChuyenKhoan;
    }

    public void setSoDonChuyenKhoan(Integer soDonChuyenKhoan) {
        this.soDonChuyenKhoan = soDonChuyenKhoan;
    }

    public Double getChiPhiNhapKho() {
        return chiPhiNhapKho;
    }

    public void setChiPhiNhapKho(Double chiPhiNhapKho) {
        this.chiPhiNhapKho = chiPhiNhapKho;
    }

    public Double getGiaVonBanHang() {
        return giaVonBanHang;
    }

    public void setGiaVonBanHang(Double giaVonBanHang) {
        this.giaVonBanHang = giaVonBanHang;
    }

    public Double getGiaTriHaoHut() {
        return giaTriHaoHut;
    }

    public void setGiaTriHaoHut(Double giaTriHaoHut) {
        this.giaTriHaoHut = giaTriHaoHut;
    }

    public Double getLoiNhuanGop() {
        return loiNhuanGop;
    }

    public void setLoiNhuanGop(Double loiNhuanGop) {
        this.loiNhuanGop = loiNhuanGop;
    }

    public Double getTongGiaTriTonKho() {
        return tongGiaTriTonKho;
    }

    public void setTongGiaTriTonKho(Double tongGiaTriTonKho) {
        this.tongGiaTriTonKho = tongGiaTriTonKho;
    }

    public Integer getTongNguyenLieu() {
        return tongNguyenLieu;
    }

    public void setTongNguyenLieu(Integer tongNguyenLieu) {
        this.tongNguyenLieu = tongNguyenLieu;
    }

    public Integer getSoNguyenLieuSapHet() {
        return soNguyenLieuSapHet;
    }

    public void setSoNguyenLieuSapHet(Integer soNguyenLieuSapHet) {
        this.soNguyenLieuSapHet = soNguyenLieuSapHet;
    }

    public List<NguyenLieu> getDanhSachSapHet() {
        return danhSachSapHet;
    }

    public void setDanhSachSapHet(List<NguyenLieu> danhSachSapHet) {
        this.danhSachSapHet = danhSachSapHet;
    }

    public List<TopMonBanChayDTO> getTopMonBanChay() {
        return topMonBanChay;
    }

    public void setTopMonBanChay(List<TopMonBanChayDTO> topMonBanChay) {
        this.topMonBanChay = topMonBanChay;
    }
}
