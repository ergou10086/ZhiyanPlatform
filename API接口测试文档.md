# 智研平台 - 项目模块API接口测试文档

> **版本**: v1.0  
> **Base URL**: `http://localhost:端口号`  
> **认证方式**: Bearer Token (JWT)  
> **更新日期**: 2025-10-17

---

## 目录

- [1. 项目管理接口 (ProjectController)](#1-项目管理接口-projectcontroller)
- [2. 项目成员管理接口 (ProjectMemberController)](#2-项目成员管理接口-projectmembercontroller)
- [3. 任务管理接口 (TaskController)](#3-任务管理接口-taskcontroller)
- [附录: 枚举类型说明](#附录-枚举类型说明)

---

## 通用说明

### 统一响应格式

```json
{
  "code": 200,           // 状态码: 200-成功, 500-失败
  "msg": "操作成功",      // 提示信息
  "data": {}            // 响应数据
}
```

### 认证Header

除公开接口外，所有接口都需要在请求头中携带JWT Token：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 分页参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码（从0开始） |
| size | int | 10/20 | 每页大小 |
| sortBy | string | createdAt | 排序字段 |
| direction | string | DESC | 排序方向(ASC/DESC) |

### 分页响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "content": [],           // 数据列表
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 100,    // 总记录数
    "totalPages": 10,        // 总页数
    "last": false,           // 是否最后一页
    "first": true            // 是否第一页
  }
}
```

---

## 1. 项目管理接口 (ProjectController)

**Base Path**: `/api/projects`

### 1.1 创建项目

**接口**: `POST /api/projects`  
**权限**: 需要认证 (OWNER/MEMBER角色)  
**描述**: 创建新项目，创建者自动成为项目拥有者

#### 请求参数 (Form Data / Query Params)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 项目名称 |
| description | string | 否 | 项目描述 |
| visibility | enum | 否 | 可见性(PUBLIC/PRIVATE)，默认PRIVATE |
| startDate | date | 否 | 开始日期(YYYY-MM-DD) |
| endDate | date | 否 | 结束日期(YYYY-MM-DD) |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/projects?name=AI研究项目&description=深度学习研究&visibility=PUBLIC&startDate=2025-01-01&endDate=2025-12-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": "1234567890123456789",
    "name": "AI研究项目",
    "description": "深度学习研究",
    "status": "PLANNING",
    "visibility": "PUBLIC",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "createdBy": "1001",
    "createdAt": "2025-10-17T10:00:00",
    "updatedAt": "2025-10-17T10:00:00"
  }
}
```

---

### 1.2 更新项目

**接口**: `PUT /api/projects/{projectId}`  
**权限**: 需要认证 (OWNER/MEMBER角色，且需要是项目成员)  
**描述**: 更新项目信息

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| projectId | long | 项目ID |

#### 请求参数 (Query Params)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 项目名称 |
| description | string | 否 | 项目描述 |
| visibility | enum | 否 | 可见性(PUBLIC/PRIVATE) |
| status | enum | 否 | 项目状态(PLANNING/ONGOING/COMPLETED/ARCHIVED) |
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |

#### 请求示例

```bash
curl -X PUT "http://localhost:8080/api/projects/1234567890123456789?name=AI高级研究&status=ONGOING" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.3 删除项目

**接口**: `DELETE /api/projects/{projectId}`  
**权限**: 需要认证 (OWNER/MEMBER角色)  
**描述**: 软删除项目

#### 请求示例

```bash
curl -X DELETE "http://localhost:8080/api/projects/1234567890123456789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 1.4 获取项目详情

**接口**: `GET /api/projects/{projectId}`  
**权限**: 需要认证  
**描述**: 根据ID获取项目详细信息

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/1234567890123456789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.5 获取所有项目（分页）

**接口**: `GET /api/projects`  
**权限**: 需要认证 (OWNER角色)  
**描述**: 分页获取所有项目（管理员）

#### 请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码 |
| size | int | 10 | 每页大小 |
| sortBy | string | createdAt | 排序字段 |
| direction | string | DESC | 排序方向 |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects?page=0&size=10&sortBy=createdAt&direction=DESC" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.6 获取我创建的项目

**接口**: `GET /api/projects/my-created`  
**权限**: 需要认证  
**描述**: 获取当前用户创建的所有项目

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/my-created?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.7 获取指定用户创建的项目

**接口**: `GET /api/projects/creator/{creatorId}`  
**权限**: 需要认证  
**描述**: 根据创建者ID获取项目列表

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/creator/1001?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.8 按状态获取项目

**接口**: `GET /api/projects/status/{status}`  
**权限**: 需要认证  
**描述**: 根据项目状态筛选项目

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| status | enum | 项目状态(PLANNING/ONGOING/COMPLETED/ARCHIVED) |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/status/ONGOING?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.9 获取我参与的项目

**接口**: `GET /api/projects/my-projects`  
**权限**: 需要认证  
**描述**: 获取当前用户参与的所有项目

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/my-projects?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.10 搜索项目

**接口**: `GET /api/projects/search`  
**权限**: 需要认证  
**描述**: 根据关键字搜索项目

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页大小 |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/search?keyword=AI&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.11 获取公开的活跃项目 ⭐

**接口**: `GET /api/projects/public/active`  
**权限**: ❌ 无需认证（公开接口）  
**描述**: 获取所有公开的活跃项目（项目广场）

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/public/active?page=0&size=10"
```

---

### 1.12 更新项目状态

**接口**: `PATCH /api/projects/{projectId}/status`  
**权限**: 需要认证 (OWNER/MEMBER角色)  
**描述**: 更新项目的状态

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | enum | 是 | 新状态(PLANNING/ONGOING/COMPLETED/ARCHIVED) |

#### 请求示例

```bash
curl -X PATCH "http://localhost:8080/api/projects/1234567890123456789/status?status=COMPLETED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.13 归档项目

**接口**: `POST /api/projects/{projectId}/archive`  
**权限**: 需要认证 (OWNER/MEMBER角色)  
**描述**: 将项目归档

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/projects/1234567890123456789/archive" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 1.14 统计我创建的项目数量

**接口**: `GET /api/projects/count/my-created`  
**权限**: 需要认证  
**描述**: 统计当前用户创建的项目数量

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/count/my-created" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": 15
}
```

---

### 1.15 统计我参与的项目数量

**接口**: `GET /api/projects/count/my-participated`  
**权限**: 需要认证  
**描述**: 统计当前用户参与的项目数量

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/count/my-participated" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 2. 项目成员管理接口 (ProjectMemberController)

**Base Path**: `/api/projects/members`

### 2.1 邀请成员加入项目

**接口**: `POST /api/projects/members/projects/{projectId}/invite`  
**权限**: 需要认证（仅项目负责人）  
**描述**: 项目负责人直接将用户添加到项目中（无需对方同意）

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| projectId | long | 项目ID |

#### 请求体 (JSON)

```json
{
  "userId": 1002,
  "role": "MEMBER"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 被邀请用户ID |
| role | enum | 否 | 角色(OWNER/MEMBER)，默认MEMBER |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/projects/members/projects/1234567890123456789/invite" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1002,
    "role": "MEMBER"
  }'
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "成员已添加",
  "data": {
    "id": "2234567890123456789",
    "projectId": "1234567890123456789",
    "userId": 1002,
    "projectRole": "MEMBER",
    "joinedAt": "2025-10-17T10:30:00"
  }
}
```

---

### 2.2 移除项目成员

**接口**: `DELETE /api/projects/members/projects/{projectId}/members/{userId}`  
**权限**: 需要认证（仅项目负责人）  
**描述**: 项目负责人移除项目成员

#### 请求示例

```bash
curl -X DELETE "http://localhost:8080/api/projects/members/projects/1234567890123456789/members/1002" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2.3 更新成员角色

