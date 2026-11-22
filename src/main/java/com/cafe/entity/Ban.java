package com.cafe.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ban")
public class Ban implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ban")
    private Integer maBan;

    @Column(name = "ten_ban", nullable = false, length = 50)
    private String tenBan;

    @Column(name = "vi_tri", length = 50)
    private String viTri;

    @Column(name = "so_ghe", nullable = false)
    private Integer soGhe = 4;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "TRONG";

    public Ban() {
    }

    public Ban(Integer maBan, String tenBan, String viTri, String trangThai) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.viTri = viTri;
        this.trangThai = trangThai;
    }

    public Ban(Integer maBan, String tenBan, String viTri, Integer soGhe, String trangThai) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.viTri = viTri;
        this.soGhe = soGhe;
        this.trangThai = trangThai;
    }

    public Integer getMaBan() {
        return maBan;
    }

    public void setMaBan(Integer maBan) {
        this.maBan = maBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public void setTenBan(String tenBan) {
        this.tenBan = tenBan;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public Integer getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(Integer soGhe) {
        this.soGhe = soGhe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
