# Dify 工作流文件处理问题说明

## 问题描述

在使用 Chatflow 接口上传文件并对话时，虽然文件上传成功并且后端正确调用了 Dify API，但是 Dify 工作流执行失败，返回错误：

```
"error": "Unsupported variable type: <class 'NoneType'>"
```

## 问题分析

### 1. 错误事件流分析

从 Dify 返回的事件流中可以看到：

**文档提取器节点失败**：
```json
{
  "event": "node_finished",
  "data": {
    "node_id": "1762236875225",
    "node_type": "document-extractor",
    "title": "文档提取器",
    "status": "failed",
    "error": "Unsupported variable type: <class 'NoneType'>",
    "inputs": {"variable_selector": ["1711528914102", "files"]},
    "process_data": {"documents": [null]}
  }
}
```

**关键信息**：
- 文档提取器尝试从 `files` 变量读取文档
- 但是获取到的值是 `null`（`documents: [null]`）

### 2. 根本原因

从 `workflow_started` 事件可以看到工作流的输入变量：

```json
"inputs": {
  "files": null,                    // ← 问题：files 变量是 null
  "sys.files": [{                   // ← 文件信息在 sys.files 中
    "id": null,
    "tenant_id": "a5044051-0c45-4c62-865b-b4043fdf8c72",
    "type": "document",
    "transfer_method": "local_file",
    "related_id": "4823f9e3-7b00-4770-aa9e-536165c6de71",
    "filename": "Spring Cloud part19—RabbitMQ的安装和使用实践.md",
    "extension": ".md",
    "mime_type": "application/octet-stream",
    "size": 63015
  }],
  "sys.user_id": "1981328826835275776",
  "sys.query": "识别这个文件的内容"
}
```

**核心问题**：
- 在 Dify 的 **Chatflow（对话流）** 模式下，通过 `files` 字段上传的文件会被自动放入 **`sys.files`** 系统变量
- 但是你的 Dify 工作流中的"文档提取器"节点配置的是从 **`files`** 变量读取文档
- 这导致文档提取器读取到 `null`，从而执行失败

### 3. Chatflow vs Workflow 的区别

| 特性 | Chatflow（对话流） | Workflow（工作流） |
|------|-------------------|-------------------|
| 文件位置 | `sys.files`（系统变量） | `files`（用户变量） |
| 使用场景 | 多轮对话，自动管理上下文 | 单次执行，完全自定义 |
| 适用接口 | `/v1/chat-messages` | `/v1/workflows/run` |

## 解决方案

### 方案1：修改 Dify 工作流配置（✅ 推荐）

**步骤**：
1. 登录 Dify 控制台
2. 进入你的应用（`ebf42242-67a9-447f-98d1-96668790889b`）
3. 编辑工作流
4. 找到"文档提取器"节点（`1762236875225`）
5. 修改输入变量选择器：
   - **原配置**：`开始 > files`
   - **新配置**：`开始 > sys.files` ⭐
6. 保存并发布工作流

**优点**：
- ✅ 符合 Dify Chatflow 的设计规范
- ✅ 无需修改后端代码
- ✅ 支持多轮对话中的文件处理
- ✅ 可以访问文件的完整元数据（文件名、大小、MIME 类型等）

**修改示意图**：
```
┌─────────────────────────────────┐
│     开始节点 (Start)              │
│                                  │
│  变量列表：                       │
│  - files: null                   │
│  - sys.files: [文件对象]         │  ← 文件在这里
│  - sys.query: "识别这个文件的内容" │
│  - sys.user_id: "..."            │
└─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│    文档提取器节点                 │
│                                  │
│  输入配置：                       │
│  ❌ 旧：开始.files (null)        │
│  ✅ 新：开始.sys.files (有值)    │
└─────────────────────────────────┘
```

### 方案2：使用 Workflow 模式而不是 Chatflow（不推荐）

如果你的应用不需要多轮对话功能，可以考虑使用 Workflow 模式：

1. 在 Dify 中创建一个 Workflow 应用
2. 后端调用 `/v1/workflows/run` 接口
3. 在 Workflow 模式下，`files` 变量会正确接收文件信息

**缺点**：
- ❌ 不支持多轮对话
- ❌ 无法使用 `conversation_id` 维持上下文
- ❌ 需要修改后端代码以支持新的接口

### 方案3：同时支持两种方式（备用）

如果 Dify 工作流需要同时支持 Chatflow 和 Workflow 模式，可以配置两个文档提取器：

1. **文档提取器1**：从 `files` 读取（用于 Workflow 模式）
2. **文档提取器2**：从 `sys.files` 读取（用于 Chatflow 模式）
3. 使用条件分支判断哪个有值，选择相应的分支继续执行

## 后端代码说明

后端发送的请求体是**完全正确**的，符合 Dify 官方文档规范：

```json
{
  "query": "识别这个文件的内容",
  "user": "1981328826835275776",
  "inputs": {},
  "files": [{
    "type": "document",
    "transfer_method": "local_file",
    "upload_file_id": "4823f9e3-7b00-4770-aa9e-536165c6de71"
  }],
  "response_mode": "streaming"
}
```

这个问题**不是后端代码的问题**，而是 **Dify 工作流配置** 与 **Chatflow 模式** 不匹配导致的。

## 验证步骤

修改 Dify 工作流配置后，按以下步骤验证：

1. **上传文件**：
```bash
curl -X POST 'http://localhost:8097/api/ai/files/upload' \
  -H 'Authorization: Bearer {token}' \
  -F 'file=@test.md'
```

2. **携带文件对话**：
```bash
curl -X POST 'http://localhost:8097/api/ai/chatflow/stream?query=识别这个文件的内容&fileIds={返回的文件ID}' \
  -H 'Authorization: Bearer {token}'
```

3. **观察事件流**：
   - ✅ 应该看到 `node_finished` 事件中 `status: "succeeded"`
   - ✅ 应该看到 `workflow_finished` 事件中 `status: "succeeded"`
   - ✅ 应该收到 AI 的回复内容

## 相关文档

- [Dify 官方文档 - 文件上传](https://docs.dify.ai/guides/knowledge-base/upload-files)
- [Dify API 文档 - Chat Messages](https://docs.dify.ai/guides/application-publishing/api-reference/chat-messages)
- [Dify 系统变量说明](https://docs.dify.ai/guides/workflow/variables#system-variables)

## 总结

- ✅ DNS 解析超时问题已通过配置 WebClient 使用 JVM 默认 DNS 解析器解决
- ✅ 文件上传功能正常，后端代码正确
- ✅ Dify API 调用成功，可以正常建立 SSE 连接
- ⚠️ **需要修改 Dify 工作流配置**，将文档提取器的输入从 `files` 改为 `sys.files`
- 📝 这是 Dify Chatflow 模式的设计特性，不是 Bug

修改后即可正常使用文件对话功能！

