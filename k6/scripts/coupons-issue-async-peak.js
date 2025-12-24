import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const messageAcceptRate = new Rate('message_accept_rate');

// 쿠폰 발급 비동기 처리 Peak Test - 선착순 이벤트 시 트래픽 급증 대응 (DAU 500 기준)
export const options = {
    stages: [
        { duration: '2m', target: 100 },   // 평상시 100명
        { duration: '30s', target: 600 },  // 선착순 시작! 급증 600명
        { duration: '2m', target: 600 },   // 2분간 유지
        { duration: '1m', target: 100 },   // 평상시로 복구
        { duration: '2m', target: 0 },     // 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],  // 비동기 접수만, 빠른 응답
        http_req_failed: ['rate<0.05'],                  // 에러율 5% 미만
        errors: ['rate<0.05'],
        message_accept_rate: ['rate>0.95'],              // 메시지 발행 성공률 95% 이상
    },
    tags: {
        test_type: 'peak',
        endpoint: '/api/coupons/issue/request',
        method: 'POST',
        target: 'coupon_async',
    },
};

// 테스트 데이터 설정
const BASE_URL = 'http://host.docker.internal:8080';
const TOTAL_USERS = 1000;  // 테스트용 사용자 수 (1-1000)
const COUPON_ID = 1;       // 테스트용 쿠폰 ID

export default function () {
    // 랜덤 사용자 선택 (1-1000)
    const userId = Math.floor(Math.random() * TOTAL_USERS) + 1;

    const payload = JSON.stringify({
        couponId: COUPON_ID,
        userId: userId,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(
        `${BASE_URL}/api/coupons/issue/request`,
        payload,
        params
    );

    // 응답 검증
    const checkResult = check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
        'message accepted': (r) => {
            if (r.status === 200) {
                try {
                    const body = JSON.parse(r.body);
                    // 비동기 접수 성공 여부 확인
                    return body.success === true || body.message === 'accepted';
                } catch {
                    return false;
                }
            }
            return false;
        },
    });

    // 커스텀 메트릭 기록
    errorRate.add(res.status !== 200);
    messageAcceptRate.add(res.status === 200);

    // 선착순 마감 시 409 또는 400 에러는 정상 (쿠폰 소진)
    if (res.status === 409 || res.status === 400) {
        console.log(`[INFO] Coupon sold out or already issued - User ${userId}`);
    }

    // 500 에러는 문제 상황
    if (res.status >= 500) {
        console.error(`[ERROR] Server error - Status: ${res.status}, User: ${userId}`);
    }

    sleep(1);  // 사용자 행동 시뮬레이션
}

// 테스트 시작 시 실행
export function setup() {
    console.log('='.repeat(60));
    console.log('쿠폰 발급 비동기 처리 Peak Test 시작');
    console.log('목표: 이벤트 시작 시 Kafka 메시지 큐 안정성 검증');
    console.log('='.repeat(60));
    console.log(`Base URL: ${BASE_URL}`);
    console.log(`Coupon ID: ${COUPON_ID}`);
    console.log(`Test Users: 1-${TOTAL_USERS}`);
    console.log('='.repeat(60));
}

// 테스트 종료 시 실행
export function teardown(data) {
    console.log('='.repeat(60));
    console.log('쿠폰 발급 비동기 처리 Peak Test 종료');
    console.log('다음 단계:');
    console.log('1. Kafka UI에서 Consumer Lag 확인 (http://localhost:8081)');
    console.log('2. 쿠폰 발급 완료율 확인 (DB 쿼리)');
    console.log('3. Grafana에서 메시지 처리 지연 시간 확인');
    console.log('='.repeat(60));
}
