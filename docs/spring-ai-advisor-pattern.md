# Spring AI Advisor 패턴

## 개요

Spring AI의 **Advisor**는 ChatClient의 요청/응답 파이프라인에 횡단 관심사(cross-cutting concerns)를 삽입하는 인터셉터 패턴입니다.
AOP의 Around Advice와 유사하게, 실제 LLM 호출 전/후에 로직을 실행할 수 있습니다.

---

## 핵심 인터페이스

### `CallAroundAdvisor`

동기 호출 파이프라인에 개입하는 기본 인터페이스입니다.

```java
public interface CallAroundAdvisor extends Advisor {
    AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain);
    String getName();
    default int getOrder() { return 0; }
}
```

| 메서드 | 설명 |
|--------|------|
| `aroundCall` | 요청을 가공하거나 응답을 후처리하는 핵심 메서드 |
| `getName` | Advisor 식별자 (로깅/디버깅용) |
| `getOrder` | 체인 내 실행 순서 (낮을수록 먼저 실행) |

### `AdvisedRequest`

LLM에 전달될 요청의 불변 스냅샷입니다. 수정하려면 `.mutate()`로 빌더를 열어 새 인스턴스를 생성합니다.

```java
// 시스템 프롬프트 덮어쓰기 예시
AdvisedRequest modified = request.mutate()
    .systemText("당신은 배민 고객 상담원입니다.")
    .build();
return chain.nextAroundCall(modified);
```

### `AdvisedResponse`

LLM 응답 래퍼입니다. 응답 후처리 시 사용합니다.

```java
AdvisedResponse response = chain.nextAroundCall(request);
// response.response().getResult().getOutput().getText() 로 응답 텍스트 접근
return response;
```

---

## Advisor 체인 실행 흐름

```
Client Request
      │
      ▼
┌─────────────────────────────────────────────┐
│  Advisor Chain (getOrder 오름차순)           │
│                                             │
│  1. InputNormalizationAdvisor (order=1)     │
│     └─ 입력 정규화 (전처리)                  │
│                                             │
│  2. SystemPromptAdvisor (order=2)           │
│     └─ 시스템 프롬프트 주입 (전처리)          │
│                                             │
│  3. SimpleLoggerAdvisor (order=3)           │
│     └─ 요청 로깅 → LLM 호출 → 응답 로깅     │
│                                             │
│  4. MessageChatMemoryAdvisor (order=4)      │
│     └─ 대화 이력 주입 및 저장               │
└─────────────────────────────────────────────┘
      │
      ▼
   LLM 호출
      │
      ▼
Client Response
```

> **주의:** `MessageChatMemoryAdvisor`는 반드시 체인의 **마지막** 또는 LLM과 가장 가까운 위치에 두어야 메모리에 올바른 컨텍스트가 쌓입니다.

---

## Advisor 구현 패턴

### 1. 시스템 프롬프트 주입 Advisor

매 요청마다 시스템 프롬프트를 동적으로 삽입합니다.

```java
@Component
public class SystemPromptAdvisor implements CallAroundAdvisor {

    private static final String SYSTEM_PROMPT = """
            당신은 배달의민족 고객 상담원입니다.
            주문, 배달, 취소, 환불 관련 질문만 답변하세요.
            항상 친절하고 간결하게 한국어로 답변하세요.
            """;

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        AdvisedRequest modified = request.mutate()
                .systemText(SYSTEM_PROMPT)
                .build();
        return chain.nextAroundCall(modified);
    }

    @Override
    public String getName() { return "SystemPromptAdvisor"; }

    @Override
    public int getOrder() { return 2; }
}
```

### 2. 로깅 Advisor

Spring AI 내장 `SimpleLoggerAdvisor`를 사용하거나 직접 구현합니다.

```java
// 내장 사용 (권장)
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;

// chatClient.prompt()
//     .advisors(new SimpleLoggerAdvisor())
//     ...
```

직접 구현 시:

```java
@Component
public class LoggingAdvisor implements CallAroundAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        log.info("[REQUEST] {}", request.userText());
        AdvisedResponse response = chain.nextAroundCall(request);
        log.info("[RESPONSE] {}", response.response().getResult().getOutput().getText());
        return response;
    }

    @Override
    public String getName() { return "LoggingAdvisor"; }

    @Override
    public int getOrder() { return 3; }
}
```

### 3. 커스텀 입력 정규화 Advisor

사용자 입력을 전처리(공백 정리, 금지어 필터 등)합니다.

```java
@Component
public class InputNormalizationAdvisor implements CallAroundAdvisor {

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        String normalized = request.userText()
                .strip()
                .replaceAll("\\s+", " ");

        AdvisedRequest modified = request.mutate()
                .userText(normalized)
                .build();
        return chain.nextAroundCall(modified);
    }

    @Override
    public String getName() { return "InputNormalizationAdvisor"; }

    @Override
    public int getOrder() { return 1; }
}
```

---

## ChatController에서 체인 구성

```java
String response = chatClient.prompt()
        .user(request.message())
        .tools(orderService)
        .advisors(
            inputNormalizationAdvisor,   // order=1: 입력 정규화
            systemPromptAdvisor,         // order=2: 시스템 프롬프트 주입
            new SimpleLoggerAdvisor(),   // order=3: 로깅
            MessageChatMemoryAdvisor.builder(chatMemory)
                    .conversationId(sessionId)
                    .build()             // order=4: 메모리 (LLM과 가장 근접)
        )
        .call()
        .content();
```

---

## 핵심 원칙 요약

| 원칙 | 설명 |
|------|------|
| `getOrder()` 낮을수록 먼저 실행 | 전처리 Advisor는 낮은 값, 메모리 Advisor는 높은 값 |
| `request.mutate()` 패턴 | `AdvisedRequest`는 불변 — 반드시 새 인스턴스 생성 |
| `chain.nextAroundCall()` 호출 필수 | 호출하지 않으면 LLM 요청 자체가 차단됨 |
| 응답 후처리는 `nextAroundCall` 이후 | LLM 응답이 반환된 뒤 `AdvisedResponse`를 가공 |

---

## 참고

- [Spring AI Advisors 공식 문서](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_advisors)
- Spring AI `CallAroundAdvisor` 인터페이스: `org.springframework.ai.chat.client.advisor.api`
