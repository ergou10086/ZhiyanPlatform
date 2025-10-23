package hbnu.project.zhiyancommonoss.service;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件权限验证服务
 * 用于验证用户是否有权限访问特定文件
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilePermissionService {

    // 成果文件路径格式: project-{projectId}/achievement-{achievementId}/v{version}_{filename}
    private static final Pattern ACHIEVEMENT_PATTERN =
            Pattern.compile("project-(\\d+)/achievement-(\\d+)/.*");

    // Wiki资源路径格式: project-{projectId}/{type}/{documentId}/{timestamp}_{filename}
    private static final Pattern WIKI_PATTERN =
            Pattern.compile("project-(\\d+)/(images|attachments)/.*");

    // 临时文件路径格式: user-{userId}/{sessionId}/{uuid}_{filename}
    private static final Pattern TEMP_PATTERN =
            Pattern.compile("user-(\\d+)/.*");

    // 项目封面路径格式: original/project-{projectId}.{ext}
    private static final Pattern PROJECT_COVER_PATTERN =
            Pattern.compile("original/project-(\\d+)\\..*");


    /**
     * 验证用户是否有权访问成果文件
     *
     * @param objectKey         对象键
     * @param userId            当前用户ID
     * @param userProjectIds    用户所在的项目ID的列表
     * @return 是否有权限
     */
    public boolean canAccessAchievementFile(String objectKey, Long userId, List<Long> userProjectIds) {
        Matcher matcher = ACHIEVEMENT_PATTERN.matcher(objectKey);

        if(!matcher.matches()){
            log.warn("成果文件路径格式不正确: {}", objectKey);
            return false;
        }

        // 提取项目ID
        Long projectId = Long.parseLong(matcher.group(1));

        // 检查用户是否在该项目中
        boolean hasAccess = userProjectIds != null && userProjectIds.contains(projectId);

        if (!hasAccess) {
            log.warn("用户{}无权访问项目{}的成果文件: {}", userId, projectId, objectKey);
        }

        return hasAccess;
    }


    /**
     * 验证用户是否有权访问临时文件
     *
     * @param objectKey 对象键
     * @param userId    当前用户ID
     * @return 是否有权限
     */
    public boolean canAccessTempFile(String objectKey, Long userId) {
        Matcher matcher = TEMP_PATTERN.matcher(objectKey);

        if (!matcher.matches()) {
            log.warn("临时文件路径格式不正确: {}", objectKey);
            return false;
        }

        Long fileUserId = Long.parseLong(matcher.group(1));
        boolean hasAccess = userId.equals(fileUserId);

        if (!hasAccess) {
            log.warn("用户{}无权访问用户{}的临时文件: {}", userId, fileUserId, objectKey);
        }

        return hasAccess;
    }


    /**
     * 从对象键中提取项目ID
     *
     * @param objectKey 对象键
     * @return 项目ID，如果无法提取则返回null
     */
    public Long extractProjectId(String objectKey) {
        // 尝试成果文件格式
        Matcher achievementMatcher = ACHIEVEMENT_PATTERN.matcher(objectKey);
        if (achievementMatcher.matches()) {
            return Long.parseLong(achievementMatcher.group(1));
        }

        // 尝试Wiki资源格式
        Matcher wikiMatcher = WIKI_PATTERN.matcher(objectKey);
        if (wikiMatcher.matches()) {
            return Long.parseLong(wikiMatcher.group(1));
        }

        // 尝试项目封面格式
        Matcher coverMatcher = PROJECT_COVER_PATTERN.matcher(objectKey);
        if (coverMatcher.matches()) {
            return Long.parseLong(coverMatcher.group(1));
        }

        return null;
    }


    /**
     * 从对象键中提取用户ID
     *
     * @param objectKey 对象键
     * @return 用户ID，如果无法提取则返回null
     */
    public Long extractUserId(String objectKey) {
        Matcher matcher = TEMP_PATTERN.matcher(objectKey);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }


    /**
     * 验证并抛出异常（如果无权限）
     *
     * @param hasPermission 是否有权限
     * @param message       错误消息
     */
    public void validateOrThrow(boolean hasPermission, String message) {
        if (!hasPermission) {
            throw new ServiceException(StringUtils.isEmpty(message) ? "无权限访问该文件" : message);
        }
    }


    /**
     * 验证用户是否有权访问Wiki文件
     *
     * @param objectKey      对象键
     * @param userId         当前用户ID
     * @param userProjectIds 用户所在的项目ID列表
     * @return 是否有权限
     */
    public boolean canAccessWikiFile(String objectKey, Long userId, List<Long> userProjectIds) {
        Matcher matcher = WIKI_PATTERN.matcher(objectKey);

        if (!matcher.matches()) {
            log.warn("Wiki文件路径格式不正确: {}", objectKey);
            return false;
        }

        // 提取项目ID
        Long projectId = Long.parseLong(matcher.group(1));

        // 检查用户是否在该项目中
        boolean hasAccess = userProjectIds != null && userProjectIds.contains(projectId);

        if (!hasAccess) {
            log.warn("用户{}无权访问项目{}的Wiki文件: {}", userId, projectId, objectKey);
        }

        return hasAccess;
    }
}
