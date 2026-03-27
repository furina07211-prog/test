package com.fruit.warehouse.module.ai.service;

import com.fruit.warehouse.module.ai.dto.AiIntentResult;

public interface AiToolQueryService {
    String execute(AiIntentResult intent);
}
