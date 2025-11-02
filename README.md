# E-Commerce API

Spring Boot 기반의 이커머스 API 입니다. 회원 관리, 상품 조회, 장바구니, 주문/결제, 쿠폰 발급 등 이커머스의 핵심 기능을 제공합니다.

## 목차
- [기술 스택](#기술-스택)
- [설치 및 실행](#설치-및-실행)
- [API 문서](#api-문서)
- [설계 문서](#설계-문서)


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