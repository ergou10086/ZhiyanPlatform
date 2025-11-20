package hbnu.project.zhiyanproject.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseAuditEntity {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '项目唯一标识（雪花ID）'")
    private Long id;

    /**
     * 项目名称
     */
    @Column(name = "name", nullable = false, length = 200, columnDefinition = "VARCHAR(200) COMMENT '项目名称'")
    private String name;

    /**
     * 项目描述
     */
    @Column(name = "description", columnDefinition = "TEXT COMMENT '项目描述'")
    private String description;

    /**
     * 项目状态
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PLANNING','ONGOING','COMPLETED','ARCHIVED') DEFAULT 'PLANNING' COMMENT '项目状态（规划中/进行中/已完成/已归档）'")
    private ProjectStatus status = ProjectStatus.PLANNING;

    /**
     * 项目可见性
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", columnDefinition = "ENUM('PUBLIC','PRIVATE') DEFAULT 'PRIVATE' COMMENT '项目可见性（公开/私有）'")
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;

    /**
     * 项目开始日期
     */
    @Column(name = "start_date", columnDefinition = "DATE COMMENT '项目开始日期'")
    private LocalDate startDate;

    /**
     * 项目结束日期
     */
    @Column(name = "end_date", columnDefinition = "DATE COMMENT '项目结束日期'")
    private LocalDate endDate;

    /**
     * 项目图片URL
     */
    @Column(name = "image_url", nullable = false, length = 1000, columnDefinition = "VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '项目图片URL'")
    @lombok.Builder.Default
    private String imageUrl = "";

    /**
     * 创建人ID（逻辑关联users表）
     */
    @CreatedBy
    @LongToString
    @Column(name = "creator_id", nullable = false, columnDefinition = "BIGINT COMMENT '创建人ID（逻辑关联users表）'")
    private Long creatorId;

    /**
     * 创建人姓名（临时字段，不存储到数据库）
     * 用于前端显示，在查询时从用户服务获取并填充
     */
    @Transient
    private String creatorName;

    /**
     * 是否已删除（软删除标记）
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否已删除'")
    @lombok.Builder.Default
    private Boolean isDeleted = false;

    // 在 Project.java 中添加以下内容

    /**
     * 项目成员列表（一对多关系）
     * 注意：
     * - @JsonIgnore: 避免 JSON 序列化时的循环引用
     * - @ToString.Exclude: 避免 toString() 触发懒加载
     */
    @Builder.Default
    @JsonIgnore
    @lombok.ToString.Exclude
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectMember> members = new ArrayList<>();

    /**
     * 获取项目拥有者列表
     * 注意：@JsonIgnore 避免 Jackson 序列化此方法
     */
    @JsonIgnore
    public List<ProjectMember> getOwners() {
        return members.stream()
                .filter(member -> member.getProjectRole() == ProjectMemberRole.OWNER)
                .collect(Collectors.toList());
    }

    /**
     * 获取普通成员列表
     * 注意：@JsonIgnore 避免 Jackson 序列化此方法
     */
    @JsonIgnore
    public List<ProjectMember> getRegularMembers() {
        return members.stream()
                .filter(member -> member.getProjectRole() == ProjectMemberRole.MEMBER)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否为项目拥有者
     * 注意：@JsonIgnore 避免 Jackson 序列化此方法
     */
    @JsonIgnore
    public boolean isOwner(Long userId) {
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(userId) 
                        && member.getProjectRole() == ProjectMemberRole.OWNER);
    }

    /**
     * 检查用户是否为项目成员
     * 注意：@JsonIgnore 避免 Jackson 序列化此方法
     */
    @JsonIgnore
    public boolean isMember(Long userId) {
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }

    /**
     * 获取用户在项目中的角色
     * 注意：@JsonIgnore 避免 Jackson 序列化此方法
     */
    @JsonIgnore
    public Optional<ProjectMemberRole> getUserRole(Long userId) {
        return members.stream()
                .filter(member -> member.getUserId().equals(userId))
                .map(ProjectMember::getProjectRole)
                .findFirst();
    }

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