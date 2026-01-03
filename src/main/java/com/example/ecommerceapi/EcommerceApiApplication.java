package com.example.ecommerceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableRetry
@SpringBootApplication
public class EcommerceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApiApplication.class, args);
        System.out.println("현재 활성 Heap(-Xms): " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");
        System.out.println("최대 가능 Heap(-Xmx): " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB");
    }

}
