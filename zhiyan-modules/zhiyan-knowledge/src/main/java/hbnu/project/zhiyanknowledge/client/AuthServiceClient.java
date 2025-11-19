package hbnu.project.zhiyanknowledge.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.model.dto.TokenValidateResponseDTO;
import hbnu.project.zhiyanknowledge.model.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Auth服务Feign客户端
 * 用于调用认证服务的接口
 *
 * @author ErgouTree
 */
@FeignClient(name = "zhiyan-auth-service", url = "http://localhost:8091", path = "/zhiyan/auth")
public interface AuthServiceClient {

    /**
     * 验证令牌有效性
     * 
     * @param token Authorization header中的token（包含"Bearer "前缀）
     * @return 验证结果，包含用户ID、角色等信息
     */
    @GetMapping("/validate")
    R<TokenValidateResponseDTO> validateToken(@RequestHeader("Authorization") String token);


    /**
     * 批量根据用户ID列表获取用户信息（服务间调用接口）
     * 用于其他微服务批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @PostMapping("users/batch-query")
    R<List<UserDTO>> getUsersByIds(@RequestBody List<Long> userIds);

    /**
     * 根据用户ID获取用户信息（服务间调用接口）
     * 用于其他微服务通过Feign调用查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/users/internal/{userId}")
    R<UserDTO> getUserById(@PathVariable("userId") Long userId);
}

