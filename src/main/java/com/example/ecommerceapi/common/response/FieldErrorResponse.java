package com.example.ecommerceapi.common.response;

import java.util.List;

public record FieldErrorResponse(
        String code,
        String message,
        List<FieldErrorDetail> errorFields
) { }
