package hbnu.project.zhiyanproject.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.dto.ProjectMemberDetailDTO;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
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
@RequestMapping("/api/projects/members")
@RequiredArgsConstructor
@Tag(name = "项目成员管理", description = "项目成员管理相关接口，包括成员邀请、角色管理等")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    // ==================== 成员管理相关接口 ====================

    /**
     * 邀请成员加入项目（直接添加，无需对方同意）
     * 业务场景：项目负责人通过用户ID直接将成员添加到项目中
     */
    @PostMapping("/projects/{projectId}/invite")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "邀请成员", description = "项目负责人直接将用户添加到项目中（无需对方同意）")
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
     * 业务场景：项目负责人移除不合适的成员
     */
    @DeleteMapping("/projects/{projectId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "移除成员", description = "项目负责人移除项目成员")
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
     * 业务场景：项目负责人修改成员在项目中的角色
     */
    @PutMapping("/projects/{projectId}/members/{userId}/role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新成员角色", description = "项目负责人修改成员的项目角色")
    public R<ProjectMember> updateMemberRole(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "新角色") ProjectMemberRole newRole) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]修改项目[{}]成员[{}]的角色为: {}", currentUserId, projectId, userId, newRole);

        try {
            ProjectMember member = projectMemberService.updateMemberRole(projectId, userId, newRole, currentUserId);
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
    @DeleteMapping("/projects/{projectId}/leave")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "退出项目", description = "成员主动退出项目")
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
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目成员", description = "获取项目的所有成员详细信息")
    public R<Page<ProjectMemberDetailDTO>> getProjectMembers(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {
        
        final Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]查看项目[{}]的成员列表", currentUserId, projectId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").ascending());
            Page<ProjectMemberDetailDTO> members = projectMemberService.getProjectMembers(projectId, pageable);

            // 标记当前用户
            final Long userId = currentUserId;
            members.forEach(member -> {
                if (member.getUserId().equals(userId)) {
                    member.setIsCurrentUser(true);
                }
            });

            return R.ok(members, "获取成功");
        } catch (Exception e) {
            log.error("获取项目成员失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取我参与的所有项目
     * 业务场景：在"我的项目"页面展示用户参与的所有项目
     */
    @GetMapping("/my-projects")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的项目", description = "获取当前用户参与的所有项目")
    public R<Page<ProjectMember>> getMyProjects(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]查看自己参与的项目", currentUserId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending());
            Page<ProjectMember> projects = projectMemberService.getMyProjects(currentUserId, pageable);
            return R.ok(projects, "获取成功");
        } catch (Exception e) {
            log.error("获取我的项目失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目中指定角色的成员
     * 业务场景：筛选项目中的负责人或特定角色成员
     */
    @GetMapping("/projects/{projectId}/role/{role}")
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
    @GetMapping("/projects/{projectId}/check-membership")
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
    @GetMapping("/projects/{projectId}/check-owner")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查负责人身份", description = "检查当前用户是否为项目负责人")
    public R<Boolean> checkOwner(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        boolean isOwner = projectMemberService.isOwner(projectId, currentUserId);
        return R.ok(isOwner);
    }

    /**
     * 获取用户在项目中的角色
     */
    @GetMapping("/projects/{projectId}/my-role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的角色", description = "获取当前用户在项目中的角色")
    public R<ProjectMemberRole> getMyRole(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        ProjectMemberRole role = projectMemberService.getUserRole(projectId, currentUserId);
        if (role == null) {
            return R.fail("您不是该项目的成员");
        }
        return R.ok(role);
    }

    /**
     * 获取项目成员数量
     */
    @GetMapping("/projects/{projectId}/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取成员数量", description = "获取项目的成员总数")
    public R<Long> getMemberCount(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        long count = projectMemberService.getMemberCount(projectId);
        return R.ok(count);
    }
}
