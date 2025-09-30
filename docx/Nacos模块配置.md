# Zhiyan Nacos 服务

智研项目的 Nacos 服务模块，用于微服务的注册与发现。

## 功能说明

1. **服务注册与发现**：向 Nacos 注册中心注册服务，支持其他微服务发现此服务
2. **配置管理**：集成 Nacos 配置中心，支持动态配置
3. **健康检查**：提供健康检查端点，确保服务运行状态
4. **监控端点**：集成 Spring Boot Actuator，提供监控和管理功能

## 配置说明

### 关键配置项

- **服务端口**：8849
- **服务名称**：zhiyan-nacos
- **Nacos 地址**：127.0.0.1:8848
- **命名空间**：zhiyan-dev
- **服务分组**：DEFAULT_GROUP

### 健康检查端点

- `GET /health` - 服务健康状态检查
- `GET /health/info` - 服务详细信息
- `GET /actuator/health` - Spring Boot Actuator 健康检查

## 启动说明

1. 确保 Nacos 服务器已启动并运行在 127.0.0.1:8848
2. 确保 `zhiyan-dev` 命名空间已在 Nacos 控制台中创建
3. 启动服务：`java -jar zhiyan-nacos-0.0.1-SNAPSHOT.jar`
4. 访问健康检查：`http://localhost:8849/health`
5. 在 Nacos 控制台查看服务列表，确认服务已注册

## 故障排除

如果服务无法在 Nacos 中显示，请检查：

1. Nacos 服务器是否正常运行
2. 网络连接是否正常
3. 命名空间 `zhiyan-dev` 是否存在
4. 防火墙是否阻止了 8849 端口
5. 服务启动日志中是否有错误信息
