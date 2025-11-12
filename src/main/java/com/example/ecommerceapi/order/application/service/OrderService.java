package com.example.ecommerceapi.order.application.service;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.aspect.WithLock;
import com.example.ecommerceapi.common.exception.*;
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
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
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


    public CreateOrderResult createOrder(CreateOrderCommand command) {
        // 1. 사용자 검증
        User user = userValidator.validateAndGetUser(command.userId());

        // 2. 장바구니 조회 및 검증
        List<CartItem> cartItems = cartItemRepository.findByUserId(command.userId());
        if (cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CART_EMPTY);
        }

        // 3. 각 상품의 재고 검증
        for (CartItem cartItem : cartItems) {
            Product product = productValidator.validateAndGetProduct(cartItem.getProductId());
            product.validateStock(cartItem.getQuantity());
        }

        // 4. 쿠폰 검증 (쿠폰 ID가 0이 아닌 경우에만)
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

        // 5. 주문 총액 계산
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

    public PaymentResult processPayment(Integer orderId, Integer userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
        order.validatePaymentAvailable();

        User user = userValidator.validateAndGetUser(userId);

        // 보상 트랜잭션을 위한 스택
        // (Stack은 레거시,성능낮음 문제로 속도 빠른 Deque 채택)
        Deque<Runnable> compensationStack = new ArrayDeque<>();

        try {
            // 1. 포인트 차감
            Integer paymentAmount = order.getFinalPaymentAmount();
            user.usePoints(paymentAmount);
            userRepository.save(user);
            compensationStack.push(() -> {
                user.addPoints(paymentAmount);
                userRepository.save(user);
            });

            // 2. 포인트 사용 이력 저장
            Point point = Point.createUseHistory(user, paymentAmount);
            pointRepository.save(point);
            compensationStack.push(() -> pointRepository.delete(point.getPointId()));

            // 3. 상품 재고 차감
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items) {
                Product product = productRepository.findById(item.getProduct().getProductId());
                product.decreaseStock(item.getOrderQuantity());
                productRepository.save(product);

                compensationStack.push(() -> {
                    product.increaseStock(item.getOrderQuantity());
                    productRepository.save(product);
                });
            }

            // 4. 쿠폰 사용 처리
            if (order.hasCoupon()) {
                CouponUser couponUser = couponUserRepository
                        .findByCouponIdAndUserId(order.getCoupon().getCouponId(), userId)
                        .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_ISSUED));

                couponUser.markAsUsed();
                couponUserRepository.save(couponUser);

                compensationStack.push(() -> {
                    couponUser.markAsUnused();
                    couponUserRepository.save(couponUser);
                });
            }

            // 5. 장바구니 삭제
            // 복원용 장바구니 아이템 보관
            List<CartItem> backupCartItems = cartItemRepository.findByUserId(userId)
                    .stream()
                    .map(CartItem::deepCopy) // 깊은 복사 필요
                    .collect(Collectors.toList());
            cartItemRepository.deleteByUserId(userId);
            compensationStack.push(() -> backupCartItems.forEach(cartItemRepository::save));

            // 6. 주문 상태 변경
            order.completePayment();
            orderRepository.save(order);
            compensationStack.push(() -> {
                order.markPaymentFailed();
                orderRepository.save(order);
            });

            return PaymentResult.from(order, user.getPointBalance());
        }
        catch (Exception e) {
            log.error("결제 처리 중 오류 발생. 주문ID: {}, 사용자ID: {}", orderId, userId, e);
            // 실패 시 보상 트랜잭션 역순 실행
            while (!compensationStack.isEmpty()) {
                try { compensationStack.pop().run(); }
                catch (Exception compensationException) {
                    log.warn("보상 트랜잭션 실행 중 오류 발생", compensationException);
                }
            }

            // 원래 예외가 비즈니스 예외면 그대로 던지기
            if (e instanceof BusinessException) {
                throw e;
            }
            // 예상치 못한 시스템 예외는 OrderException으로 감싸기
            throw new OrderException(ErrorCode.ORDER_PAY_FAILED, e);
        }
    }

}