package com.fruit.warehouse.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.module.ai.entity.AiChatLog;
import com.fruit.warehouse.module.ai.mapper.AiChatLogMapper;
import com.fruit.warehouse.module.ai.service.AiChatLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI智能助手 模块服务实现。
 */
@Service
public class AiChatLogServiceImpl extends ServiceImpl<AiChatLogMapper, AiChatLog> implements AiChatLogService {

    @Override
    public void log(Long userId,
                    String sessionId,
                    String messageType,
                    String content,
                    String intentCode,
                    String toolName,
                    String providerName,
                    String modelName,
                    boolean streamFlag) {
        if (userId == null || userId <= 0) {
            return;
        }
        AiChatLog log = new AiChatLog();
        log.setUserId(userId);
        log.setSessionId(StringUtils.hasText(sessionId) ? sessionId : "default-session");
        log.setMessageType(messageType);
        log.setContent(content == null ? "" : content);
        log.setIntentCode(intentCode);
        log.setToolName(toolName);
        log.setProviderName(providerName);
        log.setModelName(modelName);
        log.setTokenCount(0);
        log.setStreamFlag(streamFlag ? 1 : 0);
        this.save(log);
    }

    @Override
    public IPage<AiChatLog> pageBySession(Long userId, String sessionId, int pageNo, int pageSize) {
        if (userId == null || userId <= 0) {
            return new Page<>(Math.max(pageNo, 1), Math.max(pageSize, 1), 0);
        }
        LambdaQueryWrapper<AiChatLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatLog::getUserId, userId);
        wrapper.eq(StringUtils.hasText(sessionId), AiChatLog::getSessionId, sessionId);
        wrapper.orderByAsc(AiChatLog::getCreateTime);
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(pageSize, 1);
        return this.page(new Page<>(safePageNo, safePageSize), wrapper);
    }
}
