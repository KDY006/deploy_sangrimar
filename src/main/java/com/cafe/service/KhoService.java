package com.cafe.service;

import com.cafe.dto.BangCongThucMonDTO;
import com.cafe.dto.CongThucDTO;
import com.cafe.dto.DashboardKpiDTO;
import com.cafe.dto.DinhMucNguyenLieuDTO;
import com.cafe.dto.KiemKeKhoDTO;
import com.cafe.dto.NhapKhoDTO;
import com.cafe.dto.TopMonBanChayDTO;
import com.cafe.entity.ChiTietHoaDon;
import com.cafe.entity.CongThuc;
import com.cafe.entity.HoaDon;
import com.cafe.entity.LichSuKho;
import com.cafe.entity.MonNuoc;
import com.cafe.entity.NguyenLieu;
import com.cafe.entity.NhanVien;
import com.cafe.repository.CongThucRepository;
import com.cafe.repository.HoaDonRepository;
import com.cafe.repository.LichSuKhoRepository;
import com.cafe.repository.MonNuocRepository;
import com.cafe.repository.NguyenLieuRepository;
import com.cafe.repository.NhanVienRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
/** Dịch vụ quản lý kho, định mức công thức và báo cáo KPI */
public class KhoService {

    @PersistenceContext
    private EntityManager entityManager;

    private final NguyenLieuRepository nguyenLieuRepository;
    private final CongThucRepository congThucRepository;
    private final MonNuocRepository monNuocRepository;
    private final HoaDonRepository hoaDonRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LichSuKhoRepository lichSuKhoRepository;

