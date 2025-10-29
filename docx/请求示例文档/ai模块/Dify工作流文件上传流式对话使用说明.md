# Dify 工作流文件上传流式对话使用说明

## 目录
1. [功能概述](#功能概述)
2. [API 接口说明](#api-接口说明)
3. [测试步骤](#测试步骤)
4. [Postman/Apifox 测试示例](#postmanapifox-测试示例)
5. [前端调用示例](#前端调用示例)
6. [常见问题](#常见问题)

---

## 功能概述

`zhiyan-ai` 模块提供了以下核心功能：

### 1. 文件上传功能
- 上传单个文件到 Dify
- 批量上传文件到 Dify
- 从知识库获取文件并上传到 Dify

### 2. 工作流对话功能
- 流式对话（返回完整消息对象，包含事件类型、节点信息等）
- 简化流式对话（仅返回文本内容）
- 一步式：上传文件 + 工作流对话

### 3. 集成功能
- 与知识库模块集成，获取文件信息
- SSE 流式响应
- 支持对话上下文维持

---

## API 接口说明

### 基础路径
```
http://localhost:{port}/api/ai
```

### 1. 健康检查
**接口**: `GET /health`

**响应示例**:
```json
"AI Service is running"
```

---

### 2. 上传单个文件到 Dify

**接口**: `POST /files/upload`

**请求参数**:
- `file`: MultipartFile（文件）

**请求头**:
```
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "fileId": "file-abc123",
    "fileName": "测试文档.pdf",
    "fileSize": 1024000,
    "extension": "pdf",
    "mimeType": "application/pdf",
    "createdAt": 1698765432000,
    "status": "success"
  }
}
```

---

### 3. 批量上传文件
**接口**: `POST /files/upload/batch`

**请求参数**:
- `files`: List<MultipartFile>（多个文件）

**响应示例**:
```json
{
  "code": 200,
  "message": "成功上传 3 个文件",
  "data": [
    {
      "fileId": "file-abc123",
      "fileName": "文档1.pdf",
      ...
    },
    {
      "fileId": "file-def456",
      "fileName": "文档2.docx",
      ...
    }
  ]
}
```

---

### 4. 从知识库上传文件

**接口**: `POST /files/upload/knowledge`

**请求体** (JSON):
```json
[1, 2, 3]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "成功上传 3 个文件",
  "data": [
    "file-abc123",
    "file-def456",
    "file-ghi789"
  ]
}
```

---

### 5. 工作流对话（完整流式响应）
**接口**: `POST /workflow/chat/stream`

**请求参数**:
- `query`: 用户问题（必填）
- `conversationId`: 对话 ID（可选，用于维持上下文）
- `fileIds`: Dify 文件 ID 列表（可选）
- `inputs`: 工作流输入变量（可选，JSON 对象）

**请求示例**:
```
POST /api/ai/workflow/chat/stream?query=请分析这个文档&conversationId=conv-123&fileIds=file-abc123,file-def456

Body (JSON, 可选):
{
  "custom_var1": "value1",
  "custom_var2": "value2"
}
```

**响应格式** (SSE):
```
event: workflow_started
data: {"event":"workflow_started","workflowRunId":"run-123",...}

event: node_started
data: {"event":"node_started","nodeId":"node-1","nodeType":"llm",...}

event: message
data: {"event":"message","data":"这是AI的回答...",...}

event: workflow_finished
data: {"event":"workflow_finished","data":"complete",...}
```

---

### 6. 工作流对话（简化流式响应）
**接口**: `POST /workflow/chat/stream/simple`

**请求参数**: 同上

**响应格式** (SSE, 仅文本):
```
data: 这是
data: AI
data: 的
data: 回答
```

---

### 7. 一步式：上传并对话
**接口**: `POST /workflow/chat/upload-and-stream`

**请求参数**:
- `files`: 文件列表（multipart）
- `query`: 用户问题
- `conversationId`: 对话 ID（可选）
- `inputs`: 工作流变量（可选）

**功能**: 先上传文件到 Dify，然后立即开始流式对话

---

## 测试步骤

### 准备工作

1. **启动依赖服务**
   - Nacos 配置中心
   - Redis
   - MySQL
   - MinIO
   - Dify 服务

2. **配置 Nacos**
   在 Nacos 中创建配置文件 `zhiyan-ai-prod.yaml`（或 dev）:
   ```yaml
   server:
     port: 8084  # AI 服务端口
   
   dify:
     api-url: http://your-dify-server:5001/v1  # Dify API 地址
     api-key: app-xxxxxxxxxxxx  # Dify API Key
     timeout: 60000
     stream-enabled: true
   ```

3. **启动 zhiyan-ai 服务**
   ```bash
   cd zhiyan-ai
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## Postman/Apifox 测试示例

### 测试 1: 上传文件

1. **创建请求**
   - 方法: POST
   - URL: `http://localhost:8084/api/ai/files/upload`
   - Headers:
     ```
     Authorization: Bearer {你的token}
     ```

2. **设置 Body**
   - 选择 `form-data`
   - 添加字段 `file`，类型选择 `File`，选择一个文件

3. **发送请求**
   - 查看响应，获取 `fileId`

### 测试 2: 工作流对话（SSE 流式）

1. **创建请求**
   - 方法: POST
   - URL: `http://localhost:8084/api/ai/workflow/chat/stream?query=请帮我分析一下&fileIds=file-abc123`
   - Headers:
     ```
     Authorization: Bearer {你的token}
     Accept: text/event-stream
     ```

2. **设置 Body (可选)**
   - 选择 `raw` > `JSON`
   ```json
   {
     "variable1": "value1"
   }
   ```

3. **发送请求**
   - Postman 会显示 SSE 流式响应
   - 注意：Postman 对 SSE 支持有限，建议用前端或 curl 测试

### 测试 3: 使用 curl 测试 SSE

```bash
# 工作流对话
curl -N -X POST \
  'http://localhost:8084/api/ai/workflow/chat/stream/simple?query=你好' \
  -H 'Authorization: Bearer your-token' \
  -H 'Accept: text/event-stream'

# 上传并对话
curl -N -X POST \
  'http://localhost:8084/api/ai/workflow/chat/upload-and-stream?query=分析这个文件' \
  -H 'Authorization: Bearer your-token' \
  -H 'Accept: text/event-stream' \
  -F 'files=@/path/to/file.pdf'
```

参数说明：
- `-N`: 禁用缓冲，实时显示流式输出
- `-X POST`: 指定 POST 方法
- `-F`: 上传文件

---

## 前端调用示例

### 使用 EventSource（仅 GET，不推荐用于此场景）

```javascript
// 注意：EventSource 只支持 GET，对于 POST 需要用 fetch
const eventSource = new EventSource(
  'http://localhost:8084/api/ai/workflow/chat/stream?query=你好'
);

eventSource.onmessage = (event) => {
  console.log('收到消息:', event.data);
};

eventSource.onerror = (error) => {
  console.error('连接错误:', error);
  eventSource.close();
};
```

### 使用 Fetch API（推荐）

```javascript
async function streamChat(query, fileIds = []) {
  const response = await fetch(
    `http://localhost:8084/api/ai/workflow/chat/stream/simple?query=${encodeURIComponent(query)}&fileIds=${fileIds.join(',')}`,
    {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + token,
        'Accept': 'text/event-stream',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        custom_var: 'value'
      })
    }
  );

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const chunk = decoder.decode(value);
    const lines = chunk.split('\n');

    for (const line of lines) {
      if (line.startsWith('data: ')) {
        const data = line.substring(6);
        console.log('收到数据:', data);
        // 更新 UI
      }
    }
  }
}

// 调用
streamChat('请分析这个文档', ['file-abc123']);
```

### React 示例（完整组件）

```jsx
import React, { useState } from 'react';

function AIChatComponent() {
  const [messages, setMessages] = useState([]);
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSend = async () => {
    if (!query.trim()) return;

    setIsLoading(true);
    setMessages([...messages, { role: 'user', content: query }]);

    try {
      const response = await fetch(
        `http://localhost:8084/api/ai/workflow/chat/stream/simple?query=${encodeURIComponent(query)}`,
        {
          method: 'POST',
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token'),
            'Accept': 'text/event-stream'
          }
        }
      );

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let aiResponse = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value);
        const lines = chunk.split('\n');

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.substring(6);
            aiResponse += data;
            // 实时更新 UI
            setMessages(prev => {
              const newMessages = [...prev];
              const lastMsg = newMessages[newMessages.length - 1];
              if (lastMsg?.role === 'assistant') {
                lastMsg.content = aiResponse;
              } else {
                newMessages.push({ role: 'assistant', content: aiResponse });
              }
              return newMessages;
            });
          }
        }
      }
    } catch (error) {
      console.error('对话失败:', error);
    } finally {
      setIsLoading(false);
      setQuery('');
    }
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg, idx) => (
          <div key={idx} className={`message ${msg.role}`}>
            {msg.content}
          </div>
        ))}
      </div>
      <div className="input-area">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSend()}
          disabled={isLoading}
          placeholder="输入问题..."
        />
        <button onClick={handleSend} disabled={isLoading}>
          {isLoading ? '发送中...' : '发送'}
        </button>
      </div>
    </div>
  );
}

