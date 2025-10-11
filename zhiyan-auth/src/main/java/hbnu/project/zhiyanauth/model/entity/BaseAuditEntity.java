package hbnu.project.zhiyanauth.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * 提供统一的审计字段和乐观锁支持
 *
 * @author ErgouTree
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    /**
     * 数据创建时间（由审计自动填充）
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "DATETIME COMMENT '创建时间'")
    private LocalDateTime createdAt;

    /**
     * 数据最后修改时间（由审计自动更新）
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "DATETIME COMMENT '更新时间'")
    private LocalDateTime updatedAt;


    /**
     * 数据最后修改人（由审计自动更新）
     */
    @LastModifiedBy
    private String updatedBy;


    /**
     * 版本号（乐观锁）
     */
    @Version
    @Column(name = "version", nullable = false,
            columnDefinition = "INT DEFAULT 0 COMMENT '版本号（乐观锁）'")
    private Integer version = 0;
}
