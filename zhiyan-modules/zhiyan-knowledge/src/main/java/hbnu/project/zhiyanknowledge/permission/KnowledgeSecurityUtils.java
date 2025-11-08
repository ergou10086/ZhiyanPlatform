package hbnu.project.zhiyanknowledge.permission;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanknowledge.client.ProjectServiceClient;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Knowledge模块权限工具类
 * 用于检查用户对成果的访问权限
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSecurityUtils {

    @Autowired
    private final AchievementRepository achievementRepository;

    @Autowired
    private final ProjectServiceClient projectServiceClient;

    /**
     * 检查当前用户是否为成果所属项目的成员
     *
     * @param achievementId 成果ID
     * @return 是否为项目成员
     */
    public boolean isProjectMember(Long achievementId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return isProjectMember(achievementId, userId);
    }


    /**
     * 检查指定用户是否为成果所属项目的成员
     *
     * @param achievementId 成果ID
     * @param userId        用户ID
     * @return 是否为项目成员
     */
    public boolean isProjectMember(Long achievementId, Long userId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        return isProjectMemberByProjectId(achievement.getProjectId(), userId);
    }


    /**
     * 根据项目ID检查用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为项目成员
     */
    public boolean isProjectMemberByProjectId(Long projectId, Long userId) {
        try {
            return projectServiceClient.isProjectMember(projectId, userId);
        } catch (Exception e) {
            log.error("检查项目成员关系失败: projectId={}, userId={}", projectId, userId, e);
            return false;
        }
    }


    /**
     * 检查当前用户是否有权限访问成果
     *
     * @param achievementId 成果ID
     * @return 是否有访问权限
     */
    public boolean canAccess(Long achievementId) {
        return isProjectMember(achievementId);
    }


    /**
     * 检查当前用户是否有权限编辑成果
     *
     * @param achievementId 成果ID
     * @return 是否有编辑权限
     */
    public boolean canEdit(Long achievementId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        // 必须是项目成员
        if (!isProjectMemberByProjectId(achievement.getProjectId(), userId)) {
            return false;
        }

        // 创建者或项目管理员可以编辑
        if (achievement.getCreatorId().equals(userId)) {
            return true;
        }

        try {
            return projectServiceClient.hasPermission(achievement.getProjectId(), userId, "KNOWLEDGE_MANAGE");
        } catch (Exception e) {
            log.error("检查编辑权限失败: achievementId={}, userId={}", achievementId, userId, e);
            return false;
        }
    }


    /**
     * 检查当前用户是否有权限删除成果
     *
     * @param achievementId 成果ID
     * @return 是否有删除权限
     */
    public boolean canDelete(Long achievementId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        // 必须是项目成员
        if (!isProjectMemberByProjectId(achievement.getProjectId(), userId)) {
            return false;
        }

        // 创建者可以删除
        if (achievement.getCreatorId().equals(userId)) {
            return true;
        }

        // 项目拥有者/管理员可以删除
        try {
            return projectServiceClient.isProjectOwner(achievement.getProjectId(), userId) ||
                    projectServiceClient.hasPermission(achievement.getProjectId(), userId, "KNOWLEDGE_MANAGE");
        } catch (Exception e) {
            log.error("检查删除权限失败: achievementId={}, userId={}", achievementId, userId, e);
            return false;
        }
    }


    /**
     * 验证当前用户是否有成果的访问权限，如果没有则抛出异常
     *
     * @param achievementId 成果ID
     * @throws SecurityException 如果没有权限
     */
    public void requireAccess(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        // 如果是公开成果，允许访问
        if (Boolean.TRUE.equals(achievement.getIsPublic())) {
            return;
        }

        // 如果是私有成果，必须是项目成员
        if (!canAccess(achievementId)) {
            throw new SecurityException("您没有权限访问此成果，该成果为项目私有，只有项目成员可以访问");
        }
    }


    /**
     * 验证当前用户有编辑权限，如果没有则抛出异常
     *
     * @param achievementId 成果ID
     * @throws SecurityException 如果没有权限
     */
    public void requireEdit(Long achievementId) {
        if (!canEdit(achievementId)) {
            throw new SecurityException("您没有权限编辑此成果，只有创建者或项目管理员可以编辑");
        }
    }


    /**
     * 验证当前用户有删除权限，如果没有则抛出异常
     *
     * @param achievementId 成果ID
     * @throws SecurityException 如果没有权限
     */
    public void requireDelete(Long achievementId) {
        if (!canDelete(achievementId)) {
            throw new SecurityException("您没有权限删除此成果，只有创建者或项目管理员可以删除");
        }
    }


    /**
     * 验证当前用户是项目成员，如果不是则抛出异常
     *
     * @param projectId 项目ID
     * @throws SecurityException 如果不是项目成员
     */
    public void requireProjectMember(Long projectId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null || !isProjectMemberByProjectId(projectId, userId)) {
            throw new SecurityException("您不是该项目的成员，无权访问");
        }
    }

    /**
     * 检查当前用户是否有权限访问该成果
     * 公开成果：所有人可访问
     * 私有成果：仅项目成员可访问
     *
     * @param achievementId 成果ID
     * @return 是否有访问权限
     */
    public boolean canAccessAchievementId(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        // 如果是公开成果，所有人都可以访问
        if (Boolean.TRUE.equals(achievement.getIsPublic())) {
            return true;
        }

        // 如果是私有成果，必须是项目成员
        return isProjectMember(achievementId);
    }
}