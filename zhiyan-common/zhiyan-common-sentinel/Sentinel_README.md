# zhiyan-common-sentinel

**智研平台 Sentinel 流控降级通用模块** - 开箱即用的分布式流量控制、熔断降级、系统保护解决方案

---

## 📋 功能特性

- ✅ **自动配置** - 零配置即可使用，自动集成 Sentinel
- ✅ **流量控制** - QPS 限流、并发线程数限流、关联限流、链路限流
- ✅ **熔断降级** - 慢调用比例、异常比例、异常数熔断
- ✅ **系统保护** - CPU、Load、RT、线程数、入口 QPS 自适应保护
- ✅ **Dashboard 集成** - 自动连接 Sentinel Dashboard 实时监控
- ✅ **Nacos 持久化** - 规则配置持久化到 Nacos，动态生效
- ✅ **全局异常处理** - 统一的限流降级异常处理
- ✅ **Feign 支持** - 开箱即用的 Feign 熔断降级
- ✅ **工具类** - 丰富的 Sentinel 工具类和注解

---

## 🚀 快速开始

### 第一步：添加依赖

在你的微服务模块的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>hbnu.project</groupId>
    <artifactId>zhiyan-common-sentinel</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**就这么简单！** 添加依赖后，Sentinel 功能自动启用。

---

### 第二步：配置（可选）

在 `application.yml` 中添加配置（所有配置都有默认值，可以不配置）：

#### 最简配置（推荐）

```yaml
spring:
  application:
    name: zhiyan-auth  # 你的服务名

zhiyan:
  sentinel:
    enabled: true  # 默认 true，可不配
    dashboard:
      host: localhost  # Sentinel Dashboard 地址
      port: 8858       # Sentinel Dashboard 端口
```

#### 完整配置

```yaml
spring:
  application:
    name: zhiyan-auth
  cloud:
    sentinel:
      # 基础配置
      enabled: true
      eager: true  # 饥饿加载
      
      # Dashboard 配置
      transport:
        dashboard: localhost:8858
        port: 8719  # 客户端通信端口
        heartbeat-interval-ms: 5000
      
      # Web 配置
      web-context-unify: false
      http-method-specify: true

zhiyan:
  sentinel:
    enabled: true
    eager: true
    
    # Dashboard 配置
    dashboard:
      host: localhost
      port: 8858
      client-port: 8719
      heartbeat-interval-ms: 5000
    
    # 日志配置
    log:
      dir: logs/sentinel
      switch-pid: true
    
    # 全局配置
    global:
      web-context-unify: false
      http-method-specify: true
      global-qps: -1      # 全局 QPS 限制（-1 不限制）
      global-thread: -1   # 全局线程数限制（-1 不限制）
    
    # Nacos 数据源（规则持久化）
    nacos:
      enabled: true
      server-addr: localhost:8848
      namespace: 3936229d-c8b3-4947-9192-6b984dca44bf
      group-id: SENTINEL_GROUP
      username: nacos
      password: nacos
      data-id-suffix: json
```

---

### 第三步：使用

#### 1. 使用 @SentinelResource 注解

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 基础用法
     */
    @PostMapping("/login")
    @SentinelResource(
        value = "auth:login",  // 资源名称
        blockHandler = "loginBlockHandler"  // 限流处理方法
    )
    public R<String> login(@RequestBody LoginRequest request) {
        // 业务逻辑
        return R.ok("登录成功");
    }

    /**
     * 限流处理方法
     */
    public R<String> loginBlockHandler(LoginRequest request, BlockException ex) {
        return R.fail(429, "登录请求过于频繁，请稍后再试");
    }

    /**
     * 带降级处理
     */
    @GetMapping("/info")
    @SentinelResource(
        value = "auth:info",
        blockHandler = "infoBlockHandler",  // 限流处理
        fallback = "infoFallback"           // 异常降级
    )
    public R<UserInfo> getUserInfo() {
        // 业务逻辑
        return R.ok(userService.getCurrentUser());
    }

    public R<UserInfo> infoBlockHandler(BlockException ex) {
        return R.fail(429, "请求过于频繁");
    }

    public R<UserInfo> infoFallback(Throwable ex) {
        log.error("获取用户信息失败", ex);
        return R.fail(500, "服务异常");
    }
}
```

#### 2. 使用统一的异常处理器（推荐）

创建全局处理器：

```java
@Component
public class SentinelExceptionHandlerImpl implements SentinelExceptionHandler {

