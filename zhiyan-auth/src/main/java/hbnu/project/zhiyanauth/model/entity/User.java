package hbnu.project.zhiyanauth.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanauth.model.enums.UserStatus;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 用户实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditEntity {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '用户唯一标识（雪花ID）'")
    private Long id;

    /**
     *  用户邮箱（登录账号）
     */
    @Column(name = "email", nullable = false, unique = true,
            columnDefinition = "VARCHAR(255) COMMENT '用户邮箱（登录账号）'")
    private String email;

    /**
     * 用户密码哈希值
     */
    @Column(name = "password_hash", nullable = false,
            columnDefinition = "VARCHAR(255) COMMENT '密码哈希值（加密存储）'")
    private String passwordHash;

    /**
     * 用户名
     */
    @Column(name = "name", nullable = false, length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '用户姓名'")
    private String name;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500,
            columnDefinition = "VARCHAR(500) COMMENT '用户头像URL'")
    private String avatarUrl;

    /**
     * 用户职称/职位
     */
    @Column(name = "title", length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '用户职称/职位'")
    private String title;

    /**
     * 所属机构
     */
    @Column(name = "institution", length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '用户所属机构'")
    private String institution;

    /**
     * 账号是否锁定
     */
    @Builder.Default
    @Column(name = "is_locked", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否锁定（禁止登录）'")
    private Boolean isLocked = false;

    /**
     * 软删除标记
     */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '软删除标记'")
    private Boolean isDeleted = false;

    /**
     * 研究方向标签（JSON数组格式）
     * 存储格式：["机器学习", "自然语言处理", "大模型"]
     * 最多5个标签
     */
    @Column(name = "research_tags", columnDefinition = "JSON COMMENT '研究方向标签（JSON数组，最多5个）'")
    private String researchTags;

    /**
     * 用户状态
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) COMMENT '用户状态（枚举：ACTIVE/LOCKED/DISABLED/DELETED）'")
    private UserStatus status = UserStatus.ACTIVE;
    /**
     * 用户角色关联（一对多）
     * 注意：
     * - @JsonIgnore: 避免 JSON 序列化时的循环引用
     * - @lombok.ToString.Exclude: 避免 toString() 触发懒加载
     */
    @JsonIgnore
    @lombok.ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
    }


    /**
     * 获取研究方向标签列表
     */
    @Transient
    public List<String> getResearchTagList() {
        if (researchTags == null || researchTags.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return new ObjectMapper().readValue(researchTags, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    /**
     * 设置研究方向标签列表
     */
    @Transient
    public void setResearchTagList(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            this.researchTags = null;
            return;
        }
        // 限制最多5个标签
        List<String> limitedTags = tags.stream().limit(5).collect(Collectors.toList());
        try {
            this.researchTags = new ObjectMapper().writeValueAsString(limitedTags);
        } catch (Exception e) {
            this.researchTags = null;
        }
    }
}