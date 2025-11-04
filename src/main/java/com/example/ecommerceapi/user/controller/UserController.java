package com.example.ecommerceapi.user.controller;

import com.example.ecommerceapi.user.dto.ChargePointRequest;
import com.example.ecommerceapi.user.dto.PointResponse;
import com.example.ecommerceapi.user.dto.UserPointBalanceResponse;
import com.example.ecommerceapi.user.dto.UserResponse;
import com.example.ecommerceapi.point.usecase.PointUseCase;
import com.example.ecommerceapi.user.usecase.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원", description = "회원 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;
    private final PointUseCase pointUseCase;

    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userUseCase.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "회원 정보 조회", description = "회원 ID로 회원 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Integer userId) {

        UserResponse user = userUseCase.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "회원 포인트 잔액 조회", description = "회원의 현재 포인트 잔액을 조회합니다.")
    @GetMapping("/{userId}/points/balance")
    public ResponseEntity<UserPointBalanceResponse> getPointBalance(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Integer userId) {

        UserPointBalanceResponse response = userUseCase.getPointBalance(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 포인트 이력 조회", description = "회원의 포인트 충전/사용 이력을 조회합니다.")
    @GetMapping("/{userId}/points/history")
    public ResponseEntity<List<PointResponse>> getPointHistory(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Integer userId) {

        List<PointResponse> history = pointUseCase.getPointHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "포인트 충전", description = "회원의 포인트를 충전합니다.")
    @PostMapping("/{userId}/points/charge")
    public ResponseEntity<PointResponse> chargePoint(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Integer userId,
            @Valid @RequestBody ChargePointRequest request) {

        PointResponse point = pointUseCase.chargePoint(userId, request.getAmount());
        return ResponseEntity.ok(point);
    }
}