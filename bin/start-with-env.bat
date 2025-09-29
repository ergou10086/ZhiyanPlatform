@echo off
REM Windows批处理脚本 - 设置环境变量并启动应用

echo 设置环境变量...

REM 数据库配置
set DB_USERNAME=root
set DB_PASSWORD=zjm10086

REM Redis配置  
set REDIS_PASSWORD=zjm10086

REM 邮件配置 - 163邮箱
set MAIL_USERNAME=zhiyan163verif@163.com
set MAIL_PASSWORD=Zjm10086

echo 环境变量设置完成！
echo.
echo 邮件配置:
echo   用户名: %MAIL_USERNAME%
echo   密码: [已设置]
echo.

echo 开始验证环境变量...
echo --------------------------
echo 数据库用户名: %DB_USERNAME%
echo 数据库密码: %DB_PASSWORD%
echo Redis密码: %REDIS_PASSWORD%
echo 邮件用户名: %MAIL_USERNAME%
echo 邮件密码: %MAIL_PASSWORD%  // 这里临时显示密码用于验证，验证后可改回[已设置]
echo --------------------------

REM 启动应用
echo 启动应用...
mvn spring-boot:run

pause
