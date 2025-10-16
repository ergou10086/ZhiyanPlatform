package hbnu.project.zhiyanproject.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.dto.ProjectMemberDetailDTO;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 项目成员控制器
 * 基于角色的权限控制
 *
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/project-members")
@RequiredArgsConstructor
@Tag(name = "项目成员管理", description = "项目成员管理相关接口")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    /**
     * 添加项目成员
     * 权限要求：项目拥有者
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "添加项目成员", description = "向项目中添加新成员（需要项目拥有者权限）")
    public ProjectMember addMember(
            @Parameter(description = "项目ID") @RequestParam Long projectId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色") @RequestParam ProjectMemberRole role) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]向项目[{}]添加成员: userId={}, role={}", currentUserId, projectId, userId, role);

        return projectMemberService.addMember(projectId, userId, role);
    }

    /**
     * 移除项目成员
     * 权限要求：项目拥有者
     */
    @DeleteMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "移除项目成员", description = "从项目中移除成员（需要项目拥有者权限）")
    public void removeMember(
            @RequestParam Long projectId,
            @RequestParam Long userId) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]从项目[{}]移除成员: userId={}", currentUserId, projectId, userId);

        projectMemberService.removeMember(projectId, userId);
    }

    /**
     * 更新成员角色
     * 权限要求：项目拥有者
     */
    @PutMapping("/role")
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "更新成员角色", description = "更新项目成员的角色（需要项目拥有者权限）")
    public ProjectMember updateMemberRole(
            @RequestParam Long projectId,
            @RequestParam Long userId,
            @RequestParam ProjectMemberRole newRole) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]更新项目[{}]成员[{}]角色为: {}", currentUserId, projectId, userId, newRole);

        return projectMemberService.updateMemberRole(projectId, userId, newRole);
    }

    /**
     * 获取项目所有成员
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "获取项目成员", description = "获取项目的所有成员")
    public List<ProjectMember> getProjectMembers(@PathVariable Long projectId) {
        return projectMemberService.getProjectMembers(projectId);
    }

    /**
     * 获取项目成员详细信息（用于前端"查看所有成员"功能）
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}/details")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目成员详情", description = "获取项目所有成员的详细信息，用于前端展示")
    public R<List<ProjectMemberDetailDTO>> getProjectMembersDetail(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]查看项目[{}]的所有成员", currentUserId, projectId);
        
        List<ProjectMemberDetailDTO> members = projectMemberService.getProjectMembersDetail(projectId, currentUserId);
        return R.ok(members, "获取成员列表成功");
    }

    /**
     * 获取当前用户参与的所有项目
     * 权限要求：已登录用户
     */
    @GetMapping("/my-projects")
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "获取我参与的项目", description = "获取当前用户参与的所有项目")
    public List<ProjectMember> getMyProjects() {
        Long userId = SecurityUtils.getUserId();
        return projectMemberService.getUserProjects(userId);
    }

    /**
     * 获取用户参与的所有项目
     * 权限要求：已登录用户
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户项目", description = "获取指定用户参与的所有项目")
    public List<ProjectMember> getUserProjects(@PathVariable Long userId) {
        return projectMemberService.getUserProjects(userId);
    }

    /**
     * 检查当前用户是否为项目成员
     * 权限要求：已登录用户
     */
    @GetMapping("/check/my-membership")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查我的成员身份", description = "检查当前用户是否为项目成员")
    public boolean checkMyMembership(@RequestParam Long projectId) {
        Long userId = SecurityUtils.getUserId();
        return projectMemberService.isMember(projectId, userId);
    }

    /**
     * 获取当前用户在项目中的角色
     * 权限要求：已登录用户
     */
    @GetMapping("/my-role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的角色", description = "获取当前用户在项目中的角色")
    public Optional<ProjectMemberRole> getMyRole(@RequestParam Long projectId) {
        Long userId = SecurityUtils.getUserId();
        return projectMemberService.getUserRole(projectId, userId);
    }

    /**
     * 获取用户在项目中的角色
     * 权限要求：已登录用户
     */
    @GetMapping("/role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户角色", description = "获取指定用户在项目中的角色")
    public Optional<ProjectMemberRole> getUserRole(
            @RequestParam Long projectId,
            @RequestParam Long userId) {
        return projectMemberService.getUserRole(projectId, userId);
    }

    /**
     * 获取项目中指定角色的成员
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}/role/{role}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按角色获取成员", description = "获取项目中指定角色的所有成员")
    public List<ProjectMember> getMembersByRole(
            @PathVariable Long projectId,
            @PathVariable ProjectMemberRole role) {
        return projectMemberService.getMembersByRole(projectId, role);
    }

    /**
     * 检查用户是否为项目成员
     * 权限要求：已登录用户
     *
     @GetMapping("/check")
     @PreAuthorize("isAuthenticated()")
     @Operation(summary = "检查成员身份", description = "检查指定用户是否为项目成员")
     public boolean isMember(
     @RequestParam Long projectId,
     @RequestParam Long userId) {
     return projectMemberService.isMember(projectId, userId);
     }
     */
}
