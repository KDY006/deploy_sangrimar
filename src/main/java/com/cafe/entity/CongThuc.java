package com.cafe.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cong_thuc")
public class CongThuc implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cong_thuc")
    private Integer maCongThuc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_mon", nullable = false)
    private MonNuoc monNuoc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nl", nullable = false)
    private NguyenLieu nguyenLieu;

    @Column(name = "dinh_muc", nullable = false)
    private Double dinhMuc;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public CongThuc() {
    }

    public CongThuc(Integer maCongThuc, MonNuoc monNuoc, NguyenLieu nguyenLieu, Double dinhMuc, String ghiChu) {
        this.maCongThuc = maCongThuc;
        this.monNuoc = monNuoc;
        this.nguyenLieu = nguyenLieu;
        this.dinhMuc = dinhMuc;
        this.ghiChu = ghiChu;
    }

    public Integer getMaCongThuc() {
        return maCongThuc;
    }

    public void setMaCongThuc(Integer maCongThuc) {
        this.maCongThuc = maCongThuc;
    }

    public MonNuoc getMonNuoc() {
        return monNuoc;
    }

    public void setMonNuoc(MonNuoc monNuoc) {
        this.monNuoc = monNuoc;
    }

    public NguyenLieu getNguyenLieu() {
        return nguyenLieu;
    }

    public void setNguyenLieu(NguyenLieu nguyenLieu) {
        this.nguyenLieu = nguyenLieu;
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
