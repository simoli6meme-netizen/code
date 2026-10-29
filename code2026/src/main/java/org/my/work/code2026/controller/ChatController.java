package org.my.work.code2026.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestParam("prompt") String prompt,
                                              @RequestParam(value = "chatId", required = false) String chatId) {
        String finalChatId = (chatId != null && !chatId.isBlank())
                ? chatId
                : "default-session";

        log.info("AI 对话请求 chatId={}, prompt={}", finalChatId, prompt);

        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalChatId))
                .stream()
                .content()
                // 每个文本片段包装成默认事件（前端 onmessage 能收到）
                .map(chunk -> ServerSentEvent.<String>builder(chunk).build())
                // 流结束时发一个 event: complete，前端据此关闭连接
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("complete")
                                .build()
                ))
                .onErrorResume(e -> {
                    log.error("AI 调用失败 chatId={}", finalChatId, e);
                    return Flux.just(
                            ServerSentEvent.<String>builder("AI 服务暂时不可用，请稍后再试").build(),
                            ServerSentEvent.<String>builder().event("complete").build()
                    );
                });
    }
}