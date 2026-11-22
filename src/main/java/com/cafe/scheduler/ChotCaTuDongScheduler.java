package com.cafe.scheduler;

import com.cafe.service.NhanSuService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
/** Tự động quét và chốt ca làm việc quá hạn */
public class ChotCaTuDongScheduler {

    private final NhanSuService nhanSuService;

    public ChotCaTuDongScheduler(NhanSuService nhanSuService) {
        this.nhanSuService = nhanSuService;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void runAutoChotCa() {
        try {
            nhanSuService.chotCaQuaHan();
        } catch (Exception e) {
            System.err.println("Lỗi khi tự động chốt ca quá hạn: " + e.getMessage());
        }
    }
}
