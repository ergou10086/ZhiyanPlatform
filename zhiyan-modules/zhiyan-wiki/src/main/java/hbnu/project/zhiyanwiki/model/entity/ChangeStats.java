package hbnu.project.zhiyanwiki.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容变更统计实体类
 * 用于存储新旧内容对比后的变更数据（新增行数、删除行数、变更字符数）
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStats {
    /**
     * 新增行数
     */
    private Integer addedLines;

    /**
     * 删除行数
     */
    private Integer deletedLines;

    /**
     * 变更字符数（新旧内容长度差值的绝对值）
     */
    private Integer changedChars;
}
