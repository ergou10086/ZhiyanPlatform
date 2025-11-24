package hbnu.project.zhiyanauth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新研究方向标签请求体
 *
 * @author ErgouTree
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新研究方向标签请求体")
public class UpdateResearchTagsBody {

    /**
     * 研究方向标签列表
     */
    @NotEmpty(message = "研究方向标签不能为空")
    @Size(min = 1, max = 5, message = "研究方向标签数量必须在1-5个之间")
    @Schema(description = "研究方向标签", example = "[\"机器学习\", \"自然语言处理\", \"大模型\"]")
    private List<@Size(max = 50, message = "单个标签长度不能超过50个字符") String> researchTags;
}