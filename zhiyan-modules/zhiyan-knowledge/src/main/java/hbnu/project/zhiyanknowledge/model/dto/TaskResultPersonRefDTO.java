package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务成果 detailData 中的人员引用信息。
 * 用于记录成果的作者、审核人等人员信息。
 * 
 * 注意：此DTO主要用于展示，不直接持久化到detailData JSON中。
 * 人员信息通常从关联任务中动态提取。
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
     * 用户 ID（必填）。
     * 用于关联用户服务获取用户信息。
     */
    private Long id;

    /**
     * 用户名称（必填）。
     * 用于展示，通常从用户服务获取。
     */
    private String name;

    /**
     * 用户头像URL（可选）。
     * 用于前端展示用户头像。
     */
    private String avatarUrl;

    /**
     * 角色（必填）。
     * 可选值：
     * - submitter（提交人）
     * - reviewer（审核人）
     * - creator（创建者）
     * - participant（参与者）
     */
    private String role;

    /**
     * 贡献描述（可选）。
     * 用于描述该人员在成果中的具体贡献，如"主要贡献者"、"审核负责人"等。
     */
    private String contribution;

    /**
     * 关联的任务ID（可选）。
     * 用于追溯该人员来自哪个任务，便于追踪人员来源。
     */
    private Long relatedTaskId;
}
