package hbnu.project.zhiyanwiki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanwiki.client.AuthServiceClient;
import hbnu.project.zhiyanwiki.model.dto.UserDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiCollaborationDTO;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Wiki 协同编辑服务
 * 管理在线编辑者、编辑位置等信息
 * 使用 Redis 管理在线编辑者、编辑位置等信息
 *
 * @author ErgouTree
 * @author yui，ErgouTree,YesMyDarkness
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiCollaborationService {

    @Resource
    private final AuthServiceClient authServiceClient;

    @Resource
    private final RedisTemplate<String, Object> redisTemplate;

    @Resource
    private final ObjectMapper objectMapper;

    // Redis Key 前缀
    private static final String PAGE_EDITORS_KEY = "wiki:editors:page:";
    private static final String USER_CURSOR_KEY = "wiki:cursor:user:";
    private static final String USER_PAGE_KEY = "wiki:user:page:";
    private static final String PAGE_CONTENT_LOCK_KEY = "wiki:lock:page:";

    // 过期时间（秒）
    private static final long EDITOR_TTL = 300; // 5分钟
    private static final long CURSOR_TTL = 60; // 1分钟
    private static final long CONTENT_LOCK_TTL = 30; // 30秒

    // 存储每个页面的在线编辑者（页面ID -> 用户ID集合）
    private final Map<Long, Set<Long>> pageEditors = new ConcurrentHashMap<>();

    // 存储每个用户的编辑位置（用户ID -> 编辑位置信息）
    private final Map<Long, WikiCollaborationDTO.CursorPosition> userCursors = new ConcurrentHashMap<>();

    // 存储用户与页面的映射（用户ID -> 正在编辑的页面ID）
    private final Map<Long, Long> userPageMap = new ConcurrentHashMap<>();


    /**
     * 用户加入编辑
     */
    public void joinEditing(Long pageId, Long userId) {
        try {
            // 添加到页面编辑者集合
            String pageKey = PAGE_EDITORS_KEY + pageId;
            redisTemplate.opsForSet().add(pageKey, userId.toString());
            redisTemplate.expire(pageKey, Duration.ofSeconds(EDITOR_TTL));

            // 记录用户正在编辑的页面
            String userPageKey = USER_PAGE_KEY + userId;
            redisTemplate.opsForValue().set(userPageKey, pageId.toString(), EDITOR_TTL, TimeUnit.SECONDS);

            log.info("用户[{}]加入页面[{}]的编辑", userId, pageId);
        }catch (Exception e){
            log.error("用户加入编辑失败: pageId={}, userId={}", pageId, userId, e);
        }
    }


    /**
     * 用户离开编辑
     */
    public void leaveEditing(Long userId){
        try {
            // 获取用户正在编辑的页面
            String userPageKey = USER_PAGE_KEY + userId;
            String pageIdStr = (String) redisTemplate.opsForValue().get(userPageKey);

            if (pageIdStr != null) {
                Long pageId = Long.parseLong(pageIdStr);

                // 从页面编辑者集合中移除
                String pageKey = PAGE_EDITORS_KEY + pageId;
                redisTemplate.opsForSet().remove(pageKey, userId.toString());

                // 检查集合是否为空
                Long size = redisTemplate.opsForSet().size(pageKey);
                if (size != null && size == 0) {
                    redisTemplate.delete(pageKey);
                }

                log.info("用户[{}]离开页面[{}]的编辑", userId, pageId);
            }

            // 删除用户页面映射
            redisTemplate.delete(userPageKey);

            // 删除用户光标位置
            String cursorKey = USER_CURSOR_KEY + userId;
            redisTemplate.delete(cursorKey);
        } catch (Exception e) {
            log.error("用户离开编辑失败: userId={}", userId, e);
        }
    }


    /**
     * 更新用户编辑的位置
     */
    public void updateCursorPosition(Long userId, WikiCollaborationDTO.CursorPosition position) {
        try {
            position.setLastUpdate(LocalDateTime.now());
            String cursorKey = USER_CURSOR_KEY + userId;

            //序列化，然后存储
            redisTemplate.opsForValue().set(
                    cursorKey,
                    position,
                    CURSOR_TTL,
                    TimeUnit.SECONDS
            );
        }catch (Exception e){
            log.error("更新光标位置失败: userId={}", userId, e);
        }
    }


    /**
     * 获取页面所有在线编辑者信息
     */
    public List<WikiCollaborationDTO.EditorInfo> getOnlineEditors(Long pageId) {
        try {
            String pageKey = PAGE_EDITORS_KEY + pageId;
            Set<Object> userIdsObj = redisTemplate.opsForSet().members(pageKey);

            if (userIdsObj == null || userIdsObj.isEmpty()) {
                return Collections.emptyList();
            }

            // 转换为 Long 列表
            List<Long> userIds = userIdsObj.stream()
                    .map(obj -> Long.parseLong(obj.toString()))
                    .toList();

            // 批量获取用户信息
            List<UserDTO> users = authServiceClient.getUsersByIds(userIds).getData();
            if (users == null) {
                return Collections.emptyList();
            }

            return users.stream()
                    .map(user -> WikiCollaborationDTO.EditorInfo.builder()
                            .userId(user.getId())
                            .username(user.getName())
                            .avatar(user.getAvatarUrl())
                            .joinTime(LocalDateTime.now())
                            .build())
                    .toList();
        }catch (Exception e){
            log.error("获取在线编辑者信息失败: pageId={}", pageId, e);
            return Collections.emptyList();
        }
    }


    /**
     * 检查用户是否在编辑页面
     */
    public boolean isUserEditing(Long userId, Long pageId) {
        try {
            String pageKey = PAGE_EDITORS_KEY + pageId;
            Boolean isMember = redisTemplate.opsForSet().isMember(pageKey, userId.toString());
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.error("检查用户编辑状态失败: userId={}, pageId={}", userId, pageId, e);
            return false;
        }
    }


    /**
     * 获取页面所有编辑者的光标位置
     */
    public List<WikiCollaborationDTO.CursorPosition> getAllEditorsCursor(Long pageId) {
        try{
            String pageKey = PAGE_EDITORS_KEY + pageId;
            Set<Object> userIdsObj = redisTemplate.opsForSet().members(pageKey);

            if(userIdsObj == null || userIdsObj.isEmpty()){
                return Collections.emptyList();
            }

            List<WikiCollaborationDTO.CursorPosition> positions = new ArrayList<>();

            for(Object userIdObj : userIdsObj){
                long userId = Long.parseLong(userIdObj.toString());
                String cursorKey = USER_CURSOR_KEY + userId;

                WikiCollaborationDTO.CursorPosition position =
                        (WikiCollaborationDTO.CursorPosition) redisTemplate.opsForValue().get(cursorKey);

                if (position != null) {
                    // 过滤掉超过5分钟未更新的位置
                    if (position.getLastUpdate() != null &&
                            position.getLastUpdate().isAfter(LocalDateTime.now().minusMinutes(5))) {
                        positions.add(position);
                    }
                }
            }

            return positions;
        }catch (Exception e){
            log.error("获取编辑者光标位置失败: pageId={}", pageId, e);
            return Collections.emptyList();
        }
    }


    /**
     * 获取页面当前编辑者数量
     */
    public Long getEditorCount(Long pageId) {
        try {
            String pageKey = PAGE_EDITORS_KEY + pageId;
            Long size = redisTemplate.opsForSet().size(pageKey);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("获取编辑者数量失败: pageId={}", pageId, e);
            return 0L;
        }
    }


    /**
     * 刷新用户编辑状态（延长过期时间）
     */
    public void refreshEditingStatus(Long pageId, Long userId) {
        try {
            String pageKey = PAGE_EDITORS_KEY + pageId;
            redisTemplate.expire(pageKey, Duration.ofSeconds(EDITOR_TTL));

            String userPageKey = USER_PAGE_KEY + userId;
            redisTemplate.expire(userPageKey, Duration.ofSeconds(EDITOR_TTL));
        } catch (Exception e) {
            log.error("刷新编辑状态失败: pageId={}, userId={}", pageId, userId, e);
        }
    }


    /**
     * 获取内容编辑锁
     */
    public boolean tryLockContent(Long userId, Long pageId) {
        try{
            String lockKey = PAGE_CONTENT_LOCK_KEY + pageId;
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    userId.toString(),
                    CONTENT_LOCK_TTL,
                    TimeUnit.SECONDS
            );
            return Boolean.TRUE.equals(success);
        }catch (Exception e){
            log.error("获取内容锁失败: pageId={}, userId={}", pageId, userId, e);
            return false;
        }
    }


    /**
     * 释放内容编辑锁
     */
    public void releaseLock(Long pageId, Long userId) {
        try {
            String lockKey = PAGE_CONTENT_LOCK_KEY + pageId;
            String lockOwner = (String) redisTemplate.opsForValue().get(lockKey);
            if (userId.toString().equals(lockOwner)) {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            log.error("释放内容锁失败: pageId={}, userId={}", pageId, userId, e);
        }
    }
}
