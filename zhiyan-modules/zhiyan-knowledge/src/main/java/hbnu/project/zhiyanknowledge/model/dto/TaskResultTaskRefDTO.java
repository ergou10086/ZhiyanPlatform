package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务成果 detailData 中的任务引用信息。
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultTaskRefDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID。
     */
    private Long id;

    /**
     * 任务标题。
     */
    private String title;

    /**
     * 任务状态。
     */
    private String status;
}
