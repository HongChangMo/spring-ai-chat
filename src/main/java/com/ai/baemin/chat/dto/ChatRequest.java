package com.ai.baemin.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 요청")
public record ChatRequest(
        @Schema(description = "사용자 메시지 (최대 500자)", example = "ORDER-001 주문 상태 알려줘")
        String message
) {}
