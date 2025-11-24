package hbnu.project.zhiyanproject.utils.message;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanmessage.model.pojo.SendMessageRequestPOJO;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.client.MessageServiceClient;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目相关消息工具类
 * 避免在serviceimpl中大量编写发送消息的方法
 * 把消息发送的具体内容集中在这里
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectMessageUtils {

    private final MessageServiceClient messageServiceClient;
    private final AuthServiceClient authServiceClient;

    /**
     * 根据用户ID获取用户名
     *
     * @param userId 用户ID
     * @return 用户名，如果获取失败返回"未知用户"
     */
    private String getUserNameById(Long userId) {
        if (userId == null) {
            return "未知用户";
        }

        try {
            R<UserDTO> result = authServiceClient.getUserById(userId);
            if (R.isSuccess(result) && result.getData() != null && result.getData().getName() != null) {
                return result.getData().getName();
            } else {
                log.warn("获取用户[{}]信息失败: {}", userId, result != null ? result.getMsg() : "未知错误");
                return "未知用户";
            }
        } catch (Exception e) {
            log.error("获取用户[{}]信息异常", userId, e);
            return "未知用户";
        }
    }

    /**
     * 发送项目创建成功通知
     */
    public void sendProjectCreatedNotification(Project project, Long creatorId) {
        try{
            // 获取创建者姓名
            String creatorName = getUserNameById(creatorId);

            SendMessageRequestPOJO request = SendMessageRequestPOJO.builder()
                    .scene("PROJECT_CREATED")
                    .senderId(null) // 系统消息
                    .receiverId(creatorId)
                    .title("项目创建成功")
                    .content(String.format("您的项目「%s」已创建成功，创建者「%s」，开始您的项目管理之旅吧!",
                            project.getName(), creatorName))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project))
                    .build();

            R<Void> result = messageServiceClient.sendPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送项目创建通知失败: projectId={}, error={}",
                        project.getId(), result.getMsg());
            }
        }catch(Exception e){
            log.error("发送项目创建通知异常: projectId={}", project.getId(), e);
        }
    }


    /**
     * 发送项目状态变更通知
     */
    public void sendProjectStatusChangedNotification(Project project, List<Long> projectMemberIds, ProjectStatus oldStatus, ProjectStatus newStatus) {
        try {
            if (projectMemberIds.isEmpty()) {
                log.warn("你通知个击败，一个人没有");
                return;
            }

            SendMessageRequestPOJO request = SendMessageRequestPOJO.builder()
                    .scene("PROJECT_STATUS_CHANGED")
                    .receiverIds(projectMemberIds)
                    .title("项目状态变更")
                    .content(String.format("项目「%s」的状态已从「%s」变更为「%s」",
                            project.getName(),
                            oldStatus.getDescription(),
                            newStatus.getDescription()))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送项目状态变更通知失败: projectId={}, error={}",
                        project.getId(), result.getMsg());
            }
        }catch(Exception e){
            log.error("发送项目状态变更通知异常: projectId={}", project.getId(), e);
        }
    }


    /**
     * 发送项目删除通知
     */
    public void sendProjectDeletedNotification(Project project, List<Long> projectMemberIds, Long operatorId) {
        try {
            if (projectMemberIds.isEmpty()) {
                log.warn("你通知个击败，一个人没有");
                return;
            }

            // 获取操作者姓名
            String operatorName = operatorId != null ? getUserNameById(operatorId) : "系统";

            SendMessageRequestPOJO request = SendMessageRequestPOJO.builder()
                    .scene("PROJECT_DELETED")
                    .senderId(operatorId)
                    .receiverIds(projectMemberIds)
                    .title("项目已删除")
                    .content(String.format("项目「%s」已被删除，操作者「%s」，相关数据已归档",
                            project.getName(), operatorName))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送项目删除通知失败: projectId={}, error={}",
                        project.getId(), result.getMsg());
            }
        }catch(Exception e){
            log.error("发送项目删除通知异常: projectId={}", project.getId(), e);
        }
    }


    /**
     * 发送项目归档通知
     */
    public void sendProjectArchivedNotification(Project project, List<Long> projectMemberIds, Long operatorId) {
        try{
            if (projectMemberIds.isEmpty()) {
                return;
            }

            // 获取操作者姓名
            String operatorName = operatorId != null ? getUserNameById(operatorId) : "系统";

            SendMessageRequestPOJO request = SendMessageRequestPOJO.builder()
                    .scene("PROJECT_ARCHIVED")
                    .senderId(operatorId)
                    .receiverIds(projectMemberIds)
                    .title("项目已归档")
                    .content(String.format("项目「%s」已归档，操作者「%s」，您可以在归档列表中查看",
                            project.getName(), operatorName))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送项目归档通知失败: projectId={}, error={}",
                        project.getId(), result.getMsg());
            }
        }catch(Exception e){
            log.error("发送项目归档通知异常: projectId={}", project.getId(), e);
        }
    }


    /**
     * 构建项目扩展数据(JSON格式)
     */
    private String buildProjectExtendData(Project project) {
        return String.format("{\"projectId\":%d,\"projectName\":\"%s\",\"jumpUrl\":\"/projects/%d\"}",
                project.getId(),
                project.getName().replace("\"", "\\\""),
                project.getId());
    }

    /**
     * 获取状态变更描述
     */
    private String getStatusChangeDescription(ProjectStatus oldStatus, ProjectStatus newStatus) {
        return String.format("从「%s」变更为「%s」",
                oldStatus.getDescription(),
                newStatus.getDescription());
    }
}