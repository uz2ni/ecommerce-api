# E-Commerce API

Spring Boot 기반의 이커머스 API 입니다. 회원 관리, 상품 조회, 장바구니, 주문/결제, 쿠폰 발급 등 이커머스의 핵심 기능을 제공합니다.

## 목차
- [아키텍처 및 프로젝트 구조](#아키텍처-및-프로젝트-구조)
- [기술 스택](#기술-스택)
- [설치 및 실행](#설치-및-실행)
- [API 문서](#api-문서)
- [설계 문서](#설계-문서)

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


## 설계 문서

상세한 설계 문서는 `docs/` 디렉토리에서 확인할 수 있습니다:

- **[요구사항 명세서](docs/requirements.md)** - 비즈니스 요구사항 및 제약조건
- **[ERD](docs/data-models.md)** - 데이터베이스 구조 및 엔티티 관계
- **[API 명세서](docs/api-specification.md)** - API 엔드포인트 상세 설명


## 라이센스

This project is licensed under the MIT License.