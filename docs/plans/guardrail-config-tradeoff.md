# Guardrail 패턴 관리 방식 트레이드오프

## A) application.yml + @ConfigurationProperties

### 장점
- 코드 재컴파일 없이 패턴 추가/수정 가능
- 환경별(dev/prod) 패턴 분리 가능 (`application-prod.yml`)
- Spring Boot 표준 외부 설정 방식

### 단점
- 단위 테스트에서 Spring 컨텍스트 필요 (또는 수동 바인딩)
- `@ConfigurationProperties` + `@EnableConfigurationProperties` 보일러플레이트
- 패턴이 코드와 분리되어 있어 리팩터링 시 추적 어려움

---

## B) Java 설정 클래스 (GuardrailConfig)

### 장점
- 단위 테스트가 단순: `new InputGuardrailAdvisor(new GuardrailConfig())`
- 패턴이 코드와 같은 위치 → IDE 지원, 리팩터링 용이
- Spring 컨텍스트 없이 독립 실행 가능

### 단점
- 패턴 변경 시 재컴파일 필요
- 환경별 분기 시 코드 내 조건 분기 또는 Profile 필요
- 패턴이 많아지면 Java 파일이 길어짐

---

## 결론

| 기준 | A (yml) | B (Java) |
|------|---------|----------|
| 테스트 단순성 | 낮음 | 높음 |
| 운영 유연성 | 높음 | 낮음 |
| 코드 추적성 | 낮음 | 높음 |
| 보일러플레이트 | 있음 | 없음 |

**학습/프로토타입**: B 권장  
**운영 환경에서 동적 패턴 관리 필요**: A 권장
