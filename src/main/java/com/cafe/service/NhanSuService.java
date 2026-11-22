package com.cafe.service;

import com.cafe.dto.*;
import com.cafe.entity.CaLam;
import com.cafe.entity.ChamCong;
import com.cafe.entity.NhanVien;
import com.cafe.entity.PhanCa;
import com.cafe.repository.CaLamRepository;
import com.cafe.repository.ChamCongRepository;
import com.cafe.repository.NhanVienRepository;
import com.cafe.repository.PhanCaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
/** Dịch vụ quản lý nhân sự, ca làm và chấm công */
public class NhanSuService {

    private final NhanVienRepository nhanVienRepository;
    private final CaLamRepository caLamRepository;
    private final ChamCongRepository chamCongRepository;
    private final PhanCaRepository phanCaRepository;
    private Clock clock;

    @Autowired
    public NhanSuService(NhanVienRepository nhanVienRepository,
                         CaLamRepository caLamRepository,
                         ChamCongRepository chamCongRepository,
                         PhanCaRepository phanCaRepository,
                         Clock clock) {
        this.nhanVienRepository = nhanVienRepository;
        this.caLamRepository = caLamRepository;
        this.chamCongRepository = chamCongRepository;
        this.phanCaRepository = phanCaRepository;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    public void setClock(Clock clock) {
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    public Clock getClock() {
        return clock;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public NhanVien dangNhap(DangNhapDTO dto) {
        if (dto == null || dto.getTenDangNhap() == null || dto.getMatKhau() == null) {
            throw bad("Thiếu tên đăng nhập hoặc mật khẩu");
        }
        return nhanVienRepository.findByTenDangNhapAndMatKhau(dto.getTenDangNhap(), dto.getMatKhau())
                .orElseThrow(() -> new RuntimeException("Sai tên đăng nhập hoặc mật khẩu!"));
    }

    public List<NhanVien> layDanhSachNhanVien() {
        return nhanVienRepository.findAll();
    }

    @Transactional
    public NhanVien themNhanVien(NhanVien nv) {
        if (nv == null) {
            throw bad("Thiếu dữ liệu nhân viên");
        }
        chuanHoaNhanVien(nv, true);
        if (nhanVienRepository.findByTenDangNhap(nv.getTenDangNhap()).isPresent()) {
            throw conflict("Tên đăng nhập đã tồn tại");
        }
        return nhanVienRepository.save(nv);
    }

    @Transactional
    public NhanVien capNhatNhanVien(Integer maNV, NhanVien changes) {
        return capNhatNhanVien(maNV, changes, null);
    }

    @Transactional
    public NhanVien capNhatNhanVien(Integer maNV, NhanVien changes, Integer callerId) {
        NhanVien current = nhanVienRepository.findById(maNV)
                .orElseThrow(() -> bad("Không tìm thấy nhân viên"));
        if (changes == null) {
            throw bad("Thiếu dữ liệu nhân viên");
        }

        
        if (callerId != null && callerId.equals(maNV)) {
            if ("DA_NGHI".equals(changes.getTrangThai())) {
                throw bad("Không thể ngừng chính tài khoản đang đăng nhập");
            }
            if (!"QUAN_LY".equals(changes.getVaiTro()) && !"MANAGER".equals(changes.getVaiTro()) && !"ADMIN".equals(changes.getVaiTro())) {
                throw bad("Không thể tự hạ vai trò Quản lý của chính mình");
            }
        }

        
        boolean willBeInactiveOrNotManager = "DA_NGHI".equals(changes.getTrangThai()) ||
                (!"QUAN_LY".equals(changes.getVaiTro()) && !"MANAGER".equals(changes.getVaiTro()) && !"ADMIN".equals(changes.getVaiTro()));
        if (willBeInactiveOrNotManager && ("QUAN_LY".equals(current.getVaiTro()) || "MANAGER".equals(current.getVaiTro()) || "ADMIN".equals(current.getVaiTro()))) {
            long remainingActiveManagers = nhanVienRepository.findAll().stream()
                    .filter(u -> !u.getMaNV().equals(maNV))
                    .filter(u -> "DANG_LAM".equals(u.getTrangThai()))
                    .filter(u -> "QUAN_LY".equals(u.getVaiTro()) || "MANAGER".equals(u.getVaiTro()) || "ADMIN".equals(u.getVaiTro()))
                    .count();
            if (remainingActiveManagers == 0) {
                throw bad("Hệ thống phải có ít nhất 1 Quản lý đang hoạt động");
            }
        }

        current.setHoTen(changes.getHoTen());
        current.setSoDienThoai(changes.getSoDienThoai());
        current.setTenDangNhap(changes.getTenDangNhap());
        if (changes.getMatKhau() != null && !changes.getMatKhau().isBlank()) {
            current.setMatKhau(changes.getMatKhau());
        }
        current.setVaiTro(changes.getVaiTro());
        current.setTrangThai(changes.getTrangThai());
        chuanHoaNhanVien(current, false);
        nhanVienRepository.findByTenDangNhap(current.getTenDangNhap())
                .filter(other -> !other.getMaNV().equals(maNV))
                .ifPresent(other -> { throw conflict("Tên đăng nhập đã tồn tại"); });
        return nhanVienRepository.save(current);
    }

    @Transactional
    public NhanVien ngungLamViec(Integer maNV) {
        return ngungLamViec(maNV, null);
    }

    @Transactional
    public NhanVien ngungLamViec(Integer maNV, Integer callerId) {
        if (callerId != null && callerId.equals(maNV)) {
            throw bad("Không thể ngừng chính tài khoản đang đăng nhập");
        }
        NhanVien nv = nhanVienRepository.findById(maNV)
                .orElseThrow(() -> bad("Không tìm thấy nhân viên"));

        if ("QUAN_LY".equals(nv.getVaiTro()) || "MANAGER".equals(nv.getVaiTro()) || "ADMIN".equals(nv.getVaiTro())) {
            long remainingActiveManagers = nhanVienRepository.findAll().stream()
                    .filter(u -> !u.getMaNV().equals(maNV))
                    .filter(u -> "DANG_LAM".equals(u.getTrangThai()))
                    .filter(u -> "QUAN_LY".equals(u.getVaiTro()) || "MANAGER".equals(u.getVaiTro()) || "ADMIN".equals(u.getVaiTro()))
                    .count();
            if (remainingActiveManagers == 0) {
                throw bad("Hệ thống phải có ít nhất 1 Quản lý đang hoạt động");
            }
        }

        nv.setTrangThai("DA_NGHI");
        return nhanVienRepository.save(nv);
    }

    public List<CaLam> layDanhSachCaLam() {
        return caLamRepository.findAllByOrderByGioBatDauAsc();
    }

    
    
    

    public List<CaHomNayDTO> layCaHomNay(Integer maNV) {
        LocalDate currentDay = today();
        LocalDateTime currentTime = now();
        List<PhanCa> danhSachPhanCa = phanCaRepository.findByNhanVien_MaNVAndNgayLam(maNV, currentDay);
        List<CaHomNayDTO> result = new ArrayList<>();

        for (PhanCa pc : danhSachPhanCa) {
            CaHomNayDTO dto = new CaHomNayDTO();
            dto.setMaPhanCa(pc.getMaPhanCa());
            dto.setMaCa(pc.getCaLam().getMaCa());
            dto.setTenCa(pc.getCaLam().getTenCa());
            dto.setGioBatDau(pc.getCaLam().getGioBatDau());
            dto.setGioKetThuc(pc.getCaLam().getGioKetThuc());
            dto.setHeSoLuong(pc.getCaLam().getHeSoLuong());

            Optional<ChamCong> ccOpt = chamCongRepository.findByPhanCa_MaPhanCa(pc.getMaPhanCa());
            if (ccOpt.isEmpty()) {
                ccOpt = chamCongRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(maNV, pc.getCaLam().getMaCa(), currentDay);
            }

            LocalDateTime startWindow = LocalDateTime.of(currentDay, pc.getCaLam().getGioBatDau()).minusMinutes(30);
            LocalDateTime endWindow = LocalDateTime.of(currentDay, pc.getCaLam().getGioKetThuc());

            if (ccOpt.isEmpty()) {
                dto.setTrangThai("CHUA_CHECK_IN");
                dto.setCoTheCheckOut(false);
                if (currentTime.isBefore(startWindow)) {
                    dto.setCoTheCheckIn(false);
                    dto.setThongBaoCuaSo("Chưa mở cửa sổ check-in (mở từ " + pc.getCaLam().getGioBatDau().minusMinutes(30).toString().substring(0, 5) + ")");
                } else if (currentTime.isBefore(endWindow)) {
                    dto.setCoTheCheckIn(true);
                    dto.setThongBaoCuaSo("Đang trong cửa sổ check-in (đến " + pc.getCaLam().getGioKetThuc().toString().substring(0, 5) + ")");
                } else {
                    dto.setCoTheCheckIn(false);
                    dto.setThongBaoCuaSo("Đã quá giờ check-in (" + pc.getCaLam().getGioKetThuc().toString().substring(0, 5) + ")");
                }
            } else {
                ChamCong cc = ccOpt.get();
                dto.setMaChamCong(cc.getMaChamCong());
                dto.setTrangThai(cc.getTrangThai());
                dto.setGioCheckIn(cc.getGioCheckIn());
                dto.setGioCheckOut(cc.getGioCheckOut());
                dto.setSoPhutTre(cc.getSoPhutTre());
                dto.setSoPhutVeSom(cc.getSoPhutVeSom());
                dto.setSoGioLam(cc.getSoGioLam());
                dto.setGioCongQuyDoi(cc.getGioCongQuyDoi());
                dto.setGhiChu(cc.getGhiChu());
                dto.setCoTheCheckIn(false);

                if (cc.getGioCheckIn() != null && cc.getGioCheckOut() == null && !"VANG".equals(cc.getTrangThai()) && !"QUEN_CHECK_OUT".equals(cc.getTrangThai())) {
                    long sec = Duration.between(cc.getGioCheckIn(), currentTime).toSeconds();
                    if (sec >= 60) {
                        dto.setCoTheCheckOut(true);
                        dto.setThongBaoCuaSo("Đang trong ca làm việc");
                    } else {
                        dto.setCoTheCheckOut(false);
                        dto.setThongBaoCuaSo("Chưa đủ 1 phút sau khi check-in");
                    }
                } else {
                    dto.setCoTheCheckOut(false);
                    if ("VANG".equals(cc.getTrangThai())) {
                        dto.setThongBaoCuaSo("Đã chốt vắng mặt");
                    } else if ("QUEN_CHECK_OUT".equals(cc.getTrangThai())) {
                        dto.setThongBaoCuaSo("Quên check-out");
                    } else {
                        dto.setThongBaoCuaSo("Đã hoàn tất ca làm");
                    }
                }
            }
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public ChamCong checkIn(Integer maNhanVienDangNhap, ChamCongDTO dto) {
        if (dto == null) {
            throw bad("Thiếu dữ liệu chấm công");
        }
        if (dto.getMaNV() != null && !dto.getMaNV().equals(maNhanVienDangNhap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ được chấm công cho chính mình");
        }

        PhanCa pc;
        if (dto.getMaPhanCa() != null) {
            pc = phanCaRepository.findById(dto.getMaPhanCa())
                    .orElseThrow(() -> bad("Không tìm thấy phân ca: " + dto.getMaPhanCa()));
            if (!pc.getNhanVien().getMaNV().equals(maNhanVienDangNhap)) {
                throw bad("Bạn chưa được phân ca này hôm nay");
            }
        } else if (dto.getMaCa() != null) {
            pc = phanCaRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(maNhanVienDangNhap, dto.getMaCa(), today())
                    .orElseThrow(() -> bad("Bạn chưa được phân ca này hôm nay"));
        } else {
            throw bad("Hãy chọn ca làm việc");
        }

        NhanVien nv = pc.getNhanVien();
        if (!"DANG_LAM".equals(nv.getTrangThai())) {
            throw bad("Tài khoản đã ngừng làm việc");
        }

        
        if (chamCongRepository.findByPhanCa_MaPhanCa(pc.getMaPhanCa()).isPresent() ||
                chamCongRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(nv.getMaNV(), pc.getCaLam().getMaCa(), pc.getNgayLam()).isPresent()) {
            throw conflict("Ca này đã được check-in");
        }

        LocalDateTime currentTime = now();
        LocalDateTime startWindow = LocalDateTime.of(pc.getNgayLam(), pc.getCaLam().getGioBatDau()).minusMinutes(30);
        LocalDateTime endWindow = LocalDateTime.of(pc.getNgayLam(), pc.getCaLam().getGioKetThuc());

        
        if (currentTime.isBefore(startWindow)) {
            throw bad("Chưa mở cửa sổ check-in cho ca này (mở từ " + pc.getCaLam().getGioBatDau().minusMinutes(30).toString().substring(0, 5) + ")");
        }
        if (!currentTime.isBefore(endWindow)) {
            throw bad("Đã quá thời gian kết thúc ca, không thể check-in");
        }

        
        LocalDateTime shiftStart = LocalDateTime.of(pc.getNgayLam(), pc.getCaLam().getGioBatDau());
        int soPhutTre = 0;
        String trangThai = "DUNG_GIO";
        String ghiChu = (dto.getGhiChu() != null && !dto.getGhiChu().isBlank())
                ? dto.getGhiChu().strip()
                : "Check-in ca " + pc.getCaLam().getTenCa();

        if (currentTime.isAfter(shiftStart)) {
            long deltaIn = Duration.between(shiftStart, currentTime).toMinutes();
            if (deltaIn > 15) {
                soPhutTre = (int) deltaIn;
                trangThai = "DI_TRE";
                ghiChu = "Đi trễ " + deltaIn + " phút";
            }
        }

        ChamCong cc = new ChamCong();
        cc.setPhanCa(pc);
        cc.setNhanVien(nv);
        cc.setCaLam(pc.getCaLam());
        cc.setNgayLam(pc.getNgayLam());
        cc.setGioCheckIn(currentTime);
        cc.setGioCheckOut(null);
        cc.setSoPhutTre(soPhutTre);
        cc.setSoPhutVeSom(0);
        cc.setSoGioLam(0.0);
        cc.setGioCongQuyDoi(0.0);
        cc.setTrangThai(trangThai);
        cc.setGhiChu(ghiChu);

        return chamCongRepository.save(cc);
    }

    @Transactional
    public ChamCong checkOut(Integer maNhanVienDangNhap, boolean quanLy, Integer maChamCong) {
        ChamCong cc = chamCongRepository.findById(maChamCong)
                .orElseThrow(() -> bad("Không tìm thấy bản ghi chấm công: " + maChamCong));

        if (!quanLy && !cc.getNhanVien().getMaNV().equals(maNhanVienDangNhap)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ được check-out cho chính mình");
        }
        if (cc.getGioCheckOut() != null) {
            throw conflict("Ca này đã check-out");
        }
        if (cc.getGioCheckIn() == null) {
            throw bad("Chưa có thông tin check-in");
        }

        LocalDateTime currentTime = now();
        
        if (Duration.between(cc.getGioCheckIn(), currentTime).toSeconds() < 60) {
            throw bad("Chưa đủ 1 phút sau khi check-in");
        }

        
        LocalDateTime shiftStart = LocalDateTime.of(cc.getNgayLam(), cc.getCaLam().getGioBatDau());
        LocalDateTime shiftEnd = LocalDateTime.of(cc.getNgayLam(), cc.getCaLam().getGioKetThuc());

        LocalDateTime tBatDau = cc.getGioCheckIn().isBefore(shiftStart) ? shiftStart : cc.getGioCheckIn();
        LocalDateTime tKetThuc = currentTime.isAfter(shiftEnd) ? shiftEnd : currentTime;

        long minutesWorked = Math.max(0, Duration.between(tBatDau, tKetThuc).toMinutes());
        double soGioLam = round2(minutesWorked / 60.0);
        double heSo = cc.getCaLam().getHeSoLuong() != null ? cc.getCaLam().getHeSoLuong() : 1.0;
        double gioCongQuyDoi = round2(soGioLam * heSo);

        
        int soPhutVeSom = 0;
        if (currentTime.isBefore(shiftEnd)) {
            long deltaOut = Duration.between(currentTime, shiftEnd).toMinutes();
            if (deltaOut > 15) {
                soPhutVeSom = (int) deltaOut;
                if ("DI_TRE".equals(cc.getTrangThai())) {
                    cc.setTrangThai("DI_TRE_VE_SOM");
                } else {
                    cc.setTrangThai("VE_SOM");
                }
            }
        }

        cc.setGioCheckOut(currentTime);
        cc.setSoPhutVeSom(soPhutVeSom);
        cc.setSoGioLam(soGioLam);
        cc.setGioCongQuyDoi(gioCongQuyDoi);

        return chamCongRepository.save(cc);
    }

    @Transactional
    public ChotCaResponseDTO chotCaQuaHan() {
        LocalDateTime currentTime = now();
        List<PhanCa> danhSachPhanCa = phanCaRepository.findByNgayLamLessThanEqualOrderByNgayLamAsc(today());

        int soCaDanhDauVang = 0;
        int soCaQuenCheckOut = 0;

        for (PhanCa pc : danhSachPhanCa) {
            LocalDateTime shiftCutoff = LocalDateTime.of(pc.getNgayLam(), pc.getCaLam().getGioKetThuc()).plusMinutes(30);
            if (currentTime.isAfter(shiftCutoff)) {
                Optional<ChamCong> opt = chamCongRepository.findByPhanCa_MaPhanCa(pc.getMaPhanCa());
                if (opt.isEmpty()) {
                    opt = chamCongRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(pc.getNhanVien().getMaNV(), pc.getCaLam().getMaCa(), pc.getNgayLam());
                }

                if (opt.isEmpty()) {
                    ChamCong cc = new ChamCong();
                    cc.setPhanCa(pc);
                    cc.setNhanVien(pc.getNhanVien());
                    cc.setCaLam(pc.getCaLam());
                    cc.setNgayLam(pc.getNgayLam());
                    cc.setGioCheckIn(null);
                    cc.setGioCheckOut(null);
                    cc.setSoPhutTre(0);
                    cc.setSoPhutVeSom(0);
                    cc.setSoGioLam(0.0);
                    cc.setGioCongQuyDoi(0.0);
                    cc.setTrangThai("VANG");
                    cc.setGhiChu("Vắng mặt không check-in");
                    chamCongRepository.save(cc);
                    soCaDanhDauVang++;
                } else {
                    ChamCong cc = opt.get();
                    if (cc.getGioCheckIn() != null && cc.getGioCheckOut() == null && !"QUEN_CHECK_OUT".equals(cc.getTrangThai())) {
                        cc.setTrangThai("QUEN_CHECK_OUT");
                        cc.setSoGioLam(0.0);
                        cc.setGioCongQuyDoi(0.0);
                        String oldNote = cc.getGhiChu() != null ? cc.getGhiChu() + " - " : "";
                        cc.setGhiChu(oldNote + "Tự động đánh dấu quên check-out sau cuối ca");
                        chamCongRepository.save(cc);
                        soCaQuenCheckOut++;
                    }
                }
            }
        }
        return new ChotCaResponseDTO(soCaDanhDauVang, soCaQuenCheckOut);
    }

    @Transactional
    public ChamCong dieuChinhChamCong(Integer maChamCong, Integer callerId, DieuChinhChamCongDTO dto) {
        if (dto == null || dto.getLyDoDieuChinh() == null || dto.getLyDoDieuChinh().strip().isEmpty()) {
            throw bad("Bắt buộc nhập lý do điều chỉnh");
        }

        ChamCong cc = chamCongRepository.findById(maChamCong)
                .orElseThrow(() -> bad("Không tìm thấy bản ghi chấm công: " + maChamCong));

        NhanVien nguoiDieuChinh = nhanVienRepository.findById(callerId)
                .orElseThrow(() -> bad("Không tìm thấy thông tin quản lý điều chỉnh"));

        cc.setNguoiDieuChinh(nguoiDieuChinh);
        cc.setLyDoDieuChinh(dto.getLyDoDieuChinh().strip());
        cc.setThoiGianDieuChinh(now());

        if (dto.getGioCheckIn() != null) {
            cc.setGioCheckIn(dto.getGioCheckIn());
        }
        if (dto.getGioCheckOut() != null) {
            cc.setGioCheckOut(dto.getGioCheckOut());
        }

        if (cc.getGioCheckIn() != null && cc.getGioCheckOut() != null) {
            LocalDateTime shiftStart = LocalDateTime.of(cc.getNgayLam(), cc.getCaLam().getGioBatDau());
            LocalDateTime shiftEnd = LocalDateTime.of(cc.getNgayLam(), cc.getCaLam().getGioKetThuc());

            LocalDateTime tBatDau = cc.getGioCheckIn().isBefore(shiftStart) ? shiftStart : cc.getGioCheckIn();
            LocalDateTime tKetThuc = cc.getGioCheckOut().isAfter(shiftEnd) ? shiftEnd : cc.getGioCheckOut();

            long minutesWorked = Math.max(0, Duration.between(tBatDau, tKetThuc).toMinutes());
            double soGioLam = round2(minutesWorked / 60.0);
            double heSo = cc.getCaLam().getHeSoLuong() != null ? cc.getCaLam().getHeSoLuong() : 1.0;
            double gioCongQuyDoi = round2(soGioLam * heSo);

            int soPhutTre = 0;
            if (cc.getGioCheckIn().isAfter(shiftStart)) {
                long deltaIn = Duration.between(shiftStart, cc.getGioCheckIn()).toMinutes();
                if (deltaIn > 15) soPhutTre = (int) deltaIn;
            }

            int soPhutVeSom = 0;
            if (cc.getGioCheckOut().isBefore(shiftEnd)) {
                long deltaOut = Duration.between(cc.getGioCheckOut(), shiftEnd).toMinutes();
                if (deltaOut > 15) soPhutVeSom = (int) deltaOut;
            }

            cc.setSoPhutTre(soPhutTre);
            cc.setSoPhutVeSom(soPhutVeSom);
            cc.setSoGioLam(soGioLam);
            cc.setGioCongQuyDoi(gioCongQuyDoi);

            if (soPhutTre > 0 && soPhutVeSom > 0) {
                cc.setTrangThai("DI_TRE_VE_SOM");
            } else if (soPhutTre > 0) {
                cc.setTrangThai("DI_TRE");
            } else if (soPhutVeSom > 0) {
                cc.setTrangThai("VE_SOM");
            } else {
                cc.setTrangThai("DUNG_GIO");
            }
        } else if (cc.getGioCheckIn() == null) {
            cc.setTrangThai("VANG");
            cc.setSoGioLam(0.0);
            cc.setGioCongQuyDoi(0.0);
        }

        return chamCongRepository.save(cc);
    }

    @Transactional
    public PhanCa doiCa(Integer maPhanCa, DoiCaDTO dto) {
        if (dto == null || dto.getMaNVMoi() == null) {
            throw bad("Cần chọn nhân viên thay thế");
        }

        PhanCa pc = phanCaRepository.findById(maPhanCa)
                .orElseThrow(() -> bad("Không tìm thấy suất phân ca: " + maPhanCa));

        
        if (chamCongRepository.findByPhanCa_MaPhanCa(maPhanCa).isPresent() ||
                chamCongRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(pc.getNhanVien().getMaNV(), pc.getCaLam().getMaCa(), pc.getNgayLam()).isPresent()) {
            throw conflict("Không thể đổi ca đã phát sinh chấm công");
        }

        NhanVien nvMoi = nhanVienRepository.findById(dto.getMaNVMoi())
                .orElseThrow(() -> bad("Không tìm thấy nhân viên thay thế"));
        if (!"DANG_LAM".equals(nvMoi.getTrangThai())) {
            throw bad("Không thể phân ca cho nhân viên đã nghỉ");
        }

        
        Optional<PhanCa> duplicate = phanCaRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(nvMoi.getMaNV(), pc.getCaLam().getMaCa(), pc.getNgayLam());
        if (duplicate.isPresent() && !duplicate.get().getMaPhanCa().equals(maPhanCa)) {
            throw conflict("Nhân viên thay thế đã có lịch trong ca này");
        }

        pc.setNhanVien(nvMoi);
        if (dto.getLyDoDoiCa() != null && !dto.getLyDoDoiCa().isBlank()) {
            pc.setGhiChu(dto.getLyDoDoiCa().strip());
        }
        return phanCaRepository.save(pc);
    }

    public List<PhanCaTuanItemDTO> layLichTuan(Integer maNhanVienDangNhap, boolean quanLy, LocalDate ngayBatDau) {
        LocalDate base = ngayBatDau == null ? today() : ngayBatDau;
        LocalDate from = base.with(DayOfWeek.MONDAY);
        LocalDate to = from.plusDays(6);

        List<PhanCa> all = phanCaRepository.findByNgayLamBetweenOrderByNgayLamAsc(from, to);

        
        Map<String, Boolean> missingCashierMap = new HashMap<>();
        Map<String, List<PhanCa>> grouped = all.stream()
                .collect(Collectors.groupingBy(p -> p.getNgayLam().toString() + "_" + p.getCaLam().getMaCa()));

        for (Map.Entry<String, List<PhanCa>> entry : grouped.entrySet()) {
            List<PhanCa> list = entry.getValue();
            boolean hasCashierOrManager = list.stream().anyMatch(p -> {
                String r = p.getNhanVien().getVaiTro();
                return "THU_NGAN".equals(r) || "QUAN_LY".equals(r) || "MANAGER".equals(r) || "ADMIN".equals(r);
            });
            missingCashierMap.put(entry.getKey(), !list.isEmpty() && !hasCashierOrManager);
        }

        List<PhanCa> targetList = quanLy
                ? all
                : all.stream().filter(p -> p.getNhanVien().getMaNV().equals(maNhanVienDangNhap)).collect(Collectors.toList());

        List<PhanCaTuanItemDTO> result = new ArrayList<>();
        for (PhanCa p : targetList) {
            String key = p.getNgayLam().toString() + "_" + p.getCaLam().getMaCa();
            boolean thieuThuNgan = missingCashierMap.getOrDefault(key, false);

            Optional<ChamCong> ccOpt = chamCongRepository.findByPhanCa_MaPhanCa(p.getMaPhanCa());
            if (ccOpt.isEmpty()) {
                ccOpt = chamCongRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(p.getNhanVien().getMaNV(), p.getCaLam().getMaCa(), p.getNgayLam());
            }

            boolean daChamCong = ccOpt.isPresent();
            String trangThaiChamCong = ccOpt.map(ChamCong::getTrangThai).orElse("CHUA_CHECK_IN");

            result.add(new PhanCaTuanItemDTO(
                    p.getMaPhanCa(),
                    p.getNhanVien(),
                    p.getCaLam(),
                    p.getNgayLam(),
                    p.getGhiChu(),
                    thieuThuNgan,
                    daChamCong,
                    trangThaiChamCong
            ));
        }
        return result;
    }

    public List<BangCongNhanVienDTO> layBangCong(Integer maNhanVienDangNhap, boolean quanLy, LocalDate tuNgay, LocalDate denNgay) {
        LocalDate from = tuNgay != null ? tuNgay : today().with(DayOfWeek.MONDAY);
        LocalDate to = denNgay != null ? denNgay : today();

        if (from.isAfter(to)) {
            throw bad("Khoảng ngày không hợp lệ");
        }

        List<ChamCong> records = quanLy
                ? chamCongRepository.findByNgayLamBetweenOrderByNgayLamDescGioCheckInDesc(from, to)
                : chamCongRepository.findByNhanVien_MaNVAndNgayLamBetweenOrderByNgayLamDescGioCheckInDesc(maNhanVienDangNhap, from, to);

        Map<Integer, BangCongNhanVienDTO> map = new LinkedHashMap<>();

        if (!quanLy) {
            NhanVien nv = nhanVienRepository.findById(maNhanVienDangNhap)
                    .orElseThrow(() -> bad("Không tìm thấy nhân viên"));
            BangCongNhanVienDTO dto = new BangCongNhanVienDTO();
            dto.setMaNV(nv.getMaNV());
            dto.setHoTen(nv.getHoTen());
            dto.setTenDangNhap(nv.getTenDangNhap());
            dto.setVaiTro(nv.getVaiTro());
            map.put(nv.getMaNV(), dto);
        } else {
            List<NhanVien> allStaff = nhanVienRepository.findAll();
            for (NhanVien nv : allStaff) {
                BangCongNhanVienDTO dto = new BangCongNhanVienDTO();
                dto.setMaNV(nv.getMaNV());
                dto.setHoTen(nv.getHoTen());
                dto.setTenDangNhap(nv.getTenDangNhap());
                dto.setVaiTro(nv.getVaiTro());
                map.put(nv.getMaNV(), dto);
            }
        }

        for (ChamCong cc : records) {
            Integer nvId = cc.getNhanVien().getMaNV();
            BangCongNhanVienDTO dto = map.get(nvId);
            if (dto == null) {
                dto = new BangCongNhanVienDTO();
                dto.setMaNV(nvId);
                dto.setHoTen(cc.getNhanVien().getHoTen());
                dto.setTenDangNhap(cc.getNhanVien().getTenDangNhap());
                dto.setVaiTro(cc.getNhanVien().getVaiTro());
                map.put(nvId, dto);
            }

            dto.setTongSoCa(dto.getTongSoCa() + 1);
            String st = cc.getTrangThai();
            if ("DUNG_GIO".equals(st)) {
                dto.setSoCaDungGio(dto.getSoCaDungGio() + 1);
            } else if ("DI_TRE".equals(st)) {
                dto.setSoCaDiTre(dto.getSoCaDiTre() + 1);
            } else if ("VE_SOM".equals(st)) {
                dto.setSoCaVeSom(dto.getSoCaVeSom() + 1);
            } else if ("DI_TRE_VE_SOM".equals(st)) {
                dto.setSoCaDiTre(dto.getSoCaDiTre() + 1);
                dto.setSoCaVeSom(dto.getSoCaVeSom() + 1);
            } else if ("VANG".equals(st)) {
                dto.setSoCaVang(dto.getSoCaVang() + 1);
            } else if ("QUEN_CHECK_OUT".equals(st)) {
                dto.setSoCaQuenCheckOut(dto.getSoCaQuenCheckOut() + 1);
            }

            double worked = cc.getSoGioLam() != null ? cc.getSoGioLam() : 0.0;
            double converted = cc.getGioCongQuyDoi() != null ? cc.getGioCongQuyDoi() : 0.0;

            dto.setTongGioThucTe(round2(dto.getTongGioThucTe() + worked));
            dto.setTongGioQuyDoi(round2(dto.getTongGioQuyDoi() + converted));
        }

        return new ArrayList<>(map.values());
    }

    public List<ChamCong> layLichSuChamCong(Integer maNhanVienDangNhap, boolean quanLy, LocalDate tuNgay, LocalDate denNgay) {
        return layLichSuChamCong(maNhanVienDangNhap, quanLy, tuNgay, denNgay, null, null);
    }

    public List<ChamCong> layLichSuChamCong(Integer maNhanVienDangNhap, boolean quanLy, LocalDate tuNgay, LocalDate denNgay, Integer maNV, String trangThai) {
        LocalDate from = tuNgay == null ? today().minusDays(30) : tuNgay;
        LocalDate to = denNgay == null ? today() : denNgay;
        if (from.isAfter(to)) {
            throw bad("Khoảng ngày không hợp lệ");
        }

        List<ChamCong> list = quanLy
                ? (maNV != null
                    ? chamCongRepository.findByNhanVien_MaNVAndNgayLamBetweenOrderByNgayLamDescGioCheckInDesc(maNV, from, to)
                    : chamCongRepository.findByNgayLamBetweenOrderByNgayLamDescGioCheckInDesc(from, to))
                : chamCongRepository.findByNhanVien_MaNVAndNgayLamBetweenOrderByNgayLamDescGioCheckInDesc(maNhanVienDangNhap, from, to);

        if (trangThai != null && !trangThai.isBlank()) {
            list = list.stream().filter(cc -> trangThai.equalsIgnoreCase(cc.getTrangThai())).collect(Collectors.toList());
        }
        return list;
    }

    @Transactional
    public PhanCa phanCa(PhanCaDTO dto) {
        if (dto == null || dto.getMaNV() == null || dto.getMaCa() == null || dto.getNgayLam() == null) {
            throw bad("Cần chọn nhân viên, ca và ngày làm");
        }
        if (dto.getNgayLam().isBefore(today())) {
            throw bad("Không thể phân ca cho ngày đã qua");
        }
        NhanVien nv = nhanVienRepository.findById(dto.getMaNV())
                .orElseThrow(() -> bad("Không tìm thấy nhân viên"));
        if (!"DANG_LAM".equals(nv.getTrangThai())) {
            throw bad("Không thể phân ca cho nhân viên đã nghỉ");
        }
        CaLam ca = caLamRepository.findById(dto.getMaCa())
                .orElseThrow(() -> bad("Không tìm thấy ca làm"));
        String note = dto.getGhiChu() == null ? null : dto.getGhiChu().strip();
        if (note != null && note.length() > 255) {
            throw bad("Ghi chú tối đa 255 ký tự");
        }
        PhanCa item = phanCaRepository.findByNhanVien_MaNVAndCaLam_MaCaAndNgayLam(nv.getMaNV(), ca.getMaCa(), dto.getNgayLam())
                .orElse(new PhanCa());
        item.setNhanVien(nv);
        item.setCaLam(ca);
        item.setNgayLam(dto.getNgayLam());
        item.setGhiChu(note);
        return phanCaRepository.save(item);
    }

    @Transactional
    public void xoaPhanCa(Integer maPhanCa) {
        PhanCa pc = phanCaRepository.findById(maPhanCa)
                .orElseThrow(() -> bad("Không tìm thấy phân ca: " + maPhanCa));
        if (chamCongRepository.findByPhanCa_MaPhanCa(maPhanCa).isPresent()) {
            throw conflict("Không thể xóa phân ca đã phát sinh chấm công");
        }
        phanCaRepository.delete(pc);
    }

    private void chuanHoaNhanVien(NhanVien nv, boolean requirePassword) {
        nv.setHoTen(text(nv.getHoTen(), "Họ tên", 100));
        nv.setTenDangNhap(text(nv.getTenDangNhap(), "Tên đăng nhập", 50));
        if (!nv.getTenDangNhap().matches("[A-Za-z0-9._-]{3,50}")) {
            throw bad("Tên đăng nhập chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang");
        }
        if (requirePassword && (nv.getMatKhau() == null || nv.getMatKhau().length() < 6)) {
            throw bad("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (nv.getMatKhau() != null && nv.getMatKhau().length() > 255) {
            throw bad("Mật khẩu quá dài");
        }
        String phone = nv.getSoDienThoai() == null ? null : nv.getSoDienThoai().strip();
        if (phone != null && !phone.isEmpty() && !phone.matches("[0-9+ -]{8,15}")) {
            throw bad("Số điện thoại không hợp lệ");
        }
        nv.setSoDienThoai(phone == null || phone.isEmpty() ? null : phone);
        if (!List.of("QUAN_LY", "THU_NGAN", "PHUC_VU", "STAFF", "MANAGER", "ADMIN").contains(nv.getVaiTro())) {
            throw bad("Vai trò không hợp lệ");
        }
        if (nv.getTrangThai() == null) {
            nv.setTrangThai("DANG_LAM");
        }
        if (!List.of("DANG_LAM", "DA_NGHI").contains(nv.getTrangThai())) {
            throw bad("Trạng thái nhân viên không hợp lệ");
        }
    }

    private String text(String value, String label, int max) {
        String result = value == null ? "" : value.strip();
        if (result.isEmpty() || result.length() > max) {
            throw bad(label + " phải có từ 1 đến " + max + " ký tự");
        }
        return result;
    }
}
