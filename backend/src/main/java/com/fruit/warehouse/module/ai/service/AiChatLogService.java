package com.fruit.warehouse.module.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.ai.entity.AiChatLog;

public interface AiChatLogService extends IService<AiChatLog> {
    void log(Long userId,
             String sessionId,
             String messageType,
             String content,
             String intentCode,
             String toolName,
             String providerName,
             String modelName,
             boolean streamFlag);

    IPage<AiChatLog> pageBySession(Long userId, String sessionId, int pageNo, int pageSize);
}

