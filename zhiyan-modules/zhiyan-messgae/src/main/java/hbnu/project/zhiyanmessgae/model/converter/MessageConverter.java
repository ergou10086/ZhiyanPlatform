package hbnu.project.zhiyanmessgae.model.converter;

import hbnu.project.zhiyanmessgae.model.dto.MessageListDTO;
import hbnu.project.zhiyanmessgae.model.dto.MessageNotificationDTO;
import hbnu.project.zhiyanmessgae.model.entity.MessageBody;
import hbnu.project.zhiyanmessgae.model.entity.MessageRecipient;
import io.github.linpeilie.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 消息实体转换器 - 使用 MapStruct-Plus
 *
 * @author ErgouTree
 */
@Mapper(componentModel = "spring")
public interface MessageConverter extends BaseMapper<MessageBody, MessageNotificationDTO> {

    MessageConverter INSTANCE = Mappers.getMapper(MessageConverter.class);

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
    MessageNotificationDTO toNotificationDTO(MessageBody messageBody);

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
    MessageListDTO toListDTO(MessageRecipient messageRecipient);

    /**
     * 批量转换 MessageRecipient 列表
     */
    List<MessageListDTO> toListDTOs(List<MessageRecipient> messageRecipients);

    /**
     * 处理枚举类型转换
     * 将枚举转换为字符串
     */
    default String mapEnum(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : null;
    }
}
