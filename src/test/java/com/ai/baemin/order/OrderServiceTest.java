package com.ai.baemin.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(new InMemoryOrderRepository());
    }

    @Test
    void getOrderStatus_존재하는_주문ID로_조회하면_상태를_반환한다() {
        String result = orderService.getOrderStatus("ORDER-001");
        assertThat(result).contains("ORDER-001");
        assertThat(result).isNotEmpty();
    }

    @Test
    void getOrderStatus_존재하지_않는_주문ID로_조회하면_오류메시지를_반환한다() {
        String result = orderService.getOrderStatus("ORDER-999");
        assertThat(result).contains("찾을 수 없습니다");
    }

    @Test
    void trackDelivery_존재하는_주문ID로_조회하면_배달위치를_반환한다() {
        String result = orderService.trackDelivery("ORDER-001");
        assertThat(result).contains("ORDER-001");
        assertThat(result).isNotEmpty();
    }

    @Test
    void trackDelivery_존재하지_않는_주문ID로_조회하면_오류메시지를_반환한다() {
        String result = orderService.trackDelivery("ORDER-999");
        assertThat(result).contains("찾을 수 없습니다");
    }
}
