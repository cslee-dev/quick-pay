package com.example.quickpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages="com.example.quickpay")
public class QuickpayApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickpayApplication.class, args);
    }

}
