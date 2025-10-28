package com.example.ecommerceapi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointResponse {
    private Integer pointId;
    private Integer userId;
    private String pointType;
    private Integer pointAmount;
    private Integer balance;
    private LocalDateTime createdAt;
}