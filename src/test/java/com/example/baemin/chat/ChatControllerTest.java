package com.example.baemin.chat;

import com.example.baemin.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatClient chatClient;

    @MockBean
    OrderService orderService;

    @Test
    void POST_chat_메시지를_받아_응답을_반환한다() throws Exception {
        ChatClient.ChatClientRequestSpec promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(promptSpec);
        given(promptSpec.user(anyString())).willReturn(promptSpec);
        given(promptSpec.tools(any(OrderService.class))).willReturn(promptSpec);
        given(promptSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("안녕하세요! 배민 고객 상담입니다.");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"안녕하세요\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("안녕하세요! 배민 고객 상담입니다."));
    }
}