**接口**: `PUT /api/projects/members/projects/{projectId}/members/{userId}/role`  
**权限**: 需要认证（仅项目负责人）  
**描述**: 项目负责人修改成员的项目角色

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| newRole | enum | 是 | 新角色(OWNER/MEMBER) |

#### 请求示例

```bash
curl -X PUT "http://localhost:8080/api/projects/members/projects/1234567890123456789/members/1002/role?newRole=OWNER" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2.4 退出项目

**接口**: `DELETE /api/projects/members/projects/{projectId}/leave`  
**权限**: 需要认证  
**描述**: 成员主动退出项目

#### 请求示例

```bash
curl -X DELETE "http://localhost:8080/api/projects/members/projects/1234567890123456789/leave" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2.5 获取项目成员列表

**接口**: `GET /api/projects/members/projects/{projectId}`  
**权限**: 需要认证  
**描述**: 获取项目的所有成员详细信息

#### 请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码 |
| size | int | 20 | 每页大小 |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/members/projects/1234567890123456789?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "content": [
      {
        "id": "2234567890123456789",
        "projectId": "1234567890123456789",
        "userId": 1001,
        "userName": "张三",
        "userEmail": "zhangsan@example.com",
        "avatarUrl": "http://...",
        "projectRole": "OWNER",
        "roleName": "项目负责人",
        "joinedAt": "2025-10-01T10:00:00",
        "isCurrentUser": true
      },
      {
        "id": "2234567890123456790",
        "projectId": "1234567890123456789",
        "userId": 1002,
        "userName": "李四",
        "userEmail": "lisi@example.com",
        "avatarUrl": "http://...",
        "projectRole": "MEMBER",
        "roleName": "普通成员",
        "joinedAt": "2025-10-05T14:00:00",
        "isCurrentUser": false
      }
    ],
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

