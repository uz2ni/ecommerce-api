package com.example.ecommerceapi.coupon.application.dto;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
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
public class CouponUserResult {
    private Integer couponUserId;
    private Integer couponId;
    private Integer userId;
    private String userName;
    private Boolean used;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    public static CouponUserResult from(CouponUser couponUser, String userName) {
        return CouponUserResult.builder()
                .couponUserId(couponUser.getCouponUserId())
                .couponId(couponUser.getCouponId())
                .userId(couponUser.getUserId())
                .userName(userName)
                .used(couponUser.getUsed())
                .issuedAt(couponUser.getIssuedAt())
                .usedAt(couponUser.getUsedAt())
                .build();
    }

    public static List<CouponUserResult> fromList(List<CouponUser> couponUsers, java.util.function.Function<Integer, String> userNameResolver) {
        return couponUsers.stream()
                .map(cu -> from(cu, userNameResolver.apply(cu.getUserId())))
                .toList();
    }
}