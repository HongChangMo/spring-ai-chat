package com.ai.baemin.chat;

import com.ai.baemin.common.advisor.GuardrailConfig;
import com.ai.baemin.common.advisor.InputGuardrailAdvisor;
import com.ai.baemin.common.advisor.InputNormalizationAdvisor;
import com.ai.baemin.common.advisor.OutputGuardrailAdvisor;
import com.ai.baemin.common.advisor.SystemPromptAdvisor;
import com.ai.baemin.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({InputGuardrailAdvisor.class, OutputGuardrailAdvisor.class, GuardrailConfig.class, InputNormalizationAdvisor.class, SystemPromptAdvisor.class})
class AdvisorChainTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatClient chatClient;

    @MockBean
    OrderService orderService;

    @MockBean
    ChatMemory chatMemory;

    @MockBean
    VectorStore vectorStore;

    @Test
    void 입력이_500자를_초과하면_400을_반환한다() throws Exception {
        String longMessage = "가".repeat(501);

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"" + longMessage + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 정상_요청은_200을_반환한다() throws Exception {
        ChatClient.ChatClientRequestSpec promptSpec =
                mock(ChatClient.ChatClientRequestSpec.class, org.mockito.Mockito.RETURNS_SELF);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("도움이 필요하신가요?");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"주문 상태 알려줘\"}"))
                .andExpect(status().isOk());
    }
}
