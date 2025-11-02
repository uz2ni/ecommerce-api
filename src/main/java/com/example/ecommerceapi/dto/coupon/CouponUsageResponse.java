package com.example.ecommerceapi.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageResponse {
    private Integer couponUserId;
    private Integer userId;
    private String userName;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private Boolean used;
}