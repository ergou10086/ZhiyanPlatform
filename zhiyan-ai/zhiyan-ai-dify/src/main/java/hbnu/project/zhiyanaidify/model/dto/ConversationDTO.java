package hbnu.project.zhiyanaidify.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 会话信息 DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationDTO {

    /**
     * 会话 ID
     */
    @JsonProperty("id")
    private String id;


    /**
     * 会话名称
     */
    @JsonProperty("name")
    private String name;


    /**
     * 输入参数
     */
    @JsonProperty("inputs")
    private Map<String, Object> inputs;


    /**
     * 会话状态
     */
    @JsonProperty("status")
    private String status;


    /**
     * 开场白
     */
    @JsonProperty("introduction")
    private String introduction;


    /**
     * 创建时间（时间戳）
     */
    @JsonProperty("created_at")
    private Long createdAt;


    /**
     * 更新时间（时间戳）
     */
    @JsonProperty("updated_at")
    private Long updatedAt;
}
