package com.example.ecommerceapi.application.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "userId는 필수입니다.")
    private Integer userId;

    @NotBlank(message = "deliveryUsername는 빈 값일 수 없습니다.")
    private String deliveryUsername;

    @NotBlank(message = "deliveryAddress는 빈 값일 수 없습니다.")
    private String deliveryAddress;

    @NotNull(message = "couponId는 필수입니다.")
    private Integer couponId;

}