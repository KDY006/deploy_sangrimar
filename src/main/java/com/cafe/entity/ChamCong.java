package com.cafe.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cham_cong",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cc_phanca", columnNames = {"ma_phan_ca"}),
                @UniqueConstraint(name = "uk_cc_nv_ngay_ca", columnNames = {"ma_nv", "ngay_lam", "ma_ca"})
        }
)
public class ChamCong implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cham_cong")
    private Integer maChamCong;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_phan_ca")
    private PhanCa phanCa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nv", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_ca", nullable = false)
    private CaLam caLam;

    @Column(name = "ngay_lam", nullable = false)
    private LocalDate ngayLam;

    @Column(name = "gio_check_in")
    private LocalDateTime gioCheckIn;

    @Column(name = "gio_check_out")
    private LocalDateTime gioCheckOut;

    @Column(name = "so_phut_tre", nullable = false)
    private Integer soPhutTre = 0;

    @Column(name = "so_phut_ve_som", nullable = false)
    private Integer soPhutVeSom = 0;

    @Column(name = "so_gio_lam", nullable = false)
    private Double soGioLam = 0.0;

    @Column(name = "gio_cong_quy_doi", nullable = false)
    private Double gioCongQuyDoi = 0.0;

    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dieu_chinh")
    private NhanVien nguoiDieuChinh;

    @Column(name = "ly_do_dieu_chinh", length = 255)
    private String lyDoDieuChinh;

    @Column(name = "thoi_gian_dieu_chinh")
    private LocalDateTime thoiGianDieuChinh;

    public ChamCong() {
    }

    public ChamCong(Integer maChamCong, PhanCa phanCa, NhanVien nhanVien, CaLam caLam,
                    LocalDate ngayLam, LocalDateTime gioCheckIn, LocalDateTime gioCheckOut,
                    Integer soPhutTre, Integer soPhutVeSom, Double soGioLam, Double gioCongQuyDoi,
                    String trangThai, String ghiChu) {
        this.maChamCong = maChamCong;
        this.phanCa = phanCa;
        this.nhanVien = nhanVien;
        this.caLam = caLam;
        this.ngayLam = ngayLam;
        this.gioCheckIn = gioCheckIn;
        this.gioCheckOut = gioCheckOut;
        this.soPhutTre = soPhutTre != null ? soPhutTre : 0;
        this.soPhutVeSom = soPhutVeSom != null ? soPhutVeSom : 0;
        this.soGioLam = soGioLam != null ? soGioLam : 0.0;
        this.gioCongQuyDoi = gioCongQuyDoi != null ? gioCongQuyDoi : 0.0;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public Integer getMaChamCong() {
        return maChamCong;
    }

    public void setMaChamCong(Integer maChamCong) {
        this.maChamCong = maChamCong;
    }

    public PhanCa getPhanCa() {
        return phanCa;
    }

    public void setPhanCa(PhanCa phanCa) {
        this.phanCa = phanCa;
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

    public LocalDateTime getGioCheckIn() {
        return gioCheckIn;
    }

    public void setGioCheckIn(LocalDateTime gioCheckIn) {
        this.gioCheckIn = gioCheckIn;
    }

    public LocalDateTime getGioCheckOut() {
        return gioCheckOut;
    }

    public void setGioCheckOut(LocalDateTime gioCheckOut) {
        this.gioCheckOut = gioCheckOut;
    }

    public Integer getSoPhutTre() {
        return soPhutTre;
    }

    public void setSoPhutTre(Integer soPhutTre) {
        this.soPhutTre = soPhutTre != null ? soPhutTre : 0;
    }

    public Integer getSoPhutVeSom() {
        return soPhutVeSom;
    }

    public void setSoPhutVeSom(Integer soPhutVeSom) {
        this.soPhutVeSom = soPhutVeSom != null ? soPhutVeSom : 0;
    }

    public Double getSoGioLam() {
        return soGioLam;
    }

    public void setSoGioLam(Double soGioLam) {
        this.soGioLam = soGioLam != null ? soGioLam : 0.0;
    }

    public Double getGioCongQuyDoi() {
        return gioCongQuyDoi;
    }

    public void setGioCongQuyDoi(Double gioCongQuyDoi) {
        this.gioCongQuyDoi = gioCongQuyDoi != null ? gioCongQuyDoi : 0.0;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public NhanVien getNguoiDieuChinh() {
        return nguoiDieuChinh;
    }

    public void setNguoiDieuChinh(NhanVien nguoiDieuChinh) {
        this.nguoiDieuChinh = nguoiDieuChinh;
    }

    public String getLyDoDieuChinh() {
        return lyDoDieuChinh;
    }

    public void setLyDoDieuChinh(String lyDoDieuChinh) {
        this.lyDoDieuChinh = lyDoDieuChinh;
    }

    public LocalDateTime getThoiGianDieuChinh() {
        return thoiGianDieuChinh;
    }

    public void setThoiGianDieuChinh(LocalDateTime thoiGianDieuChinh) {
        this.thoiGianDieuChinh = thoiGianDieuChinh;
    }
}
