package com.example.ecommerceapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointBalanceResponse {
    private Integer userId;
    private Integer pointBalance;
}
