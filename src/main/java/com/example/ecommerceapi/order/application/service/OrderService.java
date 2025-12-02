package com.example.ecommerceapi.order.application.service;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.redis.CacheType;
import com.example.ecommerceapi.common.exception.*;
import com.example.ecommerceapi.common.lock.DistributedLock;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.order.application.dto.CreateOrderCommand;
import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.application.dto.OrderResult;
import com.example.ecommerceapi.order.application.dto.PaymentResult;
import com.example.ecommerceapi.order.application.event.OrderEventPublisher;
import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserValidator userValidator;
    private final ProductValidator productValidator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final OrderEventPublisher orderEventPublisher;


    @Transactional
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        // 1. 사용자 검증
        User user = userValidator.validateAndGetUser(command.userId());

        // 2. 주문 중복 검증
        boolean pendingOrderExists = orderRepository.existsByUserIdAndOrderStatus(user.getUserId(), OrderStatus.PENDING);
        if (pendingOrderExists) {
            throw new OrderException(ErrorCode.ORDER_ALREADY_EXISTS);
        }

        // 3. 장바구니 조회 및 검증
        List<CartItem> cartItems = cartItemRepository.findByUserId(command.userId());
        if (cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CART_EMPTY);
        }

        // 4. 각 상품의 재고 검증
        for (CartItem cartItem : cartItems) {
            Product product = productValidator.validateAndGetProduct(cartItem.getProductId());
            product.validateStock(cartItem.getQuantity());
        }

        // 5. 쿠폰 검증 (쿠폰 ID가 0이 아닌 경우에만)
        Integer discountAmount = 0;
        Coupon validCoupon = null;
        if (command.couponId() != null && command.couponId() != 0) {
            // 쿠폰 존재 여부 확인
            Optional<Coupon> couponOpt = couponRepository.findById(command.couponId());
            if (couponOpt.isEmpty()) {
                throw new CouponException(ErrorCode.COUPON_NOT_FOUND);
            }
            Coupon coupon = couponOpt.get();

            // 쿠폰 만료 여부 확인
            coupon.validateNotExpired();

            // 사용자가 쿠폰을 발급받았는지 확인
            Optional<CouponUser> couponUserOpt = couponUserRepository.findByCouponIdAndUserId(command.couponId(), command.userId());
            if (couponUserOpt.isEmpty()) {
                throw new CouponException(ErrorCode.COUPON_NOT_ISSUED);
            }

            // 쿠폰이 이미 사용되었는지 확인
            CouponUser couponUser = couponUserOpt.get();
            couponUser.validateUsable();

            discountAmount = coupon.getDiscountAmount();
            validCoupon = coupon;
        }

        // 6. 주문 총액 계산
        Integer totalOrderAmount = cartItems.stream()
                .mapToInt(CartItem::getTotalPrice)
                .sum();

        // 6. 주문 생성
        Order order = Order.createOrder(
                user,
                command.deliveryUsername(),
                command.deliveryAddress(),
                totalOrderAmount,
                discountAmount,
                validCoupon
        );
        order = orderRepository.save(order);

        // 7. 주문 항목 생성
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId());
            OrderItem orderItem = OrderItem.createOrderItem(
                    order,
                    product,
                    cartItem.getQuantity()
            );
            orderItem = orderItemRepository.save(orderItem);
            orderItems.add(orderItem);
        }

        return CreateOrderResult.from(order);
    }

    @Cacheable(value = CacheType.Names.ORDER, key = "#orderId")
    @Transactional(readOnly = true)
    public OrderResult getOrder(Integer orderId) {
        // 1. 주문 조회
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new OrderException(ErrorCode.ORDER_NOT_FOUND);
        }
        Order order = orderOpt.get();

        // 2. 주문 항목 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return OrderResult.buildGetOrder(order, orderItems);
    }


    /**
     * 쿠폰 사용 처리
     */
    @Transactional
    public CouponUser useCouponWithOptimisticLock(Integer couponId, Integer userId) {
        CouponUser couponUser = couponUserRepository
                .findByCouponIdAndUserId(couponId, userId)
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_ISSUED));

        couponUser.markAsUsed();
        return couponUserRepository.save(couponUser);
    }

    /**
     * 결제 처리
     * 전체 결제 프로세스를 하나의 트랜잭션으로 처리하여 원자성 보장
     *
     * <분산 락-SIMPLE>
     * point:#userId     // 포인트 차감 동시성 제어
     * <낙관적 락>
     * couponUser        // 쿠폰 중복 사용 제어
     * <비관적 락>
     * productId         // 재고 차감 동시성 제어
     */
    @CacheEvict(value = CacheType.Names.ORDER, key = "#orderId")
    @DistributedLock(
            key = "'point:' + #userId",
            waitTime = 5,
            leaseTime = 10
    )
    @Transactional
    public PaymentResult processPayment(Integer orderId, Integer userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.validatePaymentAvailable();

        try {
            // 1. 포인트 차감 (분산 락 적용)
            Integer paymentAmount = order.getFinalPaymentAmount();
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new UserException(ErrorCode.USER_NOT_FOUND);
            }
            user.usePoints(paymentAmount);
            userRepository.save(user);

            // 2. 포인트 사용 이력 저장
            Point point = Point.createUseHistory(user, paymentAmount);
            pointRepository.save(point);

            // 3. 상품 재고 차감 (비관적 락 사용)
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

            // 데드락 방지: ProductId 순서로 정렬하여 항상 같은 순서로 락 획득
            items.sort(Comparator.comparing(item -> item.getProduct().getProductId()));

            for (OrderItem item : items) {
                Product product = productRepository.findByIdWithLock(item.getProduct().getProductId());
                if (product == null) {
                    throw new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
                }
                product.decreaseStock(item.getOrderQuantity());
                productRepository.save(product);
            }

            // 4. 쿠폰 사용 처리 (낙관적 락 사용)
            if (order.hasCoupon()) {
                useCouponWithOptimisticLock(order.getCoupon().getCouponId(), userId);
            }

            // 5. 장바구니 삭제
            cartItemRepository.deleteByUserId(userId);

            // 6. 주문 상태 변경
            order.completePayment();
            orderRepository.save(order);

            // 7. 주문 결제 완료 이벤트 발행 (판매 랭킹 업데이트 트리거)
            orderEventPublisher.publishOrderPaidEvent(order, items);

            return PaymentResult.from(order, user.getPointBalance());
        }
        catch (Exception e) {
            log.error("결제 처리 중 오류 발생. 주문ID: {}, 사용자ID: {}", orderId, userId, e);

            // 원래 예외가 비즈니스 예외면 그대로 던지기
            if (e instanceof BusinessException) {
                throw e;
            }
            // 예상치 못한 시스템 예외는 OrderException으로 감싸기
            throw new OrderException(ErrorCode.ORDER_PAY_FAILED, e);
        }
    }

    /**
     * 초기 주문 및 결제 데이터 생성 (테스트/개발용)
     * 실제 비즈니스 로직(createOrder, processPayment)을 사용하여 랭킹 조회를 위한 충분한 데이터 생성
     */
    @Transactional
    public void init() {
        log.info("Initializing orders and payments...");

        // 사용자 및 상품 조회 (최대 10개 상품, 5명 사용자)
        List<User> users = new ArrayList<>();
        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            User user = userRepository.findById(i);
            if (user != null) users.add(user);
        }

        for (int i = 1; i <= 10; i++) {
            Product product = productRepository.findById(i);
            if (product != null) products.add(product);
        }

        if (users.isEmpty() || products.isEmpty()) {
            log.warn("No users or products found for order initialization");
            return;
        }

        // 오늘 날짜 기준으로 다양한 주문 생성
        int orderCount = 0;

        // 상품별 주문 생성 - 상품 3이 가장 많이 팔리도록
        // 상품 1: 3개, 상품 2: 5개, 상품 3: 10개, 상품 4: 6개, 상품 5: 4개
        orderCount += createOrdersForDay(users, products, new int[]{1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5});

        log.info("Orders and payments initialization completed. Total orders: {}", orderCount);
    }

    /**
     * 주문 생성 및 결제 완료 (실제 비즈니스 로직 사용)
     */
    private int createOrdersForDay(List<User> users, List<Product> products, int[] productIndices) {
        int count = 0;

        for (int productIdx : productIndices) {
            if (productIdx > products.size()) continue;

            Product product = products.get(productIdx - 1);
            User user = users.get(count % users.size());

            try {
                // 1. 장바구니에 상품 추가
                CartItem cartItem = CartItem.builder()
                        .user(user)
                        .product(product)
                        .productName(product.getProductName())
                        .productPrice(product.getProductPrice())
                        .quantity(1)
                        .totalPrice(product.getProductPrice())
                        .createdAt(LocalDateTime.now())
                        .build();
                cartItemRepository.save(cartItem);

                // 2. 주문 생성
                CreateOrderCommand orderCommand = new CreateOrderCommand(
                        user.getUserId(),
                        user.getUsername(),
                        "서울시 강남구 테헤란로 123",  // 더미 주소
                        null  // 쿠폰 없음
                );
                CreateOrderResult orderResult = createOrder(orderCommand);

                // 3. 결제 처리 (이벤트를 통해 자동으로 랭킹 업데이트)
                processPayment(orderResult.orderId(), user.getUserId());

                count++;
                log.debug("Order created and paid for product {} by user {}",
                        product.getProductId(), user.getUserId());
            } catch (Exception e) {
                log.error("Failed to create order for product {}: {}", product.getProductId(), e.getMessage());
                // 실패한 경우 장바구니 정리
                cartItemRepository.deleteByUserId(user.getUserId());
            }
        }

        return count;
    }

}