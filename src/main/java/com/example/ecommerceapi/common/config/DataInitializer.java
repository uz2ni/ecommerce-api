package com.example.ecommerceapi.common.config;

import com.example.ecommerceapi.cart.application.service.CartService;
import com.example.ecommerceapi.coupon.application.service.CouponService;
import com.example.ecommerceapi.order.application.service.OrderService;
import com.example.ecommerceapi.point.application.service.PointService;
import com.example.ecommerceapi.product.application.service.ProductService;
import com.example.ecommerceapi.product.application.service.RankingService;
import com.example.ecommerceapi.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 초기 세팅 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;
    private final PointService pointService;
    private final ProductService productService;
    private final CouponService couponService;
    private final CartService cartService;
    private final OrderService orderService;
    private final RankingService rankingService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing data...");

        // redis data
        rankingService.clearAllRankings();

        // data
        userService.init();
        pointService.init();
        productService.init();
        couponService.init();
        cartService.init();
        orderService.init();

        log.info("initialization completed");
    }
}
