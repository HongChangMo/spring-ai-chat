package com.ai.baemin.order;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, OrderData> store = new ConcurrentHashMap<>(Map.of(
            "ORDER-001", new OrderData("ORDER-001", OrderStatus.COOKING, "강남구 역삼동", "치킨버거 세트"),
            "ORDER-002", new OrderData("ORDER-002", OrderStatus.DELIVERING, "서초구 방배동", "불고기버거"),
            "ORDER-003", new OrderData("ORDER-003", OrderStatus.DELIVERED, "마포구 합정동", "새우버거 세트"),
            "ORDER-004", new OrderData("ORDER-004", OrderStatus.WAITING, "용산구 이태원동", "새우버거")
    ));

    @Override
    public Optional<OrderData> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public void save(OrderData order) {
        store.put(order.orderId(), order);
    }
}
