# IDE 运行时 Classpath 问题快速修复

## 问题

应用编译成功，但运行时找不到 `LogAutoConfiguration.class`，这是 IDE 运行时 classpath 未更新的问题。

## 快速修复步骤（IntelliJ IDEA）

### 步骤 1: 重新加载 Maven 项目

1. 打开右侧边栏的 **Maven** 工具窗口
2. 点击顶部工具栏的 **刷新按钮**（🔄 Reload All Maven Projects）
3. 等待 Maven 重新下载和解析依赖

### 步骤 2: 清理并重建项目

1. 点击菜单 **Build** → **Rebuild Project**
2. 或使用快捷键：`Ctrl + Shift + F9` (Windows/Linux) 或 `Cmd + Shift + F9` (Mac)

### 步骤 3: 清理 IDE 缓存（如果步骤 2 无效）

1. 点击菜单 **File** → **Invalidate Caches...**
2. 选择 **Invalidate and Restart**
3. 等待 IDE 重启并重新索引

### 步骤 4: 检查运行配置

1. 点击菜单 **Run** → **Edit Configurations...**
2. 选择你的运行配置
3. 检查 **Use classpath of module** 是否选择了 `zhiyan-project`
4. 检查 **JRE** 配置是否正确

### 步骤 5: 如果以上都无效，删除并重新创建运行配置

1. 删除现有的运行配置
2. 右键 `ZhiyanProjectApplication.java` → **Run 'ZhiyanProjectApplication.main()'**
3. IDE 会自动创建新的运行配置

## 验证依赖

在终端中执行以下命令，确认依赖已正确安装：

```bash
# 检查本地 Maven 仓库中是否有 jar 文件
Test-Path "D:\java\workspace\maven\repository\hbnu\project\zhiyan-common-log\0.0.1-SNAPSHOT\zhiyan-common-log-0.0.1-SNAPSHOT.jar"

# 检查 jar 文件内容
cd D:\java\workspace\maven\repository\hbnu\project\zhiyan-common-log\0.0.1-SNAPSHOT
jar -tf zhiyan-common-log-0.0.1-SNAPSHOT.jar | Select-String "LogAutoConfiguration"
```

应该看到 `hbnu/project/common/log/config/LogAutoConfiguration.class`。

## 临时解决方案（不推荐）

如果上述方法都不行，可以 temporarily 在 `application.yml` 中禁用日志自动配置：

```yaml
spring:
  autoconfigure:
    exclude:
      - hbnu.project.common.log.config.LogAutoConfiguration
```

**注意**：这会禁用日志功能，只适用于临时测试。

## 根本解决方案

确保以下操作已完成：

1. ✅ `zhiyan-common-log` 模块已重新编译并安装到本地 Maven 仓库
2. ✅ `zhiyan-project` 模块已重新编译
3. ✅ IDE 已重新加载 Maven 项目
4. ✅ IDE 缓存已清理

## 相关文档

- `ZhiyanPlatform/IDE_RUNTIME_CLASSPATH_FIX.md` - 详细的 IDE classpath 修复指南
- `ZhiyanPlatform/LOG_AUTOCONFIGURATION_FIX.md` - 编译问题修复文档



