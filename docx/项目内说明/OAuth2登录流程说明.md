# OAuth2第三方登录流程说明

## 一、新的登录策略

### 核心原则
1. **不自动创建账号**：避免自动生成密码、占位符邮箱等不可靠信息
2. **引导用户操作**：根据情况引导用户绑定已有账号或补充信息创建账号
3. **安全验证**：绑定账号时必须验证密码，确保账号安全

## 二、登录流程

### 场景1：邮箱匹配到已有账号 ✅
**流程**：
1. 用户通过GitHub授权
2. 后端获取OAuth2用户信息（包含邮箱）
3. 系统检查邮箱是否已注册
4. **如果邮箱已注册**：直接登录成功，返回JWT Token

**响应示例**：
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "status": "SUCCESS",
    "loginResponse": {
      "user": {...},
      "accessToken": "...",
      "refreshToken": "..."
    }
  }
}
```

### 场景2：邮箱未匹配，需要绑定或创建 ⚠️
**流程**：
1. 用户通过GitHub授权
2. 后端获取OAuth2用户信息（包含邮箱，但该邮箱未注册）
3. 系统返回 `NEED_BIND` 状态
4. **前端引导用户选择**：
   - **选项A：绑定已有账号**：用户输入已有账号的邮箱和密码
   - **选项B：创建新账号**：用户补充信息（邮箱+密码）创建账号

**响应示例**：
```json
{
  "code": 200,
  "msg": "需要绑定或创建账号",
  "data": {
    "status": "NEED_BIND",
    "oauth2UserInfo": {
      "provider": "github",
      "providerUserId": "123456",
      "email": "user@example.com",
      "username": "githubuser",
      "nickname": "GitHub User",
      "avatarUrl": "https://..."
    },
    "message": "检测到邮箱 user@example.com 未注册，请绑定已有账号或创建新账号"
  }
}
```

### 场景3：OAuth2信息不足（缺少邮箱） ⚠️
**流程**：
1. 用户通过GitHub授权
2. 后端获取OAuth2用户信息（**未提供邮箱**）
3. 系统返回 `NEED_SUPPLEMENT` 状态
4. **前端必须引导用户补充信息**：邮箱 + 密码

**响应示例**：
```json
{
  "code": 200,
  "msg": "需要补充信息",
  "data": {
    "status": "NEED_SUPPLEMENT",
    "oauth2UserInfo": {
      "provider": "github",
      "providerUserId": "123456",
      "email": null,
      "username": "githubuser",
      "nickname": "GitHub User",
      "avatarUrl": "https://..."
    },
    "message": "请补充邮箱和密码创建账号"
  }
}
```

## 三、API接口

### 1. 获取授权URL
```http
GET /zhiyan/auth/oauth2/authorize/{provider}
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "authorizationUrl": "https://github.com/login/oauth/authorize?...",
    "state": "abc123..."
  }
}
```

### 2. OAuth2回调
```http
GET /zhiyan/auth/oauth2/callback/{provider}?code=xxx&state=xxx
```

**响应**：根据情况返回不同的状态（见上面的场景说明）

### 3. 绑定已有账号
```http
POST /zhiyan/auth/oauth2/bind
Content-Type: application/json

{
  "provider": "github",
  "providerUserId": "123456",
  "email": "user@example.com",
  "password": "password123",
  "oauth2UserInfo": {
    "provider": "github",
    "providerUserId": "123456",
    "email": "user@example.com",
    "username": "githubuser",
    "nickname": "GitHub User",
    "avatarUrl": "https://..."
  }
}
```

**说明**：
- `email` 必须与 `oauth2UserInfo.email` 一致（安全考虑）
- `password` 是已有账号的密码（用于验证）
- `oauth2UserInfo` 应该从回调接口的响应中获取

**响应**：
```json
{
  "code": 200,
  "msg": "绑定成功",
  "data": {
    "status": "SUCCESS",
    "loginResponse": {...}
  }
}
```

### 4. 补充信息创建账号
```http
POST /zhiyan/auth/oauth2/supplement
Content-Type: application/json

{
  "provider": "github",
  "providerUserId": "123456",
  "email": "user@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "oauth2UserInfo": {
    "provider": "github",
    "providerUserId": "123456",
    "email": null,
    "username": "githubuser",
    "nickname": "GitHub User",
    "avatarUrl": "https://..."
  }
}
```

**说明**：
- `email` 用户输入的邮箱（必须未注册）
- `password` 和 `confirmPassword` 必须一致
- `oauth2UserInfo` 应该从回调接口的响应中获取

**响应**：
```json
{
  "code": 200,
  "msg": "创建账号成功",
  "data": {
    "status": "SUCCESS",
    "loginResponse": {...}
  }
}
```

## 四、前端处理流程

### 流程图
```
用户点击GitHub登录
    ↓
