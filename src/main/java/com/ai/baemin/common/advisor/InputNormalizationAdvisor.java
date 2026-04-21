package com.ai.baemin.common.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.stereotype.Component;

@Component
public class InputNormalizationAdvisor implements BaseAdvisor {

    private static final int MAX_INPUT_LENGTH = 500;

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String userText = request.prompt().getUserMessage().getText();
        String normalized = userText.strip().replaceAll("\\s+", " ");

        return request.mutate()
                .prompt(request.prompt().augmentUserMessage(normalized))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
