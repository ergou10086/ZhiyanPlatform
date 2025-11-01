# zhiyan-common-log

**智研平台全局日志系统** - 基于 Kotlin 的统一日志管理模块

---

## 🌟 为什么选择 Kotlin?

- **简洁性** - Kotlin 代码比 Java 更简洁，减少样板代码
- **null 安全** - 编译期就能避免 NPE
- **扩展函数** - 更优雅的 API 设计
- **协程支持** - 天然的异步处理能力
- **DSL 风格** - 更友好的配置和日志 API
- **与 Java 互操作** - 100% 兼容 Java，无缝集成

---

## 📋 功能特性

- ✅ **操作日志** - 自动记录用户操作（增删改查等）
- ✅ **访问日志** - 自动记录所有 API 请求
- ✅ **异常日志** - 全局捕获并记录异常
- ✅ **慢请求监控** - 自动标记慢请求
- ✅ **异步处理** - 协程异步处理，不影响性能
- ✅ **扩展函数** - 优雅的 Kotlin 日志 API
- ✅ **灵活配置** - 丰富的配置选项
- ✅ **可扩展** - 支持自定义日志处理器

---

## 🚀 快速开始

### 第一步：添加依赖

在你的微服务 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>hbnu.project</groupId>
    <artifactId>zhiyan-common-log</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**就这么简单！** 日志系统自动启用。

---

### 第二步：配置（可选）

在 `application.yml` 中添加配置（所有配置都有默认值）：

#### 最简配置

```yaml
spring:
  application:
    name: zhiyan-auth  # 你的服务名

# 使用默认配置即可
```

#### 完整配置

```yaml
zhiyan:
  log:
    # 是否启用日志系统
    enabled: true
    
    # 是否启用操作日志
    operation-enabled: true
    
    # 是否启用访问日志
    access-enabled: true
    
    # 是否启用异常日志
    exception-enabled: true
    
    # 是否记录请求参数
    record-request-params: true
    
    # 是否记录响应结果
    record-response-result: true
    
    # 是否记录请求头
    record-headers: false
    
    # 响应结果最大长度
    max-response-length: 2000
    
    # 请求参数最大长度
    max-request-length: 2000
    
    # 排除的 URL 路径（不记录日志）
    excluded-paths:
      - /actuator/**
      - /health
      - /favicon.ico
      - /swagger-ui/**
      - /v3/api-docs/**
      - /doc.html
    
    # 慢请求阈值（毫秒）
    slow-request-threshold: 3000
    
    # 是否异步处理日志
    async: true
    
    # 日志处理线程池配置
    thread-pool:
      core-size: 2
      max-size: 5
      queue-capacity: 100
      thread-name-prefix: zhiyan-log-
```

---

### 第三步：使用

#### 1. 操作日志 - @OperationLog

```kotlin
@RestController
@RequestMapping("/api/user")
class UserController {

    /**
     * 基础用法
     */
    @PostMapping
    @OperationLog(
        module = "用户管理",
        type = OperationType.INSERT,
        description = "新增用户"
    )
    fun createUser(@RequestBody user: User): R<User> {
        return R.ok(userService.save(user))
    }

    /**
     * 更新操作
     */
    @PutMapping("/{id}")
    @OperationLog(
        module = "用户管理",
        type = OperationType.UPDATE,
        description = "更新用户信息"
    )
    fun updateUser(@PathVariable id: Long, @RequestBody user: User): R<User> {
        return R.ok(userService.updateById(user))
    }

    /**
     * 删除操作
     */
    @DeleteMapping("/{id}")
    @OperationLog(
        module = "用户管理",
        type = OperationType.DELETE,
        description = "删除用户"
    )
    fun deleteUser(@PathVariable id: Long): R<Void> {
        userService.removeById(id)
        return R.ok()
    }

    /**
     * 查询操作（不记录响应结果）
     */
    @GetMapping
    @OperationLog(
        module = "用户管理",
        type = OperationType.QUERY,
        description = "查询用户列表",
        recordResult = false  // 不记录响应（数据量大时）
    )
    fun listUsers(): R<List<User>> {
        return R.ok(userService.list())
    }

    /**
     * 导出操作
     */
    @GetMapping("/export")
    @OperationLog(
        module = "用户管理",
        type = OperationType.EXPORT,
        description = "导出用户数据"
    )
    fun exportUsers(): ResponseEntity<ByteArray> {
        val data = userService.exportExcel()
        return ResponseEntity.ok(data)
    }
}
```

