# API 명세서

## 목차
- [1. 회원 API](#1-회원-api)
- [2. 상품 API](#2-상품-api)
- [3. 장바구니 API](#3-장바구니-api)
- [4. 주문/결제 API](#4-주문결제-api)
- [5. 쿠폰 API](#5-쿠폰-api)

---

## 1. 회원 API

### 1.1 회원 목록 조회
전체 회원 목록을 조회합니다.

**Endpoint**
```
GET /api/users
```

**Response**
```json
[
  {
    "userId": 1,
    "username": "김철수"
  },
  {
    "userId": 2,
    "username": "홍길동"
  }
]
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Integer | 회원 ID |
| username | String | 회원 이름 |

---

### 1.2 회원 정보 조회
회원 ID로 회원 정보를 조회합니다.

**Endpoint**
```
GET /api/users/{userId}
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |

**Response**
```json
{
  "userId": 1,
  "username": "김철수"
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Integer | 회원 ID |
| username | String | 회원 이름 |

**제약조건**
- 존재하지 않는 회원 ID인 경우 404 Not Found

---

### 1.3 회원 포인트 잔액 조회
회원의 현재 포인트 잔액을 조회합니다.

**Endpoint**
```
GET /api/users/{userId}/points/balance
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |

**Response**
```json
{
  "userId": 1,
  "balance": 500000
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Integer | 회원 ID |
| balance | Integer | 현재 포인트 잔액 |

**제약조건**
- 존재하지 않는 회원 ID인 경우 404 Not Found

---

### 1.4 회원 포인트 이력 조회
회원의 포인트 충전/사용 이력을 조회합니다.

**Endpoint**
```
GET /api/users/{userId}/points/history
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |

**Response**
```json
[
  {
    "pointId": 1,
    "userId": 1,
    "pointType": "EARN",
    "pointAmount": 500000,
    "balance": 500000,
    "createdAt": "2025-10-22T10:00:00"
  },
  {
    "pointId": 2,
    "userId": 1,
    "pointType": "USE",
    "pointAmount": 50700,
    "balance": 449300,
    "createdAt": "2025-10-29T14:30:00"
  }
]
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| pointId | Integer | 포인트 이력 ID |
| userId | Integer | 회원 ID |
| pointType | String | 포인트 타입 (EARN: 충전, USE: 사용) |
| pointAmount | Integer | 포인트 금액 |
| balance | Integer | 해당 시점의 잔액 |
| createdAt | LocalDateTime | 생성일시 |

**제약조건**
- 존재하지 않는 회원 ID인 경우 404 Not Found

---

### 1.5 포인트 충전
회원의 포인트를 충전합니다.

**Endpoint**
```
POST /api/users/{userId}/points/charge
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |

**Request Body**
```json
{
  "amount": 100000
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| amount | Integer | O | 충전할 포인트 금액 |

**Response**
```json
{
  "pointId": 3,
  "userId": 1,
  "pointType": "EARN",
  "pointAmount": 100000,
  "balance": 600000,
  "createdAt": "2025-10-29T15:00:00"
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| pointId | Integer | 포인트 이력 ID |
| userId | Integer | 회원 ID |
| pointType | String | 포인트 타입 (EARN) |
| pointAmount | Integer | 충전한 포인트 금액 |
| balance | Integer | 충전 후 잔액 |
| createdAt | LocalDateTime | 충전일시 |

**제약조건**
- 존재하지 않는 회원 ID인 경우 404 Not Found
- amount가 null이거나 0 이하인 경우 400 Bad Request

---

## 2. 상품 API

### 2.1 상품 목록 조회
전체 상품 목록을 조회합니다.

**Endpoint**
```
GET /api/products
```

**Response**
```json
[
  {
    "productId": 1,
    "productName": "유기농 딸기",
    "description": "설향 품종의 달콤한 유기농 딸기입니다. (500g)",
    "productPrice": 18900
  }
]
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| description | String | 상품 설명 |
| productPrice | Integer | 상품 가격 |

---

### 2.2 상품 정보 조회
상품 ID로 상품 상세 정보를 조회합니다.

**Endpoint**
```
GET /api/products/{productId}
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| productId | Integer | O | 상품 ID |

**Response**
```json
{
  "productId": 1,
  "productName": "유기농 딸기",
  "description": "설향 품종의 달콤한 유기농 딸기입니다. (500g)",
  "productPrice": 18900
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| description | String | 상품 설명 |
| productPrice | Integer | 상품 가격 |

**제약조건**
- 존재하지 않는 상품 ID인 경우 404 Not Found

---

### 2.3 상품 재고 조회
상품 ID로 실시간 재고를 조회합니다.

**Endpoint**
```
GET /api/products/{productId}/stock
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| productId | Integer | O | 상품 ID |

**Response**
```json
{
  "productId": 1,
  "productName": "유기농 딸기",
  "stock": 50
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| stock | Integer | 재고 수량 |

**제약조건**
- 존재하지 않는 상품 ID인 경우 404 Not Found

---

### 2.4 인기 상품 통계 조회
최근 3일 기준 top5 인기 상품을 조회합니다.

**Endpoint**
```
GET /api/products/popular
```

**Response**
```json
[
  {
    "productId": 1,
    "productName": "유기농 딸기",
    "productPrice": 18900,
    "salesCount": 320
  }
]
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| productPrice | Integer | 상품 가격 |
| salesCount | Long | 판매 수량 |

**제약조건**
- 최대 5개까지 반환

---

## 3. 장바구니 API

### 3.1 장바구니 상품 목록 조회
사용자의 장바구니에 담긴 상품 목록을 조회합니다.

**Endpoint**
```
GET /api/cart?userId={userId}
```

**Query Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |

**Response**

```json
[
  {
    "cartItemId": 1,
    "userId": 1,
    "productId": 1,
    "productName": "유기농 딸기",
    "productPrice": 18900,
    "quantity": 2,
    "totalPrice": 37800,
    "createdAt": "2025-10-28T10:00:00"
  }
]
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| cartItemId | Integer | 장바구니 상품 ID |
| userId | Integer | 회원 ID |
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| productPrice | Integer | 상품 단가 |
| quantity | Integer | 수량 |
| totalPrice | Integer | 총 가격 (단가 × 수량) |
| createdAt | LocalDateTime | 장바구니 등록일시 |

---

### 3.2 장바구니 상품 등록
장바구니에 상품을 추가합니다.

**Endpoint**
```
POST /api/cart
```

**Request Body**
```json
{
  "userId": 1,
  "productId": 1,
  "quantity": 2
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |
| productId | Integer | O | 상품 ID |
| quantity | Integer | O | 수량 |

**Response**
```json
{
  "cartItemId": 1,
  "userId": 1,
  "productId": 1,
  "productName": "유기농 딸기",
  "productPrice": 18900,
  "quantity": 2,
  "totalPrice": 37800,
  "createdAt": "2025-10-29T15:30:00"
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| cartItemId | Integer | 장바구니 상품 ID |
| userId | Integer | 회원 ID |
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| productPrice | Integer | 상품 단가 |
| quantity | Integer | 수량 |
| totalPrice | Integer | 총 가격 |
| createdAt | LocalDateTime | 등록일시 |

**제약조건**
- 존재하지 않는 상품 ID인 경우 400 Bad Request
- 수량 변경 시에는 기존 상품을 삭제한 후 재등록

---

### 3.3 장바구니 상품 삭제
장바구니에서 상품을 삭제합니다.

**Endpoint**
```
DELETE /api/cart/{cartItemId}
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| cartItemId | Integer | O | 장바구니 상품 ID |

**Response**
```json
"장바구니 상품이 삭제되었습니다."
```

**제약조건**
- 존재하지 않는 장바구니 상품 ID인 경우 404 Not Found

---

## 4. 주문/결제 API

### 4.1 주문 생성
장바구니에 담긴 상품으로 주문을 생성합니다.

**Endpoint**
```
POST /api/orders
```

**Request Body**
```json
{
  "userId": 1,
  "deliveryUsername": "김철수",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "couponId": 1
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |
| deliveryUsername | String | O | 수령인 이름 |
| deliveryAddress | String | O | 배송지 주소 |
| couponId | Integer | X | 쿠폰 ID (선택) |

**Response**
```json
{
  "orderId": 1,
  "userId": 1,
  "orderStatus": "PENDING",
  "totalOrderAmount": 50700,
  "totalDiscountAmount": 20000,
  "usedPoint": 0,
  "finalPaymentAmount": 30700,
  "deliveryUsername": "김철수",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "createdAt": "2025-10-29T16:00:00",
  "updatedAt": "2025-10-29T16:00:00",
  "orderItems": [
    {
      "orderItemId": 1,
      "productId": 1,
      "productName": "유기농 딸기",
      "description": "설향 품종의 달콤한 유기농 딸기입니다. (500g)",
      "productPrice": 18900,
      "orderQuantity": 2,
      "totalPrice": 37800
    }
  ]
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | Integer | 주문 ID |
| userId | Integer | 회원 ID |
| orderStatus | String | 주문 상태 (PENDING: 결제 대기, PAID: 결제 완료) |
| totalOrderAmount | Integer | 총 주문 금액 |
| totalDiscountAmount | Integer | 총 할인 금액 (쿠폰) |
| usedPoint | Integer | 사용 포인트 |
| finalPaymentAmount | Integer | 최종 결제 금액 |
| deliveryUsername | String | 수령인 이름 |
| deliveryAddress | String | 배송지 주소 |
| createdAt | LocalDateTime | 주문 생성일시 |
| updatedAt | LocalDateTime | 주문 수정일시 |
| orderItems | Array | 주문 상품 목록 |

**OrderItem Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| orderItemId | Integer | 주문 상품 ID |
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| description | String | 상품 설명 |
| productPrice | Integer | 상품 단가 |
| orderQuantity | Integer | 주문 수량 |
| totalPrice | Integer | 총 가격 |

**제약조건**
- 장바구니가 비어있으면 주문 생성 실패 (400 Bad Request)
- 장바구니에 담긴 상품만 주문 가능
- 주문 생성 시 재고가 부족하면 실패
- 주문 생성 시 orderStatus는 "PENDING" (결제 대기) 상태로 설정됨

---

### 4.2 주문 내역 조회
주문 ID로 주문 내역을 조회합니다.

**Endpoint**
```
GET /api/orders/{orderId}
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderId | Integer | O | 주문 ID |

**Response**
```json
{
  "orderId": 1,
  "userId": 1,
  "orderStatus": "PAID",
  "totalOrderAmount": 50700,
  "totalDiscountAmount": 20000,
  "usedPoint": 0,
  "finalPaymentAmount": 30700,
  "deliveryUsername": "김철수",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "createdAt": "2025-10-29T16:00:00",
  "updatedAt": "2025-10-29T16:00:00",
  "orderItems": [
    {
      "orderItemId": 1,
      "productId": 1,
      "productName": "유기농 딸기",
      "description": "설향 품종의 달콤한 유기농 딸기입니다. (500g)",
      "productPrice": 18900,
      "orderQuantity": 2,
      "totalPrice": 37800
    }
  ]
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | Integer | 주문 ID |
| userId | Integer | 회원 ID |
| orderStatus | String | 주문 상태 (PENDING: 결제 대기, PAID: 결제 완료) |
| totalOrderAmount | Integer | 총 주문 금액 |
| totalDiscountAmount | Integer | 총 할인 금액 (쿠폰) |
| usedPoint | Integer | 사용 포인트 |
| finalPaymentAmount | Integer | 최종 결제 금액 |
| deliveryUsername | String | 수령인 이름 |
| deliveryAddress | String | 배송지 주소 |
| createdAt | LocalDateTime | 주문 생성일시 |
| updatedAt | LocalDateTime | 주문 수정일시 |
| orderItems | Array | 주문 상품 목록 |

**OrderItem Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| orderItemId | Integer | 주문 상품 ID |
| productId | Integer | 상품 ID |
| productName | String | 상품명 |
| description | String | 상품 설명 |
| productPrice | Integer | 상품 단가 |
| orderQuantity | Integer | 주문 수량 |
| totalPrice | Integer | 총 가격 |

**제약조건**
- 존재하지 않는 주문 ID인 경우 404 Not Found

---

### 4.3 결제
포인트로 주문 금액을 결제합니다.

**Endpoint**
```
POST /api/orders/payment
```

**Request Body**
```json
{
  "orderId": 1,
  "userId": 1
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderId | Integer | O | 주문 ID |
| userId | Integer | O | 회원 ID |

**Response (성공)**
```json
{
  "orderId": 1,
  "paymentAmount": 30700,
  "remainingPoint": 469300,
  "message": "결제가 완료되었습니다."
}
```

**Response (실패)**
```json
{
  "orderId": null,
  "paymentAmount": null,
  "remainingPoint": null,
  "message": "포인트가 부족합니다."
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | Integer | 주문 ID |
| paymentAmount | Integer | 결제 금액 |
| remainingPoint | Integer | 결제 후 남은 포인트 |
| message | String | 결제 결과 메시지 |

**제약조건**
- 존재하지 않는 주문 ID인 경우 결제 실패
- 회원이 보유한 포인트로 결제 진행
- 포인트가 부족하면 결제 실패 (400 Bad Request)
- 결제 완료 시 주문 상태(orderStatus)가 "PAID" (결제 완료)로 변경됨
- 결제 완료 후 주문한 상품은 장바구니에서 자동 제거
- 결제 완료 시 포인트 이력에 USE 타입으로 기록

---

## 5. 쿠폰 API

### 5.1 쿠폰 정보 목록 조회
발급 가능한 쿠폰 목록을 조회합니다.

**Endpoint**
```
GET /api/coupons
```

**Response**
```json
[
  {
    "couponId": 1,
    "couponName": "신규 오픈 선착순 할인 쿠폰",
    "discountAmount": 20000,
    "issuedQuantity": 50,
    "usedQuantity": 3,
    "remainingQuantity": 47,
    "couponStatus": "ACTIVE"
  }
]
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| couponId | Integer | 쿠폰 ID |
| couponName | String | 쿠폰 이름 |
| discountAmount | Integer | 할인 금액 |
| issuedQuantity | Integer | 발급 가능 수량 |
| usedQuantity | Integer | 발급된 수량 |
| remainingQuantity | Integer | 남은 수량 |
| couponStatus | String | 쿠폰 상태 (ACTIVE: 발급가능, DEPLETED: 소진) |

---

### 5.2 쿠폰 발급
선착순으로 쿠폰을 발급받습니다.

**Endpoint**
```
POST /api/coupons/issue
```

**Request Body**
```json
{
  "userId": 1,
  "couponId": 1
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | Integer | O | 회원 ID |
| couponId | Integer | O | 쿠폰 ID |

**Response (성공)**
```json
"쿠폰이 발급되었습니다."
```

**Response (실패)**
```json
"쿠폰 발급에 실패했습니다. 수량이 부족하거나 존재하지 않는 쿠폰입니다."
```

**제약조건**
- 발급 수량이 소진되면 실패 (400 Bad Request)
- 존재하지 않는 쿠폰 ID인 경우 실패
- 존재하지 않는 회원 ID인 경우 실패
- 발급 성공 시 쿠폰 사용 이력에 자동 추가
- 발급된 쿠폰은 주문 생성 시 사용 가능
- 선착순 쿠폰은 사전에 등록되어 있음

---

### 5.3 쿠폰 사용 이력 조회
특정 쿠폰의 사용 이력을 조회합니다.

**Endpoint**
```
GET /api/coupons/{couponId}/usage
```

**Path Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| couponId | Integer | O | 쿠폰 ID |

**Response**
```json
[
  {
    "couponUserId": 1,
    "userId": 1,
    "userName": "김철수",
    "issuedAt": "2025-10-24T10:00:00",
    "usedAt": "2025-10-26T14:30:00",
    "used": true
  },
  {
    "couponUserId": 2,
    "userId": 2,
    "userName": "이영희",
    "issuedAt": "2025-10-25T11:00:00",
    "usedAt": null,
    "used": false
  }
]
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| couponUserId | Integer | 쿠폰 발급 이력 ID |
| userId | Integer | 회원 ID |
| userName | String | 회원 이름 |
| issuedAt | LocalDateTime | 발급일시 |
| usedAt | LocalDateTime | 사용일시 (미사용 시 null) |
| used | Boolean | 사용 여부 |

**제약조건**
- 발급 이력이 없는 경우 빈 배열 반환

---

## 공통 사항

### HTTP Status Code
| 코드               | 설명                          |
|------------------|-----------------------------|
| 200 OK           | 요청 성공                       |
| 400 Bad Request  | 잘못된 요청 (필수값 누락, 유효하지 않은 값 등) |
| 404 Not Found    | 리소스를 찾을 수 없음                |
| 500 Server Error | 서버 에러                       |

### 비즈니스 규칙
1. **회원**
   - 회원과 회원 포인트는 미리 등록되어 있음
   - 포인트 타입: EARN(충전), USE(사용)

2. **상품**
   - 상품은 옵션이 없고 단일 상품
   - 상품 등록은 이미 되어있다고 가정
   - 인기 상품: 최근 3일 기준 top5

3. **장바구니**
   - 수량 변동 시 상품 삭제 후 재등록
   - 결제 완료 후 자동으로 비워짐

4. **주문/결제**
   - 장바구니에 담긴 상품만 주문 가능
   - 장바구니가 비어있으면 주문 불가
   - 주문 생성 시 재고 부족하면 실패
   - 주문 상태: PENDING (결제 대기), PAID (결제 완료)
   - 결제는 포인트 차감으로 진행
   - 포인트 부족 시 결제 실패
   - 결제 완료 후 주문 상품은 장바구니에서 제거

5. **쿠폰**
   - 선착순 쿠폰은 미리 등록되어 있음
   - 발급 수량 소진 시 더 이상 발급 불가
   - 주문 생성 시 쿠폰 ID를 전달하여 할인 적용