export default AIChatComponent;
```

### Vue 3 示例

```vue
<template>
  <div class="ai-chat">
    <div class="messages">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        :class="['message', msg.role]"
      >
        {{ msg.content }}
      </div>
    </div>
    <div class="input-area">
      <input
        v-model="query"
        @keypress.enter="sendMessage"
        :disabled="isLoading"
        placeholder="输入问题..."
      />
      <button @click="sendMessage" :disabled="isLoading">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const messages = ref([]);
const query = ref('');
const isLoading = ref(false);

async function sendMessage() {
  if (!query.value.trim()) return;

  isLoading.value = true;
  messages.value.push({ role: 'user', content: query.value });

  try {
    const response = await fetch(
      `http://localhost:8084/api/ai/workflow/chat/stream/simple?query=${encodeURIComponent(query.value)}`,
      {
        method: 'POST',
        headers: {
          'Authorization': 'Bearer ' + localStorage.getItem('token'),
          'Accept': 'text/event-stream'
        }
      }
    );

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let aiResponse = '';
    let aiMessageIndex = messages.value.length;
    messages.value.push({ role: 'assistant', content: '' });

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value);
      const lines = chunk.split('\n');

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.substring(6);
          aiResponse += data;
          messages.value[aiMessageIndex].content = aiResponse;
        }
      }
    }
  } catch (error) {
    console.error('对话失败:', error);
  } finally {
    isLoading.value = false;
    query.value = '';
  }
}
</script>
```

---

## 常见问题

### 1. 启动失败：数据源配置错误
**解决**: 已在 `ZhiyanAiApplication` 中排除数据源自动配置

### 2. Bean 名称冲突
**解决**: 已将 `requestInterceptor` 重命名为 `difyRequestInterceptor`

### 3. SSE 连接超时
**原因**: Dify 响应时间过长
**解决**: 
- 增加 `dify.timeout` 配置
- 检查网络连接
- 检查 Dify 服务状态

### 4. 文件上传失败
**可能原因**:
- Dify API Key 错误
- 文件格式不支持
- 文件大小超限

**解决**:
- 检查 Nacos 配置中的 `dify.api-key`
- 查看 Dify 文档了解支持的格式
- 调整文件大小限制

### 5. 无法获取知识库文件
**原因**: 知识库服务未启动或 Feign 调用失败
**解决**:
- 确保 `zhiyan-knowledge` 服务已启动
- 检查 Nacos 服务注册
- 查看日志排查错误

### 6. 获取不到用户 ID
**原因**: Token 未传递或 Security 配置问题
**解决**:
- 检查请求头中的 `Authorization`
- 确保 Token 有效
- 检查 Security 配置

---

## 调试技巧

### 1. 查看日志
```bash
tail -f logs/zhiyan-ai.log
```

### 2. 开启调试模式
在配置文件中添加:
```yaml
logging:
  level:
    hbnu.project.zhiyanai: DEBUG
    org.springframework.web.reactive: DEBUG
```

### 3. 使用浏览器开发者工具
- Network 标签查看 SSE 连接
- Console 查看前端日志

### 4. Postman 测试 SSE
- 设置 Headers: `Accept: text/event-stream`
- 发送请求后观察响应

---

## 总结

本文档提供了完整的测试指南，包括：
- ✅ API 接口文档
- ✅ Postman/curl 测试示例
- ✅ 前端调用示例（Fetch、React、Vue）
- ✅ 常见问题解决方案

如有问题，请查看日志或联系开发团队。