    public KhoService(NguyenLieuRepository nguyenLieuRepository,
                      CongThucRepository congThucRepository,
                      MonNuocRepository monNuocRepository,
                      HoaDonRepository hoaDonRepository,
                      NhanVienRepository nhanVienRepository,
                      LichSuKhoRepository lichSuKhoRepository) {
        this.nguyenLieuRepository = nguyenLieuRepository;
        this.congThucRepository = congThucRepository;
        this.monNuocRepository = monNuocRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.lichSuKhoRepository = lichSuKhoRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void truKhoTuDong(HoaDon hoaDon, NhanVien nhanVien) {
        if (hoaDon == null || hoaDon.getDanhSachChiTiet() == null || hoaDon.getDanhSachChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn không có món để trừ kho");
        }

        NhanVien nv = (nhanVien != null) ? nhanVien : hoaDon.getNhanVienThuNgan();
        if (nv == null) {
            nv = nhanVienRepository.findAll().stream().findFirst().orElse(null);
        }

        
        Map<Integer, BigDecimal> nhuCau = new TreeMap<>();
        for (ChiTietHoaDon item : hoaDon.getDanhSachChiTiet()) {
            if (item.getSoLuong() == null || item.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng món không hợp lệ");
            }
            List<CongThuc> recipes = congThucRepository.findByMonNuoc(item.getMonNuoc());
            if (recipes.isEmpty()) {
                throw new IllegalArgumentException("Món chưa có công thức: " + item.getMonNuoc().getTenMon() + ". Hãy thiết lập công thức trước khi thanh toán.");
            }
            for (CongThuc ct : recipes) {
                Double rate = ct.getDinhMuc();
                if (rate == null || !Double.isFinite(rate) || rate <= 0) {
                    throw new IllegalArgumentException("Định mức công thức phải lớn hơn 0");
                }
                BigDecimal lineAmount = BigDecimal.valueOf(rate).multiply(BigDecimal.valueOf(item.getSoLuong()));
                nhuCau.merge(ct.getNguyenLieu().getMaNL(), lineAmount, BigDecimal::add);
            }
        }

        
        Map<Integer, NguyenLieu> locked = new LinkedHashMap<>();
        for (Map.Entry<Integer, BigDecimal> entry : nhuCau.entrySet()) {
            NguyenLieu nl = nguyenLieuRepository.khoaNguyenLieu(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu"));
            
            entityManager.refresh(nl, LockModeType.PESSIMISTIC_WRITE);
            Double ton = nl.getSoLuongTon();
            if (ton == null || !Double.isFinite(ton) || BigDecimal.valueOf(ton).compareTo(entry.getValue()) < 0) {
                throw new IllegalArgumentException("Không đủ nguyên liệu '" + nl.getTenNL() + "': cần " + entry.getValue() + " " + nl.getDonViTinh() + ", còn " + ton);
            }
            locked.put(entry.getKey(), nl);
        }

        
        for (Map.Entry<Integer, BigDecimal> entry : nhuCau.entrySet()) {
            NguyenLieu nl = locked.get(entry.getKey());
            double tonTruoc = nl.getSoLuongTon() != null ? nl.getSoLuongTon() : 0.0;
            double tongCanTru = entry.getValue().doubleValue();
            double tonSau = BigDecimal.valueOf(tonTruoc).subtract(entry.getValue()).doubleValue();

            nl.setSoLuongTon(tonSau);
            nguyenLieuRepository.save(nl);

            double donGia = nl.getDonGiaNhap() != null ? nl.getDonGiaNhap() : 0.0;
            double thanhTien = BigDecimal.valueOf(tongCanTru).multiply(BigDecimal.valueOf(donGia)).setScale(2, RoundingMode.HALF_UP).doubleValue();

            LichSuKho lsk = new LichSuKho();
            lsk.setNguyenLieu(nl);
            lsk.setNhanVien(nv);
            lsk.setHoaDon(hoaDon);
            lsk.setLoaiGD("XUAT_BAN_HANG");
            lsk.setSoLuongThayDoi(-tongCanTru);
            lsk.setTonTruoc(tonTruoc);
            lsk.setTonSau(tonSau);
            lsk.setDonGia(donGia);
            lsk.setThanhTien(thanhTien);
            lsk.setThoiGian(LocalDateTime.now());
            lsk.setGhiChu("Trừ kho tự động theo BOM đơn #" + (hoaDon.getMaHoaDon() != null ? hoaDon.getMaHoaDon() : ""));
            lichSuKhoRepository.save(lsk);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void truKhoTuDong(HoaDon hoaDon) {
        truKhoTuDong(hoaDon, hoaDon != null ? hoaDon.getNhanVienThuNgan() : null);
    }

    public List<NguyenLieu> layTatCaNguyenLieu() {
        return nguyenLieuRepository.findAllByOrderByMaNLAsc();
    }

    public List<NguyenLieu> layDanhSachSapHet() {
        return nguyenLieuRepository.findNguyenLieuSapHet();
    }

    @Transactional(rollbackFor = Exception.class)
    public NguyenLieu nhapKho(NhapKhoDTO dto, Integer maNV) {
        if (dto == null || dto.getMaNL() == null) {
            throw new IllegalArgumentException("Mã nguyên liệu không được để trống");
        }
        if (dto.getSoLuongNhap() == null || !Double.isFinite(dto.getSoLuongNhap()) || dto.getSoLuongNhap() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }

        NguyenLieu nl = nguyenLieuRepository.khoaNguyenLieu(dto.getMaNL())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu có mã: " + dto.getMaNL()));

        double tonTruoc = nl.getSoLuongTon() != null ? nl.getSoLuongTon() : 0.0;
        double soLuongNhap = dto.getSoLuongNhap();
        double tonSau = tonTruoc + soLuongNhap;
        double donGiaCu = nl.getDonGiaNhap() != null ? nl.getDonGiaNhap() : 0.0;
        double donGiaLichSu = donGiaCu;

        if (dto.getDonGiaNhapMoi() != null && dto.getDonGiaNhapMoi() > 0) {
            donGiaLichSu = dto.getDonGiaNhapMoi();
            double tongGiaTri = (tonTruoc * donGiaCu) + (soLuongNhap * dto.getDonGiaNhapMoi());
            double donGiaMoi = BigDecimal.valueOf(tongGiaTri / tonSau).setScale(2, RoundingMode.HALF_UP).doubleValue();
            nl.setDonGiaNhap(donGiaMoi);
        }

        nl.setSoLuongTon(tonSau);
        nguyenLieuRepository.save(nl);

        NhanVien nv = null;
        if (maNV != null) {
            nv = nhanVienRepository.findById(maNV).orElse(null);
        }
        if (nv == null) {
            nv = nhanVienRepository.findByVaiTro("QUAN_LY").stream().findFirst().orElse(null);
        }
        if (nv == null) {
            nv = nhanVienRepository.findAll().stream().findFirst().orElse(null);
        }

        double thanhTien = BigDecimal.valueOf(soLuongNhap).multiply(BigDecimal.valueOf(donGiaLichSu)).setScale(2, RoundingMode.HALF_UP).doubleValue();

        LichSuKho lsk = new LichSuKho();
        lsk.setNguyenLieu(nl);
        lsk.setNhanVien(nv);
        lsk.setLoaiGD("NHAP_KHO");
        lsk.setSoLuongThayDoi(soLuongNhap);
        lsk.setTonTruoc(tonTruoc);
        lsk.setTonSau(tonSau);
        lsk.setDonGia(donGiaLichSu);
        lsk.setThanhTien(thanhTien);
        lsk.setNhaCungCap(dto.getNhaCungCap());
        lsk.setGhiChu(dto.getGhiChu());
        lsk.setThoiGian(LocalDateTime.now());
        lichSuKhoRepository.save(lsk);

        return nl;
    }

    @Transactional(rollbackFor = Exception.class)
    public NguyenLieu nhapKho(Integer maNL, Double soLuongNhap) {
        NhapKhoDTO dto = new NhapKhoDTO(maNL, soLuongNhap);
        return nhapKho(dto, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public NguyenLieu kiemKeKho(KiemKeKhoDTO dto, Integer maNV) {
        if (dto == null || dto.getMaNL() == null) {
            throw new IllegalArgumentException("Mã nguyên liệu không được để trống");
        }
        if (dto.getGhiChu() == null || dto.getGhiChu().trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do kiểm kê điều chỉnh không được để trống");
        }
        if (dto.getSoLuongThucTe() == null || !Double.isFinite(dto.getSoLuongThucTe()) || dto.getSoLuongThucTe() < 0) {
            throw new IllegalArgumentException("Số lượng thực tế không hợp lệ");
        }

        NguyenLieu nl = nguyenLieuRepository.khoaNguyenLieu(dto.getMaNL())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu có mã: " + dto.getMaNL()));

        double tonTruoc = nl.getSoLuongTon() != null ? nl.getSoLuongTon() : 0.0;
        double soLuongThucTe = dto.getSoLuongThucTe();
        double delta = soLuongThucTe - tonTruoc;

        if (Math.abs(delta) < 0.0001) {
            throw new IllegalArgumentException("Tồn thực tế khớp hệ thống, không cần điều chỉnh");
        }

        nl.setSoLuongTon(soLuongThucTe);
        nguyenLieuRepository.save(nl);

        NhanVien nv = null;
        if (maNV != null) {
            nv = nhanVienRepository.findById(maNV).orElse(null);
        }
        if (nv == null) {
            nv = nhanVienRepository.findByVaiTro("QUAN_LY").stream().findFirst().orElse(null);
        }
        if (nv == null) {
            nv = nhanVienRepository.findAll().stream().findFirst().orElse(null);
        }

        double donGia = nl.getDonGiaNhap() != null ? nl.getDonGiaNhap() : 0.0;
        double thanhTien = BigDecimal.valueOf(Math.abs(delta)).multiply(BigDecimal.valueOf(donGia)).setScale(2, RoundingMode.HALF_UP).doubleValue();

        LichSuKho lsk = new LichSuKho();
        lsk.setNguyenLieu(nl);
        lsk.setNhanVien(nv);
        lsk.setLoaiGD("KIEM_KE_HAO_HUT");
        lsk.setSoLuongThayDoi(delta);
        lsk.setTonTruoc(tonTruoc);
        lsk.setTonSau(soLuongThucTe);
        lsk.setDonGia(donGia);
        lsk.setThanhTien(thanhTien);
        lsk.setGhiChu(dto.getGhiChu().trim());
        lsk.setThoiGian(LocalDateTime.now());
        lichSuKhoRepository.save(lsk);

        return nl;
    }

    public List<BangCongThucMonDTO> layBangCongThuc() {
        List<MonNuoc> monNuocs = monNuocRepository.findAll();
        monNuocs.sort(Comparator.comparing(MonNuoc::getMaMon));

        List<BangCongThucMonDTO> result = new ArrayList<>();
        for (MonNuoc mn : monNuocs) {
            BangCongThucMonDTO dto = new BangCongThucMonDTO(
                    mn.getMaMon(),
                    mn.getTenMon(),
                    mn.getDanhMuc(),
                    mn.getDonGia(),
                    mn.getHinhAnh(),
                    mn.getTrangThai()
            );

            List<CongThuc> recipes = congThucRepository.findByMonNuoc(mn);
            double tongGiaVon = 0.0;
            int soLyCoTheLam = 0;

            if (recipes.isEmpty()) {
                soLyCoTheLam = 0;
            } else {
                int minLy = Integer.MAX_VALUE;
                for (CongThuc ct : recipes) {
                    NguyenLieu nl = ct.getNguyenLieu();
                    double dinhMuc = ct.getDinhMuc() != null ? ct.getDinhMuc() : 0.0;
                    double donGiaNhap = (nl != null && nl.getDonGiaNhap() != null) ? nl.getDonGiaNhap() : 0.0;
                    double ton = (nl != null && nl.getSoLuongTon() != null) ? nl.getSoLuongTon() : 0.0;
                    double chiPhi = BigDecimal.valueOf(dinhMuc).multiply(BigDecimal.valueOf(donGiaNhap)).setScale(2, RoundingMode.HALF_UP).doubleValue();

                    tongGiaVon += chiPhi;

                    if (dinhMuc > 0) {
                        int lyTheoNL = (int) Math.floor(ton / dinhMuc);
                        if (lyTheoNL < minLy) {
                            minLy = lyTheoNL;
                        }
                    } else {
                        minLy = 0;
                    }

                    DinhMucNguyenLieuDTO itemDTO = new DinhMucNguyenLieuDTO(
                            ct.getMaCongThuc(),
                            nl != null ? nl.getMaNL() : null,
                            nl != null ? nl.getTenNL() : "",
                            nl != null ? nl.getDonViTinh() : "",
                            dinhMuc,
                            donGiaNhap,
                            chiPhi,
                            ton,
                            ct.getGhiChu()
                    );
                    dto.getDanhSachNguyenLieu().add(itemDTO);
                }
                soLyCoTheLam = (minLy == Integer.MAX_VALUE) ? 0 : Math.max(0, minLy);
            }

            dto.setGiaVonLy(BigDecimal.valueOf(tongGiaVon).setScale(2, RoundingMode.HALF_UP).doubleValue());
            double giaBan = mn.getDonGia() != null ? mn.getDonGia() : 0.0;
            dto.setLoiNhuanGopLy(BigDecimal.valueOf(giaBan - tongGiaVon).setScale(2, RoundingMode.HALF_UP).doubleValue());

            if (giaBan > 0) {
                double bien = ((giaBan - tongGiaVon) / giaBan) * 100.0;
                dto.setBienLoiNhuan(BigDecimal.valueOf(bien).setScale(1, RoundingMode.HALF_UP).doubleValue());
            } else {
                dto.setBienLoiNhuan(0.0);
            }

            dto.setSoLyCoTheLam(soLyCoTheLam);
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public CongThuc thietLapCongThuc(CongThucDTO dto) {
        if (dto.getDinhMuc() == null || dto.getDinhMuc() <= 0) {
            throw new IllegalArgumentException("Định mức công thức phải lớn hơn 0");
        }
        MonNuoc mn = monNuocRepository.findById(dto.getMaMon())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món nước: " + dto.getMaMon()));
        NguyenLieu nl = nguyenLieuRepository.findById(dto.getMaNL())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguyên liệu: " + dto.getMaNL()));

        Optional<CongThuc> existing = congThucRepository.findByMonNuocAndNguyenLieu(mn, nl);
        CongThuc ct;
        if (existing.isPresent()) {
            ct = existing.get();
            ct.setDinhMuc(dto.getDinhMuc());
            ct.setGhiChu(dto.getGhiChu());
        } else {
            ct = new CongThuc(null, mn, nl, dto.getDinhMuc(), dto.getGhiChu());
        }
        return congThucRepository.save(ct);
    }

    public List<CongThuc> layCongThucTheoMon(Integer maMon) {
        MonNuoc mn = monNuocRepository.findById(maMon)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món nước"));
        return congThucRepository.findByMonNuoc(mn);
    }

    public List<LichSuKho> layLichSuKho(String loaiGD, Integer maNL, String phamVi) {
        LocalDateTime tuNgay = xacDinhTuNgay(phamVi);
        return lichSuKhoRepository.traCuuLichSu(
                (loaiGD != null && !loaiGD.isBlank()) ? loaiGD.trim() : null,
                maNL,
                tuNgay
        );
    }

    public DashboardKpiDTO getDashboardKPIDTO(String phamVi) {
        String phamViChuan = chuanHoaPhamVi(phamVi);
        LocalDateTime tuNgay = xacDinhTuNgay(phamViChuan);
        LocalDateTime denNgay = (tuNgay != null) ? LocalDate.now().plusDays(1).atStartOfDay() : null;

        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.setPhamVi(phamViChuan);

        
        List<HoaDon> allHoaDon = hoaDonRepository.findByTrangThai("DA_THANH_TOAN");
        List<HoaDon> hoaDons = new ArrayList<>();
        for (HoaDon hd : allHoaDon) {
            LocalDateTime thoiGian = (hd.getThoiGianThanhToan() != null) ? hd.getThoiGianThanhToan() : hd.getThoiGianTao();
            if (thoiGian != null && tuNgay != null) {
                if (thoiGian.isBefore(tuNgay) || (denNgay != null && !thoiGian.isBefore(denNgay))) {
                    continue;
                }
            }
            hoaDons.add(hd);
        }

        double tongDoanhThu = 0.0;
        double tongChietKhau = 0.0;
        double doanhThuTienMat = 0.0;
        int soDonTienMat = 0;
        double doanhThuChuyenKhoan = 0.0;
        int soDonChuyenKhoan = 0;

        Map<Integer, TopMonBanChayDTO> topMap = new HashMap<>();

        for (HoaDon hd : hoaDons) {
            double tt = hd.getTongTienThanhToan() != null ? hd.getTongTienThanhToan() : 0.0;
            double ck = hd.getChietKhau() != null ? hd.getChietKhau() : 0.0;
            tongDoanhThu += tt;
            tongChietKhau += ck;

            String pt = hd.getPhuongThucTT();
            if ("TIEN_MAT".equalsIgnoreCase(pt)) {
                doanhThuTienMat += tt;
                soDonTienMat++;
            } else if ("CHUYEN_KHOAN".equalsIgnoreCase(pt) || "QR".equalsIgnoreCase(pt)) {
                doanhThuChuyenKhoan += tt;
                soDonChuyenKhoan++;
            }

            if (hd.getDanhSachChiTiet() != null) {
                for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                    if (ct.getMonNuoc() == null || ct.getSoLuong() == null || ct.getSoLuong() <= 0) continue;
                    Integer maMon = ct.getMonNuoc().getMaMon();
                    TopMonBanChayDTO item = topMap.computeIfAbsent(maMon, k -> new TopMonBanChayDTO(
                            maMon,
                            ct.getMonNuoc().getTenMon(),
                            ct.getMonNuoc().getDanhMuc(),
                            0,
                            0.0
                    ));
                    item.setTongSoLuong(item.getTongSoLuong() + ct.getSoLuong());
                    double dongia = ct.getDonGiaBan() != null ? ct.getDonGiaBan() : (ct.getMonNuoc().getDonGia() != null ? ct.getMonNuoc().getDonGia() : 0.0);
                    double thanhTienMon = ct.getThanhTien() != null ? ct.getThanhTien() : (dongia * ct.getSoLuong());
                    item.setTongDoanhThu(item.getTongDoanhThu() + thanhTienMon);
                }
            }
        }

        dto.setTongDoanhThu(BigDecimal.valueOf(tongDoanhThu).setScale(2, RoundingMode.HALF_UP).doubleValue());
        dto.setTongDonHang(hoaDons.size());
        dto.setTongChietKhau(BigDecimal.valueOf(tongChietKhau).setScale(2, RoundingMode.HALF_UP).doubleValue());
        dto.setDoanhThuTienMat(BigDecimal.valueOf(doanhThuTienMat).setScale(2, RoundingMode.HALF_UP).doubleValue());
        dto.setSoDonTienMat(soDonTienMat);
        dto.setDoanhThuChuyenKhoan(BigDecimal.valueOf(doanhThuChuyenKhoan).setScale(2, RoundingMode.HALF_UP).doubleValue());
        dto.setSoDonChuyenKhoan(soDonChuyenKhoan);

        
        List<TopMonBanChayDTO> topList = new ArrayList<>(topMap.values());
        topList.sort((a, b) -> {
            int cmp = Integer.compare(b.getTongSoLuong(), a.getTongSoLuong());
            if (cmp != 0) return cmp;
            return Double.compare(b.getTongDoanhThu(), a.getTongDoanhThu());
        });
        if (topList.size() > 5) {
            topList = topList.subList(0, 5);
        }
        dto.setTopMonBanChay(topList);

        
        List<LichSuKho> lskList;
        if (tuNgay != null) {
            lskList = lichSuKhoRepository.findByThoiGianGreaterThanEqual(tuNgay);
        } else {
            lskList = lichSuKhoRepository.findAll();
        }

        double chiPhiNhap = 0.0;
        double giaVonBanHang = 0.0;
        double giaTriHaoHut = 0.0;

        for (LichSuKho lsk : lskList) {
            LocalDateTime tg = lsk.getThoiGian();
            if (denNgay != null && tg != null && !tg.isBefore(denNgay)) {
                continue;
            }
            double tt = lsk.getThanhTien() != null ? lsk.getThanhTien() : 0.0;
            String loai = lsk.getLoaiGD();
            if ("NHAP_KHO".equalsIgnoreCase(loai)) {
                chiPhiNhap += tt;
            } else if ("XUAT_BAN_HANG".equalsIgnoreCase(loai)) {
                giaVonBanHang += tt;
            } else if ("KIEM_KE_HAO_HUT".equalsIgnoreCase(loai) && lsk.getSoLuongThayDoi() != null && lsk.getSoLuongThayDoi() < 0) {
                giaTriHaoHut += tt;
            }
        }

        dto.setChiPhiNhapKho(BigDecimal.valueOf(chiPhiNhap).setScale(2, RoundingMode.HALF_UP).doubleValue());
        dto.setGiaVonBanHang(BigDecimal.valueOf(giaVonBanHang).setScale(2, RoundingMode.HALF_UP).doubleValue());
        dto.setGiaTriHaoHut(BigDecimal.valueOf(giaTriHaoHut).setScale(2, RoundingMode.HALF_UP).doubleValue());

        double loiNhuanGop = tongDoanhThu - giaVonBanHang - giaTriHaoHut;
        dto.setLoiNhuanGop(BigDecimal.valueOf(loiNhuanGop).setScale(2, RoundingMode.HALF_UP).doubleValue());

        
        List<NguyenLieu> nguyenLieus = nguyenLieuRepository.findAllByOrderByMaNLAsc();
        dto.setTongNguyenLieu(nguyenLieus.size());

        double tongGiaTriTon = 0.0;
        List<NguyenLieu> sapHetList = new ArrayList<>();
        for (NguyenLieu nl : nguyenLieus) {
            double ton = nl.getSoLuongTon() != null ? nl.getSoLuongTon() : 0.0;
            double giaNhap = nl.getDonGiaNhap() != null ? nl.getDonGiaNhap() : 0.0;
            tongGiaTriTon += ton * giaNhap;
            if (nl.isSapHet()) {
                sapHetList.add(nl);
            }
        }

        dto.setTongGiaTriTonKho(BigDecimal.valueOf(tongGiaTriTon).setScale(2, RoundingMode.HALF_UP).doubleValue());
        dto.setSoNguyenLieuSapHet(sapHetList.size());
        dto.setDanhSachSapHet(sapHetList);

        return dto;
    }

    public Map<String, Object> getDashboardKPI() {
        DashboardKpiDTO dto = getDashboardKPIDTO("HOM_NAY");
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("phamVi", dto.getPhamVi());
        kpi.put("tongDoanhThu", dto.getTongDoanhThu());
        kpi.put("tongDonHang", dto.getTongDonHang());
        kpi.put("tongChietKhau", dto.getTongChietKhau());
        kpi.put("doanhThuTienMat", dto.getDoanhThuTienMat());
        kpi.put("soDonTienMat", dto.getSoDonTienMat());
        kpi.put("doanhThuChuyenKhoan", dto.getDoanhThuChuyenKhoan());
        kpi.put("soDonChuyenKhoan", dto.getSoDonChuyenKhoan());
        kpi.put("chiPhiNhapKho", dto.getChiPhiNhapKho());
        kpi.put("giaVonBanHang", dto.getGiaVonBanHang());
        kpi.put("giaTriHaoHut", dto.getGiaTriHaoHut());
        kpi.put("loiNhuanGop", dto.getLoiNhuanGop());
        kpi.put("tongGiaTriTonKho", dto.getTongGiaTriTonKho());
        kpi.put("tongNguyenLieu", dto.getTongNguyenLieu());
        kpi.put("soNguyenLieuSapHet", dto.getSoNguyenLieuSapHet());
        kpi.put("danhSachSapHet", dto.getDanhSachSapHet());
        kpi.put("topMonBanChay", dto.getTopMonBanChay());
        return kpi;
    }

    private String chuanHoaPhamVi(String phamVi) {
        if (phamVi == null || phamVi.isBlank()) return "HOM_NAY";
        String pv = phamVi.trim().toUpperCase(Locale.ROOT);
        if (pv.contains("HOM") || pv.contains("TODAY")) return "HOM_NAY";
        if (pv.contains("BAY") || pv.contains("7") || pv.contains("WEEK")) return "BAY_NGAY";
        if (pv.contains("THANG") || pv.contains("MONTH")) return "THANG_NAY";
        return "TAT_CA";
    }

    private LocalDateTime xacDinhTuNgay(String phamVi) {
        String pv = chuanHoaPhamVi(phamVi);
        return switch (pv) {
            case "HOM_NAY" -> LocalDate.now().atStartOfDay();
            case "BAY_NGAY" -> LocalDate.now().minusDays(6).atStartOfDay();
            case "THANG_NAY" -> LocalDate.now().withDayOfMonth(1).atStartOfDay();
            default -> null;
        };
    }
}
