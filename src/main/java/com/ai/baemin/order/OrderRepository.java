package com.ai.baemin.order;

import java.util.Optional;

public interface OrderRepository {
    Optional<OrderData> findById(String orderId);
    void save(OrderData order);
}
