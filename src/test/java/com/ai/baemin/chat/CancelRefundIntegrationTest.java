package com.ai.baemin.chat;

import com.ai.baemin.common.advisor.GuardrailConfig;
import com.ai.baemin.common.advisor.InputGuardrailAdvisor;
import com.ai.baemin.common.advisor.InputNormalizationAdvisor;
import com.ai.baemin.common.advisor.OutputGuardrailAdvisor;
import com.ai.baemin.common.advisor.SystemPromptAdvisor;
import com.ai.baemin.order.InMemoryOrderRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({OrderService.class, InMemoryOrderRepository.class, InputGuardrailAdvisor.class, OutputGuardrailAdvisor.class, GuardrailConfig.class, InputNormalizationAdvisor.class, SystemPromptAdvisor.class})
class CancelRefundIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatClient chatClient;

    @MockBean
    ChatMemory chatMemory;

    @MockBean
    VectorStore vectorStore;

    @Test
    void 취소_요청시_OrderService_Tool이_등록되어_응답을_반환한다() throws Exception {
        ChatClient.ChatClientRequestSpec promptSpec =
                mock(ChatClient.ChatClientRequestSpec.class, Mockito.RETURNS_SELF);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("ORDER-004 주문 취소를 원하시면 확인해주세요.");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"ORDER-004 취소해줘\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("ORDER-004 주문 취소를 원하시면 확인해주세요."));

        verify(promptSpec).tools(any(OrderService.class));
    }

    @Test
    void 환불_요청시_OrderService_Tool이_등록되어_응답을_반환한다() throws Exception {
        ChatClient.ChatClientRequestSpec promptSpec =
                mock(ChatClient.ChatClientRequestSpec.class, Mockito.RETURNS_SELF);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("ORDER-003 환불 요청을 확인해주세요.");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"ORDER-003 환불 요청해줘\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("ORDER-003 환불 요청을 확인해주세요."));

        verify(promptSpec).tools(any(OrderService.class));
    }
}
