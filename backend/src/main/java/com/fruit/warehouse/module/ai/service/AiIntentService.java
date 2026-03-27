package com.fruit.warehouse.module.ai.service;

import com.fruit.warehouse.module.ai.dto.AiIntentResult;

import java.util.Optional;

public interface AiIntentService {
    Optional<AiIntentResult> detect(String message);
}
