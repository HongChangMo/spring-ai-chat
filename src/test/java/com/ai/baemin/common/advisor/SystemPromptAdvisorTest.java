package com.ai.baemin.common.advisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SystemPromptAdvisorTest {

    private SystemPromptAdvisor advisor;
    private AdvisorChain chain;

    @BeforeEach
    void setUp() {
        advisor = new SystemPromptAdvisor();
        chain = mock(AdvisorChain.class);
    }

    @Test
    void before_호출_시_시스템_프롬프트가_주입된다() {
        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("주문 상태 알려줘"))
                .build();

        ChatClientRequest result = advisor.before(request, chain);

        String systemText = result.prompt().getSystemMessage().getText();
        assertThat(systemText).isNotBlank();
        assertThat(systemText).contains("배달의민족");
    }

    @Test
    void after는_응답을_그대로_반환한다() {
        ChatClientResponse response = mock(ChatClientResponse.class);

        ChatClientResponse result = advisor.after(response, chain);

        assertThat(result).isSameAs(response);
    }

    @Test
    void order는_2이다() {
        assertThat(advisor.getOrder()).isEqualTo(2);
    }
}
