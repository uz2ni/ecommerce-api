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
public class CouponResponse {
    private Integer couponId;
    private String couponName;
    private Integer discountAmount;
    private Integer issuedQuantity;
    private Integer usedQuantity;
    private Integer remainingQuantity;
    private LocalDateTime expiredAt;
    private String couponStatus;
}