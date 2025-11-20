package hbnu.project.zhiyanproject.utils.message;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanmessgae.model.dto.SendMessageRequestDTO;

import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.client.MessageServiceClient;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目成员相关工具类
 * 避免在serviceimpl中大量编写发送消息的方法
 * 把消息发送的具体内容集中在这里
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectMemberMessageUtils {

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
                log.warn("获取用户[{}]信息失败: {}", userId, result.getMsg());
                return "未知用户";
            }
        } catch (Exception e) {
            log.error("获取用户[{}]信息异常", userId, e);
            return "未知用户";
        }
    }

    /**
     * 发送成员邀请的通知
     */
    public void sendMessageInvitedNotification(Project project, ProjectMember member, Long inviterId){
        try{
            // 获取邀请者姓名
            String inviterName = getUserNameById(inviterId);

            SendMessageRequestDTO sendMessageRequestDTO = SendMessageRequestDTO.builder()
                    .scene("PROJECT_MEMBER_INVITED")
                    .senderId(inviterId)
                    .receiverId(member.getUserId())
                    .title(String.format("项目%s邀请成员", project.getName()))
                    .content(String.format("您已被「%s」邀请加入项目「%s」，作为角色「%s」，希望您能为项目贡献出一份力",
                            inviterName,
                            project.getName(),
                            member.getProjectRole()))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project, member))
                    .build();
            R<Void> result = messageServiceClient.sendPersonalMessage(sendMessageRequestDTO);
            if(!R.isSuccess(result)){
                log.warn("发送成员邀请通知失败: projectId={}, userId={}, error={}", project.getId(), member.getUserId(), result.getMsg());
            }
        }catch (Exception e){
            log.error("发送成员邀请通知异常: projectId={}, userId={}", project.getId(), member.getUserId(), e);
        }
    }


    /**
     * 通知其他成员有新成员加入
     */
    public void sendNewMemberJoinedNotification(Project project, ProjectMember newMember,
                                                Long inviterId, List<Long> existingMemberIds) {
        try{
            if(existingMemberIds.isEmpty()){
                log.warn("你邀请啥了");
                return;
            }

            // 获取邀请者姓名
            String inviterName = getUserNameById(inviterId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_MEMBER_JOINED")
                    .senderId(inviterId)
                    .receiverIds(existingMemberIds)
                    .title(String.format("项目%s新成员加入", project.getName()))
                    .content(String.format("项目「%s」新增成员，角色为「%s」，由「%s」邀请加入，欢迎",
                            project.getName(),
                            newMember.getProjectRole(),
                            inviterName))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project, newMember))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送新成员加入通知失败: projectId={}, error={}",
                        project.getId(), result.getMsg());
            }
        }catch (Exception e){
            log.error("发送新成员加入通知异常: projectId={}", project.getId(), e);
        }
    }


    /**
     * 发送成员被移除通知
     */
    public void sendMemberRemovedNotification(Project project, Long removedUserId, Long operatorId) {
        try{
            // 获取操作者姓名
            String operatorName = getUserNameById(operatorId);

            SendMessageRequestDTO sendMessageRequestDTO = SendMessageRequestDTO.builder()
                    .scene("PROJECT_MEMBER_REMOVED")
                    .senderId(operatorId)  // 操作者是发送者
                    .receiverId(removedUserId)  // 被移除的用户是接收者
                    .title(String.format("项目%s成员移除", project.getName()))
                    .content(String.format("您已被「%s」移出项目「%s」，感谢你对项目的贡献", operatorName, project.getName()))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project, null))
                    .build();


            R<Void> result = messageServiceClient.sendPersonalMessage(sendMessageRequestDTO);
            if (!R.isSuccess(result)) {
                log.warn("发送成员移除通知失败: projectId={}, userId={}, error={}",
                        project.getId(), removedUserId, result.getMsg());
            }
        }catch (Exception e){
            log.error("发送成员移除通知异常: projectId={}, userId={}", project.getId(), removedUserId, e);
        }
    }


    /**
     * 通知其他成员有成员离组
     */
    public void sendMemberLeftNotification(Project project, ProjectMember leftMember, Long operatorId, List<Long> adminIds) {
        try{
            if(adminIds.isEmpty()){
                log.warn("66啥都没有就通知");
                return;
            }

            String action = operatorId.equals(leftMember.getUserId()) ? "主动退出" : "被移出";
            // 获取操作者姓名
            String operatorName = getUserNameById(operatorId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_MEMBER_REMOVED")
                    .senderId(operatorId)
                    .receiverIds(adminIds)
                    .title("成员离组通知")
                    .content(String.format("项目「%s」有成员%s离开项目，操作者「%s」，感谢他做出的贡献",
                            project.getName(), action, operatorName))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project, leftMember))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送成员离开通知失败: projectId={}, error={}",
                        project.getId(), result.getMsg());
            }
        }catch (Exception e) {
            log.error("发送成员离开通知异常: projectId={}", project.getId(), e);
        }
    }


    /**
     * 发送成员角色变更通知给成员本人
     */
    public void sendMemberRoleChangedNotification(Project project, ProjectMember member,
                                                  String oldRole,
                                                  String newRole,
                                                  Long operatorId) {
        try {
            // 获取操作者姓名
            String operatorName = getUserNameById(operatorId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_ROLE_CHANGED")
                    .senderId(operatorId)
                    .receiverId(member.getUserId())
                    .title("角色变更通知")
                    .content(String.format("您在项目「%s」中的角色已从「%s」变更为「%s」，操作者「%s」",
                            project.getName(),
                            oldRole,
                            newRole,
                            operatorName))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project, member))
                    .build();

            R<Void> result = messageServiceClient.sendPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送角色变更通知给成员失败: projectId={}, userId={}, error={}",
                        project.getId(), member.getUserId(), result.getMsg());
            }
        }catch (Exception e){
            log.error("发送角色变更通知给成员异常: projectId={}, userId={}", project.getId(), member.getUserId(), e);
        }
    }


    /**
     * 发送成员角色变更通知给项目管理员
     */
    public void sendMemberRoleChangedNotificationToAdmins(Project project, ProjectMember member, String oldRole, String newRole, List<Long> adminIds){
        try {
            if (adminIds.isEmpty()) {
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("PROJECT_ROLE_CHANGED_ADMIN")
                    .senderId(null)
                    .receiverIds(adminIds)
                    .title("成员角色变更")
                    .content(String.format("项目「%s」中成员的角色已从「%s」变更为「%s」",
                            project.getName(),
                            oldRole,
                            newRole))
                    .businessId(project.getId())
                    .businessType("PROJECT")
                    .extendData(buildProjectExtendData(project, member))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送角色变更通知给管理员失败: projectId={}, error={}",
                        project.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送角色变更通知给管理员异常: projectId={}", project.getId(), e);
        }
    }


    /**
     * 构建项目扩展数据
     */
    private String buildProjectExtendData(Project project, ProjectMember member) {
        try {
            if (member != null) {
                return String.format(
                        "{\"projectId\":%d,\"projectName\":\"%s\",\"userId\":%d,\"role\":\"%s\",\"roleName\":\"%s\",\"jumpUrl\":\"/projects/%d\"}",
                        project.getId(),
                        escapeJsonString(project.getName()),
                        member.getUserId(),
                        member.getProjectRole().name(),  // 角色代码
                        escapeJsonString(member.getProjectRole().getRoleName()),  // 角色名称
                        project.getId());
            } else {
                return String.format(
                        "{\"projectId\":%d,\"projectName\":\"%s\",\"jumpUrl\":\"/projects/%d\"}",
                        project.getId(),
                        escapeJsonString(project.getName()),
                        project.getId());
            }
        } catch (Exception e) {
            log.error("构建项目扩展数据异常", e);
            return "{}";
        }
    }


    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}