### 2.6 获取我参与的项目

**接口**: `GET /api/projects/members/my-projects`  
**权限**: 需要认证  
**描述**: 获取当前用户参与的所有项目

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/members/my-projects?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2.7 按角色获取成员

**接口**: `GET /api/projects/members/projects/{projectId}/role/{role}`  
**权限**: 需要认证  
**描述**: 获取项目中指定角色的所有成员

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| role | enum | 角色(OWNER/MEMBER) |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/members/projects/1234567890123456789/role/OWNER" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2.8 检查成员身份

**接口**: `GET /api/projects/members/projects/{projectId}/check-membership`  
**权限**: 需要认证  
**描述**: 检查当前用户是否为项目成员

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/members/projects/1234567890123456789/check-membership" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": true
}
```

---

### 2.9 检查负责人身份

**接口**: `GET /api/projects/members/projects/{projectId}/check-owner`  
**权限**: 需要认证  
**描述**: 检查当前用户是否为项目负责人

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/members/projects/1234567890123456789/check-owner" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2.10 获取我的角色

**接口**: `GET /api/projects/members/projects/{projectId}/my-role`  
**权限**: 需要认证  
**描述**: 获取当前用户在项目中的角色

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/members/projects/1234567890123456789/my-role" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "OWNER"
}
```

---

### 2.11 获取成员数量

**接口**: `GET /api/projects/members/projects/{projectId}/count`  
**权限**: 需要认证  
**描述**: 获取项目的成员总数

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/members/projects/1234567890123456789/count" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 3. 任务管理接口 (TaskController)

**Base Path**: `/api/projects/tasks`

### 3.1 创建任务

**接口**: `POST /api/projects/tasks`  
**权限**: 需要认证  
**描述**: 在项目中创建新任务

#### 请求体 (JSON)

```json
{
  "projectId": "1234567890123456789",
  "title": "完成数据库设计",
  "description": "设计用户表、项目表、任务表的数据库结构",
  "priority": "HIGH",
  "assigneeIds": [1002, 1003],
  "dueDate": "2025-10-30"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| projectId | long | 是 | 项目ID |
| title | string | 是 | 任务标题 |
| description | string | 否 | 任务描述 |
| priority | enum | 否 | 优先级(HIGH/MEDIUM/LOW)，默认MEDIUM |
| assigneeIds | array | 否 | 执行者ID列表（必须是项目成员） |
| dueDate | date | 否 | 截止日期(YYYY-MM-DD) |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/projects/tasks" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "1234567890123456789",
    "title": "完成数据库设计",
    "description": "设计用户表、项目表、任务表的数据库结构",
    "priority": "HIGH",
    "assigneeIds": [1002, 1003],
    "dueDate": "2025-10-30"
  }'
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "任务创建成功",
  "data": {
    "id": "3234567890123456789",
    "projectId": "1234567890123456789",
    "title": "完成数据库设计",
    "description": "设计用户表、项目表、任务表的数据库结构",
    "status": "TODO",
    "priority": "HIGH",
    "assigneeId": "[1002,1003]",
    "dueDate": "2025-10-30",
    "createdBy": 1001,
    "createdAt": "2025-10-17T11:00:00",
    "updatedAt": "2025-10-17T11:00:00"
  }
}
```

