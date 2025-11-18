package hbnu.project.zhiyanproject.utils.message;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanmessgae.model.dto.SendMessageRequestDTO;
import hbnu.project.zhiyanproject.client.MessageServiceClient;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 发送项目创建成功通知
     */
    public void sendProjectCreatedNotification(Project project, Long creatorId) {
        try{
            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_CREATED")
                    .senderId(null) // 系统消息
                    .receiverId(creatorId)
                    .title("项目创建成功")
                    .content(String.format("您的项目「%s」已创建成功,开始您的项目管理之旅吧!", project.getName()))
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

            String statusChangeDesc = getStatusChangeDescription(oldStatus, newStatus);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_STATUS_CHANGED")
                    .senderId(null)
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
    public void sendProjectDeletedNotification(Project project, List<Long> projectMemberIds) {
        try {
            if (projectMemberIds.isEmpty()) {
                log.warn("你通知个击败，一个人没有");
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_DELETED")
                    .senderId(null)
                    .receiverIds(projectMemberIds)
                    .title("项目已删除")
                    .content(String.format("项目「%s」已被删除,相关数据已归档", project.getName()))
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
    public void sendProjectArchivedNotification(Project project, List<Long> projectMemberIds) {
        try{
            if (projectMemberIds.isEmpty()) {
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_ARCHIVED")
                    .senderId(null)
                    .receiverIds(projectMemberIds)
                    .title("项目已归档")
                    .content(String.format("项目「%s」已归档,您可以在归档列表中查看", project.getName()))
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
