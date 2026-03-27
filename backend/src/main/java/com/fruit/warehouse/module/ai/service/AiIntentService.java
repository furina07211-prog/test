package com.fruit.warehouse.module.ai.service;

import com.fruit.warehouse.module.ai.dto.AiIntentResult;

import java.util.Optional;

/**
 * AI智能助手 模块服务接口。
 */
public interface AiIntentService {
    Optional<AiIntentResult> detect(String message);
}
