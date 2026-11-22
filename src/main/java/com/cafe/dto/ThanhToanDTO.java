package com.cafe.dto;

public class ThanhToanDTO {

    private Double chietKhau = 0.0;
    private String phuongThucTT = "TIEN_MAT";
    private Double tienKhachDua;
    private Boolean xacNhanChuyenKhoan = false;
    private String maThamChieu;
    private String minhChungChuyenKhoan;
    private String yeuCauThanhToan;
    private Long phienBan;

    public ThanhToanDTO() {
    }

    public ThanhToanDTO(Double chietKhau, String phuongThucTT) {
        this.chietKhau = chietKhau;
        this.phuongThucTT = phuongThucTT;
    }

    public ThanhToanDTO(Double chietKhau, String phuongThucTT, Double tienKhachDua,
                        Boolean xacNhanChuyenKhoan, String maThamChieu, String yeuCauThanhToan, Long phienBan) {
        this.chietKhau = chietKhau;
        this.phuongThucTT = phuongThucTT;
        this.tienKhachDua = tienKhachDua;
        this.xacNhanChuyenKhoan = xacNhanChuyenKhoan;
        this.maThamChieu = maThamChieu;
        this.yeuCauThanhToan = yeuCauThanhToan;
        this.phienBan = phienBan;
    }

    public Double getChietKhau() {
        return chietKhau;
    }

    public void setChietKhau(Double chietKhau) {
        this.chietKhau = chietKhau;
    }

    public String getPhuongThucTT() {
        return phuongThucTT;
    }

    public void setPhuongThucTT(String phuongThucTT) {
        this.phuongThucTT = phuongThucTT;
    }

    public Double getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(Double tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public Boolean getXacNhanChuyenKhoan() {
        return xacNhanChuyenKhoan;
    }

    public void setXacNhanChuyenKhoan(Boolean xacNhanChuyenKhoan) {
        this.xacNhanChuyenKhoan = xacNhanChuyenKhoan;
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
}
