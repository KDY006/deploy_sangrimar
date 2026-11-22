package com.cafe.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_kho")
public class LichSuKho implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_gd")
    private Integer maGD;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nl", nullable = false)
    private NguyenLieu nguyenLieu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nv", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoa_don")
    @JsonIgnore
    private HoaDon hoaDon;

    @Column(name = "loai_gd", nullable = false, length = 30)
    private String loaiGD; 

    @Column(name = "so_luong_thay_doi", nullable = false)
    private Double soLuongThayDoi;

    @Column(name = "ton_truoc", nullable = false)
    private Double tonTruoc;

    @Column(name = "ton_sau", nullable = false)
    private Double tonSau;

    @Column(name = "don_gia", nullable = false)
    private Double donGia = 0.0;

    @Column(name = "thanh_tien", nullable = false)
    private Double thanhTien = 0.0;

    @Column(name = "nha_cung_cap", length = 120)
    private String nhaCungCap;

    @Column(name = "thoi_gian", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGian = LocalDateTime.now();

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public LichSuKho() {
    }

    public LichSuKho(Integer maGD, NguyenLieu nguyenLieu, NhanVien nhanVien, HoaDon hoaDon,
                     String loaiGD, Double soLuongThayDoi, Double tonTruoc, Double tonSau,
                     Double donGia, Double thanhTien, String nhaCungCap, LocalDateTime thoiGian, String ghiChu) {
        this.maGD = maGD;
        this.nguyenLieu = nguyenLieu;
        this.nhanVien = nhanVien;
        this.hoaDon = hoaDon;
        this.loaiGD = loaiGD;
        this.soLuongThayDoi = soLuongThayDoi;
        this.tonTruoc = tonTruoc;
        this.tonSau = tonSau;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.nhaCungCap = nhaCungCap;
        this.thoiGian = thoiGian != null ? thoiGian : LocalDateTime.now();
        this.ghiChu = ghiChu;
    }

    public Integer getMaGD() {
        return maGD;
    }

    public void setMaGD(Integer maGD) {
        this.maGD = maGD;
    }

    public NguyenLieu getNguyenLieu() {
        return nguyenLieu;
    }

    public void setNguyenLieu(NguyenLieu nguyenLieu) {
        this.nguyenLieu = nguyenLieu;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getLoaiGD() {
        return loaiGD;
    }

    public void setLoaiGD(String loaiGD) {
        this.loaiGD = loaiGD;
    }

    public Double getSoLuongThayDoi() {
        return soLuongThayDoi;
    }

    public void setSoLuongThayDoi(Double soLuongThayDoi) {
        this.soLuongThayDoi = soLuongThayDoi;
    }

    public Double getTonTruoc() {
        return tonTruoc;
    }

    public void setTonTruoc(Double tonTruoc) {
        this.tonTruoc = tonTruoc;
    }

    public Double getTonSau() {
        return tonSau;
    }

    public void setTonSau(Double tonSau) {
        this.tonSau = tonSau;
    }

    public Double getDonGia() {
        return donGia;
    }

    public void setDonGia(Double donGia) {
        this.donGia = donGia;
    }

    public Double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(Double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(String nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    
    @JsonProperty("maHoaDon")
    public Integer getMaHoaDon() {
        return hoaDon != null ? hoaDon.getMaHoaDon() : null;
    }

    @JsonProperty("maNL")
    public Integer getMaNL() {
        return nguyenLieu != null ? nguyenLieu.getMaNL() : null;
    }

    @JsonProperty("tenNL")
    public String getTenNL() {
        return nguyenLieu != null ? nguyenLieu.getTenNL() : null;
    }

    @JsonProperty("donViTinh")
    public String getDonViTinh() {
        return nguyenLieu != null ? nguyenLieu.getDonViTinh() : null;
    }

    @JsonProperty("maNV")
    public Integer getMaNV() {
        return nhanVien != null ? nhanVien.getMaNV() : null;
    }

    @JsonProperty("tenNV")
    public String getTenNV() {
        return nhanVien != null ? nhanVien.getHoTen() : null;
    }
}
