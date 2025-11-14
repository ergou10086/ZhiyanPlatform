package hbnu.project.zhiyanproject.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonidempotent.annotation.Idempotent;
import hbnu.project.zhiyancommonidempotent.enums.IdempotentType;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.handler.ProjectSentinelHandler;
import hbnu.project.zhiyanproject.model.dto.ImageUploadResponse;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.service.ProjectImageService;
import hbnu.project.zhiyanproject.model.form.CreateProjectRequest;
import hbnu.project.zhiyanproject.service.ProjectService;
import hbnu.project.zhiyanproject.utils.ProjectSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.multipart.MultipartFile;


/**
 * 项目控制器
 * 基于角色的权限控制
 *
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/zhiyan/projects")      // 原 /api/projects
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目管理相关接口")
@AccessLog("项目管理")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectSecurityUtils projectSecurityUtils;
    private final ProjectImageService projectImageService;

    // ==================== 图片上传相关 ====================

    /**
     * 上传项目图片
     * 权限要求：已登录用户
     */
    @PostMapping("/upload-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传项目图片", description = "上传项目封面图片到MinIO，返回图片URL")
    @OperationLog(module = "项目管理", type = OperationType.UPLOAD, description = "上传项目封面图片到MinIO")
    @SentinelResource(
        value = "uploadProjectImage",
        blockHandlerClass = ProjectSentinelHandler.class,
        blockHandler = "handleUploadImageBlock"
    )
    @Idempotent(type = IdempotentType.PARAM, timeout = 3, message = "图片上传中，请勿重复提交")
    public R<ImageUploadResponse> uploadProjectImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "项目ID（可选）") @RequestParam(required = false) Long projectId) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail("未登录或令牌无效");
        }

        log.info("用户[{}]上传项目图片, projectId={}", userId, projectId);

        return projectImageService.uploadProjectImage(file, projectId, userId);
    }

    /**
     * 删除项目图片
     * 权限要求：已登录用户
     */
    @DeleteMapping("/delete-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除项目图片", description = "从MinIO删除项目图片")
    @OperationLog(module = "项目管理", type = OperationType.DELETE, description = "从MinIO删除项目图片")
    public R<Void> deleteProjectImage(
            @Parameter(description = "图片URL") @RequestParam String imageUrl) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]删除项目图片: {}", userId, imageUrl);

        return projectImageService.deleteProjectImage(imageUrl);
    }

    // ==================== 项目CRUD相关 ====================

    /**
     * 创建项目
     * 权限要求：已登录用户（任何登录用户都可以创建项目）
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建项目", description = "创建新项目，创建者自动成为项目拥有者")
    @OperationLog(module = "项目管理", type = OperationType.INSERT, description = "创建新项目，创建者自动成为项目拥有者")
    @SentinelResource(
        value = "createProject",
        blockHandlerClass = ProjectSentinelHandler.class,
        blockHandler = "handleCreateProjectBlock",
        fallbackClass = ProjectSentinelHandler.class,
        fallback = "handleCreateProjectFallback"
    )
    @Idempotent(type = IdempotentType.PARAM, timeout = 2, message = "项目创建中，请勿重复提交")
    public R<Project> createProject(@RequestBody @Valid CreateProjectRequest request) {

        // 从 Spring Security Context 获取当前登录用户ID
        Long creatorId = SecurityUtils.getUserId();
        if (creatorId == null) {
            return hbnu.project.zhiyancommonbasic.domain.R.fail(hbnu.project.zhiyancommonbasic.domain.R.UNAUTHORIZED, "未登录或令牌无效");
        }
        log.info("用户[{}]创建项目: {}", creatorId, request.getName());

        return projectService.createProject(
                request.getName(),
                request.getDescription(),
                request.getVisibility(),
                request.getStartDate(),
                request.getEndDate(),
                request.getImageUrl(),
                creatorId
        );
    }

    /**
     * 更新项目
     * 权限要求：已登录 + 项目创建人（拥有者）
     */
    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新项目", description = "更新项目信息，只有项目创建人可以编辑")
    @OperationLog(module = "项目管理", type = OperationType.UPDATE, description = "更新项目信息，只有项目创建人可以编辑")
    @Idempotent(type = IdempotentType.PARAM, timeout = 1, message = "项目更新中，请勿重复提交")
    public R<Project> updateProject(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody @jakarta.validation.Valid hbnu.project.zhiyanproject.model.form.UpdateProjectRequest request) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新项目[{}]", userId, projectId);

        // 项目级权限检查：只有项目创建人（拥有者）才能编辑
        projectSecurityUtils.requireOwner(projectId);

        return projectService.updateProject(projectId, request.getName(), request.getDescription(), 
                request.getVisibility(), request.getStatus(), request.getStartDate(), request.getEndDate(),
                request.getImageUrl());
    }

    /**
     * 删除项目（软删除）
     * 权限要求：已登录 + 项目拥有者权限（通过方法内部检查）
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除项目", description = "软删除项目，只有项目拥有者可以删除")
    @OperationLog(module = "项目管理", type = OperationType.DELETE, description = "软删除项目，只有项目拥有者可以删除")
    @SentinelResource(
        value = "deleteProject",
        blockHandlerClass = ProjectSentinelHandler.class,
        blockHandler = "handleDeleteProjectBlock"
    )
    @Idempotent(
            type = IdempotentType.SPEL,
            key = "#projectId",
            timeout = 2,
            message = "项目删除中，请勿重复操作"
    )
    public R<Void> deleteProject(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]删除项目[{}]", userId, projectId);

        // 项目级权限检查：必须是项目拥有者才能删除
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_DELETE);

        return projectService.deleteProject(projectId, userId);
    }

    /**
     * 根据ID获取项目
     * 权限要求：已登录用户
     */
    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目详情", description = "根据ID获取项目详细信息")
    @OperationLog(module = "项目管理", type = OperationType.QUERY, description = "根据ID获取项目详细信息")
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
    @OperationLog(module = "项目管理", type = OperationType.QUERY, description = "分页获取所有项目（管理员）", recordResult = false)
    @SentinelResource(
        value = "getAllProjects",
        blockHandlerClass = ProjectSentinelHandler.class,
        blockHandler = "handleGetProjectsBlock"
    )
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
    @OperationLog(module = "项目管理", type = OperationType.QUERY, description = "获取当前用户创建的所有项目", recordResult = false)
    @SentinelResource(
        value = "getMyCreatedProjects",
        blockHandlerClass = ProjectSentinelHandler.class,
        blockHandler = "handleGetProjectsBlock"
    )
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
    @OperationLog(module = "项目管理", type = OperationType.QUERY, description = "根据创建者ID获取项目列表", recordResult = false)
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
     * 分页获取当前用户参与的所有项目
     * 权限要求：已登录用户
     */
    @GetMapping("/page-my-projects")
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
     * 权限要求：已登录 + 项目管理权限（通过方法内部检查）
     */
    @PatchMapping("/{projectId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新项目状态", description = "更新项目的状态")
    @OperationLog(module = "项目管理", type = OperationType.UPDATE, description = "更新项目状态", recordParams = true, recordResult = true)
    public R<Project> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestBody @jakarta.validation.Valid hbnu.project.zhiyanproject.model.form.UpdateProjectStatusRequest request) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新项目[{}]状态为: {}", userId, projectId, request.getStatus());

        // 项目级权限检查：必须有项目管理权限
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_MANAGE);

        return projectService.updateProjectStatus(projectId, request.getStatus());
    }

    /**
     * 归档项目
     * 权限要求：已登录 + 项目拥有者权限（通过方法内部检查）
     */
    @PostMapping("/{projectId}/archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "归档项目", description = "将项目归档")
    @OperationLog(module = "项目管理", type = OperationType.UPDATE, description = "归档项目", recordParams = true, recordResult = false)
    public R<Void> archiveProject(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]归档项目[{}]", userId, projectId);

        // 项目级权限检查：只有项目拥有者可以归档
        projectSecurityUtils.requireOwner(projectId);

        return projectService.archiveProject(projectId, userId);
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

    /**
     * 检查当前用户是否有权限访问项目
     * 权限要求：已登录用户

     @GetMapping("/{projectId}/access")
     @PreAuthorize("isAuthenticated()")
     @Operation(summary = "检查访问权限", description = "检查当前用户是否有权限访问项目")
     public R<Boolean> hasAccessPermission(@PathVariable Long projectId) {
     Long userId = SecurityUtils.getUserId();
     return projectService.hasAccessPermission(projectId, userId);
     }*/
}
