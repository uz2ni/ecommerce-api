# Redis 기반 시스템 개선 보고서

## 목차
1. [개요](#1-개요)
2. [판매 랭킹 시스템](#2-판매-랭킹-시스템)
3. [선착순 쿠폰 발급 시스템](#3-선착순-쿠폰-발급-시스템)
4. [Redis 설정 및 관리](#4-redis-설정-및-관리)

---

## 1. 개요

### 1.1 개선 배경
이커머스 서비스의 핵심 기능인 **판매 랭킹 조회**와 **선착순 쿠폰 발급**에서 다음과 같은 문제가 발생할 수 있습니다.

**판매 랭킹 조회 문제**
- 매 요청마다 DB 집계 쿼리 실행으로 인한 성능 저하
- 대용량 트래픽 시 DB 부하 증가

**선착순 쿠폰 발급 문제**
- 기존 DB 락, 분산 락은 순서 보장 어려움
- 대용량 트래픽 요청 시 성능 저하

### 1.2 개선 방향
Redis를 활용하여 다음과 같이 시스템을 개선했습니다.

1. **Redis Sorted Set**: 판매 랭킹 실시간 집계(비동기) 및 조회
2. **Redis Stream**: 쿠폰 발급 메시지 큐 (비동기)

---

## 2. 판매 랭킹 시스템

### 2.1 아키텍처

#### 기존 방식 (DB 기반)
```
[주문 완료] → [DB 저장] → [랭킹 조회 시 매번 DB 집계]
   ↓
문제점:
- 매 요청마다 복잡한 GROUP BY, COUNT 쿼리 실행
- DB 부하 증가
```

#### 개선 방식 (Redis Sorted Set)
```
[주문 완료] → [DB 저장] → [Redis Sorted Set 판매량 증가]
                               ↓
                          [랭킹 조회 시 Redis에서 즉시 반환]

장점:
- O(log N) 시간 복잡도로 빠른 조회
- DB 부하 감소
```

### 2.2 Redis 자료구조

**Redis Sorted Set 활용**

```redis
# 일간 랭킹 (2025-12-04 기준)
ZINCRBY store:ranking:sales:daily:2025-12-04 5 "productId:1"
ZINCRBY store:ranking:sales:daily:2025-12-04 3 "productId:2"

# 주간 랭킹 (2025년 49주차)
ZINCRBY store:ranking:sales:weekly:2025-W49 5 "productId:1"
ZINCRBY store:ranking:sales:weekly:2025-W49 3 "productId:2"
```

**Key 구조**
- 일간 랭킹: `store:ranking:sales:daily:{YYYY-MM-DD}`
- 주간 랭킹: `store:ranking:sales:weekly:{YYYY-Www}`
- Score: 판매 수량
- Member: productId

**TTL 관리**
- 일간 랭킹: 2일 후 자동 삭제
- 주간 랭킹: 8일 후 자동 삭제

---

## 3. 선착순 쿠폰 발급 시스템

### 3.1 아키텍처

#### 3.1.1 대기열 처리 방식 (Redis Stream)

```
[요청] → [검증] → [Stream Publisher 발행] → [즉시 응답 (PENDING)]
                       ↓
                  [Consumer 수신]
                       ↓
                [쿠폰 발급 접수 처리]
                       ↓
                   [ACK 처리]

특징:
- 즉시 응답 (낮은 대기 시간)
- 대용량 트래픽 처리 가능
- 비동기 처리로 DB 부하 분산
```

### 3.2 Redis 자료구조

#### 3.2.1 Redis Stream 구조

**Stream Key**
```
store:coupon:issue:stream
```

**Consumer Group**
```
store:coupon:issue:group
```

**Consumer Name**
```
store:coupon:issue:consumer-1
```

**메시지 구조**
```json
{
  "couponId": "1",
  "userId": "123"
}
```

### 3.3 구현 상세

#### 3.3.1 비동기 쿠폰 발급 흐름

**1단계: Publisher - 발급 요청 접수**

**CouponService.java** (src/main/java/com/example/ecommerceapi/coupon/application/service/CouponService.java:96-126)
```java
@Transactional
public IssueCouponResult issueCouponAsync(IssueCouponCommand command) {
    // 1. 기본 검증
    User user = userValidator.validateAndGetUser(command.userId());
    Coupon coupon = couponRepository.findById(command.couponId())
        .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));

    // 2. 쿠폰 발급 가능 여부 검증 (만료, 수량)
    coupon.validIssueCoupon();

    // 3. Redis Stream에 이벤트 발행
    String eventId = couponIssuePublisher.publish(
        command.couponId(),
        command.userId()
    );

    // 4. 즉시 응답 (PENDING 상태)
    return IssueCouponResult.pending(
        command.couponId(),
        command.userId(),
        eventId
    );
}
```

**CouponEventPublisher.java** (src/main/java/com/example/ecommerceapi/coupon/infrastructure/stream/publisher/CouponEventPublisher.java:28-46)
```java
@Override
public String publish(Integer couponId, Integer userId) {
    // Map 형태로 이벤트 데이터 생성
    Map<String, String> eventData = new HashMap<>();
    eventData.put("couponId", String.valueOf(couponId));
    eventData.put("userId", String.valueOf(userId));

    // MapRecord 생성
    MapRecord<String, String, String> record = StreamRecords.newRecord()
        .in(CouponStreamConstants.COUPON_STREAM_KEY)
        .ofMap(eventData);

    // Redis Stream에 발행 (XADD)
    RecordId recordId = redisTemplate.opsForStream().add(record);

    return recordId.getValue();
}
```

**2단계: Consumer - 이벤트 수신 및 처리**

**CouponEventConsumer.java** (src/main/java/com/example/ecommerceapi/coupon/presentation/consumer/CouponEventConsumer.java:30-62)
```java
@Override
public void onMessage(MapRecord<String, String, String> message) {
    RecordId recordId = message.getId();

    try {
        // 1. 메시지 파싱
        Integer couponId = Integer.valueOf(message.getValue().get("couponId"));
        Integer userId = Integer.valueOf(message.getValue().get("userId"));

        // 2. 쿠폰 발급 처리 (분산 락 + 트랜잭션)
        couponService.processCouponIssue(couponId, userId);

        // 3. ACK 처리 (XACK)
        streamAcknowledger.acknowledge(recordId);

    } catch (CouponException e) {
        // 비즈니스 예외는 재시도 불필요 → ACK 처리
        log.warn("Coupon issue failed: {}", e.getMessage());
        streamAcknowledger.acknowledge(recordId);

    } catch (Exception e) {
        // 시스템 예외는 재시도 가능 → ACK 하지 않음
        log.error("System error: {}", e.getMessage(), e);
    }
}
```

**3단계: 실제 발급 처리**

**CouponService.java** (src/main/java/com/example/ecommerceapi/coupon/application/service/CouponService.java:152-181)
```java
@Transactional
public void processCouponIssue(Integer couponId, Integer userId) {
    // 1. 회원 검증
    User user = userValidator.validateAndGetUser(userId);

    // 2. 쿠폰 검증
    Coupon coupon = couponRepository.findById(couponId)
        .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));

    // 3. 중복 발급 검증
    Optional<CouponUser> existingCouponUser =
        couponUserRepository.findByCouponIdAndUserId(couponId, userId);
    if (existingCouponUser.isPresent()) {
        throw new CouponException(ErrorCode.COUPON_ALREADY_ISSUED);
    }

    // 4. 쿠폰 발급
    coupon.issueCoupon();
    couponRepository.save(coupon);

    // 5. 발급 이력 생성
    CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);
    couponUserRepository.save(couponUser);
}
```

#### 3.6.2 재시도 전략

**Consumer 재시도**
- 비즈니스 예외 (쿠폰 소진, 중복 발급 등): 즉시 ACK 처리 (재시도 불필요)
- 시스템 예외 (네트워크, DB 장애 등): ACK 하지 않음 (Redis Stream 자동 재시도)

```java
try {
    couponService.processCouponIssue(couponId, userId);
    streamAcknowledger.acknowledge(recordId);
} catch (CouponException e) {
    // 비즈니스 예외 → ACK
    streamAcknowledger.acknowledge(recordId);
} catch (Exception e) {
    // 시스템 예외 → ACK 하지 않음 (재시도)
}
```

---

## 4. Redis 설정 및 관리

### 4.1 Redis 자료구조 타입 관리

**CacheType.java** (src/main/java/com/example/ecommerceapi/common/redis/CacheType.java)
```java
public enum CacheType implements RedisType {
    ALL_PRODUCTS("allProducts", Duration.ofMinutes(30)),
    PRODUCT("product", Duration.ofMinutes(30)),
    POPULAR_PRODUCTS_SALES("popularProducts:SALES", Duration.ofMinutes(5)),
    POPULAR_PRODUCTS_VIEWS("popularProducts:VIEWS", Duration.ofMinutes(3)),
    ORDER("order", Duration.ofMinutes(60)),
    ALL_COUPONS("allCoupons", Duration.ofMinutes(60));

    private final String cacheName;
    private final Duration ttl;
}
```

**StorageType.java** (src/main/java/com/example/ecommerceapi/common/redis/StorageType.java)
```java
public enum StorageType implements RedisType {
    DAILY_SALES_RANKING("store:ranking:sales:daily", Duration.ofDays(2)),
    WEEKLY_SALES_RANKING("store:ranking:sales:weekly", Duration.ofDays(8));

    private final String keyPrefix;
    private final Duration ttl;

    public String getKeyWithDate(LocalDate date) {
        return keyPrefix + ":" + date;
    }
}
```

### 4.2 TTL 전략

| Redis Key | TTL | 이유 |
|-----------|-----|------|
| 일간 랭킹 | 2일 | 어제/오늘 데이터 조회 가능 |
| 주간 랭킹 | 8일 | 지난 주 데이터 조회 가능 |
| 상품 캐시 | 30분 | 상품 정보는 자주 변경되지 않음 |
| 인기 상품 (판매) | 5분 | 주문 데이터는 상대적으로 느리게 변경 |
| 인기 상품 (조회) | 3분 | 조회수는 빈번하게 변경 |
| 쿠폰 캐시 | 60분 | 쿠폰 정보는 거의 변경되지 않음 |