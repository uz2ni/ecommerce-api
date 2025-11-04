package com.example.ecommerceapi.application.dto.coupon;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCouponRequest {

    @NotNull(message = "userId는 필수입니다.")
    private Integer userId;

    @NotNull(message = "couponId는 필수입니다.")
    private Integer couponId;

}