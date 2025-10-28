package hbnu.project.zhiyanai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简易 AI 对话请求 DTO（前端传递）
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRequest {

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 对话 ID（可选，用于维持上下文）
     */
    private String conversationId;

    /**
     * 关联的知识库文件 ID 列表
     */
    @NotEmpty(message = "至少需要选择一个文件")
    private List<Long> fileIds;

    /**
     * 是否启用流式响应
     */
    private Boolean streamMode = false;
}