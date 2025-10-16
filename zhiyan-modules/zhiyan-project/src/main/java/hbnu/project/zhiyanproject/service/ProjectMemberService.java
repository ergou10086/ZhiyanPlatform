package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;

import java.util.List;
import java.util.Optional;

/**
 * 项目成员服务接口
 *
 * @author  Tokito
 */
public interface ProjectMemberService {

    /**
     * 添加项目成员
     */
    ProjectMember addMember(Long projectId, Long userId, ProjectMemberRole role);

    /**
     * 移除项目成员
     */
    void removeMember(Long projectId, Long userId);

    /**
     * 更新成员角色
     */
    ProjectMember updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole);

    /**
     * 获取项目所有成员
     */
    List<ProjectMember> getProjectMembers(Long projectId);

    /**
     * 获取用户参与的所有项目
     */
    List<ProjectMember> getUserProjects(Long userId);

    /**
     * 检查用户是否为项目成员
     */
    boolean isMember(Long projectId, Long userId);

    /**
     * 获取用户在项目中的角色
     */
    Optional<ProjectMemberRole> getUserRole(Long projectId, Long userId);

    /**
     * 获取项目中指定角色的成员
     */
    List<ProjectMember> getMembersByRole(Long projectId, ProjectMemberRole role);
}