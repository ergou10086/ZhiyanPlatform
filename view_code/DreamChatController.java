package com.hbnu.dreamparseor.backend.controller;

import com.hbnu.dreamparseor.backend.model.dto.DreamChatDTO;
import com.hbnu.dreamparseor.backend.model.dto.DifyRequestDTO;
import com.hbnu.dreamparseor.backend.model.dto.DifyResponseDTO;
import com.hbnu.dreamparseor.backend.exception.DifyException;
import com.hbnu.dreamparseor.backend.service.DifyService;
import com.hbnu.dreamparseor.backend.config.DifyConfig;

import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify Ai 解梦的控制器，负责处理与梦境解析和AI聊天相关的HTTP请求。
 * 该控制器作为前端与Dify AI服务之间的桥梁，接收用户请求，调用相应的服务方法，并将处理结果以统一的JSON格式返回给客户端。
 * @author 树上的二狗
 */
@Slf4j
@RestController
@RequestMapping("/api/dream")
@CrossOrigin(origins = "*") // 根据需要调整跨域设置
public class DreamChatController {

    @Resource
    private DifyService difyService;
    
    @Resource
    private DifyConfig difyConfig;
    
    /**
     * 获取Dify AI公开访问URL
     * 
     * @return 包含公开访问URL的响应
     */
    @GetMapping("/chat/config")
    public ResponseEntity<Map<String, Object>> getChatConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("publicUrl", difyConfig.getFullChatUrl());
        response.put("apiKey", difyConfig.getApiKey());
        response.put("message", "获取聊天配置成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 通用聊天接口 - 允许用户与AI进行自由对话
     *
     * @param dreamChat 包含用户消息、用户ID和会话ID的DTO
     * @return 包含AI回复的JSON响应
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody DreamChatDTO dreamChat) {
        Map<String, Object> response = new HashMap<>();

        try{
            // 记录请求日志
            log.info("收到聊天请求，用户ID: {}", dreamChat.getUserId());

            // 参数验证，检查消息内容是否为空
            if (dreamChat.getDreamDescription() == null || dreamChat.getDreamDescription().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 参数验证：检查用户ID是否为空
            if (dreamChat.getUserId() == null || dreamChat.getUserId().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 构建请求DTO
            String query = dreamChat.getDreamDescription().trim();
            // 如果是解梦模式，自动添加解梦提示词
            if (dreamChat.getIsDreamMode() != null && dreamChat.getIsDreamMode()) {
                query = "请帮我解释这个梦境：" + query;
            }
            
            DifyRequestDTO request = DifyRequestDTO.builder()
                    .query(query)
                    .userId(dreamChat.getUserId().trim())
                    .conversationId(dreamChat.getConversationId())
                    .responseMode(true) // 默认不使用阻塞式响应模式
                    .build();
            
            // 调用服务层的发送消息方法，获取AI响应
            DifyResponseDTO difyResponse = difyService.sendMessage(request);
            
            // 检查difyResponse是否为null
            if (difyResponse == null) {
                log.error("从服务层获取的响应为null，用户ID: {}", dreamChat.getUserId());
                response.put("success", false);
                response.put("message", "聊天服务暂时不可用，请稍后再试");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

            // 构建成功响应
            response.put("success", true);
            response.put("answer", difyResponse.getAnswer());
            response.put("conversationId", difyResponse.getConversationId());
            response.put("messageId", difyResponse.getMessageId());
            response.put("message", "聊天成功");

            // 记录成功处理日志
            log.info("聊天请求处理成功，用户ID: {}", dreamChat.getUserId());
            return ResponseEntity.ok(response);
        } catch (DifyException e) {
            // 处理Dify服务异常，记录错误日志并返回适当的错误响应
            log.error("聊天请求处理失败", e);
            response.put("success", false);
            response.put("message", "聊天服务暂时不可用：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            // 处理未知异常，记录错误日志并返回通用错误响应
            log.error("聊天请求处理时发生未知错误", e);
            response.put("success", false);
            response.put("message", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 解梦接口 - 专门用于解析用户梦境的含义
     *
     * @param dreamChat 包含梦境描述、用户ID和会话ID的DTO
     * @return 包含解梦结果的JSON响应
     */
    @PostMapping("/interpret")
    public ResponseEntity<Map<String, Object>> interpretDream(@RequestBody DreamChatDTO dreamChat) {
        Map<String, Object> response = new HashMap<>();

        try{
            // 记录请求日志，包含用户ID信息
            log.info("收到解梦请求，用户ID: {}", dreamChat.getUserId());

            // 参数验证，检查梦境描述是否为空
            if (dreamChat.getDreamDescription() == null || dreamChat.getDreamDescription().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "梦境描述不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 参数验证：检查用户ID是否为空
            if (dreamChat.getUserId() == null || dreamChat.getUserId().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置为解梦模式
            dreamChat.setIsDreamMode(true);
            
            // 构建请求DTO
            String query = "请帮我解释这个梦境：" + dreamChat.getDreamDescription().trim();
            DifyRequestDTO request = DifyRequestDTO.builder()
                    .query(query)
                    .userId(dreamChat.getUserId().trim())
                    .conversationId(dreamChat.getConversationId())
                    .responseMode(true)
                    .build();
            
            // 调用服务层的发送消息方法，获取AI响应
            DifyResponseDTO difyResponse = difyService.sendMessage(request);

            // 构建成功响应
            response.put("success", true);
            response.put("interpretation", difyResponse.getAnswer());
            response.put("conversationId", difyResponse.getConversationId());
            response.put("messageId", difyResponse.getMessageId());
            response.put("message", "解梦成功");

            // 记录成功处理日志
            log.info("解梦请求处理成功，用户ID: {}", dreamChat.getUserId());
            return ResponseEntity.ok(response);
        } catch (DifyException e) {
            // 处理Dify服务异常，记录错误日志并返回适当的错误响应
            log.error("解梦请求处理失败", e);
            response.put("success", false);
            response.put("message", "解梦服务暂时不可用：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            // 处理未知异常，记录错误日志并返回通用错误响应
            log.error("解梦请求处理时发生未知错误", e);
            response.put("success", false);
            response.put("message", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Ai 服务状态检查端口
     * 检查Dify AI服务是否可用
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        boolean isAvailable = difyService.isServiceAvailable();
        
        response.put("status", isAvailable ? "OK" : "ERROR");
        response.put("service", "Dream Chat Service");
        response.put("difyServiceAvailable", isAvailable);
        response.put("timestamp", System.currentTimeMillis());
        
        if (isAvailable) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
