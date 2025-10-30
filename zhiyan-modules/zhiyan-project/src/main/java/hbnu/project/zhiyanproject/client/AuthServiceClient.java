package hbnu.project.zhiyanproject.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.dto.PageResult;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Auth服务Feign客户端
 * 用于调用用户认证服务的接口
 *
 * @author Tokito
 */
@FeignClient(name = "zhiyan-auth-service", url = "http://localhost:8091", path = "/zhiyan/users")
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
     * 根据姓名查询用户信息
     *
     * @param name 用户姓名
     * @return 用户信息
     */
    @GetMapping("/name")
    R<UserDTO> getUserByName(@RequestParam("name") String name);

    /**
     * 根据用户ID查询用户信息（使用内部接口，无需权限）
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/internal/{id}")
    R<UserDTO> getUserById(@PathVariable("id") Long id);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @PostMapping("/batch-query")
    R<List<UserDTO>> getUsersByIds(@RequestBody List<Long> userIds);

    /**
     * 搜索用户（用于项目成员邀请等场景）
     * 根据关键词搜索用户（姓名、邮箱等）
     *
     * @param keyword 搜索关键词
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 用户信息分页列表
     */
    @GetMapping("/search")
    R<PageResult<UserDTO>> searchUsers(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );
}