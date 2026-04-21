# Step 5 RAG 설계

## 개요

SimpleVectorStore + OpenAI EmbeddingModel을 사용해 FAQ/환불 정책 문서를 기반으로 RAG 파이프라인을 구성한다.

## 패키지 구조

```
src/main/resources/docs/
  faq.txt
  refund-policy.txt

src/main/java/com/ai/baemin/
  config/
    VectorStoreConfig.java   # SimpleVectorStore 빈 + ApplicationRunner
  chat/
    ChatController.java      # Advisor 체인에 QuestionAnswerAdvisor 추가
```

## 의존성

```groovy
implementation 'org.springframework.ai:spring-ai-starter-vector-store-simple'
```

## 데이터 흐름

```
앱 시작 → ApplicationRunner → docs/*.txt 로드
       → EmbeddingModel(OpenAI) → SimpleVectorStore 저장

요청 → InputNormalization(1) → SystemPrompt(2) → QuestionAnswerAdvisor(3)
     → (벡터 검색 → 컨텍스트 주입) → SimpleLogger → Memory → ChatClient
```

## Advisor 체인 순서

| 순서 | Advisor | 역할 |
|------|---------|------|
| 1 | InputNormalizationAdvisor | 입력 공백 정규화 |
| 2 | SystemPromptAdvisor | 상담원 역할 시스템 프롬프트 |
| 3 | QuestionAnswerAdvisor | RAG 컨텍스트 주입 |
| 4 | SimpleLoggerAdvisor | 요청/응답 로깅 |
| 5 | MessageChatMemoryAdvisor | 세션별 대화 히스토리 |

## 테스트 전략

- `RagDocumentLoaderTest`: 문서 로드 및 벡터 검색 단위 테스트
- `ChatControllerTest`: `VectorStore` MockBean 추가로 기존 테스트 통과 유지
- `RagAdvisorChainTest`: RAG 포함 체인 정상 응답 확인
