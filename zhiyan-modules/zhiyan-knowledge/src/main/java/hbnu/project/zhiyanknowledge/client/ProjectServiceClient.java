package hbnu.project.zhiyanknowledge.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 项目服务Feign客户端
 * 用于调用项目模块的API检查权限
 *
 * @author ErgouTree
 */
@FeignClient(name = "zhiyan-project", path = "/api/projects")
public interface ProjectServiceClient {

    /**
     * 检查用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为项目成员
     */
    @GetMapping("/{projectId}/members/check")
    Boolean isProjectMember(@PathVariable("projectId") Long projectId,
                            @RequestParam("userId") Long userId);


    /**
     * 检查用户是否为项目拥有者
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为项目拥有者
     */
    @GetMapping("/{projectId}/owner/check")
    Boolean isProjectOwner(@PathVariable("projectId") Long projectId,
                           @RequestParam("userId") Long userId);


    /**
     * 检查用户是否有指定权限
     *
     * @param projectId  项目ID
     * @param userId     用户ID
     * @param permission 权限代码
     * @return 是否有权限
     */
    @GetMapping("/{projectId}/permissions/check")
    Boolean hasPermission(@PathVariable("projectId") Long projectId,
                          @RequestParam("userId") Long userId,
                          @RequestParam("permission") String permission);
}