    @Override
    public Object handle(Object request, BlockException exception) {
        // 统一的限流处理
        if (exception instanceof FlowException) {
            return R.fail(429, "访问过于频繁，请稍后再试");
        } else if (exception instanceof DegradeException) {
            return R.fail(503, "服务暂时不可用");
        }
        return R.fail(429, "系统繁忙");
    }

    @Override
    public Object handleFallback(Object request, Throwable throwable) {
        // 统一的异常降级处理
        log.error("服务异常", throwable);
        return R.fail(500, "服务异常，请稍后重试");
    }
}
```

使用：

```java
@SentinelResource(
    value = "auth:login",
    blockHandlerClass = SentinelExceptionHandlerImpl.class,
    blockHandler = "handle"
)
public R<String> login(@RequestBody LoginRequest request) {
    return R.ok("登录成功");
}
```

#### 3. 使用 SentinelUtil 工具类

```java
@Service
public class UserService {

    /**
     * 手动流控
     */
    public User getUserById(Long userId) {
        return SentinelUtil.executeWithFallback(
            "user:getById",
            // 业务逻辑
            () -> userMapper.selectById(userId),
            // 降级逻辑
            () -> getUserFromCache(userId)
        );
    }

    /**
     * 动态添加流控规则
     */
    public void addRateLimit(String resource, int qps) {
        SentinelUtil.addFlowRule(resource, qps);
    }

    /**
     * 检查是否被限流
     */
    public boolean checkLimit(String resource) {
        return SentinelUtil.isBlocked(resource);
    }
}
```

#### 4. Feign 集成

**方式一：使用默认降级工厂**

```java
@FeignClient(
    name = "zhiyan-project",
    path = "/api/project",
    fallbackFactory = SentinelFeignFallbackFactory.class  // 使用通用降级工厂
)
public interface ProjectFeignClient {
    
    @GetMapping("/{id}")
    R<Project> getProject(@PathVariable("id") Long id);
}
```

**方式二：自定义降级工厂**

```java
@FeignClient(
    name = "zhiyan-project",
    path = "/api/project",
    fallbackFactory = ProjectFeignFallback.class
)
public interface ProjectFeignClient {
    @GetMapping("/{id}")
    R<Project> getProject(@PathVariable("id") Long id);
}

@Component
public class ProjectFeignFallback implements FallbackFactory<ProjectFeignClient> {
    
    @Override
    public ProjectFeignClient create(Throwable cause) {
        return new ProjectFeignClient() {
            @Override
            public R<Project> getProject(Long id) {
                log.error("调用项目服务失败", cause);
                return R.fail("项目服务暂时不可用");
            }
        };
    }
}
```

配置 Feign 支持：

```yaml
feign:
  sentinel:
    enabled: true
```

---

## 📊 与 Sentinel Dashboard 联动

### 启动 Dashboard

```bash
# 方式一：启动官方 Dashboard
cd zhiyan-monitor/zhiyan-sentinel-dashboard/src/main/resources/lib
java -jar sentinel-dashboard-1.8.8.jar --server.port=8858

