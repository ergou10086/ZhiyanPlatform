# Apifox 自动Token配置完整指南

## 🎯 目标
实现：登录一次 → 自动保存Token → 后续请求自动携带Token

---

## 📋 配置步骤

### 第一步：配置环境变量

1. 点击Apifox左上角的 **环境** 图标
2. 选择或新建环境（如"本地开发环境"）
3. 添加以下变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| baseUrl | http://localhost:8080 | 服务基础URL |
| authBaseUrl | http://localhost:8091 | 认证服务URL（如果不同端口） |
| accessToken | (空) | 访问令牌（登录后自动填充） |
| refreshToken | (空) | 刷新令牌（登录后自动填充） |
| userId | (空) | 用户ID（登录后自动填充） |
| projectId | 1 | 测试用项目ID |
| creatorId | 1 | 测试用创建者ID |

**注意**：accessToken初始值留空即可，登录后会自动填充！

---

### 第二步：配置登录接口

#### 1. 基本信息
- **名称**：用户登录
- **方法**：POST
- **URL**：`{{authBaseUrl}}/zhiyan/auth/login` 或 `http://localhost:8091/zhiyan/auth/login`

#### 2. Headers
```
Content-Type: application/json
```

#### 3. Body（选择raw → JSON）
```json
{
  "email": "768331153@qq.com",
  "password": "123456",
  "rememberMe": false
}
```

#### 4. 后置脚本（重点！）

点击 **"后置操作"** 标签页，选择 **"脚本"**，输入：

```javascript
// ========== 自动保存Token到环境变量 ==========

if (pm.response.code === 200) {
    try {
        var jsonData = pm.response.json();
        
        // 检查业务状态码
        if (jsonData.code === 200 && jsonData.data) {
            
            // 保存accessToken
            if (jsonData.data.accessToken) {
                pm.environment.set("accessToken", jsonData.data.accessToken);
                console.log("✅ AccessToken已保存");
            }
            
            // 保存refreshToken
            if (jsonData.data.refreshToken) {
                pm.environment.set("refreshToken", jsonData.data.refreshToken);
                console.log("✅ RefreshToken已保存");
            }
            
            // 保存用户信息
            if (jsonData.data.user) {
                pm.environment.set("userId", jsonData.data.user.id);
                pm.environment.set("userName", jsonData.data.user.name);
                pm.environment.set("userEmail", jsonData.data.user.email);
                console.log("✅ 用户信息已保存: " + jsonData.data.user.name);
            }
            
            console.log("🎉 登录成功！所有信息已自动保存到环境变量");
            
        } else {
            console.error("❌ 登录失败: " + (jsonData.msg || "未知错误"));
        }
    } catch (e) {
        console.error("❌ 脚本执行失败:", e.message);
    }
} else {
    console.error("❌ HTTP请求失败，状态码:", pm.response.code);
}
```

#### 5. 测试用例（可选）

在 **"测试"** 标签页添加：

```javascript
// 测试1：检查HTTP状态码
pm.test("HTTP状态码为200", function () {
    pm.response.to.have.status(200);
});

// 测试2：检查业务状态码
pm.test("登录成功", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
});

// 测试3：检查返回了Token
pm.test("返回了AccessToken", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data).to.have.property('accessToken');
    pm.expect(jsonData.data.accessToken).to.be.a('string');
});
```

---

### 第三步：在其他接口中使用Token

#### 方法A：单个接口配置（适合少量接口）

对每个需要认证的接口：

1. 打开接口（如"1. 创建项目"）
2. 点击 **Headers** 标签
3. 添加Header：
   - **Key**: `Authorization`
   - **Value**: `Bearer {{accessToken}}`
   - ✅ 勾选启用

**示例**：
```
Authorization: Bearer {{accessToken}}
```

#### 方法B：全局配置（推荐，适合大量接口）

1. 点击环境设置
2. 找到 **"公共Header"** 或 **"全局参数"** 选项
3. 添加：
   - **参数名**: `Authorization`
   - **参数值**: `Bearer {{accessToken}}`
   - **应用范围**: 选择需要的接口分组

这样所有接口都会自动携带这个Header！

---

## 🧪 测试流程

### 完整测试步骤：

#### 1. 第一次登录
```
打开"用户登录"接口
→ 点击"发送"
→ 查看响应（应该成功）
→ 查看控制台（Console），应该显示：
   ✅ AccessToken已保存
   ✅ RefreshToken已保存
   ✅ 用户信息已保存: 张三
   🎉 登录成功！所有信息已自动保存到环境变量
```

