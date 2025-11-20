package hbnu.project.zhiyanknowledge.message;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.client.AuthServiceClient;
import hbnu.project.zhiyanknowledge.client.MessageServiceClient;
import hbnu.project.zhiyanknowledge.client.ProjectServiceClient;
import hbnu.project.zhiyanknowledge.model.dto.UserDTO;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.entity.AchievementFile;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanmessgae.model.dto.SendMessageRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库部分的消息服务
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeMessageService {

    private final MessageServiceClient messageServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final AuthServiceClient authServiceClient;

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
     * 根据用户ID列表批量获取用户名映射
     *
     * @param userIds 用户ID列表
     * @return 用户ID到用户名的映射
     */
    public Map<Long, String> getUserNamesByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        try{
            R<List<UserDTO>> result = authServiceClient.getUsersByIds(userIds);
            if(R.isSuccess(result) && result.getData() != null) {
                Map<Long, String> userMap = result.getData().stream()
                        .filter(user -> user.getId() != null && user.getName() != null)
                        .collect(Collectors.toMap(
                                UserDTO::getId,
                                UserDTO::getName,
                                (existing, replacement) -> existing
                        ));
                log.info("批量获取用户信息成功，共{}个用户", userMap.size());
                return userMap;
            }else{
                log.warn("批量获取用户信息失败: {}", result.getMsg());
                return Map.of();
            }
        }catch (Exception e){
            log.error("批量获取用户信息异常", e);
            return Map.of();
        }
    }

    /**
     * 根据项目ID获取项目成员的用户名映射
     *
     * @param projectId 项目ID
     * @return 用户ID到用户名的映射
     */
    public Map<Long, String> getProjectMemberUserNames(Long projectId) {
        List<Long> userIds = getProjectMemberUserIds(projectId);
        if (userIds.isEmpty()) {
            log.warn("项目[{}]没有成员", projectId);
            return Map.of();
        }
        return getUserNamesByIds(userIds);
    }

    // —————————————————————————————————通知方法———————————————————————————————————————

    /**
     * 成果状态的变更通知
     */
    public void notifyAchievementStatusChange(Achievement achievement, AchievementStatus oldStatus, AchievementStatus newStatus, Long operatorId){
        if(achievement == null || oldStatus == null || newStatus == null){
            log.warn("啥都没有就发送？？");
            return;
        }

        List<Long> receiverIds = getProjectMemberUserIds(achievement.getProjectId());

        if (receiverIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送通知，没有人就硬发", achievement.getProjectId());
            return;
        }

        // 过滤操作者自己
        List<Long> filteredReceiverIds = receiverIds.stream()
                .filter(memberId -> !memberId.equals(operatorId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.info("过滤后没有接收者，跳过发送");
            return;
        }

        // 获取操作者姓名
        String operatorName = getUserNameById(operatorId);

        SendMessageRequestDTO requestDTO = SendMessageRequestDTO.builder()
                .scene("ACHIEVEMENT_STATUS_CHANGED")
                .senderId(operatorId)
                .receiverIds(filteredReceiverIds)
                .title("成果状态变更")
                .content(String.format("成果「%s」状态已从「%s」变更为「%s」，操作者「%s」，请及时查看",
                        achievement.getTitle(), formatStatus(oldStatus), formatStatus(newStatus), operatorName))
                .businessId(achievement.getId())
                .businessType("ACHIEVEMENT")
                .extendData(buildExtendData(achievement))
                .build();

        sendBatchMessage(requestDTO, achievement.getId());
    }

    /**
     * 发送成果文件上传的通知
     * 发送给除了上传者的所有人
     */
    public void notifyAchievementFileUpload(Achievement achievement, AchievementFile file, Long uploaderId) {
        if (achievement == null || file == null) {
            log.warn("文件上传通知参数不完整");
            return;
        }

        List<Long> projectMemberIds = getProjectMemberUserIds(achievement.getProjectId());

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送通知，难道发给pxl？", achievement.getProjectId());
            return;
        }

        // 过滤上传者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(receiverId -> !receiverId.equals(uploaderId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.warn("都不知道要发谁就发送？");
            return;
        }

        // 获取上传者姓名
        String uploaderName = getUserNameById(uploaderId);
        // 格式化文件大小
        String fileSizeStr = formatFileSize(file.getFileSize());

        SendMessageRequestDTO requestDTO = SendMessageRequestDTO.builder()
                .scene("ACHIEVEMENT_FILE_UPLOADED")
                .senderId(uploaderId)
                .receiverIds(filteredReceiverIds)
                .title("成果文件上传")
                .content(String.format("成果「%s」有新文件上传\n文件名：「%s」\n文件大小：%s\n上传者：「%s」\n该成果已更新，请及时查看",
                        achievement.getTitle(), file.getFileName(), fileSizeStr, uploaderName))
                .businessId(achievement.getId())
                .businessType("ACHIEVEMENT")
                .extendData(buildFileUploadExtendData(achievement, file))
                .build();

        sendBatchMessage(requestDTO, achievement.getId());
    }

    /**
     * 发送成果创建的通知
     */
    public void notifyAchievementCreated(Achievement achievement){
        if (achievement == null) {
            log.warn("成果创建通知参数不完整");
            return;
        }

        List<Long> projectMemberIds = getProjectMemberUserIds(achievement.getProjectId());

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送通知", achievement.getProjectId());
            return;
        }

        // 过滤掉创建者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(memberId -> !memberId.equals(achievement.getCreatorId()))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.info("过滤后没有接收者，跳过发送");
            return;
        }

        // 获取创建者姓名
        String creatorName = getUserNameById(achievement.getCreatorId());

        SendMessageRequestDTO requestDTO = SendMessageRequestDTO.builder()
                .scene("ACHIEVEMENT_CREATED")
                .senderId(achievement.getCreatorId())
                .receiverIds(filteredReceiverIds)
                .title("新成果创建")
                .content(String.format("项目中有新成果「%s」创建\n成果类型：%s\n创建者：「%s」\n请及时查看",
                        achievement.getTitle(),
                        achievement.getType() != null ? achievement.getType().getName() : "未知类型",
                        creatorName))
                .businessId(achievement.getId())
                .businessType("ACHIEVEMENT")
                .extendData(buildExtendData(achievement))
                .build();

        sendBatchMessage(requestDTO, achievement.getId());
    }


    /**
     * 成果删除通知
     */
    public void notifyAchievementDeleted(Achievement achievement, Long operatorId) {
        if (achievement == null) {
            log.warn("成果删除通知参数不完整,没有成果通知鸡毛");
            return;
        }

        List<Long> projectMemberIds = getProjectMemberUserIds(achievement.getProjectId());

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送通知，发给滚木？", achievement.getProjectId());
            return;
        }

        // 过滤掉操作者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(memberId -> !memberId.equals(operatorId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.info("过滤后没有接收者，跳过发送");
            return;
        }

        // 获取操作者姓名
        String operatorName = getUserNameById(operatorId);

        SendMessageRequestDTO requestDTO = SendMessageRequestDTO.builder()
                .scene("ACHIEVEMENT_DELETED")
                .senderId(operatorId)
                .receiverIds(filteredReceiverIds)
                .title("成果删除")
                .content(String.format("成果「%s」已被删除\n成果类型：%s\n操作者：「%s」\n请知悉",
                        achievement.getTitle(),
                        achievement.getType() != null ? achievement.getType().getName() : "未知类型",
                        operatorName))
                .businessId(achievement.getId())
                .businessType("ACHIEVEMENT")
                .extendData(buildDeletedExtendData(achievement))
                .build();

        sendBatchMessage(requestDTO, achievement.getId());
    }

    /**
     * 发送成果文件被删除的通知
     */
    public void notifyAchievementFileDeleted(Achievement achievement, AchievementFile achievementFile, Long operatorId) {
        if (achievement == null || achievementFile == null) {
            log.warn("成果文件删除通知参数不完整");
            return;
        }

        List<Long> projectMemberIds = getProjectMemberUserIds(achievement.getProjectId());

        if (projectMemberIds.isEmpty()) {
            log.warn("项目[{}]没有成员，无法发送通知", achievement.getProjectId());
            return;
        }

        // 过滤掉操作者自己
        List<Long> filteredReceiverIds = projectMemberIds.stream()
                .filter(memberId -> !memberId.equals(operatorId))
                .toList();

        if (filteredReceiverIds.isEmpty()) {
            log.info("过滤后没有接收者，跳过发送");
            return;
        }

        // 获取操作者姓名
        String operatorName = getUserNameById(operatorId);
        // 格式化文件大小
        String fileSizeStr = formatFileSize(achievementFile.getFileSize());

        SendMessageRequestDTO requestDTO = SendMessageRequestDTO.builder()
                .scene("ACHIEVEMENT_FILE_DELETED")
                .senderId(operatorId)
                .receiverIds(filteredReceiverIds)
                .title("成果文件删除")
                .content(String.format("成果「%s」的文件已被删除\n文件名：「%s」\n文件大小：%s\n文件类型：%s\n操作者：「%s」\n请知悉",
                        achievement.getTitle(),
                        achievementFile.getFileName(),
                        fileSizeStr,
                        achievementFile.getFileType() != null ? achievementFile.getFileType() : "未知",
                        operatorName))
                .businessId(achievement.getId())
                .businessType("ACHIEVEMENT")
                .extendData(buildFileDeletedExtendData(achievement, achievementFile))
                .build();

        sendBatchMessage(requestDTO, achievement.getId());
    }

    private void sendPersonalMessage(SendMessageRequestDTO request, Long achievementId) {
        try {
            R<Void> resp = messageServiceClient.sendPersonalMessage(request);
            if (!R.isSuccess(resp)) {
                log.warn("发送成果消息失败: achievementId={}, error={}", achievementId, resp.getMsg());
            } else {
                log.info("成果消息发送成功: achievementId={}, scene={}", achievementId, request.getScene());
            }
        } catch (Exception ex) {
            log.error("调用消息服务异常: achievementId={}", achievementId, ex);
        }
    }


    private void sendBatchMessage(SendMessageRequestDTO request, Long achievementId) {
        try {
            R<Void> resp = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(resp)) {
                log.warn("发送批量成果消息失败: achievementId={}, error={}", achievementId, resp.getMsg());
            } else {
                log.info("批量成果消息发送成功: achievementId={}, scene={}, receiverCount={}",
                        achievementId, request.getScene(),
                        request.getReceiverIds() != null ? request.getReceiverIds().size() : 0);
            }
        } catch (Exception ex) {
            log.error("调用批量消息服务异常: achievementId={}", achievementId, ex);
        }
    }


    private String buildExtendData(Achievement achievement) {
        return String.format(
                "{\"achievementId\":%d,\"projectId\":%d,\"status\":\"%s\",\"title\":\"%s\",\"type\":\"%s\",\"isPublic\":%s,\"jumpUrl\":\"/project-knowledge/%d\"}",
                achievement.getId(),
                achievement.getProjectId(),
                achievement.getStatus().name(),
                escapeJson(achievement.getTitle()),
                achievement.getType() != null ? achievement.getType().name() : "UNKNOWN",
                Boolean.TRUE.equals(achievement.getIsPublic()),
                achievement.getProjectId());
    }

    private String buildFileUploadExtendData(Achievement achievement, AchievementFile file) {
        return String.format(
                "{\"achievementId\":%d,\"projectId\":%d,\"fileId\":%d,\"title\":\"%s\",\"fileName\":\"%s\",\"fileSize\":%d,\"fileType\":\"%s\",\"jumpUrl\":\"/project-knowledge/%d/files/%d\"}",
                achievement.getId(),
                achievement.getProjectId(),
                file.getId(),
                escapeJson(achievement.getTitle()),
                escapeJson(file.getFileName()),
                file.getFileSize(),
                file.getFileType() != null ? file.getFileType() : "",
                achievement.getProjectId(),
                file.getId());
    }

    private String buildDeletedExtendData(Achievement achievement) {
        return String.format(
                "{\"achievementId\":%d,\"projectId\":%d,\"title\":\"%s\",\"type\":\"%s\",\"isPublic\":%s}",
                achievement.getId(),
                achievement.getProjectId(),
                escapeJson(achievement.getTitle()),
                achievement.getType() != null ? achievement.getType().name() : "UNKNOWN",
                Boolean.TRUE.equals(achievement.getIsPublic()));
    }

    private String buildFileDeletedExtendData(Achievement achievement, AchievementFile file) {
        return String.format(
                "{\"achievementId\":%d,\"projectId\":%d,\"fileId\":%d,\"title\":\"%s\",\"fileName\":\"%s\",\"fileSize\":%d,\"fileType\":\"%s\"}",
                achievement.getId(),
                achievement.getProjectId(),
                file.getId(),
                escapeJson(achievement.getTitle()),
                escapeJson(file.getFileName()),
                file.getFileSize(),
                file.getFileType() != null ? file.getFileType() : "");
    }

    private String escapeJson(String raw) {
        return raw == null ? "" : raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String formatStatus(AchievementStatus status) {
        return switch (status) {
            case draft -> "草稿";
            case under_review -> "审核中";
            case published -> "已发布";
            case obsolete -> "已归档";
        };
    }

    /**
     * 格式化文件大小为可读格式
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }

        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
