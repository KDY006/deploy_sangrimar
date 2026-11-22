package com.cafe.service;

import com.cafe.dto.BanHienThiDTO;
import com.cafe.dto.ChiTietDonHangDTO;
import com.cafe.dto.DonHangDTO;
import com.cafe.dto.MonNuocPosDTO;
import com.cafe.dto.ThanhToanDTO;
import com.cafe.entity.Ban;
import com.cafe.entity.ChiTietHoaDon;
import com.cafe.entity.CongThuc;
import com.cafe.entity.HoaDon;
import com.cafe.entity.MonNuoc;
import com.cafe.entity.NhanVien;
import com.cafe.entity.NguyenLieu;
import com.cafe.repository.BanRepository;
import com.cafe.repository.CongThucRepository;
import com.cafe.repository.HoaDonRepository;
import com.cafe.repository.MonNuocRepository;
import com.cafe.repository.NguyenLieuRepository;
import com.cafe.repository.NhanVienRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
/** Dịch vụ bán hàng và quản lý đơn hàng POS */
public class DonHangService {

    public static final String TRANG_THAI_BAN_TRONG = "TRONG";
    public static final String TRANG_THAI_BAN_DANG_DUNG = "DANG_DUNG_NUOC";
    public static final String TRANG_THAI_BAN_CHO_TT = "CHO_THANH_TOAN";

    public static final String HD_DANG_PHUC_VU = "DANG_PHUC_VU";
    public static final String HD_CHO_THANH_TOAN = "CHO_THANH_TOAN";
    public static final String HD_DA_THANH_TOAN = "DA_THANH_TOAN";
    public static final String HD_DA_HUY = "DA_HUY";

    public static final List<String> OPEN_STATUSES = List.of(HD_DANG_PHUC_VU, HD_CHO_THANH_TOAN, "CHO_THANH_TOAN");
    public static final String OPEN = "CHO_THANH_TOAN";

    private final HoaDonRepository hoaDonRepository;
    private final BanRepository banRepository;
    private final MonNuocRepository monNuocRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhoService khoService;
    private final CongThucRepository congThucRepository;
    private final NguyenLieuRepository nguyenLieuRepository;

