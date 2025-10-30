package com.example.ecommerceapi.exception;

import java.util.List;

public record FieldErrorResponse(
        String code,
        List<FieldErrorDetail> errorFields
) {}