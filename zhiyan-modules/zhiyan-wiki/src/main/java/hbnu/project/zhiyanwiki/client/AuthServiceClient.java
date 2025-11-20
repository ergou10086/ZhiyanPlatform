package hbnu.project.zhiyanwiki.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanwiki.model.dto.UserDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 认证部分的feign远程调用
 *
 * @author ErgouTree
 */
@FeignClient(name = "zhiyan-auth", url = "http://localhost:8091", path = "/zhiyan/auth")
public interface AuthServiceClient {

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
