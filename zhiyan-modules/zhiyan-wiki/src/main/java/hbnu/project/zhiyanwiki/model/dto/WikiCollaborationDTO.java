package hbnu.project.zhiyanwiki.model.dto;

import lombok.*;

import java.time.LocalDateTime;


/**
 * 协同编辑DTO
 *
 * @author ErgouTree
 */
public class WikiCollaborationDTO {

    /**
     * 编辑位置信息
     */
    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursorPosition {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 用户头像URL
         */
        private String avatar;

        /**
         * 光标位置（行号，从0开始）
         */
        private Integer line;

        /**
         * 光标位置（列号，从0开始）
         */
        private Integer column;

        /**
         * 选中的文本范围（开始位置）
         */
        private Integer selectionStart;

        /**
         * 选中的文本范围（结束位置）
         */
        private Integer selectionEnd;

        /**
         * 最后更新时间
         */
        private LocalDateTime lastUpdate;

        /**
         * 段落或区域标识（可选，用于标记编辑的段落）
         */
        private String paragraphId;
    }

    /**
     * 在线编辑者信息
     */
    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EditorInfo {
        private Long userId;
        private String username;
        private String avatar;
        private LocalDateTime joinTime;
    }

    /**
     * 编辑内容变更消息
     */
    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentChange {
        private Long pageId;
        private Long userId;
        private String content;
        private Integer version;
        private LocalDateTime timestamp;
    }


    /**
     * 增量编辑操作（用于实时同步）
     */
    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncrementalChange {
        /**
         * 操作类型：INSERT、DELETE、REPLACE
         */
        private String operation;
        /**
         * 操作位置（字符位置）
         */
        private Integer position;
        /**
         * 插入的文本
         */
        private String text;
        /**
         * 删除的文本长度
         */
        private Integer length;
        /**
         * 用户ID
         */
        private Long userId;
        /**
         * 时间戳
         */
        private LocalDateTime timestamp;
    }


    /**
     * 编辑状态同步消息
     */
    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncMessage {
        /**
         * 消息类型：JOIN、LEAVE、CURSOR_UPDATE、CONTENT_CHANGE
         */
        private String type;
        /**
         * 页面ID
         */
        private Long pageId;
        /**
         * 用户ID
         */
        private Long userId;
        /**
         * 消息数据
         */
        private Object data;
        /**
         * 时间戳
         */
        private LocalDateTime timestamp;
    }
}
