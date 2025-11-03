# 任务API错误修复指南

## 🐛 错误概述

### 错误1：更新任务状态 400 Bad Request
```
PATCH /zhiyan/api/projects/tasks/{taskId}/status 400
```

### 错误2：分配任务 500 Internal Server Error
```
PUT /zhiyan/api/projects/tasks/{taskId}/assign 500
```

## 🔍 问题分析

### 问题1：状态更新400错误

**根本原因**：前后端状态格式不匹配

**后端期望**：
```java
// UpdateTaskStatusRequest.java
public class UpdateTaskStatusRequest {
    @NotNull
    private TaskStatus status;  // 枚举值：TODO, IN_PROGRESS, BLOCKED, DONE
}
```

**前端发送**（修复前）：
```javascript
{ status: "待接取" }  // ❌ 中文字符串
```

**修复**：前端需要先转换为枚举值
```javascript
const statusValue = this.getStatusValue("待接取")  // 返回 "TODO"
await taskAPI.updateTaskStatus(taskId, statusValue)
```

### 问题2：分配任务500错误

**可能原因**：
1. 前端发送的ID格式问题
2. 数据库JSON列的格式问题
3. ObjectMapper配置问题

**检查步骤**：
1. 查看后端日志中的详细错误堆栈
2. 检查数据库中 `tasks` 表的 `assignee_id` 列数据格式

## ✅ 修复方案

### 方案1：修复状态转换

前端代码已有转换逻辑，但需要确保映射正确：

```javascript
// ProjectDetail.vue
getStatusValue(status) {
  const reverseMap = {
    '待接取': 'TODO',          // ✅ 正确
    '进行中': 'IN_PROGRESS',   // ✅ 正确
    '暂停': 'PAUSED',          // ⚠️ 后端没有此状态
    '完成': 'DONE',            // ✅ 正确
    '阻塞': 'BLOCKED'          // ✅ 正确
  }
  return reverseMap[status] || status || 'TODO'
}
```

**问题**：后端 `TaskStatus` 枚举没有 `PAUSED` 状态！

```java
public enum TaskStatus {
    TODO,         // 待办
    IN_PROGRESS,  // 进行中
    BLOCKED,      // 阻塞
    DONE          // 已完成
    // ❌ 没有 PAUSED
}
```

**修复**：移除或替换"暂停"选项

### 方案2：修复后端添加PAUSED状态

修改 `TaskStatus.java`：

```java
public enum TaskStatus {
    TODO("待办", "任务待处理"),
    IN_PROGRESS("进行中", "任务正在执行中"),
    PAUSED("暂停", "任务已暂停"),      // ✅ 新增
    BLOCKED("阻塞", "任务被阻塞"),
    DONE("已完成", "任务已完成");
    
    // ... 其他代码
}
```

同时修改数据库：
```sql
ALTER TABLE tasks 
MODIFY COLUMN status 
ENUM('TODO','IN_PROGRESS','PAUSED','BLOCKED','DONE') 
DEFAULT 'TODO' 
COMMENT '任务状态（待办/进行中/暂停/阻塞/已完成）';
```

### 方案3：检查500错误

**步骤1**：查看后端日志
```bash
# 查看项目服务日志
tail -f ZhiyanPlatformgood/logs/zhiyan-project-service.log
```

查找包含以下关键词的错误：
- `JsonProcessingException`
- `convertListToJson`
- `assignTask`
- `1985264259663269888`

**步骤2**：检查数据库
```sql
-- 检查任务表结构
DESC tasks;

-- 检查assignee_id列的数据
SELECT id, title, assignee_id 
FROM tasks 
WHERE id = 1985264259663269888;
```

**步骤3**：验证前端发送的数据

在浏览器控制台运行：
```javascript
// 检查发送的数据格式
console.log(typeof assigneeIds)        // 应该是 "object"
console.log(Array.isArray(assigneeIds)) // 应该是 true
console.log(assigneeIds)                // 应该是 [数字ID]
```

## 🚀 完整修复步骤

### 步骤1：修复前端状态映射

修改 `zhiyan_front/src/views/ProjectDetail.vue`：

```vue
<div class="task-status-menu" v-if="task.showStatusMenu">
  <button @click="changeTaskStatus(task, '待接取')" class="status-option">待接取</button>
  <button @click="changeTaskStatus(task, '进行中')" class="status-option">进行中</button>
  <!-- ❌ 移除暂停选项，因为后端没有此状态 -->
  <!-- <button @click="changeTaskStatus(task, '暂停')" class="status-option">暂停</button> -->
  <button @click="changeTaskStatus(task, '阻塞')" class="status-option">阻塞</button>
  <button @click="changeTaskStatus(task, '完成')" class="status-option">完成</button>
</div>
```

### 步骤2：添加错误处理

