// 将此代码添加到 SecurityConfig.java 的 filterChain 方法中
// 位置：在 "其他所有接口需要认证" 之前

// ========== 开发测试模式 - 临时放开项目模块所有GET接口 ==========
// TODO: 上线前必须删除此配置！仅用于开发测试！
.requestMatchers(
        org.springframework.http.HttpMethod.GET,
        "/api/projects/**",
        "/api/project-members/**",
        "/api/tasks/**"
).permitAll()
// ========== 开发测试模式结束 ==========

