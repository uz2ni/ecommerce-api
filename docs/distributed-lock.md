# 분산 락 전환 보고서

## 개요

JPA 기반 락 전략에서 Redis 분산 락으로 전환하여 다중 서버 환경에서의 동시성 제어를 개선했습니다.

## 변경 사항

### 이전: JPA 기반 락 (DB 레벨)

**비관적 락**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Coupon> findByIdWithPessimisticLock(Integer couponId);
```
- 쿠폰 발급, 재고 차감에 적용
- DB 행 단위 락 획득
- 순차 처리로 정확성 보장

**낙관적 락**
```java
@Version
private Integer version;

@Retryable(retryFor = {OptimisticLockException.class}, maxAttempts = 5)
public void chargePointsWithRetry(User user, Integer amount) {
    user.chargePoints(amount);
    userRepository.save(user);
}
```
- 포인트 충전/차감, 쿠폰 사용에 적용
- 버전 필드로 충돌 감지
- 충돌 시 재시도 로직 필요

**문제점**
- 단일 DB 인스턴스에만 유효
- 다중 서버 환경에서 동시성 제어 불가능
- DB 커넥션 점유로 인한 성능 병목
- 락 전략 선택의 유연성 부족

### 현재: Redis 분산 락 (애플리케이션 레벨)

**AOP 기반 선언적 락 처리**
```java
@DistributedLock(key = "'coupon' + #command.couponId", type = LockType.PUB_SUB)
@Transactional
public IssueCouponResult issueCoupon(IssueCouponCommand command) {
    // 비즈니스 로직
}
```

**4가지 락 전략**

| 타입 | 설명 | 적용 대상 | 특징 |
|------|------|----------|------|
| **SIMPLE** | 기본 RLock, 즉시 실패 | 포인트 충전 | 빠른 실패, waitTime=0 |
| **SPIN** | 짧은 간격 폴링 | - | 짧은 작업, CPU 사용 ↑ |
| **PUB_SUB** | 이벤트 기반 대기 | 쿠폰 발급 | 긴 대기 허용, CPU 절약 |
| **MULTI** | 여러 리소스 원자적 락 | 결제 (주문+재고) | 데드락 방지 |

**구현 구조**
```
@DistributedLock (어노테이션)
    ↓
DistributedLockAspect (AOP)
    ↓
LockStrategyFactory (전략 선택)
    ↓
SimpleLock / SpinLock / PubSubLock / MultiLock (전략 구현)
    ↓
RedissonClient (Redisson 라이브러리)
```

## 적용 사례

### 1. 쿠폰 발급 (PUB_SUB)
```java
@DistributedLock(key = "'coupon' + #command.couponId", type = LockType.PUB_SUB,
                 waitTime = 5, leaseTime = 10)
