package hbnu.project.zhiyanproject.utils;

import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 项目级别的安全工具类
 * 用于检查用户在特定项目中的权限
 *
 * @author Tokito
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectSecurityUtils {

    private final ProjectMemberRepository projectMemberRepository;

    /**
     * 检查当前用户是否为项目成员
     *
     * @param projectId 项目ID
     * @return 是否为成员
     */
    public boolean isMember(Long projectId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return isMember(projectId, userId);
    }

    /**
     * 检查指定用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为成员
     */
    public boolean isMember(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isPresent();
    }

    /**
     * 检查当前用户是否为项目拥有者
     *
     * @param projectId 项目ID
     * @return 是否为拥有者
     */
    public boolean isOwner(Long projectId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return isOwner(projectId, userId);
    }

    /**
     * 检查指定用户是否为项目拥有者
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为拥有者
     */
    public boolean isOwner(Long projectId, Long userId) {
        Optional<ProjectMember> member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        return member.isPresent() && member.get().getProjectRole() == ProjectMemberRole.OWNER;
    }

    /**
     * 检查当前用户是否为项目管理员（包括OWNER和ADMIN）
     *
     * @param projectId 项目ID
     * @return 是否为项目管理员
     */
    public boolean isAdmin(Long projectId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return isAdmin(projectId, userId);
    }

    /**
     * 检查指定用户是否为项目管理员（包括OWNER和ADMIN）
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为项目管理员
     */
    public boolean isAdmin(Long projectId, Long userId) {
        Optional<ProjectMember> member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (!member.isPresent()) {
            return false;
        }
        ProjectMemberRole role = member.get().getProjectRole();
        return role == ProjectMemberRole.OWNER || role == ProjectMemberRole.ADMIN;
    }

    /**
     * 检查当前用户在项目中是否拥有指定权限
     *
     * @param projectId  项目ID
     * @param permission 权限
     * @return 是否拥有权限
     */
    public boolean hasPermission(Long projectId, ProjectPermission permission) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return hasPermission(projectId, userId, permission);
    }

    /**
     * 检查指定用户在项目中是否拥有指定权限
     *
     * @param projectId  项目ID
     * @param userId     用户ID
     * @param permission 权限
     * @return 是否拥有权限
     */
    public boolean hasPermission(Long projectId, Long userId, ProjectPermission permission) {
        Optional<ProjectMember> member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        return member.map(projectMember -> projectMember.getProjectRole().hasPermission(permission)).orElse(false);
    }

    /**
     * 获取当前用户在项目中的角色
     *
     * @param projectId 项目ID
     * @return 角色（如果不是成员则返回null）
     */
    public ProjectMemberRole getRole(Long projectId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return null;
        }
        return getRole(projectId, userId);
    }

    /**
     * 获取指定用户在项目中的角色
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 角色（如果不是成员则返回null）
     */
    public ProjectMemberRole getRole(Long projectId, Long userId) {
        Optional<ProjectMember> member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        return member.map(ProjectMember::getProjectRole).orElse(null);
    }

    /**
     * 验证当前用户是否为项目成员，如果不是则抛出异常
     *
     * @param projectId 项目ID
     * @throws SecurityException 如果不是项目成员
     */
    public void requireMember(Long projectId) {
        if (!isMember(projectId)) {
            throw new SecurityException("您不是该项目的成员，无权访问");
        }
    }

    /**
     * 验证当前用户是否为项目拥有者，如果不是则抛出异常
     *
     * @param projectId 项目ID
     * @throws SecurityException 如果不是项目拥有者
     */
    public void requireOwner(Long projectId) {
        if (!isOwner(projectId)) {
            throw new SecurityException("只有项目拥有者可以执行此操作");
        }
    }

    /**
     * 验证当前用户是否为项目管理员（包括OWNER和ADMIN），如果不是则抛出异常
     *
     * @param projectId 项目ID
     * @throws SecurityException 如果不是项目管理员
     */
    public void requireAdmin(Long projectId) {
        if (!isAdmin(projectId)) {
            throw new SecurityException("只有项目管理员可以执行此操作");
        }
    }

    /**
     * 验证当前用户在项目中拥有指定权限，如果没有则抛出异常
     *
     * @param projectId  项目ID
     * @param permission 权限
     * @throws SecurityException 如果没有权限
     */
    public void requirePermission(Long projectId, ProjectPermission permission) {
        if (!hasPermission(projectId, permission)) {
            throw new SecurityException("您在该项目中没有 " + permission.getDescription() + " 权限");
        }
    }
}

