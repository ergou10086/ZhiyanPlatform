package hbnu.project.zhiyanproject.model.dto;

import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目成员数据传输对象
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDTO {

    /**
     * 成员记录ID
     */
    private String id;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 项目内角色
     */
    private ProjectMemberRole projectRole;

    /**
     * 加入项目时间
     */
    private LocalDateTime joinedAt;
}
