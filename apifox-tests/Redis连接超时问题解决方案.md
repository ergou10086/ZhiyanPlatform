# Redis连接超时问题解决方案

## 问题描述

应用启动时出现以下错误：
```
org.redisson.client.RedisTimeoutException: Command execution timeout for command: (PING)
Redis client: [addr=redis://10.7.10.98:6379]
```

## 问题原因

1. **环境配置错误**：本地开发时使用了生产环境（prod）配置，尝试连接服务器Redis（`10.7.10.98:6379`）
2. **网络不通**：本地网络（`192.168.1.143`）无法访问服务器网络（`10.7.10.98`）
3. **超时时间太短**：原配置超时时间为5秒，网络延迟高时容易超时

## 解决方案

### 方案1：切换到开发环境（推荐）

**本地开发时使用本地Redis**

1. **修改默认环境配置**

   文件：`zhiyan-common-redis/src/main/resources/application.yml`
   ```yaml
   spring:
     profiles:
       active: ${SPRING_PROFILES_ACTIVE:dev}  # 改为dev
   ```

2. **启动本地Redis**

   Windows:
   ```bash
   # 如果已安装Redis，在Redis安装目录执行
   redis-server.exe redis.windows.conf
   ```

   Docker:
   ```bash
   docker run -d --name redis -p 6379:6379 redis:latest --requirepass zjm10086
   ```

3. **验证本地Redis连接**
   ```bash
   redis-cli -h 127.0.0.1 -p 6379 -a zjm10086
   ping  # 应返回PONG
   ```

### 方案2：通过VPN/跳板机连接服务器Redis

如果必须连接服务器Redis：

1. **建立VPN连接**
   - 连接到公司VPN，确保能访问 `10.7.10.98`

2. **验证网络连通性**
   ```bash
   # Windows PowerShell
   Test-NetConnection -ComputerName 10.7.10.98 -Port 6379
   
   # 或使用telnet
   telnet 10.7.10.98 6379
   ```

3. **使用SSH隧道（端口转发）**
   ```bash
   # 假设跳板机IP为 jump.server.com
   ssh -L 6379:10.7.10.98:6379 user@jump.server.com
   
   # 然后修改配置连接本地6379端口
   ```

### 方案3：增加超时时间（已优化）

**配置文件已优化**：`application-prod.yml`

```yaml
spring:
  data:
    redis:
      timeout: 30000          # 连接超时：30秒
      connect-timeout: 10000  # 命令超时：10秒
      lettuce:
        pool:
          max-wait: 10000     # 连接池等待：10秒
```

### 方案4：使用环境变量动态切换

**IDEA启动配置**：

1. 打开 Run/Debug Configurations
2. 添加环境变量：`SPRING_PROFILES_ACTIVE=dev`
3. 或在VM options中添加：`-Dspring.profiles.active=dev`

**Maven启动**：
```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**JAR包启动**：
```bash
# 开发环境
java -jar -Dspring.profiles.active=dev your-app.jar

# 生产环境  
java -jar -Dspring.profiles.active=prod your-app.jar
```

## 环境配置说明

### 开发环境（dev）
- Redis地址：`127.0.0.1:6379`
- 用于本地开发和测试
- 需要本地安装Redis

### 生产环境（prod）
- Redis地址：`10.7.10.98:6379`
- 用于服务器部署
- 需要在服务器网络内才能访问

## 快速诊断命令

### 1. 检查Redis是否运行
```bash
# Windows
tasklist | findstr redis

# Linux
ps aux | grep redis
netstat -tlnp | grep 6379
```

### 2. 检查网络连通性
```bash
# Ping测试
ping 10.7.10.98

# 端口测试（PowerShell）
Test-NetConnection -ComputerName 10.7.10.98 -Port 6379

# Telnet测试
telnet 10.7.10.98 6379
```

### 3. 测试Redis连接
```bash
# 本地Redis
redis-cli -h 127.0.0.1 -p 6379 -a zjm10086 ping

# 远程Redis
redis-cli -h 10.7.10.98 -p 6379 -a zjm10086 ping
```

### 4. 查看应用健康状态
```bash
# 访问健康检查端点
curl http://localhost:8099/actuator/health

# 查看Redis健康状态
curl http://localhost:8099/actuator/health/redis
```

## 常见错误及解决

### 错误1：连接被拒绝（Connection refused）
```
原因：Redis服务未启动或端口不正确
解决：启动Redis服务，检查端口配置
```

### 错误2：连接超时（Connection timeout）
```
原因：网络不通或防火墙阻止
解决：检查网络连通性，配置防火墙规则
```

### 错误3：认证失败（Authentication failed）
```
原因：密码错误或Redis未配置密码
解决：检查配置文件中的密码设置
```

### 错误4：执行超时（Command execution timeout）
```
原因：网络延迟高或Redis负载过大
解决：增加超时时间，优化Redis性能
```

## 最佳实践

1. **环境隔离**
   - 本地开发使用 dev 环境
   - 服务器部署使用 prod 环境
   - 通过环境变量控制切换

2. **配置分离**
   - 敏感信息（如密码）使用环境变量
   - 不同环境使用不同配置文件

3. **健康检查**
   - 启用 Actuator 健康检查
   - 监控 Redis 连接状态

4. **连接池优化**
   - 根据业务量调整连接池大小
   - 设置合理的超时时间
   - 开启连接有效性检查

5. **错误处理**
   - 捕获Redis异常并优雅降级
   - 记录详细的错误日志
   - 提供备用方案（如本地缓存）

## 服务器部署检查清单

部署到服务器前确认：

- [ ] Redis服务已启动
- [ ] 防火墙规则已配置（开放6379端口）
- [ ] Redis密码已配置
- [ ] 应用环境变量设置为 `prod`
- [ ] 网络连通性已测试
- [ ] 健康检查端点可访问
- [ ] 日志目录有写入权限

## 监控和告警

建议监控以下指标：

1. **连接状态**：Redis连接池使用率
2. **响应时间**：命令执行耗时
3. **错误率**：连接失败/超时次数
4. **内存使用**：Redis内存占用
5. **网络流量**：进出Redis的流量

## 联系支持

如果问题仍未解决，请提供以下信息：

1. 完整的错误日志
2. 应用配置文件（脱敏后）
3. 网络环境描述
4. Redis版本和配置
5. 操作系统和Java版本

---

**更新日期**：2025-10-23  
**文档版本**：v1.0

