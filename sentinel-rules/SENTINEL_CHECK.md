# Sentinel 配置完整性检查清单

## ✅ 已完成的配置

### 1. Maven 依赖
- ✅ `pom.xml` 已添加 `zhiyan-common-sentinel` 依赖

### 2. 配置文件
- ✅ `application-prod.yml` 配置完整
  - `spring.cloud.sentinel` - Spring Cloud Sentinel 配置
  - `zhiyan.sentinel` - 自定义 Sentinel 配置
  - Nacos 数据源配置完整

### 3. 自动配置
- ✅ `spring.factories` 存在于 `zhiyan-common-sentinel`
- ✅ 自动配置类 `SentinelAutoConfiguration` 已注册

### 4. Nacos 规则
- ✅ 流控规则已上传到 Nacos
- ✅ Dashboard 已配置

---

## 🔍 Sentinel 是否真正启用？验证步骤

### 步骤 1: 重新编译项目

**重要！添加依赖后必须重新编译！**

#### IDEA 中操作：
```bash
# 方法 1: Maven 刷新
右键点击项目 → Maven → Reload Project

# 方法 2: 清理并重新编译
mvn clean install -DskipTests
```

#### 命令行操作：
```bash
cd zhiyan-modules/zhiyan-project
mvn clean compile
```

---

### 步骤 2: 启动应用并查看日志

启动 `zhiyan-project` 服务，观察启动日志中**必须出现**以下内容：

#### ✅ 正确的启动日志应该包含：

```log
# 1. Sentinel 自动配置加载
[Sentinel] 注册 SentinelResourceAspect 切面
[Sentinel] 注册默认限流降级处理器
[Sentinel] 注册全局限流异常处理器
[Sentinel] 注册 Sentinel 规则提供者
[Sentinel] 注册 Sentinel 初始化运行器

# 2. Sentinel 初始化
========================================
    Sentinel 流控保护模块初始化
========================================
[Sentinel] Dashboard 地址: localhost:8858
[Sentinel] 客户端通信端口: 8721
[Sentinel] 日志目录: logs/sentinel

# 3. 从 Nacos 加载规则
[Sentinel] 从 Nacos 加载规则配置...
[Sentinel] 加载流控规则，dataId: zhiyan-project-flow-rules.json
[Sentinel] 流控规则加载成功，规则数量: 10
[Sentinel] 加载降级规则，dataId: zhiyan-project-degrade-rules.json
[Sentinel] 降级规则加载成功，规则数量: 4
[Sentinel] 加载系统规则，dataId: zhiyan-project-system-rules.json
[Sentinel] 系统规则加载成功，规则数量: 1
[Sentinel] Nacos 规则配置加载成功

# 4. 初始化完成
========================================
  Sentinel 初始化完成
  应用名称: zhiyan-project
  Dashboard: localhost:8858
  客户端端口: 8721
  饥饿加载: true
  Nacos数据源: 已启用
========================================
```

#### ❌ 如果没有这些日志，说明 Sentinel 没有启用！

---

### 步骤 3: 验证 Sentinel Dashboard 连接

#### 3.1 启动 Sentinel Dashboard
```bash
# 确保 Dashboard 正在运行
java -Dserver.port=8858 -jar sentinel-dashboard.jar
```

#### 3.2 触发接口调用
**重要：Sentinel 采用懒加载机制，必须先访问接口！**

```bash
# 访问任意项目接口
curl http://localhost:8095/api/projects

# 或在浏览器访问
http://localhost:8095/api/projects
```

#### 3.3 查看 Dashboard
访问：`http://localhost:8858`

**应该看到：**
- 左侧菜单出现 `zhiyan-project` 服务
- 点击查看实时监控、流控规则、降级规则

---

### 步骤 4: 验证规则是否生效

#### 方法 1: 通过 Sentinel API 查询
```bash
# 查询流控规则
curl http://localhost:8721/getRules?type=flow

# 查询降级规则
curl http://localhost:8721/getRules?type=degrade

# 查询系统规则
curl http://localhost:8721/getRules?type=system
```

**期望输出：**
```json
[
  {
    "resource": "/api/projects",
    "limitApp": "default",
    "grade": 1,
    "count": 100,
    ...
  }
]
```

#### 方法 2: 测试限流效果
```bash
# 使用 ab 工具压测（需要安装 Apache Bench）
ab -n 1000 -c 50 http://localhost:8095/api/projects

# 观察是否有请求被限流（返回 429 或限流提示）
```

---

### 步骤 5: 检查 Sentinel 日志

查看 Sentinel 生成的日志文件：

```bash
# 进入日志目录
cd logs/sentinel/

# 查看限流记录
cat sentinel-block.log

# 查看通用记录
cat sentinel-record.log
```

---

## 🚨 常见问题排查

### 问题 1: 启动日志中没有 Sentinel 相关内容

**可能原因：**
1. ❌ Maven 依赖没有刷新
2. ❌ 项目没有重新编译
3. ❌ `zhiyan.sentinel.enabled` 配置为 `false`