```javascript
async changeTaskStatus(task, newStatus) {
  try {
    const { taskAPI } = await import('@/api/task')
    const statusValue = this.getStatusValue(newStatus)
    
    // ✅ 验证状态值
    const validStatuses = ['TODO', 'IN_PROGRESS', 'BLOCKED', 'DONE']
    if (!validStatuses.includes(statusValue)) {
      console.error('无效的状态值:', statusValue)
      alert(`无效的任务状态: ${newStatus}`)
      return
    }
    
    console.log(`更新任务状态: ${newStatus} -> ${statusValue}`)
    
    const response = await taskAPI.updateTaskStatus(task.id, statusValue)
    
    if (response && response.code === 200) {
      await this.loadProjectTasks()
      this.showSuccessToast('任务状态已更新！')
    } else {
      console.error('API返回失败:', response)
      alert('更新失败：' + (response.msg || '未知错误'))
    }
  } catch (error) {
    console.error('更新任务状态失败:', error)
    alert('更新失败，请稍后重试')
  } finally {
    this.$set(task, 'showStatusMenu', false)
  }
}
```

### 步骤3：修复分配任务

检查前端发送的数据：

```javascript
async confirmAssignTask() {
  if (!this.selectedAssigneeId || !this.taskToAssign) return
  
  try {
    const { taskAPI } = await import('@/api/task')
    
    // ✅ 确保ID是数字数组
    const assigneeIds = [Number(this.selectedAssigneeId)]
    
    console.log('分配任务数据:', {
      taskId: this.taskToAssign.id,
      assigneeIds: assigneeIds,
      type: typeof assigneeIds[0]  // 应该是 "number"
    })
    
    const response = await taskAPI.assignTask(this.taskToAssign.id, assigneeIds)
    
    if (response && response.code === 200) {
      await this.loadProjectTasks()
      this.showSuccessToast('任务分配成功！')
      this.closeAssignTaskModal()
    } else {
      console.error('API返回:', response)
      alert('分配失败：' + (response.msg || '未知错误'))
    }
  } catch (error) {
    console.error('分配任务失败:', error)
    // ✅ 显示详细错误信息
    if (error.response) {
      console.error('响应数据:', error.response.data)
      alert(`分配失败：${error.response.status} - ${error.response.data.error || '服务器错误'}`)
    } else {
      alert('分配失败，请稍后重试')
    }
  }
}
```

## 🔧 临时解决方案

如果后端不方便修改，可以前端临时处理：

### 方案A：隐藏"暂停"选项

```vue
<!-- 只显示后端支持的4个状态 -->
<div class="task-status-menu" v-if="task.showStatusMenu">
  <button @click="changeTaskStatus(task, '待接取')">待接取</button>
  <button @click="changeTaskStatus(task, '进行中')">进行中</button>
  <button @click="changeTaskStatus(task, '阻塞')">阻塞</button>
  <button @click="changeTaskStatus(task, '完成')">完成</button>
</div>
```

### 方案B：映射暂停到阻塞

```javascript
getStatusValue(status) {
  const reverseMap = {
    '待接取': 'TODO',
    '进行中': 'IN_PROGRESS',
    '暂停': 'BLOCKED',    // ✅ 暂停映射到阻塞
    '阻塞': 'BLOCKED',
    '完成': 'DONE'
  }
  return reverseMap[status] || status || 'TODO'
}
```

## 📋 验证清单

完成修复后，请验证：

- [ ] 创建任务后状态显示为"待接取"
- [ ] 点击状态下拉菜单可以切换状态
- [ ] 切换到"进行中"成功
- [ ] 切换到"完成"成功
- [ ] 切换到"阻塞"成功
- [ ] 分配任务给团队成员成功
- [ ] 成员接取任务成功
- [ ] 浏览器控制台没有错误
- [ ] 后端日志没有错误

## 🆘 如果问题仍然存在

### 1. 收集信息
- 浏览器控制台的完整错误信息
- 后端日志的详细堆栈信息
- Network面板中请求和响应的完整数据

### 2. 检查数据库
```sql
-- 检查任务数据
SELECT * FROM tasks WHERE id = [任务ID];

-- 检查assignee_id列的格式
SELECT assignee_id FROM tasks LIMIT 10;
```

### 3. 调试建议
在 `TaskServiceImpl.java` 的 `assignTask` 方法添加详细日志：

```java
@Override
@Transactional
public Tasks assignTask(Long taskId, List<Long> assigneeIds, Long operatorId) {
    log.info("========== 开始分配任务 ==========");
    log.info("任务ID: {}", taskId);
    log.info("执行者ID列表: {}", assigneeIds);
    log.info("操作人ID: {}", operatorId);
    
    Tasks task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
    
    log.info("任务信息: projectId={}, title={}", task.getProjectId(), task.getTitle());
    
    // 转换JSON前
    log.info("转换前的assigneeIds: {}", assigneeIds);
    String assigneeIdsJson = convertListToJson(assigneeIds);
    log.info("转换后的JSON: {}", assigneeIdsJson);
    
    task.setAssigneeId(assigneeIdsJson);
    
    Tasks saved = taskRepository.save(task);
    log.info("任务保存成功");
    log.info("========== 分配任务完成 ==========");
    
    return saved;
}
```

## 📝 总结

主要问题：
1. ✅ 前端状态映射包含后端不支持的"暂停"状态
2. ❌ 500错误需要查看详细日志才能确定原因

修复方案：
1. 移除或重新映射"暂停"选项
2. 添加详细的错误处理和日志
3. 验证数据类型和格式

