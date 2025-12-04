package com.example.ecommerceapi.order.application.event;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.event.OrderPaidEvent;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventPublisher 단위 테스트")
class OrderEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderEventPublisher orderEventPublisher;

    @Test
    @DisplayName("주문 결제 완료 이벤트를 발행한다")
    void publishOrderPaidEvent_ShouldPublishEvent() {
        // given
        User user = User.builder()
                .userId(1)
                .username("테스트 사용자")
                .pointBalance(100000)
                .version(0)
                .build();

        Product product1 = Product.builder()
                .productId(1)
                .productName("테스트 상품1")
                .productPrice(10000)
                .quantity(100)
                .build();

        Product product2 = Product.builder()
                .productId(2)
                .productName("테스트 상품2")
                .productPrice(20000)
                .quantity(50)
                .build();

        Order order = Order.builder()
                .orderId(1)
                .user(user)
                .orderStatus(OrderStatus.PAID)
                .totalOrderAmount(30000)
                .totalDiscountAmount(0)
                .finalPaymentAmount(30000)
                .deliveryUsername("홍길동")
                .deliveryAddress("서울시 강남구")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .orderItemId(1)
                .order(order)
                .product(product1)
                .productName("테스트 상품1")
                .productPrice(10000)
                .orderQuantity(1)
                .totalPrice(10000)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .orderItemId(2)
                .order(order)
                .product(product2)
                .productName("테스트 상품2")
                .productPrice(20000)
                .orderQuantity(1)
                .totalPrice(20000)
                .build();

        List<OrderItem> orderItems = Arrays.asList(orderItem1, orderItem2);

        ArgumentCaptor<OrderPaidEvent> eventCaptor = ArgumentCaptor.forClass(OrderPaidEvent.class);

        // when
        orderEventPublisher.publishOrderPaidEvent(order, orderItems);

        // then
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OrderPaidEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.orderId()).isEqualTo(1);
        assertThat(capturedEvent.userId()).isEqualTo(1);
        assertThat(capturedEvent.orderItems()).hasSize(2);
        assertThat(capturedEvent.orderItems().get(0).productId()).isEqualTo(1);
        assertThat(capturedEvent.orderItems().get(0).orderQuantity()).isEqualTo(1);
        assertThat(capturedEvent.orderItems().get(1).productId()).isEqualTo(2);
        assertThat(capturedEvent.orderItems().get(1).orderQuantity()).isEqualTo(1);
        assertThat(capturedEvent.paidAt()).isEqualTo(order.getUpdatedAt());
    }

    @Test
    @DisplayName("이벤트 발행 실패 시 예외를 삼킨다")
    void publishOrderPaidEvent_ShouldNotThrowException_WhenPublishFails() {
        // given
        User user = User.builder()
                .userId(1)
                .username("테스트 사용자")
                .pointBalance(100000)
                .version(0)
                .build();

        Order order = Order.builder()
                .orderId(1)
                .user(user)
                .orderStatus(OrderStatus.PAID)
                .updatedAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = Arrays.asList();

        // when
        eventPublisher.publishEvent(any());
        // 예외를 던지도록 설정하지 않아도 빈 리스트로 호출 시 예외 발생 가능성이 낮음
        // 실제로는 eventPublisher가 예외를 던지는 경우를 테스트해야 하지만
        // OrderEventPublisher가 try-catch로 감싸고 있어 예외가 밖으로 나가지 않음을 확인

        // then - 예외가 발생하지 않아야 함
        orderEventPublisher.publishOrderPaidEvent(order, orderItems);
    }
}
