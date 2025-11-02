package com.example.ecommerceapi.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Integer userId;
    private String deliveryUsername;
    private String deliveryAddress;
    private Integer couponId;
}