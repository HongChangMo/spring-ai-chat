package com.ai.baemin.chat;

import com.ai.baemin.chat.dto.ChatRequest;
import com.ai.baemin.chat.dto.ChatResponse;
import com.ai.baemin.order.OrderService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final OrderService orderService;
    private final ChatMemory chatMemory;

    public ChatController(ChatClient chatClient, OrderService orderService, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.orderService = orderService;
        this.chatMemory = chatMemory;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody ChatRequest request) {

        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String response = chatClient.prompt()
                .user(request.message())
                .tools(orderService)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
                        .build())
                .call()
                .content();

        return ResponseEntity.ok(new ChatResponse(response));
    }
}
