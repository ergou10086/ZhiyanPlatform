package hbnu.project.zhiyanaidify.service;

import hbnu.project.zhiyanaidify.model.request.TaskResultGenerateRequest;
import hbnu.project.zhiyanaidify.model.response.TaskResultGenerateResponse;

import java.util.List;

/**
 * 任务成果AI生成服务
 * 负责调用Dify生成任务成果草稿
 * 
 * 职责说明：
 * 1. 管理AI生成任务的异步执行
 * 2. 调用项目服务获取任务详情和提交记录
 * 3. 构建AI提示词并调用Dify API
 * 4. 解析AI返回并转换为TaskResultDetailDTO
 * 5. 管理生成任务的状态（Redis存储）
 * 
 * @author Tokito
 */
public interface TaskResultAIGenerateService {
    
    /**
     * 生成任务成果草稿
     * 异步任务，返回任务ID
     * 
     * @param request 生成请求
     * @return 生成任务ID
     */
    String generateTaskResultDraft(TaskResultGenerateRequest request);
    
    /**
     * 查询生成任务状态
     * 
     * @param jobId 任务ID
     * @param userId 用户ID（用于权限验证）
     * @return 任务状态和结果
     */
    TaskResultGenerateResponse getGenerateStatus(String jobId, Long userId);
    
    /**
     * 取消生成任务
     * 
     * @param jobId 任务ID
     * @param userId 用户ID（用于权限验证）
     */
    void cancelGenerate(String jobId, Long userId);
    
    /**
     * 获取用户的AI草稿列表
     * 
     * @param userId 用户ID
     * @return 草稿列表（按创建时间倒序）
     */
    List<TaskResultGenerateResponse> getAIDrafts(Long userId);
}




