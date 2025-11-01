## 模块的启动中添加参数
### 虚拟机选项
```bash
-javaagent:D:\OtherLanguageSetting\SkyWalking\apache-skywalking-apm-10.2.0\apache-skywalking-apm-bin\agent\skywalking-agent.jar
-Dskywalking.agent.service_name=      # 微服务名称（自定义，如order-service）
-Dskywalking.collector.backend_service=localhost:11800  # Windows本地OAP地址，默认端口11800
```
-javaagent:D:\OtherLanguageSetting\SkyWalking\apache-skywalking-apm-10.2.0\apache-skywalking-apm-bin\agent\skywalking-agent.jar -Dskywalking.agent.service_name=zhiyan-project -Dskywalking.collector.backend_service=localhost:11800
