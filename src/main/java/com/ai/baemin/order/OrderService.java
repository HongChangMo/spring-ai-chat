package com.ai.baemin.order;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Tool(description = "주문 ID로 현재 주문 상태를 조회합니다.")
    public String getOrderStatus(String orderId) {
        return orderRepository.findById(orderId)
                .map(o -> String.format("주문 %s의 현재 상태: %s (메뉴: %s)", o.orderId(), o.status().label(), o.menu()))
                .orElse("주문 " + orderId + "을(를) 찾을 수 없습니다.");
    }

    @Tool(description = "주문 ID로 배달 현황 및 위치를 추적합니다.")
    public String trackDelivery(String orderId) {
        return orderRepository.findById(orderId)
                .map(o -> String.format("주문 %s 배달 현황: %s, 현재 위치: %s", o.orderId(), o.status().label(), o.location()))
                .orElse("주문 " + orderId + "을(를) 찾을 수 없습니다.");
    }

    @Tool(description = "주문 취소를 요청합니다. 취소 가능 여부를 확인하고 사용자에게 확인을 요청합니다.")
    public String cancelOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(o -> {
                    if (!o.status().isCancellable()) {
                        return String.format("주문 %s은(는) %s 상태로 취소할 수 없습니다.", orderId, o.status().label());
                    }
                    return String.format("주문 %s(%s)을 취소하시겠습니까? 확인하시면 취소를 진행합니다.", orderId, o.menu());
                })
                .orElse("주문 " + orderId + "을(를) 찾을 수 없습니다.");
    }

    @Tool(description = "사용자가 확인한 후 주문을 실제로 취소합니다.")
    public String confirmCancelOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(o -> {
                    orderRepository.save(o.withStatus(OrderStatus.CANCELLED));
                    return String.format("주문 %s이(가) 취소되었습니다.", orderId);
                })
                .orElse("주문 " + orderId + "을(를) 찾을 수 없습니다.");
    }

    @Tool(description = "환불을 요청합니다. 사용자 확인 후 confirmRefund를 호출하세요.")
    public String requestRefund(String orderId, String reason) {
        return orderRepository.findById(orderId)
                .map(o -> String.format("주문 %s에 대해 '%s' 사유로 환불을 요청하시겠습니까? 확인하시면 환불을 진행합니다.", orderId, reason))
                .orElse("주문 " + orderId + "을(를) 찾을 수 없습니다.");
    }

    @Tool(description = "사용자가 확인한 후 환불을 실제로 처리합니다.")
    public String confirmRefund(String orderId) {
        return orderRepository.findById(orderId)
                .map(o -> {
                    orderRepository.save(o.withStatus(OrderStatus.REFUNDED));
                    return String.format("주문 %s의 환불이 처리되었습니다.", orderId);
                })
                .orElse("주문 " + orderId + "을(를) 찾을 수 없습니다.");
    }
}
