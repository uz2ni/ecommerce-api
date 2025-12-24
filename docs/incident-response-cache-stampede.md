# 장애 대응 보고서 - 인기상품 조회 API 캐시 적용 후 실패율 증가

## 1. 장애 개요

### 발생 일시
- 부하 테스트 중 4분 20초 ~ 5분 10초 구간

### 증상
- HTTP 타임아웃 급증: 6건 → 38건 (6.3배)
- 실패율 증가: 0.01% → 0.06% (6배)
- 피크 구간 집중 발생

### 영향 범위
- API: `GET /api/products/popular?type=SALES&days=3&limit=5`
- 300 VU 부하 테스트 환경

## 2. 원인 분석

### 근본 원인: Cache Stampede (캐시 스탬피드)

**발생 메커니즘:**
```
캐시 만료 → 300개 동시 요청 → 모두 캐시 미스 → 300개 DB 쿼리 동시 실행 → DB 과부하 → 타임아웃
```

### 기술적 원인

#### 1) @Cacheable의 동시성 제어 부재
```java
// ProductService.java:60
@Cacheable(value = "popularProducts", key = "#type + ':' + #days + ':' + #limit")
public List<PopularProductResult> getPopularProducts(...)
```
- 캐시 미스 시 모든 스레드가 동시에 DB 조회 실행
- `sync = true` 옵션 미적용

#### 2) 고비용 집계 쿼리
```java
// ProductService.java:82-84
orderItemRepository.findAllOrderByOrderQuantityDesc(OrderStatus.PAID, startDate, pageRequest)
```
- ORDER_ITEM 테이블 풀 스캔 + GROUP BY + ORDER BY
- 동시 300개 실행 시 DB 부하 급증

#### 3) 캐시 설정 불일치
```java
// 실제 사용: "popularProducts" (기본 TTL 10분)
// 정의된 설정: "popularProducts:SALES" (TTL 5분), "popularProducts:VIEWS" (TTL 3분)
```

## 3. 성능 비교

| 지표 | AS-IS (캐시 없음) | TO-BE (캐시 적용) | 변화 |
|------|------------------|------------------|------|
| **평균 응답 시간** | 17.19ms | 13.35ms | ✓ 22% 개선 |
| **p(95) 응답 시간** | 48.23ms | 35.55ms | ✓ 26% 개선 |
| **p(99) 응답 시간** | 155.07ms | 95.3ms | ✓ 38% 개선 |
| **HTTP 실패율** | 0.01% (6건) | 0.06% (38건) | ✗ 6배 증가 |
| **타임아웃** | 6건 | 38건 | ✗ 6.3배 증가 |

**결론:** 응답 속도는 개선되었으나 안정성 저하

## 4. 해결 방안

### 즉시 조치 (Immediate Fix)

#### 4-1) @Cacheable sync 옵션 활성화
```java
@Cacheable(
    value = CacheType.Names.POPULAR_PRODUCTS_SALES,
    key = "#type + ':' + #days + ':' + #limit",
    sync = true  // ✓ 추가: 캐시 미스 시 하나의 스레드만 DB 조회
)
```

**효과:**
- 캐시 미스 시 첫 번째 스레드만 DB 조회
- 나머지 스레드는 대기 후 캐시된 결과 사용
- DB 동시 쿼리 300개 → 1개로 감소

#### 4-2) 캐시 이름 수정
```java
// Before
@Cacheable(value = "popularProducts", ...)

// After
@Cacheable(value = CacheType.Names.POPULAR_PRODUCTS_SALES, ...)
```

**효과:**
- 정의된 TTL 적용 (5분)
- 캐시 정책 일관성 확보

### 중기 개선 (Medium-term)

#### 4-3) 캐시 워밍업 추가
```java
@Scheduled(fixedRate = 240000) // 4분마다
public void warmUpPopularProductsCache() {
    getPopularProducts("SALES", 3, 5);
}
```

**효과:**
- 캐시 만료 전 미리 갱신
- 사용자 요청 시 캐시 히트율 증가

#### 4-4) DB 인덱스 최적화 확인
```sql
-- ORDER_ITEM 테이블 인덱스 확인
SHOW INDEX FROM order_item WHERE Key_name LIKE '%status%' OR Key_name LIKE '%created_at%';
```

**효과:**
- 집계 쿼리 성능 향상
- 캐시 미스 시에도 빠른 응답

### 장기 개선 (Long-term)

#### 4-5) 배치 집계 + 캐시 전용 테이블
```java
@Scheduled(cron = "0 */10 * * * *") // 10분마다
public void aggregatePopularProducts() {
    // 집계 결과를 별도 테이블에 저장
    // 조회는 집계 테이블에서만
}
```

**효과:**
- 실시간 집계 부하 제거
- 안정적인 응답 시간 보장

## 5. 재발 방지

### 체크리스트
- [ ] 모든 @Cacheable에 `sync = true` 검토
- [ ] 캐시 이름과 CacheType 정의 일치 확인
- [ ] 고비용 쿼리는 캐시 워밍업 적용
- [ ] 부하 테스트 시 캐시 만료 시나리오 포함

### 모니터링
```java
// 캐시 히트율 모니터링 추가
@Aspect
public class CacheMonitoringAspect {
    // 캐시 미스/히트 로깅
}
```

## 6. 참고 자료

### 관련 파일
- `ProductService.java:60` - 문제 발생 지점
- `CacheType.java:17-18` - 캐시 설정 정의
- `RedisConfig.java:113-131` - 캐시 매니저 설정

### 테스트 결과
- AS-IS: `docs/products-popular-list-peak-asis.md`
- TO-BE: `docs/products-popular-list-peak-tobe.md`
