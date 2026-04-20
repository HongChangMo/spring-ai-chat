package com.example.baemin.chat;

import com.example.baemin.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import(OrderService.class)
class ToolCallingTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatClient chatClient;

    @Test
    void 주문상태_조회_요청시_OrderService_Tool이_등록되어_응답을_반환한다() throws Exception {
        ChatClient.ChatClientRequestSpec promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.user(anyString())).willReturn(promptSpec);
        given(promptSpec.tools(any(OrderService.class))).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("ORDER-001의 현재 상태는 조리 중입니다.");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"ORDER-001 주문 상태 알려줘\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("ORDER-001의 현재 상태는 조리 중입니다."));

        verify(promptSpec).tools(any(OrderService.class));
    }

    @Test
    void 배달위치_추적_요청시_OrderService_Tool이_등록되어_응답을_반환한다() throws Exception {
        ChatClient.ChatClientRequestSpec promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.user(anyString())).willReturn(promptSpec);
        given(promptSpec.tools(any(OrderService.class))).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("ORDER-002는 현재 서초구 방배동 근처에서 배달 중입니다.");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"ORDER-002 배달 위치 알려줘\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("ORDER-002는 현재 서초구 방배동 근처에서 배달 중입니다."));

        verify(promptSpec).tools(any(OrderService.class));
    }
}
