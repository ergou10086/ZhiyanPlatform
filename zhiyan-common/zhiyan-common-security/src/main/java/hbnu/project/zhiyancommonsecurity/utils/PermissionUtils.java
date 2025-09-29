package hbnu.project.zhiyancommonsecurity.utils;

import hbnu.project.zhiyansecurity.context.LoginUserBody;
import hbnu.project.zhiyansecurity.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限工具类
 * 权限检查方法
 *
 * @author ErgouTree
 */
@Slf4j
public class PermissionUtils {

    /**
     * 检查当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permission) {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasPermission(permission);
    }

    /**
     * 检查当前用户是否拥有指定角色
     */
    public static boolean hasRole(String role) {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasRole(role);
    }

    /**
     * 检查当前用户是否拥有任意一个指定权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasAnyPermission(permissions);
    }

    /**
     * 检查当前用户是否可以创建项目
     */
    public static boolean canCreateProject() {
        return hasPermission("project:create");
    }

    /**
     * 检查当前用户是否可以管理项目（基于项目ID的动态权限检查）
     * 注意：这里只检查基础权限，实际项目管理权限需要结合项目成员关系判断
     */
    public static boolean canManageProject() {
        return hasPermission("project:manage");
    }

    /**
     * 检查当前用户是否可以删除项目
     * 注意：实际删除权限需要结合项目所有者关系判断
     */
    public static boolean canDeleteProject() {
        return hasPermission("project:delete");
    }

    /**
     * 检查当前用户是否可以管理知识库
     */
    public static boolean canManageKnowledge() {
        return hasPermission("knowledge:manage");
    }

    /**
     * 检查当前用户是否为系统管理员
     */
    public static boolean isSystemAdmin() {
        return hasRole("DEVELOPER") || hasPermission("system:admin");
    }

    /**
     * 检查当前用户是否为开发者
     */
    public static boolean isDeveloper() {
        return hasRole("DEVELOPER");
    }

    /**
     * 检查当前用户是否为普通用户
     */
    public static boolean isUser() {
        return hasRole("USER");
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户邮箱
     */
    public static String getCurrentUserEmail() {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        return loginUser != null ? loginUser.getEmail() : null;
    }

    /**
     * 检查当前用户是否已登录
     */
    public static boolean isLoggedIn() {
        return SecurityContextHolder.isLogin();
    }

    /**
     * 检查当前用户在指定项目中是否具有指定角色
     * TODO: 需要在项目服务中实现具体逻辑
     */
    public static boolean hasProjectRole(Long projectId, String projectRole) {
        // 这里需要调用项目服务来检查用户在特定项目中的角色
        // 暂时返回false，等项目服务实现后再完善
        log.debug("检查用户在项目[{}]中是否具有角色[{}]，当前未实现", projectId, projectRole);
        return false;
    }

    /**
     * 检查当前用户是否为指定项目的拥有者
     */
    public static boolean isProjectOwner(Long projectId) {
        return hasProjectRole(projectId, "OWNER");
    }

    /**
     * 检查当前用户是否为指定项目的成员
     */
    public static boolean isProjectMember(Long projectId) {
        return hasProjectRole(projectId, "MEMBER") || isProjectOwner(projectId);
    }
}