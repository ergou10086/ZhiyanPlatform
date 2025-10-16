package hbnu.project.zhiyanproject.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseAuditEntity {

    /**
     * 项目唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '项目唯一标识'")
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
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PLANNING','ONGOING','COMPLETED','ARCHIVED') DEFAULT 'PLANNING' COMMENT '项目状态（规划中/进行中/已完成/已归档）'")
    private ProjectStatus status = ProjectStatus.PLANNING;

    /**
     * 项目可见性
     */
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
     * 创建人ID（逻辑关联users表）
     */
    @CreatedBy
    @LongToString
    @Column(name = "created_by", nullable = false, columnDefinition = "BIGINT COMMENT '创建人ID（逻辑关联users表）'")
    private Long createdBy;



    // 在 Project.java 中添加以下内容

    /**
     * 项目成员列表（一对多关系）
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectMember> members = new ArrayList<>();

    /**
     * 获取项目负责人列表
     */
    public List<ProjectMember> getLeaders() {
        return members.stream()
                .filter(member -> member.getProjectRole() == ProjectMemberRole.LEADER)
                .collect(Collectors.toList());
    }

    /**
     * 获取项目维护者列表
     */
    public List<ProjectMember> getMaintainers() {
        return members.stream()
                .filter(member -> member.getProjectRole() == ProjectMemberRole.MAINTAINER)
                .collect(Collectors.toList());
    }

    /**
     * 获取普通成员列表
     */
    public List<ProjectMember> getRegularMembers() {
        return members.stream()
                .filter(member -> member.getProjectRole() == ProjectMemberRole.MEMBER)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否为项目成员
     */
    public boolean isMember(Long userId) {
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }

    /**
     * 获取用户在项目中的角色
     */
    public Optional<ProjectMemberRole> getUserRole(Long userId) {
        return members.stream()
                .filter(member -> member.getUserId().equals(userId))
                .map(ProjectMember::getProjectRole)
                .findFirst();
    }
}