
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `quanly_cafe`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE `quanly_cafe`;

-- Xóa bảng theo thứ tự ngược khóa ngoại để đảm bảo tái nạp sạch 100%
DROP TABLE IF EXISTS `lich_su_kho`;
DROP TABLE IF EXISTS `cham_cong`;
DROP TABLE IF EXISTS `phan_ca`;
DROP TABLE IF EXISTS `chi_tiet_hoa_don`;
DROP TABLE IF EXISTS `hoa_don`;
DROP TABLE IF EXISTS `cong_thuc`;
DROP TABLE IF EXISTS `nguyen_lieu`;
DROP TABLE IF EXISTS `mon_nuoc`;
DROP TABLE IF EXISTS `ban`;
DROP TABLE IF EXISTS `ca_lam`;
DROP TABLE IF EXISTS `nhan_vien`;

-- ==============================================================================
-- PHẦN 1: ĐỊNH NGHĨA CẤU TRÚC 11 BẢNG DỮ LIỆU (DDL)
-- ==============================================================================

-- 1. BẢNG NHÂN VIÊN (3 vai trò: QUAN_LY, THU_NGAN, PHUC_VU)
CREATE TABLE `nhan_vien` (
    `ma_nv` INT AUTO_INCREMENT PRIMARY KEY,
    `ho_ten` VARCHAR(100) NOT NULL,
    `so_dien_thoai` VARCHAR(15),
    `ten_dang_nhap` VARCHAR(50) NOT NULL UNIQUE,
    `mat_khau` VARCHAR(255) NOT NULL,
    `vai_tro` VARCHAR(20) NOT NULL DEFAULT 'PHUC_VU',
    `trang_thai` VARCHAR(20) NOT NULL DEFAULT 'DANG_LAM',
    CONSTRAINT `chk_nv_vai_tro` CHECK (`vai_tro` IN ('QUAN_LY', 'THU_NGAN', 'PHUC_VU')),
    CONSTRAINT `chk_nv_trang_thai` CHECK (`trang_thai` IN ('DANG_LAM', 'DA_NGHI'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG CA LÀM VIỆC
CREATE TABLE `ca_lam` (
    `ma_ca` INT AUTO_INCREMENT PRIMARY KEY,
    `ten_ca` VARCHAR(50) NOT NULL,
    `gio_bat_dau` TIME NOT NULL,
    `gio_ket_thuc` TIME NOT NULL,
    `he_so_luong` DOUBLE NOT NULL DEFAULT 1.0,
    CONSTRAINT `chk_ca_gio` CHECK (`gio_ket_thuc` > `gio_bat_dau`),
    CONSTRAINT `chk_ca_he_so` CHECK (`he_so_luong` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG PHÂN CA THEO NGÀY
CREATE TABLE `phan_ca` (
    `ma_phan_ca` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_nv` INT NOT NULL,
    `ma_ca` INT NOT NULL,
    `ngay_lam` DATE NOT NULL,
    `ghi_chu` VARCHAR(255),
    INDEX `idx_pc_ngay_lam` (`ngay_lam`),
    CONSTRAINT `uk_phan_ca_nhan_vien_ngay_ca` UNIQUE (`ma_nv`, `ngay_lam`, `ma_ca`),
    CONSTRAINT `fk_pc_nhanvien` FOREIGN KEY (`ma_nv`) REFERENCES `nhan_vien`(`ma_nv`) ON DELETE CASCADE,
    CONSTRAINT `fk_pc_calam` FOREIGN KEY (`ma_ca`) REFERENCES `ca_lam`(`ma_ca`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG CHẤM CÔNG (Hỗ trợ đầy đủ 7 quy tắc BR-CC & kiểm toán điều chỉnh)
CREATE TABLE `cham_cong` (
    `ma_cham_cong` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_phan_ca` INT NULL,
    `ma_nv` INT NOT NULL,
    `ma_ca` INT NOT NULL,
    `ngay_lam` DATE NOT NULL,
    `gio_check_in` DATETIME NULL,
    `gio_check_out` DATETIME NULL,
    `so_phut_tre` INT NOT NULL DEFAULT 0,
    `so_phut_ve_som` INT NOT NULL DEFAULT 0,
    `so_gio_lam` DOUBLE NOT NULL DEFAULT 0,
    `gio_cong_quy_doi` DOUBLE NOT NULL DEFAULT 0,
    `trang_thai` VARCHAR(30) NOT NULL,
    `ghi_chu` VARCHAR(255) NULL,
    `nguoi_dieu_chinh` INT NULL,
    `ly_do_dieu_chinh` VARCHAR(255) NULL,
    `thoi_gian_dieu_chinh` DATETIME NULL,
    INDEX `idx_cc_ngay_lam` (`ngay_lam`),
    INDEX `idx_cc_trang_thai` (`trang_thai`),
    CONSTRAINT `uk_cc_phanca` UNIQUE (`ma_phan_ca`),
    CONSTRAINT `uk_cc_nv_ngay_ca` UNIQUE (`ma_nv`, `ngay_lam`, `ma_ca`),
    CONSTRAINT `chk_cc_trang_thai` CHECK (`trang_thai` IN ('DUNG_GIO', 'DI_TRE', 'VE_SOM', 'DI_TRE_VE_SOM', 'VANG', 'QUEN_CHECK_OUT')),
    CONSTRAINT `chk_cc_so_lieu` CHECK (`so_phut_tre` >= 0 AND `so_phut_ve_som` >= 0 AND `so_gio_lam` >= 0 AND `gio_cong_quy_doi` >= 0),
    CONSTRAINT `fk_cc_phanca` FOREIGN KEY (`ma_phan_ca`) REFERENCES `phan_ca`(`ma_phan_ca`) ON DELETE SET NULL,
    CONSTRAINT `fk_cc_nhanvien` FOREIGN KEY (`ma_nv`) REFERENCES `nhan_vien`(`ma_nv`) ON DELETE CASCADE,
    CONSTRAINT `fk_cc_calam` FOREIGN KEY (`ma_ca`) REFERENCES `ca_lam`(`ma_ca`) ON DELETE CASCADE,
    CONSTRAINT `fk_cc_nguoidieuchinh` FOREIGN KEY (`nguoi_dieu_chinh`) REFERENCES `nhan_vien`(`ma_nv`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG BÀN (3 trạng thái: TRONG, DANG_DUNG_NUOC, CHO_THANH_TOAN)
CREATE TABLE `ban` (
    `ma_ban` INT AUTO_INCREMENT PRIMARY KEY,
    `ten_ban` VARCHAR(50) NOT NULL,
    `vi_tri` VARCHAR(50),
    `so_ghe` INT NOT NULL DEFAULT 4,
    `trang_thai` VARCHAR(20) NOT NULL DEFAULT 'TRONG',
    CONSTRAINT `chk_ban_so_ghe` CHECK (`so_ghe` > 0),
    CONSTRAINT `chk_ban_trang_thai` CHECK (`trang_thai` IN ('TRONG', 'DANG_DUNG_NUOC', 'CHO_THANH_TOAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG MÓN NƯỚC (12 món tinh gọn – 4 danh mục)
CREATE TABLE `mon_nuoc` (
    `ma_mon` INT AUTO_INCREMENT PRIMARY KEY,
    `ten_mon` VARCHAR(100) NOT NULL,
    `danh_muc` VARCHAR(50) NOT NULL,
    `don_gia` DOUBLE NOT NULL,
    `hinh_anh` VARCHAR(255) NULL,
    `trang_thai` VARCHAR(20) NOT NULL DEFAULT 'DANG_BAN',
    CONSTRAINT `chk_mon_don_gia` CHECK (`don_gia` >= 0),
    CONSTRAINT `chk_mon_trang_thai` CHECK (`trang_thai` IN ('DANG_BAN', 'NGUNG_BAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. BẢNG HÓA ĐƠN (Tách người tạo đơn & Thu ngân chốt tiền, lưu tiền thừa & minh chứng CK)
CREATE TABLE `hoa_don` (
    `ma_hoa_don` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban` INT NULL,
    `ma_nv` INT NOT NULL,
    `ma_nv_thu_ngan` INT NULL,
    `thoi_gian_tao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `thoi_gian_yeu_cau_tt` DATETIME NULL,
    `thoi_gian_thanh_toan` DATETIME NULL,
    `loai_don` VARCHAR(20) NOT NULL DEFAULT 'TAI_BAN',
    `tong_tien_goc` DOUBLE NOT NULL DEFAULT 0,
    `chiet_khau` DOUBLE NOT NULL DEFAULT 0,
    `tong_tien_thanh_toan` DOUBLE NOT NULL DEFAULT 0,
    `tien_khach_dua` DOUBLE NULL,
    `tien_thua` DOUBLE NOT NULL DEFAULT 0,
    `phuong_thuc_tt` VARCHAR(20) NULL DEFAULT 'TIEN_MAT',
    `ma_tham_chieu` VARCHAR(100) NULL UNIQUE,
    `xac_nhan_chuyen_khoan` TINYINT(1) NOT NULL DEFAULT 0,
    `minh_chung_chuyen_khoan` VARCHAR(255) NULL,
    `trang_thai` VARCHAR(20) NOT NULL DEFAULT 'DANG_PHUC_VU',
    `yeu_cau_tao` VARCHAR(36) NULL UNIQUE,
    `yeu_cau_thanh_toan` VARCHAR(36) NULL UNIQUE,
    `phien_ban` BIGINT NOT NULL DEFAULT 0,
    INDEX `idx_hd_trang_thai` (`trang_thai`),
    INDEX `idx_hd_thoi_gian_tt` (`thoi_gian_thanh_toan`),
    CONSTRAINT `chk_hd_loai_don` CHECK (`loai_don` IN ('TAI_BAN', 'MANG_DI')),
    CONSTRAINT `chk_hd_trang_thai` CHECK (`trang_thai` IN ('DANG_PHUC_VU', 'CHO_THANH_TOAN', 'DA_THANH_TOAN', 'DA_HUY')),
    CONSTRAINT `chk_hd_tien` CHECK (`tong_tien_goc` >= 0 AND `chiet_khau` >= 0 AND `tong_tien_thanh_toan` >= 0 AND `tien_thua` >= 0),
    CONSTRAINT `fk_hd_ban` FOREIGN KEY (`ma_ban`) REFERENCES `ban`(`ma_ban`) ON DELETE SET NULL,
    CONSTRAINT `fk_hd_nhanvien` FOREIGN KEY (`ma_nv`) REFERENCES `nhan_vien`(`ma_nv`),
    CONSTRAINT `fk_hd_thungan` FOREIGN KEY (`ma_nv_thu_ngan`) REFERENCES `nhan_vien`(`ma_nv`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. BẢNG CHI TIẾT HÓA ĐƠN (Composition của HoaDon + Theo dõi số ly đã in Quầy Bar)
CREATE TABLE `chi_tiet_hoa_don` (
    `ma_cthd` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_hoa_don` INT NOT NULL,
    `ma_mon` INT NOT NULL,
    `so_luong` INT NOT NULL DEFAULT 1,
    `so_luong_da_in_bar` INT NOT NULL DEFAULT 0,
    `don_gia_ban` DOUBLE NOT NULL,
    `thanh_tien` DOUBLE NOT NULL,
    `ghi_chu` VARCHAR(255) NULL,
    CONSTRAINT `chk_cthd_sl` CHECK (`so_luong` BETWEEN 1 AND 99 AND `so_luong_da_in_bar` >= 0),
    CONSTRAINT `chk_cthd_gia` CHECK (`don_gia_ban` >= 0 AND `thanh_tien` >= 0),
    CONSTRAINT `fk_cthd_hoadon` FOREIGN KEY (`ma_hoa_don`) REFERENCES `hoa_don`(`ma_hoa_don`) ON DELETE CASCADE,
    CONSTRAINT `fk_cthd_monnuoc` FOREIGN KEY (`ma_mon`) REFERENCES `mon_nuoc`(`ma_mon`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. BẢNG NGUYÊN LIỆU (13 nguyên liệu thật 100%)
CREATE TABLE `nguyen_lieu` (
    `ma_nl` INT AUTO_INCREMENT PRIMARY KEY,
    `ten_nl` VARCHAR(100) NOT NULL,
    `don_vi_tinh` VARCHAR(20) NOT NULL,
    `so_luong_ton` DOUBLE NOT NULL DEFAULT 0,
    `muc_ton_toi_thieu` DOUBLE NOT NULL DEFAULT 100,
    `don_gia_nhap` DOUBLE NOT NULL DEFAULT 0,
    CONSTRAINT `chk_nl_so_lieu` CHECK (`so_luong_ton` >= 0 AND `muc_ton_toi_thieu` >= 0 AND `don_gia_nhap` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. BẢNG CÔNG THỨC BOM (Association Class – Chặn trùng cặp ma_mon, ma_nl)
CREATE TABLE `cong_thuc` (
    `ma_cong_thuc` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_mon` INT NOT NULL,
    `ma_nl` INT NOT NULL,
    `dinh_muc` DOUBLE NOT NULL,
    `ghi_chu` VARCHAR(255) NULL,
    CONSTRAINT `uk_cong_thuc_mon_nl` UNIQUE (`ma_mon`, `ma_nl`),
    CONSTRAINT `chk_ct_dinh_muc` CHECK (`dinh_muc` > 0),
    CONSTRAINT `fk_ct_monnuoc` FOREIGN KEY (`ma_mon`) REFERENCES `mon_nuoc`(`ma_mon`) ON DELETE CASCADE,
    CONSTRAINT `fk_ct_nguyenlieu` FOREIGN KEY (`ma_nl`) REFERENCES `nguyen_lieu`(`ma_nl`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. BẢNG LỊCH SỬ KHO (Audit Trail: NHAP_KHO, XUAT_BAN_HANG, KIEM_KE_HAO_HUT)
CREATE TABLE `lich_su_kho` (
    `ma_gd` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_nl` INT NOT NULL,
    `ma_nv` INT NOT NULL,
    `ma_hoa_don` INT NULL,
    `loai_gd` VARCHAR(30) NOT NULL,
    `so_luong_thay_doi` DOUBLE NOT NULL,
    `ton_truoc` DOUBLE NOT NULL,
    `ton_sau` DOUBLE NOT NULL,
    `don_gia` DOUBLE NOT NULL DEFAULT 0,
    `thanh_tien` DOUBLE NOT NULL DEFAULT 0,
    `nha_cung_cap` VARCHAR(120) NULL,
    `thoi_gian` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ghi_chu` VARCHAR(255) NULL,
    INDEX `idx_lsk_thoi_gian` (`thoi_gian`),
    INDEX `idx_lsk_loai_gd` (`loai_gd`),
    INDEX `idx_lsk_ma_nl` (`ma_nl`),
    CONSTRAINT `chk_lsk_loai_gd` CHECK (`loai_gd` IN ('NHAP_KHO', 'XUAT_BAN_HANG', 'KIEM_KE_HAO_HUT')),
    CONSTRAINT `chk_lsk_so_lieu` CHECK (`ton_truoc` >= 0 AND `ton_sau` >= 0 AND `don_gia` >= 0 AND `thanh_tien` >= 0),
    CONSTRAINT `fk_lsk_nguyenlieu` FOREIGN KEY (`ma_nl`) REFERENCES `nguyen_lieu`(`ma_nl`) ON DELETE CASCADE,
    CONSTRAINT `fk_lsk_nhanvien` FOREIGN KEY (`ma_nv`) REFERENCES `nhan_vien`(`ma_nv`),
    CONSTRAINT `fk_lsk_hoadon` FOREIGN KEY (`ma_hoa_don`) REFERENCES `hoa_don`(`ma_hoa_don`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- PHẦN 2: DỮ LIỆU MẪU CHUẨN HÓA (SEED DATA V2.0)
-- ==============================================================================

-- 1. Nạp đúng 3 tài khoản chuẩn theo 3 vai trò tinh gọn
INSERT INTO `nhan_vien` (`ma_nv`, `ho_ten`, `so_dien_thoai`, `ten_dang_nhap`, `mat_khau`, `vai_tro`, `trang_thai`) VALUES
(1, 'Nguyễn Văn Duy (Quản lý)', '0901112222', 'quanly', 'quanly', 'QUAN_LY', 'DANG_LAM'),
(2, 'Trần Văn Quý (Thu ngân)', '0903334444', 'thungan', 'thungan', 'THU_NGAN', 'DANG_LAM'),
(3, 'Lê Tấn Đạt (Phục vụ)', '0905556666', 'phucvu', 'phucvu', 'PHUC_VU', 'DANG_LAM');

-- 2. Nạp 3 Ca Làm Việc chuẩn
INSERT INTO `ca_lam` (`ma_ca`, `ten_ca`, `gio_bat_dau`, `gio_ket_thuc`, `he_so_luong`) VALUES
(1, 'Ca Sáng', '07:00:00', '12:00:00', 1.0),
(2, 'Ca Chiều', '12:00:00', '17:00:00', 1.0),
(3, 'Ca Tối', '17:00:00', '22:00:00', 1.2);

-- 3. Nạp Lịch Phân Ca mẫu (Hôm qua / Hôm nay / Ngày mai)
-- • Ca Chiều hôm nay (ma_phan_ca = 7) chỉ có Phục vụ để kích hoạt cảnh báo BR-CC-07
-- • Ca Sáng hôm nay của Phục vụ (ma_phan_ca = 6) chưa Check-in để demo Check-in
-- • Ca Chiều ngày mai (ma_phan_ca = 8) dùng để demo Đổi ca đột xuất
INSERT INTO `phan_ca` (`ma_phan_ca`, `ma_nv`, `ma_ca`, `ngay_lam`, `ghi_chu`) VALUES
(1, 2, 1, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), 'Thu ngân trực ca sáng'),
(2, 3, 1, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), 'Phục vụ bàn ca sáng'),
(3, 3, 2, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), 'Phục vụ bàn ca chiều'),
(4, 2, 3, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), 'Thu ngân trực ca tối'),
(5, 2, 1, CURRENT_DATE(), 'Thu ngân trực ca sáng'),
(6, 3, 1, CURRENT_DATE(), 'Phục vụ bàn ca sáng'),
(7, 3, 2, CURRENT_DATE(), 'Phục vụ bàn ca chiều'),
(8, 3, 2, DATE_ADD(CURRENT_DATE(), INTERVAL 1 DAY), 'Phục vụ bàn ca chiều');

-- 4. Nạp Dữ Liệu Chấm Công mẫu (Minh họa đầy đủ: DUNG_GIO, DI_TRE, QUEN_CHECK_OUT, VANG)
INSERT INTO `cham_cong` (
    `ma_cham_cong`, `ma_phan_ca`, `ma_nv`, `ma_ca`, `ngay_lam`,
    `gio_check_in`, `gio_check_out`,
    `so_phut_tre`, `so_phut_ve_som`, `so_gio_lam`, `gio_cong_quy_doi`,
    `trang_thai`, `ghi_chu`, `nguoi_dieu_chinh`, `ly_do_dieu_chinh`, `thoi_gian_dieu_chinh`
) VALUES
(1, 1, 2, 1, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY),
 CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 06:55:00'),
 CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 12:02:00'),
 0, 0, 5.00, 5.00, 'DUNG_GIO', 'Hoàn thành ca đúng giờ', NULL, NULL, NULL),

(2, 2, 3, 1, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY),
 CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 07:22:00'),
 CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 12:00:00'),
 22, 0, 4.63, 4.63, 'DI_TRE', 'Đi trễ 22 phút', NULL, NULL, NULL),

(3, 3, 3, 2, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY),
 CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 11:58:00'),
 NULL,
 0, 0, 0.00, 0.00, 'QUEN_CHECK_OUT', 'Tự động đánh dấu quên check-out sau cuối ca', NULL, NULL, NULL),

(4, 4, 2, 3, DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY),
 NULL, NULL,
 0, 0, 0.00, 0.00, 'VANG', 'Vắng mặt không check-in', NULL, NULL, NULL),

(5, 5, 2, 1, CURRENT_DATE(),
 CONCAT(CURRENT_DATE(), ' 06:57:00'),
 NULL,
 0, 0, 0.00, 0.00, 'DUNG_GIO', 'Check-in đúng giờ ca sáng', NULL, NULL, NULL);

-- 5. Nạp Danh Sách 10 Bàn (8 Xanh dương TRONG, 1 Xanh lá DANG_DUNG_NUOC, 1 Vàng CHO_THANH_TOAN)
INSERT INTO `ban` (`ma_ban`, `ten_ban`, `vi_tri`, `so_ghe`, `trang_thai`) VALUES
(1, 'Bàn 01', 'Tầng 1', 2, 'TRONG'),
(2, 'Bàn 02', 'Tầng 1', 4, 'TRONG'),
(3, 'Bàn 03', 'Tầng 1', 4, 'DANG_DUNG_NUOC'),
(4, 'Bàn 04', 'Tầng 1', 4, 'TRONG'),
(5, 'Bàn 05', 'Tầng 1', 6, 'TRONG'),
(6, 'Bàn 06', 'Tầng 2', 2, 'TRONG'),
(7, 'Bàn 07', 'Tầng 2', 4, 'CHO_THANH_TOAN'),
(8, 'Bàn 08', 'Tầng 2', 4, 'TRONG'),
(9, 'Bàn 09', 'Ngoài trời', 4, 'TRONG'),
(10, 'Bàn 10', 'Ngoài trời', 6, 'TRONG');

-- 6. Nạp Thực Đơn Tinh Gọn 12 Món (4 Nhóm, tối đa 4 món/nhóm)
INSERT INTO `mon_nuoc` (`ma_mon`, `ten_mon`, `danh_muc`, `don_gia`, `trang_thai`) VALUES
-- Nhóm Cà phê (4 món)
(1, 'Cà phê đen đá', 'Cà phê', 25000, 'DANG_BAN'),
(2, 'Cà phê sữa đá', 'Cà phê', 29000, 'DANG_BAN'),
(3, 'Bạc xỉu Sài Gòn', 'Cà phê', 32000, 'DANG_BAN'),
(4, 'Espresso nguyên chất', 'Cà phê', 30000, 'DANG_BAN'),
-- Nhóm Trà (3 món)
(5, 'Trà đào cam sả', 'Trà', 38000, 'DANG_BAN'),
(6, 'Trà sữa trân châu', 'Trà', 42000, 'DANG_BAN'),
(7, 'Trà đen macchiato', 'Trà', 35000, 'DANG_BAN'),
-- Nhóm Đá xay (3 món)
(8, 'Matcha đá xay kem sữa', 'Đá xay', 48000, 'DANG_BAN'),
(9, 'Chocolate đá xay', 'Đá xay', 45000, 'DANG_BAN'),
(10, 'Cà phê đá xay kem sữa', 'Đá xay', 45000, 'DANG_BAN'),
-- Nhóm Nước ép (2 món)
(11, 'Nước ép cam tươi', 'Nước ép', 35000, 'DANG_BAN'),
(12, 'Nước ép dưa hấu', 'Nước ép', 30000, 'DANG_BAN');

-- 7. Nạp Kho 13 Nguyên Liệu Thật 100% (Tổng giá trị tồn: 2.613.600 đ)
INSERT INTO `nguyen_lieu` (`ma_nl`, `ten_nl`, `don_vi_tinh`, `so_luong_ton`, `muc_ton_toi_thieu`, `don_gia_nhap`) VALUES
(1, 'Cốt cà phê Robusta', 'ml', 2500, 300, 150),
(2, 'Sữa đặc Ngôi Sao', 'ml', 120, 100, 80),
(3, 'Sữa tươi thanh trùng', 'ml', 3000, 500, 35),
(4, 'Trà đen Phúc Long', 'g', 1500, 200, 200),
(5, 'Siro đào Teisseire', 'ml', 1200, 200, 180),
(6, 'Đào ngâm miếng Kronos', 'g', 2000, 300, 120),
(7, 'Đường nước mía', 'ml', 4000, 500, 25),
(8, 'Bột Matcha Nhật Bản', 'g', 800, 150, 450),
(9, 'Bột Chocolate nguyên chất', 'g', 1000, 200, 250),
(10, 'Kem béo thực vật Rich', 'ml', 1800, 300, 90),
(11, 'Trân châu đường đen', 'g', 1200, 200, 110),
(12, 'Cam tươi', 'g', 6000, 1000, 35),
(13, 'Dưa hấu tươi', 'g', 7000, 1000, 22);

-- 8. Nạp 34 Dòng Định Mức Công Thức BOM Thật 100% cho đủ 12 Món
INSERT INTO `cong_thuc` (`ma_cong_thuc`, `ma_mon`, `ma_nl`, `dinh_muc`, `ghi_chu`) VALUES
-- 1. Cà phê đen đá (4.875đ)
(1, 1, 1, 30, 'Cốt cà phê Robusta đậm đặc'),
(2, 1, 7, 15, 'Đường nước mía vừa ngọt'),
-- 2. Cà phê sữa đá (6.150đ)
(3, 2, 1, 25, 'Cốt cà phê Robusta'),
(4, 2, 2, 30, 'Sữa đặc béo ngọt'),
-- 3. Bạc xỉu Sài Gòn (7.550đ)
(5, 3, 1, 15, 'Cốt cà phê nhẹ'),
(6, 3, 2, 40, 'Sữa đặc'),
(7, 3, 3, 60, 'Sữa tươi thanh trùng'),
-- 4. Espresso nguyên chất (6.000đ)
(8, 4, 1, 40, 'Chiết xuất cà phê espresso đậm'),
-- 5. Trà đào cam sả (12.300đ)
(9, 5, 4, 15, 'Cốt trà đen ủ lạnh'),
(10, 5, 5, 25, 'Siro đào thơm'),
(11, 5, 6, 40, 'Đào ngâm miếng giòn'),
-- 6. Trà sữa trân châu (13.500đ)
(12, 6, 4, 20, 'Nền trà đen đậm vị'),
(13, 6, 3, 100, 'Sữa tươi thanh trùng'),
(14, 6, 2, 20, 'Sữa đặc tăng độ béo'),
(15, 6, 11, 40, 'Trân châu đường đen dẻo'),
-- 7. Trà đen macchiato (7.075đ)
(16, 7, 4, 20, 'Nền trà đen'),
(17, 7, 7, 15, 'Đường nước mía'),
(18, 7, 10, 30, 'Lớp kem béo macchiato đánh mịn'),
-- 8. Matcha đá xay kem sữa (13.850đ)
(19, 8, 8, 15, 'Bột Matcha Nhật Bản rây mịn'),
(20, 8, 3, 80, 'Sữa tươi xay cùng đá'),
(21, 8, 2, 20, 'Sữa đặc tạo ngọt'),
(22, 8, 10, 30, 'Kem béo phủ mặt'),
-- 9. Chocolate đá xay (13.650đ)
(23, 9, 9, 25, 'Bột Chocolate nguyên chất'),
(24, 9, 3, 100, 'Sữa tươi xay mịn'),
(25, 9, 2, 15, 'Sữa đặc'),
(26, 9, 10, 30, 'Kem béo phủ mặt'),
-- 10. Cà phê đá xay kem sữa (11.600đ)
(27, 10, 1, 30, 'Cốt cà phê Robusta'),
(28, 10, 3, 80, 'Sữa tươi'),
(29, 10, 2, 20, 'Sữa đặc'),
(30, 10, 10, 30, 'Kem béo phủ mặt'),
-- 11. Nước ép cam tươi (10.750đ)
(31, 11, 12, 300, 'Cam tươi vắt nguyên chất'),
(32, 11, 7, 10, 'Đường nước mía cân vị'),
-- 12. Nước ép dưa hấu (7.950đ)
(33, 12, 13, 350, 'Dưa hấu tươi ép lạnh'),
(34, 12, 7, 10, 'Đường nước mía nhẹ');

-- 9. Nạp 4 Hóa Đơn Mẫu:
-- • Đơn #1 & #2: Đã thanh toán hôm nay (Doanh thu thuần: 125.000đ, Chiết khấu: 5.000đ)
-- • Đơn #3: Bàn 03 đang dùng nước (Viền xanh lá - 63.000đ)
-- • Đơn #4: Bàn 07 đang chờ thanh toán (Viền vàng hổ phách - 70.000đ)
INSERT INTO `hoa_don` (
    `ma_hoa_don`, `ma_ban`, `ma_nv`, `ma_nv_thu_ngan`,
    `thoi_gian_tao`, `thoi_gian_yeu_cau_tt`, `thoi_gian_thanh_toan`,
    `loai_don`, `tong_tien_goc`, `chiet_khau`, `tong_tien_thanh_toan`,
    `tien_khach_dua`, `tien_thua`, `phuong_thuc_tt`, `ma_tham_chieu`,
    `xac_nhan_chuyen_khoan`, `minh_chung_chuyen_khoan`, `trang_thai`
) VALUES
(1, 1, 3, 2,
 DATE_SUB(NOW(), INTERVAL 150 MINUTE), DATE_SUB(NOW(), INTERVAL 125 MINUTE), DATE_SUB(NOW(), INTERVAL 120 MINUTE),
 'TAI_BAN', 54000, 0, 54000, 100000, 46000, 'TIEN_MAT', NULL, 0, NULL, 'DA_THANH_TOAN'),

(2, NULL, 2, 1,
 DATE_SUB(NOW(), INTERVAL 65 MINUTE), DATE_SUB(NOW(), INTERVAL 62 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE),
 'MANG_DI', 76000, 5000, 71000, NULL, 0, 'CHUYEN_KHOAN', 'VCB20261119001', 1, 'Khách chuyển khoản VietQR thành công', 'DA_THANH_TOAN'),

(3, 3, 3, NULL,
 DATE_SUB(NOW(), INTERVAL 25 MINUTE), NULL, NULL,
 'TAI_BAN', 63000, 0, 63000, NULL, 0, NULL, NULL, 0, NULL, 'DANG_PHUC_VU'),

(4, 7, 3, NULL,
 DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL,
 'TAI_BAN', 70000, 0, 70000, NULL, 0, NULL, NULL, 0, NULL, 'CHO_THANH_TOAN');

-- 10. Nạp Chi Tiết cho 4 Hóa Đơn (Đã đồng bộ so_luong_da_in_bar)
INSERT INTO `chi_tiet_hoa_don` (`ma_cthd`, `ma_hoa_don`, `ma_mon`, `so_luong`, `so_luong_da_in_bar`, `don_gia_ban`, `thanh_tien`, `ghi_chu`) VALUES
-- Đơn #1 (Đã thanh toán)
(1, 1, 1, 1, 1, 25000, 25000, 'Ít đường'),
(2, 1, 2, 1, 1, 29000, 29000, 'Bình thường'),
-- Đơn #2 (Đã thanh toán - Mang đi)
(3, 2, 5, 2, 2, 38000, 76000, 'Nhiều đá mang đi'),
-- Đơn #3 (Bàn 03 - DANG_DUNG_NUOC: 63.000đ, không dùng Sữa đặc)
(4, 3, 1, 1, 1, 25000, 25000, 'Không đường'),
(5, 3, 5, 1, 1, 38000, 38000, 'Ít đá'),
-- Đơn #4 (Bàn 07 - CHO_THANH_TOAN: 70.000đ, không dùng Sữa đặc)
(6, 4, 11, 1, 1, 35000, 35000, 'Ít đường'),
(7, 4, 7, 1, 1, 35000, 35000, 'Bình thường');

-- 11. Nạp Nhật Ký Biến Động Kho Mẫu (Khớp 100% số tồn kho hiện hành & KPI)
INSERT INTO `lich_su_kho` (
    `ma_nl`, `ma_nv`, `ma_hoa_don`, `loai_gd`,
    `so_luong_thay_doi`, `ton_truoc`, `ton_sau`,
    `don_gia`, `thanh_tien`, `nha_cung_cap`, `thoi_gian`, `ghi_chu`
) VALUES
-- Nhập kho đầu ngày của Quản lý (ma_nv = 1, Tổng chi phí nhập: 1.381.925đ)
(1, 1, NULL, 'NHAP_KHO', 2555, 0, 2555, 150, 383250, 'Cà phê Buôn Ma Thuột', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Nhập lô cốt cà phê đầu ngày'),
(2, 1, NULL, 'NHAP_KHO', 150, 0, 150, 80, 12000, 'Đại lý Vinamilk Q7', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Nhập lô sữa đặc đầu ngày'),
(3, 1, NULL, 'NHAP_KHO', 3020, 0, 3020, 35, 105700, 'Đại lý Vinamilk Q7', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Nhập sữa tươi thanh trùng'),
(4, 1, NULL, 'NHAP_KHO', 1530, 0, 1530, 200, 306000, 'Trà Phúc Long Sỉ', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Nhập trà đen ủ lạnh'),
(5, 1, NULL, 'NHAP_KHO', 1250, 0, 1250, 180, 225000, 'Nguyên liệu Pha chế Q7', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Nhập siro đào Teisseire'),
(6, 1, NULL, 'NHAP_KHO', 2080, 0, 2080, 120, 249600, 'Nguyên liệu Pha chế Q7', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Nhập đào ngâm đóng lon'),
(7, 1, NULL, 'NHAP_KHO', 4015, 0, 4015, 25, 100375, 'Tổng kho Đường Biên Hòa', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Nhập đường nước mía'),

-- Xuất bán hàng tự động từ Đơn #1 (Giá vốn: 11.025đ, Thu ngân ma_nv = 2 chốt đơn)
(1, 2, 1, 'XUAT_BAN_HANG', -55, 2555, 2500, 150, 8250, NULL, DATE_SUB(NOW(), INTERVAL 120 MINUTE), 'Trừ kho tự động theo BOM đơn #1'),
(2, 2, 1, 'XUAT_BAN_HANG', -30, 150, 120, 80, 2400, NULL, DATE_SUB(NOW(), INTERVAL 120 MINUTE), 'Trừ kho tự động theo BOM đơn #1'),
(7, 2, 1, 'XUAT_BAN_HANG', -15, 4015, 4000, 25, 375, NULL, DATE_SUB(NOW(), INTERVAL 120 MINUTE), 'Trừ kho tự động theo BOM đơn #1'),

-- Xuất bán hàng tự động từ Đơn #2 (Giá vốn: 24.600đ, Quản lý ma_nv = 1 chốt đơn)
(4, 1, 2, 'XUAT_BAN_HANG', -30, 1530, 1500, 200, 6000, NULL, DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'Trừ kho tự động theo BOM đơn #2'),
(5, 1, 2, 'XUAT_BAN_HANG', -50, 1250, 1200, 180, 9000, NULL, DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'Trừ kho tự động theo BOM đơn #2'),
(6, 1, 2, 'XUAT_BAN_HANG', -80, 2080, 2000, 120, 9600, NULL, DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'Trừ kho tự động theo BOM đơn #2'),

-- Kiểm kê hao hụt mẫu của Quản lý (Giá trị hao hụt: 700đ)
(3, 1, NULL, 'KIEM_KE_HAO_HUT', -20, 3020, 3000, 35, 700, NULL, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'Hao hụt pha chế đánh bọt sữa');

SET FOREIGN_KEY_CHECKS = 1;