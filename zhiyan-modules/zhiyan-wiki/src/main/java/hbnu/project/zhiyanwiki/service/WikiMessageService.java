package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyancommonbasic.domain.R;

import hbnu.project.zhiyanmessage.model.pojo.SendMessageRequestPOJO;
import hbnu.project.zhiyanwiki.client.AuthServiceClient;
import hbnu.project.zhiyanwiki.client.MessageServiceClient;
import hbnu.project.zhiyanwiki.client.ProjectServiceClient;
import hbnu.project.zhiyanwiki.model.dto.ProjectDTO;
import hbnu.project.zhiyanwiki.model.dto.UserDTO;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.model.enums.PageType;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * wiki相关的消息服务
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiMessageService {

    @Resource
    private final MessageServiceClient messageServiceClient;

    @Resource
    private final AuthServiceClient authServiceClient;

    @Resource
    private final ProjectServiceClient projectServiceClient;


    /**
     * 新建wiki页面通知
     */
    public void notifyWikiPageCreate(WikiPage wikiPage, Long projectId, Long creatorId) {
        // 必要字段不完整，不行
        if (wikiPage == null || projectId == null || creatorId == null) {
            log.error("必要字段不完整，信息无法发送");
        }

        List<Long> projectMemberIds = getProjectMemberUserIds(projectId);

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送通知，难道发给pxl？", projectId);
            return;
        }

        // 过滤创建者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(receiverId -> !receiverId.equals(creatorId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.warn("都不知道是谁搞的就发送？");
            return;
        }

        // 获取创建者姓名
        String creatorName = getUserNameById(creatorId);

        // 获取项目名称
        String projectName = getProjectNameById(projectId);

        // 构建消息内容
        String pageTypeText = wikiPage.getPageType() == PageType.DIRECTORY ? "目录" : "文档";
        String content = String.format(
                "项目「%s」有新Wiki%s被创建\n页面名：「%s」\n创建者：「%s」\n该Wiki已更新，请及时查看",
                projectName,
                pageTypeText,
                wikiPage.getTitle(),
                creatorName
        );

        SendMessageRequestPOJO requestDTO = SendMessageRequestPOJO.builder()
                .scene("WIKI_PAGE_CREATED")
                .senderId(creatorId)
                .receiverIds(filteredReceiverIds)
                .title("Wiki页面创建")
                .content(content)
                .businessId(wikiPage.getId())
                .businessType("WIKI")
                .extendData(buildWikiPageExtendData(wikiPage, projectId))
                .build();

        sendBatchMessage(requestDTO, wikiPage.getId());
    }


    /**
     * Wiki页面更新通知
     * 发送给除了编辑者之外的所有项目成员
     */
    public void notifyWikiPageUpdate(WikiPage wikiPage, Long projectId, Long editorId, String changeDescription) {
        // 参数校验
        if (wikiPage == null || projectId == null || editorId == null) {
            log.error("Wiki页面更新通知参数不完整，无法发送");
            return;
        }

        // 获取项目成员列表
        List<Long> projectMemberIds = getProjectMemberUserIds(projectId);

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送Wiki更新通知", projectId);
            return;
        }

        // 过滤编辑者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(receiverId -> !receiverId.equals(editorId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.info("过滤后没有接收者，跳过发送Wiki更新通知");
            return;
        }

        // 获取编辑者姓名
        String editorName = getUserNameById(editorId);

        // 获取项目名称
        String projectName = getProjectNameById(projectId);

        // 构建消息内容
        String changeDesc = (changeDescription != null && !changeDescription.isEmpty())
                ? "\n修改说明：" + changeDescription
                : "";
        String content = String.format(
                "项目「%s」的Wiki页面被更新\n页面名：「%s」\n编辑者：「%s」%s\n请及时查看",
                projectName,
                wikiPage.getTitle(),
                editorName,
                changeDesc
        );

        SendMessageRequestPOJO requestDTO = SendMessageRequestPOJO.builder()
                .scene("WIKI_PAGE_UPDATED")
                .senderId(editorId)
                .receiverIds(filteredReceiverIds)
                .title("Wiki页面更新")
                .content(content)
                .businessId(wikiPage.getId())
                .businessType("WIKI")
                .extendData(buildWikiPageExtendData(wikiPage, projectId))
                .build();

        sendBatchMessage(requestDTO, wikiPage.getId());
    }


    /**
     * Wiki页面删除通知
     * 发送给除了删除者之外的所有项目成员
     */
    public void notifyWikiPageDelete(WikiPage wikiPage, Long projectId, Long operatorId) {
        // 参数校验
        if (wikiPage == null || projectId == null || operatorId == null) {
            log.error("Wiki页面删除通知参数不完整，无法发送");
            return;
        }

        // 获取项目成员列表
        List<Long> projectMemberIds = getProjectMemberUserIds(projectId);

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送Wiki删除通知", projectId);
            return;
        }

        // 过滤操作者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(receiverId -> !receiverId.equals(operatorId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.info("过滤后没有接收者，跳过发送Wiki删除通知");
            return;
        }

        // 获取操作者姓名
        String operatorName = getUserNameById(operatorId);

        // 获取项目名称
        String projectName = getProjectNameById(projectId);

        // 构建消息内容
        String pageTypeText = wikiPage.getPageType() == PageType.DIRECTORY ? "目录" : "文档";
        String content = String.format(
                "项目「%s」的Wiki%s被删除\n页面名：「%s」\n操作者：「%s」\n请注意相关内容已被移除",
                projectName,
                pageTypeText,
                wikiPage.getTitle(),
                operatorName
        );

        SendMessageRequestPOJO requestDTO = SendMessageRequestPOJO.builder()
                .scene("WIKI_PAGE_DELETED")
                .senderId(operatorId)
                .receiverIds(filteredReceiverIds)
                .title("Wiki页面删除")
                .content(content)
                .businessId(wikiPage.getId())
                .businessType("WIKI")
                .extendData(buildWikiPageExtendData(wikiPage, projectId))
                .build();

        sendBatchMessage(requestDTO, wikiPage.getId());
    }


    /**
     * Wiki页面移动通知（更改父目录）
     * 发送给除了操作者之外的所有项目成员
     */
    public void notifyWikiPageMove(WikiPage wikiPage, Long projectId, Long operatorId, String oldPath, String newPath) {
        // 参数校验
        if (wikiPage == null || projectId == null || operatorId == null) {
            log.error("Wiki页面移动通知参数不完整，无法发送");
            return;
        }

        // 获取项目成员列表
        List<Long> projectMemberIds = getProjectMemberUserIds(projectId);

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送Wiki移动通知", projectId);
            return;
        }

        // 过滤操作者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(receiverId -> !receiverId.equals(operatorId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.info("过滤后没有接收者，跳过发送Wiki移动通知");
            return;
        }

        // 获取操作者姓名
        String operatorName = getUserNameById(operatorId);

        // 获取项目名称
        String projectName = getProjectNameById(projectId);

        // 构建消息内容
        String content = String.format(
                "项目「%s」的Wiki页面位置发生变更\n页面名：「%s」\n原路径：%s\n新路径：%s\n操作者：「%s」",
                projectName,
                wikiPage.getTitle(),
                oldPath != null ? oldPath : "根目录",
                newPath != null ? newPath : "根目录",
                operatorName
        );

        SendMessageRequestPOJO requestDTO = SendMessageRequestPOJO.builder()
                .scene("WIKI_PAGE_MOVED")
                .senderId(operatorId)
                .receiverIds(filteredReceiverIds)
                .title("Wiki页面移动")
                .content(content)
                .businessId(wikiPage.getId())
                .businessType("WIKI")
                .extendData(buildWikiPageExtendData(wikiPage, projectId))
                .build();

        sendBatchMessage(requestDTO, wikiPage.getId());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 发送批量消息
     *
     * @param request  消息请求DTO
     * @param wikiPageId Wiki页面ID（用于日志）
     */
    private void sendBatchMessage(SendMessageRequestPOJO request, Long wikiPageId) {
        try {
            R<Void> resp = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(resp)) {
                log.warn("发送Wiki批量消息失败: wikiPageId={}, error={}", wikiPageId, resp.getMsg());
            } else {
                log.info("Wiki批量消息发送成功: wikiPageId={}, scene={}, receiverCount={}",
                        wikiPageId, request.getScene(),
                        request.getReceiverIds() != null ? request.getReceiverIds().size() : 0);
            }
        } catch (Exception ex) {
            log.error("调用Wiki批量消息服务异常: wikiPageId={}", wikiPageId, ex);
        }
    }


    /**
     * 构建Wiki页面扩展数据（JSON格式）
     *
     * @param wikiPage  Wiki页面实体
     * @param projectId 项目ID
     * @return JSON格式的扩展数据
     */
    private String buildWikiPageExtendData(WikiPage wikiPage, Long projectId) {
        return String.format(
                "{\"wikiPageId\":%d,\"projectId\":%d,\"title\":\"%s\",\"pageType\":\"%s\",\"path\":\"%s\",\"isPublic\":%s,\"jumpUrl\":\"/project-wiki/%d/page/%d\"}",
                wikiPage.getId(),
                projectId,
                escapeJson(wikiPage.getTitle()),
                wikiPage.getPageType().name(),
                escapeJson(wikiPage.getPath() != null ? wikiPage.getPath() : ""),
                Boolean.TRUE.equals(wikiPage.getIsPublic()),
                projectId,
                wikiPage.getId()
        );
    }


    /**
     * 转义JSON字符串中的特殊字符
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 根据项目ID获取项目所有成员的用户ID列表
     *
     * @param projectId 项目ID
     * @return 用户ID列表
     */
    public List<Long> getProjectMemberUserIds(Long projectId) {
        try {
            R<List<Long>> result = projectServiceClient.getProjectMemberUserIds(projectId);
            if (R.isSuccess(result) && result.getData() != null) {
                log.info("获取项目[{}]成员ID列表成功，共{}个成员", projectId, result.getData().size());
                return result.getData();
            } else {
                log.warn("获取项目[{}]成员ID列表失败: {}", projectId, result.getMsg());
                return List.of();
            }
        } catch (Exception e) {
            log.error("获取项目[{}]成员ID列表异常", projectId, e);
            return List.of();
        }
    }


    /**
     * 根据用户ID获取单个用户名
     *
     * @param userId 用户ID
     * @return 用户名，如果获取失败返回"未知用户"
     */
    public String getUserNameById(Long userId) {
        if(userId == null){
            return "未知用户";
        }
        try{
            // 调用返回 UserDTO 的接口
            R<UserDTO> result = authServiceClient.getUserById(userId);
            if(R.isSuccess(result) && result.getData() != null){
                return result.getData().getName() != null ? result.getData().getName() : "未知用户";
            }else {
                log.warn("获取用户[{}]信息失败: {}", userId, result.getMsg());
                return "未知用户";
            }
        }catch (Exception e){
            log.error("获取用户[{}]信息异常", userId, e);
            return "未知用户";
        }
    }


    /**
     * 根据项目ID获取项目名称
     *
     * @param projectId 项目ID
     * @return 项目名称，如果获取失败返回"未知项目"
     */
    public String getProjectNameById(Long projectId) {
        if(projectId == null){
            return "未知项目";
        }

        try{
            R<ProjectDTO> result = projectServiceClient.getProjectById(projectId);
            if (R.isSuccess(result) && result.getData() != null) {
                return result.getData().getName() != null ? result.getData().getName() : "未知项目";
            } else {
                log.warn("获取项目[{}]信息失败: {}", projectId, result.getMsg());
                return "未知项目";
            }
        }catch (Exception e){
            log.error("获取项目[{}]信息异常", projectId, e);
            return "未知项目";
        }
    }
}
