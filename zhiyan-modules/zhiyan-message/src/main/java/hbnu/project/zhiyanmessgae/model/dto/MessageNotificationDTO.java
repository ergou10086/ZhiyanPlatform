package hbnu.project.zhiyanmessgae.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SSE消息通知DTO
 *
 * @author ErgouTree
 */
@Data
public class MessageNotificationDTO {
    private Long messageId;
    private String title;
    private String content;
    private String scene;
    private String priority;
    private Long businessId;
    private String businessType;
    private String extendData;
    private LocalDateTime triggerTime;
}
