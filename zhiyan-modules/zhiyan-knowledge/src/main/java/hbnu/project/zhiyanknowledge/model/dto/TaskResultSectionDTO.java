package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务成果 detailData 中的章节内容。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultSectionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 章节标题。
     */
    private String title;

    /**
     * 章节内容（Markdown / 文本）。
     */
    private String content;
}
