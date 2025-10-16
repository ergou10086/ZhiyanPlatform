package hbnu.project.zhiyanproject.model.dto;

import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目成员详细信息DTO
 * 用于前端展示项目成员列表
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDetailDTO {

    /**
     * 成员记录ID
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（需要从auth服务获取）
     */
    private String username;

    /**
     * 用户邮箱（需要从auth服务获取）
     */
    private String email;

    /**
     * 在项目中的角色
     */
    private ProjectMemberRole projectRole;

    /**
     * 角色显示名称
     */
    private String roleName;

    /**
     * 加入项目时间
     */
    private LocalDateTime joinedAt;

    /**
     * 是否是当前用户
     */
    private Boolean isCurrentUser;
}

