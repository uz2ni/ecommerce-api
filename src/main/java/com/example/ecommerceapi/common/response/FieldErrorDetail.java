package com.example.ecommerceapi.common.response;

public record FieldErrorDetail(
        String field,
        String message
) {}