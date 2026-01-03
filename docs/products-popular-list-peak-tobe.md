     scenarios: (100.00%) 1 scenario, 300 max VUs, 8m0s max duration (incl. graceful stop):
              * default: Up to 300 looping VUs for 7m30s over 5 stages (gracefulRampDown: 30s, gracefulStop: 30s)

WARN[0251] Request Failed                                error="Get \"http://host.docker.internal:8080/api/products/popular?type=SALES&days=3&limit=5\": dial: i/o timeout"
WARN[0251] Request Failed                                error="Get \"http://host.docker.internal:8080/api/products/popular?type=SALES&days=3&limit=5\": dial: i/o timeout"
WARN[0300] Request Failed                                error="Get \"http://host.docker.internal:8080/api/products/popular?type=SALES&days=3&limit=5\": dial: i/o timeout"


█ THRESHOLDS

    http_req_duration
    ✓ 'p(95)<500' p(95)=27.26ms
    ✓ 'p(99)<1000' p(99)=67.91ms

    http_req_failed
    ✓ 'rate<0.05' rate=0.00%


█ TOTAL RESULTS

    checks_total.......: 176238 391.588239/s
    checks_succeeded...: 99.99% 176232 out of 176238
    checks_failed......: 0.00%  6 out of 176238

    ✗ status is 200
      ↳  99% — ✓ 58743 / ✗ 3
    ✓ response time < 1s
    ✗ has popular products
      ↳  99% — ✓ 58743 / ✗ 3

    HTTP
    http_req_duration..............: avg=12.13ms min=0s     med=8.52ms max=483.12ms p(90)=20.14ms p(95)=27.26ms
      { expected_response:true }...: avg=12.13ms min=2.26ms med=8.52ms max=483.12ms p(90)=20.14ms p(95)=27.26ms
    http_req_failed................: 0.00%  3 out of 58746
    http_reqs......................: 58746  130.529413/s

    EXECUTION
    iteration_duration.............: avg=1.01s   min=1s     med=1s     max=31s      p(90)=1.02s   p(95)=1.02s  
    iterations.....................: 58746  130.529413/s
    vus............................: 1      min=1          max=300
    vus_max........................: 300    min=300        max=300

    NETWORK
    data_received..................: 38 MB  85 kB/s
    data_sent......................: 7.5 MB 17 kB/s




running (7m30.1s), 000/300 VUs, 58746 complete and 0 interrupted iterations
default ✓ [======================================] 000/300 VUs  7m30s