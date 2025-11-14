# 邮件配置说明

## 网易163邮箱配置

### 1. 环境变量设置

为了保护邮箱密码安全，请使用环境变量来配置邮箱信息：

```bash
# Windows (PowerShell)
$env:MAIL_USERNAME="zhiyan163verif@163.com"
$env:MAIL_PASSWORD="your_authorization_code"

# Windows (CMD)
set MAIL_USERNAME=zhiyan163verif@163.com
set MAIL_PASSWORD=your_authorization_code

# Linux/Mac
export MAIL_USERNAME="zhiyan163verif@163.com"
export MAIL_PASSWORD="your_authorization_code"
```

### 2. 163邮箱授权码获取步骤

1. 登录163邮箱网页版
2. 点击右上角"设置" → "POP3/SMTP/IMAP"
3. 开启"SMTP服务"
4. 设置授权码（通常需要手机验证）
5. 记录生成的授权码（16位字符串）

### 3. IDEA/Eclipse 环境变量配置

#### IDEA配置：
1. 打开 Run/Debug Configurations
2. 选择你的应用配置
3. 在 Environment variables 中添加：
   - `MAIL_USERNAME=zhiyan163verif@163.com`
   - `MAIL_PASSWORD=your_authorization_code`

#### Eclipse配置：
1. 右键项目 → Run As → Run Configurations
2. 选择 Environment 选项卡
3. 添加环境变量

### 4. Docker/生产环境配置

```yaml
# docker-compose.yml
services:
  zhiyan-auth-service:
    environment:
      - MAIL_USERNAME=zhiyan163verif@163.com
      - MAIL_PASSWORD=your_authorization_code
```

### 5. 配置文件说明

当前配置已调整为163邮箱：
- SMTP服务器: smtp.163.com
- 端口: 465 (SSL)
- 用户名: 通过环境变量 `MAIL_USERNAME` 设置
- 密码: 通过环境变量 `MAIL_PASSWORD` 设置

### 6. 安全注意事项

⚠️ **重要提醒：**
- 永远不要将真实密码写在配置文件中
- 使用163邮箱的"授权码"而不是登录密码
- 授权码具有独立的权限，更加安全
- 生产环境建议使用密钥管理服务

### 7. 测试邮件发送

启动应用后，可以通过验证码接口测试邮件发送功能：

```bash
curl -X POST http://localhost:8091/api/auth/verification-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","type":"REGISTER"}'
```
