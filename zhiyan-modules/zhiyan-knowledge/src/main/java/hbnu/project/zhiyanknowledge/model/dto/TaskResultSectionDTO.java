package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务成果 detailData 中的章节内容。
 * 用于组织成果的章节结构，支持Markdown格式。
 * 
 * 注意：此DTO会持久化到detailData JSON中，用于存储章节内容。
 * 
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultSectionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 章节标题（必填）。
     * 用于标识章节，如"项目背景"、"实现方案"、"成果总结"等。
     */
    private String title;

    /**
     * 章节内容（必填）。
     * 支持Markdown格式，用于存储章节的详细内容。
     */
    private String content;

    /**
     * 排序顺序（用于控制章节显示顺序）。
     * 默认值为0，数值越小越靠前。
     */
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 章节类型（可选）。
     * 用于标识章节的类型，便于前端渲染和样式处理。
     * 可选值：
     * - introduction（引言）
     * - methodology（方法论）
     * - results（结果）
     * - conclusion（结论）
     * - appendix（附录）
     * - 其他自定义类型
     */
    private String sectionType;
}
