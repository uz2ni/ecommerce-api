package com.example.ecommerceapi.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "orderId는 필수입니다.")
    private Integer orderId;

    @NotNull(message = "userId는 필수입니다.")
    private Integer userId;

}