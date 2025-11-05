package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.IncrementProductViewResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncrementProductViewResponse {
    private Integer viewCount;

    public static IncrementProductViewResponse from(IncrementProductViewResponseDto dto) {
        return IncrementProductViewResponse.builder()
                .viewCount(dto.getViewCount())
                .build();
    }
}
