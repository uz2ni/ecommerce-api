package com.example.ecommerceapi.coupon.infrastructure;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import jakarta.annotation.PostConstruct;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryCouponUserRepository implements CouponUserRepository {

    private final Map<Integer, CouponUser> store = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostConstruct
    public void init() {
        // 쿠폰 1 발급 이력
        CouponUser cu1 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(1)
                .userId(1)
                .used(true)
                .issuedAt(LocalDateTime.now().minusDays(5))
                .usedAt(LocalDateTime.now().minusDays(3))
                .build();
        store.put(cu1.getCouponUserId(), cu1);

        CouponUser cu2 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(1)
                .userId(2)
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(4))
                .usedAt(null)
                .build();
        store.put(cu2.getCouponUserId(), cu2);

        CouponUser cu3 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(1)
                .userId(3)
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(2))
                .usedAt(null)
                .build();
        store.put(cu3.getCouponUserId(), cu3);

        // 쿠폰 2 발급 이력
        CouponUser cu4 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(2)
                .userId(1)
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(10))
                .usedAt(null)
                .build();
        store.put(cu4.getCouponUserId(), cu4);

        CouponUser cu5 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(2)
                .userId(2)
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(9))
                .usedAt(null)
                .build();
        store.put(cu5.getCouponUserId(), cu5);

        CouponUser cu6 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(2)
                .userId(4)
                .used(true)
                .issuedAt(LocalDateTime.now().minusDays(8))
                .usedAt(LocalDateTime.now().minusDays(7))
                .build();
        store.put(cu6.getCouponUserId(), cu6);

        // 쿠폰 3 발급 이력
        CouponUser cu7 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(3)
                .userId(4)
                .used(true)
                .issuedAt(LocalDateTime.now().minusDays(3))
                .usedAt(LocalDateTime.now().minusDays(1))
                .build();
        store.put(cu7.getCouponUserId(), cu7);

        CouponUser cu8 = CouponUser.builder()
                .couponUserId(idGenerator.getAndIncrement())
                .couponId(3)
                .userId(5)
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(2))
                .usedAt(null)
                .build();
        store.put(cu8.getCouponUserId(), cu8);
    }

    /**
     * 쿠폰 사용자 저장
     */
    public CouponUser save(CouponUser couponUser) {
        if (couponUser.getCouponUserId() == null) {
            couponUser.setCouponUserId(idGenerator.getAndIncrement());
        }
        store.put(couponUser.getCouponUserId(), couponUser);
        return couponUser;
    }

    /**
     * ID로 쿠폰 사용자 조회
     */
    public Optional<CouponUser> findById(Integer couponUserId) {
        return Optional.ofNullable(store.get(couponUserId));
    }

    /**
     * 특정 쿠폰의 모든 발급 이력 조회
     */
    public List<CouponUser> findByCouponId(Integer couponId) {
        return store.values().stream()
                .filter(cu -> cu.getCouponId().equals(couponId))
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 특정 쿠폰 발급 이력 조회
     */
    public Optional<CouponUser> findByCouponIdAndUserId(Integer couponId, Integer userId) {
        return store.values().stream()
                .filter(cu -> cu.getCouponId().equals(couponId) && cu.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 특정 사용자의 모든 쿠폰 발급 이력 조회
     */
    public List<CouponUser> findByUserId(Integer userId) {
        return store.values().stream()
                .filter(cu -> cu.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 모든 데이터 삭제 (테스트용)
     */
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    /**
     * 전체 쿠폰 사용자 개수 조회 (테스트용)
     */
    public int count() {
        return store.size();
    }
}