package com.hbnu.dreamparseor.backend.model.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 用户与AI聊天的数据传输对象
 * 既可用于解梦场景，也可用于通用聊天场景
 * @author 树上的二狗
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DreamChatDTO {
    /**
     * 用户梦境描述或聊天内容
     */
    private String dreamDescription;

    /**
     * 对话ID（前端可选传入）
     * 用于维持对话上下文的连续性
     */
    private String conversationId;

    /**
     * 用户ID
     * 用于区分不同用户的对话
     */
    private String userId;
    
    /**
     * 是否是解梦场景
     * true: 解梦场景，会自动添加解梦提示词
     * false: 通用聊天场景，直接发送用户输入
     */
    private Boolean isDreamMode;
}
