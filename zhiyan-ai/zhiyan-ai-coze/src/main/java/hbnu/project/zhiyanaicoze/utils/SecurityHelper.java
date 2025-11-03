package hbnu.project.zhiyanaicoze.utils;

import hbnu.project.zhiyanaicoze.client.AuthServiceClient;
import hbnu.project.zhiyanaicoze.model.response.TokenValidateResponse;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 安全辅助工具类
 * 通过调用 Auth 服务来验证 Token 并获取用户信息
 * Coze 模块不直接解析 JWT，而是通过 HTTP 调用 Auth 服务进行验证
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final AuthServiceClient authServiceClient;

    /**
     * 从 Authorization 请求头中提取并验证 Token，获取用户ID
     *
     * @param authorizationHeader Authorization 请求头（格式：Bearer {token}）
     * @return 用户ID，如果 Token 无效或未提供则返回 null
     */
    public Long getUserId(String authorizationHeader) {
        if (StringUtils.isBlank(authorizationHeader)) {
            log.warn("[SecurityHelper] Authorization 头为空");
            return null;
        }

        // 确保 Authorization 头包含 Bearer 前缀
        String token = authorizationHeader;
        if (!authorizationHeader.startsWith("Bearer ")) {
            token = "Bearer " + authorizationHeader;
        }

        log.debug("[SecurityHelper] 开始验证 Token，通过 Auth 服务调用");

        try {
            // 通过 Auth 服务验证 Token
            TokenValidateResponse response = authServiceClient.validateToken(token)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (response == null) {
                log.warn("[SecurityHelper] Auth 服务返回 null");
                return null;
            }

            if (Boolean.TRUE.equals(response.getIsValid()) && StringUtils.isNotBlank(response.getUserId())) {
                try {
                    Long userId = Long.parseLong(response.getUserId());
                    log.debug("[SecurityHelper] Token 验证成功，用户ID: {}", userId);
                    return userId;
                } catch (NumberFormatException e) {
                    log.error("[SecurityHelper] 用户ID格式错误: {}", response.getUserId(), e);
                    return null;
                }
            } else {
                log.warn("[SecurityHelper] Token 验证失败: {}", response.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("[SecurityHelper] 调用 Auth 服务验证 Token 时发生异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从 Spring Security 上下文中获取当前登录用户ID
     * 注意：Coze 模块未启用完整的 Spring Security，此方法需要从请求头中获取
     * 此方法保留用于兼容性，实际应使用 getUserId(String authorizationHeader)
     *
     * @return 用户ID，如果无法获取则返回 null
     */
    public Long getUserId() {
        log.warn("[SecurityHelper] getUserId() 方法需要在 Controller 中传入 Authorization 头，请使用 getUserId(String authorizationHeader)");
        return null;
    }
}