调用 /authorize/github 获取授权URL
    ↓
跳转到GitHub授权页面
    ↓
用户授权后，GitHub回调到 /callback/github
    ↓
后端返回 OAuth2LoginResponse
    ↓
判断 status：
    ├─ SUCCESS → 直接登录，保存Token
    ├─ NEED_BIND → 显示绑定/创建选择界面
    │   ├─ 选择绑定 → 调用 /bind 接口
    │   └─ 选择创建 → 调用 /supplement 接口
    └─ NEED_SUPPLEMENT → 显示补充信息表单
        └─ 用户填写后 → 调用 /supplement 接口
```

### 前端代码示例（伪代码）

```javascript
// 1. 获取授权URL
async function getAuthUrl(provider) {
  const response = await fetch(`/zhiyan/auth/oauth2/authorize/${provider}`);
  const result = await response.json();
  if (result.code === 200) {
    // 跳转到授权URL
    window.location.href = result.data.authorizationUrl;
  }
}

// 2. 处理回调
async function handleCallback(provider, code, state) {
  const response = await fetch(
    `/zhiyan/auth/oauth2/callback/${provider}?code=${code}&state=${state}`
  );
  const result = await response.json();
  
  if (result.code === 200) {
    const data = result.data;
    
    switch (data.status) {
      case 'SUCCESS':
        // 直接登录成功
        saveToken(data.loginResponse);
        redirectToHome();
        break;
        
      case 'NEED_BIND':
        // 显示绑定/创建选择界面
        showBindOrCreateDialog(data.oauth2UserInfo);
        break;
        
      case 'NEED_SUPPLEMENT':
        // 显示补充信息表单
        showSupplementForm(data.oauth2UserInfo);
        break;
    }
  }
}

// 3. 绑定已有账号
async function bindAccount(oauth2UserInfo, email, password) {
  const response = await fetch('/zhiyan/auth/oauth2/bind', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      provider: oauth2UserInfo.provider,
      providerUserId: oauth2UserInfo.providerUserId,
      email: email,
      password: password,
      oauth2UserInfo: oauth2UserInfo
    })
  });
  
  const result = await response.json();
  if (result.code === 200 && result.data.status === 'SUCCESS') {
    saveToken(result.data.loginResponse);
    redirectToHome();
  }
}

// 4. 补充信息创建账号
async function supplementInfo(oauth2UserInfo, email, password, confirmPassword) {
  const response = await fetch('/zhiyan/auth/oauth2/supplement', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      provider: oauth2UserInfo.provider,
      providerUserId: oauth2UserInfo.providerUserId,
      email: email,
      password: password,
      confirmPassword: confirmPassword,
      oauth2UserInfo: oauth2UserInfo
    })
  });
  
  const result = await response.json();
  if (result.code === 200 && result.data.status === 'SUCCESS') {
    saveToken(result.data.loginResponse);
    redirectToHome();
  }
}
```

## 五、安全考虑

### 1. 绑定账号安全
- ✅ 只能绑定OAuth2提供的邮箱对应的账号
- ✅ 必须验证密码
- ✅ 验证OAuth2用户ID匹配

### 2. 创建账号安全
- ✅ 邮箱必须未注册
- ✅ 密码强度验证
- ✅ 密码一致性验证
- ✅ 验证OAuth2用户ID匹配

### 3. 信息完整性
- ✅ 使用OAuth2提供的头像、昵称等信息
- ✅ 不自动生成占位符信息
- ✅ 用户必须主动提供必要信息


## 六、注意事项

1. **前端必须保存OAuth2UserInfo**：在回调接口返回后，前端需要保存 `oauth2UserInfo`，以便后续绑定或补充信息时使用

2. **邮箱一致性**：绑定账号时，输入的邮箱必须与OAuth2提供的邮箱一致

3. **错误处理**：
   - 绑定失败：提示用户密码错误或账号不存在
   - 创建失败：提示用户邮箱已注册或信息不完整

4. **用户体验优化**：
   - 在 `NEED_BIND` 状态下，可以预填OAuth2的邮箱
   - 在 `NEED_SUPPLEMENT` 状态下，可以预填OAuth2的昵称和头像

