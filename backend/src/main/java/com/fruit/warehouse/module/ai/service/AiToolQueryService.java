package com.fruit.warehouse.module.ai.service;

import com.fruit.warehouse.module.ai.dto.AiIntentResult;

/**
 * AI智能助手 模块服务接口。
 */
public interface AiToolQueryService {
    String execute(AiIntentResult intent);
}
