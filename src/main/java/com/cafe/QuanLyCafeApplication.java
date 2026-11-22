package com.cafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@SpringBootApplication
@EnableScheduling
public class QuanLyCafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanLyCafeApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(">>> HE THONG QUAN LY QUAN CA PHE DA KHOI CHAY THANH CONG! <<<");
        System.out.println(">>> TRUY CAP GIAO DIEN WEB TAI: http://localhost:8080/index.html <<<");
        System.out.println(">>> POS BAN HANG:               http://localhost:8080/banhang.html <<<");
        System.out.println(">>> QUAN LY KHO & KPI:          http://localhost:8080/kho.html <<<");
        System.out.println(">>> QUAN LY NHAN SU & CHAM CONG: http://localhost:8080/nhansu.html <<<");
        System.out.println("==================================================================");
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