#### 2. 访问日志 - @AccessLog

```kotlin
/**
 * 方式一：标注在类上，记录所有方法
 */
@RestController
@RequestMapping("/api/project")
@AccessLog("项目管理")
class ProjectController {

    @GetMapping
    fun listProjects(): R<List<Project>> {
        return R.ok(projectService.list())
    }

    @PostMapping
    fun createProject(@RequestBody project: Project): R<Project> {
        return R.ok(projectService.save(project))
    }
}

/**
 * 方式二：标注在方法上
 */
@RestController
@RequestMapping("/api/auth")
class AuthController {

    @PostMapping("/login")
    @AccessLog(
        value = "用户登录",
        recordParams = true,  // 记录请求参数
        recordResult = false, // 不记录响应（包含敏感信息）
        recordHeaders = true  // 记录请求头
    )
    fun login(@RequestBody request: LoginRequest): R<LoginResponse> {
        return R.ok(authService.login(request))
    }
}

/**
 * 方式三：不使用注解（自动记录所有 Controller）
 */
@RestController
@RequestMapping("/api/data")
class DataController {
    // 所有方法自动记录访问日志
    @GetMapping
    fun getData(): R<Data> {
        return R.ok(dataService.getData())
    }
}
```

#### 3. 使用 Kotlin 扩展函数（优雅的日志 API）

```kotlin
@Service
class UserService {

    // 使用扩展函数获取 Logger
    private val log = logger()

    fun createUser(user: User): User {
        // DSL 风格的日志（只在 Info 级别启用时才执行）
        log.info { "创建用户: ${user.username}" }
        
        // Debug 日志
        log.debug { "用户详情: ${user.toJson()}" }
        
        // Warn 日志
        if (user.age < 18) {
            log.warn { "用户年龄小于18岁: ${user.username}" }
        }
        
        try {
            return userRepository.save(user)
        } catch (e: Exception) {
            // Error 日志（带异常）
            log.error(e) { "创建用户失败: ${user.username}" }
            throw e
        }
    }

    fun deleteUser(userId: Long) {
        log.info { "删除用户: $userId" }
        userRepository.deleteById(userId)
    }
}
```

#### 4. 彩色日志输出

```kotlin
@Service
class TaskService {

    private val log = logger()

    fun runTask() {
        log.info(ColorLog.green("✓ 任务开始执行"))
        
        try {
            // 执行任务
            processTask()
            log.info(ColorLog.green("✓ 任务执行成功"))
        } catch (e: Exception) {
            log.error(ColorLog.red("✗ 任务执行失败: ${e.message}"))
        }
    }
}
```

#### 5. 自定义日志处理器

```kotlin
/**
 * 自定义日志处理器 - 存储到数据库
 */
@Component
class DatabaseLogHandler(
    private val logRepository: LogRepository
) : LogHandler {

    private val log = logger()

    override fun handle(logRecord: LogRecord) {
        try {
            // 转换为数据库实体
            val entity = LogEntity(
                logId = logRecord.logId,
                logType = logRecord.logType.name,
                appName = logRecord.appName,
                requestUri = logRecord.requestUri,
                requestMethod = logRecord.requestMethod,
                clientIp = logRecord.clientIp,
                userId = logRecord.userId,
                username = logRecord.username,
                executionTime = logRecord.executionTime,
                success = logRecord.success,
                exception = logRecord.exception,
                createTime = logRecord.createTime
            )
            
            // 异步保存到数据库
            logRepository.save(entity)
            
        } catch (e: Exception) {
            log.error(e) { "保存日志到数据库失败" }
        }
    }

    override fun getOrder(): Int = 10
}

/**
 * 自定义日志处理器 - 发送到 MQ
 */
@Component
class MqLogHandler(
    private val rabbitTemplate: RabbitTemplate
) : LogHandler {

    private val log = logger()

    override fun handle(logRecord: LogRecord) {
        try {
            // 发送到消息队列
            rabbitTemplate.convertAndSend(
                "log-exchange",
                "log.${logRecord.logType.name.lowercase()}",
                logRecord.toJson()
            )
        } catch (e: Exception) {
            log.error(e) { "发送日志到MQ失败" }
        }
    }

    override fun getOrder(): Int = 20
}
```

