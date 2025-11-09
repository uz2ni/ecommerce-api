package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
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

    public IssueCouponCommand toCommand() {
        return IssueCouponCommand.builder()
                .userId(this.userId)
                .couponId(this.couponId)
                .build();
    }
}