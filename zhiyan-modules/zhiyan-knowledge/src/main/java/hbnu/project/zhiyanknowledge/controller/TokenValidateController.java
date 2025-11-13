package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.client.AuthServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Token验证控制器
 * 提供token验证接口，调用auth模块进行验证
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/zhiyan/achievement/token")    //未修改
@Tag(name = "Token验证", description = "Token验证相关接口")
@AccessLog("Token验证")
public class TokenValidateController {

    private final AuthServiceClient authServiceClient;

    /**
     * 验证token有效性
     * 调用auth模块的验证接口，检查token是否有效
     */
    @GetMapping("/validate")
    @Operation(summary = "验证Token", description = "验证JWT Token是否有效，返回用户信息")
    public R<AuthServiceClient.TokenValidateResponse> validateToken(
            @Parameter(description = "Authorization token (Bearer xxx)") 
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("验证Token请求: Authorization header存在={}", authHeader != null);
        
        try {
            // 调用auth模块的验证接口
            R<AuthServiceClient.TokenValidateResponse> result = authServiceClient.validateToken(authHeader);
            
            if (R.isSuccess(result) && result.getData() != null) {
                log.info("Token验证成功: userId={}", result.getData().getUserId());
                return R.ok(result.getData(), "Token验证成功");
            } else {
                log.warn("Token验证失败");
                return R.fail(401, "Token验证失败");
            }
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return R.fail(401, "Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 检查token是否有效（简化版本）
     * 只返回true/false，不返回详细信息
     */
    @GetMapping("/check")
    @Operation(summary = "检查Token", description = "简单检查Token是否有效")
    public R<Boolean> checkToken(
            @Parameter(description = "Authorization token (Bearer xxx)") 
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("检查Token请求: Authorization header存在={}", authHeader != null);
        
        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.warn("Token检查失败: Authorization header为空");
            return R.ok(false, "未提供Token");
        }
        
        try {
            R<AuthServiceClient.TokenValidateResponse> result = authServiceClient.validateToken(authHeader);
            
            if (R.isSuccess(result) && result.getData() != null && result.getData().getUserId() != null) {
                log.info("Token检查成功: userId={}", result.getData().getUserId());
                return R.ok(Boolean.TRUE, "Token有效");
            } else {
                log.warn("Token检查失败");
                return R.ok(Boolean.FALSE, "Token无效或已过期");
            }
        } catch (Exception e) {
            log.error("Token检查异常", e);
            return R.ok(Boolean.FALSE, "Token验证异常: " + e.getMessage());
        }
    }

    /**
     * 获取当前token对应的用户ID
     * 用于文件下载等需要用户ID的场景
     */
    @GetMapping("/user-id")
    @Operation(summary = "获取用户ID", description = "从Token中获取用户ID")
    public R<Long> getUserIdFromToken(
            @Parameter(description = "Authorization token (Bearer xxx)") 
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("获取用户ID请求");
        
        try {
            R<AuthServiceClient.TokenValidateResponse> result = authServiceClient.validateToken(authHeader);
            
            if (R.isSuccess(result) && result.getData() != null) {
                String userIdStr = result.getData().getUserId();
                if (userIdStr != null) {
                    Long userId = Long.parseLong(userIdStr);
                    log.info("成功获取用户ID: {}", userId);
                    return R.ok(userId, "获取用户ID成功");
                }
            }
            
            log.warn("无法从Token获取用户ID");
            return R.fail(401, "无法从Token获取用户ID");
        } catch (Exception e) {
            log.error("获取用户ID异常", e);
            return R.fail(401, "获取用户ID失败: " + e.getMessage());
        }
    }
}

