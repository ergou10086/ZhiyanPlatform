package hbnu.project.zhiyanknowledge.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 成果-任务关联实体
 * 
 * 作用说明：
 * 1. 用于在应用层实现成果与任务的关联关系（跨数据库关联方案）
 * 2. 由于成果表在知识库数据库，任务表在项目数据库，无法使用数据库外键关联
 * 3. 此表存储在知识库数据库中，只存储成果ID和任务ID的映射关系
 * 4. 实际的任务详情通过调用项目服务API获取
 * 5. 支持一个成果关联多个任务，一个任务可以被多个成果关联
 * 
 * 设计要点：
 * - 使用唯一约束确保同一成果不会重复关联同一任务
 * - 软删除支持（继承BaseAuditEntity）
 * - 支持审计字段（创建人、创建时间等）
 * 
 * @author Tokito
 */
@Entity
@Table(name = "achievement_task_ref", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"achievement_id", "task_id"}),
       indexes = {
           @Index(name = "idx_achievement_id", columnList = "achievement_id"),
           @Index(name = "idx_task_id", columnList = "task_id")
       })
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class AchievementTaskRef extends BaseAuditEntity {
    
    /**
     * 关联ID（雪花ID）
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '关联ID（雪花ID）'")
    private Long id;
    
    /**
     * 成果ID（关联achievement表）
     * 注意：这里不使用JPA外键关联，因为achievement表在同一数据库中
     * 但为了数据一致性，建议在应用层进行校验
     */
    @LongToString
    @Column(name = "achievement_id", nullable = false, columnDefinition = "BIGINT COMMENT '成果ID'")
    private Long achievementId;
    
    /**
     * 任务ID（关联项目服务中的tasks表）
     * 注意：这是跨数据库关联，任务表在项目数据库中
     * 实际的任务详情需要通过调用项目服务API获取
     */
    @LongToString
    @Column(name = "task_id", nullable = false, columnDefinition = "BIGINT COMMENT '任务ID（跨数据库关联）'")
    private Long taskId;
    
    /**
     * 关联备注（可选，用于记录关联原因或说明）。
     * 例如：记录为什么关联此任务、关联的用途等。
     */
    @Column(name = "remark", length = 500, columnDefinition = "VARCHAR(500) COMMENT '关联备注（可选，用于记录关联原因或说明）'")
    private String remark;
    
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

