package com.example.ecommerceapi.exception;

public record ErrorResponse(
        String code,
        String message
) {
}
