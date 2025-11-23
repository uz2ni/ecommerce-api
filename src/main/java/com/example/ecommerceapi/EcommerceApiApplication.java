package com.example.ecommerceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class EcommerceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApiApplication.class, args);
    }

}
