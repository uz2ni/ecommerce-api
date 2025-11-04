package com.example.ecommerceapi.user.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargePointRequest {

    @NotNull(message = "amount는 필수입니다.")
    @Min(value = 1000, message = "amount는 1000 이상이어야 합니다.")
    private Integer amount;

}