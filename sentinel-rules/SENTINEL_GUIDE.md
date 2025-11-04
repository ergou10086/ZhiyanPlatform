# zhiyan-project 模块 Sentinel 流控配置指南

## 📋 目录
- [配置原理](#配置原理)
- [自动加载流程](#自动加载流程)
- [Nacos 规则配置](#nacos-规则配置)
- [规则说明](#规则说明)
- [使用示例](#使用示例)
- [监控与调试](#监控与调试)

---

## 🔧 配置原理

### 1. 整体架构

```
zhiyan-project 启动
    ↓
加载 zhiyan-common-sentinel 依赖
    ↓
SentinelAutoConfiguration 自动配置
    ↓
SentinelInitRunner 启动时运行
    ↓
SentinelRuleProvider 从 Nacos 加载规则
    ↓
规则动态监听并应用
```

### 2. 关键配置文件

#### application-prod.yml
```yaml
zhiyan:
  sentinel:
    enabled: true                    # 启用 Sentinel
    eager: true                      # 饥饿加载，启动时立即初始化
    dashboard:
      host: localhost
      port: 8858
      client-port: 8721             # 与 Dashboard 通信端口
    nacos:
      enabled: true                  # 启用 Nacos 数据源
      server-addr: 10.7.10.98:8848  # Nacos 地址
      namespace: 3936229d-c8b3-4947-9192-6b984dca44bf
      group-id: SENTINEL_GROUP       # 规则分组
      username: nacos
      password: nacos
      data-id-suffix: json           # 规则文件后缀
```

---

## 🚀 自动加载流程

### 流程详解

#### 步骤 1: Spring Boot 启动
```java
@SpringBootApplication
public class ZhiyanProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhiyanProjectApplication.class, args);
    }
}
```

#### 步骤 2: 自动配置触发
Spring Boot 通过 `spring.factories` 自动扫描并加载：
```
zhiyan-common-sentinel/src/main/resources/META-INF/spring.factories
```

内容：
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  hbnu.project.common.sentinel.config.SentinelAutoConfiguration
```

#### 步骤 3: 注册 Bean
`SentinelAutoConfiguration` 注册以下 Bean：
1. **SentinelResourceAspect**: 使 `@SentinelResource` 注解生效
2. **SentinelRuleProvider**: 规则加载器
3. **SentinelInitRunner**: 初始化运行器
4. **GlobalBlockExceptionHandler**: 全局异常处理

#### 步骤 4: 启动时初始化
`SentinelInitRunner` 实现了 `ApplicationRunner` 接口，在应用启动后自动执行：

```java
@Override
public void run(ApplicationArguments args) {
    // 1. 获取应用名称
    String appName = environment.getProperty("spring.application.name");
    // appName = "zhiyan-project"
    
    // 2. 配置 Dashboard 连接
    configureDashboard(appName);
    
    // 3. 从 Nacos 加载规则
    loadRules(appName);
}
```

#### 步骤 5: 从 Nacos 加载规则
`SentinelRuleProvider.loadRulesFromNacos()` 方法：

```java
public void loadRulesFromNacos(String appName) {
    // appName = "zhiyan-project"
    
    // 1. 加载流控规则
    // dataId = "zhiyan-project-flow-rules.json"
    loadFlowRulesFromNacos(appName, nacos);
    
    // 2. 加载降级规则
    // dataId = "zhiyan-project-degrade-rules.json"
    loadDegradeRulesFromNacos(appName, nacos);
    
    // 3. 加载系统规则
    // dataId = "zhiyan-project-system-rules.json"
    loadSystemRulesFromNacos(appName, nacos);
}
```

#### 步骤 6: 创建 Nacos 数据源
```java
private void loadFlowRulesFromNacos(String appName, SentinelProperties.Nacos nacos) {
    // 构建 dataId
    String dataId = appName + "-flow-rules." + nacos.getDataIdSuffix();
    // dataId = "zhiyan-project-flow-rules.json"
    
    // 创建 NacosDataSource
    ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = 
        new NacosDataSource<>(
            nacos.getServerAddr(),    // 10.7.10.98:8848
            nacos.getGroupId(),       // SENTINEL_GROUP
            dataId,                   // zhiyan-project-flow-rules.json
            source -> JSON.parseObject(source, new TypeReference<>() {})
        );
    
    // 注册到 FlowRuleManager（自动监听 Nacos 配置变化）
    FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    
    // 加载初始规则
    List<FlowRule> rules = flowRuleDataSource.loadConfig();
}
```

---

## 📝 Nacos 规则配置

### 如何在 Nacos 中配置规则

#### 1. 登录 Nacos 控制台
访问：`http://10.7.10.98:8848/nacos`
- 用户名：nacos
- 密码：nacos

#### 2. 创建配置
进入 **配置管理 → 配置列表**，点击 **"+"** 创建配置：

**流控规则配置：**
- **Data ID**: `zhiyan-project-flow-rules.json`
- **Group**: `SENTINEL_GROUP`
- **配置格式**: `JSON`
- **配置内容**: 参考 `docx/sentinel-rules/zhiyan-project-flow-rules.json`

**降级规则配置：**
- **Data ID**: `zhiyan-project-degrade-rules.json`
- **Group**: `SENTINEL_GROUP`
- **配置格式**: `JSON`
- **配置内容**: 参考 `docx/sentinel-rules/zhiyan-project-degrade-rules.json`

**系统规则配置：**
- **Data ID**: `zhiyan-project-system-rules.json`
- **Group**: `SENTINEL_GROUP`
- **配置格式**: `JSON`
- **配置内容**: 参考 `docx/sentinel-rules/zhiyan-project-system-rules.json`

#### 3. 规则自动生效
配置保存后，Sentinel 会：
- ✅ 自动监听 Nacos 配置变化
- ✅ 实时拉取最新规则
- ✅ 动态更新规则（无需重启服务）

---

## 📊 规则说明

### 流控规则 (Flow Rules)

#### 规则字段说明
```json
{
  "resource": "/api/projects",       // 资源名称（接口路径）
  "limitApp": "default",             // 来源应用（default表示所有）
  "grade": 1,                        // 限流阈值类型（0=线程数，1=QPS）
  "count": 100,                      // 限流阈值
  "strategy": 0,                     // 流控模式（0=直接，1=关联，2=链路）
  "controlBehavior": 0,              // 流控效果（0=快速失败，1=Warm Up，2=排队等待）
  "clusterMode": false               // 是否集群模式
}
```

#### 项目关键接口限流配置

| 接口 | QPS 限制 | 说明 |
|------|---------|------|
| `GET /api/projects` | 100 | 查询项目列表 |
| `POST /api/projects` | 20 | 创建项目（限制频繁创建） |
| `PUT /api/projects/*` | 50 | 更新项目 |
| `DELETE /api/projects/*` | 20 | 删除项目（敏感操作） |
| `GET /api/projects/tasks` | 200 | 查询任务列表 |
| `POST /api/projects/tasks` | 30 | 创建任务 |
| `PUT /api/projects/tasks/*` | 50 | 更新任务 |
| `POST /api/projects/upload-image` | 10 | 上传图片（资源密集型） |
| `GET /api/projects/members` | 100 | 查询成员列表 |
| `POST /api/projects/members/invite` | 10 | 邀请成员（防止滥用） |

### 降级规则 (Degrade Rules)

#### 规则字段说明
```json
{
  "resource": "/api/projects",       // 资源名称
  "grade": 0,                        // 降级策略（0=慢调用比例，1=异常比例，2=异常数）
  "count": 1.0,                      // 阈值（秒或比例）
  "timeWindow": 10,                  // 熔断时长（秒）
  "minRequestAmount": 5,             // 最小请求数
  "slowRatioThreshold": 0.5,         // 慢调用比例阈值
  "statIntervalMs": 1000             // 统计时长（毫秒）
}
```

#### 降级策略说明

**慢调用比例模式 (grade=0)**
- `/api/projects`: 响应时间 > 1秒，且慢调用比例 > 50%，触发熔断 10秒
- `/api/projects/tasks`: 响应时间 > 1.5秒，且慢调用比例 > 50%，触发熔断 10秒

**异常数模式 (grade=2)**
- `/api/projects/upload-image`: 异常数 > 10%，触发熔断 30秒
- `POST:/api/projects`: 异常数 > 5%，触发熔断 30秒

### 系统规则 (System Rules)

```json
{
  "avgRt": 2000,              // 平均响应时间阈值（毫秒）
  "maxThread": -1,            // 最大并发线程数（-1 表示不限制）
  "qps": -1,                  // 全局 QPS 限制（-1 表示不限制）
  "highestSystemLoad": -1,    // 系统负载阈值（-1 表示不限制）
  "highestCpuUsage": 0.8      // CPU 使用率阈值（80%）
}
```

---

## 💡 使用示例

### 1. 在 Controller 中使用 @SentinelResource

```java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @GetMapping("/{id}")
    @SentinelResource(
        value = "getProjectById",
        blockHandler = "handleBlock",
        fallback = "handleFallback"
    )
    public R<Project> getProject(@PathVariable Long id) {
        return projectService.getById(id);
    }
    
    /**
     * 限流处理方法
     */
    public R<Project> handleBlock(Long id, BlockException ex) {
        log.warn("接口被限流: {}", ex.getMessage());
        return R.fail("系统繁忙，请稍后再试");
    }
    
    /**
     * 降级处理方法
     */
    public R<Project> handleFallback(Long id, Throwable ex) {
        log.error("接口降级: {}", ex.getMessage());
        return R.fail("服务暂时不可用");
    }
}
```

### 2. 自动 URL 模式匹配

由于配置了 `spring.cloud.sentinel.web-context-unify: false`，Sentinel 会自动为所有 HTTP 接口创建资源：

```
资源名称格式：HTTP方法:URL路径
示例：
- GET:/api/projects
- POST:/api/projects
- GET:/api/projects/{id}
- PUT:/api/projects/{id}
```

### 3. 编程式流控

```java
@Service
public class ProjectService {

    public void createProject(Project project) {
        Entry entry = null;
        try {
            // 定义资源
            entry = SphU.entry("createProject");
            
            // 业务逻辑
            projectRepository.save(project);
            
        } catch (BlockException e) {
            // 被限流或降级
            log.warn("创建项目被限流");
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
```

---

## 🔍 监控与调试

### 1. Sentinel Dashboard

访问：`http://localhost:8858`

**功能：**
- 📊 实时监控流量、QPS、响应时间
- ⚙️ 动态配置流控、降级规则
- 📈 查看规则效果
- 🔔 查看限流降级事件

### 2. 日志查看

**Sentinel 日志位置：**
```
logs/sentinel/
├── sentinel-record.log     # 流控记录
├── sentinel-block.log      # 限流日志
└── command-center.log      # 命令中心日志
```

**应用日志：**
```
logs/zhiyan-project-service.log
```

查看启动日志：
```
[Sentinel] 加载流控规则，dataId: zhiyan-project-flow-rules.json
[Sentinel] 流控规则加载成功，规则数量: 10
[Sentinel] 降级规则加载成功，规则数量: 4
[Sentinel] 系统规则加载成功，规则数量: 1
```

### 3. 调试技巧

#### 验证规则是否生效
```bash
# 查看当前流控规则
curl http://localhost:8721/getRules?type=flow

# 查看降级规则
curl http://localhost:8721/getRules?type=degrade

# 查看系统规则
curl http://localhost:8721/getRules?type=system
```

#### 测试限流效果
```bash
# 使用 ab 工具进行压测
ab -n 1000 -c 10 http://localhost:8095/api/projects

# 或使用 JMeter 进行压测
```

---

## 🎯 最佳实践

### 1. 合理设置 QPS 阈值

**推荐策略：**
- **查询接口**: 根据实际并发量设置（如 100-200 QPS）
- **写入接口**: 适当降低（如 20-50 QPS）
- **资源密集型接口**: 严格限制（如 10-20 QPS）

### 2. 配置降级规则

**建议：**
- 为关键接口配置慢调用降级
- 为不稳定的第三方调用配置异常降级
- 设置合理的熔断时长（10-60秒）

### 3. 系统保护

**CPU 使用率 > 80%** 时触发系统保护，自动限流，防止系统崩溃。

### 4. 动态调整规则

在 Nacos 中修改规则后，无需重启服务，规则会自动生效。

---

## 🚨 常见问题

### Q1: 规则没有生效？

**检查清单：**
1. ✅ 确认 Nacos 连接正常
2. ✅ 检查 dataId 和 group 是否正确
3. ✅ 查看应用日志是否有加载成功日志
4. ✅ 确认 `zhiyan.sentinel.nacos.enabled=true`

### Q2: Dashboard 看不到服务？

**解决方案：**
1. 确认 `spring.cloud.sentinel.transport.dashboard` 配置正确
2. 确认客户端端口 `8721` 未被占用
3. 触发一次接口调用（Sentinel 懒加载机制）

### Q3: 规则更新不生效？

**原因：**
- Nacos 数据源连接断开
- dataId 或 group 配置错误

**解决：**
- 重启服务
- 检查 Nacos 连接配置

---

## 📚 相关文档

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/)
- [Nacos 配置中心](https://nacos.io/zh-cn/docs/config.html)
- [项目 Sentinel 通用配置](../../zhiyan-common/zhiyan-common-sentinel/Sentinel_README.md)

---

## 📝 总结

**zhiyan-project 模块通过以下方式读取 Nacos 流控规则：**

1. **启动时自动初始化**: `SentinelInitRunner` 在应用启动后自动执行
2. **读取配置**: 从 `application-prod.yml` 读取 Nacos 连接信息
3. **构建 dataId**: 根据应用名称生成规则 dataId（如 `zhiyan-project-flow-rules.json`）
4. **创建数据源**: 使用 `NacosDataSource` 连接 Nacos 并加载规则
5. **注册监听**: 自动监听 Nacos 配置变化，实时更新规则
6. **应用规则**: 规则生效，保护接口

**优势：**
- ✅ 零代码侵入（自动配置）
- ✅ 动态更新（无需重启）
- ✅ 统一管理（Nacos 配置中心）
- ✅ 实时监控（Sentinel Dashboard）

