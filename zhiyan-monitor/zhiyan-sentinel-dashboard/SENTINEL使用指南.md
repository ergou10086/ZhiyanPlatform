# Sentinel Dashboard 完整使用指南

## 📖 目录

1. [Sentinel Dashboard 启动方式](#sentinel-dashboard-启动方式)
2. [微服务集成 Sentinel](#微服务集成-sentinel)
3. [规则配置与管理](#规则配置与管理)
4. [监控数据查看](#监控数据查看)
5. [完整示例](#完整示例)
6. [常见问题](#常见问题)

---

## 🚀 Sentinel Dashboard 启动方式

Sentinel Dashboard 有两种启动方式：

### 方式一：启动官方 Sentinel Dashboard（推荐用于可视化界面）

官方 Dashboard 提供完整的 Web UI 界面，可以直观地管理和监控。

```bash
# 进入 lib 目录
cd D:\WorkSpace\JavaDemo\Programe\ZhiyanPlatform\ZhiyanPlatform\zhiyan-monitor\zhiyan-sentinel-dashboard\src\main\resources\lib

# 启动 Sentinel Dashboard（使用 1.8.8 版本）
java -jar sentinel-dashboard-1.8.8.jar --server.port=8858

# 或者使用 1.8.9 版本
java -jar sentinel-dashboard-1.8.9.jar --server.port=8858
```

**启动参数说明：**

- `--server.port=8858`：Dashboard 运行端口，默认 8080
- `-Dsentinel.dashboard.auth.username=sentinel`：设置登录用户名（可选）
- `-Dsentinel.dashboard.auth.password=sentinel`：设置登录密码（可选）

**访问 Dashboard UI：**

```
http://localhost:8858
默认账号：sentinel
默认密码：sentinel
```

**完整启动命令（带认证）：**
```bash
java -Dserver.port=8858 ^
     -Dcsp.sentinel.dashboard.server=localhost:8858 ^
     -Dproject.name=sentinel-dashboard ^
     -Dsentinel.dashboard.auth.username=admin ^
     -Dsentinel.dashboard.auth.password=admin123 ^
     -jar sentinel-dashboard-1.8.8.jar
```

---

### 方式二：启动自定义 Spring Boot 应用（API 方式）

你的项目中还提供了一个基于 Spring Boot 的 RESTful API 服务，用于程序化管理 Sentinel。

```bash
# 进入项目根目录
cd D:\WorkSpace\JavaDemo\Programe\ZhiyanPlatform\ZhiyanPlatform

# 启动 Spring Boot 应用
cd zhiyan-monitor\zhiyan-sentinel-dashboard
mvn spring-boot:run

# 或者直接运行主类
# 在 IDE 中运行 ZhiyanSentinelDashboardApplication
```

**服务信息：**
- **端口**：9091
- **Swagger UI**：http://localhost:9091/doc.html
- **健康检查**：http://localhost:9091/health
- **API 文档**：http://localhost:9091/v3/api-docs

---

### 推荐架构

**生产环境推荐同时启动两个：**

1. **官方 Dashboard（端口 8858）**：用于可视化查看和管理
2. **Spring Boot API（端口 9091）**：用于程序化管理和集成

```plaintext
┌─────────────────────────────────────────────────┐
│                 开发人员/运维                    │
└─────────────────┬───────────────────────────────┘
                  │
          ┌───────┴────────┐
          │                │
          ▼                ▼
┌─────────────────┐  ┌─────────────────┐
│ Sentinel UI     │  │ Spring Boot API │
│ :8858           │  │ :9091           │
│ (可视化界面)     │  │ (RESTful API)   │
└─────────────────┘  └─────────────────┘
          │                │
          └────────┬───────┘
                   │
                   ▼
        ┌──────────────────────┐
        │   Nacos Config       │
        │   规则持久化          │
        └──────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
┌──────────────┐      ┌──────────────┐
│ zhiyan-auth  │      │ zhiyan-xxx   │
│ :8001        │      │ :8xxx        │
│ (微服务1)     │      │ (微服务N)     │
└──────────────┘      └──────────────┘
```

---

## 🔧 微服务集成 Sentinel

要让你的微服务被 Sentinel Dashboard 监控，需要在每个微服务中添加 Sentinel 客户端依赖和配置。

### 第一步：添加依赖

在需要监控的微服务的 `pom.xml` 中添加：

```xml
<!-- Spring Cloud Alibaba Sentinel -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>

<!-- 如果需要规则持久化到 Nacos -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
</dependency>

<!-- 如果需要 OpenFeign 整合 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

---

### 第二步：配置 Sentinel

在微服务的 `application.yml` 或 `bootstrap.yml` 中添加配置：

#### 基础配置（开发环境）

```yaml
spring:
  application:
    name: zhiyan-auth  # 你的服务名称
  cloud:
    sentinel:
      # 开启 Sentinel
      enabled: true
      # 饥饿加载（立即初始化，而不是懒加载）
      eager: true
      
      # 连接到 Sentinel Dashboard
      transport:
        # Sentinel Dashboard 地址
        dashboard: localhost:8858
        # 与 Dashboard 通信的端口（每个服务不同）
        port: 8719
        # 心跳发送周期（毫秒）
        heartbeat-interval-ms: 5000
      
      # Web 过滤器配置
      web-context-unify: false
      
      # HTTP 方法规则
      http-method-specify: true
```

#### 完整配置（生产环境）

```yaml
spring:
  application:
    name: zhiyan-auth
  cloud:
    sentinel:
      enabled: true
      eager: true
      
      # Dashboard 配置
      transport:
        dashboard: ${SENTINEL_DASHBOARD_HOST:localhost}:8858
        port: 8719
        heartbeat-interval-ms: 5000
        client-ip: ${SENTINEL_CLIENT_IP:}  # 指定客户端 IP（多网卡时使用）
      
      # 日志配置
      log:
        dir: logs/sentinel
        switch-pid: true
      
      # 规则持久化到 Nacos
      datasource:
        # 流控规则
        flow:
          nacos:
            server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:3936229d-c8b3-4947-9192-6b984dca44bf}
            group-id: SENTINEL_GROUP
            data-id: ${spring.application.name}-flow-rules
            rule-type: flow
            username: nacos
            password: nacos
        
        # 降级规则
        degrade:
          nacos:
            server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:3936229d-c8b3-4947-9192-6b984dca44bf}
            group-id: SENTINEL_GROUP
            data-id: ${spring.application.name}-degrade-rules
            rule-type: degrade
            username: nacos
            password: nacos
        
        # 系统规则
        system:
          nacos:
            server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:3936229d-c8b3-4947-9192-6b984dca44bf}
            group-id: SENTINEL_GROUP
            data-id: ${spring.application.name}-system-rules
            rule-type: system
            username: nacos
            password: nacos
        
        # 授权规则
        authority:
          nacos:
            server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:3936229d-c8b3-4947-9192-6b984dca44bf}
            group-id: SENTINEL_GROUP
            data-id: ${spring.application.name}-authority-rules
            rule-type: authority
            username: nacos
            password: nacos
        
        # 热点参数规则
        param-flow:
          nacos:
            server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:3936229d-c8b3-4947-9192-6b984dca44bf}
            group-id: SENTINEL_GROUP
            data-id: ${spring.application.name}-param-flow-rules
            rule-type: param-flow
            username: nacos
            password: nacos

# Feign 整合 Sentinel
feign:
  sentinel:
    enabled: true

# 暴露 Sentinel 端点
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

---

### 第三步：代码中使用 @SentinelResource

#### 1. 基础用法

```java
package hbnu.project.zhiyanauth.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import hbnu.project.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 登录接口 - 带流控保护
     */
    @PostMapping("/login")
    @SentinelResource(
        value = "login",                      // 资源名称
        blockHandler = "loginBlockHandler",   // 限流/降级处理方法
        fallback = "loginFallback"            // 异常处理方法
    )
    public R<String> login(@RequestBody LoginRequest request) {
        // 业务逻辑
        log.info("用户登录：{}", request.getUsername());
        return R.ok("登录成功", "token-xxx");
    }

    /**
     * 限流处理方法
     * 注意：参数要和原方法一致，最后加一个 BlockException
     */
    public R<String> loginBlockHandler(LoginRequest request, BlockException ex) {
        log.warn("登录接口被限流，用户：{}", request.getUsername());
        return R.fail("系统繁忙，请稍后再试");
    }

    /**
     * 异常降级处理方法
     * 注意：参数要和原方法一致，最后加一个 Throwable
     */
    public R<String> loginFallback(LoginRequest request, Throwable ex) {
        log.error("登录接口异常，用户：{}", request.getUsername(), ex);
        return R.fail("服务异常，请稍后重试");
    }
}
```

#### 2. 使用独立的 Handler 类（推荐）

```java
/**
 * Sentinel 统一异常处理器
 */
@Slf4j
@Component
public class SentinelExceptionHandler {

    /**
     * 通用限流处理
     */
    public static R<Void> handleBlock(BlockException ex) {
        log.warn("请求被限流：{}", ex.getRule());
        return R.fail(429, "系统繁忙，请稍后再试");
    }

    /**
     * 通用异常处理
     */
    public static R<Void> handleFallback(Throwable ex) {
        log.error("服务异常", ex);
        return R.fail(500, "服务暂时不可用，请稍后重试");
    }
}

// 使用方式
@SentinelResource(
    value = "getUserInfo",
    blockHandlerClass = SentinelExceptionHandler.class,
    blockHandler = "handleBlock",
    fallbackClass = SentinelExceptionHandler.class,
    fallback = "handleFallback"
)
public R<UserInfo> getUserInfo(Long userId) {
    // 业务逻辑
    return R.ok(userService.getById(userId));
}
```

#### 3. Feign 整合 Sentinel

```java
/**
 * Feign 客户端
 */
@FeignClient(
    name = "zhiyan-project",
    path = "/api/project",
    fallbackFactory = ProjectFeignFallback.class  // 降级工厂
)
public interface ProjectFeignClient {
    
    @GetMapping("/{id}")
    R<Project> getProject(@PathVariable("id") Long id);
}

/**
 * Feign 降级工厂
 */
@Slf4j
@Component
public class ProjectFeignFallback implements FallbackFactory<ProjectFeignClient> {

    @Override
    public ProjectFeignClient create(Throwable cause) {
        return new ProjectFeignClient() {
            @Override
            public R<Project> getProject(Long id) {
                log.error("调用项目服务失败，项目ID：{}", id, cause);
                return R.fail("项目服务暂时不可用");
            }
        };
    }
}
```

---

### 第四步：验证服务是否成功接入

#### 1. 启动微服务

启动你的微服务后，观察日志：

```
[Sentinel] Transport started on port 8719
[Sentinel] Connecting to dashboard at localhost:8858
[Sentinel] Successfully connected to dashboard
```

#### 2. 访问 Dashboard 查看

打开 Sentinel Dashboard：http://localhost:8858

在左侧菜单会看到你的服务名称（如 `zhiyan-auth`）

**注意**：服务需要至少有一次请求后才会出现在 Dashboard 中。

#### 3. 发起测试请求

```bash
# 访问你的服务接口
curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

刷新 Dashboard，应该能看到：
- 服务名称
- 实时监控数据（QPS、响应时间等）
- 资源列表（所有的 @SentinelResource 和 Controller 方法）

---

## 📊 规则配置与管理

### 在 Dashboard UI 中配置（推荐新手）

#### 1. 流控规则

1. 在 Dashboard 左侧选择你的服务（如 `zhiyan-auth`）
2. 点击「流控规则」
3. 点击「新增流控规则」
4. 配置参数：
   - **资源名**：`/api/auth/login`（从簇点链路中选择）
   - **阈值类型**：QPS（每秒请求数）
   - **单机阈值**：100（每秒最多 100 个请求）
   - **流控模式**：直接
   - **流控效果**：快速失败

#### 2. 降级规则

1. 点击「降级规则」
2. 点击「新增降级规则」
3. 配置参数：
   - **资源名**：`/api/auth/login`
   - **降级策略**：慢调用比例
   - **慢调用 RT**：500ms
   - **比例阈值**：0.5（50%）
   - **熔断时长**：10秒
   - **最小请求数**：5

#### 3. 热点规则

用于对特定参数进行限流（如针对特定用户 ID）。

#### 4. 系统规则

全局系统保护：
- **Load 阈值**：系统负载
- **RT 阈值**：平均响应时间
- **线程数**：并发线程数
- **入口 QPS**：总入口 QPS

---

### 通过 API 配置（程序化管理）

使用你的 Spring Boot API 服务（端口 9091）：

#### 1. 添加流控规则

```bash
curl -X POST http://localhost:9091/sentinel/rules/flow \
  -H "Content-Type: application/json" \
  -d '{
    "app": "zhiyan-auth",
    "resource": "/api/auth/login",
    "grade": 1,
    "count": 100,
    "limitApp": "default",
    "strategy": 0,
    "controlBehavior": 0
  }'
