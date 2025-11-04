package com.example.ecommerceapi.common.response;

public record ErrorResponse(
        String code,
        String message
) { }