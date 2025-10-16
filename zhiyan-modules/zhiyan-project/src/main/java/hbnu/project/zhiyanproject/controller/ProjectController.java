package hbnu.project.zhiyanproject.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import hbnu.project.zhiyanproject.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 项目控制器
 * 基于角色的权限控制
 *
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目管理相关接口")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     * 权限要求：已登录用户（USER 角色及以上）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "创建项目", description = "创建新项目，创建者自动成为项目拥有者")
    public R<Project> createProject(
            @Parameter(description = "项目名称") @RequestParam String name,
            @Parameter(description = "项目描述") @RequestParam(required = false) String description,
            @Parameter(description = "可见性") @RequestParam(required = false) ProjectVisibility visibility,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // 从 Spring Security Context 获取当前登录用户ID
        Long creatorId = SecurityUtils.getUserId();
        log.info("用户[{}]创建项目: {}", creatorId, name);

        return projectService.createProject(name, description, visibility, startDate, endDate, creatorId);
    }

    /**
     * 更新项目
     * 权限要求：项目成员（需要在业务层验证是否为项目成员）
     */
    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "更新项目", description = "更新项目信息，需要是项目成员")
    public R<Project> updateProject(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) ProjectVisibility visibility,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新项目[{}]", userId, projectId);

        return projectService.updateProject(projectId, name, description, visibility, status, startDate, endDate);
    }

    /**
     * 删除项目（软删除）
     * 权限要求：项目拥有者或管理员
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "删除项目", description = "软删除项目，只有项目拥有者可以删除")
    public R<Void> deleteProject(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]删除项目[{}]", userId, projectId);

        return projectService.deleteProject(projectId, userId);
    }

    /**
     * 根据ID获取项目
     * 权限要求：已登录用户
     */
    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目详情", description = "根据ID获取项目详细信息")
    public R<Project> getProjectById(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询项目[{}]", userId, projectId);

        return projectService.getProjectById(projectId);
    }

    /**
     * 获取所有项目（分页）
     * 权限要求：管理员或开发者
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "获取所有项目", description = "分页获取所有项目（管理员）")
    public R<Page<Project>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return projectService.getAllProjects(pageable);
    }

    /**
     * 获取当前用户创建的项目
     * 权限要求：已登录用户
     */
    @GetMapping("/my-created")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我创建的项目", description = "获取当前用户创建的所有项目")
    public R<Page<Project>> getMyCreatedProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long creatorId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return projectService.getProjectsByCreator(creatorId, pageable);
    }

    /**
     * 根据创建者获取项目列表
     * 权限要求：已登录用户
     */
    @GetMapping("/creator/{creatorId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取指定用户创建的项目", description = "根据创建者ID获取项目列表")
    public R<Page<Project>> getProjectsByCreator(
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectService.getProjectsByCreator(creatorId, pageable);
    }

    /**
     * 根据状态获取项目列表
     * 权限要求：已登录用户
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按状态获取项目", description = "根据项目状态筛选项目")
    public R<Page<Project>> getProjectsByStatus(
            @PathVariable ProjectStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectService.getProjectsByStatus(status, pageable);
    }

    /**
     * 获取当前用户参与的所有项目
     * 权限要求：已登录用户
     */
    @GetMapping("/my-projects")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我参与的项目", description = "获取当前用户参与的所有项目")
    public R<Page<Project>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return projectService.getUserProjects(userId, pageable);
    }

    /**
     * 搜索项目
     * 权限要求：已登录用户
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "搜索项目", description = "根据关键字搜索项目")
    public R<Page<Project>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectService.searchProjects(keyword, pageable);
    }

    /**
     * 获取公开的活跃项目
     * 权限要求：无需登录（公开接口）
     */
    @GetMapping("/public/active")
    @Operation(summary = "获取公开项目", description = "获取所有公开的活跃项目")
    public R<Page<Project>> getPublicActiveProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectService.getPublicActiveProjects(pageable);
    }

    /**
     * 更新项目状态
     * 权限要求：项目成员
     */
    @PatchMapping("/{projectId}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "更新项目状态", description = "更新项目的状态")
    public R<Project> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestParam ProjectStatus status) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新项目[{}]状态为: {}", userId, projectId, status);

        return projectService.updateProjectStatus(projectId, status);
    }

    /**
     * 归档项目
     * 权限要求：项目拥有者
     */
    @PostMapping("/{projectId}/archive")
    @PreAuthorize("hasAnyRole('OWNER', 'MEMBER')")
    @Operation(summary = "归档项目", description = "将项目归档")
    public R<Void> archiveProject(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]归档项目[{}]", userId, projectId);

        return projectService.archiveProject(projectId, userId);
    }

    /**
     * 检查当前用户是否有权限访问项目
     * 权限要求：已登录用户
     */
    @GetMapping("/{projectId}/access")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查访问权限", description = "检查当前用户是否有权限访问项目")
    public R<Boolean> hasAccessPermission(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        return projectService.hasAccessPermission(projectId, userId);
    }

    /**
     * 统计当前用户创建的项目数量
     * 权限要求：已登录用户
     */
    @GetMapping("/count/my-created")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计我创建的项目", description = "统计当前用户创建的项目数量")
    public R<Long> countMyCreatedProjects() {
        Long userId = SecurityUtils.getUserId();
        return projectService.countUserCreatedProjects(userId);
    }

    /**
     * 统计当前用户参与的项目数量
     * 权限要求：已登录用户
     */
    @GetMapping("/count/my-participated")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计我参与的项目", description = "统计当前用户参与的项目数量")
    public R<Long> countMyParticipatedProjects() {
        Long userId = SecurityUtils.getUserId();
        return projectService.countUserParticipatedProjects(userId);
    }
}
