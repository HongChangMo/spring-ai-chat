package com.ai.baemin.common.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.stereotype.Component;

@Component
public class InputGuardrailAdvisor implements BaseAdvisor {

    private final GuardrailProperties config;

    public InputGuardrailAdvisor(GuardrailProperties config) {
        this.config = config;
    }

    public void validate(String message) {
        String input = message.toLowerCase();

        for (String pattern : config.injectionPatterns()) {
            if (input.contains(pattern.toLowerCase())) {
                throw new IllegalArgumentException("허용되지 않는 입력입니다.");
            }
        }

        boolean isRelevant = config.allowedKeywords().stream().anyMatch(input::contains);
        if (!isRelevant) {
            throw new IllegalArgumentException("배민 고객 상담 범위 외의 질문입니다.");
        }
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        validate(request.prompt().getUserMessage().getText());
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.INPUT_GUARDRAIL.value();
    }
}
