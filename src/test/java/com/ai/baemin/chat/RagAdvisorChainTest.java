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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({OrderService.class, InMemoryOrderRepository.class, InputGuardrailAdvisor.class, OutputGuardrailAdvisor.class, GuardrailConfig.class, InputNormalizationAdvisor.class, SystemPromptAdvisor.class})
class RagAdvisorChainTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatClient chatClient;

    @MockBean
    ChatMemory chatMemory;

    @MockBean
    VectorStore vectorStore;

    @Test
    void RAG_Advisor가_포함된_체인으로_정상_응답을_반환한다() throws Exception {
        ChatClient.ChatClientRequestSpec promptSpec =
                mock(ChatClient.ChatClientRequestSpec.class, Mockito.RETURNS_SELF);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("환불은 3~5 영업일 이내에 처리됩니다.");

        mockMvc.perform(post("/chat")
                        .header("X-Session-Id", "session-rag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"환불 처리 기간이 얼마나 걸리나요?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("환불은 3~5 영업일 이내에 처리됩니다."));
    }
}
