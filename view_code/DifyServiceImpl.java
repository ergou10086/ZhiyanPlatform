package com.hbnu.dreamparseor.backend.service.impl;

import com.hbnu.dreamparseor.backend.config.DifyConfig;
import com.hbnu.dreamparseor.backend.model.dto.DifyRequestDTO;
import com.hbnu.dreamparseor.backend.model.dto.DifyResponseDTO;
import com.hbnu.dreamparseor.backend.exception.DifyException;
import com.hbnu.dreamparseor.backend.service.DifyService;

import jakarta.annotation.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * DifyServiceImpl 是 Dify Ai 服务的实现类，负责与 Dify API 进行通信
 * 该类提供了发送消息和解梦的功能，通过封装Dify API的调用细节，为上层服务提供统一的接口。
 * @author 树上的二狗
 */
@Slf4j
@Service
public class DifyServiceImpl implements DifyService {

    @Resource
    private DifyConfig difyConfig;

    @Resource
    private RestTemplate restTemplate;


    /**
     * 向 Dify API发送聊天消息并获取响应
     *
     * @param difyRequest 包含聊天请求信息的DTO
     * @return Dify API返回的响应DTO
     * @throws DifyException 当API调用失败或处理响应时发生错误
     */
    @Override
    public DifyResponseDTO sendMessage(DifyRequestDTO difyRequest) throws DifyException {
        try {
            // 构建请求头，设置内容类型为JSON并添加Dify API认证信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(difyConfig.getApiKey());

            // 构建请求体，将DifyRequestDTO中的信息转换为API所需的参数格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", difyRequest.getQuery());

            // Dify的Ai不是Agent，不支持阻塞模式，始终使用流式模式，日后根据模型再改吧
            requestBody.put("response_mode", "streaming");
            requestBody.put("user", difyRequest.getUserId());

            // 如果存在会话ID，则添加到请求中以保持对话上下文
            if (difyRequest.getConversationId() != null && !difyRequest.getConversationId().isEmpty()) {
                requestBody.put("conversation_id", difyRequest.getConversationId());
            }

            // 添加inputs参数，这是Dify API要求的，不填报错
            if (difyRequest.getInputs() != null) {
                requestBody.put("inputs", difyRequest.getInputs());
            } else {
                // 如果没有输入参数，仍然发送一个空的inputs对象
                requestBody.put("inputs", new HashMap<String, String>());
            }

            // 创建包含请求头和请求体的 HTTP 实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 构建 API 请求URL 并发送 POST 请求
            String url = difyConfig.getBaseUrl() + "/chat-messages";
            // 日志显示，看 Dify 有没有运行
            log.info("发送请求到Dify: {}", url);
            log.debug("请求体: {}", requestBody);
            log.debug("请求头: {}", headers);

            // 对于流式响应，需要先获取原始数据，然后手动解析
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            // 检查响应状态码是否有效，这里日志都带着
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Dify响应成功");
                
                // 从流式响应中提取数据
                String responseBody = response.getBody();
                log.info("原始响应体: {}", responseBody);
                
                // 创建响应DTO
                DifyResponseDTO difyResponseDTO = new DifyResponseDTO();
                
                try {
                    // 处理SSE格式的流式响应
                    // 流式响应格式为: data: {JSON数据}\n\n
                    if (Objects.nonNull(responseBody) && !responseBody.isEmpty()) {
                        // 分割每个数据块
                        String[] events = responseBody.split("\\n\\n");    // 注意转义
                        StringBuilder fullAnswer = new StringBuilder();
                        String conversationId = null;
                        String messageId = null;
                        
                        // 先遍历所有数据块，找到message_end事件，它包含完整的元数据
                        for (String event : events) {
                            if (event.startsWith("data: ")) {
                                // 提取JSON数据部分
                                String jsonData = event.substring(6).trim();
                                
                                // 使用Jackson解析JSON
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = objectMapper.readTree(jsonData);

                                // 提取事件类型
                                if (rootNode.has("event") && "message_end".equals(rootNode.get("event").asText())) {
                                    // 这是最后一个数据块，包含完整的元数据
                                    if (rootNode.has("conversation_id")) {
                                        conversationId = rootNode.get("conversation_id").asText();
                                    }
                                    if (rootNode.has("message_id")) {
                                        messageId = rootNode.get("message_id").asText();
                                    }
                                    break;
                                }
                                
                                // 如果是agent_message事件，收集回答内容
                                if (rootNode.has("event") && "agent_message".equals(rootNode.get("event").asText())) {
                                    if (rootNode.has("answer")) {
                                        fullAnswer.append(rootNode.get("answer").asText());
                                    }
                                    
                                    // 同时记录会话和消息的ID
                                    if (conversationId == null && rootNode.has("conversation_id")) {
                                        conversationId = rootNode.get("conversation_id").asText();
                                    }
                                    if (messageId == null && rootNode.has("message_id")) {
                                        messageId = rootNode.get("message_id").asText();
                                    }
                                }
                            }
                        }
                        
                        // 设置最终结果
                        difyResponseDTO.setAnswer(fullAnswer.toString());
                        difyResponseDTO.setConversationId(conversationId);
                        difyResponseDTO.setMessageId(messageId);
                        
                        log.info("解析完成，回答: {}, 会话ID: {}, 消息ID: {}", 
                                difyResponseDTO.getAnswer(), 
                                difyResponseDTO.getConversationId(), 
                                difyResponseDTO.getMessageId());
                    }
                } catch (Exception e) {
                    log.error("解析流式响应时出错", e);
                    // 如果解析失败，尝试使用简单的字符串处理
                    if (Objects.requireNonNull(responseBody).contains("\"answer\":")) {
                        int answerStart = responseBody.indexOf("\"answer\":") + 10;
                        int answerEnd = responseBody.indexOf("\"", answerStart);
                        if (answerEnd > answerStart) {
                            difyResponseDTO.setAnswer(responseBody.substring(answerStart, answerEnd));
                        }
                    }
                    
                    if (responseBody.contains("\"conversation_id\":")) {
                        int idStart = responseBody.indexOf("\"conversation_id\":") + 18;
                        int idEnd = responseBody.indexOf("\"", idStart);
                        if (idEnd > idStart) {
                            difyResponseDTO.setConversationId(responseBody.substring(idStart, idEnd));
                        }
                    }
                    
                    if (responseBody.contains("\"message_id\":")) {
                        int idStart = responseBody.indexOf("\"message_id\":") + 13;
                        int idEnd = responseBody.indexOf("\"", idStart);
                        if (idEnd > idStart) {
                            difyResponseDTO.setMessageId(responseBody.substring(idStart, idEnd));
                        }
                    }
                }
                
                return difyResponseDTO;
            } else {
                throw new DifyException("Dify API返回空响应");
            }
        } catch (HttpClientErrorException e) {
            // 处理客户端错误（4xx状态码）
            log.error("Dify API客户端错误: {}", e.getMessage());
            throw new DifyException(e.getStatusCode().value(), "API调用失败: " + e.getResponseBodyAsString());
        }catch (HttpServerErrorException e) {
            // 处理服务器错误（5xx状态码）
            log.error("Dify API服务器错误: {}", e.getMessage());
            throw new DifyException(e.getStatusCode().value(), "服务器内部错误: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // 处理其他未知异常
            log.error("调用Dify API时发生未知错误", e);
            throw new DifyException("调用AI服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查Dify AI服务是否可用
     * 通过发送一个简单的请求来检测服务状态
     *
     * @return 如果服务可用返回true，否则返回false
     */
    @Override
    public boolean isServiceAvailable() {
        try {
            // 构建请求头，设置内容类型为JSON并添加Dify API认证信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(difyConfig.getApiKey());
            
            // 构建一个简单的请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", "Hello");
            requestBody.put("response_mode", "streaming"); // 使用流式模式，因为Dify不支持阻塞模式
            requestBody.put("user", "health_check_user");

            // 添加inputs参数，这是Dify API要求的
            Map<String, String> inputs = new HashMap<>();
            requestBody.put("inputs", inputs);

            // 创建包含请求头和请求体的 HTTP 实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 构建健康检查URL
            String url = difyConfig.getBaseUrl() + "/chat-messages";
            
            // 对于流式响应，我们使用String类型接收
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            
            // 检查响应状态码
            if (!response.getStatusCode().is2xxSuccessful()) {
                return false;
            }
            response.getBody();
            return true;
            
        } catch (Exception e) {
            log.error("检查Dify服务可用性时发生错误", e);
            return false;
        }
    }
}




