# Zhiyan Nacos 配置管理 API 测试指南

## 📋 目录
- [环境准备](#环境准备)
- [API 接口测试](#api-接口测试)
- [配置变更监听测试](#配置变更监听测试)
- [Swagger UI 使用](#swagger-ui-使用)

---

## 🚀 环境准备

### 1. 启动 Nacos 服务
确保你已经启动了 Nacos 服务器（默认端口：8848）

### 2. 启动 zhiyan-nacos 模块
```bash
cd zhiyan-nacos
mvn spring-boot:run
```

服务默认运行在端口：**8849**

### 3. 访问 Swagger UI
打开浏览器访问：
- **Knife4j 增强文档**：http://localhost:8849/doc.html
- **原生 Swagger UI**：http://localhost:8849/swagger-ui.html

---

## 🧪 API 接口测试

### 1️⃣ 配置列表获取测试

#### 接口信息

- **接口**：`GET /nacos/config/list`
- **说明**：获取所有配置列表

#### 使用 curl 测试
```bash
# 获取第1页，每页10条
curl -X GET "http://localhost:8849/nacos/config/list?pageNo=1&pageSize=10"

# 搜索特定配置
curl -X GET "http://localhost:8849/nacos/config/list?pageNo=1&pageSize=10&search=zhiyan-auth"
```

#### 使用 Postman 测试
1. 创建新请求：`GET http://localhost:8849/nacos/config/list`
2. 添加查询参数：
   - `pageNo`: 1
   - `pageSize`: 10
   - `search`: （可选）zhiyan-auth
3. 点击 Send

#### 预期响应
```json
{
  "code": 200,
  "msg": "获取配置列表成功",
  "data": [
    {
      "dataId": "zhiyan-auth-prod.yaml",
      "group": "DEFAULT_GROUP",
      "content": "server:\n  port: 8080",
      "md5": "abc123def456",
      "type": "yaml"
    }
  ],
  "success": true
}
```

---

### 2️⃣ 获取单个配置测试

#### 接口信息
- **接口**：`GET /nacos/config/{dataId}`
- **说明**：获取指定配置的内容

#### 使用 curl 测试
```bash
# 获取配置（使用默认分组）
curl -X GET "http://localhost:8849/nacos/config/zhiyan-auth-dev.yaml"

# 指定分组获取配置
curl -X GET "http://localhost:8849/nacos/config/zhiyan-auth-dev.yaml?group=DEFAULT_GROUP"
```

#### 预期响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "server:\n  port: 8080\nspring:\n  application:\n    name: zhiyan-auth",
  "success": true
}
```

---

### 3️⃣ 发布配置测试

#### 接口信息
- **接口**：`POST /nacos/config/{dataId}`
- **说明**：创建或更新配置

#### 使用 curl 测试
```bash
curl -X POST "http://localhost:8849/nacos/config/test-config.yaml" \
  -H "Content-Type: text/plain" \
  -d "server:
  port: 9090
spring:
  application:
    name: test-service"
```

#### 使用 Postman 测试
1. 创建新请求：`POST http://localhost:8849/nacos/config/test-config.yaml`
2. 在 Body 中选择 `raw` 和 `Text`
3. 输入配置内容：
```yaml
server:
  port: 9090
spring:
  application:
    name: test-service
```
4. 点击 Send

#### 预期响应
```json
{
  "code": 200,
  "msg": "发布成功",
  "data": true,
  "success": true
}
```

**✨ 注意**：发布配置后，会自动记录到历史记录中！

---

### 4️⃣ 删除配置测试

#### 接口信息
- **接口**：`DELETE /nacos/config/{dataId}`
- **说明**：删除指定配置

#### 使用 curl 测试
```bash
curl -X DELETE "http://localhost:8849/nacos/config/test-config.yaml"
```

#### 预期响应
```json
{
  "code": 200,
  "msg": "删除成功",
  "data": true,
  "success": true
}
```

---

### 5️⃣ 添加配置监听器测试

#### 接口信息
- **接口**：`POST /nacos/config/listener/{dataId}`
- **说明**：为配置添加变更监听器

#### 使用 curl 测试
```bash
# 为 test-config.yaml 添加监听器
curl -X POST "http://localhost:8849/nacos/config/listener/test-config.yaml?operator=admin"
```

#### 使用 Postman 测试
1. 创建新请求：`POST http://localhost:8849/nacos/config/listener/test-config.yaml`
2. 添加查询参数：
   - `group`: DEFAULT_GROUP（可选）
   - `operator`: admin
3. 点击 Send

#### 预期响应
```json
{
  "code": 200,
  "msg": "添加监听器成功",
  "data": true,
  "success": true
}
```

---

### 6️⃣ 获取配置历史测试

#### 6.1 获取指定配置的历史记录

**接口**：`GET /nacos/config/history/{dataId}`

```bash
# 获取 test-config.yaml 的历史记录
curl -X GET "http://localhost:8849/nacos/config/history/test-config.yaml?group=DEFAULT_GROUP"
```

**预期响应**：
```json
{
  "code": 200,
  "msg": "获取历史记录成功",
  "data": [
    {
      "id": 3,
      "dataId": "test-config.yaml",
      "group": "DEFAULT_GROUP",
      "content": "server:\n  port: 9090",
      "opType": "UPDATE",
      "operator": "admin",
      "createTime": "2025-10-31T10:30:00"
    },
    {
      "id": 1,
      "dataId": "test-config.yaml",
      "group": "DEFAULT_GROUP",
      "content": "server:\n  port: 8080",
      "opType": "CREATE/UPDATE",
      "operator": "admin",
      "createTime": "2025-10-31T10:00:00"
    }
  ],
  "success": true
}
```

#### 6.2 获取所有配置的历史记录

**接口**：`GET /nacos/config/history/all`

```bash
curl -X GET "http://localhost:8849/nacos/config/history/all"
```

#### 6.3 获取最近N条历史记录

**接口**：`GET /nacos/config/history/recent`

```bash
# 获取最近5条历史记录
curl -X GET "http://localhost:8849/nacos/config/history/recent?limit=5"
```

---

## 🎧 配置变更监听测试

### 完整测试流程

#### 步骤 1：发布一个初始配置
```bash
curl -X POST "http://localhost:8849/nacos/config/test-listener.yaml" \
  -H "Content-Type: text/plain" \
  -d "version: 1.0.0
message: Initial Config"
```

#### 步骤 2：为配置添加监听器
```bash
curl -X POST "http://localhost:8849/nacos/config/listener/test-listener.yaml?operator=张三"
```

#### 步骤 3：更新配置触发监听
```bash
curl -X POST "http://localhost:8849/nacos/config/test-listener.yaml" \
  -H "Content-Type: text/plain" \
  -d "version: 2.0.0
message: Updated Config
new-feature: enabled"
```

#### 步骤 4：查看控制台日志
你应该看到类似以下的日志输出：

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔔 配置变更通知
📝 DataId: test-listener.yaml
📁 Group: DEFAULT_GROUP
👤 Operator: 张三
⏰ Time: 2025-10-31T10:35:22
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ 配置变更历史已记录
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

#### 步骤 5：查看历史记录
```bash
curl -X GET "http://localhost:8849/nacos/config/history/test-listener.yaml"
```

**你应该看到两条记录**：
- 一条是初始发布的记录（CREATE/UPDATE）
- 一条是监听器捕获的更新记录（UPDATE）

---

## 🌐 Swagger UI 使用

### 访问 Knife4j 文档

1. 打开浏览器：http://localhost:8849/doc.html
2. 展开 **"Nacos配置管理"** 分组
3. 你会看到所有的 API 接口

### 使用 Swagger UI 测试接口

#### 示例：测试"获取配置列表"

1. 点击 `GET /nacos/config/list` 接口
2. 点击 **"调试"** 或 **"Try it out"**
3. 填写参数：
   - pageNo: 1
   - pageSize: 10
   - search: （留空或填写搜索关键字）
4. 点击 **"执行"**
5. 查看响应结果

---

## 🧪 完整测试场景

### 场景 1：配置管理完整流程

```bash
# 1. 查看配置列表
curl -X GET "http://localhost:8849/nacos/config/list?pageNo=1&pageSize=10"

# 2. 创建新配置
curl -X POST "http://localhost:8849/nacos/config/demo-app.yaml" \
  -H "Content-Type: text/plain" \
  -d "server:
  port: 8080
app:
  name: demo-app
  version: 1.0.0"

# 3. 添加监听器
curl -X POST "http://localhost:8849/nacos/config/listener/demo-app.yaml?operator=测试员"

# 4. 获取配置
curl -X GET "http://localhost:8849/nacos/config/demo-app.yaml"

# 5. 更新配置（触发监听器）
curl -X POST "http://localhost:8849/nacos/config/demo-app.yaml" \
  -H "Content-Type: text/plain" \
  -d "server:
  port: 8080
app:
  name: demo-app
  version: 2.0.0
  new-feature: enabled"

# 6. 查看配置历史
curl -X GET "http://localhost:8849/nacos/config/history/demo-app.yaml"

# 7. 查看最近的所有历史
curl -X GET "http://localhost:8849/nacos/config/history/recent?limit=10"

# 8. 删除配置
curl -X DELETE "http://localhost:8849/nacos/config/demo-app.yaml"
```

### 场景 2：服务管理测试

```bash
# 1. 获取服务列表
curl -X GET "http://localhost:8849/nacos/service/list?pageNo=1&pageSize=10"

# 2. 获取指定服务的实例
curl -X GET "http://localhost:8849/nacos/service/zhiyan-auth/instances"

# 3. 获取健康的实例
curl -X GET "http://localhost:8849/nacos/service/zhiyan-auth/healthy-instances"
```

---

## 📊 验证配置历史记录

配置历史记录包含以下信息：

| 字段 | 说明 | 示例 |
|------|------|------|
| id | 历史记录ID | 1 |
| dataId | 配置ID | test-config.yaml |
| group | 分组 | DEFAULT_GROUP |
| content | 配置内容 | server:\n  port: 8080 |
| opType | 操作类型 | CREATE/UPDATE/DELETE |
| operator | 操作人 | admin |
| createTime | 创建时间 | 2025-10-31T10:30:00 |

---

## 🎯 测试检查清单

- [ ] 能够获取配置列表
- [ ] 能够获取单个配置内容
- [ ] 能够创建/更新配置
- [ ] 能够删除配置
- [ ] 能够添加配置监听器
- [ ] 配置变更时能看到日志输出
- [ ] 能够查看指定配置的历史记录
- [ ] 能够查看所有配置的历史记录
- [ ] 能够查看最近N条历史记录
- [ ] Swagger UI 可以正常访问
- [ ] 所有接口返回格式正确

---

## ❓ 常见问题

### 1. 无法获取配置列表
**原因**：可能 Nacos 服务器未启动或连接失败
**解决**：
- 检查 Nacos 是否运行：访问 http://localhost:8848/nacos
- 检查配置文件中的 `nacos.management.server-addr`

### 2. 监听器没有触发
**原因**：可能没有先添加监听器就更新了配置
**解决**：先调用 `POST /nacos/config/listener/{dataId}` 添加监听器

### 3. 历史记录为空
**原因**：历史记录存储在内存中，重启后会清空
**解决**：
- 这是正常的，当前版本使用内存存储
- 生产环境建议改用数据库存储

### 4. Swagger UI 无法访问
**原因**：端口被占用或服务未启动
**解决**：
- 检查服务是否正常启动
- 查看日志确认端口是否为 8849
- 访问：http://localhost:8849/doc.html

---

## 📝 小提示

1. **配置内容格式**：发布配置时，Content-Type 使用 `text/plain`，内容可以是 YAML 或 properties 格式
2. **监听器**：添加监听器后，只有新的配置变更才会被记录到历史
3. **历史记录**：当前使用内存存储，应用重启后会清空（可改造为数据库存储）
4. **操作人**：添加监听器时可以指定操作人，默认为 "admin"
5. **日志级别**：开发环境建议将日志级别设为 DEBUG，可以看到更详细的配置内容

---

## 🎉 测试完成

恭喜！你已经完成了所有 Nacos 配置管理 API 的测试。

如有问题，请查看应用日志：`logs/zhiyan-nacos/zhiyan-nacos.log`