#### 2. 验证Token已保存
```
点击环境图标
→ 查看当前环境的变量
→ 确认 accessToken 已经有值（一长串字符串）
```

#### 3. 测试其他接口
```
打开任意需要认证的接口（如"获取我创建的项目"）
→ 确认Headers中有：Authorization: Bearer {{accessToken}}
→ 点击"发送"
→ 应该成功返回数据（因为已经自动携带了Token）
```

#### 4. Token过期后重新登录
```
如果接口返回401（未授权）
→ 重新调用"用户登录"接口
→ Token会自动更新
→ 再次调用其他接口即可
```

---

## 📊 环境变量引用语法

在Apifox中，使用双花括号引用环境变量：

```
{{variableName}}
```

**示例**：

```javascript
// URL中使用
{{baseUrl}}/api/projects

// Header中使用
Authorization: Bearer {{accessToken}}

// Body中使用（JSON）
{
  "userId": {{userId}},
  "email": "{{userEmail}}"
}

// Query参数中使用
?projectId={{projectId}}
```

---

## 🔧 常见问题

### Q1: 登录后Token没有自动保存？

**检查清单**：
1. ✅ 确认添加了后置脚本
2. ✅ 查看控制台（Console）是否有错误
3. ✅ 确认响应JSON结构正确
4. ✅ 检查环境变量名拼写是否一致

**调试方法**：
```javascript
// 在后置脚本中添加调试日志
console.log("响应数据:", JSON.stringify(pm.response.json(), null, 2));
```

### Q2: 其他接口提示401未授权？

**检查清单**：
1. ✅ 确认Header中添加了 `Authorization: Bearer {{accessToken}}`
2. ✅ 确认环境变量 `accessToken` 有值
3. ✅ 确认Token格式正确（Bearer + 空格 + token）
4. ✅ 确认Token未过期（可以重新登录）

### Q3: Token过期了怎么办？

**解决方法**：
- 重新调用登录接口，Token会自动更新
- 或者使用刷新Token接口（如果有）

### Q4: 如何查看当前Token的值？

**方法1**：查看环境变量
```
点击环境图标 → 查看当前环境 → 找到accessToken
```

**方法2**：在脚本中打印
```javascript
console.log("当前Token:", pm.environment.get("accessToken"));
```

---

## 💡 高级技巧

### 技巧1：自动判断Token是否即将过期

在需要认证的接口的 **前置脚本** 中添加：

```javascript
// 检查Token是否存在
var token = pm.environment.get("accessToken");
if (!token) {
    console.warn("⚠️ 未找到Token，请先登录");
}
```

### 技巧2：统一错误处理

在登录接口的后置脚本中添加：

```javascript
// 统一错误处理
if (pm.response.code !== 200) {
    console.error("❌ 请求失败");
    console.error("状态码:", pm.response.code);
    console.error("响应:", pm.response.text());
}
```

### 技巧3：保存多个环境的Token

为不同环境（开发、测试、生产）分别保存Token：

```
环境：本地开发 → accessToken: xxx
环境：测试服务器 → accessToken: yyy
环境：生产环境 → accessToken: zzz
```

切换环境时，Token会自动切换！

---

## 🎯 推荐的项目结构

```
智研平台API测试
├── 📁 认证模块
│   ├── ✅ 5. 用户登录 (含后置脚本)
│   ├── 6. 用户注册
│   └── 7. 刷新Token
├── 📁 项目管理API
│   ├── 1. 创建项目 (自动携带Token)
│   ├── 2. 更新项目 (自动携带Token)
│   ├── 3. 删除项目 (自动携带Token)
│   └── ...
└── 📁 环境配置
    ├── 本地开发 (baseUrl: localhost:8080)
    ├── 测试环境 (baseUrl: test.example.com)
    └── 生产环境 (baseUrl: api.example.com)
```

---

## ✅ 配置完成检查清单

- [ ] 创建了环境并添加了必要的变量
- [ ] 登录接口配置了后置脚本
- [ ] 测试登录接口，Token成功保存
- [ ] 其他接口添加了 Authorization Header
- [ ] 测试至少一个需要认证的接口
- [ ] 了解如何查看和更新Token

---

**🎉 配置完成后，你就可以：**
1. 登录一次
2. Token自动保存
3. 后续所有接口自动携带Token
4. Token过期后重新登录即可

**无需每次手动复制粘贴Token！** 🚀

