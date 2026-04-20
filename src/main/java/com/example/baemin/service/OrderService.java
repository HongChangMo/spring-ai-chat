package com.example.baemin.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderService {

    private static final Map<String, OrderData> ORDERS = Map.of(
        "ORDER-001", new OrderData("ORDER-001", "조리 중", "강남구 역삼동", "치킨버거 세트"),
        "ORDER-002", new OrderData("ORDER-002", "배달 중", "서초구 방배동", "불고기버거"),
        "ORDER-003", new OrderData("ORDER-003", "배달 완료", "마포구 합정동", "새우버거 세트")
    );

    @Tool(description = "주문 ID로 현재 주문 상태를 조회합니다.")
    public String getOrderStatus(String orderId) {
        OrderData order = ORDERS.get(orderId);
        if (order == null) {
            return "주문 " + orderId + "을(를) 찾을 수 없습니다.";
        }
        return String.format("주문 %s의 현재 상태: %s (메뉴: %s)", orderId, order.status(), order.menu());
    }

    @Tool(description = "주문 ID로 배달 현황 및 위치를 추적합니다.")
    public String trackDelivery(String orderId) {
        OrderData order = ORDERS.get(orderId);
        if (order == null) {
            return "주문 " + orderId + "을(를) 찾을 수 없습니다.";
        }
        return String.format("주문 %s 배달 현황: %s, 현재 위치: %s", orderId, order.status(), order.location());
    }

    private record OrderData(String orderId, String status, String location, String menu) {}
}
