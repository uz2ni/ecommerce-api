# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

E-commerce API built with Spring Boot 3.5.7, Java 17, and Gradle. This project implements core e-commerce features including products, shopping cart, orders/payments, and coupons.

## Build & Run Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests com.example.ecommerceapi.YourTestClass

# Run a single test method
./gradlew test --tests com.example.ecommerceapi.YourTestClass.testMethod

# Clean build
./gradlew clean build
```

## Architecture

### Domain Structure (docs/requirements.md, docs/erd.md)

**Core entities:**
- USER: 회원 (사전 등록됨)
  - POINT: 포인트 이력
- PRODUCT: 상품 (단일 상품, 옵션 없음, 사전 등록됨)
- CART_ITEM: 장바구니 (수량 변경 시 삭제 후 재등록)
- ORDER: 주문
  - ORDER_ITEM: 주문 상품
- COUPON: 선착순 쿠폰 (사전 등록됨)
  - COUPON_USER: 발급 이력

**Key business rules:**
- 장바구니에 담긴 상품만 주문 가능
- 주문 생성 시 재고 부족하면 실패
- 결제는 포인트 차감, 부족 시 실패
- 결제 완료 후 장바구니에서 주문 상품 제거
- 인기 상품: 최근 3일 기준 top5

### Technology Stack

- Spring Boot 3.5.7
- Java 17
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5

### API Documentation

Swagger UI available at `http://localhost:8080/swagger-ui.html` after running the application.

Use `@Tag` and `@Operation` annotations from `io.swagger.v3.oas.annotations` for API documentation.

## PR Guidelines

Follow the PR template in `.github/pull_request_template.md`:
- Include commit links with descriptions
- Add review points/questions
- Provide brief retrospective (잘한 점, 어려운 점, 다음 시도)