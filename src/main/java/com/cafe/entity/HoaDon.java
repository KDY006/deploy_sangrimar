package com.cafe.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hoa_don")
public class HoaDon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hoa_don")
    private Integer maHoaDon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_ban", nullable = true)
    private Ban ban;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nv", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nv_thu_ngan", nullable = true)
    private NhanVien nhanVienThuNgan;

    @Column(name = "thoi_gian_tao", nullable = false)
    private LocalDateTime thoiGianTao = LocalDateTime.now();

    @Column(name = "thoi_gian_yeu_cau_tt")
    private LocalDateTime thoiGianYeuCauTT = null;

    @Column(name = "thoi_gian_thanh_toan")
    private LocalDateTime thoiGianThanhToan = null;

    @Column(name = "loai_don", nullable = false, length = 20)
    private String loaiDon = "TAI_BAN";

    @Column(name = "tong_tien_goc", nullable = false)
    private Double tongTienGoc = 0.0;

    @Column(name = "chiet_khau", nullable = false)
    private Double chietKhau = 0.0;

    @Column(name = "tong_tien_thanh_toan", nullable = false)
    private Double tongTienThanhToan = 0.0;

    @Column(name = "tien_khach_dua")
    private Double tienKhachDua = null;

    @Column(name = "tien_thua", nullable = false)
    private Double tienThua = 0.0;

    @Column(name = "phuong_thuc_tt", length = 20)
    private String phuongThucTT = "TIEN_MAT";

    @Column(name = "ma_tham_chieu", length = 100, unique = true)
    private String maThamChieu = null;

    @Column(name = "minh_chung_chuyen_khoan", length = 255)
    private String minhChungChuyenKhoan = null;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "DANG_PHUC_VU";

    @Column(name = "yeu_cau_tao", length = 36, unique = true)
    private String yeuCauTao = null;

    @Column(name = "yeu_cau_thanh_toan", length = 36, unique = true)
    private String yeuCauThanhToan = null;

    @Version
    @Column(name = "phien_ban", columnDefinition = "bigint default 0")
    private Long phienBan = 0L;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();

    @Transient
    private List<ChiTietHoaDon> phieuInBar;

    @Transient
    private List<ChiTietHoaDon> phieuInBarBoSung;

    public HoaDon() {
    }

    public Integer getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(Integer maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public Ban getBan() {
        return ban;
    }

    public void setBan(Ban ban) {
        this.ban = ban;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public NhanVien getNhanVienThuNgan() {
        return nhanVienThuNgan;
    }

    public void setNhanVienThuNgan(NhanVien nhanVienThuNgan) {
        this.nhanVienThuNgan = nhanVienThuNgan;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public LocalDateTime getThoiGianYeuCauTT() {
        return thoiGianYeuCauTT;
    }

    public void setThoiGianYeuCauTT(LocalDateTime thoiGianYeuCauTT) {
        this.thoiGianYeuCauTT = thoiGianYeuCauTT;
    }

    public LocalDateTime getThoiGianThanhToan() {
        return thoiGianThanhToan;
    }

    public void setThoiGianThanhToan(LocalDateTime thoiGianThanhToan) {
        this.thoiGianThanhToan = thoiGianThanhToan;
    }

    public String getLoaiDon() {
        return loaiDon;
    }

    public void setLoaiDon(String loaiDon) {
        this.loaiDon = loaiDon;
    }

    public Double getTongTienGoc() {
        return tongTienGoc;
    }

    public void setTongTienGoc(Double tongTienGoc) {
        this.tongTienGoc = tongTienGoc;
    }

    public Double getChietKhau() {
        return chietKhau;
    }

    public void setChietKhau(Double chietKhau) {
        this.chietKhau = chietKhau;
    }

    public Double getTongTienThanhToan() {
        return tongTienThanhToan;
    }

    public void setTongTienThanhToan(Double tongTienThanhToan) {
        this.tongTienThanhToan = tongTienThanhToan;
    }

    public Double getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(Double tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public Double getTienThua() {
        return tienThua;
    }

    public void setTienThua(Double tienThua) {
        this.tienThua = tienThua;
    }

    public String getPhuongThucTT() {
        return phuongThucTT;
    }

    public void setPhuongThucTT(String phuongThucTT) {
        this.phuongThucTT = phuongThucTT;
    }

    public String getMaThamChieu() {
        return maThamChieu;
    }

    public void setMaThamChieu(String maThamChieu) {
        this.maThamChieu = maThamChieu;
    }

    public String getMinhChungChuyenKhoan() {
        return minhChungChuyenKhoan;
    }

    public void setMinhChungChuyenKhoan(String minhChungChuyenKhoan) {
        this.minhChungChuyenKhoan = minhChungChuyenKhoan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getYeuCauTao() {
        return yeuCauTao;
    }

    public void setYeuCauTao(String yeuCauTao) {
        this.yeuCauTao = yeuCauTao;
    }

    public String getYeuCauThanhToan() {
        return yeuCauThanhToan;
    }

    public void setYeuCauThanhToan(String yeuCauThanhToan) {
        this.yeuCauThanhToan = yeuCauThanhToan;
    }

    public Long getPhienBan() {
        return phienBan;
    }

    public void setPhienBan(Long phienBan) {
        this.phienBan = phienBan;
    }

    public List<ChiTietHoaDon> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietHoaDon> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
    }

    public void addChiTiet(ChiTietHoaDon chiTiet) {
        danhSachChiTiet.add(chiTiet);
        chiTiet.setHoaDon(this);
    }

    public List<ChiTietHoaDon> getPhieuInBar() {
        return phieuInBar;
    }

    public void setPhieuInBar(List<ChiTietHoaDon> phieuInBar) {
        this.phieuInBar = phieuInBar;
    }

    public List<ChiTietHoaDon> getPhieuInBarBoSung() {
        return phieuInBarBoSung;
    }

    public void setPhieuInBarBoSung(List<ChiTietHoaDon> phieuInBarBoSung) {
        this.phieuInBarBoSung = phieuInBarBoSung;
    }
}
