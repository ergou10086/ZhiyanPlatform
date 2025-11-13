package hbnu.project.zhiyanactivelog.model.vo;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 聚合后的统一日志视图
 * @author yui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedOperationLogVO {

    private Long id;

    private Long projectId;

    private Long userId;

    private String username;

    // 业务模块：项目管理/任务管理/Wiki管理/成果管理/登录
    private String operationModule;

    // 操作类型（字符串化）
    private String operationType;

    // 描述/标题等
    private String title;

    private String description;

    private String ip;

    private String userAgent;

    // 统一时间字段
    private LocalDateTime time;

    // 来源：PROJECT/TASK/WIKI/ACHIEVEMENT/LOGIN
    private String source;

    // 可能的业务ID（任务/成果/Wiki）
    private Long relatedId;
}
