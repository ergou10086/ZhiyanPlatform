# Sentinel Dashboard 流控监控平台使用指南

## 📋 功能说明

Sentinel Dashboard 是智研平台的流量控制和熔断降级监控管理模块，提供：

- ✅ **流量控制（Flow Control）**：QPS限流、并发线程数限流
- ✅ **熔断降级（Circuit Breaking）**：慢调用比例、异常比例、异常数熔断
- ✅ **系统保护（System Protection）**：CPU、Load、RT、线程数、入口QPS保护
- ✅ **实时监控（Real-time Monitoring）**：通过QPS、Block QPS、异常、RT等指标
- ✅ **规则管理（Rule Management）**：动态配置和持久化规则

---

## 🚀 快速启动

### 1. 启动服务

```bash
cd zhiyan-monitor/zhiyan-sentinel-dashboard
mvn spring-boot:run
```

服务运行在端口：**9091**

### 2. 访问管理界面

**Knife4j API文档**：
```
http://localhost:9091/doc.html
```

**健康检查**：
```
http://localhost:9091/health
```

---

## 📊 核心功能

### 1️⃣ 流控规则管理

#### 查询流控规则
```bash
GET /sentinel/rules/flow?app=zhiyan-auth
```

#### 添加流控规则
```bash
POST /sentinel/rules/flow
Content-Type: application/json

{
  "resource": "/api/auth/login",
  "grade": 1,              // 限流类型：0-线程数 1-QPS
  "count": 100,            // 阈值
  "limitApp": "default",   // 来源应用
  "strategy": 0,           // 策略：0-直接 1-关联 2-链路
  "controlBehavior": 0     // 效果：0-快速失败 1-Warm Up 2-排队等待
}
```

#### 更新流控规则
```bash
PUT /sentinel/rules/flow
Content-Type: application/json

{
  "resource": "/api/auth/login",
  "count": 200
}
```

#### 删除流控规则
```bash
DELETE /sentinel/rules/flow/{resource}
```

---

### 2️⃣ 降级规则管理

#### 查询降级规则
```bash
GET /sentinel/rules/degrade?app=zhiyan-auth
```

#### 添加降级规则
```bash
POST /sentinel/rules/degrade
Content-Type: application/json

{
  "resource": "/api/auth/login",
  "grade": 0,                    // 策略：0-慢调用比例 1-异常比例 2-异常数
  "count": 0.5,                  // 阈值
  "timeWindow": 10,              // 熔断时长（秒）
  "minRequestAmount": 5,         // 最小请求数
  "slowRatioThreshold": 500,     // 慢调用RT（毫秒）
  "statIntervalMs": 1000         // 统计窗口（毫秒）
}
```

---

### 3️⃣ 系统规则管理

#### 查询系统规则
```bash
GET /sentinel/rules/system
```

#### 添加系统规则
```bash
POST /sentinel/rules/system
Content-Type: application/json

{
  "highestSystemLoad": -1,      // Load阈值（-1表示不限制）
  "avgRt": 1000,                // 平均RT阈值（毫秒）
  "maxThread": 100,             // 最大线程数
  "qps": 1000,                  // 入口QPS阈值
  "highestCpuUsage": 0.8        // CPU使用率阈值（0-1）
}
```

---

### 4️⃣ 监控数据查询

#### 获取应用列表
```bash
GET /sentinel/metrics/apps
```

#### 注册应用
```bash
POST /sentinel/metrics/apps/zhiyan-auth
```

#### 获取历史监控数据
```bash
GET /sentinel/metrics?app=zhiyan-auth&resource=/api/auth/login&startTime=1730361600000&endTime=1730365200000
```

#### 获取实时监控指标
```bash
GET /sentinel/metrics/realtime?app=zhiyan-auth&resource=/api/auth/login
```

**返回数据示例**：
```json
{
  "code": 200,
  "data": {
    "app": "zhiyan-auth",
    "resource": "/api/auth/login",
    "timestamp": 1730361600000,
    "passQps": 100,      // 通过QPS
    "successQps": 98,    // 成功QPS
    "blockQps": 2,       // 限流QPS
    "exceptionQps": 0,   // 异常QPS
    "rt": 50.5,          // 平均响应时间（毫秒）
    "occupiedPassQps": 10 // 并发线程数
  }
}
```

---

## 🔧 微服务集成

### 在其他微服务中添加 Sentinel 支持

#### 1. 添加依赖（在各微服务 pom.xml 中）

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

#### 2. 配置 Sentinel（在 application.yml 中）

```yaml
spring:
  cloud:
    sentinel:
      transport:
        # Sentinel Dashboard 地址
        dashboard: localhost:9091
        # 与 Dashboard 通信端口
        port: 8719
      # 开启 Sentinel
      enabled: true
      # 饥饿加载
      eager: true
```

