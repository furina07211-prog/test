package com.fruit.warehouse.module.ai.service;

import com.fruit.warehouse.module.ai.dto.AiChatRequest;
import com.fruit.warehouse.module.ai.dto.AiChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiService {
    AiChatResponse chat(AiChatRequest request);

    void chatStream(AiChatRequest request, SseEmitter emitter);
}
