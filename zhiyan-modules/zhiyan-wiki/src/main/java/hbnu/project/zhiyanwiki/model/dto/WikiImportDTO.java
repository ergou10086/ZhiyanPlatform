package hbnu.project.zhiyanwiki.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wiki导入DTO
 * 用于导入请求参数
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiImportDTO {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 父页面ID（可选，不指定则为根页面）
     */
    private Long parentId;

    /**
     * 导入格式（MARKDOWN, WORD）
     */
    private String format;

    /**
     * 是否覆盖同名页面
     */
    @Builder.Default
    private Boolean overwrite = false;

    /**
     * 是否导入为公开页面
     */
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * 导入者ID
     */
    private Long importBy;
}

