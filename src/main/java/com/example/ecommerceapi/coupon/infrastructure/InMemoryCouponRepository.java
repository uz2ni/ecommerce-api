package com.example.ecommerceapi.coupon.infrastructure;

import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import jakarta.annotation.PostConstruct;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryCouponRepository implements CouponRepository {

    private final Map<Integer, Coupon> store = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostConstruct
    public void init() {
        // 쿠폰 1: 신규 오픈 선착순 할인 쿠폰
        Coupon coupon1 = Coupon.builder()
                .couponId(idGenerator.getAndIncrement())
                .couponName("신규 오픈 선착순 할인 쿠폰")
                .discountAmount(20000)
                .totalQuantity(50)
                .issuedQuantity(3)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();
        store.put(coupon1.getCouponId(), coupon1);

        // 쿠폰 2: 3명 한정 선착순 할인 쿠폰 (소진됨)
        Coupon coupon2 = Coupon.builder()
                .couponId(idGenerator.getAndIncrement())
                .couponName("3명 한정 선착순 할인 쿠폰")
                .discountAmount(15000)
                .totalQuantity(3)
                .issuedQuantity(3)
                .expiredAt(LocalDateTime.now().plusDays(15))
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();
        store.put(coupon2.getCouponId(), coupon2);

        // 쿠폰 3: 누구나 선착순 할인 쿠폰
        Coupon coupon3 = Coupon.builder()
                .couponId(idGenerator.getAndIncrement())
                .couponName("누구나 선착순 할인 쿠폰")
                .discountAmount(10000)
                .totalQuantity(30)
                .issuedQuantity(2)
                .expiredAt(LocalDateTime.now().plusDays(60))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();
        store.put(coupon3.getCouponId(), coupon3);
    }

    /**
     * 쿠폰 저장 (업데이트)
     */
    public Coupon save(Coupon coupon) {
        if (coupon.getCouponId() == null) {
            coupon.setCouponId(idGenerator.getAndIncrement());
        }
        store.put(coupon.getCouponId(), coupon);
        return coupon;
    }

    /**
     * ID로 쿠폰 조회
     */
    public Optional<Coupon> findById(Integer couponId) {
        return Optional.ofNullable(store.get(couponId));
    }

    /**
     * 모든 쿠폰 조회
     */
    public List<Coupon> findAll() {
        return List.copyOf(store.values());
    }

    /**
     * 모든 데이터 삭제 (테스트용)
     */
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    /**
     * 전체 쿠폰 개수 조회 (테스트용)
     */
    public int count() {
        return store.size();
    }
}