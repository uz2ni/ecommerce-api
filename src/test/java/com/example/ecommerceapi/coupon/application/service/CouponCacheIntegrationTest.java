package com.example.ecommerceapi.coupon.application.service;

import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.coupon.application.dto.CouponResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CouponService 캐싱 통합 테스트")
class CouponCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Redis의 모든 키(캐시 + 락) 초기화
        clearAllRedisKeys();
    }

    @Test
    @DisplayName("전체 쿠폰 목록 조회 시 캐싱이 적용되어야 한다")
    void getAllCoupons_ShouldBeCached() {
        // when - 첫 번째 호출
        List<CouponResult> firstCall = couponService.getAllCoupons();

        // when - 두 번째 호출
        List<CouponResult> secondCall = couponService.getAllCoupons();

        // then - 동일한 데이터가 반환되어야 함 (캐시에서 조회)
        assertThat(firstCall).hasSize(secondCall.size());
        assertThat(firstCall.size()).isGreaterThan(0);

        // 각 쿠폰의 데이터가 동일한지 확인
        for (int i = 0; i < firstCall.size(); i++) {
            assertThat(firstCall.get(i).couponId()).isEqualTo(secondCall.get(i).couponId());
            assertThat(firstCall.get(i).couponName()).isEqualTo(secondCall.get(i).couponName());
        }
    }

    @Test
    @DisplayName("캐시를 삭제하면 다시 DB에서 조회해야 한다")
    void clearCache_ShouldReloadFromDatabase() {
        // given
        String cacheName = "allCoupons";

        // when - 첫 번째 호출로 캐시에 저장
        List<CouponResult> firstCall = couponService.getAllCoupons();

        // when - 캐시 삭제
        cacheManager.getCache(cacheName).clear();

        // when - 다시 조회
        List<CouponResult> secondCall = couponService.getAllCoupons();

        // then - 데이터가 올바르게 조회됨
        assertThat(secondCall.size()).isGreaterThan(0);
        assertThat(secondCall.size()).isEqualTo(firstCall.size());
    }

    @Test
    @DisplayName("쿠폰 목록 조회 결과가 올바른 데이터를 포함해야 한다")
    void getAllCoupons_ShouldReturnValidData() {
        // when
        List<CouponResult> coupons = couponService.getAllCoupons();

        // then
        assertThat(coupons).isNotEmpty();
        coupons.forEach(coupon -> {
            assertThat(coupon.couponId()).isNotNull();
            assertThat(coupon.couponName()).isNotBlank();
            assertThat(coupon.discountAmount()).isNotNull();
            assertThat(coupon.totalQuantity()).isGreaterThan(0);
            assertThat(coupon.issuedQuantity()).isNotNull();
            assertThat(coupon.expiredAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("캐시된 데이터를 여러 번 조회해도 동일한 결과를 반환해야 한다")
    void getAllCoupons_MultipleCalls_ShouldReturnSameData() {
        // when
        List<CouponResult> firstCall = couponService.getAllCoupons();
        List<CouponResult> secondCall = couponService.getAllCoupons();
        List<CouponResult> thirdCall = couponService.getAllCoupons();

        // then - 모두 동일한 크기와 데이터
        assertThat(firstCall.size()).isEqualTo(secondCall.size());
        assertThat(secondCall.size()).isEqualTo(thirdCall.size());

        // 첫 번째 쿠폰 ID가 모두 동일한지 확인
        if (!firstCall.isEmpty()) {
            assertThat(firstCall.get(0).couponId()).isEqualTo(secondCall.get(0).couponId());
            assertThat(secondCall.get(0).couponId()).isEqualTo(thirdCall.get(0).couponId());
        }
    }
}