**解决方案：**
```bash
# 1. 清理并重新编译
mvn clean install -DskipTests

# 2. 检查配置
确认 application-prod.yml 中:
zhiyan:
  sentinel:
    enabled: true  # 必须为 true
```

---

### 问题 2: 规则加载失败

**启动日志显示：**
```log
[Sentinel] Nacos 规则配置加载失败: Client not connected
```

**可能原因：**
1. ❌ Nacos 服务未启动
2. ❌ Nacos 地址配置错误
3. ❌ Nacos 中没有创建规则配置

**解决方案：**

#### 检查 Nacos 连接
```bash
# 测试 Nacos 是否可访问
curl http://10.7.10.98:8848/nacos/v1/console/health/readiness
```

#### 检查 Nacos 配置是否存在
登录 Nacos 控制台：`http://10.7.10.98:8848/nacos`

确认以下配置存在：
- **Data ID**: `zhiyan-project-flow-rules.json`
- **Group**: `SENTINEL_GROUP`
- **Namespace**: `3936229d-c8b3-4947-9192-6b984dca44bf`

---

### 问题 3: Dashboard 看不到服务

**可能原因：**
1. ❌ 没有触发任何接口调用（懒加载）
2. ❌ 客户端端口被占用
3. ❌ Dashboard 地址配置错误

**解决方案：**
```bash
# 1. 检查端口是否被占用
netstat -ano | findstr 8721

# 2. 触发接口调用
curl http://localhost:8095/api/projects

# 3. 检查 Dashboard 配置
确认 application-prod.yml 中:
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8858
        port: 8721
```

---

### 问题 4: 规则不生效

**可能原因：**
1. ❌ 接口路径不匹配
2. ❌ 规则配置错误
3. ❌ QPS 阈值设置过高

**解决方案：**

#### 检查资源名称
Sentinel 默认的资源名称格式：`HTTP方法:URL路径`

例如：
- `GET:/api/projects` ✅
- `/api/projects` ❌（缺少 HTTP 方法）

#### 调整规则进行测试
在 Nacos 中修改流控规则，将 `count` 设置为 `1`：
```json
{
  "resource": "GET:/api/projects",
  "count": 1
}
```

然后快速连续访问两次接口，第二次应该被限流。

---

## 📋 完整检查清单

### 配置文件检查
- [ ] `pom.xml` 包含 `zhiyan-common-sentinel` 依赖
- [ ] `application-prod.yml` 配置 `spring.cloud.sentinel`
- [ ] `application-prod.yml` 配置 `zhiyan.sentinel`
- [ ] `zhiyan.sentinel.enabled=true`
- [ ] `zhiyan.sentinel.nacos.enabled=true`
- [ ] Nacos 地址、命名空间、用户名密码正确

### Nacos 规则检查
- [ ] 登录 Nacos 控制台成功
- [ ] 存在 `zhiyan-project-flow-rules.json` (Group: SENTINEL_GROUP)
- [ ] 存在 `zhiyan-project-degrade-rules.json` (Group: SENTINEL_GROUP)
- [ ] 存在 `zhiyan-project-system-rules.json` (Group: SENTINEL_GROUP)
- [ ] JSON 格式正确，无语法错误

### 运行时检查
- [ ] Maven 依赖已刷新
- [ ] 项目已重新编译
- [ ] 使用 `-Dspring.profiles.active=prod` 启动
- [ ] 启动日志包含 Sentinel 初始化信息
- [ ] 启动日志显示规则加载成功
- [ ] Dashboard 正在运行（端口 8858）
- [ ] 已触发至少一次接口调用
- [ ] Dashboard 中能看到 `zhiyan-project` 服务

### 功能验证
- [ ] `/getRules?type=flow` 返回流控规则
- [ ] `/getRules?type=degrade` 返回降级规则
- [ ] 压测触发限流，返回限流提示
- [ ] `logs/sentinel/` 目录存在且有日志文件
- [ ] Dashboard 实时监控有数据

---

## 🎯 快速验证命令

将以下命令复制到终端，一键验证：

```bash
# 1. 重新编译
cd zhiyan-modules/zhiyan-project && mvn clean compile

# 2. 检查 Nacos 连接
curl http://10.7.10.98:8848/nacos/v1/console/health/readiness

# 3. 启动服务（在 IDEA 中启动，或使用命令行）
# 确保使用 prod profile: -Dspring.profiles.active=prod

# 4. 触发接口
curl http://localhost:8095/api/projects

# 5. 查询规则
curl http://localhost:8721/getRules?type=flow

# 6. 查看 Dashboard
# 浏览器访问: http://localhost:8858
```

---

## 📞 如果还是不行

**请提供以下信息：**
1. 完整的启动日志（前 100 行）
2. `mvn dependency:tree` 输出
3. 是否看到 Sentinel 相关日志
4. Nacos 控制台截图
5. 当前使用的 profile（dev 还是 prod）

**然后我可以帮你具体诊断问题所在。**

