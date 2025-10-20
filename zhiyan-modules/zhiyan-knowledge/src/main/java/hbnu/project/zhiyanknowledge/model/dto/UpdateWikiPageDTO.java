package hbnu.project.zhiyanknowledge.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新Wiki页面DTO
 * 用于接收前端更新Wiki页面的请求数据
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWikiPageDTO {

    /**
     * 页面ID（必填）
     */
    @NotNull(message = "页面ID不能为空")
    private Long id;

    /**
     * 页面标题（可选）
     */
    @Size(min = 1, max = 255, message = "页面标题长度必须在1-255字符之间")
    private String title;

    /**
     * Markdown内容（可选）
     */
    private String content;

    /**
     * 父页面ID（可选）
     */
    private Long parentId;

    /**
     * 排序序号（可选）
     */
    private Integer sortOrder;

    /**
     * 是否公开（可选）
     */
    private Boolean isPublic;

    /**
     * 修改说明（可选，用于版本历史）
     */
    @Size(max = 500, message = "修改说明长度不能超过500字符")
    private String changeDescription;

    /**
     * 版本号（乐观锁，用于并发控制）
     */
    private Integer version;

    /**
     * 编辑者ID（由后端从上下文获取）
     */
    private Long editorId;
}

