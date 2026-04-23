package com.ai.baemin.order;

public enum OrderStatus {
    WAITING("접수 대기"),
    COOKING("조리 중"),
    DELIVERING("배달 중"),
    DELIVERED("배달 완료"),
    CANCELLED("취소됨"),
    REFUNDED("환불됨");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isCancellable() {
        return this == WAITING;
    }

    public boolean isRefundable() {
        return this == DELIVERED;
    }
}