---

### 3.2 更新任务

**接口**: `PUT /api/projects/tasks/{taskId}`  
**权限**: 需要认证（项目成员）  
**描述**: 更新任务的标题、描述、状态、优先级等信息

#### 请求体 (JSON)

```json
{
  "title": "完成数据库设计和实现",
  "description": "更新后的描述",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "assigneeIds": [1002, 1003, 1004],
  "dueDate": "2025-11-05"
}
```

所有字段都是可选的，只更新提供的字段。

#### 请求示例

```bash
curl -X PUT "http://localhost:8080/api/projects/tasks/3234567890123456789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

---

### 3.3 删除任务

**接口**: `DELETE /api/projects/tasks/{taskId}`  
**权限**: 需要认证（任务创建者或项目负责人）  
**描述**: 软删除任务

#### 请求示例

```bash
curl -X DELETE "http://localhost:8080/api/projects/tasks/3234567890123456789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.4 更新任务状态

**接口**: `PATCH /api/projects/tasks/{taskId}/status`  
**权限**: 需要认证（项目成员）  
**描述**: 更新任务的状态（拖拽看板时调用）

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | enum | 是 | 新状态(TODO/IN_PROGRESS/BLOCKED/DONE) |

#### 请求示例

```bash
curl -X PATCH "http://localhost:8080/api/projects/tasks/3234567890123456789/status?status=DONE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.5 分配任务

**接口**: `PUT /api/projects/tasks/{taskId}/assign`  
**权限**: 需要认证（项目成员）  
**描述**: 将任务分配给项目成员

#### 请求体 (JSON)

```json
[1002, 1003, 1004]
```

#### 请求示例

```bash
curl -X PUT "http://localhost:8080/api/projects/tasks/3234567890123456789/assign" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[1002, 1003]'
```

---

### 3.6 获取任务详情

**接口**: `GET /api/projects/tasks/{taskId}`  
**权限**: 需要认证  
**描述**: 获取任务的详细信息

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/3234567890123456789" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": "3234567890123456789",
    "projectId": "1234567890123456789",
    "projectName": "AI研究项目",
    "title": "完成数据库设计",
    "description": "设计用户表、项目表、任务表的数据库结构",
    "status": "IN_PROGRESS",
    "statusName": "进行中",
    "priority": "HIGH",
    "priorityName": "高优先级",
    "assignees": [
      {
        "userId": "1002",
        "userName": "李四",
        "email": "lisi@example.com",
        "avatarUrl": "http://..."
      },
      {
        "userId": "1003",
        "userName": "王五",
        "email": "wangwu@example.com",
        "avatarUrl": "http://..."
      }
    ],
    "dueDate": "2025-10-30",
    "isOverdue": false,
    "createdBy": "1001",
    "creatorName": "张三",
    "createdAt": "2025-10-17T11:00:00",
    "updatedAt": "2025-10-17T12:00:00"
  }
}
```

---

### 3.7 获取项目任务看板

