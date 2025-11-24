package hbnu.project.zhiyanauth.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 知识库服务 Feign 客户端
 * 用于查询成果信息
 *
 * @author ErgouTree
 */
@FeignClient(name = "zhiyan-knowledge", contextId = "authKnowledgeClient")
public interface KnowledgeServiceClient {

    /**
     * 根据成果ID查询成果详情
     *
     * @param achievementId 成果ID
     * @return 成果信息
     */
    @GetMapping("/zhiyan/achievement/{achievementId}")
    R<Object> getAchievementById(@PathVariable("achievementId") Long achievementId);


    /**
     * 根据项目ID查询公开成果列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param size 每页数量
     * @return 公开成果列表
     */
    @GetMapping("/zhiyan/achievement/search/project/{projectId}")
    R<Object> getPublicAchievementsByProject(
            @PathVariable("projectId") Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    );


    /**
     * 批量查询成果信息
     *
     * @param achievementIds 成果ID列表（逗号分隔）
     * @return 成果列表
     */
    @GetMapping("/zhiyan/achievement/batch")
    R<Object> getAchievementsByIds(@RequestParam("ids") String achievementIds);
}
