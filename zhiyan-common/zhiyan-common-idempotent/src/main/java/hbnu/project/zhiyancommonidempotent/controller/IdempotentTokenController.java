package hbnu.project.zhiyancommonidempotent.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonidempotent.service.IdempotentTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 幂等Token控制器
 * 提供Token的申请接口
 *
 * @author yui
 */
@Tag(name = "幂等Token管理")
@RestController
@RequestMapping("/idempotent")
@RequiredArgsConstructor
public class IdempotentTokenController {

    private final IdempotentTokenService idempotentTokenService;

    /**
     * 申请幂等Token（默认5分钟过期）
     *
     * @return Token信息
     */
    @Operation(summary = "申请幂等Token")
    @GetMapping("/token")
    public R<Map<String, String>> generateToken() {
        String token = idempotentTokenService.generateToken();
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("header", "Idempotent-Token");
        return R.ok(result);
    }

    /**
     * 申请幂等Token并指定过期时间
     *
     * @param timeout 过期时间（秒），默认300秒
     * @return Token信息
     */
    @Operation(summary = "申请幂等Token（指定过期时间）")
    @GetMapping("/token/{timeout}")
    public R<Map<String, String>> generateTokenWithTimeout(@PathVariable Long timeout) {
        if (timeout == null || timeout <= 0) {
            timeout = 300L;
        }
        String token = idempotentTokenService.generateToken(timeout);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("header", "Idempotent-Token");
        result.put("timeout", timeout.toString());
        return R.ok(result);
    }

    /**
     * 验证Token是否有效（不消费）
     *
     * @param token Token字符串
     * @return 验证结果
     */
    @Operation(summary = "验证Token是否有效")
    @GetMapping("/validate")
    public R<Boolean> validateToken(@RequestParam String token) {
        boolean valid = idempotentTokenService.validateToken(token);
        return R.ok(valid);
    }

    /**
     * 手动删除Token（取消操作时使用）
     *
     * @param token Token字符串
     * @return 删除结果
     */
    @Operation(summary = "删除Token")
    @DeleteMapping("/token")
    public R<Boolean> removeToken(@RequestParam String token) {
        boolean removed = idempotentTokenService.removeToken(token);
        return R.ok(removed);
    }
}

