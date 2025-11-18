package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务成果 detailData 中的人员引用信息。
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultPersonRefDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户名称。
     */
    private String name;

    /**
     * 角色：submitter / reviewer / participant 等。
     */
    private String role;
}
