package com.example.ecommerceapi.coupon.infrastructure;

import com.example.ecommerceapi.coupon.dto.CouponResponse;
import com.example.ecommerceapi.coupon.dto.CouponUsageResponse;
import com.example.ecommerceapi.user.infrastructure.MockUserData;

import java.time.LocalDateTime;
import java.util.*;

public class MockCouponData {

    private static final Map<Integer, CouponResponse> COUPONS = new HashMap<>();
    private static final Map<Integer, List<CouponUsageResponse>> COUPON_USAGE_HISTORY = new HashMap<>();
    private static int couponUserIdCounter = 1; // 초기 이력 5개 이후부터

    static {
        COUPONS.put(1, CouponResponse.builder()
                .couponId(1)
                .couponName("신규 오픈 선착순 할인 쿠폰")
                .discountAmount(20000)
                .issuedQuantity(50)
                .usedQuantity(1)
                .remainingQuantity(47)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .couponStatus("ACTIVE")
                .build());

        COUPONS.put(2, CouponResponse.builder()
                .couponId(2)
                .couponName("3명 한정 선착순 할인 쿠폰")
                .discountAmount(15000)
                .issuedQuantity(3)
                .usedQuantity(3)
                .remainingQuantity(3)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .couponStatus("DEPLETED")
                .build());

        COUPONS.put(3, CouponResponse.builder()
                .couponId(3)
                .couponName("누구나 선착순 할인 쿠폰")
                .discountAmount(10000)
                .issuedQuantity(30)
                .usedQuantity(1)
                .remainingQuantity(2)
                .expiredAt(LocalDateTime.now().plusDays(60))
                .couponStatus("ACTIVE")
                .build());

        // 쿠폰 1 사용 이력
        List<CouponUsageResponse> coupon1History = new ArrayList<>();
        coupon1History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(1)
                .userName("김철수")
                .issuedAt(LocalDateTime.now().minusDays(5))
                .usedAt(LocalDateTime.now().minusDays(3))
                .used(true)
                .build());
        coupon1History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(2)
                .userName("이영희")
                .issuedAt(LocalDateTime.now().minusDays(4))
                .usedAt(LocalDateTime.now().minusDays(1))
                .used(true)
                .build());
        coupon1History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(3)
                .userName("박민수")
                .issuedAt(LocalDateTime.now().minusDays(2))
                .usedAt(null)
                .used(false)
                .build());
        COUPON_USAGE_HISTORY.put(1, coupon1History);

        // 쿠폰 2 사용 이력
        List<CouponUsageResponse> coupon2History = new ArrayList<>();
        coupon2History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(1)
                .userName("김철수")
                .issuedAt(LocalDateTime.now().minusDays(5))
                .usedAt(LocalDateTime.now().minusDays(3))
                .used(false)
                .build());
        coupon2History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(2)
                .userName("이영희")
                .issuedAt(LocalDateTime.now().minusDays(4))
                .usedAt(LocalDateTime.now().minusDays(1))
                .used(false)
                .build());
        coupon2History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(3)
                .userName("박민수")
                .issuedAt(LocalDateTime.now().minusDays(2))
                .usedAt(null)
                .used(true)
                .build());
        COUPON_USAGE_HISTORY.put(2, coupon2History);

        // 쿠폰 3 사용 이력
        List<CouponUsageResponse> coupon3History = new ArrayList<>();
        coupon3History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(4)
                .userName("정수진")
                .issuedAt(LocalDateTime.now().minusDays(10))
                .usedAt(LocalDateTime.now().minusDays(7))
                .used(true)
                .build());
        coupon3History.add(CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(5)
                .userName("최동욱")
                .issuedAt(LocalDateTime.now().minusDays(3))
                .usedAt(null)
                .used(false)
                .build());
        COUPON_USAGE_HISTORY.put(3, coupon3History);
    }

    public static List<CouponResponse> getAllCoupons() {
        return new ArrayList<>(COUPONS.values());
    }

    public static CouponResponse getCoupon(Integer couponId) {
        return COUPONS.get(couponId);
    }

    public static List<CouponUsageResponse> getCouponUsageHistory(Integer couponId) {
        return COUPON_USAGE_HISTORY.getOrDefault(couponId, new ArrayList<>());
    }

    public static boolean issueCoupon(Integer couponId, Integer userId) {
        CouponResponse coupon = COUPONS.get(couponId);
        if (coupon == null || coupon.getRemainingQuantity() <= 0) {
            return false;
        }

        // 쿠폰 만료일 검증
        if (coupon.getExpiredAt() != null && LocalDateTime.now().isAfter(coupon.getExpiredAt())) {
            return false; // 쿠폰이 만료됨
        }

        // 회원 정보 조회
        var user = MockUserData.getUser(userId);
        if (user == null) {
            return false;
        }

        // 쿠폰 발급 이력 추가
        List<CouponUsageResponse> history = COUPON_USAGE_HISTORY.computeIfAbsent(couponId, k -> new ArrayList<>());

        CouponUsageResponse usage = CouponUsageResponse.builder()
                .couponUserId(couponUserIdCounter++)
                .userId(userId)
                .userName(user.getUsername())
                .issuedAt(LocalDateTime.now())
                .usedAt(null)
                .used(false)
                .build();

        history.add(usage);

        // 쿠폰 수량 업데이트
        coupon.setUsedQuantity(coupon.getUsedQuantity() + 1);
        coupon.setRemainingQuantity(coupon.getRemainingQuantity() - 1);

        if (coupon.getRemainingQuantity() == 0) {
            coupon.setCouponStatus("DEPLETED");
        }

        return true;
    }
}