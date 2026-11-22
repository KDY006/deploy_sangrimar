package com.cafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(
        name = "phan_ca",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_phan_ca_nhan_vien_ngay_ca",
                columnNames = {"ma_nv", "ngay_lam", "ma_ca"}
        )
)
public class PhanCa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phan_ca")
    private Integer maPhanCa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nv", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_ca", nullable = false)
    private CaLam caLam;

    @Column(name = "ngay_lam", nullable = false)
    private LocalDate ngayLam;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public PhanCa() {
    }

    public PhanCa(Integer maPhanCa, NhanVien nhanVien, CaLam caLam, LocalDate ngayLam, String ghiChu) {
        this.maPhanCa = maPhanCa;
        this.nhanVien = nhanVien;
        this.caLam = caLam;
        this.ngayLam = ngayLam;
        this.ghiChu = ghiChu;
    }

    public Integer getMaPhanCa() {
        return maPhanCa;
    }

    public void setMaPhanCa(Integer maPhanCa) {
        this.maPhanCa = maPhanCa;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public CaLam getCaLam() {
        return caLam;
    }

    public void setCaLam(CaLam caLam) {
        this.caLam = caLam;
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
