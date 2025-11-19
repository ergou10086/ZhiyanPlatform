package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.service.UserCacheService;
import hbnu.project.zhiyanproject.model.dto.ProjectMemberDTO;
import hbnu.project.zhiyanproject.model.dto.RoleInfoDTO;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import hbnu.project.zhiyanproject.model.form.InviteMemberRequest;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import hbnu.project.zhiyanproject.utils.message.ProjectMemberMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目成员服务实现类
 * 项目成员采用直接邀请方式，无需申请审批流程
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final AuthServiceClient authServiceClient;
    private final UserCacheService userCacheService;

    private final ProjectMemberMessageUtils projectMemberMessageUtils;

    // ==================== 成员管理相关 ====================

    @Override
    @Transactional
    public ProjectMember inviteMember(Long projectId, InviteMemberRequest request, Long inviterId) {
        // 1. 检查项目是否存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 2. 检查邀请人是否为项目管理员（OWNER或ADMIN）
        if (!isAdmin(projectId, inviterId)) {
            throw new IllegalArgumentException("只有项目管理员可以邀请成员");
        }

        // 3. 检查被邀请用户是否存在
        Long userId = request.getUserId();
        R<UserDTO> userResponse = authServiceClient.getUserById(userId);
        if (!R.isSuccess(userResponse) || userResponse.getData() == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 4. 检查用户是否已经是项目成员
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new IllegalArgumentException("该用户已经是项目成员");
        }

        // 5. 添加成员
        ProjectMemberRole role = request.getRole() != null ? request.getRole() : ProjectMemberRole.MEMBER;
        
        // 6. ADMIN不能将新成员设置为OWNER，只有OWNER可以
        ProjectMemberRole inviterRole = getUserRole(projectId, inviterId);
        if (inviterRole == ProjectMemberRole.ADMIN && role == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("管理员不能将新成员设置为项目负责人");
        }
        
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .projectRole(role)
                .joinedAt(LocalDateTime.now())
                .build();

        ProjectMember saved;
        try {
            saved = projectMemberRepository.save(member);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 处理并发情况下的唯一约束冲突
            if (e.getMessage().contains("project_id") || e.getMessage().contains("Duplicate entry")) {
                throw new IllegalArgumentException("该用户已经是项目成员");
            }
            throw e;
        }
        
        log.info("项目[{}]管理员[{}]直接添加用户[{}]为项目成员，角色: {}", projectId, inviterId, userId, role);

        // 发送邀请通知给新成员
        projectMemberMessageUtils.sendMessageInvitedNotification(project, member, inviterId);

        // 通知项目其他成员有新成员加入
        List<Long> existingMemberIds = projectMemberRepository.findByProjectId(project.getId())
                .stream()
                .map(ProjectMember::getUserId)
                .filter(id -> !id.equals(member.getUserId()))
                .collect(Collectors.toList());
        projectMemberMessageUtils.sendNewMemberJoinedNotification(project, member, inviterId, existingMemberIds);

        return saved;
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId, Long operatorId) {
        // 0.查询项目是否存在
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 1. 检查操作人是否为项目管理员（OWNER或ADMIN）
        if (!isAdmin(projectId, operatorId)) {
            throw new IllegalArgumentException("只有项目管理员可以移除成员");
        }

        // 2. 不能移除自己
        if (userId.equals(operatorId)) {
            throw new IllegalArgumentException("不能移除自己，如需退出项目请使用退出功能");
        }

        // 3. 检查被移除用户是否为项目成员
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("该用户不是项目成员"));

        // 4. 不能移除项目负责人（OWNER），只有OWNER自己可以转让项目
        if (member.getProjectRole() == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("不能移除项目负责人");
        }

        // 5. ADMIN不能移除其他ADMIN，只有OWNER可以
        ProjectMemberRole operatorRole = getUserRole(projectId, operatorId);
        if (operatorRole == ProjectMemberRole.ADMIN && member.getProjectRole() == ProjectMemberRole.ADMIN) {
            throw new IllegalArgumentException("管理员不能移除其他管理员，只有项目负责人可以");
        }

        // 6. 移除成员
        projectMemberRepository.delete(member);
        log.info("项目[{}]管理员[{}]移除成员[{}]", projectId, operatorId, userId);

        // 发送通知给被移除用户
        projectMemberMessageUtils.sendMemberRemovedNotification(project, userId, operatorId);

        // 通知其他成员
        List<Long> adminIds = projectMemberRepository.findByProjectId(project.getId())
                .stream()
                .filter(m -> !m.getUserId().equals(userId))
                .filter(m -> m.getProjectRole() == ProjectMemberRole.OWNER ||
                        m.getProjectRole() == ProjectMemberRole.ADMIN)
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());
        projectMemberMessageUtils.sendMemberLeftNotification(project, member, operatorId, adminIds);
    }

    @Override
    @Transactional
    public ProjectMember updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole, Long operatorId) {
        // 0.查询项目是否存在
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 1. 检查操作人是否为项目管理员（OWNER或ADMIN）
        if (!isAdmin(projectId, operatorId)) {
            throw new IllegalArgumentException("只有项目管理员可以修改成员角色");
        }

        // 2. 查询成员
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("该用户不是项目成员"));

        // 3. 不能修改负责人的角色
        if (member.getProjectRole() == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("不能修改项目负责人的角色");
        }

        // 4. ADMIN不能修改其他ADMIN的角色，只有OWNER可以
        ProjectMemberRole operatorRole = getUserRole(projectId, operatorId);
        if (operatorRole == ProjectMemberRole.ADMIN && member.getProjectRole() == ProjectMemberRole.ADMIN) {
            throw new IllegalArgumentException("管理员不能修改其他管理员的角色，只有项目负责人可以");
        }

        // 5. ADMIN可以将MEMBER提升为ADMIN，但不能设置为OWNER
        if (operatorRole == ProjectMemberRole.ADMIN && newRole == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("管理员不能将成员设置为项目负责人");
        }

        // 6. 更新角色
        ProjectMemberRole oldRole = member.getProjectRole();
        member.setProjectRole(newRole);
        ProjectMember saved = projectMemberRepository.save(member);

        log.info("项目[{}]管理员[{}]将成员[{}]角色修改为: {}", projectId, operatorId, userId, newRole);

        // 发送通知给该成员（通过消息队列）
        projectMemberMessageUtils.sendMemberRoleChangedNotification(project, member, oldRole.getRoleName(), newRole.getRoleName(), operatorId);

        // 同时通知项目管理员
        List<Long> adminIds = projectMemberRepository.findByProjectId(project.getId())
                .stream()
                .filter(m -> !m.getUserId().equals(member.getUserId()))
                .filter(m -> m.getProjectRole() == ProjectMemberRole.OWNER ||
                        m.getProjectRole() == ProjectMemberRole.ADMIN)
                .map(ProjectMember::getUserId)
                .toList();

        projectMemberMessageUtils.sendMemberRoleChangedNotificationToAdmins(project, member, oldRole.getRoleName(), newRole.getRoleName(), adminIds);

        return saved;
    }

    @Override
    @Transactional
    public void leaveProject(Long projectId, Long userId) {
        // 1. 检查用户是否为项目成员
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("您不是该项目的成员"));

        // 2. 项目负责人不能直接退出
        if (member.getProjectRole() == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("项目负责人不能退出项目，请先转让项目或删除项目");
        }

        // 3. 退出项目
        projectMemberRepository.delete(member);
        log.info("用户[{}]退出项目[{}]", userId, projectId);

        // 发送通知给项目负责人和管理员
        List<Long> adminIds = projectMemberRepository.findByProjectId(projectId)
                .stream()
                .filter(m -> m.getProjectRole() == ProjectMemberRole.OWNER ||
                        m.getProjectRole() == ProjectMemberRole.ADMIN)
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());

        projectMemberMessageUtils.sendMemberLeftNotification(
                projectRepository.findById(projectId).orElse(null),
                member,
                userId,
                adminIds
        );
    }

    // ==================== 查询相关 ====================

    @Override
    public Page<ProjectMember> getMyProjects(Long userId, Pageable pageable) {
        return projectMemberRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<ProjectMember> getMembersByRole(Long projectId, ProjectMemberRole role) {
        return projectMemberRepository.findByProjectIdAndProjectRole(projectId, role);
    }

    @Override
    public boolean isMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public boolean isOwner(Long projectId, Long userId) {
        log.info("检查用户[{}]是否为项目[{}]的拥有者", userId, projectId);
        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (memberOpt.isPresent()) {
            ProjectMember member = memberOpt.get();
            boolean isOwner = member.getProjectRole() == ProjectMemberRole.OWNER;
            log.info("用户[{}]在项目[{}]中的角色: {}, 是否为拥有者: {}", userId, projectId, member.getProjectRole(), isOwner);
            return isOwner;
        } else {
            log.warn("用户[{}]不是项目[{}]的成员，尝试查询所有成员", userId, projectId);
            // 尝试查询项目中的所有成员，看看是否有问题
            List<ProjectMember> allMembers = projectMemberRepository.findByProjectId(projectId);
            log.info("项目[{}]的所有成员 (总数: {}): {}", projectId, allMembers.size(), allMembers.stream()
                    .map(m -> "userId=" + m.getUserId() + " (类型: " + m.getUserId().getClass().getSimpleName() + "), role=" + m.getProjectRole())
                    .collect(Collectors.joining(", ")));
            log.info("当前查询的用户ID: {} (类型: {})", userId, userId != null ? userId.getClass().getSimpleName() : "null");
            return false;
        }
    }

    @Override
    public boolean isAdmin(Long projectId, Long userId) {
        log.info("检查用户[{}]是否为项目[{}]的管理员", userId, projectId);
        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (memberOpt.isPresent()) {
            ProjectMember member = memberOpt.get();
            ProjectMemberRole role = member.getProjectRole();
            boolean isAdmin = role == ProjectMemberRole.OWNER || role == ProjectMemberRole.ADMIN;
            log.info("用户[{}]在项目[{}]中的角色: {}, 是否为管理员: {}", userId, projectId, role, isAdmin);
            return isAdmin;
        } else {
            log.warn("用户[{}]不是项目[{}]的成员，尝试查询所有成员", userId, projectId);
            // 尝试查询项目中的所有成员，看看是否有问题
            List<ProjectMember> allMembers = projectMemberRepository.findByProjectId(projectId);
            log.info("项目[{}]的所有成员 (总数: {}): {}", projectId, allMembers.size(), allMembers.stream()
                    .map(m -> "userId=" + m.getUserId() + " (类型: " + m.getUserId().getClass().getSimpleName() + "), role=" + m.getProjectRole())
                    .collect(Collectors.joining(", ")));
            log.info("当前查询的用户ID: {} (类型: {})", userId, userId != null ? userId.getClass().getSimpleName() : "null");
            return false;
        }
    }

    @Override
    public ProjectMemberRole getUserRole(Long projectId, Long userId) {
        return projectMemberRepository.findUserRoleInProject(userId, projectId)
                .orElse(null);
    }

    @Override
    public long getMemberCount(Long projectId) {
        return projectMemberRepository.countByProjectId(projectId);
    }

    // ==================== 新增方法（用于新的角色管理接口） ====================

    @Override
    @Transactional
    public ProjectMember addMemberInternal(Long projectId, Long userId, ProjectMemberRole role) {
        // 1. 检查项目是否存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 2. 检查用户是否已经是项目成员
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            log.warn("用户[{}]已经是项目[{}]的成员", userId, projectId);
            return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                    .orElse(null);
        }

        // 3. 创建项目成员记录（不验证用户，用于内部调用）
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .projectRole(role)
                .joinedAt(LocalDateTime.now())
                .build();

        // 4. 保存到数据库
        ProjectMember saved = projectMemberRepository.save(member);
        log.info("成功添加用户[{}]到项目[{}]，角色: {}（内部调用）", userId, projectId, role);

        return saved;
    }

    @Override
    @Transactional
    public R<Void> addMemberWithValidation(Long projectId, Long userId, ProjectMemberRole role) {
        try {
            // 1. 检查项目是否存在
            if (!projectRepository.existsById(projectId)) {
                return R.fail("项目不存在");
            }

            // 2. 检查用户是否存在（通过认证服务）
            R<UserDTO> userResponse = authServiceClient.getUserById(userId);
            if (!R.isSuccess(userResponse) || userResponse.getData() == null) {
                return R.fail("用户不存在");
            }

            // 3. 检查用户是否已经是项目成员
            if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
                return R.fail("该用户已经是项目成员");
            }

            // 4. 创建成员记录
            ProjectMember member = ProjectMember.builder()
                    .projectId(projectId)
                    .userId(userId)
                    .projectRole(role)
                    .joinedAt(LocalDateTime.now())
                    .build();

            try {
                projectMemberRepository.save(member);
                log.info("成功添加用户[{}]到项目[{}]，角色: {}（API调用）", userId, projectId, role);
                return R.ok();
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // 处理并发情况下的唯一约束冲突
                if (e.getMessage().contains("project_id") || e.getMessage().contains("Duplicate entry")) {
                    return R.fail("该用户已经是项目成员");
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("添加项目成员失败: projectId={}, userId={}, role={}", projectId, userId, role, e);
            return R.fail("添加成员失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> removeMember(Long projectId, Long userId) {
        try {
            // 1. 检查成员是否存在
            ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                    .orElse(null);
            
            if (member == null) {
                return R.fail("该用户不是项目成员");
            }

            // 2. 移除成员
            projectMemberRepository.delete(member);
            log.info("成功移除项目[{}]成员[{}]", projectId, userId);

            return R.ok();
        } catch (Exception e) {
            log.error("移除项目成员失败: projectId={}, userId={}", projectId, userId, e);
            return R.fail("移除成员失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole) {
        try {
            // 1. 查询成员
            ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                    .orElse(null);
            
            if (member == null) {
                return R.fail("该用户不是项目成员");
            }

            // 2. 不能修改拥有者的角色
            if (member.getProjectRole() == ProjectMemberRole.OWNER && newRole != ProjectMemberRole.OWNER) {
                return R.fail("不能修改项目拥有者的角色");
            }

            // 3. 更新角色
            member.setProjectRole(newRole);
            projectMemberRepository.save(member);
            log.info("成功更新项目[{}]成员[{}]角色为: {}", projectId, userId, newRole);

            return R.ok();
        } catch (Exception e) {
            log.error("更新成员角色失败: projectId={}, userId={}, newRole={}", projectId, userId, newRole, e);
            return R.fail("更新角色失败: " + e.getMessage());
        }
    }

    @Override
    public R<ProjectMemberRole> getMemberRole(Long projectId, Long userId) {
        try {
            ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                    .orElse(null);
            
            if (member == null) {
                return R.fail("该用户不是项目成员");
            }

            return R.ok(member.getProjectRole());
        } catch (Exception e) {
            log.error("获取成员角色失败: projectId={}, userId={}", projectId, userId, e);
            return R.fail("获取角色失败: " + e.getMessage());
        }
    }

    @Override
    public R<RoleInfoDTO> getUserRoleInfo(Long userId, Long projectId) {
        try {
            // 1. 查询成员角色
            ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                    .orElse(null);
            
            if (member == null) {
                return R.fail("该用户不是项目成员");
            }

            ProjectMemberRole role = member.getProjectRole();

            // 2. 获取角色权限
            Set<String> permissions = role.getPermissions().stream()
                    .map(ProjectPermission::getCode)
                    .collect(Collectors.toSet());

            // 3. 构建 DTO
            RoleInfoDTO dto = RoleInfoDTO.builder()
                    .roleCode(role.name())
                    .roleName(role.getRoleName())
                    .roleDescription(role.getDescription())
                    .permissions(permissions)
                    .build();

            return R.ok(dto);
        } catch (Exception e) {
            log.error("获取用户角色信息失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("获取角色信息失败: " + e.getMessage());
        }
    }

    @Override
    public R<Set<String>> getUserPermissions(Long userId, Long projectId) {
        try {
            // 1. 查询成员角色
            ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                    .orElse(null);
            
            if (member == null) {
                return R.fail("该用户不是项目成员");
            }

            // 2. 获取角色权限
            Set<String> permissions = member.getProjectRole().getPermissions().stream()
                    .map(ProjectPermission::getCode)
                    .collect(Collectors.toSet());

            return R.ok(permissions);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("获取权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> hasPermission(Long userId, Long projectId, String permissionCode) {
        try {
            // 1. 查询成员角色
            ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                    .orElse(null);
            
            if (member == null) {
                return R.ok(false);
            }

            // 2. 检查权限
            boolean hasPermission = member.getProjectRole().hasPermission(permissionCode);
            return R.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, projectId={}, permission={}", 
                    userId, projectId, permissionCode, e);
            return R.fail("检查权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<ProjectMemberDTO>> getProjectMembers(Long projectId, Pageable pageable) {
        try {
            // 1. 检查项目是否存在
            if (!projectRepository.existsById(projectId)) {
                return R.fail("项目不存在");
            }

            // 2. 查询成员
            Page<ProjectMember> members = projectMemberRepository.findByProjectId(projectId, pageable);

            // 3. 批量查询用户信息
            List<Long> userIds = members.getContent().stream()
                    .map(ProjectMember::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("准备批量查询用户信息，用户ID列表: {}", userIds);

            Map<Long, UserDTO> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                try {
                    R<List<UserDTO>> userResponse = userCacheService.getUsersByIds(userIds);
                    log.debug("批量查询用户响应: code={}, hasData={}", 
                             userResponse != null ? userResponse.getCode() : null,
                             userResponse != null && userResponse.getData() != null);
                    
                    if (R.isSuccess(userResponse) && userResponse.getData() != null) {
                        List<UserDTO> users = userResponse.getData();
                        log.debug("成功获取 {} 个用户信息", users.size());
                        
                        // 打印每个用户的详细信息（调试用）
                        users.forEach(user -> log.debug("用户信息: id={}, name={}, email={}, avatarUrl={}",
                                 user.getId(), user.getName(), user.getEmail(), user.getAvatarUrl()));
                        
                        // 将List转换为Map
                        userMap = users.stream()
                                .filter(user -> user.getId() != null)
                                .collect(Collectors.toMap(UserDTO::getId, user -> user));
                    } else {
                        log.warn("批量查询用户信息失败: code={}, msg={}", 
                                userResponse != null ? userResponse.getCode() : null,
                                userResponse != null ? userResponse.getMsg() : null);
                    }
                } catch (Exception e) {
                    log.error("批量查询用户信息异常", e);
                }
            }
            final Map<Long, UserDTO> finalUserMap = userMap;

            // 4. 转换为 DTO
            Page<ProjectMemberDTO> dtoPage = members.map(member -> {
                UserDTO user = finalUserMap.get(member.getUserId());
                ProjectMemberRole role = member.getProjectRole();
                
                // 如果查询不到用户信息，记录警告
                if (user == null) {
                    log.warn("无法获取用户信息: userId={}, 将使用默认值", member.getUserId());
                }
                
                String username = user != null && user.getName() != null ? user.getName() : "未知用户";
                String email = user != null && user.getEmail() != null ? user.getEmail() : "";
                String avatar = user != null && user.getAvatarUrl() != null ? user.getAvatarUrl() : "";
                
                return ProjectMemberDTO.builder()
                        .id(member.getId())
                        .userId(member.getUserId())
                        .username(username)
                        .email(email)
                        .avatar(avatar)
                        .projectId(member.getProjectId())
                        .roleCode(role.name())
                        .roleName(role.getRoleName())
                        .joinedAt(member.getJoinedAt())
                        .build();
            });

            return R.ok(dtoPage);
        } catch (Exception e) {
            log.error("获取项目成员列表失败: projectId={}", projectId, e);
            return R.fail("获取成员列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<Long> getProjectMemberUserIds(Long projectId) {
        return projectMemberRepository.findUserIdsByProjectId(projectId);
    }
}
