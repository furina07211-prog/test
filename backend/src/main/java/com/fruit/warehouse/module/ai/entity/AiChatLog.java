package com.fruit.warehouse.module.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_log")
public class AiChatLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String sessionId;
    private String messageType;
    private String providerName;
    private String modelName;
    private String intentCode;
    private String toolName;
    private String content;
    private Integer tokenCount;
    private Integer streamFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

