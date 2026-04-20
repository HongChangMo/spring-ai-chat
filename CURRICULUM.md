# Spring AI 학습 커리큘럼
## 배민 고객 상담 에이전트 구축

> Java/Spring 개발자를 위한 Spring AI 단계별 실습 프로젝트  
> LLM: Anthropic Claude | 목표: 주문 조회 · 배달 추적 · 취소/환불 처리 에이전트

---

## 단계별 로드맵

### Step 1 — 프로젝트 부트스트랩
**목표:** Spring AI + Anthropic 연동, 첫 번째 대화 완성

- Spring Boot 프로젝트 생성 (spring-ai-bom 의존성)
- `application.yml`에 Anthropic API 키 설정
- `ChatClient` 빈 구성
- REST 엔드포인트 `/chat` 구현 → 단순 Q&A 동작 확인

**핵심 개념:** `ChatClient`, `ChatClient.Builder`, Anthropic Spring AI 자동 설정  
**커밋 태그:** `step-1/bootstrap`

---

### Step 2 — Tool Calling
**목표:** LLM이 판단하고, 서버 함수를 직접 호출하는 구조 이해

- 가짜(Mock) 주문 데이터 준비
- `@Tool` 메서드 구현
  - `getOrderStatus(orderId)` — 주문 상태 조회
  - `trackDelivery(orderId)` — 배달 위치 추적
- `ChatClient`에 Tool 등록
- "내 주문 어디까지 왔어?" → LLM이 함수 선택 → 결과 답변

**핵심 개념:** `@Tool`, Function Calling 흐름 (LLM → Tool 호출 → 결과 반환 → 최종 답변)  
**커밋 태그:** `step-2/tool-calling`

---

### Step 3 — Chat Memory
**목표:** 대화 맥락 유지 (이전 주문 번호 기억, 연속 질문 처리)

- `InMemoryChatMemory` 구성
- `MessageChatMemoryAdvisor` 적용
- 세션 ID 기반 대화 분리
- 테스트: "내 주문 상태 알려줘" → "언제 도착해?" (주문번호 재입력 없이 동작)

**핵심 개념:** `ChatMemory`, `MessageChatMemoryAdvisor`, 대화 히스토리 윈도우 설정  
**커밋 태그:** `step-3/chat-memory`

---

### Step 4 — Advisor
**목표:** 시스템 프롬프트 · 로깅 · 입력 전처리 레이어 구성

- `SystemPromptTemplateAdvisor`로 상담원 역할 부여
- `SimpleLoggerAdvisor`로 요청/응답 로깅
- 커스텀 Advisor 작성: 비속어 필터, 입력 정규화
- Advisor 체인 순서 이해

**핵심 개념:** `Advisor` 인터페이스, `AdvisedRequest`, Advisor 실행 순서  
**커밋 태그:** `step-4/advisor`

---

### Step 5 — RAG (Retrieval-Augmented Generation)
**목표:** FAQ/환불 정책 문서를 검색해 답변 품질 높이기

- 샘플 FAQ 문서 준비 (환불 정책, 배달 지연 보상 등)
- `SimpleVectorStore` 또는 `PgVector` 설정
- 문서 임베딩 → VectorStore 저장
- `QuestionAnswerAdvisor`로 RAG 파이프라인 연결
- 테스트: "비 올 때 배달 지연되면 어떻게 돼?" → 정책 문서 기반 답변

**핵심 개념:** `VectorStore`, `EmbeddingModel`, `DocumentReader`, `QuestionAnswerAdvisor`  
**커밋 태그:** `step-5/rag`

---

### Step 6 — 취소/환불 처리 Tool
**목표:** 위험 액션(취소/환불)을 Tool Calling으로 안전하게 처리

- `cancelOrder(orderId)`, `requestRefund(orderId, reason)` Tool 구현
- 실행 전 사용자 확인 단계 설계 ("정말 취소하시겠어요?")
- 취소 가능 조건 검증 로직 (조리 시작 후 취소 불가 등)
- Step 2 Tool들과 통합

**핵심 개념:** 상태 기반 Tool 설계, 위험 액션 가드 패턴  
**커밋 태그:** `step-6/cancel-refund`

---

### Step 7 — Guardrail + 마무리
**목표:** 할루시네이션 방지, 민감 정보 차단, 프로젝트 완성

- 출력 Guardrail: 응답에 개인정보(전화번호, 주소) 포함 시 마스킹
- 입력 Guardrail: 범위 밖 질문 거절 ("주식 추천해줘" → 거절)
- 프롬프트 인젝션 방어 기본 패턴
- README 작성, 전체 흐름 다이어그램 추가

**핵심 개념:** Output Parsing, 커스텀 Guardrail Advisor, 방어적 프롬프트 설계  
**커밋 태그:** `step-7/guardrail`

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Spring AI | 1.x (spring-ai-bom) |
| LLM | Anthropic Claude (claude-sonnet-4-6) |
| Vector Store | SimpleVectorStore (Step 5), 필요시 PgVector |
| Build | Gradle |
| 배포 | 로컬 실행 (GitHub 코드 공유) |

---

## 학습 원칙

- 각 단계는 **독립 커밋**으로 분리 → GitHub 히스토리 = 학습 기록
- Mock 데이터로 시작, 실제 DB는 선택 사항
- 단계마다 `README` 섹션 업데이트

---

## 참고 자료

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [Anthropic Spring AI 연동](https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html)
