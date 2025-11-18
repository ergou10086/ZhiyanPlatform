package hbnu.project.zhiyanmessgae.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发送消息请求DTO
 * 用于项目模块向消息模块发送消息
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequestDTO {

    /**
     * 消息场景(必填)
     * PROJECT_CREATED - 项目创建
     * PROJECT_STATUS_CHANGED - 项目状态变更
     * PROJECT_DELETED - 项目删除
     * PROJECT_ARCHIVED - 项目归档
     * PROJECT_MEMBER_INVITED - 成员被邀请
     * PROJECT_MEMBER_JOINED - 新成员加入
     * PROJECT_MEMBER_REMOVED - 成员被移除
     * PROJECT_MEMBER_LEFT - 成员离开
     * PROJECT_ROLE_CHANGED - 角色变更
     */
    private String scene;

    /**
     * 发送人ID(可为null表示系统消息)
     */
    private Long senderId;

    /**
     * 单个收件人ID(用于个人消息)
     */
    private Long receiverId;

    /**
     * 多个收件人ID列表(用于批量消息)
     */
    private List<Long> receiverIds;

    /**
     * 消息标题(必填)
     */
    private String title;

    /**
     * 消息内容(必填)
     */
    private String content;

    /**
     * 业务关联ID(如项目ID)
     */
    private Long businessId;

    /**
     * 业务类型(如"PROJECT")
     */
    private String businessType;

    /**
     * 扩展数据(JSON格式)
     * 如: {"projectId":123,"projectName":"项目名称","jumpUrl":"/projects/123"}
     */
    private String extendData;
}
