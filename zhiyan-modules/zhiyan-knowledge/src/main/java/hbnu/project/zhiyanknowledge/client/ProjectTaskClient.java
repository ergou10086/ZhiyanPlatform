package hbnu.project.zhiyanknowledge.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 项目任务 Feign 客户端
 *
 * 作用：
 * - 为 Knowledge 模块/TaskResult 相关功能提供按项目与状态查询任务的能力
 * - 目前主要用于查询指定项目下状态为 DONE 的任务列表，供成果关联和 AI 任务成果生成使用
 *
 * 说明：
 * - 直接对接 zhiyan-project 模块中的 TaskController
 *   基础路径：/zhiyan/projects/tasks
 *   接口：GET /projects/{projectId}/status/{status}
 * - 为了避免跨模块强耦合，这里先返回泛型 R<Object>，
 *   由上层服务在需要时将结果映射为 TaskResultTaskRefDTO 等内部 DTO。
 */
@FeignClient(
        name = "zhiyan-project",
        contextId = "zhiyanProjectTaskClient",
        url = "http://localhost:8095",
        path = "/zhiyan/projects/tasks"
)
public interface ProjectTaskClient {

    /**
     * 按状态分页获取项目任务列表。
     *
     * 常用场景：传入 status = "DONE"，获取项目内已完成任务，用于成果关联或 AI 生成成果。
     *
     * @param projectId 项目 ID
     * @param status    任务状态字符串（如：TODO/IN_PROGRESS/BLOCKED/DONE）
     * @param page      页码，从 0 开始
     * @param size      每页大小
     * @return 通用响应体，data 字段为分页结果（由项目模块返回的 Page<TaskDetailDTO> 序列化结构）
     */
    @GetMapping("/projects/{projectId}/status/{status}")
    R<Object> getTasksByStatus(
            @PathVariable("projectId") Long projectId,
            @PathVariable("status") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );
}
