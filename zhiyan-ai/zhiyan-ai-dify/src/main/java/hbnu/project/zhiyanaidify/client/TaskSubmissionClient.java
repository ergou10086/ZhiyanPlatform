package hbnu.project.zhiyanaidify.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanaidify.model.dto.TaskResultContextDTO;
import hbnu.project.zhiyanaidify.model.dto.TaskSubmissionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 任务提交Feign客户端
 * 用于调用zhiyan-project模块的任务提交接口
 *
 * @author Tokito
 */
@FeignClient(name = "zhiyan-project", contextId = "zhiyan-project-ai-dify", path = "/zhiyan/projects/tasks/submissions")
public interface TaskSubmissionClient {

    /**
     * 获取任务的最新提交记录
     *
     * @param taskId 任务ID
     * @return 任务提交DTO
     */
    @GetMapping("/task/{taskId}/latest")
    R<TaskSubmissionDTO> getLatestSubmission(@PathVariable("taskId") Long taskId);

    /**
     * 获取任务的所有提交记录
     *
     * @param taskId 任务ID
     * @return 任务提交DTO列表
     */
    @GetMapping("/task/{taskId}")
    R<List<TaskSubmissionDTO>> getTaskSubmissions(@PathVariable("taskId") Long taskId);

    /**
     * 获取任务的成果上下文（任务详情 + 所有提交记录）
     *
     * @param taskId 任务ID
     * @return 任务成果上下文DTO
     */
    @GetMapping("/task/{taskId}/result-context")
    R<TaskResultContextDTO> getTaskResultContext(@PathVariable("taskId") Long taskId);
}
