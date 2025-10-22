# Redis 连接失败解决方案

## 🔴 错误信息
```
Unable to connect to Redis server: localhost/127.0.0.1:6379
```

---

## ✅ 解决方案（按推荐顺序）

### 方案1：启动 Redis 服务（推荐）

#### Windows 系统：
```powershell
# 如果已安装 Redis，启动服务
redis-server

# 或者通过 Windows 服务启动
# 1. Win + R 输入 services.msc
# 2. 找到 Redis 服务
# 3. 右键点击"启动"
```

#### Linux/Mac 系统：
```bash
# 启动 Redis
redis-server

# 或者使用系统服务
sudo systemctl start redis
# 或
sudo service redis start
```

#### 使用 Docker 启动 Redis（推荐）：
```bash
# 快速启动 Redis 容器
docker run -d --name redis -p 6379:6379 redis:latest

# 验证 Redis 是否启动成功
docker ps | grep redis
```

---

### 方案2：临时禁用 Redis（仅开发测试用）

如果暂时不需要 Redis 功能，可以在配置文件中禁用。

**修改 `application.yml` 或 `application-dev.yml`：**

```yaml
spring:
  data:
    redis:
      enabled: false  # 禁用 Redis
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration
      - org.redisson.spring.starter.RedissonAutoConfigurationV2
```

**⚠️ 注意**：禁用 Redis 可能会影响以下功能：
- 分布式缓存
- Session 共享
- 分布式锁
- 消息队列

---

### 方案3：修改 Redis 连接配置

如果 Redis 运行在其他地址或端口：

```yaml
spring:
  data:
    redis:
      host: your-redis-host  # 修改为实际地址
      port: 6379
      password: your-password  # 如果有密码
      timeout: 3000
```

---

## 🔍 验证 Redis 是否启动

### 方法1：使用 redis-cli
```bash
redis-cli ping
# 如果返回 PONG，说明 Redis 正常运行
```

### 方法2：使用 telnet
```bash
telnet localhost 6379
```

### 方法3：检查端口占用（Windows）
```powershell
netstat -ano | findstr 6379
```

### 方法4：检查端口占用（Linux/Mac）
```bash
lsof -i:6379
# 或
netstat -tuln | grep 6379
```

---

## 📦 快速安装 Redis

### Windows：
1. 下载 Redis for Windows：
   - GitHub: https://github.com/tporadowski/redis/releases
   - 或使用 Chocolatey: `choco install redis-64`

2. 解压后运行 `redis-server.exe`

### Linux (Ubuntu/Debian)：
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis
sudo systemctl enable redis  # 开机自启
```

### Mac：
```bash
brew install redis
brew services start redis
```

### Docker（推荐，跨平台）：
```bash
# 启动 Redis
docker run -d \
  --name zhiyan-redis \
  -p 6379:6379 \
  -v redis-data:/data \
  redis:latest \
  redis-server --appendonly yes

# 查看日志
docker logs -f zhiyan-redis

# 停止 Redis
docker stop zhiyan-redis

# 重启 Redis
docker start zhiyan-redis
```

---

## 🎯 推荐做法

**对于开发环境：**
使用 Docker 启动 Redis（简单、隔离、易管理）

```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

**对于生产环境：**
使用系统服务方式安装并配置持久化、密码等安全选项。

---

## ✅ 启动顺序

正确的微服务启动顺序：
1. ✅ **Nacos** - 服务注册中心（已启动）
2. ✅ **Redis** - 缓存服务（**需要启动**）
3. ✅ **MySQL** - 数据库服务
4. 启动各个微服务模块

---

## 🐛 常见问题

### Q1: Redis 启动后立即退出？
**A**: 检查端口是否被占用，或者查看 Redis 日志

### Q2: Redis 连接超时？
**A**: 检查防火墙设置，确保 6379 端口开放

### Q3: Docker 中的 Redis 无法连接？
**A**: 确保端口映射正确：`-p 6379:6379`

---

**选择一种方案后，重新启动项目即可！** 🚀

