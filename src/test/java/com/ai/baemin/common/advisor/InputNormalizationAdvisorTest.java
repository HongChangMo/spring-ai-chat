package com.ai.baemin.common.advisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InputNormalizationAdvisorTest {

    private InputNormalizationAdvisor advisor;
    private AdvisorChain chain;

    @BeforeEach
    void setUp() {
        advisor = new InputNormalizationAdvisor();
        chain = mock(AdvisorChain.class);
    }

    @Test
    void 앞뒤_공백을_제거한다() {
        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("  안녕하세요  "))
                .build();

        ChatClientRequest result = advisor.before(request, chain);

        assertThat(result.prompt().getUserMessage().getText()).isEqualTo("안녕하세요");
    }

    @Test
    void 연속_공백을_단일_공백으로_줄인다() {
        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("주문   상태   알려줘"))
                .build();

        ChatClientRequest result = advisor.before(request, chain);

        assertThat(result.prompt().getUserMessage().getText()).isEqualTo("주문 상태 알려줘");
    }

    @Test
    void after는_응답을_그대로_반환한다() {
        ChatClientResponse response = mock(ChatClientResponse.class);

        ChatClientResponse result = advisor.after(response, chain);

        assertThat(result).isSameAs(response);
    }
}
