package hbnu.project.zhiyanaidify.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 项目任务 Feign 客户端（AI 模块）
 *
 * 作用：
 * - 为 zhiyan-ai-dify 模块提供按项目与状态查询任务的能力
 * - 主要用于从项目模块获取状态为 DONE 的任务列表，作为 AI 生成“任务成果型”内容的输入
 *
 * 说明：
 * - 对接 zhiyan-project 模块 TaskController：
 *   基础路径：/zhiyan/projects/tasks
 *   接口：GET /projects/{projectId}/status/{status}
 * - 这里同样先返回泛型 R<Object>，由 AI 侧服务根据需要映射为内部 DTO。
 */
@FeignClient(name = "zhiyan-project", path = "/zhiyan/projects/tasks")
public interface ProjectTaskClient {

    /**
     * 按状态分页获取项目任务列表。
     *
     * 常用场景：status = "DONE"，获取某项目下已完成任务列表，
     * 作为 AI 分析和生成 TaskResultDetailDTO 的输入数据之一。
     *
     * @param projectId 项目 ID
     * @param status    任务状态字符串（如：TODO/IN_PROGRESS/BLOCKED/DONE）
     * @param page      页码，从 0 开始
     * @param size      每页大小
     * @return 通用响应体，data 为项目模块返回的分页任务数据结构
     */
    @GetMapping("/projects/{projectId}/status/{status}")
    R<Object> getTasksByStatus(
            @PathVariable("projectId") Long projectId,
            @PathVariable("status") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );
}
