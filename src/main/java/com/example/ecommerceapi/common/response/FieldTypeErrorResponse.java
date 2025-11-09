package com.example.ecommerceapi.common.response;

import java.util.List;

public record FieldTypeErrorResponse(
        String code,
        List<String> errorFields
) { }
