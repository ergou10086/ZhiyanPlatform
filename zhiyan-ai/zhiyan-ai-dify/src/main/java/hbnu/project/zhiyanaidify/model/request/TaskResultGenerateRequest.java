package hbnu.project.zhiyanaidify.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务成果生成请求
 * 用于AI生成任务成果草稿的请求参数
 * 
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultGenerateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID（必填）
     * 用于标识成果所属的项目
     */
    private Long projectId;

    /**
     * 成果标题（可选）
     * 如果为空，AI会根据任务信息自动生成标题
     */
    private String achievementTitle;

    /**
     * 关联的任务ID列表（必填）
     * 用于生成成果的任务列表，至少需要一个任务
     */
    @Builder.Default
    private List<Long> taskIds = new ArrayList<>();

    /**
     * 目标读者（可选）
     * 用于指导AI生成内容的风格和深度
     * 例如：技术人员、管理人员、学术研究人员等
     */
    private String targetAudience;

    /**
     * 是否包含附件（可选，默认false）
     * 如果为true，会将任务的附件信息包含在生成内容中
     */
    @Builder.Default
    private Boolean includeAttachments = false;

    /**
     * 附件过滤（可选）
     * 用于指定需要包含的附件类型，如：["pdf", "docx", "xlsx"]
     * 如果为空，则包含所有附件类型
     */
    @Builder.Default
    private List<String> attachmentFilters = new ArrayList<>();

    /**
     * 请求用户ID（由后端从上下文获取，前端无需传递）
     */
    private Long userId;
}




