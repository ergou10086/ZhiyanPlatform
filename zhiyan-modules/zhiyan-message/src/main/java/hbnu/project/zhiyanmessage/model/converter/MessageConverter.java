package hbnu.project.zhiyanmessage.model.converter;


import hbnu.project.zhiyanmessage.model.entity.Message;
import hbnu.project.zhiyanmessage.model.entity.MessageReception;
import hbnu.project.zhiyanmessage.model.pojo.MessageListPOJO;
import hbnu.project.zhiyanmessage.model.pojo.MessageNotificationPOJO;
import io.github.linpeilie.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 消息实体转换器 - 使用 MapStruct-Plus
 *
 * @author ErgouTree
 */
@Mapper(componentModel = "spring")
public interface MessageConverter extends BaseMapper<Message, MessageNotificationPOJO> {

    /**
     * MessageBody 转 MessageNotificationDTO
     * 用于SSE推送
     */
    @Mappings({
            @Mapping(source = "id", target = "messageId"),
            @Mapping(source = "scene", target = "scene"),
            @Mapping(source = "priority", target = "priority"),
            @Mapping(source = "title", target = "title"),
            @Mapping(source = "content", target = "content"),
            @Mapping(source = "businessId", target = "businessId"),
            @Mapping(source = "businessType", target = "businessType"),
            @Mapping(source = "extendData", target = "extendData"),
            @Mapping(source = "triggerTime", target = "triggerTime")
    })
    MessageNotificationPOJO toNotificationDTO(Message messageBody);

    /**
     * MessageRecipient 转 MessageListDTO
     * 用于列表展示
     */
    @Mappings({
            @Mapping(source = "id", target = "recipientId"),
            @Mapping(source = "messageBody.id", target = "messageId"),
            @Mapping(source = "messageBody.senderId", target = "senderId"),
            @Mapping(source = "sceneCode", target = "scene"),
            @Mapping(source = "messageBody.priority", target = "priority"),
            @Mapping(source = "messageBody.title", target = "title"),
            @Mapping(source = "messageBody.content", target = "content"),
            @Mapping(source = "messageBody.businessId", target = "businessId"),
            @Mapping(source = "messageBody.businessType", target = "businessType"),
            @Mapping(source = "messageBody.extendData", target = "extendData"),
            @Mapping(source = "readFlag", target = "readFlag"),
            @Mapping(source = "readAt", target = "readAt"),
            @Mapping(source = "triggerTime", target = "triggerTime")
    })
    MessageListPOJO toListDTO(MessageReception messageRecipient);

    /**
     * 批量转换 MessageRecipient 列表
     */
    List<MessageListPOJO> toListDTOs(List<MessageReception> messageRecipients);

    /**
     * 处理枚举类型转换
     * 将枚举转换为字符串
     */
    default String mapEnum(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : null;
    }
}
