package hbnu.project.zhiyanaidify.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 停止响应请求 DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopChatRequest {

    /**
     * 用户标识（必填）
     * 用于定义终端用户的身份，必须和发送消息接口传入 user 保持一致
     */
    @NotBlank(message = "用户标识不能为空")
    private String user;
}