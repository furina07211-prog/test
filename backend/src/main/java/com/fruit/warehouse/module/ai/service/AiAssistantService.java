package com.fruit.warehouse.module.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmResponse;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchResponse;
import com.fruit.warehouse.module.ai.dto.AiAssistantHistoryItem;

/**
 * AI智能助手 模块服务接口。
 */
public interface AiAssistantService {
    /**
     * 智能助手分发：识别意图并返回查询结果或待确认预览。
     */
    AiAssistantDispatchResponse dispatch(AiAssistantDispatchRequest request);

    /**
     * 智能助手确认：执行或取消待确认写操作。
     */
    AiAssistantConfirmResponse confirm(AiAssistantConfirmRequest request);

    /**
     * 按会话分页查询历史消息。
     */
    IPage<AiAssistantHistoryItem> history(String sessionId, Integer pageNo, Integer pageSize);
}

