package com.ai.baemin.order;

public record OrderData(String orderId, OrderStatus status, String location, String menu) {

    public OrderData withStatus(OrderStatus newStatus) {
        return new OrderData(orderId, newStatus, location, menu);
    }
}