@Transactional
public IssueCouponResult issueCoupon(IssueCouponCommand command)
```
- **선택 이유**: 선착순 이벤트로 대기 허용, 이벤트 기반으로 CPU 절약
- **변경 전**: `findByIdWithPessimisticLock()` → DB 커넥션 점유
- **변경 후**: Redis Pub/Sub → DB 부하 없음

### 2. 포인트 충전 (SIMPLE)
```java
@DistributedLock(key = "'point:' + #userId", type = LockType.SIMPLE)
@Transactional
public PointResult chargePoint(Integer userId, Integer amount)
```
- **선택 이유**: 충돌 드물어 즉시 실패 후 사용자 재시도
- **변경 전**: `@Version` + `@Retryable` → 복잡한 재시도 로직
- **변경 후**: 락 획득 실패 시 즉시 예외 → 간결한 코드

### 3. 결제 (SIMPLE)
```java
@DistributedLock(key = "'point:' + #userId", type = LockType.SIMPLE)
@Transactional
public PaymentResult processPayment(Integer orderId, Integer userId)
```
- **선택 이유**: 포인트 리소스만 분산 락으로 제어 (동일 사용자 동시 결제 방지)
- **변경 전**: 비관적 락 개별 적용 → 데드락 가능성
- **변경 후**: 포인트 차감은 분산 락, 재고/쿠폰은 DB 레벨 락 유지

## 현재 락 적용 현황

### 동시성 제어 지점별 적용 전략

| 동시성 제어 지점 | 락 전략 | 락 타입 | 락 키 | 적용 위치 | 비고 |
|---------------|---------|---------|-------|----------|------|
| **쿠폰 발급** | Redis 분산 락 | PUB_SUB | `coupon:{couponId}` | `CouponService.issueCoupon()` | 완전 전환 ✓ |
| **포인트 충전** | Redis 분산 락 | SIMPLE | `point:{userId}` | `PointService.chargePoint()` | 완전 전환 ✓ |
| **결제 - 전체** | Redis 분산 락 | SIMPLE | `point:{userId}` | `OrderService.processPayment()` | 부분 전환 |
| **결제 - 포인트 차감** | 분산 락으로 보호 | - | (상위 SIMPLE 락) | `processPayment()` 내부 | 분산 락 내에서 처리 |
| **결제 - 재고 차감** | JPA 비관적 락 | PESSIMISTIC_WRITE | - | `ProductRepository.findByIdWithLock()` | **JPA 락 유지** ⚠️ |
| **결제 - 쿠폰 사용** | JPA 낙관적 락 | @Version | - | `OrderService.useCouponWithOptimisticLock()` | **JPA 락 유지** ⚠️ |

### 혼용 전략 상세

**결제 프로세스 (processPayment)**
```java
@DistributedLock(key = "'point:' + #userId", type = LockType.SIMPLE)
@Transactional
public PaymentResult processPayment(Integer orderId, Integer userId) {
    // 1. 포인트 차감 - 분산 락(SIMPLE)으로 보호
    user.usePoints(paymentAmount);

    // 2. 재고 차감 - 비관적 락 사용
    Product product = productRepository.findByIdWithLock(productId);
    product.decreaseStock(quantity);

    // 3. 쿠폰 사용 - 낙관적 락(@Version) 사용
    useCouponWithOptimisticLock(couponId, userId);
}
```

**혼용 이유**
1. **포인트 차감 (분산 락 SIMPLE)**
   - 동일 사용자의 동시 결제 방지가 주 목적
   - `point:{userId}` 키로 사용자별 락 제어
   - 주문 간 독립성 보장 (orderId 락 불필요)

2. **재고 차감 (비관적 락 유지)**
   - 여러 주문에서 동일 상품 재고를 차감하는 경우
   - 상품별 재고 보호를 위해 DB 레벨 비관적 락 유지
   - 향후 `product:{productId}` 분산 락 추가 고려 가능
   - 분산 락 미적용 이유: product는 메서드 내에서 조회하는 값이라 파라미터로 락 키를 가져올 수 없었음

3. **쿠폰 사용 (낙관적 락 유지)**
   - 한 사용자가 동일 쿠폰을 여러 주문에서 동시 사용하는 경우 드뭄
   - 분산 락으로 `point:{userId}` 보호하므로 동일 사용자 동시 결제 불가
   - `@Version` 필드로 충분히 보호 가능
   - 성능상 오버헤드 최소화
   - 분산 락 미적용 이유: couponUser는 메서드 내에서 조회하는 값이라 파라미터로 락 키를 가져올 수 없었음

### 락 전략 조합의 장점

- **계층적 보호**: 애플리케이션(Redis) → DB(JPA) 레벨 다중 방어
- **성능 최적화**: 빈번한 충돌 지점만 분산 락 적용
- **점진적 전환**: 핵심 지점부터 단계적으로 전환 가능
- **데이터 정합성**: 여러 락 전략의 조합으로 강력한 보호

## 전환 이유

### 1. 다중 서버 환경 대응
- **문제**: JPA 락은 단일 DB 인스턴스 내에서만 유효
- **해결**: Redis를 중앙 락 저장소로 사용하여 모든 서버 간 동기화

### 2. 성능 개선
- **DB 부하 감소**: 락 관리를 Redis로 분리하여 DB 커넥션 절약
- **응답 시간 단축**: Redis 메모리 기반 처리로 락 획득/해제 속도 향상
- **커넥션 효율**: 비관적 락의 긴 커넥션 점유 문제 해결

### 3. 유연성 향상
- **전략 선택**: 비즈니스 특성에 맞는 락 타입 선택 가능
- **동적 키**: SpEL 표현식으로 런타임 락 키 생성
- **세밀한 제어**: waitTime, leaseTime을 상황별로 조정

### 4. 운영 효율성
- **선언적 코드**: AOP로 비즈니스 로직과 락 처리 분리
- **자동 해제**: leaseTime으로 데드락 방지 (락 누수 없음)
- **모니터링**: Redis를 통한 중앙 집중식 락 상태 추적


## 주요 개선 지표

| 항목 | 이전 (JPA 락) | 현재 (Redis 분산 락) |
|------|--------------|-------------------|
| **확장성** | 단일 DB만 지원 | 다중 서버 지원 |
| **DB 부하** | 락으로 인한 커넥션 점유 | 락 처리를 Redis로 분리 |
| **유연성** | 고정된 2가지 전략 | 4가지 전략 선택 가능 |
| **코드 복잡도** | Repository + 재시도 로직 | AOP + 어노테이션 |
| **데드락 위험** | 여러 리소스 락 시 위험 | MultiLock으로 방지 |

## 마이그레이션 작업

### 1. 인프라 구성
- Redis 서버 설치 및 Redisson 의존성 추가
- RedissonClient 빈 설정

### 2. 공통 모듈 구현
- `@DistributedLock` 어노테이션
- `DistributedLockAspect` (AOP)
- 4가지 락 전략 구현
- `LockStrategyFactory` (팩토리 패턴)

### 3. 서비스 레이어 변경
- Repository 락 메서드 제거
- 서비스 메서드에 `@DistributedLock` 적용
- 엔티티 `@Version` 필드 및 재시도 로직 제거

### 4. 테스트 개선
- `AbstractIntegrationTest`에 Redis 락 정리 유틸리티 추가
- 기존 동시성 테스트 검증 통과

## 결론

Redis 분산 락 도입으로 **확장 가능하고, 성능이 개선되며, 유지보수가 용이한** 동시성 제어 시스템을 구축했습니다. 비즈니스 특성에 맞는 락 전략을 선택할 수 있어 유연성이 높아졌으며, 선언적 코드로 가독성도 향상되었습니다.

다중 서버 환경에서도 안정적으로 동작하며, DB 부하를 Redis로 분산하여 전체 시스템 성능이 개선되었습니다.