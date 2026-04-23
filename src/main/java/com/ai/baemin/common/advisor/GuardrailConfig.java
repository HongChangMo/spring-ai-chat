package com.ai.baemin.common.advisor;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GuardrailConfig implements GuardrailProperties {

    @Override
    public List<String> injectionPatterns() {
        return List.of(
                "ignore previous instructions",
                "ignore previous",
                "system prompt",
                "너는 이제부터",
                "당신은 이제부터"
        );
    }

    @Override
    public List<String> allowedKeywords() {
        return List.of(
                "주문", "배달", "취소", "환불", "쿠폰", "리뷰", "결제", "음식", "배민", "상담"
        );
    }
}
