package hbnu.project.zhiyanmessage.model.enums;

/**
 * 消息类型枚举
 *
 * @author ErgouTree
 */
public enum MessageType {
    /**
     * 个人消息 - 发送给特定用户
     */
    PERSONAL,

    /**
     * 群组消息 - 发送给群组内所有成员
     */
    GROUP,

    /**
     * 广播消息 - 发送给平台所有用户
     */
    BROADCAST
}
