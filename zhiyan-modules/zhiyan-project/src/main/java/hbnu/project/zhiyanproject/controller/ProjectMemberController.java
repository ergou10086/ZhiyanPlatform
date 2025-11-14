package hbnu.project.zhiyanproject.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.model.dto.PageResult;
import hbnu.project.zhiyanproject.model.dto.ProjectMemberDTO;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import hbnu.project.zhiyanproject.handler.ProjectSentinelHandler;
import hbnu.project.zhiyanproject.model.form.InviteMemberRequest;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目成员控制器
 * 项目成员采用直接邀请方式，无需申请审批流程
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/zhiyan/projects")      // 原 /api
@RequiredArgsConstructor
@Tag(name = "项目成员管理", description = "项目成员管理相关接口，包括成员邀请、角色管理等")
@SecurityRequirement(name = "Bearer Authentication")
@AccessLog("项目成员管理")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final AuthServiceClient authServiceClient;

    // ==================== 成员管理相关接口 ====================

    /**
     * 邀请成员加入项目（直接添加，无需对方同意）
     * 业务场景：项目管理员（OWNER或ADMIN）通过用户ID直接将成员添加到项目中
     */
    @PostMapping("/{projectId}/invite")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "邀请成员", description = "项目管理员直接将用户添加到项目中（无需对方同意）")
    @OperationLog(module = "项目成员管理", type = OperationType.INSERT, description = "邀请成员加入项目", recordParams = true, recordResult = true)
    @SentinelResource(
        value = "inviteMember",
        blockHandlerClass = ProjectSentinelHandler.class,
        blockHandler = "handleInviteMemberBlock",
        fallbackClass = ProjectSentinelHandler.class,
        fallback = "handleInviteMemberFallback"
    )
    public R<ProjectMember> inviteMember(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @Valid @RequestBody InviteMemberRequest request) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]邀请用户[{}]加入项目[{}]", currentUserId, request.getUserId(), projectId);

        try {
            ProjectMember member = projectMemberService.inviteMember(projectId, request, currentUserId);
            return R.ok(member, "成员已添加");
        } catch (IllegalArgumentException e) {
            log.warn("邀请成员失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 移除项目成员
     * 业务场景：项目管理员移除不合适的成员
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "移除成员", description = "项目管理员移除项目成员")
    @OperationLog(module = "项目成员管理", type = OperationType.DELETE, description = "移除项目成员", recordParams = true, recordResult = false)
    public R<Void> removeMember(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "用户ID") Long userId) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]从项目[{}]移除成员[{}]", currentUserId, projectId, userId);

        try {
            projectMemberService.removeMember(projectId, userId, currentUserId);
            return R.ok(null, "成员已移除");
        } catch (IllegalArgumentException e) {
            log.warn("移除成员失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 更新成员角色
     * 业务场景：项目管理员修改成员在项目中的角色，可以将普通成员提升为管理员
     */
    @PutMapping("/{projectId}/members/{userId}/role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新成员角色", description = "项目管理员修改成员的项目角色，可以将普通成员提升为管理员")
    @OperationLog(module = "项目成员管理", type = OperationType.GRANT, description = "更新成员角色", recordParams = true, recordResult = true)
    public R<ProjectMember> updateMemberRole(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "用户ID") Long userId,
            @Valid @RequestBody hbnu.project.zhiyanproject.model.form.UpdateMemberRoleRequest request) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]修改项目[{}]成员[{}]的角色为: {}", currentUserId, projectId, userId, request.getNewRole());

        try {
            ProjectMember member = projectMemberService.updateMemberRole(projectId, userId, request.getNewRole(), currentUserId);
            return R.ok(member, "角色已更新");
        } catch (IllegalArgumentException e) {
            log.warn("更新成员角色失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 退出项目
     * 业务场景：成员主动退出项目
     */
    @DeleteMapping("/{projectId}/leave")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "退出项目", description = "成员主动退出项目")
    @OperationLog(module = "项目成员管理", type = OperationType.OTHER, description = "退出项目", recordParams = true, recordResult = false)
    public R<Void> leaveProject(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]退出项目[{}]", currentUserId, projectId);

        try {
            projectMemberService.leaveProject(projectId, currentUserId);
            return R.ok(null, "已退出项目");
        } catch (IllegalArgumentException e) {
            log.warn("退出项目失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    // ==================== 查询相关接口 ====================

    /**
     * 获取项目成员列表（含详细信息）
     * 业务场景：在项目详情页的"成员"标签页展示成员列表
     */
    @GetMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目成员", description = "获取项目的所有成员详细信息")
    public R<Page<ProjectMemberDTO>> getProjectMembers(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {
        
        final Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]查看项目[{}]的成员列表", currentUserId, projectId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").ascending());
            return projectMemberService.getProjectMembers(projectId, pageable);
        } catch (Exception e) {
            log.error("获取项目成员失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 搜索用户（用于邀请成员时搜索）
     * 业务场景：项目成员邀请时搜索用户
     * 通过Feign调用认证服务的搜索接口
     */
    @GetMapping("/users/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户（用于邀请成员）")
    public R<PageResult<UserDTO>> searchUsers(
            @RequestParam @Parameter(description = "搜索关键词") String keyword,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") int size) {
        
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]搜索用户: keyword={}, page={}, size={}", currentUserId, keyword, page, size);

        try {
            // 通过Feign调用认证服务的搜索接口
            R<PageResult<UserDTO>> result = authServiceClient.searchUsers(keyword, page, size);
            
            if (R.isSuccess(result)) {
                log.info("搜索用户成功: keyword={}, 结果数={}", keyword, 
                    result.getData() != null ? result.getData().getTotalElements() : 0);
                return result;
            } else {
                log.warn("搜索用户失败: {}", result.getMsg());
                return R.fail(result.getMsg());
            }
        } catch (Exception e) {
            log.error("搜索用户异常: keyword={}", keyword, e);
            return R.fail("搜索用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取我参与的所有项目成员关系
     * 业务场景：在"我的项目"页面展示用户参与的所有项目的成员关系信息
     */
    @GetMapping("/my-memberships")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的项目成员关系", description = "获取当前用户参与的所有项目的成员关系信息")
    public R<Page<ProjectMember>> getMyMemberships(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]查看自己参与的项目成员关系", currentUserId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending());
            Page<ProjectMember> projects = projectMemberService.getMyProjects(currentUserId, pageable);
            return R.ok(projects, "获取成功");
        } catch (Exception e) {
            log.error("获取我的项目成员关系失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目中指定角色的成员
     * 业务场景：筛选项目中的负责人或特定角色成员
     */
    @GetMapping("/{projectId}/role/{role}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按角色获取成员", description = "获取项目中指定角色的所有成员")
    public R<List<ProjectMember>> getMembersByRole(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "角色") ProjectMemberRole role) {

        log.info("查询项目[{}]中角色为[{}]的成员", projectId, role);

        try {
            List<ProjectMember> members = projectMemberService.getMembersByRole(projectId, role);
            return R.ok(members, "获取成功");
        } catch (Exception e) {
            log.error("获取成员失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否为项目成员
     */
    @GetMapping("/{projectId}/check-membership")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查成员身份", description = "检查当前用户是否为项目成员")
    public R<Boolean> checkMembership(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        boolean isMember = projectMemberService.isMember(projectId, currentUserId);
        return R.ok(isMember);
    }

    /**
     * 检查用户是否为项目负责人
     */
    @GetMapping("/{projectId}/check-owner")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查负责人身份", description = "检查当前用户是否为项目负责人")
    public R<Boolean> checkOwner(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]检查项目[{}]的拥有者身份", currentUserId, projectId);
        boolean isOwner = projectMemberService.isOwner(projectId, currentUserId);
        log.info("用户[{}]在项目[{}]中的拥有者身份检查结果: {}", currentUserId, projectId, isOwner);
        return R.ok(isOwner);
    }

    /**
     * 检查用户是否为项目管理员（包括OWNER和ADMIN）
     */
    @GetMapping("/{projectId}/check-admin")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查管理员身份", description = "检查当前用户是否为项目管理员（包括项目负责人和管理员）")
    public R<Boolean> checkAdmin(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]检查项目[{}]的管理员身份", currentUserId, projectId);
        boolean isAdmin = projectMemberService.isAdmin(projectId, currentUserId);
        log.info("用户[{}]在项目[{}]中的管理员身份检查结果: {}", currentUserId, projectId, isAdmin);
        return R.ok(isAdmin);
    }

    /**
     * 获取项目成员数量
     */
    @GetMapping("/{projectId}/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取成员数量", description = "获取项目的成员总数")
    public R<Long> getMemberCount(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        long count = projectMemberService.getMemberCount(projectId);
        return R.ok(count);
    }


    /**
     * 检查用户是否为项目成员（用于其他微服务调用）
     */
    @GetMapping("/{projectId}/members/check")
    @Operation(summary = "检查成员关系", description = "检查用户是否为项目成员")
    public Boolean isProjectMember(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        return projectMemberService.isMember(projectId, userId);
    }


    /**
     * 检查用户是否为项目拥有者（用于其他微服务调用）
     */
    @GetMapping("/{projectId}/owner/check")
    @Operation(summary = "检查拥有者", description = "检查用户是否为项目拥有者")
    public Boolean isProjectOwner(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        return projectMemberService.isOwner(projectId, userId);
    }

    /**
     * 检查用户是否为项目管理员（用于其他微服务调用）
     */
    @GetMapping("/{projectId}/admin/check")
    @Operation(summary = "检查管理员", description = "检查用户是否为项目管理员（包括OWNER和ADMIN）")
    public Boolean isProjectAdmin(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        return projectMemberService.isAdmin(projectId, userId);
    }


    /**
     * 检查用户权限（用于其他微服务调用）
     */
    @GetMapping("/{projectId}/permissions/check")
    @Operation(summary = "检查权限", description = "检查用户是否有指定权限")
    public Boolean hasPermission(
            @PathVariable Long projectId,
            @RequestParam Long userId,
            @RequestParam String permission) {
        try {
            ProjectPermission perm = ProjectPermission.valueOf(permission);
            ProjectMemberRole role = projectMemberService.getUserRole(projectId, userId);
            return role != null && role.hasPermission(perm);
        } catch (Exception e) {
            log.error("检查权限失败: projectId={}, userId={}, permission={}",
                    projectId, userId, permission, e);
            return false;
        }
    }
}
