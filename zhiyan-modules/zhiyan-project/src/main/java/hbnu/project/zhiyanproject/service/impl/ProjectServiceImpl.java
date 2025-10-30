package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import hbnu.project.zhiyanproject.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberService projectMemberService;
    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public R<Project> createProject(String name, String description, ProjectVisibility visibility,
                                    LocalDate startDate, LocalDate endDate, String imageUrl, Long creatorId) {
        try {
            // 验证项目名称是否已存在（只检查未删除的项目）
            if (projectRepository.existsByNameAndIsDeletedFalse(name)) {
                return R.fail("项目名称已存在: " + name);
            }

            // 创建项目
            Project project = Project.builder()
                    .name(name)
                    .description(description)
                    .status(ProjectStatus.PLANNING)
                    .visibility(visibility != null ? visibility : ProjectVisibility.PRIVATE)
                    .startDate(startDate)
                    .endDate(endDate)
                    .imageUrl(imageUrl != null ? imageUrl : "")
                    .creatorId(creatorId)
                    .isDeleted(false)
                    .build();

            if (creatorId == null) {
                return R.fail("未登录或令牌无效，无法创建项目");
            }
            project = projectRepository.save(project);

            // 自动将创建者添加为项目拥有者（使用内部方法，不验证用户）
            projectMemberService.addMemberInternal(project.getId(), creatorId, ProjectMemberRole.OWNER);

            log.info("成功创建项目: id={}, name={}, creator={}", project.getId(), name, creatorId);
            return R.ok(project, "项目创建成功");
        } catch (Exception e) {
            log.error("创建项目失败: name={}, creatorId={}", name, creatorId, e);
            return R.fail("项目创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Project> updateProject(Long projectId, String name, String description,
                                    ProjectVisibility visibility, ProjectStatus status,
                                    LocalDate startDate, LocalDate endDate, String imageUrl) {
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在");
            }

            // 更新项目信息
            if (StringUtils.hasText(name) && !name.equals(project.getName())) {
                if (projectRepository.existsByNameAndIdNotAndIsDeleted(name, projectId, false)) {
                    return R.fail("项目名称已存在: " + name);
                }
                project.setName(name);
            }

            if (StringUtils.hasText(description)) {
                project.setDescription(description);
            }

            if (visibility != null) {
                project.setVisibility(visibility);
            }

            if (status != null) {
                project.setStatus(status);
            }

            if (startDate != null) {
                project.setStartDate(startDate);
            }

            if (endDate != null) {
                project.setEndDate(endDate);
            }

            if (StringUtils.hasText(imageUrl)) {
                project.setImageUrl(imageUrl);
            }

            project = projectRepository.save(project);
            log.info("成功更新项目: id={}, name={}", projectId, project.getName());
            return R.ok(project, "项目更新成功");
        } catch (Exception e) {
            log.error("更新项目失败: projectId={}", projectId, e);
            return R.fail("项目更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> deleteProject(Long projectId, Long userId) {
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在");
            }

            // 检查是否为项目拥有者
            if (!project.isOwner(userId)) {
                return R.fail("只有项目拥有者才能删除项目");
            }

            // 软删除：只标记为已删除
            project.setIsDeleted(true);
            projectRepository.save(project);

            log.info("成功删除项目（软删除）: id={}, name={}, userId={}", projectId, project.getName(), userId);
            return R.ok(null, "项目删除成功");
        } catch (Exception e) {
            log.error("删除项目失败: projectId={}, userId={}", projectId, userId, e);
            return R.fail("项目删除失败: " + e.getMessage());
        }
    }

    @Override
    public R<Project> getProjectById(Long projectId) {
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在");
            }
            
            // 填充创建人姓名
            fillCreatorName(project);
            
            return R.ok(project);
        } catch (Exception e) {
            log.error("获取项目失败: projectId={}", projectId, e);
            return R.fail("获取项目失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> getAllProjects(Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.findAll(pageable);
            
            // 填充创建人姓名
            if (projects.hasContent()) {
                fillCreatorNames(projects.getContent());
            }
            
            return R.ok(projects);
        } catch (Exception e) {
            log.error("获取项目列表失败", e);
            return R.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> getProjectsByCreator(Long creatorId, Pageable pageable) {
        try {
            // 只查询未删除的项目
            Page<Project> projects = projectRepository.findByCreatorIdAndIsDeleted(creatorId, false, pageable);
            
            // 填充创建人姓名
            if (projects.hasContent()) {
                fillCreatorNames(projects.getContent());
            }
            
            return R.ok(projects);
        } catch (Exception e) {
            log.error("获取用户创建的项目列表失败: creatorId={}", creatorId, e);
            return R.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        try {
            // 只查询未删除的项目
            Page<Project> projects = projectRepository.findByStatusAndIsDeleted(status, false, pageable);
            
            // 填充创建人姓名
            if (projects.hasContent()) {
                fillCreatorNames(projects.getContent());
            }
            
            return R.ok(projects);
        } catch (Exception e) {
            log.error("根据状态获取项目列表失败: status={}", status, e);
            return R.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> getUserProjects(Long userId, Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.findUserProjects(userId, pageable);
            
            // 填充创建人姓名
            if (projects.hasContent()) {
                fillCreatorNames(projects.getContent());
            }
            
            return R.ok(projects);
        } catch (Exception e) {
            log.error("获取用户参与的项目列表失败: userId={}", userId, e);
            return R.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> searchProjects(String keyword, Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.searchByKeyword(keyword, pageable);
            
            // 填充创建人姓名
            if (projects.hasContent()) {
                fillCreatorNames(projects.getContent());
            }
            
            return R.ok(projects);
        } catch (Exception e) {
            log.error("搜索项目失败: keyword={}", keyword, e);
            return R.fail("搜索项目失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> getPublicActiveProjects(Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.findPublicActiveProjects(pageable);
            
            // 批量查询创建者姓名并填充到项目对象中
            if (projects.hasContent()) {
                // 提取所有唯一的创建者ID
                List<Long> creatorIds = projects.getContent().stream()
                        .map(Project::getCreatorId)
                        .distinct()
                        .collect(Collectors.toList());
                
                // 批量查询创建者信息
                try {
                    R<List<UserDTO>> usersResponse = authServiceClient.getUsersByIds(creatorIds);
                    if (usersResponse != null && usersResponse.getData() != null) {
                        // 创建 userId -> userName 的映射
                        Map<Long, String> userNameMap = usersResponse.getData().stream()
                                .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
                        
                        // 填充创建者姓名
                        projects.getContent().forEach(project -> {
                            String creatorName = userNameMap.get(project.getCreatorId());
                            project.setCreatorName(creatorName != null ? creatorName : "未知用户");
                        });
                        
                        log.debug("成功填充 {} 个项目的创建者姓名", projects.getContent().size());
                    } else {
                        log.warn("批量查询用户信息失败，将使用默认值");
                        // 如果批量查询失败，设置默认值
                        projects.getContent().forEach(project -> project.setCreatorName("未知用户"));
                    }
                } catch (Exception userQueryException) {
                    log.error("批量查询用户信息时发生异常，将使用默认值", userQueryException);
                    // 发生异常时，设置默认值
                    projects.getContent().forEach(project -> project.setCreatorName("未知用户"));
                }
            }
            
            return R.ok(projects);
        } catch (Exception e) {
            log.error("获取公开活跃项目失败", e);
            return R.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Project> updateProjectStatus(Long projectId, ProjectStatus status) {
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在");
            }

            project.setStatus(status);
            project = projectRepository.save(project);

            log.info("成功更新项目状态: id={}, status={}", projectId, status);
            return R.ok(project, "项目状态更新成功");
        } catch (Exception e) {
            log.error("更新项目状态失败: projectId={}, status={}", projectId, status, e);
            return R.fail("更新项目状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> archiveProject(Long projectId, Long userId) {
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在");
            }

            // 检查是否为项目拥有者
            if (!project.isOwner(userId)) {
                return R.fail("只有项目拥有者才能归档项目");
            }

            project.setStatus(ProjectStatus.ARCHIVED);
            projectRepository.save(project);

            log.info("成功归档项目: id={}, userId={}", projectId, userId);
            return R.ok(null, "项目归档成功");
        } catch (Exception e) {
            log.error("归档项目失败: projectId={}, userId={}", projectId, userId, e);
            return R.fail("归档项目失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> hasAccessPermission(Long projectId, Long userId) {
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.ok(false);
            }

            // 公开项目所有人都可以访问
            if (project.getVisibility() == ProjectVisibility.PUBLIC) {
                return R.ok(true);
            }

            // 私有项目只有成员可以访问
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
            return R.ok(isMember);
        } catch (Exception e) {
            log.error("检查访问权限失败: projectId={}, userId={}", projectId, userId, e);
            return R.fail("检查访问权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<Long> countUserCreatedProjects(Long userId) {
        try {
            long count = projectRepository.countByCreatorId(userId);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计用户创建的项目数量失败: userId={}", userId, e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }

    @Override
    public R<Long> countUserParticipatedProjects(Long userId) {
        try {
            long count = projectMemberRepository.countByUserId(userId);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计用户参与的项目数量失败: userId={}", userId, e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 填充单个项目的创建人姓名
     * 
     * @param project 项目对象
     */
    private void fillCreatorName(Project project) {
        if (project == null || project.getCreatorId() == null) {
            return;
        }
        
        try {
            R<UserDTO> userResponse = authServiceClient.getUserById(project.getCreatorId());
            if (userResponse != null && userResponse.getData() != null) {
                project.setCreatorName(userResponse.getData().getName());
                log.debug("成功填充项目 {} 的创建者姓名: {}", project.getId(), project.getCreatorName());
            } else {
                project.setCreatorName("未知用户");
                log.warn("查询用户信息失败，项目 {} 的创建者姓名设置为默认值", project.getId());
            }
        } catch (Exception e) {
            log.error("查询用户信息时发生异常，项目 {} 的创建者姓名设置为默认值", project.getId(), e);
            project.setCreatorName("未知用户");
        }
    }
    
    /**
     * 批量填充项目列表的创建人姓名
     * 
     * @param projects 项目列表
     */
    private void fillCreatorNames(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return;
        }
        
        // 提取所有唯一的创建者ID
        List<Long> creatorIds = projects.stream()
                .map(Project::getCreatorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (creatorIds.isEmpty()) {
            return;
        }
        
        // 批量查询创建者信息
        try {
            R<List<UserDTO>> usersResponse = authServiceClient.getUsersByIds(creatorIds);
            if (usersResponse != null && usersResponse.getData() != null) {
                // 创建 userId -> userName 的映射
                Map<Long, String> userNameMap = usersResponse.getData().stream()
                        .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
                
                // 填充创建者姓名
                projects.forEach(project -> {
                    if (project.getCreatorId() != null) {
                        String creatorName = userNameMap.get(project.getCreatorId());
                        project.setCreatorName(creatorName != null ? creatorName : "未知用户");
                    }
                });
                
                log.debug("成功填充 {} 个项目的创建者姓名", projects.size());
            } else {
                log.warn("批量查询用户信息失败，将使用默认值");
                projects.forEach(project -> project.setCreatorName("未知用户"));
            }
        } catch (Exception e) {
            log.error("批量查询用户信息时发生异常，将使用默认值", e);
            projects.forEach(project -> project.setCreatorName("未知用户"));
        }
    }
}
