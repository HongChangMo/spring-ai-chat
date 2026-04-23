package com.ai.baemin.common.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.stereotype.Component;

@Component
public class SystemPromptAdvisor implements BaseAdvisor {

    private static final String SYSTEM_PROMPT = """
            당신은 배달의민족 고객 상담원입니다.
            주문, 배달, 취소, 환불 관련 질문에만 답변하세요.
            항상 친절하고 간결하게 한국어로 답변하세요.
            """;

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        return request.mutate()
                .prompt(request.prompt().augmentSystemMessage(SYSTEM_PROMPT))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.SYSTEM_PROMPT.value();
    }
}
