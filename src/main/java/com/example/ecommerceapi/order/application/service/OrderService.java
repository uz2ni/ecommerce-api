package com.example.ecommerceapi.order.application.service;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.exception.*;
import com.example.ecommerceapi.common.lock.DistributedLock;
import com.example.ecommerceapi.common.lock.LockType;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.order.application.dto.CreateOrderCommand;
import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.application.dto.OrderResult;
import com.example.ecommerceapi.order.application.dto.PaymentResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
     * <분산 락-MULTI>
     * payment:#orderId  // 동일 주문 중복 제어
     * point:#userId     // 포인트 차감 동시성 제어
     * <낙관적 락>
     * couponUser        // 쿠폰 중복 사용 제어
     * <비관적 락>
     * productId         // 재고 차감 동시성 제어
     */
    @DistributedLock(
            keys = { "'payment:' + #orderId", "'point:' + #userId" },
            type = LockType.MULTI,
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

}