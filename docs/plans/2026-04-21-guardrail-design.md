# Step 7 Guardrail 설계

## 결정 사항

- 구현 방식: 커스텀 `BaseAdvisor` (`common/advisor/`)
- 거절 처리: 예외 → `GlobalExceptionHandler` → 400 Bad Request

## 컴포넌트

### InputGuardrailAdvisor (order=0)

`before()`에서 두 가지 검사:
1. 프롬프트 인젝션 패턴 탐지 (`ignore previous instructions`, `system prompt`, `너는 이제부터` 등)
2. 범위 외 질문 탐지 (배달/주문/취소/환불/쿠폰/리뷰 키워드 없으면 거절)

### OutputGuardrailAdvisor (order=Integer.MAX_VALUE)

`after()`에서 AI 응답 마스킹:
- 전화번호: `010-1234-5678` → `010-****-****`
- 주소 상세: `역삼동 123-4` → `역삼동 ***`

## Advisor 체인 순서

```
InputGuardrailAdvisor(0) → InputNormalizationAdvisor(1) → SystemPromptAdvisor(2)
→ QuestionAnswerAdvisor(3) → SimpleLoggerAdvisor(4) → MessageChatMemoryAdvisor(5)
→ OutputGuardrailAdvisor(MAX)
```

## 테스트

- `InputGuardrailAdvisorTest`: 단위 테스트 (인젝션, 범위 외, 정상 통과)
- `OutputGuardrailAdvisorTest`: 단위 테스트 (전화번호/주소 마스킹)
- `GuardrailIntegrationTest`: `@WebMvcTest` (400 반환, 마스킹 확인)
