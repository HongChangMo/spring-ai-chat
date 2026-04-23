package com.ai.baemin.chat;

import com.ai.baemin.chat.dto.ChatRequest;
import com.ai.baemin.chat.dto.ChatResponse;
import com.ai.baemin.common.advisor.InputGuardrailAdvisor;
import com.ai.baemin.common.advisor.InputNormalizationAdvisor;
import com.ai.baemin.common.advisor.OutputGuardrailAdvisor;
import com.ai.baemin.common.advisor.SystemPromptAdvisor;
import com.ai.baemin.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = "고객 상담 채팅 API")
@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final OrderService orderService;
    private final ChatMemory chatMemory;
    private final InputGuardrailAdvisor inputGuardrailAdvisor;
    private final OutputGuardrailAdvisor outputGuardrailAdvisor;
    private final InputNormalizationAdvisor inputNormalizationAdvisor;
    private final SystemPromptAdvisor systemPromptAdvisor;
    private final VectorStore vectorStore;

    public ChatController(ChatClient chatClient, OrderService orderService, ChatMemory chatMemory,
                          InputGuardrailAdvisor inputGuardrailAdvisor,
                          OutputGuardrailAdvisor outputGuardrailAdvisor,
                          InputNormalizationAdvisor inputNormalizationAdvisor,
                          SystemPromptAdvisor systemPromptAdvisor,
                          VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.orderService = orderService;
        this.chatMemory = chatMemory;
        this.inputGuardrailAdvisor = inputGuardrailAdvisor;
        this.outputGuardrailAdvisor = outputGuardrailAdvisor;
        this.inputNormalizationAdvisor = inputNormalizationAdvisor;
        this.systemPromptAdvisor = systemPromptAdvisor;
        this.vectorStore = vectorStore;
    }

    @Operation(summary = "채팅 메시지 전송", description = "사용자 메시지를 AI 상담원에게 전송하고 응답을 반환합니다.")
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Parameter(description = "대화 세션 ID", required = true) @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody ChatRequest request) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("X-Session-Id 헤더가 필요합니다.");
        }

        if (request.message() != null && request.message().length() > 500) {
            throw new IllegalArgumentException("입력이 너무 깁니다. 최대 500자까지 입력할 수 있습니다.");
        }

        inputGuardrailAdvisor.validate(request.message());

        String response = chatClient.prompt()
                .user(request.message())
                .tools(orderService)
                .advisors(
                        inputNormalizationAdvisor,
                        systemPromptAdvisor,
                        new QuestionAnswerAdvisor(vectorStore),
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(sessionId)
                                .build(),
                        outputGuardrailAdvisor
                )
                .call()
                .content();

        return ResponseEntity.ok(new ChatResponse(response));
    }
}
