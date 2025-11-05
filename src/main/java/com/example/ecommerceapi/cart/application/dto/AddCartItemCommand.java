package com.example.ecommerceapi.cart.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemCommand {
    private Integer userId;
    private Integer productId;
    private int quantity;
}