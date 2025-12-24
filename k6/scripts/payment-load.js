import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const paymentSuccessRate = new Rate('payment_success_rate');
const pointShortageErrors = new Counter('point_shortage_errors');

// 결제 처리 Load Test - 포인트 차감 정합성 및 장바구니 연동 검증 (DAU 500 기준)
export const options = {
    stages: [
        { duration: '2m', target: 100 },   // 2분 동안 100명까지 증가
        { duration: '5m', target: 100 },   // 5분 동안 100명 유지
        { duration: '2m', target: 0 },     // 2분 동안 0명으로 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<1500', 'p(99)<3000'],  // 결제 처리 시간 (정합성 우선)
        http_req_failed: ['rate<0.01'],                   // 에러율 1% 미만
        errors: ['rate<0.01'],
        payment_success_rate: ['rate>0.95'],             // 결제 성공률 95% 이상
    },
    tags: {
        test_type: 'load',
        endpoint: '/api/orders/payment',
        method: 'POST',
        target: 'payment',
    },
};

// 테스트 데이터 설정
const BASE_URL = 'http://host.docker.internal:8080';
const TOTAL_USERS = 1000;  // 테스트용 사용자 수 (1-1000)

// 배송지 목록
const DELIVERY_ADDRESSES = [
    '서울시 강남구 테헤란로 123',
    '서울시 서초구 서초대로 456',
    '서울시 송파구 올림픽로 789',
];

export default function () {
    // 랜덤 사용자 선택 (1-1000)
    const userId = Math.floor(Math.random() * TOTAL_USERS) + 1;
    const deliveryAddress = DELIVERY_ADDRESSES[Math.floor(Math.random() * DELIVERY_ADDRESSES.length)];

    // Step 1: 주문 생성 (결제를 위한 사전 단계)
    const orderPayload = JSON.stringify({
        userId: userId,
        deliveryUsername: `테스트유저${userId}`,
        deliveryAddress: deliveryAddress,
        couponId: null,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const orderRes = http.post(
        `${BASE_URL}/api/orders`,
        orderPayload,
        params
    );

    // 주문 생성 실패 시 결제 건너뛰기
    if (orderRes.status !== 200) {
        if (orderRes.status === 400 || orderRes.status === 409) {
            console.log(`[INFO] Order creation failed (empty cart or stock shortage) - User: ${userId}`);
        } else {
            console.error(`[ERROR] Order creation failed - Status: ${orderRes.status}, User: ${userId}`);
        }
        errorRate.add(1);
        paymentSuccessRate.add(0);
        sleep(1);
        return;
    }

    // 주문 ID 추출
    let orderId;
    try {
        const orderBody = JSON.parse(orderRes.body);
        orderId = orderBody.orderId;
        if (!orderId) {
            console.error(`[ERROR] No orderId in response - User: ${userId}`);
            errorRate.add(1);
            paymentSuccessRate.add(0);
            sleep(1);
            return;
        }
    } catch (e) {
        console.error(`[ERROR] Failed to parse order response - User: ${userId}`);
        errorRate.add(1);
        paymentSuccessRate.add(0);
        sleep(1);
        return;
    }

    // 약간의 대기 시간 (사용자가 주문 확인하는 시간)
    sleep(0.5);

    // Step 2: 결제 처리 (핵심 테스트 대상)
    const paymentPayload = JSON.stringify({
        orderId: orderId,
        userId: userId,
    });

    const paymentRes = http.post(
        `${BASE_URL}/api/orders/payment`,
        paymentPayload,
        params
    );

    // 결제 응답 검증
    const checkResult = check(paymentRes, {
        'payment status is 200': (r) => r.status === 200,
        'payment response time < 1500ms': (r) => r.timings.duration < 1500,
        'has payment data': (r) => {
            if (r.status === 200) {
                try {
                    const body = JSON.parse(r.body);
                    return body.orderId !== undefined;
                } catch {
                    return false;
                }
            }
            return false;
        },
    });

    // 커스텀 메트릭 기록
    if (paymentRes.status === 200) {
        paymentSuccessRate.add(1);
        errorRate.add(0);

        // 결제 성공 로그 (샘플링)
        if (Math.random() < 0.01) {  // 1%만 로그
            console.log(`[SUCCESS] Payment completed - Order: ${orderId}, User: ${userId}`);
        }
    } else {
        paymentSuccessRate.add(0);
        errorRate.add(1);

        // 포인트 부족 에러 (400)
        if (paymentRes.status === 400) {
            pointShortageErrors.add(1);
            console.log(`[INFO] Point shortage - Order: ${orderId}, User: ${userId}`);
        }

        // 서버 에러
        if (paymentRes.status >= 500) {
            console.error(`[ERROR] Payment server error - Status: ${paymentRes.status}, Order: ${orderId}, User: ${userId}`);
        }
    }

    sleep(1);  // 사용자 행동 시뮬레이션
}

// 테스트 시작 시 실행
export function setup() {
    console.log('='.repeat(60));
    console.log('결제 처리 Load Test 시작');
    console.log('목표: 포인트 차감 정합성 및 장바구니 연동 검증');
    console.log('='.repeat(60));
    console.log(`Base URL: ${BASE_URL}`);
    console.log(`Test Users: 1-${TOTAL_USERS}`);
    console.log('');
    console.log('⚠️  사전 준비 필요:');
    console.log('1. 테스트용 사용자 (1-1000)의 장바구니에 상품 추가');
    console.log('2. 테스트용 사용자에게 충분한 포인트 충전 (예: 100만원)');
    console.log('3. 상품 재고 충분히 설정');
    console.log('');
    console.log('💡 포인트 충전 예시:');
    console.log('UPDATE users SET point = 1000000 WHERE id <= 1000;');
    console.log('');
    console.log('📝 테스트 플로우:');
    console.log('각 iteration마다 [주문 생성 → 결제] 전체 플로우 실행');
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
    console.log('결제 처리 Load Test 종료');
    console.log('');
    console.log('📊 검증 항목:');
    console.log('');
    console.log('1. 포인트 정합성 확인:');
    console.log('   SELECT id, point FROM users WHERE id <= 10 ORDER BY id;');
    console.log('   SELECT SUM(total_amount) FROM orders WHERE status = \'PAID\';');
    console.log('   (초기 포인트 합계 - 결제 총액 = 최종 포인트 합계)');
    console.log('');
    console.log('2. 장바구니 삭제 확인:');
    console.log('   SELECT COUNT(*) FROM cart_items;');
    console.log('   (결제 완료된 주문의 상품은 장바구니에서 삭제되어야 함)');
    console.log('');
    console.log('3. 주문 상태 확인:');
    console.log('   SELECT status, COUNT(*) FROM orders GROUP BY status;');
    console.log('');
    console.log('4. Grafana 대시보드 확인 (http://localhost:3001)');
    console.log('='.repeat(60));
}
