package com.fruit.warehouse.module.ai.service.impl;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingActionStore {

    private static final long EXPIRE_SECONDS = 10 * 60;
    private final ConcurrentHashMap<String, PendingAction> actions = new ConcurrentHashMap<>();

    public String put(Long userId, String sessionId, String intentCode, Map<String, Object> payload) {
        String actionId = "act-" + UUID.randomUUID();
        PendingAction action = PendingAction.builder()
            .actionId(actionId)
            .userId(userId)
            .sessionId(sessionId)
            .intentCode(intentCode)
            .payload(payload)
            .createTime(LocalDateTime.now())
            .build();
        actions.put(actionId, action);
        return actionId;
    }

    public PendingAction get(String actionId) {
        PendingAction action = actions.get(actionId);
        if (action == null) {
            return null;
        }
        if (isExpired(action)) {
            actions.remove(actionId);
            return null;
        }
        return action;
    }

    public PendingAction consume(String actionId) {
        PendingAction action = get(actionId);
        if (action == null) {
            return null;
        }
        actions.remove(actionId);
        return action;
    }

    public void remove(String actionId) {
        actions.remove(actionId);
    }

    private boolean isExpired(PendingAction action) {
        return action.getCreateTime() == null
            || action.getCreateTime().plusSeconds(EXPIRE_SECONDS).isBefore(LocalDateTime.now());
    }

    @Data
    @Builder
    public static class PendingAction {
        private String actionId;
        private Long userId;
        private String sessionId;
        private String intentCode;
        private Map<String, Object> payload;
        private LocalDateTime createTime;
    }
}

