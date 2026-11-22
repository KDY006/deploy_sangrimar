package com.cafe.dto;

import com.cafe.entity.CaLam;
import com.cafe.entity.NhanVien;

import java.time.LocalDate;

public class PhanCaTuanItemDTO {

    private Integer maPhanCa;
    private NhanVien nhanVien;
    private CaLam caLam;
    private LocalDate ngayLam;
    private String ghiChu;
    private boolean thieuThuNgan;
    private boolean daChamCong;
    private String trangThaiChamCong;

    public PhanCaTuanItemDTO() {
    }

    public PhanCaTuanItemDTO(Integer maPhanCa, NhanVien nhanVien, CaLam caLam, LocalDate ngayLam,
                            String ghiChu, boolean thieuThuNgan, boolean daChamCong, String trangThaiChamCong) {
        this.maPhanCa = maPhanCa;
        this.nhanVien = nhanVien;
        this.caLam = caLam;
        this.ngayLam = ngayLam;
        this.ghiChu = ghiChu;
        this.thieuThuNgan = thieuThuNgan;
        this.daChamCong = daChamCong;
        this.trangThaiChamCong = trangThaiChamCong;
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

    public boolean isThieuThuNgan() {
        return thieuThuNgan;
    }

    public void setThieuThuNgan(boolean thieuThuNgan) {
        this.thieuThuNgan = thieuThuNgan;
    }

    public boolean isDaChamCong() {
        return daChamCong;
    }

    public void setDaChamCong(boolean daChamCong) {
        this.daChamCong = daChamCong;
    }

    public String getTrangThaiChamCong() {
        return trangThaiChamCong;
    }

    public void setTrangThaiChamCong(String trangThaiChamCong) {
        this.trangThaiChamCong = trangThaiChamCong;
    }

    public Integer getMaNV() {
        return nhanVien != null ? nhanVien.getMaNV() : null;
    }

    public String getHoTen() {
        return nhanVien != null ? nhanVien.getHoTen() : null;
    }

    public String getVaiTro() {
        return nhanVien != null ? nhanVien.getVaiTro() : null;
    }

    public Integer getMaCa() {
        return caLam != null ? caLam.getMaCa() : null;
    }

    public String getTenCa() {
        return caLam != null ? caLam.getTenCa() : null;
    }
}