---

## 📊 日志输出示例

### 普通访问日志

```
[访问日志] GET /api/user/list | 用户: admin | IP: 192.168.1.100 | 耗时: 125ms
```

### 慢请求日志

```
╔════════════════════════════════════════════════════════════════
║ ⚠️ 慢请求
║ 请求: GET /api/project/list
║ 用户: admin (1001)
║ IP: 192.168.1.100
║ 耗时: 3500ms
║ 参数: page=1&size=100
╚════════════════════════════════════════════════════════════════
```

### 异常日志

```
╔════════════════════════════════════════════════════════════════
║ ❌ 异常日志
║ 请求: POST /api/user
║ 用户: admin (1001)
║ IP: 192.168.1.100
║ 模块: 用户管理
║ 操作: 新增用户
║ 耗时: 50ms
║ 异常: Duplicate entry 'admin' for key 'username'
╚════════════════════════════════════════════════════════════════
```

---

## 🎯 典型应用场景

### 场景1：用户操作审计

```kotlin
@OperationLog(
    module = "系统设置",
    type = OperationType.UPDATE,
    description = "修改系统配置"
)
fun updateSystemConfig(@RequestBody config: SystemConfig): R<Void> {
    // 记录谁在什么时间修改了什么配置
    systemConfigService.update(config)
    return R.ok()
}
```

### 场景2：敏感操作记录

```kotlin
@OperationLog(
    module = "权限管理",
    type = OperationType.GRANT,
    description = "授予管理员权限",
    recordParams = true,  // 记录参数
    recordResult = true   // 记录结果
)
fun grantAdminRole(@PathVariable userId: Long): R<Void> {
    userService.grantRole(userId, "ROLE_ADMIN")
    return R.ok()
}
```

### 场景3：API 访问监控

```kotlin
// 自动记录所有 Controller 的访问日志
@RestController
@RequestMapping("/api")
class ApiController {
    // 无需添加注解，自动记录访问日志
    
    @GetMapping("/data")
    fun getData(): R<Data> {
        return R.ok(dataService.getData())
    }
}
```

### 场景4：慢请求分析

```yaml
# 配置慢请求阈值为 2 秒
zhiyan:
  log:
    slow-request-threshold: 2000
```

系统会自动标记并突出显示超过阈值的请求，方便性能优化。

---

## 🔧 配置说明

### 操作类型

| 类型 | 说明 | 使用场景 |
|------|------|----------|
| `QUERY` | 查询 | 列表查询、详情查询 |
| `INSERT` | 新增 | 创建资源 |
| `UPDATE` | 更新 | 修改资源 |
| `DELETE` | 删除 | 删除资源 |
| `EXPORT` | 导出 | 导出数据 |
| `IMPORT` | 导入 | 导入数据 |
| `LOGIN` | 登录 | 用户登录 |
| `LOGOUT` | 登出 | 用户登出 |
| `GRANT` | 授权 | 权限授予 |
| `UPLOAD` | 上传 | 文件上传 |
| `DOWNLOAD` | 下载 | 文件下载 |
| `OTHER` | 其他 | 其他操作 |

### 日志类型

| 类型 | 说明 |
|------|------|
| `ACCESS` | 访问日志 - 记录 API 访问 |
| `OPERATION` | 操作日志 - 记录用户操作 |
| `EXCEPTION` | 异常日志 - 记录系统异常 |
| `SYSTEM` | 系统日志 - 系统级日志 |
| `SECURITY` | 安全日志 - 安全相关日志 |

