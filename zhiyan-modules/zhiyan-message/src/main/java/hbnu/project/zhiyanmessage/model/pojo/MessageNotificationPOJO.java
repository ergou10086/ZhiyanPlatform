package hbnu.project.zhiyanmessage.model.pojo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SSE消息通知DTO
 *
 * @author ErgouTree
 */
@Getter
@Setter
public class MessageNotificationPOJO {
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
