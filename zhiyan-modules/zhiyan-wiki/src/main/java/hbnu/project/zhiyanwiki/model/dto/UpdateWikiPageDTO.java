package hbnu.project.zhiyanwiki.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新Wiki页面DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWikiPageDTO {

    /**
     * 页面标题
     */
    @Size(min = 1, max = 255, message = "页面标题长度必须在1-255字符之间")
    private String title;

    /**
     * Markdown内容
     */
    private String content;

    /**
     * 修改说明
     */
    @Size(max = 500, message = "修改说明长度不能超过500字符")
    private String changeDescription;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 编辑者ID（从上下文获取）
     */
    private Long editorId;
}