#### 3. 使用注解保护资源

```java
@RestController
public class AuthController {
    
    @GetMapping("/api/auth/login")
    @SentinelResource(
        value = "login",                    // 资源名称
        blockHandler = "handleBlock",       // 限流处理方法
        fallback = "handleFallback"         // 降级处理方法
    )
    public R<String> login(@RequestBody LoginBody body) {
        // 业务逻辑
        return R.ok("登录成功");
    }
    
    // 限流处理
    public R<String> handleBlock(LoginBody body, BlockException ex) {
        return R.fail("系统繁忙，请稍后再试");
    }
    
    // 降级处理
    public R<String> handleFallback(LoginBody body, Throwable ex) {
        return R.fail("服务异常，请稍后再试");
    }
}
```

---

## 📈 监控指标说明

| 指标 | 说明 |
|------|------|
| **passQps** | 通过的请求数量（QPS） |
| **successQps** | 成功的请求数量 |
| **blockQps** | 被限流的请求数量 |
| **exceptionQps** | 发生异常的请求数量 |
| **rt** | 平均响应时间（毫秒） |
| **occupiedPassQps** | 当前并发线程数 |

---

## 🎯 典型应用场景

### 场景1：接口限流
为登录接口设置 QPS 限制为 100：

```bash
curl -X POST http://localhost:9091/sentinel/rules/flow \
  -H "Content-Type: application/json" \
  -d '{
    "resource": "/api/auth/login",
    "grade": 1,
    "count": 100,
    "limitApp": "default"
  }'
```

### 场景2：慢调用熔断
对响应时间超过 500ms 的调用进行熔断：

```bash
curl -X POST http://localhost:9091/sentinel/rules/degrade \
  -H "Content-Type: application/json" \
  -d '{
    "resource": "/api/projects/list",
    "grade": 0,
    "count": 0.5,
    "timeWindow": 10,
    "slowRatioThreshold": 500,
    "minRequestAmount": 5
  }'
```

### 场景3：系统保护
设置系统级 CPU 保护：

```bash
curl -X POST http://localhost:9091/sentinel/rules/system \
  -H "Content-Type: application/json" \
  -d '{
    "highestCpuUsage": 0.8,
    "qps": 5000
  }'
```

---

## 🔐 生产环境配置

### 修改 application-prod.yml

```yaml
server:
  port: 9091

sentinel:
  dashboard:
    address: ${SENTINEL_DASHBOARD_ADDRESS:10.7.10.98:8858}
    enabled: true
  datasource:
    nacos:
      server-addr: ${NACOS_SERVER_ADDR:10.7.10.98:8848}
      namespace: ${NACOS_NAMESPACE:your-namespace-id}
      group-id: SENTINEL_GROUP
      username: nacos
      password: nacos
```

---

## ⚠️ 注意事项

1. **规则持久化**：当前规则存储在内存中，重启会丢失。建议配置 Nacos 作为规则数据源实现持久化。

2. **Dashboard 部署**：如需使用 Sentinel 官方 Dashboard UI，可以运行：
   ```bash
   java -jar sentinel-dashboard-1.8.8.jar --server.port=8858
   ```

3. **性能影响**：Sentinel 对性能影响极小（<1%），可放心使用。

4. **规则生效**：规则修改后实时生效，无需重启服务。

5. **监控数据**：当前监控数据存储在内存中，生产环境建议接入时序数据库。

---

## 🎉 快速测试

### 1. 启动服务
```bash
mvn spring-boot:run
```

### 2. 访问 Swagger UI
```
http://localhost:9091/doc.html
```

### 3. 注册应用
```bash
curl -X POST http://localhost:9091/sentinel/metrics/apps/zhiyan-auth
```

### 4. 添加流控规则
在 Swagger UI 中测试 `POST /sentinel/rules/flow` 接口

### 5. 查看规则
在 Swagger UI 中测试 `GET /sentinel/rules/flow` 接口

---

## 📚 相关文档

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)
- [Sentinel Dashboard](https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0)

---

## 🆘 常见问题

**Q: 如何查看规则是否生效？**
A: 调用 `GET /sentinel/rules/flow` 查看已配置的规则。

**Q: 规则重启后会丢失吗？**
A: 当前版本规则存储在内存中，重启会丢失。需要配置 Nacos 数据源实现持久化。

**Q: 如何接入生产环境？**
A: 修改 `application-prod.yml` 中的 Sentinel Dashboard 地址和 Nacos 配置。

**Q: 支持哪些限流策略？**
A: 支持 QPS 限流、并发线程数限流、关联限流、链路限流等多种策略。


