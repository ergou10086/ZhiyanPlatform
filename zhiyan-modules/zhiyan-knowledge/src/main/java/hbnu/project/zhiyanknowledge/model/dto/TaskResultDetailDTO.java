package hbnu.project.zhiyanknowledge.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务成果型 detailData 顶层结构。
 * 用于存储任务成果类型的详细信息，包括关联任务、人员、章节等内容。
 * 
 * 注意：
 * - 此DTO用于序列化到achievement_detail.detail_data字段（JSON格式）
 * - tasks字段不持久化，仅用于查询时填充展示
 * - authors和reviewers字段不持久化，从关联任务中动态提取
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
     * schema 版本（便于后续扩展和兼容性处理）。
     * 默认值：1.0
     */
    @Builder.Default
    private String schemaVersion = "1.0";

    /**
     * 数据来源：AI（AI生成） / MANUAL（手动撰写）。
     */
    private String source;

    /**
     * AI生成时的任务ID（用于追踪生成任务）。
     * 仅在source为AI时有效。
     */
    private String aiGenerateJobId;

    /**
     * 关联任务列表（任务ID列表，用于跨数据库关联查询）。
     * 注意：这里只存储任务ID，实际的任务详情通过应用层关联查询获取。
     * 此字段会持久化到detailData JSON中。
     */
    @Builder.Default
    private List<Long> linkedTaskIds = new ArrayList<>();

    /**
     * 关联任务详细信息（用于展示，不持久化到detailData）。
     * 在查询时通过应用层关联填充，不会序列化到JSON中。
     */
    @JsonIgnore
    @Builder.Default
    private List<TaskResultTaskRefDTO> tasks = new ArrayList<>();

    /**
     * 主作者/提交人列表。
     * 从关联任务的提交人中提取，不持久化，查询时动态填充。
     */
    @JsonIgnore
    @Builder.Default
    private List<TaskResultPersonRefDTO> authors = new ArrayList<>();

    /**
     * 审核人/责任人列表。
     * 从关联任务的创建者/审核人中提取，不持久化，查询时动态填充。
     */
    @JsonIgnore
    @Builder.Default
    private List<TaskResultPersonRefDTO> reviewers = new ArrayList<>();

    /**
     * 摘要，与 AchievementDetail.abstractText 对应。
     * 用于快速预览和搜索。
     */
    private String summary;

    /**
     * 章节内容列表（Markdown / 文本格式）。
     * 按sortOrder排序展示。
     */
    @Builder.Default
    private List<TaskResultSectionDTO> sections = new ArrayList<>();

    /**
     * 目标读者（AI生成时使用）。
     * 可选字段，用于记录成果的目标受众。
     */
    private String targetAudience;

    /**
     * 创建时间（草稿创建时间）。
     * 可选字段，用于记录草稿的创建时间。
     */
    private LocalDateTime draftCreatedAt;

    /**
     * 最后更新时间。
     * 可选字段，用于记录草稿的最后更新时间。
     */
    private LocalDateTime lastUpdatedAt;
}
