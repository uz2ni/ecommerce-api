# 쿼리 최적화 및 락 처리 방안 보고서

## 목차
1. [개요](#개요)
2. [병목 예상 쿼리 분석 및 개선 방안](#병목-예상-쿼리-분석-및-개선-방안)
3. [도메인별 락 처리 전략](#도메인별-락-처리-전략)
4. [전체 인덱스 목록 및 용도](#전체-인덱스-목록-및-용도)

---

## 개요

E-commerce API의 JPA 영속성 계층을 구현하였습니다. 본 보고서는 쿼리 성능 최적화 및 락 처리 방안에 대해 기술합니다.   

**주요 작업 범위:**
- JPA Repository 구현 (Product, Order, Cart, Coupon, Point 도메인)
- 인덱스 기반 쿼리 최적화
- 낙관적/비관적 락 적용

---

## 병목 예상 쿼리 분석 및 개선 방안

### 1. 인기 상품 조회 쿼리 최적화

#### 📍 위치
`src/main/java/com/example/ecommerceapi/order/infrastructure/persistence/JpaOrderItemRepository.java:30-45`

#### 🔍 문제 분석
인기 상품 조회는 최근 3일간의 주문 데이터를 집계하는 OLAP성 쿼리로, 다음과 같은 병목이 예상됩니다:

1. **Full Table Scan 위험**: `ORDER` 테이블 전체를 조회하여 날짜 필터링
2. **조인 비용**: `ORDER` → `ORDER_ITEM` → `PRODUCT` 3-way 조인
3. **GROUP BY 연산**: 상품별 판매량 집계로 인한 정렬 비용

#### ✅ 적용한 개선 방안

##### 1) 복합 인덱스 설계

**Order 엔티티** (`src/main/java/com/example/ecommerceapi/order/domain/Order.java:32-36`)
```java
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user_id", columnList = "user_id"),
    @Index(name = "idx_orders_status_createdat", columnList = "order_status, created_at")
})
```

**설계 근거:**
- `order_status`를 선행 컬럼으로 하여 WHERE 절의 등가 조건 활용
- `created_at`을 후행 컬럼으로 범위 검색 최적화
- 커버링 인덱스로 테이블 액세스 최소화

**실행 계획 비교:**

| 항목 | 인덱스 없음 | 복합 인덱스 적용 |
|------|-------------|-----------------|
| **접근 방식** | Table Scan | Index Range Scan |
| **스캔 행수** | ~100,000 rows | ~5,000 rows |
| **인덱스 사용** | - | idx_orders_status_createdat |
| **Extra** | Using where; Using filesort | Using index condition |

##### 2) 최적화된 JPQL 쿼리

```java
@Query("""
    SELECT p AS product,
            SUM(oi.orderQuantity) AS salesCount
    FROM OrderItem oi
    JOIN oi.order o
    JOIN oi.product p
    WHERE o.orderStatus = :status
      AND o.createdAt >= :startDate
    GROUP BY p
    ORDER BY SUM(oi.orderQuantity) DESC
    """)
List<PopularProductBySailsResult> findAllOrderByOrderQuantityDesc(
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        Pageable pageable
);
```

**최적화 포인트:**
- **조기 필터링**: WHERE 절에서 날짜 범위 먼저 제한
- **Projection 인터페이스**: 필요한 컬럼만 선택적으로 조회
- **페이지네이션**: `Pageable`로 TOP 5만 조회하여 메모리 절약

**생성되는 SQL:**
```sql
SELECT p.product_id,
       p.product_name,
       SUM(oi.order_quantity) as sales_count
FROM order_item oi
INNER JOIN orders o ON oi.order_id = o.order_id
INNER JOIN product p ON oi.product_id = p.product_id
WHERE o.order_status = 'PAID'
  AND o.created_at >= '2025-11-09 00:00:00'
GROUP BY p.product_id, p.product_name
ORDER BY sales_count DESC
LIMIT 5;
```

##### 3) 조인 테이블 인덱스

**OrderItem 엔티티**
```java
@Index(name = "idx_order_item_order_id", columnList = "order_id"),
@Index(name = "idx_order_item_product_id", columnList = "product_id")
```

- 조인 성능 향상을 위한 Foreign Key 인덱스
- Nested Loop Join 시 내부 테이블 조회 최적화

---

### 2. 상품 조회수 기반 인기 상품 쿼리

#### 📍 위치
`src/main/java/com/example/ecommerceapi/product/infrastructure/persistence/JpaProductRepository.java:19-21`

```java
@Query("SELECT p FROM Product p ORDER BY p.viewCount DESC")
List<Product> findAllOrderByViewCountDesc(Pageable pageable);
```

#### ✅ 개선 방안

**Product 엔티티 인덱스**
```java
@Index(name = "idx_product_view_count", columnList = "view_count")
```

**실행 계획:**
- `idx_product_view_count` 인덱스 스캔으로 정렬 없이 상위 5개 조회
- Using index (커버링 인덱스로 테이블 액세스 없음)

---

### 3. 장바구니 조회 쿼리

#### 📍 위치
`src/main/java/com/example/ecommerceapi/cart/infrastructure/persistence/JpaCartItemRepository.java:18-20`

```java
@Query("SELECT c FROM CartItem c WHERE c.user.userId = :userId")
List<CartItem> findByUserId(@Param("userId") Integer userId);
```

#### ✅ 개선 방안

**CartItem 엔티티 인덱스**
```java
@Index(name = "idx_cart_item_user_id", columnList = "user_id"),
@Index(name = "idx_cart_item_product_id", columnList = "product_id")
```

**최적화 효과:**
- 사용자별 장바구니 조회 시 인덱스 활용
- 상품 정보 조인 시 성능 향상

---

### 4. 쿠폰 관련 쿼리

#### 📍 위치
- `JpaCouponUserRepository.java:18-26` (중복 발급 체크)
- `JpaCouponUserRepository.java:28-30` (사용자별 쿠폰 조회)

#### ✅ 개선 방안

**CouponUser 엔티티 인덱스**
```java
@Index(name = "idx_coupon_user_coupon_id", columnList = "coupon_id"),
@Index(name = "idx_coupon_user_user_id", columnList = "user_id"),
@Index(name = "idx_coupon_user_used", columnList = "is_used")
```

**복합 쿼리 최적화:**
```java
// 중복 발급 체크 (복합 인덱스 활용)
@Query("""
    SELECT cu FROM CouponUser cu
    WHERE cu.coupon.couponId = :couponId
      AND cu.user.userId = :userId
      AND cu.isUsed = false
    """)
```

- `(coupon_id, user_id, is_used)` 조합으로 복합 인덱스 고려 가능
- 현재는 단일 컬럼 인덱스로 구현

---

## 도메인별 락/트랜잭션 처리 전략

| 도메인 | 락/트랜잭션 종류                 | 적용 위치 | 이유 |
|--------|---------------------------|-----------|------|
| **User** | 비관적 락 (PESSIMISTIC_WRITE) | 포인트 충전/차감 | 금전적 가치, 정확성 필수 |
| **Product** | 낙관적 락 (@Version)          | 재고 차감 | 낮은 충돌 빈도, 재시도 가능 |
| **Coupon** | 낙관적 락 (@Version)          | 발급 수량 증가 | 선착순 특성, 빠른 응답 필요 |
| **Order** | 트랜잭션 (@Transactional)     | 주문 생성/결제 | 복잡한 비즈니스 로직 |
| **CartItem** | 트랜잭션 (@Transactional)     | 추가/삭제 | 단순 CRUD 작업 |

---


## 전체 인덱스 목록 및 용도

| 테이블 | 인덱스명 | 컬럼 | 용도 |
|--------|----------|------|------|
| `user` | PRIMARY | user_id | 기본 키 |
| `product` | PRIMARY | product_id | 기본 키 |
| `product` | idx_product_view_count | view_count | 조회수 기반 정렬 |
| `cart_item` | idx_cart_item_user_id | user_id | 사용자별 장바구니 조회 |
| `cart_item` | idx_cart_item_product_id | product_id | 상품별 장바구니 조회 |
| `orders` | idx_orders_user_id | user_id | 사용자별 주문 조회 |
| `orders` | idx_orders_status_createdat | order_status, created_at | 인기 상품 쿼리 최적화 |
| `order_item` | idx_order_item_order_id | order_id | 주문-주문상품 조인 |
| `order_item` | idx_order_item_product_id | product_id | 상품-주문상품 조인 |
| `coupon` | idx_coupon_expired_at | expired_at | 만료 쿠폰 필터링 |
| `coupon_user` | idx_coupon_user_coupon_id | coupon_id | 쿠폰별 발급 내역 |
| `coupon_user` | idx_coupon_user_user_id | user_id | 사용자별 쿠폰 조회 |
| `coupon_user` | idx_coupon_user_used | is_used | 사용 가능 쿠폰 필터링 |