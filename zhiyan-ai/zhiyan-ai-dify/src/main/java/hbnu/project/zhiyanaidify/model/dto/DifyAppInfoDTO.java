package hbnu.project.zhiyanaidify.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dify 应用基本信息 DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifyAppInfoDTO {

    /**
     * 应用名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 应用描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 应用标签
     */
    @JsonProperty("tags")
    private List<String> tags;

    /**
     * 模式（advanced-chat/agent-chat/chat/workflow）
     */
    @JsonProperty("mode")
    private String mode;

    /**
     * 作者名称
     */
    @JsonProperty("author_name")
    private String authorName;
}