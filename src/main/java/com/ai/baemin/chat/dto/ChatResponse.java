package com.ai.baemin.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 응답")
public record ChatResponse(
        @Schema(description = "AI 상담원 응답 메시지")
        String response
) {}
