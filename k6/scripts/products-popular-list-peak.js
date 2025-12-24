import http from 'k6/http';
import { check, sleep } from 'k6';

// 인기 상품 조회 Peak Test - 이벤트/프로모션 시 트래픽 급증 대응 (DAU 500 기준)
export const options = {
    stages: [
        { duration: '2m', target: 60 },    // 평상시 60명
        { duration: '30s', target: 300 },  // 이벤트 시작! 급증 300명
        { duration: '2m', target: 300 },   // 2분간 유지
        { duration: '1m', target: 60 },    // 평상시로 복구
        { duration: '2m', target: 0 },     // 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],  // Peak 시 여유있게
        http_req_failed: ['rate<0.05'],                  // 에러율 5% 미만
    },
    tags: {
        test_type: 'peak',
        endpoint: '/api/products/popular',
        method: 'GET',
        target: 'popular_list',
    },
};

export default function () {
    // 인기 상품 목록 조회 (최근 3일, 판매량 기준 top5)
    const type = 'SALES';
    const days = 3;
    const limit = 5;
    const res = http.get(`http://host.docker.internal:8080/api/products/popular?type=${type}&days=${days}&limit=${limit}`);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 1s': (r) => r.timings.duration < 1000,
        'has popular products': (r) => {
            try {
                const data = JSON.parse(r.body);
                return Array.isArray(data) && data.length > 0;
            } catch {
                return false;
            }
        },
    });

    sleep(1);
}
