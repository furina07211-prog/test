package com.fruit.warehouse.module.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmResponse;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchResponse;
import com.fruit.warehouse.module.ai.dto.AiAssistantHistoryItem;

public interface AiAssistantService {
    AiAssistantDispatchResponse dispatch(AiAssistantDispatchRequest request);

    AiAssistantConfirmResponse confirm(AiAssistantConfirmRequest request);

    IPage<AiAssistantHistoryItem> history(String sessionId, Integer pageNo, Integer pageSize);
}

