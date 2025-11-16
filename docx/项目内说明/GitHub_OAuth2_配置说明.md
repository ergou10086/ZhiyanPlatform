# GitHub OAuth2 第三方登录配置说明

## 一、在GitHub上注册OAuth App

### 1. 进入GitHub开发者设置页面

1. 登录GitHub账号
2. 点击右上角头像 → **Settings（设置）**
3. 在左侧菜单栏找到 **Developer settings（开发者设置）**
4. 点击 **OAuth Apps（OAuth应用）**
5. 点击右上角的 **New OAuth App（新建OAuth应用）** 按钮

### 2. 填写OAuth App信息

在注册表单中，需要填写以下信息：

#### **Application name（应用名称）** ⭐ 必填
- **填写内容**：`智研平台` 或 `Zhiyan Platform`（或其他你喜欢的名称）
- **说明**：这是用户授权时会看到的应用名称，建议使用有意义的名称

#### **Homepage URL（主页URL）** ⭐ 必填
- **开发环境填写**：`http://localhost:8091`
- **生产环境填写**：`https://yourdomain.com`（你的实际域名）
- **说明**：这是你的应用主页地址

#### **Application description（应用描述）** 可选
- **填写内容**：`智研平台 - 知识管理与协作平台`（或其他描述）
- **说明**：应用的简短描述，会在授权页面显示给用户

#### **Authorization callback URL（授权回调URL）** ⭐ 必填 ⚠️ **最重要**
- **开发环境填写**：`http://localhost:8091/zhiyan/auth/oauth2/callback/github`
- **生产环境填写**：`https://yourdomain.com/zhiyan/auth/oauth2/callback/github`
- **说明**：
  - 这是GitHub授权成功后回调的地址
  - **必须与配置文件中的回调地址完全一致**
  - 格式：`{callback-base-url}/zhiyan/auth/oauth2/callback/github`
  - 其中 `{callback-base-url}` 对应配置文件中的 `zhiyan.oauth2.callback-base-url`

#### **Enable Device Flow（启用设备流）** 可选
- **建议**：不勾选（除非你需要设备流授权）
- **说明**：设备流用于无法直接打开浏览器的设备，一般不需要

### 3. 注册应用

填写完成后，点击 **Register application（注册应用）** 按钮。

### 4. 获取Client ID和Client Secret

注册成功后，GitHub会显示：

- **Client ID**：一个公开的标识符，可以暴露在前端代码中
- **Client Secret**：一个密钥，**必须保密**，只能在后端使用

⚠️ **重要提示**：
- **Client Secret** 只显示一次，请立即保存
- 如果丢失，需要重新生成（点击 **Generate a new client secret**）

## 二、配置应用

### 1. 在配置文件中设置

将获取到的 `Client ID` 和 `Client Secret` 配置到 `application-dev.yml`（或其他环境的配置文件）：

```yaml
zhiyan:
  oauth2:
    enabled: true
    callback-base-url: http://localhost:8091  # 开发环境
    github:
      enabled: true
      client-id: your-github-client-id        # 替换为你的Client ID
      client-secret: your-github-client-secret # 替换为你的Client Secret
      scope: read:user,user:email
```

### 2. 使用环境变量（推荐）

为了安全，建议使用环境变量：

```yaml
zhiyan:
  oauth2:
    github:
      client-id: ${GITHUB_CLIENT_ID:your-github-client-id}
      client-secret: ${GITHUB_CLIENT_SECRET:your-github-client-secret}
```

然后在启动应用时设置环境变量：
- Windows: `set GITHUB_CLIENT_ID=your-id && set GITHUB_CLIENT_SECRET=your-secret`
- Linux/Mac: `export GITHUB_CLIENT_ID=your-id && export GITHUB_CLIENT_SECRET=your-secret`

## 三、回调URL配置说明

### 回调URL的组成

回调URL由以下部分组成：
```
{callback-base-url}/zhiyan/auth/oauth2/callback/{provider}
```

- `{callback-base-url}`：配置文件中的 `zhiyan.oauth2.callback-base-url`
- `/zhiyan/auth/oauth2/callback/`：固定的回调路径前缀
- `{provider}`：提供商名称（如：`github`）

### 示例

#### 开发环境
- **配置文件**：`callback-base-url: http://localhost:8091`
- **GitHub回调URL**：`http://localhost:8091/zhiyan/auth/oauth2/callback/github`

#### 生产环境
- **配置文件**：`callback-base-url: https://api.yourdomain.com`
- **GitHub回调URL**：`https://api.yourdomain.com/zhiyan/auth/oauth2/callback/github`

⚠️ **重要**：
- GitHub上的回调URL必须与配置文件中的回调URL完全一致
- 包括协议（http/https）、域名、端口、路径
- 如果使用网关，回调URL应该是网关地址，而不是服务地址

## 四、测试OAuth2登录

### 1. 启动应用

确保应用已启动，并且OAuth2配置已正确加载。

### 2. 获取授权URL

调用接口获取GitHub授权URL：
```http
GET http://localhost:8091/zhiyan/auth/oauth2/authorize/github
```

响应示例：
```json
{
  "code": 200,
  "msg": "获取授权URL成功",
  "data": {
    "authorizationUrl": "https://github.com/login/oauth/authorize?client_id=xxx&redirect_uri=xxx&state=xxx&response_type=code&scope=read:user,user:email",
    "state": "abc123..."
  }
}
```

### 3. 用户授权

1. 前端跳转到 `authorizationUrl`
2. 用户在GitHub页面点击 **Authorize（授权）**
3. GitHub会重定向到回调URL，携带 `code` 和 `state` 参数

### 4. 自动登录

后端会自动：
1. 验证 `state` 参数（防CSRF攻击）
2. 通过 `code` 获取访问令牌
3. 获取用户信息
4. 如果用户不存在，自动注册
5. 如果用户已存在，直接登录
6. 返回JWT Token和用户信息

## 五、常见问题

### 1. 回调URL不匹配

**错误信息**：`redirect_uri_mismatch`

**解决方法**：
- 检查GitHub上的回调URL是否与配置文件中的完全一致
- 注意协议（http/https）、端口、路径都要一致

### 2. Client Secret错误

**错误信息**：`bad_verification_code` 或 `invalid_client`

**解决方法**：
- 检查Client Secret是否正确
- 如果重新生成了Client Secret，需要更新配置文件

### 3. 无法获取邮箱

**问题**：用户信息中邮箱为null

**解决方法**：
- 确保scope中包含 `user:email`
- 检查GitHub用户是否设置了邮箱隐私（某些用户可能不公开邮箱）

### 4. 本地开发回调问题

**问题**：本地开发时，GitHub无法回调到 `localhost`

**解决方法**：
- 使用内网穿透工具（如：ngrok、natapp）
- 或者使用GitHub的测试环境（如果有）

## 六、生产环境配置

### 1. 更新回调URL

在生产环境部署时，需要：
1. 在GitHub上更新OAuth App的回调URL
2. 更新配置文件中的 `callback-base-url`

### 2. 安全建议

- ✅ 使用HTTPS
- ✅ Client Secret使用环境变量或配置中心（如Nacos）
- ✅ 不要将Client Secret提交到代码仓库
- ✅ 定期轮换Client Secret

## 七、相关文档

- [GitHub OAuth文档](https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps)
- [GitHub OAuth Scopes](https://docs.github.com/en/developers/apps/building-oauth-apps/scopes-for-oauth-apps)
- [项目OAuth2模块文档](../zhiyan-common/zhiyan-common-oauth/README.md)

