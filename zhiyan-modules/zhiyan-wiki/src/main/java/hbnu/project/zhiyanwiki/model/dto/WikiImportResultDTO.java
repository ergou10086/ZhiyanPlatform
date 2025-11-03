package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Wiki导入结果DTO
 * 返回导入操作的结果信息
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiImportResultDTO {

    /**
     * 导入是否成功
     */
    private Boolean success;

    /**
     * 导入的页面数量
     */
    @Builder.Default
    private Integer importedCount = 0;

    /**
     * 失败的数量
     */
    @Builder.Default
    private Integer failedCount = 0;

    /**
     * 导入的页面ID列表
     */
    @Builder.Default
    private List<String> pageIds = new ArrayList<>();

    /**
     * 错误信息列表
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * 详细信息
     */
    private String message;
}

