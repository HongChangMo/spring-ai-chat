package com.ai.baemin.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceCancelRefundTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        repository.save(new OrderData("ORDER-WAIT", OrderStatus.WAITING, "강남구", "치킨버거"));
        repository.save(new OrderData("ORDER-COOK", OrderStatus.COOKING, "서초구", "불고기버거"));
        repository.save(new OrderData("ORDER-DONE", OrderStatus.DELIVERED, "마포구", "새우버거"));
        orderService = new OrderService(repository);
    }

    @Test
    void 접수대기_주문은_취소_확인_메시지를_반환한다() {
        String result = orderService.cancelOrder("ORDER-WAIT");

        assertThat(result).contains("취소").contains("확인");
    }

    @Test
    void 조리중_주문은_취소_불가_메시지를_반환한다() {
        String result = orderService.cancelOrder("ORDER-COOK");

        assertThat(result).contains("취소할 수 없습니다");
    }

    @Test
    void 존재하지_않는_주문은_취소_불가_메시지를_반환한다() {
        String result = orderService.cancelOrder("ORDER-999");

        assertThat(result).contains("찾을 수 없습니다");
    }

    @Test
    void confirmCancelOrder는_주문을_CANCELLED_상태로_변경한다() {
        orderService.confirmCancelOrder("ORDER-WAIT");

        String status = orderService.getOrderStatus("ORDER-WAIT");
        assertThat(status).contains(OrderStatus.CANCELLED.label());
    }

    @Test
    void requestRefund는_환불_확인_메시지를_반환한다() {
        String result = orderService.requestRefund("ORDER-DONE", "음식 품질 불량");

        assertThat(result).contains("환불").contains("확인");
    }

    @Test
    void 배달완료_아닌_주문은_환불_불가_메시지를_반환한다() {
        String result = orderService.requestRefund("ORDER-COOK", "단순 변심");

        assertThat(result).contains("환불할 수 없습니다");
    }

    @Test
    void confirmRefund는_주문을_REFUNDED_상태로_변경한다() {
        orderService.confirmRefund("ORDER-DONE");

        String status = orderService.getOrderStatus("ORDER-DONE");
        assertThat(status).contains(OrderStatus.REFUNDED.label());
    }
}
