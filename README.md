# E-Commerce API

Spring Boot 기반의 이커머스 API 입니다. 회원 관리, 상품 조회, 장바구니, 주문/결제, 쿠폰 발급 등 이커머스의 핵심 기능을 제공합니다.

## 목차
- [아키텍처 및 프로젝트 구조](#아키텍처-및-프로젝트-구조)
- [동시성 제어](#동시성-제어)
- [기술 스택](#기술-스택)
- [설치 및 실행](#설치-및-실행)
- [API 문서](#api-문서)
- [설계 & 보고서 문서](#설계-&-보고서-문서)

## 아키텍처 및 프로젝트 구조

**레이어드 아키텍처 (Layered Architecture)**

이 프로젝트는 레이어드 아키텍처를 채택하여 관심사의 분리와 유지보수성을 확보합니다.

### 프로젝트 구조

```
├── presentation    (프레젠테이션 레이어)
│   └── controller 
│   └── dto         // request, response DTO
├── application      (애플리케이션 레이어)
│   └── service     // 여러 entity, repository 조합한 비즈니스 로직
│   └── dto         // application layer DTO
│   └── enums       // service 내 사용 enum
│   └── validator   // 해당 도메인의 검증 로직 (다른 도메인에서 사용할 수 있도록 분리)
├── domain          (도메인 레이어)
│   └── entity      // 도메인 객체, 도메인에 해당하는 비즈니스 로직
└── infrastructure  (인프라스트럭처 레이어)
    └── InMemoryXXRepository // 인메모리 저장소 
```

### 각 레이어의 역할

- **Presentation Layer**: HTTP 요청/응답 처리, DTO 변환
- **Application Layer**: 비즈니스 유스케이스 조율, 트랜잭션 관리
- **Domain Layer**: 핵심 비즈니스 로직 및 도메인 모델
- **Infrastructure Layer**: 데이터베이스 접근, 외부 시스템 연동

### 선정 이유

1. **명확한 책임 분리**: 각 레이어가 명확한 역할을 가지고 있어 코드의 응집도가 높고 결합도가 낮음
2. **테스트 용이성**: 레이어별로 독립적인 테스트 작성이 가능하며, 모킹이 용이함
3. **유지보수성**: 변경 사항이 특정 레이어에 국한되어 영향 범위가 제한적임
4. **확장성**: 새로운 기능 추가 시 기존 레이어 구조를 따라 일관되게 구현 가능
5. **팀 협업**: 레이어별로 작업을 분담하기 용이하여 병렬 개발에 유리함

## 동시성 제어

이커머스 시스템에서 발생할 수 있는 동시성 문제를 해결하기 위해 **커스텀 락(Lock) 메커니즘**을 구현했습니다.

### 구현 방식

**@WithLock 어노테이션 + AOP (Aspect)**

```java
@WithLock(key = "'issueCoupon:' + #command.couponId")
public IssueCouponResult issueCoupon(IssueCouponCommand command) {
    // 쿠폰 발급 로직
}
```

#### 핵심 컴포넌트

1. **@WithLock 어노테이션**
   - SpEL 표현식을 사용하여 락 키를 동적으로 지정
   - `timeout`: 락 획득 대기 시간 설정 (기본 3초)
   - `ignoreIfLocked`: 락 획득 실패 시 즉시 반환 여부 (기본 false)

2. **LockAspect**
   - `ConcurrentHashMap<Object, ReentrantLock>` 기반 락 관리
   - 공정한 락 획득을 위해 `ReentrantLock(true)` 사용
   - 락 키별로 독립적인 락 객체 생성 및 관리

#### 기술적 특징

- **세밀한 락 제어**: 전역 락이 아닌 키 기반 락으로 성능 최적화
- **타임아웃 설정**: 데드락 방지 및 응답 시간 보장
- **SpEL 지원**: 메서드 파라미터를 활용한 동적 락 키 생성

### 적용 사례

#### 1. 쿠폰 발급 (CouponService)

**문제 상황**
- 동일 사용자가 같은 쿠폰을 동시에 여러 번 요청
- 제한된 수량의 쿠폰을 여러 사용자가 동시에 요청

**해결 방법**
```java
@WithLock(key = "'issueCoupon:' + #command.couponId")
public IssueCouponResult issueCoupon(IssueCouponCommand command)
```
- 쿠폰 ID를 기준으로 락 획득
- 동일 쿠폰에 대한 동시 발급 요청을 순차 처리하여 중복 발급 방지

**검증**
- 20개 스레드가 동시에 동일 사용자로 발급 시도 → 1개만 발급 성공
- 20개 사용자가 마지막 1개 쿠폰 발급 시도 → 1명만 성공

#### 2. 주문 결제 (OrderService)

**문제 상황**
- 동일 주문에 대해 여러 번 결제 시도 (중복 결제)
- 재고 차감, 포인트 차감 시 경쟁 조건 발생

**해결 방법**
```java
@WithLock(key = "'processPayment:' + #userId")
public PaymentResult processPayment(Integer orderId, Integer userId)
```
- 사용자 ID를 기준으로 락 획득
- 동일 사용자의 동시 결제를 순차 처리하여 중복 결제 방지
- 보상 트랜잭션을 통해 결제 실패 시 재고/포인트 원상복구

**검증**
- 10개 스레드가 동일 주문 결제 시도 → 1번만 성공, 포인트 1회만 차감
- 서로 다른 사용자의 서로 다른 주문 → 모두 정상 처리 (락 경합 없음)

#### 3. 포인트 충전 (PointService)

**문제 상황**
- 동일 사용자가 동시에 여러 번 포인트 충전

**해결 방법**
```java
@WithLock(key = "'chargePoint:' + #userId", ignoreIfLocked = true)
public PointResult chargePoint(Integer userId, Integer amount)
```
- 사용자 ID를 기준으로 락 획득
- `ignoreIfLocked = true`: 락 획득 실패 시 즉시 null 반환 (타임아웃 없음)

### 동시성 테스트

모든 동시성 제어 로직은 통합 테스트로 검증되었습니다.

**테스트 방법**
- `ExecutorService` + `CountDownLatch`를 사용한 멀티스레드 환경 시뮬레이션
- 일반적으로 10~20개 스레드로 동시 요청 테스트
- `AtomicInteger`로 성공/실패 카운트 추적

**테스트 케이스**
- `CouponServiceConcurrencyIntegrationTest`: 쿠폰 중복 발급 방지, 선착순 처리
- `OrderServiceConcurrencyIntegrationTest`: 중복 결제 방지, 보상 트랜잭션
- `PointServiceConcurrencyIntegrationTest`: 포인트 충전 동시성 제어

### 선택 이유

**다른 방식 대비 장점**

1. **synchronized 대비**
   - 세밀한 락 제어 가능 (키 기반)
   - 타임아웃 설정 가능
   - 공정성 보장 (fair lock)

2. **@Transactional + DB 락 대비**
   - 인메모리 저장소 사용 시에도 동작
   - DB 부하 감소
   - 응답 속도 향상

3. **분산 락(Redis) 대비**
   - 단일 인스턴스 환경에 적합
   - 외부 의존성 없음
   - 구현 및 테스트 용이

**한계점**
- 단일 애플리케이션 인스턴스에서만 동작
- 다중 인스턴스 환경에서는 분산 락(Redis, Zookeeper 등) 필요


## 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.3.1**
- **Gradle**

### Documentation
- **SpringDoc OpenAPI 3** (Swagger UI)


## 설치 및 실행

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd ecommerce-api
```

### 2. 빌드
```bash
# Gradle Wrapper 사용 (권장)
./gradlew build

# 또는 시스템 Gradle 사용
gradle build
```

### 3. 실행
```bash
# Gradle Wrapper 사용
./gradlew bootRun

# 또는 빌드된 JAR 파일 직접 실행
java -jar build/libs/ecommerce-api-0.0.1-SNAPSHOT.jar
```

### 4. 애플리케이션 확인
서버가 정상적으로 시작되면 다음 주소로 접속할 수 있습니다:
- **API 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### 5. 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests com.example.ecommerceapi.YourTestClass

# 특정 테스트 메서드만 실행
./gradlew test --tests com.example.ecommerceapi.YourTestClass.testMethod
```

## API 문서

### Swagger UI
애플리케이션 실행 후 Swagger UI를 통해 모든 API를 테스트할 수 있습니다:

**URL**: http://localhost:8080/swagger-ui.html

### API 사용 예시

#### 1. 장바구니에 상품 추가
```bash
curl -X POST http://localhost:8080/api/cart \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2
  }'
```


## 설계 & 보고서 문서

상세한 설계 & 보고서 문서는 `docs/` 디렉토리에서 확인할 수 있습니다:

- **[요구사항 명세서](docs/requirements.md)** - 비즈니스 요구사항 및 제약조건
- **[ERD](docs/data-models.md)** - 데이터베이스 구조 및 엔티티 관계
- **[API 명세서](docs/api-specification.md)** - API 엔드포인트 상세 설명
- **[Sequence Diagram](docs/sequence.md)** - 주요 기능별 시퀀스 다이어그램
- **[쿼리 최적화 보고서](docs/query-optimization.md)** - 쿼리 최적화 방안 설명
- **[동시성 문제 처리 방안 보고서](docs/lock.md)** - 동시성 제어 설계 및 구현
- **[분산 락 전환 보고서](docs/distributed-lock.md)** - Redis 분산 락 전환 과정 및 성능 분석
- **[성능 개선 보고서](docs/performance-improvement.md)** - Redis 캐싱 적용 및 성능 개선 결과

## 라이센스

This project is licensed under the MIT License.