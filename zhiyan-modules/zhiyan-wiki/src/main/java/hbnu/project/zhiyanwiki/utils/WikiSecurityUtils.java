package hbnu.project.zhiyanwiki.utils;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanwiki.client.ProjectServiceClient;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.repository.WikiPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Wiki权限工具类
 * 用于检查用户对Wiki资源的访问权限
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WikiSecurityUtils {

    @Autowired
    private final WikiPageRepository wikiPageRepository;

    @Autowired
    private final ProjectServiceClient projectServiceClient;

    /**
     * 检查当前用户是否为Wiki所属项目的成员
     *
     * @param wikiPageId Wiki页面ID
     * @return 是否为项目成员
     */
    public boolean isProjectMember(Long wikiPageId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return isProjectMember(wikiPageId, userId);
    }


    /**
     * 检查指定用户是否为Wiki所属项目的成员
     *
     * @param wikiPageId Wiki页面ID
     * @param userId     用户ID
     * @return 是否为项目成员
     */
    public boolean isProjectMember(Long wikiPageId, Long userId) {
        WikiPage page = wikiPageRepository.findById(wikiPageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        return isProjectMemberByProjectId(page.getProjectId(), userId);
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
            // 调用Project服务检查成员关系
            return projectServiceClient.isProjectMember(projectId, userId);
        } catch (Exception e) {
            log.error("检查项目成员关系失败: projectId={}, userId={}", projectId, userId, e);
            return false;
        }
    }


    /**
     * 检查当前用户是否有权限访问Wiki页面
     * 规则：
     * 1. 公开的Wiki页面所有人都可以访问
     * 2. 非公开的Wiki页面只有项目成员可以访问
     *
     * @param wikiPageId Wiki页面ID
     * @return 是否有访问权限
     */
    public boolean canAccess(Long wikiPageId) {
        WikiPage page = wikiPageRepository.findById(wikiPageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        // 公开页面所有人都可以访问
        if (Boolean.TRUE.equals(page.getIsPublic())) {
            return true;
        }

        // 非公开页面需要是项目成员
        return isProjectMember(wikiPageId);
    }


    /**
     * 检查当前用户是否有权限编辑Wiki页面
     * 规则：必须是项目成员
     *
     * @param wikiPageId Wiki页面ID
     * @return 是否有编辑权限
     */
    public boolean canEdit(Long wikiPageId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }

        WikiPage page = wikiPageRepository.findById(wikiPageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        // 必须是项目成员才能编辑
        return isProjectMemberByProjectId(page.getProjectId(), userId);
    }


    /**
     * 检查当前用户是否有权限删除Wiki页面
     * 规则：必须是项目成员且是创建者或项目管理员
     *
     * @param wikiPageId Wiki页面ID
     * @return 是否有删除权限
     */
    public boolean canDelete(Long wikiPageId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }

        WikiPage page = wikiPageRepository.findById(wikiPageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        // 必须是项目成员
        if (!isProjectMemberByProjectId(page.getProjectId(), userId)) {
            return false;
        }

        // 创建者可以删除自己的页面
        if (page.getCreatorId().equals(userId)) {
            return true;
        }

        // 项目拥有者/管理员可以删除任何页面
        try {
            return projectServiceClient.isProjectOwner(page.getProjectId(), userId) ||
                    projectServiceClient.hasPermission(page.getProjectId(), userId, "WIKI_DELETE");
        } catch (Exception e) {
            log.error("检查删除权限失败: wikiPageId={}, userId={}", wikiPageId, userId, e);
            return false;
        }
    }


    /**
     * 验证当前用户有访问权限，如果没有则抛出异常
     *
     * @param wikiPageId Wiki页面ID
     * @throws SecurityException 如果没有权限
     */
    public void requireAccess(Long wikiPageId) {
        if (!canAccess(wikiPageId)) {
            throw new SecurityException("您没有权限访问此Wiki页面");
        }
    }


    /**
     * 验证当前用户有编辑权限，如果没有则抛出异常
     *
     * @param wikiPageId Wiki页面ID
     * @throws SecurityException 如果没有权限
     */
    public void requireEdit(Long wikiPageId) {
        if (!canEdit(wikiPageId)) {
            throw new SecurityException("您没有权限编辑此Wiki页面，只有项目成员可以编辑");
        }
    }


    /**
     * 验证当前用户有删除权限，如果没有则抛出异常
     *
     * @param wikiPageId Wiki页面ID
     * @throws SecurityException 如果没有权限
     */
    public void requireDelete(Long wikiPageId) {
        if (!canDelete(wikiPageId)) {
            throw new SecurityException("您没有权限删除此Wiki页面，只有创建者或项目管理员可以删除");
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
     * 检查当前用户是否有权限访问项目的Wiki
     *
     * @param projectId 项目ID
     * @return 是否有权限
     */
    public boolean canAccessProject(Long projectId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return isProjectMemberByProjectId(projectId, userId);
    }
}
