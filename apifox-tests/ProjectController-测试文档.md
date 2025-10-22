# 智研平台 - 项目管理模块 API 测试文档

## 📋 目录
- [环境配置](#环境配置)
- [认证说明](#认证说明)
- [接口列表](#接口列表)
- [测试用例](#测试用例)

## 🌍 环境配置

### 开发环境
- **Base URL**: `http://localhost:8080`
- **描述**: 本地开发环境

### 测试环境
- **Base URL**: `http://test-server:8080`
- **描述**: 测试服务器环境

### 生产环境
- **Base URL**: `https://api.zhiyan.com`
- **描述**: 生产环境（谨慎操作）

## 🔐 认证说明

### 认证方式
- **类型**: Bearer Token
- **Header**: `Authorization: Bearer {accessToken}`

### 获取Token步骤
1. 调用登录接口获取 accessToken
2. 在后续请求的 Header 中添加: `Authorization: Bearer {accessToken}`
3. Token包含的信息：userId、email、roles

### 权限级别
- **isAuthenticated()**: 需要登录（任何已登录用户）
- **hasRole('OWNER')**: 需要OWNER角色
- **hasAnyRole('OWNER', 'MEMBER')**: 需要OWNER或MEMBER角色
- **无权限标记**: 公开接口，无需登录

---

## 📝 接口列表

| 序号 | 接口名称 | HTTP方法 | 路径 | 权限要求 |
|------|---------|----------|------|----------|
| 1 | 创建项目 | POST | /api/projects | OWNER/MEMBER |
| 2 | 更新项目 | PUT | /api/projects/{projectId} | OWNER/MEMBER |
| 3 | 删除项目 | DELETE | /api/projects/{projectId} | OWNER/MEMBER |
| 4 | 获取项目详情 | GET | /api/projects/{projectId} | 需登录 |
| 5 | 获取所有项目 | GET | /api/projects | OWNER |
| 6 | 获取我创建的项目 | GET | /api/projects/my-created | 需登录 |
| 7 | 获取指定用户创建的项目 | GET | /api/projects/creator/{creatorId} | 需登录 |
| 8 | 按状态获取项目 | GET | /api/projects/status/{status} | 需登录 |
| 9 | 获取我参与的项目 | GET | /api/projects/my-projects | 需登录 |
| 10 | 搜索项目 | GET | /api/projects/search | 需登录 |
| 11 | 获取公开项目 | GET | /api/projects/public/active | 无需登录 |
| 12 | 更新项目状态 | PATCH | /api/projects/{projectId}/status | OWNER/MEMBER |
| 13 | 归档项目 | POST | /api/projects/{projectId}/archive | OWNER/MEMBER |
| 14 | 统计我创建的项目数 | GET | /api/projects/count/my-created | 需登录 |
| 15 | 统计我参与的项目数 | GET | /api/projects/count/my-participated | 需登录 |

---

## 🧪 测试用例

### 1. 创建项目

**接口**: `POST /api/projects`

**权限**: OWNER/MEMBER

**请求参数** (Query Params):
```
name: 项目名称 (必填)
description: 项目描述 (可选)
visibility: 可见性 (可选) - PUBLIC/PRIVATE/INTERNAL
startDate: 开始日期 (可选) - 格式: YYYY-MM-DD
endDate: 结束日期 (可选) - 格式: YYYY-MM-DD
```

**测试用例**:

#### 用例1.1: 正常创建公开项目
```http
POST http://localhost:8080/api/projects?name=AI智能分析平台&description=基于深度学习的数据分析平台&visibility=PUBLIC&startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回创建的项目对象，包含项目ID、创建时间等信息

#### 用例1.2: 创建私有项目
```http
POST http://localhost:8080/api/projects?name=内部研发项目&description=公司内部研发项目&visibility=PRIVATE
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- visibility字段为PRIVATE

#### 用例1.3: 缺少必填参数（异常测试）
```http
POST http://localhost:8080/api/projects?description=没有项目名称
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 400
- 返回参数校验错误信息

---

### 2. 更新项目

**接口**: `PUT /api/projects/{projectId}`

**权限**: OWNER/MEMBER

**路径参数**:
```
projectId: 项目ID
```

**请求参数** (Query Params):
```
name: 项目名称 (可选)
description: 项目描述 (可选)
visibility: 可见性 (可选)
status: 项目状态 (可选) - PLANNING/IN_PROGRESS/COMPLETED/CANCELLED/ARCHIVED
startDate: 开始日期 (可选)
endDate: 结束日期 (可选)
```

**测试用例**:

#### 用例2.1: 更新项目名称和描述
```http
PUT http://localhost:8080/api/projects/1?name=更新后的AI平台&description=新的描述内容
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回更新后的项目信息

#### 用例2.2: 更新项目状态
```http
PUT http://localhost:8080/api/projects/1?status=IN_PROGRESS
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- status字段已更新为IN_PROGRESS

---

### 3. 删除项目

**接口**: `DELETE /api/projects/{projectId}`

**权限**: OWNER/MEMBER

**路径参数**:
```
projectId: 项目ID
```

**测试用例**:

#### 用例3.1: 删除自己创建的项目
```http
DELETE http://localhost:8080/api/projects/1
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回删除成功的消息

#### 用例3.2: 删除不存在的项目（异常测试）
```http
DELETE http://localhost:8080/api/projects/99999
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 404
- 返回项目不存在的错误信息

---

### 4. 获取项目详情

**接口**: `GET /api/projects/{projectId}`

**权限**: 需登录

**路径参数**:
```
projectId: 项目ID
```

**测试用例**:

#### 用例4.1: 获取已存在的项目
```http
GET http://localhost:8080/api/projects/1
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回完整的项目详情

#### 用例4.2: 获取不存在的项目
```http
GET http://localhost:8080/api/projects/99999
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 404

---

### 5. 获取所有项目（管理员）

**接口**: `GET /api/projects`

**权限**: OWNER

**请求参数** (Query Params):
```
page: 页码，从0开始 (默认: 0)
size: 每页数量 (默认: 10)
sortBy: 排序字段 (默认: createdAt)
direction: 排序方向 ASC/DESC (默认: DESC)
```

**测试用例**:

#### 用例5.1: 获取第一页，每页10条
```http
GET http://localhost:8080/api/projects?page=0&size=10
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回分页数据，包含总数、当前页等信息

#### 用例5.2: 按创建时间升序排序
```http
GET http://localhost:8080/api/projects?page=0&size=20&sortBy=createdAt&direction=ASC
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 数据按创建时间升序排列

---

### 6. 获取我创建的项目

**接口**: `GET /api/projects/my-created`

**权限**: 需登录

**请求参数** (Query Params):
```
page: 页码 (默认: 0)
size: 每页数量 (默认: 10)
```

**测试用例**:

#### 用例6.1: 获取我创建的项目列表
```http
GET http://localhost:8080/api/projects/my-created?page=0&size=10
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回当前用户创建的所有项目

---

### 7. 获取指定用户创建的项目

**接口**: `GET /api/projects/creator/{creatorId}`

**权限**: 需登录

**路径参数**:
```
creatorId: 创建者用户ID
```

**请求参数** (Query Params):
```
page: 页码 (默认: 0)
size: 每页数量 (默认: 10)
```

**测试用例**:

#### 用例7.1: 查询用户ID为1的创建项目
```http
GET http://localhost:8080/api/projects/creator/1?page=0&size=10
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回该用户创建的项目列表

---

### 8. 按状态获取项目

**接口**: `GET /api/projects/status/{status}`

**权限**: 需登录

**路径参数**:
```
status: 项目状态
  - PLANNING: 计划中
  - IN_PROGRESS: 进行中
  - COMPLETED: 已完成
  - CANCELLED: 已取消
  - ARCHIVED: 已归档
```

**请求参数** (Query Params):
```
page: 页码 (默认: 0)
size: 每页数量 (默认: 10)
```

**测试用例**:

#### 用例8.1: 获取进行中的项目
```http
GET http://localhost:8080/api/projects/status/IN_PROGRESS?page=0&size=10
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回所有进行中的项目

#### 用例8.2: 获取已完成的项目
```http
GET http://localhost:8080/api/projects/status/COMPLETED?page=0&size=10
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回所有已完成的项目

---

### 9. 获取我参与的项目

**接口**: `GET /api/projects/my-projects`

**权限**: 需登录

**请求参数** (Query Params):
```
page: 页码 (默认: 0)
size: 每页数量 (默认: 10)
```

**测试用例**:

#### 用例9.1: 获取我参与的所有项目
```http
GET http://localhost:8080/api/projects/my-projects?page=0&size=10
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回当前用户参与的所有项目（包括创建和加入的）

---

### 10. 搜索项目

**接口**: `GET /api/projects/search`

**权限**: 需登录

**请求参数** (Query Params):
```
keyword: 搜索关键字 (必填)
page: 页码 (默认: 0)
size: 每页数量 (默认: 10)
```

**测试用例**:

#### 用例10.1: 搜索包含"AI"的项目
```http
GET http://localhost:8080/api/projects/search?keyword=AI&page=0&size=10
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回名称或描述中包含"AI"的项目

#### 用例10.2: 搜索包含"平台"的项目
```http
GET http://localhost:8080/api/projects/search?keyword=平台&page=0&size=20
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回相关搜索结果

---

### 11. 获取公开项目（无需登录）

**接口**: `GET /api/projects/public/active`

**权限**: 无需登录

**请求参数** (Query Params):
```
page: 页码 (默认: 0)
size: 每页数量 (默认: 10)
```

**测试用例**:

#### 用例11.1: 不登录获取公开项目
```http
GET http://localhost:8080/api/projects/public/active?page=0&size=10
```

**预期结果**: 
- 状态码: 200
- 返回所有公开且活跃的项目
- **注意**: 此接口无需 Authorization 头

---

### 12. 更新项目状态

**接口**: `PATCH /api/projects/{projectId}/status`

**权限**: OWNER/MEMBER

**路径参数**:
```
projectId: 项目ID
```

**请求参数** (Query Params):
```
status: 新状态 (必填)
  - PLANNING: 计划中
  - IN_PROGRESS: 进行中
  - COMPLETED: 已完成
  - CANCELLED: 已取消
  - ARCHIVED: 已归档
```

**测试用例**:

#### 用例12.1: 将项目状态更新为进行中
```http
PATCH http://localhost:8080/api/projects/1/status?status=IN_PROGRESS
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 项目状态已更新

#### 用例12.2: 将项目标记为已完成
```http
PATCH http://localhost:8080/api/projects/1/status?status=COMPLETED
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 项目状态更新为已完成

---

### 13. 归档项目

**接口**: `POST /api/projects/{projectId}/archive`

**权限**: OWNER/MEMBER

**路径参数**:
```
projectId: 项目ID
```

**测试用例**:

#### 用例13.1: 归档已完成的项目
```http
POST http://localhost:8080/api/projects/1/archive
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 项目状态更新为ARCHIVED

---

### 14. 统计我创建的项目数

**接口**: `GET /api/projects/count/my-created`

**权限**: 需登录

**测试用例**:

#### 用例14.1: 获取我创建的项目总数
```http
GET http://localhost:8080/api/projects/count/my-created
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回数字，表示创建的项目总数

---

### 15. 统计我参与的项目数

**接口**: `GET /api/projects/count/my-participated`

**权限**: 需登录

**测试用例**:

#### 用例15.1: 获取我参与的项目总数
```http
GET http://localhost:8080/api/projects/count/my-participated
Authorization: Bearer {accessToken}
```

**预期结果**: 
- 状态码: 200
- 返回数字，表示参与的项目总数

---

## 📊 枚举值说明

### 项目状态 (ProjectStatus)
| 枚举值 | 说明 |
|--------|------|
| PLANNING | 计划中 |
| IN_PROGRESS | 进行中 |
| COMPLETED | 已完成 |
| CANCELLED | 已取消 |
| ARCHIVED | 已归档 |

### 项目可见性 (ProjectVisibility)
| 枚举值 | 说明 |
|--------|------|
| PUBLIC | 公开 - 所有人可见 |
| PRIVATE | 私有 - 仅成员可见 |
| INTERNAL | 内部 - 组织内可见 |

---

## ⚠️ 常见错误码

| 状态码 | 说明 | 可能原因 |
|--------|------|----------|
| 200 | 成功 | 请求正常处理 |
| 400 | 请求参数错误 | 缺少必填参数或参数格式不正确 |
| 401 | 未授权 | 未登录或Token过期 |
| 403 | 权限不足 | 没有相应的操作权限 |
| 404 | 资源不存在 | 请求的项目ID不存在 |
| 500 | 服务器内部错误 | 服务器异常，请联系管理员 |

---

## 🔧 测试前准备

### 1. 获取访问Token
```http
POST http://localhost:8080/zhiyan/auth/login
Content-Type: application/x-www-form-urlencoded

email=your_email@example.com&password=your_password
```

### 2. 在Apifox中设置环境变量
- 打开 Apifox
- 选择环境 → 选择对应环境（本地开发/测试/生产）
- 添加变量 `accessToken`，值为登录返回的token

### 3. 开始测试
- 按照接口顺序依次测试
- 先测试创建，再测试查询、更新、删除
- 注意测试用例之间的依赖关系

---

## 📝 测试注意事项

1. **测试数据隔离**: 使用测试账号，避免污染生产数据
2. **清理测试数据**: 测试完成后及时删除测试项目
3. **Token过期**: Token默认30分钟过期，过期需重新登录
4. **权限测试**: 使用不同角色的账号测试权限控制
5. **分页测试**: 注意page从0开始
6. **日期格式**: 严格按照 YYYY-MM-DD 格式

---

## 📞 技术支持

如遇到问题，请联系：
- 开发团队: dev@zhiyan.com
- 技术文档: https://docs.zhiyan.com