```

#### 2. 查询流控规则

```bash
curl -X GET "http://localhost:9091/sentinel/rules/flow?app=zhiyan-auth"
```

#### 3. 更新流控规则

```bash
curl -X PUT http://localhost:9091/sentinel/rules/flow \
  -H "Content-Type: application/json" \
  -d '{
    "app": "zhiyan-auth",
    "resource": "/api/auth/login",
    "count": 200
  }'
```

#### 4. 删除流控规则

```bash
curl -X DELETE "http://localhost:9091/sentinel/rules/flow/{resource}?app=zhiyan-auth"
```

---

## 📈 监控数据查看

### 实时监控

在 Dashboard 中：
1. 选择服务
2. 点击「实时监控」
3. 可以看到：
   - **通过 QPS**：成功的请求数
   - **拒绝 QPS**：被限流的请求数
   - **异常 QPS**：异常的请求数
   - **平均 RT**：平均响应时间

### 簇点链路

显示所有被监控的资源（接口、方法等），可以直接在这里添加规则。

### 机器列表

查看该服务的所有实例。

---

## 🎯 完整示例

### 示例场景：为认证服务添加流控保护

#### 1. 服务配置

**application-dev.yml**
```yaml
spring:
  application:
    name: zhiyan-auth
  cloud:
    sentinel:
      enabled: true
      eager: true
      transport:
        dashboard: localhost:8858
        port: 8719
      datasource:
        flow:
          nacos:
            server-addr: localhost:8848
            namespace: 3936229d-c8b3-4947-9192-6b984dca44bf
            group-id: SENTINEL_GROUP
            data-id: zhiyan-auth-flow-rules
            rule-type: flow
            username: nacos
            password: nacos

