package hbnu.project.zhiyanaidify.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务成果生成响应
 * 用于返回AI生成任务成果草稿的状态和结果
 * 
 * 注意：使用Map存储草稿内容，避免依赖knowledge模块
 * 草稿内容的结构应符合TaskResultDetailDTO的格式（由knowledge模块定义）
 * 
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultGenerateResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 生成任务ID（必填）
     * 用于追踪生成任务的唯一标识
     */
    private String jobId;

    /**
     * 任务状态（必填）
     * 可选值：
     * - PENDING：排队中
     * - PROCESSING：生成中
     * - COMPLETED：已完成
     * - FAILED：失败
     * - CANCELLED：已取消
     */
    private String status;

    /**
     * 生成完成后的草稿内容（可选）
     * 当status为COMPLETED时，此字段包含生成的草稿内容
     * 使用Map存储，避免跨模块依赖
     * Map结构应符合TaskResultDetailDTO的格式：
     * {
     *   "schemaVersion": "1.0",
     *   "source": "AI",
     *   "linkedTaskIds": [1, 2, 3],
     *   "summary": "...",
     *   "sections": [...],
     *   ...
     * }
     */
    private Map<String, Object> draftContent;

    /**
     * 错误信息（可选）
     * 当status为FAILED时，此字段包含错误描述
     */
    private String errorMessage;

    /**
     * 进度百分比（可选）
     * 范围：0-100，表示生成进度
     */
    @Builder.Default
    private Integer progress = 0;

    /**
     * 用户ID（必填）
     * 用于标识生成任务的用户
     */
    private Long userId;

    /**
     * 项目ID（必填）
     * 用于标识成果所属的项目
     */
    private Long projectId;

    /**
     * 创建时间（可选）
     * 记录生成任务的创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间（可选）
     * 记录生成任务的最后更新时间
     */
    private LocalDateTime updatedAt;
}

