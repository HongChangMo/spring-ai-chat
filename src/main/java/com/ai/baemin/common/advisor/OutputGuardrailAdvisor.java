package com.ai.baemin.common.advisor;

import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OutputGuardrailAdvisor implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        String original = response.chatResponse().getResult().getOutput().getText();
        String masked = mask(original);

        if (masked.equals(original)) {
            return response;
        }

        AssistantMessage maskedMsg = new AssistantMessage(masked);
        ChatResponse maskedChatResponse = new ChatResponse(List.of(new Generation(maskedMsg)));
        return ChatClientResponse.builder()
                .chatResponse(maskedChatResponse)
                .context(response.context())
                .build();
    }

    private String mask(String text) {
        // 전화번호 마스킹: 010-1234-5678 → 010-****-****
        text = text.replaceAll("(\\d{3})-(\\d{3,4})-(\\d{4})", "$1-****-****");
        // 주소 상세 마스킹: 동 + 번지수 → 동 ***
        text = text.replaceAll("(\\S+동)\\s+\\d+[-\\d]*", "$1 ***");
        return text;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
