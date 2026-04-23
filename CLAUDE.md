# 절대 하지 말아야 할 것들
- 내 허락 없이 파일 삭제하지 마
- 모르면 추측하지 말고 물어봐
- 작업 중간에 임의로 다른 방향으로 바꾸지 마

# project spec
- 도메인 중심 패키지 구조 (DDD-lite)
- 패키지는 레이어가 아닌 도메인(비즈니스 개념) 단위로 구성
  - `chat/`     : AI 채팅 (Controller, DTO)
  - `order/`    : 주문/배달 도메인 (@Tool 메서드, Mock 데이터, 도메인 모델)
  - `common/`   : 공통 기능 (Advisor, Guardrail 등 횡단 관심사)
  - `config/`   : Spring Bean 설정
- Spring AI 기능(Tool, Advisor, RAG 등)은 속하는 도메인 패키지 안에 위치

# harness
- 작업 시에 harness/feature_list.json 작업 목록 확인
- 작업이 끝나면 harness/feature_list.json 작업 내역 완료 표시
- github 관련 작업은 harness/github.md 확인