# 方式二：启动自定义 API 服务
cd zhiyan-monitor/zhiyan-sentinel-dashboard
mvn spring-boot:run
```

### 查看监控数据

1. 访问 Dashboard：http://localhost:8858
2. 默认账号密码：sentinel / sentinel
3. 在左侧菜单选择你的服务（如 `zhiyan-auth`）
4. 查看实时监控、流控规则、降级规则等

### 配置规则

#### 在 Dashboard UI 中配置

1. 点击「流控规则」-> 「新增流控规则」
2. 选择资源名（如 `auth:login`）
3. 设置阈值类型（QPS）和单机阈值（如 100）
4. 点击「新增」

#### 通过 API 配置

```bash
# 添加流控规则
curl -X POST http://localhost:9091/sentinel/rules/flow \
  -H "Content-Type: application/json" \
  -d '{
    "app": "zhiyan-auth",
    "resource": "auth:login",
    "grade": 1,
    "count": 100
  }'

# 查询流控规则
curl http://localhost:9091/sentinel/rules/flow?app=zhiyan-auth
```

#### 在 Nacos 中持久化配置

在 Nacos 中创建配置：

- **Data ID**: `zhiyan-auth-flow-rules.json`
- **Group**: `SENTINEL_GROUP`
- **配置内容**:

```json
[
  {
    "resource": "auth:login",
    "grade": 1,
    "count": 100,
    "limitApp": "default",
    "strategy": 0,
    "controlBehavior": 0
  }
]
```

规则会自动同步到服务，并在 Dashboard 中显示。

---

## 🎯 典型应用场景

### 场景1：接口限流

```java
/**
 * 登录接口 - 限制每秒 100 个请求
 */
@PostMapping("/login")
@SentinelResource(value = "auth:login", blockHandler = "loginBlockHandler")
public R<String> login(@RequestBody LoginRequest request) {
    return R.ok(authService.login(request));
}
```

**在 Dashboard 中配置**：
- 资源名：`auth:login`
- 阈值类型：QPS
- 单机阈值：100

### 场景2：慢调用熔断

```java
/**
 * 查询项目列表 - 响应时间超过 500ms 触发熔断
 */
@GetMapping("/projects")
@SentinelResource(value = "project:list", blockHandler = "listBlockHandler")
public R<List<Project>> listProjects() {
    return R.ok(projectService.list());
}
```

**在 Dashboard 中配置**：
- 资源名：`project:list`
- 降级策略：慢调用比例
- 慢调用 RT：500ms
- 比例阈值：50%
- 熔断时长：10秒

### 场景3：热点参数限流

```java
/**
 * 根据用户 ID 查询 - 对特定用户限流
 */
@GetMapping("/user/{id}")
@SentinelResource(value = "user:get", blockHandler = "getBlockHandler")
public R<User> getUser(@PathVariable Long id) {
    return R.ok(userService.getById(id));
}
```

**在 Dashboard 中配置**：
- 资源名：`user:get`
- 参数索引：0（第一个参数）
- 单机阈值：10
- 针对特定值（如 userId=1）设置特殊阈值

### 场景4：服务调用降级

```java
/**
 * 调用其他微服务 - 自动降级
 */
@Autowired
private ProjectFeignClient projectClient;

public R<Project> getProjectInfo(Long projectId) {
    // Feign 自动降级
    return projectClient.getProject(projectId);
}
```

### 场景5：系统保护

在 Dashboard 中配置系统规则：
- CPU 使用率：80%
- 入口 QPS：5000
- 平均 RT：1000ms

当系统负载过高时，自动限流保护。

---

## 📚 配置说明

### 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `zhiyan.sentinel.enabled` | 是否启用 Sentinel | `true` |
| `zhiyan.sentinel.eager` | 是否饥饿加载 | `true` |
| `zhiyan.sentinel.dashboard.host` | Dashboard 地址 | `localhost` |
| `zhiyan.sentinel.dashboard.port` | Dashboard 端口 | `8858` |
| `zhiyan.sentinel.dashboard.client-port` | 客户端通信端口 | `8719` |
| `zhiyan.sentinel.log.dir` | 日志目录 | `logs/sentinel` |
| `zhiyan.sentinel.global.global-qps` | 全局 QPS 限制 | `-1`（不限制） |
| `zhiyan.sentinel.global.global-thread` | 全局线程数限制 | `-1`（不限制） |
| `zhiyan.sentinel.nacos.enabled` | 是否启用 Nacos 数据源 | `true` |
| `zhiyan.sentinel.nacos.server-addr` | Nacos 地址 | `localhost:8848` |
| `zhiyan.sentinel.nacos.namespace` | Nacos 命名空间 | `` |
| `zhiyan.sentinel.nacos.group-id` | 分组 ID | `SENTINEL_GROUP` |

---

## 🔧 高级用法

### 自定义异常处理器

```java
@Component
public class CustomSentinelExceptionHandler implements SentinelExceptionHandler {

