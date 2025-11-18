package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 任务成果型 detailData 顶层结构。
 * 
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * schema 版本（便于后续扩展）。
     */
    @Builder.Default
    private String schemaVersion = "1.0";

    /**
     * 数据来源：AI / MANUAL。
     */
    private String source;

    /**
     * 关联任务列表。
     */
    private List<TaskResultTaskRefDTO> tasks;

    /**
     * 主作者/提交人。
     */
    private List<TaskResultPersonRefDTO> authors;

    /**
     * 审核人/责任人。
     */
    private List<TaskResultPersonRefDTO> reviewers;

    /**
     * 摘要，与 AchievementDetail.abstractText 对应。
     */
    private String summary;

    /**
     * 章节内容（Markdown / 文本）。
     */
    private List<TaskResultSectionDTO> sections;
}
