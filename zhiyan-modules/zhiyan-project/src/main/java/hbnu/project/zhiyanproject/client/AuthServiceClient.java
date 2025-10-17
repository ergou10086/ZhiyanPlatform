package hbnu.project.zhiyanproject.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Auth服务Feign客户端
 * 用于调用用户认证服务的接口
 *
 * @author Tokito
 */
@FeignClient(name = "zhiyan-auth-service", path = "/auth/users")
public interface AuthServiceClient {

    /**
     * 根据邮箱查询用户信息
     *
     * @param email 用户邮箱
     * @return 用户信息
     */
    @GetMapping("/email")
    R<UserDTO> getUserByEmail(@RequestParam("email") String email);

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    R<UserDTO> getUserById(@PathVariable("id") Long id);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID到用户信息的映射
     */
    @GetMapping("/batch")
    R<Map<Long, UserDTO>> getUsersByIds(@RequestParam("ids") List<Long> userIds);
}