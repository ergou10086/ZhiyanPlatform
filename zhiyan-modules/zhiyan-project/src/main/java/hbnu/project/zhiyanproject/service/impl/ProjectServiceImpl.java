package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
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

    @Override
    @Transactional
    public R<Project> createProject(String name, String description, ProjectVisibility visibility,
                                    LocalDate startDate, LocalDate endDate, String imageUrl, Long creatorId) {
        try {
            // 验证项目名称是否已存在
            if (projectRepository.existsByName(name)) {
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
                if (projectRepository.existsByNameAndIdNot(name, projectId)) {
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
            return R.ok(projects);
        } catch (Exception e) {
            log.error("获取项目列表失败", e);
            return R.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> getProjectsByCreator(Long creatorId, Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.findByCreatorId(creatorId, pageable);
            return R.ok(projects);
        } catch (Exception e) {
            log.error("获取用户创建的项目列表失败: creatorId={}", creatorId, e);
            return R.fail("获取项目列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Project>> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.findByStatus(status, pageable);
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
}
