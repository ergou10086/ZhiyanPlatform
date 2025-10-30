package hbnu.project.zhiyanaidify.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 工作流对话请求 DTO
 * 用于调用 Dify 工作流
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // ✅ 忽略 null 字段
public class WorkflowChatRequest {

    /**
     * 输入参数（工作流变量）
     * ⚠️ 必填字段，即使为空也要传 {}
     */
    private Map<String, Object> inputs;

    /**
     * 响应模式（streaming/blocking）
     */
    @JsonProperty("response_mode")
    private String responseMode = "streaming";

    /**
     * 用户标识
     */
    private String user;

    /**
     * 文件列表（如果工作流支持文件）
     */
    private List<Map<String, Object>> files;
}