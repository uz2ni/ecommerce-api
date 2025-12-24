import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const orderSuccessRate = new Rate('order_success_rate');
const stockErrors = new Counter('stock_shortage_errors');

// 주문 생성 Load Test - 재고 차감 동시성 및 트랜잭션 정합성 검증 (DAU 500 기준)
export const options = {
    stages: [
        { duration: '2m', target: 120 },   // 2분 동안 120명까지 증가
        { duration: '5m', target: 120 },   // 5분 동안 120명 유지
        { duration: '2m', target: 0 },     // 2분 동안 0명으로 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000', 'p(99)<2000'],  // 비즈니스 로직 처리 시간
        http_req_failed: ['rate<0.05'],                   // 에러율 5% 미만
        errors: ['rate<0.05'],
        order_success_rate: ['rate>0.90'],               // 주문 성공률 90% 이상 (재고 부족 제외)
    },
    tags: {
        test_type: 'load',
        endpoint: '/api/orders',
        method: 'POST',
        target: 'order_create',
    },
};

// 테스트 데이터 설정
const BASE_URL = 'http://host.docker.internal:8080';
const TOTAL_USERS = 1000;  // 테스트용 사용자 수 (1-1000)

// 배송지 목록 (랜덤 선택)
const DELIVERY_ADDRESSES = [
    '서울시 강남구 테헤란로 123',
    '서울시 서초구 서초대로 456',
    '서울시 송파구 올림픽로 789',
    '경기도 성남시 분당구 판교역로 100',
    '경기도 수원시 영통구 광교중앙로 200',
];

export default function () {
    // 랜덤 사용자 선택 (1-1000)
    const userId = Math.floor(Math.random() * TOTAL_USERS) + 1;
    const deliveryAddress = DELIVERY_ADDRESSES[Math.floor(Math.random() * DELIVERY_ADDRESSES.length)];

    const payload = JSON.stringify({
        userId: userId,
        deliveryUsername: `테스트유저${userId}`,
        deliveryAddress: deliveryAddress,
        couponId: null,  // 쿠폰 미사용 (선택 사항)
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(
        `${BASE_URL}/api/orders`,
        payload,
        params
    );

    // 응답 검증
    const checkResult = check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
        'has order data': (r) => {
            if (r.status === 200) {
                try {
                    const body = JSON.parse(r.body);
                    return body.orderId !== undefined && body.orderId > 0;
                } catch {
                    return false;
                }
            }
            return false;
        },
    });

    // 커스텀 메트릭 기록
    if (res.status === 200) {
        orderSuccessRate.add(1);
        errorRate.add(0);

        // 주문 성공 로그 (샘플링)
        if (Math.random() < 0.01) {  // 1%만 로그
            console.log(`[SUCCESS] Order created - User: ${userId}`);
        }
    } else {
        orderSuccessRate.add(0);
        errorRate.add(1);

        // 재고 부족 에러 (400 또는 409)
        if (res.status === 400 || res.status === 409) {
            stockErrors.add(1);
            console.log(`[INFO] Stock shortage or empty cart - User: ${userId}, Status: ${res.status}`);
        }

        // 서버 에러
        if (res.status >= 500) {
            console.error(`[ERROR] Server error - Status: ${res.status}, User: ${userId}, Body: ${res.body}`);
        }
    }

    sleep(1);  // 사용자 행동 시뮬레이션
}

// 테스트 시작 시 실행
export function setup() {
    console.log('='.repeat(60));
    console.log('주문 생성 Load Test 시작');
    console.log('목표: 재고 차감 동시성 제어 및 트랜잭션 정합성 검증');
    console.log('='.repeat(60));
    console.log(`Base URL: ${BASE_URL}`);
    console.log(`Test Users: 1-${TOTAL_USERS}`);
    console.log('');
    console.log('⚠️  사전 준비 필요:');
    console.log('1. 테스트용 사용자 (1-1000)의 장바구니에 상품 추가');
    console.log('2. 상품 재고 충분히 설정 (예: 100,000개)');
    console.log('');
    console.log('💡 장바구니 데이터 준비 예시:');
    console.log('INSERT INTO cart_items (user_id, product_id, quantity)');
    console.log('SELECT n, FLOOR(1 + RAND() * 100), FLOOR(1 + RAND() * 3)');
    console.log('FROM (SELECT @row := @row + 1 AS n FROM ...) numbers');
    console.log('WHERE n <= 1000;');
    console.log('='.repeat(60));

    // 간단한 health check
    const healthRes = http.get(`${BASE_URL}/actuator/health`);
    if (healthRes.status !== 200) {
        throw new Error('API 서버가 응답하지 않습니다. 서버를 먼저 시작하세요.');
    }
}

// 테스트 종료 시 실행
export function teardown(data) {
    console.log('='.repeat(60));
    console.log('주문 생성 Load Test 종료');
    console.log('');
    console.log('📊 검증 항목:');
    console.log('1. 재고 정합성 확인:');
    console.log('   SELECT product_id, stock FROM products WHERE product_id <= 100;');
    console.log('   (초기 재고 - 주문된 총 수량 = 최종 재고)');
    console.log('');
    console.log('2. 주문 데이터 확인:');
    console.log('   SELECT COUNT(*) FROM orders;');
    console.log('   SELECT COUNT(*) FROM order_items;');
    console.log('');
    console.log('3. Grafana 대시보드 확인 (http://localhost:3001)');
    console.log('='.repeat(60));
}
