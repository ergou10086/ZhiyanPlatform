package hbnu.project.zhiyanaidify.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import hbnu.project.zhiyanai.model.dto.MessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息列表响应
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListResponse {

    /**
     * 返回条数
     */
    @JsonProperty("limit")
    private Integer limit;

    /**
     * 是否有更多数据
     */
    @JsonProperty("has_more")
    private Boolean hasMore;

    /**
     * 消息列表
     */
    @JsonProperty("data")
    private List<MessageDTO> data;

}
