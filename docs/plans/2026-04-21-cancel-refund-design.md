# Step 6 취소/환불 처리 설계

## 개요

`cancelOrder`, `confirmCancelOrder`, `requestRefund`, `confirmRefund` Tool을 추가해 2단계 확인 흐름으로 취소/환불을 처리한다.

## 패키지 구조

```
order/
  OrderStatus.java      # 상태 enum (isCancellable, label 포함)
  OrderData.java        # 주문 데이터 record (별도 파일)
  OrderRepository.java  # ConcurrentHashMap 기반 인메모리 저장소 (@Repository)
  OrderService.java     # OrderRepository 의존성 주입, Tool 4개 추가
```

## 취소 가능 조건

| 상태 | 취소 가능 |
|------|----------|
| WAITING (접수 대기) | O |
| COOKING (조리 중) | X |
| DELIVERING (배달 중) | X |
| DELIVERED (배달 완료) | X |
| CANCELLED / REFUNDED | X |

## Tool 흐름

```
1. cancelOrder(orderId)       → 조건 체크 → 확인 메시지 반환
2. confirmCancelOrder(orderId) → 실제 CANCELLED 상태로 변경

1. requestRefund(orderId, reason) → 확인 메시지 반환
2. confirmRefund(orderId)          → 실제 REFUNDED 상태로 변경
```

## Mock 데이터 (OrderRepository 초기값)

- ORDER-001: COOKING (조리 중) → 취소 불가
- ORDER-002: DELIVERING (배달 중) → 취소 불가
- ORDER-003: DELIVERED (배달 완료) → 취소 불가
- ORDER-004: WAITING (접수 대기) → 취소 가능

## 테스트 전략

- `OrderServiceCancelRefundTest`: 테스트용 OrderRepository에 원하는 데이터 주입, Tool 동작 단위 테스트
- `CancelRefundIntegrationTest`: WebMvcTest로 Step 2 Tool과 통합 확인
