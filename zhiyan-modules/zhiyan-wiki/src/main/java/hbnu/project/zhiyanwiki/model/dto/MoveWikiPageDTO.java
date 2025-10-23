package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动Wiki页面DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveWikiPageDTO {

    /**
     * 新的父页面ID（null表示移动到根目录）
     */
    private Long newParentId;

    /**
     * 新的排序序号（可选）
     */
    private Integer newSortOrder;
}
