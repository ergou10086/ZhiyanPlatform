package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyanproject.model.dto.ProjectMemberDetailDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目成员服务实现类
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProjectMember addMember(Long projectId, Long userId, ProjectMemberRole role) {
        // 检查用户是否已经是项目成员
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new IllegalArgumentException("用户已经是该项目的成员");
        }

        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .projectRole(role)
                .joinedAt(LocalDateTime.now())
                .build();

        log.info("添加项目成员: projectId={}, userId={}, role={}", projectId, userId, role);
        return projectMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不是该项目的成员"));

        projectMemberRepository.delete(member);
        log.info("移除项目成员: projectId={}, userId={}", projectId, userId);
    }

    @Override
    @Transactional
    public ProjectMember updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不是该项目的成员"));

        member.setProjectRole(newRole);
        log.info("更新项目成员角色: projectId={}, userId={}, newRole={}", projectId, userId, newRole);
        return projectMemberRepository.save(member);
    }

    @Override
    public List<ProjectMember> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    @Override
    public List<ProjectMemberDetailDTO> getProjectMembersDetail(Long projectId, Long currentUserId) {
        // 获取项目信息
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            log.warn("项目不存在: projectId={}", projectId);
            return Collections.emptyList();
        }

        Project project = projectOptional.get();
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

        // 转换为DTO
        return members.stream()
                .map(member -> ProjectMemberDetailDTO.builder()
                        .id(member.getId())
                        .projectId(member.getProjectId())
                        .projectName(project.getName())
                        .userId(member.getUserId())
                        .projectRole(member.getProjectRole())
                        .roleName(member.getProjectRole().getRoleName())
                        .joinedAt(member.getJoinedAt())
                        .isCurrentUser(member.getUserId().equals(currentUserId))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectMember> getProjectMembersByName(String projectName) {
        // 根据项目名称查找项目
        Optional<Project> projectOptional = projectRepository.findByName(projectName);
        if (projectOptional.isEmpty()) {
            log.warn("未找到名称为 [{}] 的项目", projectName);
            return Collections.emptyList();
        }
        
        Long projectId = projectOptional.get().getId();
        log.info("根据项目名称 [{}] 查询成员，项目ID: {}", projectName, projectId);
        return projectMemberRepository.findByProjectId(projectId);
    }

    @Override
    public List<ProjectMember> getUserProjects(Long userId) {
        return projectMemberRepository.findByUserId(userId);
    }

    @Override
    public boolean isMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public Optional<ProjectMemberRole> getUserRole(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ProjectMember::getProjectRole);
    }

    @Override
    public List<ProjectMember> getMembersByRole(Long projectId, ProjectMemberRole role) {
        return projectMemberRepository.findByProjectIdAndProjectRole(projectId, role);
    }
}