    @Override
    public Object handle(Object request, BlockException exception) {
        // 根据不同的限流类型返回不同的响应
        if (exception instanceof FlowException) {
            return buildResponse(429, "请求过于频繁");
        } else if (exception instanceof DegradeException) {
            return buildResponse(503, "服务降级中");
        }
        return buildResponse(429, "系统限流");
    }

    @Override
    public Object handleFallback(Object request, Throwable throwable) {
        log.error("服务异常", throwable);
        return buildResponse(500, "服务异常");
    }

    private Object buildResponse(int code, String message) {
        return R.builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
```

### 动态调整规则

```java
@Service
public class SentinelRuleService {

    /**
     * 动态调整 QPS
     */
    public void adjustQps(String resource, int newQps) {
        SentinelUtil.addFlowRule(resource, newQps);
        log.info("已调整资源 {} 的 QPS 为 {}", resource, newQps);
    }

    /**
     * 临时限流
     */
    public void temporaryLimit(String resource, int qps, int durationSeconds) {
        SentinelUtil.addFlowRule(resource, qps);
        
        // 定时恢复
        scheduler.schedule(() -> {
            SentinelUtil.removeRules(resource);
            log.info("资源 {} 限流已恢复", resource);
        }, durationSeconds, TimeUnit.SECONDS);
    }

    /**
     * 查看当前规则
     */
    public List<FlowRule> getCurrentRules() {
        return SentinelUtil.getFlowRules();
    }
}
```

### 手动流控

```java
@Service
public class OrderService {

    public void createOrder(Order order) {
        try {
            SentinelUtil.execute("order:create", () -> {
                // 受保护的业务逻辑
                orderMapper.insert(order);
                return null;
            });
        } catch (BlockException e) {
            log.warn("订单创建被限流");
            throw new BusinessException("系统繁忙，请稍后再试");
        }
    }
}
```

---

## ❓ 常见问题

### 1. Dashboard 看不到服务？

**解决方案**：
- 确保 Dashboard 地址配置正确
- 至少访问一次服务接口（Sentinel 懒加载）
- 设置 `zhiyan.sentinel.eager=true` 开启饥饿加载
- 检查端口是否被占用（默认 8719）

### 2. 规则不生效？

**检查**：
- 资源名是否正确（区分大小写）
- 在「簇点链路」中确认资源是否存在
- 查看服务日志是否有错误

### 3. 规则重启后丢失？

**解决方案**：
启用 Nacos 数据源实现持久化：

```yaml
zhiyan:
  sentinel:
    nacos:
      enabled: true
      server-addr: localhost:8848
```

### 4. 多个服务端口冲突？

**解决方案**：
为每个服务设置不同的客户端端口：

```yaml
zhiyan:
  sentinel:
    dashboard:
      client-port: 8719  # 服务 1
      # client-port: 8720  # 服务 2
      # client-port: 8721  # 服务 3
```

---

## 📖 相关文档

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/)
- [Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)
- [Sentinel Dashboard 使用指南](../../zhiyan-monitor/zhiyan-sentinel-dashboard/SENTINEL使用指南.md)
- [Sentinel Guide](../../zhiyan-monitor/zhiyan-sentinel-dashboard/SENTINEL_GUIDE.md)

---

---

## 📄 许可证

Apache License 2.0

---

**作者**: 智研平台开发团队  
**更新时间**: 2025-11-01  
**版本**: v1.0.0

