package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wiki版本历史DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiVersionDTO {

    /**
     * 版本
     */
    private Integer version;

    /**
     * 变更描述
     */
    private String changeDescription;

    /**
     * 编辑者ID
     */
    private String editorId;

    /**
     * 编辑者名称
     */
    private String editorName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 新增行数
     */
    private Integer addedLines;

    /**
     * 删除行数
     */
    private Integer deletedLines;

    /**
     * 变更字符数
     */
    private Integer changedChars;

    /**
     * 是否已归档
     */
    private Boolean isArchived;
}
