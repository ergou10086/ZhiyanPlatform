package hbnu.project.zhiyanaicoze.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * Coze Chat 请求 DTO
 * 对应 Coze API v3/chat 接口
 *
 * @author ErgouTree
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CozeChatRequest {

    /**
     * 对话 ID（可选，用于维持会话）
     * 标识对话发生在哪一次会话中。
     * 会话是 Bot 和用户之间的一段问答交互。一个会话包含一条或多条消息。对话是会话中对 Bot 的一次调用，Bot 会将对话中产生的消息添加到会话中。
     *
     *     可以使用已创建的会话，会话中已存在的消息将作为上下文传递给模型。创建会话的方式可参考创建会话。
     *     对于一问一答等不需要区分 conversation 的场合可不传该参数，系统会自动生成一个会话
     *
     *
     * 一个会话中，只能有一个进行中的对话，否则调用此接口时会报错 4016。
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 智能体ID（必需）
     * 要进行会话聊天的智能体 ID。
     * 进入智能体的 开发页面，开发页面 URL 中 bot 参数后的数字就是智能体 ID。
     * 例如https://www.coze.cn/space/341xxxx/bot/73428668，智能体 ID 为73428668
     * 确保当前使用的访问密钥已被授予智能体所属空间的 chat 权限。
     */
    @JsonProperty("bot_id")
    @NotBlank(message = "智能体ID不能为空")
    private String botId;

    /**
     * 用户ID（必需）
     * 标识当前与智能体对话的用户，由使用方自行定义、生成与维护。user_id 用于标识对话中的不同用户，不同的 user_id，其对话的上下文消息、数据库等对话记忆数据互相隔离。如果不需要用户数据隔离，可将此参数固定为一个任意字符串，例如 123，abc 等。
     */
    @JsonProperty("user_id")
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 是否启用流式响应
     *
     *     true：采用流式响应。 “流式响应” 将模型的实时响应提供给客户端，类似打字机效果。你可以实时获取服务端返回的对话、消息事件，并在客户端中同步处理、实时展示，也可以直接在 completed 事件中获取智能体最终的回复。
     *     false：（默认）采用非流式响应。 “非流式响应” 是指响应中仅包含本次对话的状态等元数据。此时应同时开启 auto_save_history，在本次对话处理结束后再查看模型回复等完整响应内容。可以参考以下业务流程：
     *
     *
     * a. 调用发起会话接口，并设置 stream = false，auto_save_history=true，表示使用非流式响应，并记录历史消息。
     * 你需要记录会话的 Conversation ID 和 Chat ID，用于后续查看详细信息。
     * b. 定期轮询查看对话详情接口，建议每次间隔 1 秒以上，直到会话状态流转为终态，即 status 为 completed、required_action、canceled 或 failed。
     * c. 调用查看对话消息详情接口，查询大模型生成的最终结果。
     */
    @Builder.Default
    private Boolean stream = true;


    /**
     * 附加消息列表
     * 对话的附加信息。你可以通过此字段传入历史消息和本次对话中用户的问题。数组长度限制为 100，即最多传入 100 条消息。
     *
     *     若未设置 additional_messages，智能体收到的消息只有会话中已有的消息内容，其中最后一条作为本次对话的用户输入，其他内容均为本次对话的上下文。
     *     若设置了 additional_messages，智能体收到的消息包括会话中已有的消息和 additional_messages 中添加的消息，其中 additional_messages 最后一条消息会作为本次对话的用户输入，其他内容均为本次对话的上下文。
     *
     *
     * 消息结构可参考EnterMessage Object，具体示例可参考携带上下文。
     *
     * 会话或 additional_messages 中最后一条消息应为 role=user 的记录，以免影响模型效果。
     * 如果本次对话未指定会话或指定的会话中无消息时，必须通过此参数传入智能体用户的问题。
     */
    @JsonProperty("additional_messages")
    private List<CozeMessage> additionalMessages;


    /**
     * 自定义变量
     * 智能体中定义的变量。在智能体 prompt 中设置变量 {{key}} 后，可以通过该参数传入变量值，同时支持 Jinja2 语法。详细说明可参考变量示例。
     *
     * 变量名只支持英文字母和下划线。
     */
    @JsonProperty("custom_variables")
    private Map<String, String> customVariables;


    /**
     * 是否保存本次对话记录。
     *
     *     true：（默认）会话中保存本次对话记录，包括 additional_messages 中指定的所有消息、本次对话的模型回复结果、模型执行中间结果。
     *     false：会话中不保存本次对话记录，后续也无法通过任何方式查看本次对话信息、消息详情。在同一个会话中再次发起对话时，本次会话也不会作为上下文传递给模型。
     *     非流式响应下（stream=false），此参数必须设置为 true，即保存本次对话记录，否则无法查看对话状态和模型回复。
     *     调用端插件时，此参数必须设置为 true，即保存本次对话记录，否则提交工具执行结果时会提示 5000 错误，端插件的详细 API 使用示例请参见通过 API 使用端插件。
     */
    @JsonProperty("auto_save_history")
    @Builder.Default
    private boolean autoSaveHistory = true;


    /**
     * 附加信息
     * 通常用于封装一些业务相关的字段。查看对话详情时，扣子会透传此附加信息，查看消息列表时不会返回该附加信息。
     * 自定义键值对，应指定为 Map 对象格式。长度为 16 对键值对，其中键（key）的长度范围为 1～64 个字符，值（value）的长度范围为 1～512 个字符。
     */
    @JsonProperty("meta_data")
    private Map<String, String> metaData;


    /**
     * 附加参数
     * 附加参数，通常用于特殊场景下指定一些必要参数供模型判断，例如指定经纬度，并询问智能体此位置的天气。
     * 自定义键值对格式，其中键（key）仅支持设置为：
     *
     *     latitude：纬度，此时值（Value）为纬度值，例如 39.9800718。
     *     longitude：经度，此时值（Value）为经度值，例如 116.309314。
     */
    @JsonProperty("extra_params")
    private Map<String, Object> extraParams;



    /**
     * Coze 消息对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CozeMessage {
        /**
         * 消息角色：user, assistant, system
         */
        private String role;

        /**
         * 消息内容
         * - 当 content_type="text" 时，content 应该是 String
         * - 当 content_type="file" 时，content 应该是 FileContent
         */
        private Object content;

        /**
         * 消息类型：text, image, file 等
         */
        @JsonProperty("content_type")
        @Builder.Default
        private String contentType = "text";

        /**
         * 文件ID列表（用于在文本消息中附加文件）
         */
        @JsonProperty("file_ids")
        private List<String> fileIds;
    }

    /**
     * 文件消息的content对象（简化版，用于消息中）
     * 符合 Coze API v3 文件消息格式
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FileContent {
        /**
         * 文件 ID（必需）
         */
        private String id;

        /**
         * 对象类型（固定为 "file"）
         */
        @Builder.Default
        private String object = "file";

        /**
         * 文件名
         */
        private String filename;

        /**
         * 用途（固定为 "assistants"）
         */
        @Builder.Default
        private String purpose = "assistants";
    }
}