    public DonHangService(HoaDonRepository hoaDonRepository,
                          BanRepository banRepository,
                          MonNuocRepository monNuocRepository,
                          NhanVienRepository nhanVienRepository,
                          KhoService khoService,
                          CongThucRepository congThucRepository,
                          NguyenLieuRepository nguyenLieuRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.banRepository = banRepository;
        this.monNuocRepository = monNuocRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.khoService = khoService;
        this.congThucRepository = congThucRepository;
        this.nguyenLieuRepository = nguyenLieuRepository;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public List<Ban> layDanhSachBan() {
        return banRepository.findAllByOrderByMaBanAsc();
    }

    public List<BanHienThiDTO> layDanhSachBanHienThi() {
        List<Ban> list = banRepository.findAllByOrderByMaBanAsc();
        List<BanHienThiDTO> result = new ArrayList<>();
        for (Ban b : list) {
            Optional<HoaDon> opt = hoaDonRepository.findFirstByBanAndTrangThaiInOrderByMaHoaDonDesc(b, OPEN_STATUSES);
            Integer maHD = null;
            Double tongTien = 0.0;
            LocalDateTime thoiGian = null;
            if (opt.isPresent()) {
                HoaDon hd = opt.get();
                maHD = hd.getMaHoaDon();
                tongTien = hd.getTongTienThanhToan() != null && hd.getTongTienThanhToan() > 0
                        ? hd.getTongTienThanhToan()
                        : hd.getTongTienGoc();
                thoiGian = hd.getThoiGianTao();
            }
            result.add(new BanHienThiDTO(
                    b.getMaBan(),
                    b.getTenBan(),
                    b.getViTri(),
                    b.getSoGhe(),
                    b.getTrangThai(),
                    maHD,
                    tongTien,
                    thoiGian
            ));
        }
        return result;
    }

    public List<MonNuoc> layDanhSachMonNuoc() {
        return layDanhSachMonNuoc(null, null);
    }

    public List<MonNuoc> layDanhSachMonNuoc(String danhMuc, String q) {
        String search = (q == null) ? "" : q.strip().toLowerCase(Locale.ROOT);
        return monNuocRepository.findByTrangThai("DANG_BAN").stream()
                .filter(m -> danhMuc == null || danhMuc.isBlank() || danhMuc.equals(m.getDanhMuc()))
                .filter(m -> m.getTenMon().toLowerCase(Locale.ROOT).contains(search))
                .toList();
    }

    public List<MonNuocPosDTO> layDanhSachMonNuocPos(String danhMuc, String q, Integer boQuaHoaDonId) {
        String search = (q == null) ? "" : q.strip().toLowerCase(Locale.ROOT);
        List<MonNuoc> list = monNuocRepository.findByTrangThai("DANG_BAN").stream()
                .filter(m -> danhMuc == null || danhMuc.isBlank() || danhMuc.equals(m.getDanhMuc()))
                .filter(m -> m.getTenMon().toLowerCase(Locale.ROOT).contains(search))
                .toList();

        
        Map<Integer, Double> daGiuCho = new HashMap<>();
        List<HoaDon> openOrders = hoaDonRepository.findAll().stream()
                .filter(hd -> OPEN_STATUSES.contains(hd.getTrangThai()))
                .filter(hd -> boQuaHoaDonId == null || !Objects.equals(hd.getMaHoaDon(), boQuaHoaDonId))
                .toList();

        for (HoaDon hd : openOrders) {
            for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                if (ct.getMonNuoc() == null || ct.getSoLuong() == null) continue;
                List<CongThuc> boms = congThucRepository.findByMonNuoc(ct.getMonNuoc());
                for (CongThuc bm : boms) {
                    if (bm.getNguyenLieu() == null || bm.getDinhMuc() == null) continue;
                    double amount = bm.getDinhMuc() * ct.getSoLuong();
                    daGiuCho.merge(bm.getNguyenLieu().getMaNL(), amount, Double::sum);
                }
            }
        }

        
        List<NguyenLieu> nguyenLieus = nguyenLieuRepository.findAll();
        Map<Integer, Double> tonKhaDungMap = new HashMap<>();
        for (NguyenLieu nl : nguyenLieus) {
            double ton = nl.getSoLuongTon() != null ? nl.getSoLuongTon() : 0.0;
            double reserved = daGiuCho.getOrDefault(nl.getMaNL(), 0.0);
            tonKhaDungMap.put(nl.getMaNL(), Math.max(0.0, ton - reserved));
        }

        
        List<MonNuocPosDTO> result = new ArrayList<>();
        for (MonNuoc m : list) {
            List<CongThuc> boms = congThucRepository.findByMonNuoc(m);
            int soLy;
            if (boms.isEmpty()) {
                soLy = 0; 
            } else {
                int minLy = Integer.MAX_VALUE;
                for (CongThuc bm : boms) {
                    if (bm.getNguyenLieu() == null || bm.getDinhMuc() == null || bm.getDinhMuc() <= 0) {
                        minLy = 0;
                        break;
                    }
                    double khaDung = tonKhaDungMap.getOrDefault(bm.getNguyenLieu().getMaNL(), 0.0);
                    int lyTheoNL = (int) Math.floor(khaDung / bm.getDinhMuc());
                    if (lyTheoNL < minLy) {
                        minLy = lyTheoNL;
                    }
                }
                soLy = Math.max(0, minLy == Integer.MAX_VALUE ? 0 : minLy);
            }

            result.add(new MonNuocPosDTO(
                    m.getMaMon(),
                    m.getTenMon(),
                    m.getDanhMuc(),
                    m.getDonGia(),
                    m.getHinhAnh(),
                    m.getTrangThai(),
                    soLy
            ));
        }

        return result;
    }

    public List<HoaDon> layDanhSachHoaDon() {
        return hoaDonRepository.findAllByOrderByMaHoaDonDesc();
    }

