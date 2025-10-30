package com.example.ecommerceapi.exception;

public record FieldErrorDetail(
        String field,
        String message
) {}