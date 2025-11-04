package com.example.ecommerceapi.common.exception;

import com.example.ecommerceapi.common.response.*;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler{

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse response = new ErrorResponse(
                e.getErrorCodeValue(),
                e.getMessage()
        );
        log.debug(response.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<FieldErrorDetail> errorDetails = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldErrorDetail(err.getField(), err.getDefaultMessage()))
                .toList(); // Java 16 이상, 그렇지 않으면 collect(Collectors.toList())

        FieldErrorResponse response = new FieldErrorResponse(
                ErrorCode.FIELD_NOT_VALID.getCode(),
                errorDetails
        );

        log.info(response.toString());
        return ResponseEntity.badRequest().body(response);

    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                    HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<FieldErrorDetail> errorDetails =  new ArrayList<>();

        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException ife) {
            for (JsonMappingException.Reference ref : ife.getPath()) {

                String fieldName = ref.getFieldName();
                String rejectedValue = String.valueOf(ife.getValue());
                String targetType = ife.getTargetType().getSimpleName();

                errorDetails.add(new FieldErrorDetail(fieldName, targetType + " 타입이어야 합니다. (입력값: " + rejectedValue + ")"));
            }
        }

        FieldErrorResponse response = new FieldErrorResponse(
                ErrorCode.FIELD_TYPE_NOT_VALID.getCode(),
                errorDetails
        );

        log.info(response.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerException(Exception e) {
        ErrorResponse response = new ErrorResponse(
                ErrorCode.SERVER_ERROR.getCode(),
                ErrorCode.SERVER_ERROR.getMessage()
        );
        log.error(response.toString());
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}