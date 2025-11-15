package com.example.ecommerceapi.coupon.application.dto;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;

import java.time.LocalDateTime;
import java.util.List;

public record CouponUserResult(
        Integer couponUserId,
        Integer couponId,
        Integer userId,
        String userName,
        Boolean used,
        LocalDateTime issuedAt,
        LocalDateTime usedAt
) {
    public static CouponUserResult from(CouponUser couponUser, String userName) {
        return new CouponUserResult(
                couponUser.getCouponUserId(),
                couponUser.getCoupon().getCouponId(),
                couponUser.getUser().getUserId(),
                userName,
                couponUser.getUsed(),
                couponUser.getIssuedAt(),
                couponUser.getUsedAt()
        );
    }

    public static List<CouponUserResult> fromList(List<CouponUser> couponUsers, java.util.function.Function<Integer, String> userNameResolver) {
        return couponUsers.stream()
                .map(cu -> from(cu, userNameResolver.apply(cu.getUser().getUserId())))
                .toList();
    }
}