package com.ai.baemin.common.advisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OutputGuardrailAdvisorTest {

    private OutputGuardrailAdvisor advisor;
    private AdvisorChain chain;

    @BeforeEach
    void setUp() {
        advisor = new OutputGuardrailAdvisor();
        chain = mock(AdvisorChain.class);
    }

    private ChatClientResponse responseWith(String content) {
        AssistantMessage msg = new AssistantMessage(content);
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(msg)));
        return ChatClientResponse.builder().chatResponse(chatResponse).context(Map.of()).build();
    }

    private String extractContent(ChatClientResponse response) {
        return response.chatResponse().getResult().getOutput().getText();
    }

    @Test
    void 전화번호를_마스킹한다() {
        ChatClientResponse response = responseWith("010-1234-5678로 연락하세요.");

        ChatClientResponse result = advisor.after(response, chain);

        assertThat(extractContent(result)).isEqualTo("010-****-****로 연락하세요.");
    }

    @Test
    void 주소_상세를_마스킹한다() {
        ChatClientResponse response = responseWith("역삼동 123-4로 배달됩니다.");

        ChatClientResponse result = advisor.after(response, chain);

        assertThat(extractContent(result)).doesNotContain("123-4");
    }

    @Test
    void 마스킹_대상이_없으면_응답을_그대로_반환한다() {
        ChatClientResponse response = responseWith("주문이 정상 처리되었습니다.");

        ChatClientResponse result = advisor.after(response, chain);

        assertThat(extractContent(result)).isEqualTo("주문이 정상 처리되었습니다.");
    }

    @Test
    void getOrder는_Integer_MAX_VALUE를_반환한다() {
        assertThat(advisor.getOrder()).isEqualTo(Integer.MAX_VALUE);
    }
}
