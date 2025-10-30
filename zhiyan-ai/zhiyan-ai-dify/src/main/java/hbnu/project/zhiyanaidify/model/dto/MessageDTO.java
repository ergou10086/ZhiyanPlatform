package hbnu.project.zhiyanai.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 消息信息 DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDTO {

    /**
     * 消息 ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 会话 ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 输入参数
     */
    @JsonProperty("inputs")
    private Map<String, Object> inputs;

    /**
     * 用户查询
     */
    @JsonProperty("query")
    private String query;

    /**
     * AI 回答内容
     */
    @JsonProperty("answer")
    private String answer;

    /**
     * 消息文件列表
     */
    @JsonProperty("message_files")
    private List<MessageFile> messageFiles;

    /**
     * 反馈信息
     */
    @JsonProperty("feedback")
    private Feedback feedback;

    /**
     * 检索器资源列表
     */
    @JsonProperty("retriever_resources")
    private List<RetrieverResource> retrieverResources;

    /**
     * 创建时间（时间戳）
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 消息文件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MessageFile {
        /**
         * 文件 ID
         */
        @JsonProperty("id")
        private String id;

        /**
         * 文件类型（image/file）
         */
        @JsonProperty("type")
        private String type;

        /**
         * 文件 URL
         */
        @JsonProperty("url")
        private String url;

        /**
         * 归属方（user/assistant）
         */
        @JsonProperty("belongs_to")
        private String belongsTo;
    }

    /**
     * 反馈信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Feedback {
        /**
         * 评分（like/dislike）
         */
        @JsonProperty("rating")
        private String rating;
    }

    /**
     * 检索器资源
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RetrieverResource {
        /**
         * 位置
         */
        @JsonProperty("position")
        private Integer position;

        /**
         * 数据集 ID
         */
        @JsonProperty("dataset_id")
        private String datasetId;

        /**
         * 数据集名称
         */
        @JsonProperty("dataset_name")
        private String datasetName;

        /**
         * 文档 ID
         */
        @JsonProperty("document_id")
        private String documentId;

        /**
         * 文档名称
         */
        @JsonProperty("document_name")
        private String documentName;

        /**
         * 段落 ID
         */
        @JsonProperty("segment_id")
        private String segmentId;

        /**
         * 相似度分数
         */
        @JsonProperty("score")
        private Double score;

        /**
         * 内容
         */
        @JsonProperty("content")
        private String content;
    }
}