package com.ai.baemin.chat;

import com.ai.baemin.common.advisor.GuardrailConfig;
import com.ai.baemin.common.advisor.InputGuardrailAdvisor;
import com.ai.baemin.common.advisor.InputNormalizationAdvisor;
import com.ai.baemin.common.advisor.OutputGuardrailAdvisor;
import com.ai.baemin.common.advisor.SystemPromptAdvisor;
import com.ai.baemin.order.OrderRepository;
import com.ai.baemin.order.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({OrderService.class, OrderRepository.class, InputGuardrailAdvisor.class, OutputGuardrailAdvisor.class, GuardrailConfig.class, InputNormalizationAdvisor.class, SystemPromptAdvisor.class})
class ChatMemoryTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatClient chatClient;

    @MockBean
    ChatMemory chatMemory;

    @MockBean
    VectorStore vectorStore;

    private void stubChatClient(String responseContent) {
        ChatClient.ChatClientRequestSpec promptSpec =
                mock(ChatClient.ChatClientRequestSpec.class, Mockito.RETURNS_SELF);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn(responseContent);
    }

    @Test
    void 세션ID가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"주문 질문\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 세션ID가_있으면_정상_응답을_반환한다() throws Exception {
        stubChatClient("배민 상담 응답입니다.");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"주문 질문\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("배민 상담 응답입니다."));
    }

    @Test
    void 세션ID가_다른_두_요청은_각각_독립적으로_응답한다() throws Exception {
        stubChatClient("세션A 응답");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"주문 질문A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("세션A 응답"));

        stubChatClient("세션B 응답");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-B")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"배달 질문B\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("세션B 응답"));
    }

    @Test
    void 같은_세션ID로_연속_요청시_모두_정상_응답한다() throws Exception {
        stubChatClient("연속 응답");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"첫 번째 주문 질문\"}"))
                .andExpect(status().isOk());

        stubChatClient("연속 응답 2");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"두 번째 주문 질문\"}"))
                .andExpect(status().isOk());
    }
}
