package com.example.ecommerceapi.coupon.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCouponCommand {
    private Integer userId;
    private Integer couponId;
}