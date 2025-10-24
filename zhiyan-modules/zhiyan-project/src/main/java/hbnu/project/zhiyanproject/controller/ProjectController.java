package hbnu.project.zhiyanproject.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.model.dto.ImageUploadResponse;
import hbnu.project.zhiyanproject.model.dto.ProjectDTO;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final ProjectSecurityUtils projectSecurityUtils;
    private final ProjectImageService projectImageService;
    private final AuthServiceClient authServiceClient;

    // ==================== 图片上传相关 ====================

    /**
     * 上传项目图片
     * 权限要求：已登录用户
     */
    @PostMapping("/upload-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传项目图片", description = "上传项目封面图片到MinIO，返回图片URL")
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
     * 权限要求：已登录 + 项目内有管理权限（通过方法内部检查）
     */
    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新项目", description = "更新项目信息，需要是项目成员")
    public R<Project> updateProject(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody @jakarta.validation.Valid hbnu.project.zhiyanproject.model.form.UpdateProjectRequest request) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新项目[{}]", userId, projectId);

        // 项目级权限检查：必须是项目成员且有管理权限
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_MANAGE);

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
    public R<Page<ProjectDTO>> getPublicActiveProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("获取公开活跃项目列表，page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 获取项目列表
        R<Page<Project>> projectsResult = projectService.getPublicActiveProjects(pageable);
        if (projectsResult.getCode() != 200 || projectsResult.getData() == null) {
            log.warn("获取项目列表失败: {}", projectsResult.getMsg());
            return R.fail(projectsResult.getMsg());
        }
        
        Page<Project> projects = projectsResult.getData();
        log.info("查询到 {} 个公开项目", projects.getTotalElements());
        
        // 收集所有创建者ID
        List<Long> creatorIds = projects.getContent().stream()
                .map(Project::getCreatorId)
                .distinct()
                .collect(Collectors.toList());
        
        log.info("需要查询 {} 个创建者的信息: {}", creatorIds.size(), creatorIds);
        
        // 批量查询创建者信息
        Map<Long, UserDTO> creatorMap = null;
        try {
            if (!creatorIds.isEmpty()) {
                R<List<UserDTO>> usersResult = authServiceClient.getUsersByIds(creatorIds);
                if (usersResult != null && usersResult.getCode() == 200 && usersResult.getData() != null) {
                    // 将List转换为Map，以userId为key
                    creatorMap = usersResult.getData().stream()
                            .collect(Collectors.toMap(
                                    UserDTO::getId,
                                    user -> user
                            ));
                    log.info("成功获取 {} 个用户信息", creatorMap.size());
                } else {
                    log.warn("批量查询用户信息失败: {}", usersResult != null ? usersResult.getMsg() : "响应为空");
                }
            }
        } catch (Exception e) {
            log.error("调用auth服务查询用户信息失败", e);
        }
        
        // 转换为ProjectDTO并填充创建者名称
        final Map<Long, UserDTO> finalCreatorMap = creatorMap;
        List<ProjectDTO> projectDTOs = projects.getContent().stream()
                .map(project -> {
                    ProjectDTO dto = ProjectDTO.builder()
                            .id(String.valueOf(project.getId()))
                            .name(project.getName())
                            .description(project.getDescription())
                            .status(project.getStatus())
                            .visibility(project.getVisibility())
                            .startDate(project.getStartDate())
                            .endDate(project.getEndDate())
                            .imageUrl(project.getImageUrl())
                            .creatorId(String.valueOf(project.getCreatorId()))
                            .createdAt(project.getCreatedAt())
                            .updatedAt(project.getUpdatedAt())
                            .build();
                    
                    // 填充创建者名称
                    if (finalCreatorMap != null && finalCreatorMap.containsKey(project.getCreatorId())) {
                        UserDTO creator = finalCreatorMap.get(project.getCreatorId());
                        dto.setCreatorName(creator.getName());
                        log.info("✅ 项目 {} (ID:{}) 的创建者: {} (ID:{})", project.getName(), project.getId(), creator.getName(), creator.getId());
                    } else {
                        dto.setCreatorName("未知用户");
                        log.warn("⚠️ 项目 {} (ID:{}) 的创建者信息未找到，设置为未知用户", project.getName(), project.getId());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        // 创建新的分页对象
        Page<ProjectDTO> projectDTOPage = new PageImpl<>(projectDTOs, pageable, projects.getTotalElements());
        
        log.info("成功返回 {} 个项目信息（含创建者名称）", projectDTOs.size());
        return R.ok(projectDTOPage);
    }

    /**
     * 更新项目状态
     * 权限要求：已登录 + 项目管理权限（通过方法内部检查）
     */
    @PatchMapping("/{projectId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新项目状态", description = "更新项目的状态")
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
