package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.entity.ProjectRole;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.repository.ProjectRoleRepository;
import hbnu.project.zhiyanproject.service.ProjectRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目角色服务实现类
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectRoleServiceImpl implements ProjectRoleService {

    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public R<ProjectRole> createProjectRole(Long projectId, ProjectMemberRole roleEnum, String customRoleName) {
        try {
            // 验证项目是否存在
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在: " + projectId);
            }

            // 确定角色名称
            String roleName = StringUtils.hasText(customRoleName) ? customRoleName : roleEnum.getRoleName();

            // 检查角色名称在项目中是否已存在
            if (projectRoleRepository.existsByProjectIdAndName(projectId, roleName)) {
                return R.fail("角色名称在该项目中已存在: " + roleName);
            }

            // 创建项目角色
            ProjectRole projectRole = ProjectRole.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .name(roleName)
                    .description(roleEnum.getDescription())
                    .roleType("PROJECT")
                    .projectId(projectId)
                    .roleEnum(roleEnum)
                    .isSystemDefault(false)
                    .build();

            projectRole = projectRoleRepository.save(projectRole);

            log.info("成功创建项目角色: {} -> {}, 项目ID: {}", roleEnum.getRoleName(), roleName, projectId);
            return R.ok(projectRole, "项目角色创建成功");
        } catch (Exception e) {
            log.error("创建项目角色失败: projectId={}, roleEnum={}, customRoleName={}", projectId, roleEnum, customRoleName, e);
            return R.fail("项目角色创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> deleteProjectRole(Long roleId) {
        try {
            ProjectRole projectRole = projectRoleRepository.findById(roleId).orElse(null);
            if (projectRole == null) {
                return R.fail("项目角色不存在");
            }

            // 注意：ProjectRole 实体与 ProjectMember 没有直接关联
            // ProjectMember 使用的是 ProjectMemberRole 枚举
            // 如果需要关联检查，需要重新设计实体关系
            
            // 暂时允许删除（因为没有实际关联）
            // long memberCount = projectMemberRepository.countByProjectRoleId(roleId);
            // if (memberCount > 0) {
            //     return R.fail("该角色正在被 " + memberCount + " 个成员使用，无法删除");
            // }

            projectRoleRepository.delete(projectRole);
            log.info("成功删除项目角色: {}", projectRole.getName());
            return R.ok(null, "项目角色删除成功");
        } catch (Exception e) {
            log.error("删除项目角色失败: roleId={}", roleId, e);
            return R.fail("项目角色删除失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<ProjectRole> updateProjectRole(Long roleId, String name, String description) {
        try {
            ProjectRole projectRole = projectRoleRepository.findById(roleId).orElse(null);
            if (projectRole == null) {
                return R.fail("项目角色不存在");
            }

            // 检查新名称是否在项目中已存在（排除当前角色）
            if (StringUtils.hasText(name) && !name.equals(projectRole.getName())) {
                if (projectRoleRepository.existsByProjectIdAndNameAndIdNot(projectRole.getProjectId(), name, roleId)) {
                    return R.fail("角色名称在该项目中已存在: " + name);
                }
                projectRole.setName(name);
            }

            if (StringUtils.hasText(description)) {
                projectRole.setDescription(description);
            }

            projectRole = projectRoleRepository.save(projectRole);
            log.info("成功更新项目角色: {}", projectRole.getName());
            return R.ok(projectRole, "项目角色更新成功");
        } catch (Exception e) {
            log.error("更新项目角色失败: roleId={}, name={}, description={}", roleId, name, description, e);
            return R.fail("项目角色更新失败: " + e.getMessage());
        }
    }

    @Override
    public R<ProjectRole> getProjectRoleById(Long roleId) {
        try {
            ProjectRole projectRole = projectRoleRepository.findById(roleId).orElse(null);
            if (projectRole == null) {
                return R.fail("项目角色不存在");
            }
            return R.ok(projectRole);
        } catch (Exception e) {
            log.error("查询项目角色失败: roleId={}", roleId, e);
            return R.fail("查询项目角色失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<ProjectRole>> getProjectRolesByProjectId(Long projectId, Pageable pageable) {
        try {
            Page<ProjectRole> rolePage = projectRoleRepository.findByProjectId(projectId, pageable);
            log.debug("获取项目[{}]角色列表，页码: {}, 大小: {}, 总数: {}",
                    projectId, pageable.getPageNumber(), pageable.getPageSize(), rolePage.getTotalElements());
            return R.ok(rolePage);
        } catch (Exception e) {
            log.error("获取项目角色列表失败: projectId={}", projectId, e);
            return R.fail("获取项目角色列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> assignRoleToUser(Long userId, Long projectId, ProjectMemberRole roleEnum) {
        try {
            // 检查用户是否已经是项目成员
            ProjectMember existingMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);
            if (existingMember != null) {
                // 更新现有成员的角色
                existingMember.setProjectRole(roleEnum);
                projectMemberRepository.save(existingMember);
                log.info("更新用户[{}]在项目[{}]中的角色为[{}]", userId, projectId, roleEnum.getRoleName());
                return R.ok(null, "用户角色更新成功");
            }

            // 创建新的项目成员记录
            ProjectMember projectMember = ProjectMember.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .projectId(projectId)
                    .userId(userId)
                    .projectRole(roleEnum)
                    .joinedAt(LocalDateTime.now())
                    .build();

            projectMemberRepository.save(projectMember);
            log.info("为用户[{}]在项目[{}]中分配角色[{}]成功", userId, projectId, roleEnum.getRoleName());
            return R.ok(null, "项目角色分配成功");
        } catch (Exception e) {
            log.error("为用户分配项目角色失败: userId={}, projectId={}, roleEnum={}", userId, projectId, roleEnum, e);
            return R.fail("项目角色分配失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> removeUserRole(Long userId, Long projectId) {
        try {
            ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);
            if (projectMember == null) {
                return R.ok(null, "用户不是该项目的成员");
            }

            projectMemberRepository.delete(projectMember);
            log.info("移除用户[{}]在项目[{}]中的角色成功", userId, projectId);
            return R.ok(null, "用户项目角色移除成功");
        } catch (Exception e) {
            log.error("移除用户项目角色失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("用户项目角色移除失败: " + e.getMessage());
        }
    }

    @Override
    public R<ProjectMemberRole> getUserRoleInProject(Long userId, Long projectId) {
        try {
            ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);
            if (projectMember == null) {
                return R.fail("用户不是该项目的成员");
            }
            return R.ok(projectMember.getProjectRole());
        } catch (Exception e) {
            log.error("获取用户项目角色失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("获取用户项目角色失败: " + e.getMessage());
        }
    }

    @Override
    public R<Set<ProjectPermission>> getUserPermissionsInProject(Long userId, Long projectId) {
        try {
            ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);
            if (projectMember == null) {
                return R.fail("用户不是该项目的成员");
            }

            Set<ProjectPermission> permissions = new HashSet<>(projectMember.getProjectRole().getPermissions());
            log.debug("获取用户[{}]在项目[{}]中的权限列表，共{}个权限", userId, projectId, permissions.size());
            return R.ok(permissions);
        } catch (Exception e) {
            log.error("获取用户项目权限失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("获取用户项目权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> hasPermission(Long userId, Long projectId, ProjectPermission permission) {
        try {
            ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);
            if (projectMember == null) {
                return R.ok(false);
            }

            boolean hasPermission = projectMember.getProjectRole().hasPermission(permission);
            return R.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查用户项目权限失败: userId={}, projectId={}, permission={}", userId, projectId, permission, e);
            return R.fail("检查用户项目权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> hasPermission(Long userId, Long projectId, String permissionCode) {
        try {
            ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId).orElse(null);
            if (projectMember == null) {
                return R.ok(false);
            }

            boolean hasPermission = projectMember.getProjectRole().hasPermission(permissionCode);
            return R.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查用户项目权限失败: userId={}, projectId={}, permissionCode={}", userId, projectId, permissionCode, e);
            return R.fail("检查用户项目权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Object>> getProjectMembersWithRoles(Long projectId, Pageable pageable) {
        try {
            Page<ProjectMember> memberPage = projectMemberRepository.findByProjectId(projectId, pageable);
            
            List<Object> memberRoleList = memberPage.getContent().stream()
                    .map(member -> {
                        Map<String, Object> memberInfo = new HashMap<>();
                        memberInfo.put("userId", member.getUserId());
                        memberInfo.put("projectRole", member.getProjectRole());
                        memberInfo.put("roleName", member.getProjectRole().getRoleName());
                        memberInfo.put("joinedAt", member.getJoinedAt());
                        memberInfo.put("permissions", member.getProjectRole().getPermissions());
                        return memberInfo;
                    })
                    .collect(Collectors.toList());

            Page<Object> result = new PageImpl<>(memberRoleList, pageable, memberPage.getTotalElements());
            log.debug("获取项目[{}]成员角色列表，页码: {}, 大小: {}, 总数: {}",
                    projectId, pageable.getPageNumber(), pageable.getPageSize(), memberPage.getTotalElements());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取项目成员角色列表失败: projectId={}", projectId, e);
            return R.fail("获取项目成员角色列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<List<ProjectRole>> initializeDefaultRoles(Long projectId) {
        try {
            // 验证项目是否存在
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在: " + projectId);
            }

            List<ProjectRole> defaultRoles = new ArrayList<>();

            // 为每个角色枚举创建默认角色
            for (ProjectMemberRole roleEnum : ProjectMemberRole.values()) {
                String roleName = roleEnum.getRoleName();
                
                // 检查角色是否已存在
                if (!projectRoleRepository.existsByProjectIdAndName(projectId, roleName)) {
                    ProjectRole projectRole = ProjectRole.builder()
                            .id(SnowflakeIdUtil.nextId())
                            .name(roleName)
                            .description(roleEnum.getDescription())
                            .roleType("PROJECT")
                            .projectId(projectId)
                            .roleEnum(roleEnum)
                            .isSystemDefault(true)
                            .build();

                    defaultRoles.add(projectRoleRepository.save(projectRole));
                }
            }

            log.info("为项目[{}]初始化了{}个默认角色", projectId, defaultRoles.size());
            return R.ok(defaultRoles, "项目默认角色初始化成功");
        } catch (Exception e) {
            log.error("初始化项目默认角色失败: projectId={}", projectId, e);
            return R.fail("初始化项目默认角色失败: " + e.getMessage());
        }
    }
}