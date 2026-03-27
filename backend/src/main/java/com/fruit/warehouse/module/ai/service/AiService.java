package com.fruit.warehouse.module.ai.service;

import com.fruit.warehouse.module.ai.dto.AiChatRequest;
import com.fruit.warehouse.module.ai.dto.AiChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI智能助手 模块服务接口。
 */
public interface AiService {
    /**
     * 非流式 AI 对话。
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * 流式 AI 对话（SSE）。
     */
    void chatStream(AiChatRequest request, SseEmitter emitter);
}
