# Chatflow API 使用说明

## 📋 修改总结

已完成以下修改：

### ✅ 1. 清理了 Workflow 相关代码
- 从 `AIChatController` 中删除了 `workflowChatStream` 接口
- 从 `DifyStreamService` 接口中移除了 workflow 相关方法
- 从 `DifyStreamServiceImpl` 实现中删除了 workflow 相关实现
- 移除了 `WorkflowChatRequest` 的导入

### ✅ 2. 添加了 Chatflow 流式接口
- 新增 `callChatflowStream()` - 返回完整的流式消息对象
- 新增 `callChatflowStreamSimple()` - 仅返回文本内容
- 修复了 `ChatRequest.DifyFile` 内部类结构

### ✅ 3. 修复了编译错误
- 修复了 `AIChatServiceImpl` 中的文件类型不匹配问题
- 添加了 `@Builder.Default` 注解到 `responseMode` 字段

---

## 🚀 API 使用指南

### 1. Chatflow 完整流式对话

**接口地址**: `POST /api/ai/chatflow/stream`

**请求参数**:
```
query: 用户问题（必填，query parameter）
conversationId: 对话ID（可选，query parameter）
fileIds: Dify文件ID列表（可选，query parameter）
inputs: 输入变量（可选，request body JSON）
```

**示例请求**:
```bash
curl -X POST "http://localhost:8097/api/ai/chatflow/stream?query=这是我对你的流式回答测试&conversationId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**响应格式**: Server-Sent Events (SSE)
```
event: message
data: {"event":"message","conversationId":"xxx","messageId":"xxx","data":"回答内容...","timestamp":1234567890}

event: message_end
data: {"event":"message_end","conversationId":"xxx","messageId":"xxx","timestamp":1234567890}
```

---

### 2. Chatflow 简化流式对话（仅文本）

**接口地址**: `POST /api/ai/chatflow/stream/simple`

**请求参数**: 同上

**示例请求**:
```bash
curl -X POST "http://localhost:8097/api/ai/chatflow/stream/simple?query=你好" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**响应格式**: Server-Sent Events (SSE)，纯文本
```
data: 你好！

data: 我是AI助手，

data: 有什么可以帮助你的吗？
```

---

### 3. 文件上传 + 对话

#### 步骤1: 上传文件到 Dify
```bash
curl -X POST "http://localhost:8097/api/ai/files/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/file.pdf"
```

**响应**:
```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "fileId": "upload_file_xxxxx",
    "fileName": "file.pdf"
  }
}
```

#### 步骤2: 使用文件进行对话
```bash
curl -X POST "http://localhost:8097/api/ai/chatflow/stream?query=请总结这个文件的内容&fileIds=upload_file_xxxxx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

### 4. 从知识库获取文件后对话

#### 步骤1: 从知识库上传文件到 Dify
```bash
curl -X POST "http://localhost:8097/api/ai/files/upload/knowledge" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[3, 5, 7]'
```

**响应**:
```json
{
  "code": 200,
  "message": "成功上传 3 个文件",
  "data": ["upload_file_aaa", "upload_file_bbb", "upload_file_ccc"]
}
```

#### 步骤2: 使用这些文件进行对话
```bash
curl -X POST "http://localhost:8097/api/ai/chatflow/stream?query=请分析这些文档&fileIds=upload_file_aaa,upload_file_bbb,upload_file_ccc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## 🔧 Dify 配置说明

你的 Dify Chatflow 配置：

```yaml
dify:
  api-url: https://dify.aipfuture.com/v1
  chat-url: https://dify.aipfuture.com/chat/RtOy1uQ5x1tTWfV5
  api-key: app-Z0Uo6XAytf94sGkSO7oYMQV5
  timeout: 120000
  stream-enabled: true
```

**重要说明**:
- ✅ Chatflow 使用 `/chat-messages` 端点（已实现）
- ❌ Workflow 使用 `/workflows/run` 端点（已删除）

---

## 📝 前端集成示例

### JavaScript/TypeScript (使用 EventSource)

```javascript
// 简化版（纯文本）
const eventSource = new EventSource(
  '/api/ai/chatflow/stream/simple?query=' + encodeURIComponent('你好')
);

eventSource.onmessage = (event) => {
  console.log('收到文本:', event.data);
  // 更新UI显示文本
};

eventSource.onerror = (error) => {
  console.error('连接错误:', error);
  eventSource.close();
};
```

```javascript
// 完整版（包含事件类型）
const eventSource = new EventSource(
  '/api/ai/chatflow/stream?query=' + encodeURIComponent('你好')
);

eventSource.addEventListener('message', (event) => {
  const data = JSON.parse(event.data);
  console.log('事件类型:', data.event);
  console.log('消息内容:', data.data);
});

eventSource.addEventListener('message_end', (event) => {
  console.log('对话结束');
  eventSource.close();
});
```

---

## 🐛 问题排查

### 1. 400 Bad Request - "not_workflow_app"
**原因**: 使用了 workflow 接口调用 chatflow 应用  
**解决**: 使用 `/api/ai/chatflow/stream` 而不是 `/api/ai/workflow/chat/stream`

### 2. 403 Forbidden
**原因**: 缺少 JWT Token  
**解决**: 确保请求头包含有效的 `Authorization: Bearer <token>`

### 3. 流式响应中断
**原因**: 超时或网络问题  
**解决**: 检查 `dify.timeout` 配置，确保足够长（建议 120000ms）

---

## 🎯 关键变更点

| 旧接口 | 新接口 | 说明 |
|--------|--------|------|
| `/api/ai/workflow/chat/stream` | `/api/ai/chatflow/stream` | 使用 Chatflow |
| `WorkflowChatRequest` | `ChatRequest` | 请求对象 |
| `callWorkflowStream()` | `callChatflowStream()` | Service 方法 |

---

## ✅ 下一步

1. 重新编译并启动 `zhiyan-ai` 模块
2. 使用新的 `/api/ai/chatflow/stream` 接口进行测试
3. 确认流式响应正常工作
4. 更新前端代码以使用新的接口

---

**作者**: AI Assistant  
**日期**: 2025-10-29  
**版本**: v1.0

