package hbnu.project.zhiyanaicoze.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Coze 文件详情响应
 *
 * @author ErgouTree
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CozeFileDetailResponse {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 文件详细信息
     */
    private CozeFileUploadResponse.FileData data;
}
