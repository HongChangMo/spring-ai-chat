package com.example.baemin.controller;

import com.example.baemin.controller.dto.ChatRequest;
import com.example.baemin.controller.dto.ChatResponse;
import com.example.baemin.service.OrderService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final OrderService orderService;

    public ChatController(ChatClient chatClient, OrderService orderService) {
        this.chatClient = chatClient;
        this.orderService = orderService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String response = chatClient.prompt()
                .user(request.message())
                .tools(orderService)
                .call()
                .content();
        return new ChatResponse(response);
    }
}
