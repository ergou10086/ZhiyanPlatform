#!/bin/bash
# Linux/Mac启动脚本 - 设置环境变量并启动应用

echo "设置环境变量..."

# 数据库配置
export DB_USERNAME="root"
export DB_PASSWORD="zjm10086"

# Redis配置
export REDIS_PASSWORD="zjm10086"

# 邮件配置 - 163邮箱
export MAIL_USERNAME="zhiyan163verif@163.com"
export MAIL_PASSWORD="Zjm10086"

echo "环境变量设置完成！"
echo ""
echo "邮件配置:"
echo "  用户名: $MAIL_USERNAME"
echo "  密码: [已设置]"
echo ""

# 启动应用
echo "启动应用..."
mvn spring-boot:run
