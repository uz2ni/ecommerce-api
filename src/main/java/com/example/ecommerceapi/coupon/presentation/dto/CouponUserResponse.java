package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.CouponUserResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUserResponse {
    private Integer couponUserId;
    private Integer userId;
    private String userName;
    private Boolean used;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    public static CouponUserResponse from(CouponUserResult couponUserResult) {
        return CouponUserResponse.builder()
                .couponUserId(couponUserResult.getCouponUserId())
                .userId(couponUserResult.getUserId())
                .userName(couponUserResult.getUserName())
                .used(couponUserResult.getUsed())
                .issuedAt(couponUserResult.getIssuedAt())
                .usedAt(couponUserResult.getUsedAt())
                .build();
    }

    public static List<CouponUserResponse> fromList(List<CouponUserResult> couponUserResults) {
        return couponUserResults.stream()
                .map(CouponUserResponse::from)
                .toList();
    }
}