**接口**: `GET /api/projects/tasks/projects/{projectId}/board`  
**权限**: 需要认证  
**描述**: 获取项目的任务看板数据，按状态分组

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/board" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "todoTasks": [
      {
        "id": "3234567890123456789",
        "title": "任务1",
        "priority": "HIGH",
        "assignees": [...],
        "dueDate": "2025-10-30"
      }
    ],
    "inProgressTasks": [
      {
        "id": "3234567890123456790",
        "title": "任务2",
        "priority": "MEDIUM",
        "assignees": [...],
        "dueDate": "2025-11-05"
      }
    ],
    "blockedTasks": [],
    "doneTasks": [],
    "statistics": {
      "todoCount": 5,
      "inProgressCount": 3,
      "blockedCount": 0,
      "doneCount": 12,
      "totalCount": 20,
      "overdueCount": 2
    }
  }
}
```

---

### 3.8 获取项目任务列表

**接口**: `GET /api/projects/tasks/projects/{projectId}`  
**权限**: 需要认证  
**描述**: 分页获取项目的所有任务

#### 请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | int | 0 | 页码 |
| size | int | 20 | 每页大小 |
| sortBy | string | createdAt | 排序字段 |
| direction | string | DESC | 排序方向 |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789?page=0&size=20&sortBy=dueDate&direction=ASC" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.9 按状态获取任务

**接口**: `GET /api/projects/tasks/projects/{projectId}/status/{status}`  
**权限**: 需要认证  
**描述**: 根据任务状态筛选项目任务

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| status | enum | 任务状态(TODO/IN_PROGRESS/BLOCKED/DONE) |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/status/TODO?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.10 按优先级获取任务

**接口**: `GET /api/projects/tasks/projects/{projectId}/priority/{priority}`  
**权限**: 需要认证  
**描述**: 根据任务优先级筛选项目任务

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| priority | enum | 优先级(HIGH/MEDIUM/LOW) |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/priority/HIGH?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.11 获取我的任务

**接口**: `GET /api/projects/tasks/my-assigned`  
**权限**: 需要认证  
**描述**: 获取分配给当前用户的所有任务

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/my-assigned?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.12 获取我创建的任务

**接口**: `GET /api/projects/tasks/my-created`  
**权限**: 需要认证  
**描述**: 获取当前用户创建的所有任务

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/my-created?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.13 搜索任务

**接口**: `GET /api/projects/tasks/projects/{projectId}/search`  
**权限**: 需要认证  
**描述**: 根据关键词搜索项目任务（标题或描述）

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页大小 |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/search?keyword=数据库&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.14 获取即将到期的任务

**接口**: `GET /api/projects/tasks/projects/{projectId}/upcoming`  
**权限**: 需要认证  
**描述**: 获取指定天数内即将到期的任务

#### 请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| days | int | 7 | 未来天数 |
| page | int | 0 | 页码 |
| size | int | 20 | 每页大小 |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/upcoming?days=7&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.15 获取已逾期的任务

**接口**: `GET /api/projects/tasks/projects/{projectId}/overdue`  
**权限**: 需要认证  
**描述**: 获取项目中已逾期且未完成的任务

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/overdue?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.16 统计项目任务数量

**接口**: `GET /api/projects/tasks/projects/{projectId}/count`  
**权限**: 需要认证  
**描述**: 统计项目中的任务总数

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/count" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": 45
}
```

---

### 3.17 按状态统计任务

**接口**: `GET /api/projects/tasks/projects/{projectId}/count/status/{status}`  
**权限**: 需要认证  
**描述**: 统计项目中指定状态的任务数量

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/count/status/TODO" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3.18 统计逾期任务

**接口**: `GET /api/projects/tasks/projects/{projectId}/count/overdue`  
**权限**: 需要认证  
**描述**: 统计项目中已逾期的任务数量

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/projects/tasks/projects/1234567890123456789/count/overdue" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 附录: 枚举类型说明

### ProjectStatus (项目状态)

| 值 | 说明 |
|------|------|
| PLANNING | 规划中 |
| ONGOING | 进行中 |
| COMPLETED | 已完成 |
| ARCHIVED | 已归档 |

### ProjectVisibility (项目可见性)

| 值 | 说明 |
|------|------|
| PUBLIC | 公开 |
| PRIVATE | 私有 |

### ProjectMemberRole (项目成员角色)

| 值 | 说明 |
|------|------|
| OWNER | 项目负责人 |
| MEMBER | 普通成员 |

### TaskStatus (任务状态)

| 值 | 说明 |
|------|------|
| TODO | 待办 |
| IN_PROGRESS | 进行中 |
| BLOCKED | 受阻 |
| DONE | 已完成 |

### TaskPriority (任务优先级)

| 值 | 说明 |
|------|------|
| HIGH | 高优先级 |
| MEDIUM | 中优先级 |
| LOW | 低优先级 |

---

## Postman Collection 导入

您可以根据上述接口文档创建Postman Collection进行测试。建议创建以下环境变量：

```json
{
  "base_url": "http://localhost:8080",
  "jwt_token": "your_jwt_token_here",
  "project_id": "1234567890123456789",
  "task_id": "3234567890123456789",
  "user_id": "1001"
}
```

---

## 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 401 | 未认证 | 检查JWT Token是否有效 |
| 403 | 权限不足 | 检查用户角色和权限 |
| 404 | 资源不存在 | 检查ID是否正确 |
| 500 | 服务器错误 | 查看服务器日志 |

---

**文档更新日期**: 2025-10-17  
**联系方式**: 如有疑问，请联系开发团队

