package hbnu.project.zhiyanknowledge.model.entity;

import com.fasterxml.jackson.annotation.JsonRawValue;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;

import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;

/**
 * 成果详情表实体类
 *
 * @author ErgouTree
 */
@Data
@Entity
@Table(name = "achievement_detail")
@DynamicInsert
@DynamicUpdate
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDetail extends BaseAuditEntity{

    /**
     * 详情唯一标识
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '详情唯一标识'")
    private Long id;


    /**
     * 关联的成果ID
     */
    @LongToString
    @Column(name = "achievement_id", nullable = false, unique = true, columnDefinition = "BIGINT COMMENT '关联的成果ID'")
    private Long achievementId;


    /**
     * 详细信息JSON
     * 示例结构：
     * 论文: {"authors": [], "journal": "", "abstract": "", "doi": ""}
     * 专利: {"patent_no": "", "inventors": [], "application_date": ""}
     * 数据集: {"description": "", "version": "", "format": "", "size": ""}
     * 模型: {"framework": "", "version": "", "purpose": ""}
     */
    @JsonRawValue
    @Column(name = "detail_data", nullable = false, columnDefinition = "JSON COMMENT '详细信息JSON'")
    private String detailData;


    /**
     * 摘要/描述（冗余存储，便于搜索）
     */
    @Column(name = "abstract", columnDefinition = "TEXT COMMENT '摘要/描述（冗余存储，便于搜索）'")
    private String abstractText;


//    /**
//     * 标签，逗号分隔
//     */
//    @Column(name = "tags", length = 500, columnDefinition = "VARCHAR(500) COMMENT '标签，逗号分隔'")
//    private String tags;


    /**
     * 关联的成果实体（外键关联）
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "achievement_detail_ibfk_1"))
    private Achievement achievement;


    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
    }
}
