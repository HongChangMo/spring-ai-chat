# Spring AI 학습 프로젝트 — 배민 고객 상담 챗봇

배달의민족 고객 상담 시나리오를 모티브로 한 Spring AI 학습 프로젝트입니다.  
Step 1~7을 순서대로 구현하며 Spring AI의 핵심 기능을 단계별로 익힙니다.

---

## 아키텍처

```
클라이언트
    │
    ▼
POST /chat  (X-Session-Id 헤더 필수)
    │
    ▼
ChatController
    │
    ├─ [1] 길이 검증 (> 500자 → 400)
    ├─ [2] InputGuardrailAdvisor.validate()
    │       ├─ 프롬프트 인젝션 탐지 → 400
    │       └─ 범위 외 질문 탐지 → 400
    │
    └─ ChatClient.prompt()
            │
            ├─ tools(OrderService)  ← Tool Calling
            │
            └─ advisors (order 오름차순으로 before, 역순으로 after)
                    │
                    ├─ InputNormalizationAdvisor  (order=1)   before: 공백 정규화
                    ├─ SystemPromptAdvisor         (order=2)   before: 시스템 프롬프트 주입
                    ├─ QuestionAnswerAdvisor       (order=3)   before: RAG 컨텍스트 주입
                    ├─ SimpleLoggerAdvisor         (order=4)   before/after: 요청·응답 로깅
                    ├─ MessageChatMemoryAdvisor    (order=5)   before: 히스토리 주입, after: 저장
                    └─ OutputGuardrailAdvisor      (order=MAX) after: 개인정보 마스킹
                            │
                            ▼
                        OpenAI API (GPT)
```

---

## 단계별 구현 내용

| Step | 브랜치 | 주제 | 핵심 기능 |
|------|--------|------|-----------|
| 1 | step1 | 프로젝트 부트스트랩 | Spring Boot + Spring AI, `/chat` 엔드포인트 |
| 2 | step2 | Tool Calling | `@Tool` — 주문 상태 조회, 배달 위치 추적 |
| 3 | step3 | Chat Memory | `InMemoryChatMemory`, 세션 ID 기반 대화 분리 |
| 4 | step4 | Advisor | `SystemPromptAdvisor`, `SimpleLoggerAdvisor`, 커스텀 Advisor |
| 5 | step5 | RAG | `SimpleVectorStore`, `QuestionAnswerAdvisor`, FAQ/환불 문서 임베딩 |
| 6 | step6 | 취소/환불 | 2단계 확인 흐름, `cancelOrder`, `requestRefund` Tool |
| 7 | step7 | Guardrail | 입력 검증(인젝션/범위), 출력 마스킹(전화번호·주소) |

---

## 패키지 구조

```
com.ai.baemin
├── chat/               # AI 채팅 (Controller, DTO)
├── order/              # 주문 도메인 (@Tool, OrderRepository, OrderStatus)
├── common/
│   └── advisor/        # Advisor, Guardrail, GuardrailConfig
└── config/             # Spring Bean 설정 (ChatClient, VectorStore)
```

---

## 주요 컴포넌트

### Tool Calling (Step 2, 6)

`OrderService`에 `@Tool`로 정의된 6개의 Tool:

| Tool | 설명 |
|------|------|
| `getOrderStatus(orderId)` | 주문 상태 조회 |
| `trackDelivery(orderId)` | 배달 위치 추적 |
| `cancelOrder(orderId)` | 취소 가능 여부 확인 + 확인 메시지 반환 |
| `confirmCancelOrder(orderId)` | 취소 확정 (CANCELLED) |
| `requestRefund(orderId, reason)` | 환불 요청 확인 메시지 반환 |
| `confirmRefund(orderId)` | 환불 확정 (REFUNDED) |

취소·환불은 2단계 확인 흐름으로 동작합니다 — Tool이 확인 메시지를 반환하면 AI가 사용자에게 전달하고, 사용자 확인 후 confirm Tool이 실제 상태를 변경합니다.

### Chat Memory (Step 3)

`MessageChatMemoryAdvisor` + `InMemoryChatMemory`로 세션 ID 기반 대화 히스토리 관리.  
`X-Session-Id` 헤더로 대화 세션을 구분합니다.

### RAG (Step 5)

`src/main/resources/docs/`의 FAQ와 환불 정책 문서를 `SimpleVectorStore`에 임베딩.  
`QuestionAnswerAdvisor`가 질문 관련 문서를 검색해 컨텍스트로 주입합니다.

### Guardrail (Step 7)

**InputGuardrailAdvisor** — 입력 검증 (ChatClient 호출 전):
- 프롬프트 인젝션 패턴 탐지 (`ignore previous instructions`, `system prompt`, `너는 이제부터` 등)
- 범위 외 질문 거절 — 주문·배달·취소·환불·쿠폰·리뷰 키워드 없으면 400

**OutputGuardrailAdvisor** — 출력 마스킹 (응답 반환 전):
- 전화번호: `010-1234-5678` → `010-****-****`
- 주소 상세: `역삼동 123-4` → `역삼동 ***`

패턴 목록은 `GuardrailConfig`에서 Java 설정으로 관리합니다 (재컴파일 없이 수정 불가 — 운영 환경에서는 `@ConfigurationProperties` 방식이 유연합니다).

---

## 실행 방법

```bash
# Docker로 실행
docker-compose up

# 또는 직접 실행 (OPENAI_API_KEY 환경 변수 필요)
OPENAI_API_KEY=sk-... ./gradlew bootRun
```

### API 예시

```bash
# 주문 상태 조회
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: session-001" \
  -d '{"message": "ORDER-001 주문 상태 알려줘"}'

# 환불 처리 기간 문의 (RAG)
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: session-001" \
  -d '{"message": "환불 처리 기간이 얼마나 걸리나요?"}'

# 범위 외 질문 → 400
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: session-001" \
  -d '{"message": "오늘 날씨 어때?"}'
```

---

## 학습 단계 완료 현황

- [x] Step 1: 프로젝트 부트스트랩
- [x] Step 2: Tool Calling
- [x] Step 3: Chat Memory
- [x] Step 4: Advisor
- [x] Step 5: RAG
- [x] Step 6: 취소/환불 처리
- [x] Step 7: Guardrail

---

## 기술 스택

- Java 21 / Spring Boot 3.4.x / Spring AI 1.0.0
- OpenAI GPT (ChatModel, EmbeddingModel)
- Gradle / Docker / docker-compose