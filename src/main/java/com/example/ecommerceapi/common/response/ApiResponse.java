package com.example.ecommerceapi.common.response;

public record ApiResponse<T, E extends BaseErrorResponse>(
        boolean success,
        T data,
        E error
) { }