server:
  port: 8001
```

#### 2. Controller 代码

```java
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @SentinelResource(
        value = "auth:login",
        blockHandler = "loginBlockHandler"
    )
    public R<LoginVO> login(@RequestBody @Validated LoginRequest request) {
        LoginVO result = authService.login(request);
        return R.ok(result);
    }

    /**
     * 登录限流处理
     */
    public R<LoginVO> loginBlockHandler(LoginRequest request, BlockException ex) {
        log.warn("登录接口触发限流，用户：{}", request.getUsername());
        return R.fail(429, "登录请求过于频繁，请稍后再试");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    @SentinelResource(value = "auth:info")
    public R<UserInfo> getUserInfo() {
        UserInfo userInfo = authService.getCurrentUserInfo();
        return R.ok(userInfo);
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @SentinelResource(
        value = "auth:refresh",
        blockHandler = "refreshBlockHandler"
    )
    public R<TokenVO> refreshToken(@RequestBody RefreshTokenRequest request) {
        TokenVO token = authService.refreshToken(request.getRefreshToken());
        return R.ok(token);
    }

    public R<TokenVO> refreshBlockHandler(RefreshTokenRequest request, BlockException ex) {
        return R.fail(429, "刷新令牌请求过于频繁");
    }
}
```

#### 3. 启动流程

1. **启动 Nacos**（如果还没启动）
   ```bash
   cd D:\nacos\bin
   startup.cmd -m standalone
   ```

2. **启动 Sentinel Dashboard**
   ```bash
   cd D:\WorkSpace\JavaDemo\Programe\ZhiyanPlatform\ZhiyanPlatform\zhiyan-monitor\zhiyan-sentinel-dashboard\src\main\resources\lib
   java -jar sentinel-dashboard-1.8.8.jar --server.port=8858
   ```

3. **启动认证服务**
   ```bash
   cd zhiyan-auth
   mvn spring-boot:run
   ```

4. **发起测试请求**
   ```bash
   curl -X POST http://localhost:8001/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"123456"}'
   ```

5. **访问 Dashboard**
   - 打开：http://localhost:8858
   - 登录：sentinel / sentinel
   - 查看 `zhiyan-auth` 服务

6. **配置流控规则**
   - 资源名：`auth:login`
   - QPS 阈值：10
   - 流控效果：快速失败

7. **压测验证**
   ```bash
   # 使用 ab 或 jmeter 进行压测
   ab -n 1000 -c 50 -T 'application/json' \
      -p login.json \
      http://localhost:8001/api/auth/login
   ```

8. **观察监控**
   - 在 Dashboard 实时监控中查看通过/拒绝的 QPS
   - 在簇点链路中查看各资源的调用情况

---

## ❓ 常见问题

### 1. Dashboard 中看不到我的服务？

**原因**：
- 服务还没有流量（Sentinel 懒加载）
- Dashboard 地址配置错误
- 端口冲突

**解决方案**：
```yaml
spring:
  cloud:
    sentinel:
      eager: true  # 开启饥饿加载
      transport:
        dashboard: localhost:8858  # 确认地址正确
        port: 8719  # 确保端口不冲突
```

至少访问一次服务接口，然后刷新 Dashboard。

### 2. 规则配置后不生效？

**检查**：
1. 资源名是否正确（区分大小写）
2. 在「簇点链路」中确认资源是否存在
3. 查看服务日志是否有错误

### 3. 规则重启后丢失？

**原因**：规则默认存储在内存中。

**解决方案**：配置 Nacos 数据源实现持久化（参考上面的完整配置）。

### 4. @SentinelResource 不生效？

**检查**：
1. 是否添加了 `spring-cloud-starter-alibaba-sentinel` 依赖
2. `blockHandler` 和 `fallback` 方法签名是否正确
3. 方法必须是 public
4. 如果使用独立的 Handler 类，方法必须是 static

### 5. 端口冲突问题

如果多个服务在同一台机器上，`transport.port` 会冲突。

**解决方案**：
```yaml
# 服务 1
sentinel:
  transport:
    port: 8719

# 服务 2
sentinel:
  transport:
    port: 8720

# 服务 3
sentinel:
  transport:
    port: 8721
```

或者设置为自动分配：
```yaml
sentinel:
  transport:
    port: -1  # 自动选择可用端口
```

### 6. Feign 调用不生效？

确保配置：
```yaml
feign:
  sentinel:
    enabled: true
```

并且使用 `fallbackFactory` 而不是 `fallback`。

---

## 📝 总结

### 核心步骤回顾

1. **启动 Sentinel Dashboard**
   ```bash
   java -jar sentinel-dashboard-1.8.8.jar --server.port=8858
   ```

2. **微服务添加依赖**
   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
   </dependency>
   ```

3. **微服务配置**
   ```yaml
   spring:
     cloud:
       sentinel:
         enabled: true
         transport:
           dashboard: localhost:8858
           port: 8719
   ```

4. **代码中使用**
   ```java
   @SentinelResource(value = "resourceName", blockHandler = "handleBlock")
   public R<T> method() { ... }
   ```

5. **访问接口触发监控**

6. **在 Dashboard 中配置规则**

7. **（可选）配置 Nacos 持久化**

---

## 🔗 参考资源

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/)
- [Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)
- [SENTINEL_GUIDE.md](./SENTINEL_GUIDE.md)

---

**编写者**：智研平台开发团队  
**更新时间**：2025-11-01  
**版本**：v1.0