    public HoaDon layHoaDonTheoId(Integer id) {
        HoaDon h = hoaDonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));
        h.getDanhSachChiTiet().size(); 
        return h;
    }

    public HoaDon layDonTheoBan(Integer id) {
        Ban b = banRepository.findById(id).orElseThrow(() -> bad("Không tìm thấy bàn"));
        return hoaDonRepository.findFirstByBanAndTrangThaiInOrderByMaHoaDonDesc(b, OPEN_STATUSES)
                .orElse(null);
    }

    public HoaDon layDonTheoYeuCau(String request) {
        return hoaDonRepository.findByYeuCauTao(validateKey(request)).orElse(null);
    }

    private Ban khoaBan(Integer id) {
        return banRepository.khoaBan(id).orElseThrow(() -> bad("Không tìm thấy bàn"));
    }

    private HoaDon khoaDon(Integer id) {
        return hoaDonRepository.khoaHoaDon(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));
    }

    private void kiemTraDonMo(HoaDon h) {
        if (!HD_DANG_PHUC_VU.equals(h.getTrangThai()) &&
                !HD_CHO_THANH_TOAN.equals(h.getTrangThai()) &&
                !"CHO_THANH_TOAN".equals(h.getTrangThai())) {
            throw conflict("Chỉ được thao tác đơn đang phục vụ hoặc chờ thanh toán");
        }
    }

    private void kiemTraPhien(HoaDon h, Long version) {
        if (version == null || !Objects.equals(h.getPhienBan(), version)) {
            throw conflict("Đơn đã thay đổi ở phiên khác. Hãy tải lại đơn trước khi tiếp tục.");
        }
    }

    private String validateKey(String key) {
        try {
            return UUID.fromString(key).toString();
        } catch (Exception e) {
            throw bad("Thiếu mã yêu cầu hợp lệ; tải lại giao diện");
        }
    }

    private BigDecimal validateTien(Double value, String label) {
        if (value == null || !Double.isFinite(value) || value < 0 || value > 1_000_000_000_000d || value != Math.rint(value)) {
            throw bad(label + " phải là số đồng nguyên không âm (tối đa 1.000 tỷ)");
        }
        return BigDecimal.valueOf(value);
    }

    private List<ChiTietHoaDon> chuanBiMon(DonHangDTO dto, HoaDon owner) {
        if (dto.getDanhSachMon() == null || dto.getDanhSachMon().isEmpty() || dto.getDanhSachMon().size() > 100) {
            throw bad("Đơn phải có từ 1 đến 100 dòng món");
        }
        List<ChiTietHoaDon> out = new ArrayList<>();
        for (ChiTietDonHangDTO item : dto.getDanhSachMon()) {
            if (item == null || item.getMaMon() == null || item.getSoLuong() == null || item.getSoLuong() < 1 || item.getSoLuong() > 99) {
                throw bad("Món không hợp lệ hoặc số lượng ngoài 1–99");
            }
            String note = (item.getGhiChu() == null) ? "" : item.getGhiChu().strip();
            if (note.length() > 255) {
                throw bad("Ghi chú tối đa 255 ký tự");
            }
            MonNuoc m = monNuocRepository.findById(item.getMaMon())
                    .orElseThrow(() -> bad("Không tìm thấy món"));
            if (!"DANG_BAN".equals(m.getTrangThai())) {
                throw bad("Món đã ngừng bán: " + m.getTenMon());
            }

            Double price = owner.getDanhSachChiTiet().stream()
                    .filter(l -> l.getMonNuoc().getMaMon().equals(m.getMaMon()))
                    .map(ChiTietHoaDon::getDonGiaBan)
                    .findFirst()
                    .orElse(m.getDonGia());

            BigDecimal amount = validateTien(price, "Đơn giá").multiply(BigDecimal.valueOf(item.getSoLuong()));
            out.add(new ChiTietHoaDon(null, owner, m, item.getSoLuong(), item.getSoLuong(), price, amount.doubleValue(), note));
        }
        return out;
    }

    private void kiemTraNhuCauNguyenLieu(List<ChiTietHoaDon> items, Integer boQuaHoaDonId) {
        Map<Integer, Double> incoming = new HashMap<>();
        for (ChiTietHoaDon ct : items) {
            if (ct.getMonNuoc() == null || ct.getSoLuong() == null) continue;
            List<CongThuc> boms = congThucRepository.findByMonNuoc(ct.getMonNuoc());
            for (CongThuc bm : boms) {
                if (bm.getNguyenLieu() == null || bm.getDinhMuc() == null) continue;
                double amount = bm.getDinhMuc() * ct.getSoLuong();
                incoming.merge(bm.getNguyenLieu().getMaNL(), amount, Double::sum);
            }
        }

        List<HoaDon> openOrders = hoaDonRepository.findAll().stream()
                .filter(hd -> OPEN_STATUSES.contains(hd.getTrangThai()))
                .filter(hd -> boQuaHoaDonId == null || !Objects.equals(hd.getMaHoaDon(), boQuaHoaDonId))
                .toList();

        Map<Integer, Double> others = new HashMap<>();
        for (HoaDon hd : openOrders) {
            for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                if (ct.getMonNuoc() == null || ct.getSoLuong() == null) continue;
                List<CongThuc> boms = congThucRepository.findByMonNuoc(ct.getMonNuoc());
                for (CongThuc bm : boms) {
                    if (bm.getNguyenLieu() == null || bm.getDinhMuc() == null) continue;
                    double amount = bm.getDinhMuc() * ct.getSoLuong();
                    others.merge(bm.getNguyenLieu().getMaNL(), amount, Double::sum);
                }
            }
        }

        for (Map.Entry<Integer, Double> entry : incoming.entrySet()) {
            Integer maNL = entry.getKey();
            double totalDemand = entry.getValue() + others.getOrDefault(maNL, 0.0);
            NguyenLieu nl = nguyenLieuRepository.findById(maNL).orElse(null);
            if (nl != null) {
                double ton = nl.getSoLuongTon() != null ? nl.getSoLuongTon() : 0.0;
                if (totalDemand > ton) {
                    throw bad("Số lượng món vượt quá khả năng phục vụ của quán hiện tại. Vui lòng kiểm tra lại số ly còn lại.");
                }
            }
        }
    }

    private void thayMon(HoaDon h, List<ChiTietHoaDon> lines) {
        h.getDanhSachChiTiet().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (ChiTietHoaDon line : lines) {
            h.addChiTiet(line);
            total = total.add(BigDecimal.valueOf(line.getThanhTien()));
        }
        if (total.compareTo(BigDecimal.valueOf(1_000_000_000_000L)) > 0) {
            throw bad("Tổng đơn vượt giới hạn");
        }
        h.setTongTienGoc(total.doubleValue());
        h.setChietKhau(0d);
        h.setTongTienThanhToan(total.doubleValue());
    }

    @Transactional(rollbackFor = Exception.class)
    public HoaDon taoDonHang(DonHangDTO dto) {
        String request = validateKey(dto.getYeuCauTao());
        Optional<HoaDon> previous = hoaDonRepository.findByYeuCauTao(request);
        if (previous.isPresent()) {
            if (!Objects.equals(previous.get().getNhanVien().getMaNV(), dto.getMaNV())) {
                throw conflict("Mã yêu cầu thuộc nhân viên khác");
            }
            return previous.get();
        }

        if (!"TAI_BAN".equals(dto.getLoaiDon()) && !"MANG_DI".equals(dto.getLoaiDon())) {
            throw bad("Loại đơn không hợp lệ");
        }

        Ban ban = null;
        if ("TAI_BAN".equals(dto.getLoaiDon())) {
            if (dto.getMaBan() == null) {
                throw bad("Đơn tại bàn phải chọn bàn");
            }
            ban = khoaBan(dto.getMaBan());
            if (hoaDonRepository.findFirstByBanAndTrangThaiInOrderByMaHoaDonDesc(ban, OPEN_STATUSES).isPresent()) {
                throw conflict("Bàn đã có đơn mở. Hãy mở lại đơn của bàn.");
            }
        } else if (dto.getMaBan() != null) {
            throw bad("Đơn mang đi không gắn bàn");
        }

        NhanVien nv = nhanVienRepository.findById(dto.getMaNV())
                .orElseThrow(() -> bad("Không tìm thấy nhân viên"));
        if (!"DANG_LAM".equals(nv.getTrangThai())) {
            throw bad("Nhân viên đã ngừng làm việc");
        }

        HoaDon h = new HoaDon();
        h.setBan(ban);
        h.setNhanVien(nv);
        h.setLoaiDon(dto.getLoaiDon());
        h.setTrangThai(HD_DANG_PHUC_VU);
        h.setYeuCauTao(request);
        h.setThoiGianTao(LocalDateTime.now());

        List<ChiTietHoaDon> lines = chuanBiMon(dto, h);
        kiemTraNhuCauNguyenLieu(lines, null);
        thayMon(h, lines);

        if (ban != null) {
            ban.setTrangThai(TRANG_THAI_BAN_DANG_DUNG);
            banRepository.save(ban);
        }
        h.setPhieuInBar(new ArrayList<>(h.getDanhSachChiTiet()));
        return hoaDonRepository.saveAndFlush(h);
    }

    @Transactional(rollbackFor = Exception.class)
    public HoaDon capNhatDon(Integer id, DonHangDTO dto) {
        HoaDon h = khoaDon(id);
        kiemTraDonMo(h);
        if (HD_CHO_THANH_TOAN.equals(h.getTrangThai())) {
            throw conflict("Bàn đang ở trạng thái Chờ thanh toán. Vui lòng bấm 'Quay lại phục vụ' trước khi gọi thêm món.");
        }
        kiemTraPhien(h, dto.getPhienBan());

        if (!Objects.equals(h.getLoaiDon(), dto.getLoaiDon()) ||
                !Objects.equals(h.getBan() == null ? null : h.getBan().getMaBan(), dto.getMaBan())) {
            throw bad("Không đổi bàn/loại đơn khi cập nhật món");
        }

        
        Map<Integer, Integer> daInBarMap = new HashMap<>();
        for (ChiTietHoaDon ct : h.getDanhSachChiTiet()) {
            if (ct.getMonNuoc() != null) {
                daInBarMap.put(ct.getMonNuoc().getMaMon(), ct.getSoLuongDaInBar() != null ? ct.getSoLuongDaInBar() : 0);
            }
        }

        List<ChiTietHoaDon> lines = chuanBiMon(dto, h);
        kiemTraNhuCauNguyenLieu(lines, id);

        
        List<ChiTietHoaDon> boSung = new ArrayList<>();
        for (ChiTietHoaDon line : lines) {
            int prevPrinted = daInBarMap.getOrDefault(line.getMonNuoc().getMaMon(), 0);
            int delta = line.getSoLuong() - prevPrinted;
            if (delta > 0) {
                ChiTietHoaDon itemBoSung = new ChiTietHoaDon(null, h, line.getMonNuoc(), delta, delta,
                        line.getDonGiaBan(), line.getDonGiaBan() * delta, line.getGhiChu());
                boSung.add(itemBoSung);
            }
            line.setSoLuongDaInBar(line.getSoLuong());
        }

        thayMon(h, lines);
        h.setPhieuInBarBoSung(boSung);
        return hoaDonRepository.saveAndFlush(h);
    }

    @Transactional(rollbackFor = Exception.class)
    public HoaDon yeuCauThanhToan(Integer id, Long phienBan) {
        HoaDon h = khoaDon(id);
        kiemTraDonMo(h);
        kiemTraPhien(h, phienBan);

        h.setTrangThai(HD_CHO_THANH_TOAN);
        h.setThoiGianYeuCauTT(LocalDateTime.now());

        if (h.getBan() != null) {
            Ban b = khoaBan(h.getBan().getMaBan());
            b.setTrangThai(TRANG_THAI_BAN_CHO_TT);
            banRepository.save(b);
        }
        return hoaDonRepository.saveAndFlush(h);
    }

    @Transactional(rollbackFor = Exception.class)
    public HoaDon quayLaiPhucVu(Integer id, Long phienBan) {
        HoaDon h = khoaDon(id);
        kiemTraPhien(h, phienBan);

        h.setTrangThai(HD_DANG_PHUC_VU);

        if (h.getBan() != null) {
            Ban b = khoaBan(h.getBan().getMaBan());
            b.setTrangThai(TRANG_THAI_BAN_DANG_DUNG);
            banRepository.save(b);
        }
        return hoaDonRepository.saveAndFlush(h);
    }

    @Transactional(rollbackFor = Exception.class)
    public HoaDon thanhToanDonHang(Integer id, ThanhToanDTO dto) {
        return thanhToanDonHang(id, dto, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public HoaDon thanhToanDonHang(Integer id, ThanhToanDTO dto, Integer maNVThuNgan) {
        HoaDon h = khoaDon(id);
        String request = validateKey(dto.getYeuCauThanhToan());

        if (HD_DA_THANH_TOAN.equals(h.getTrangThai()) && request.equals(h.getYeuCauThanhToan())) {
            return h;
        }

        kiemTraDonMo(h);
        kiemTraPhien(h, dto.getPhienBan());

        if (h.getDanhSachChiTiet().isEmpty()) {
            throw bad("Không thanh toán đơn trống");
        }

        BigDecimal discount = validateTien(dto.getChietKhau() == null ? 0d : dto.getChietKhau(), "Chiết khấu");
        BigDecimal total = validateTien(h.getTongTienGoc(), "Tổng tiền");
        if (discount.compareTo(total) > 0) {
            throw bad("Chiết khấu không được lớn hơn tổng đơn");
        }
        total = total.subtract(discount);

        String method = dto.getPhuongThucTT();
        double received;
        double change = 0.0;
        if ("TIEN_MAT".equals(method)) {
            BigDecimal cash = validateTien(dto.getTienKhachDua(), "Tiền khách đưa");
            if (cash.compareTo(total) < 0) {
                throw bad("Tiền khách đưa chưa đủ");
            }
            received = cash.doubleValue();
            change = cash.subtract(total).doubleValue();
        } else if ("CHUYEN_KHOAN".equals(method)) {
            String ref = (dto.getMaThamChieu() == null) ? "" : dto.getMaThamChieu().strip();
            if (!Boolean.TRUE.equals(dto.getXacNhanChuyenKhoan()) || ref.isEmpty() || ref.length() > 100) {
                throw bad("Cần kiểm tra tiền vào tài khoản và nhập mã tham chiếu (tối đa 100 ký tự)");
            }
            if (hoaDonRepository.existsByMaThamChieu(ref)) {
                throw conflict("Mã tham chiếu chuyển khoản đã dùng");
            }
            h.setMaThamChieu(ref);
            h.setMinhChungChuyenKhoan(dto.getMinhChungChuyenKhoan());
            received = total.doubleValue();
            change = 0.0;
        } else {
            throw bad("Phương thức thanh toán không hợp lệ");
        }

        
        if (maNVThuNgan != null) {
            nhanVienRepository.findById(maNVThuNgan).ifPresent(h::setNhanVienThuNgan);
        }

        
        khoService.truKhoTuDong(h, h.getNhanVienThuNgan());

        h.setChietKhau(discount.doubleValue());
        h.setTongTienThanhToan(total.doubleValue());
        h.setPhuongThucTT(method);
        h.setTienKhachDua(received);
        h.setTienThua(change);
        h.setTrangThai(HD_DA_THANH_TOAN);
        h.setThoiGianThanhToan(LocalDateTime.now());
        h.setYeuCauThanhToan(request);

        traBan(h);
        return hoaDonRepository.saveAndFlush(h);
    }

    private void traBan(HoaDon h) {
        if (h.getBan() != null) {
            Ban b = khoaBan(h.getBan().getMaBan());
            boolean coDonKhac = hoaDonRepository.findFirstByBanAndTrangThaiInOrderByMaHoaDonDesc(b, OPEN_STATUSES).isPresent();
            b.setTrangThai(coDonKhac ? TRANG_THAI_BAN_DANG_DUNG : TRANG_THAI_BAN_TRONG);
            banRepository.save(b);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public HoaDon huyDonHang(Integer id, Long version) {
        HoaDon h = khoaDon(id);
        if (HD_DA_HUY.equals(h.getTrangThai())) {
            return h;
        }
        kiemTraDonMo(h);
        kiemTraPhien(h, version);
        h.setTrangThai(HD_DA_HUY);
        traBan(h);
        return hoaDonRepository.saveAndFlush(h);
    }
}
