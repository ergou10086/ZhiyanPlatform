package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.model.dto.ProjectMemberDetailDTO;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.form.InviteMemberRequest;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // ==================== 成员管理相关 ====================

    @Override
    @Transactional
    public ProjectMember inviteMember(Long projectId, InviteMemberRequest request, Long inviterId) {
        // 1. 检查项目是否存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 2. 检查邀请人是否为项目负责人
        if (!isOwner(projectId, inviterId)) {
            throw new IllegalArgumentException("只有项目负责人可以邀请成员");
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
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .projectRole(role)
                .joinedAt(LocalDateTime.now())
                .build();

        ProjectMember saved = projectMemberRepository.save(member);
        log.info("项目[{}]负责人[{}]直接添加用户[{}]为项目成员，角色: {}", projectId, inviterId, userId, role);

        // TODO: 发送通知给被添加的用户（通过消息队列），告知已被添加到项目

        return saved;
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId, Long operatorId) {
        // 1. 检查操作人是否为项目负责人
        if (!isOwner(projectId, operatorId)) {
            throw new IllegalArgumentException("只有项目负责人可以移除成员");
        }

        // 2. 不能移除负责人自己
        if (userId.equals(operatorId)) {
            throw new IllegalArgumentException("不能移除自己，如需退出项目请使用退出功能");
        }

        // 3. 检查被移除用户是否为项目成员
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("该用户不是项目成员"));

        // 4. 不能移除项目负责人
        if (member.getProjectRole() == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("不能移除项目负责人");
        }

        // 5. 移除成员
        projectMemberRepository.delete(member);
        log.info("项目[{}]负责人[{}]移除成员[{}]", projectId, operatorId, userId);

        // TODO: 发送通知给被移除用户（通过消息队列）
    }

    @Override
    @Transactional
    public ProjectMember updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole, Long operatorId) {
        // 1. 检查操作人是否为项目负责人
        if (!isOwner(projectId, operatorId)) {
            throw new IllegalArgumentException("只有项目负责人可以修改成员角色");
        }

        // 2. 查询成员
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("该用户不是项目成员"));

        // 3. 不能修改负责人的角色
        if (member.getProjectRole() == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("不能修改项目负责人的角色");
        }

        // 4. 更新角色
        member.setProjectRole(newRole);
        ProjectMember saved = projectMemberRepository.save(member);

        log.info("项目[{}]负责人[{}]将成员[{}]角色修改为: {}", projectId, operatorId, userId, newRole);

        // TODO: 发送通知给该成员（通过消息队列）

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

        // TODO: 发送通知给项目负责人（通过消息队列）
    }

    // ==================== 查询相关 ====================

    @Override
    public Page<ProjectMemberDetailDTO> getProjectMembers(Long projectId, Pageable pageable) {
        // 1. 查询项目
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 2. 查询成员
        Page<ProjectMember> members = projectMemberRepository.findByProjectId(projectId, pageable);

        // 3. 批量查询用户信息
        List<Long> userIds = members.getContent().stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());

        Map<Long, UserDTO> userMap = new HashMap<>();
        try {
            R<Map<Long, UserDTO>> userResponse = authServiceClient.getUsersByIds(userIds);
            if (R.isSuccess(userResponse) && userResponse.getData() != null) {
                userMap = userResponse.getData();
            }
        } catch (Exception e) {
            log.error("批量查询用户信息失败", e);
        }

        // 4. 转换为DTO
        return members.map(member -> {
            UserDTO user = userMap.get(member.getUserId());
            return ProjectMemberDetailDTO.builder()
                    .id(member.getId())
                    .projectId(member.getProjectId())
                    .projectName(project.getName())
                    .userId(member.getUserId())
                    .username(user != null ? user.getName() : "未知用户")
                    .email(user != null ? user.getEmail() : "")
                    .projectRole(member.getProjectRole())
                    .roleName(member.getProjectRole().getRoleName())
                    .joinedAt(member.getJoinedAt())
                    .isCurrentUser(false) // 在Controller层设置
                    .build();
        });
    }

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
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(member -> member.getProjectRole() == ProjectMemberRole.OWNER)
                .orElse(false);
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

    @Override
    public ProjectMember addMember(Long projectId, Long creatorId, Enum roleEnum) {
        return null;
    }
}
