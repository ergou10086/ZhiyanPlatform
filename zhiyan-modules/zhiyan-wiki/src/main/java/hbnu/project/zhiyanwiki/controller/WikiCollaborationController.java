package hbnu.project.zhiyanwiki.controller;

import hbnu.project.zhiyancommonbasic.exception.ControllerException;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanwiki.model.dto.WikiCollaborationDTO;
import hbnu.project.zhiyanwiki.service.WikiCollaborationService;
import hbnu.project.zhiyanwiki.service.WikiContentService;
import hbnu.project.zhiyanwiki.service.WikiPageService;
import hbnu.project.zhiyanwiki.utils.WikiSecurityUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

/**
 * Wiki 协同编辑 WebSocket 消息控制器
 *
 * @author ErgouTree
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WikiCollaborationController {

    @Resource
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Resource
    private final WikiCollaborationService collaborationService;

    @Resource
    private final WikiContentService wikiContentService;

    @Resource
    private final WikiPageService wikiPageService;

    @Resource
    private final WikiSecurityUtils wikiSecurityUtils;


    /**
     * 用户加入编辑页面
     * 客户端发送消息到: /app/wiki/{pageId}/join
     */
    @MessageMapping("/wiki/{pageId}/join")
    public void joinEditing(
            @DestinationVariable Long pageId,
            Principal principal){
        try{
            Long userId = Long.parseLong(principal.getName());

            // 权限检查
            wikiSecurityUtils.requireAccess(pageId);

            // 旅行伙伴加入
            collaborationService.joinEditing(pageId, userId);

            // 刷新编辑状态
            collaborationService.refreshEditingStatus(pageId, userId);

            // 通知其他用户有新用户加入
            List<WikiCollaborationDTO.EditorInfo> editors =
                    collaborationService.getOnlineEditors(pageId);

            // 广播在线编辑者列表
            simpMessagingTemplate.convertAndSend(
                    "/topic/wiki/" + pageId + "/editors",
                    editors
            );

            // 发送当前在线编辑者信息给新加入的用户
            simpMessagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "queue/editors",
                    editors
            );

            // 发送当前所有光标位置给新加入的用户
            List<WikiCollaborationDTO.CursorPosition> cursors =
                    collaborationService.getAllEditorsCursor(pageId);
            if (!cursors.isEmpty()) {
                simpMessagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/cursors",
                        cursors
                );
            }

            log.info("用户[{}]加入页面[{}]编辑", userId, pageId);
        }catch (ControllerException e){
            log.error("用户加入编辑失败", e);
            simpMessagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    "加入编辑失败: " + e.getMessage()
            );
        }
    }


    /**
     * 用户离开编辑页面
     * 客户端发送消息到: /app/wiki/{pageId}/leave
     */
    @MessageMapping("/wiki/{pageId}/leave")
    public void leaveEditing(
            @DestinationVariable Long pageId,
            Principal principal) {
        try{
            Long userId = Long.parseLong(principal.getName());

            // 用户离开编辑
            collaborationService.leaveEditing(userId);

            // 释放内容锁（如果有）
            collaborationService.releaseLock(pageId, userId);

            // 通知其他用户有人离开
            List<WikiCollaborationDTO.EditorInfo> editors =
                    collaborationService.getOnlineEditors(pageId);

            // 发送消息给大伙
            simpMessagingTemplate.convertAndSend(
                    "topic/wiki/" + pageId + "/editors",
                    editors
            );
            log.info("用户[{}]离开页面[{}]编辑", userId, pageId);
        } catch (ControllerException e) {
            log.error("用户离开编辑失败", e);
        }
    }


    /**
     * 更新光标位置
     * 客户端发送消息到: /app/wiki/{pageId}/cursor
     */
    @MessageMapping("/wiki/{pageId}/cursor")
    public void updateCursor(
            @DestinationVariable Long pageId,
            @Payload WikiCollaborationDTO.CursorPosition cursorPosition,
            Principal principal
    ){
        try{
            Long userId = Long.parseLong(principal.getName());

            // 权限检查
            wikiSecurityUtils.requireAccess(pageId);

            // 检查用户是否在编辑
            if(!collaborationService.isUserEditing(userId, pageId)){
                return;
            }

            // 刷新编辑状态
            collaborationService.refreshEditingStatus(pageId, userId);

            // 保存用户位置
            cursorPosition.setUserId(userId);
            collaborationService.updateCursorPosition(userId, cursorPosition);

            // 广播光标位置给用户
            simpMessagingTemplate.convertAndSend(
                    "/topic/wiki/"+ pageId + "/cursors",
                    cursorPosition
            );
        }catch (ControllerException e){
            log.error("更新光标位置失败", e);
        }
    }


    /**
     * 提交内容变更
     * 客户端发送消息到: /app/wiki/{pageId}/content-change
     */
    @MessageMapping("/wiki/{pageId}/content-change")
    public void handleContentChange(
            @DestinationVariable Long pageId,
            @Payload WikiCollaborationDTO.ContentChange change,
            Principal principal) {
        try{
            Long userId = Long.parseLong(principal.getName());

            // 权限检查
            wikiSecurityUtils.requireEdit(pageId);

            // 尝试获取编辑锁（防止并发冲突）
            if(!collaborationService.tryLockContent(pageId, userId)){
                simpMessagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/error",
                        "该部分正在被其他用户编辑，为避免冲突请稍后重试"
                );
                return;
            }

            try {
                // 获取项目ID
                Long projectId = wikiContentService.getProjectIdByPageId(pageId);
                if (projectId == null) {
                    throw new RuntimeException("无法获取项目ID");
                }

                // 保存新版本，先输入的被覆盖
                String newContent = change.getContent();
                wikiContentService.saveNewVersion(
                        pageId,
                        projectId,
                        newContent,
                        "协同编辑更新",
                        userId
                );

                // 广播内容变更给所有编辑者
                change.setUserId(userId);
                change.setTimestamp(java.time.LocalDateTime.now());

                simpMessagingTemplate.convertAndSend(
                        "/topic/wiki/" + pageId + "/content",
                        change
                );

                log.info("用户[{}]提交页面[{}]内容变更", userId, pageId);
            }finally {
                // 释放锁
                collaborationService.releaseLock(pageId, userId);
            }
        } catch (ControllerException e) {
            log.error("处理内容变更失败", e);
            // 发送错误消息给用户
            simpMessagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    "保存失败: " + e.getMessage()
            );
        }
    }


    /**
     * 增量内容变更（用于实时同步）
     * 客户端发送消息到: /app/wiki/{pageId}/incremental-change
     */
    @MessageMapping("/wiki/{pageId}/incremental-change")
    public void handleIncrementalChange(
            @DestinationVariable Long pageId,
            @Payload WikiCollaborationDTO.IncrementalChange change,
            Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());

            // 权限检查
            if (!collaborationService.isUserEditing(userId, pageId)) {
                return;
            }

            // 刷新编辑状态
            collaborationService.refreshEditingStatus(pageId, userId);

            // 设置用户ID和时间戳
            change.setUserId(userId);
            change.setTimestamp(java.time.LocalDateTime.now());

            // 广播增量变更给其他用户（不发给发送者自己）
            simpMessagingTemplate.convertAndSend(
                    "/topic/wiki/" + pageId + "/incremental",
                    change
            );
        }catch (ControllerException e){
            log.error("处理增量变更失败", e);
        }
    }


    /**
     * 心跳消息（保持连接活跃）
     * 客户端发送消息到: /app/wiki/{pageId}/heartbeat
     */
    @MessageMapping("/wiki/{pageId}/heartbeat")
    public void heartbeat(
            @DestinationVariable Long pageId,
            Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            // 刷新编辑状态，延长过期时间
            collaborationService.refreshEditingStatus(pageId, userId);
        } catch (Exception e) {
            log.error("处理心跳失败", e);
        }
    }
}