---

## 📚 Kotlin 扩展函数

### Logger 扩展

```kotlin
// 获取 Logger
private val log = logger()

// DSL 风格日志（懒加载）
log.info { "消息: $message" }
log.debug { "详情: ${obj.toJson()}" }
log.warn { "警告: $warning" }
log.error(exception) { "错误: $error" }
```

### LogRecord 扩展

```kotlin
// 转为 JSON
val json = logRecord.toJson()

// 转为简单日志
val simpleLog = logRecord.toSimpleLog()
```

---

## 🔌 集成示例

### 与 Sentinel 集成

```kotlin
@RestController
@RequestMapping("/api/order")
class OrderController {

    @PostMapping
    @SentinelResource(value = "order:create", blockHandler = "createBlockHandler")
    @OperationLog(module = "订单管理", type = OperationType.INSERT, description = "创建订单")
    fun createOrder(@RequestBody order: Order): R<Order> {
        // 同时记录 Sentinel 限流和操作日志
        return R.ok(orderService.create(order))
    }
}
```

### 与数据库集成

```kotlin
@Entity
@Table(name = "sys_log")
data class LogEntity(
    @Id
    var logId: String? = null,
    var logType: String? = null,
    var appName: String? = null,
    var requestUri: String? = null,
    var requestMethod: String? = null,
    var clientIp: String? = null,
    var userId: String? = null,
    var username: String? = null,
    var executionTime: Long? = null,
    var success: Boolean = true,
    var exception: String? = null,
    var createTime: LocalDateTime = LocalDateTime.now()
)

@Component
class DatabaseLogHandler(
    private val logRepository: LogRepository
) : LogHandler {
    override fun handle(logRecord: LogRecord) {
        // 保存到数据库
        val entity = logRecord.toEntity()
        logRepository.save(entity)
    }
}
```

---

## ❓ 常见问题

### 1. 如何禁用某些路径的日志？

```yaml
zhiyan:
  log:
    excluded-paths:
      - /actuator/**
      - /health
      - /your-path/**
```

### 2. 如何只记录操作日志，不记录访问日志？

```yaml
zhiyan:
  log:
    operation-enabled: true
    access-enabled: false
```

### 3. 日志会影响性能吗？

不会。日志处理是异步的（基于 Kotlin 协程），不会阻塞主线程。

### 4. 如何自定义日志格式？

实现 `LogHandler` 接口：

```kotlin
@Component
class CustomLogHandler : LogHandler {
    override fun handle(logRecord: LogRecord) {
        // 自定义处理逻辑
    }
}
```

### 5. Kotlin 代码能在 Java 项目中使用吗？

可以！Kotlin 与 Java 100% 互操作，在 Java 项目中可以直接使用。

---

## 🌈 Kotlin 的优势

### 简洁性对比

**Java 代码**:
```java
@Component
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    public void createUser(User user) {
        if (log.isInfoEnabled()) {
            log.info("创建用户: " + user.getUsername());
        }
    }
}
```

**Kotlin 代码**:
```kotlin
@Component
class UserService {
    private val log = logger()
    
    fun createUser(user: User) {
        log.info { "创建用户: ${user.username}" }
    }
}
```

### Null 安全

```kotlin
// Kotlin 编译期就能发现 NPE
fun getUser(id: Long): User? {
    val user = userRepository.findById(id)
    // 如果不处理 null，编译报错
    return user?.apply {
        log.info { "查询到用户: $username" }
    }
}
```

### 协程优势

```kotlin
// 轻量级异步处理
scope.launch {
    // 异步处理日志，不阻塞主线程
    processLog(logRecord)
}
```

---

## 📖 相关文档

- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)
- [Kotlin 协程指南](https://kotlinlang.org/docs/coroutines-guide.html)
- [Spring Boot Kotlin 支持](https://spring.io/guides/tutorials/spring-boot-kotlin/)

---

**作者**: 智研平台开发团队  
**语言**: Kotlin 2.0  
**更新时间**: 2025-11-01  
**版本**: v1.0.0

