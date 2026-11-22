package com.cafe.dto;

import java.time.LocalDateTime;

public class DieuChinhChamCongDTO {

    private LocalDateTime gioCheckIn;
    private LocalDateTime gioCheckOut;
    private String lyDoDieuChinh;

    public DieuChinhChamCongDTO() {
    }

    public DieuChinhChamCongDTO(LocalDateTime gioCheckIn, LocalDateTime gioCheckOut, String lyDoDieuChinh) {
        this.gioCheckIn = gioCheckIn;
        this.gioCheckOut = gioCheckOut;
        this.lyDoDieuChinh = lyDoDieuChinh;
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

    public String getLyDoDieuChinh() {
        return lyDoDieuChinh;
    }

    public void setLyDoDieuChinh(String lyDoDieuChinh) {
        this.lyDoDieuChinh = lyDoDieuChinh;
    }
}
