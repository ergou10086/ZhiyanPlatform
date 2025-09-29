package hbnu.project.zhiyanauth.model.entity;

import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 验证码实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "verification_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '验证码唯一标识（雪花ID）'")
    private Long id;

    /**
     * 用户邮箱
     */
    @Column(name = "email", nullable = false, length = 255,
            columnDefinition = "VARCHAR(255) COMMENT '用户邮箱'")
    private String email;

    /**
     * 验证码
     */
    @Column(name = "code", nullable = false, length = 10,
            columnDefinition = "VARCHAR(10) COMMENT '验证码'")
    private String code;

    /**
     * 验证码类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) COMMENT '验证码类型（REGISTER/RESET_PASSWORD/CHANGE_EMAIL）'")
    private VerificationCodeType type;

    /**
     * 过期时间
     */
    @Column(name = "expires_at", nullable = false,
            columnDefinition = "DATETIME COMMENT '验证码过期时间'")
    private LocalDateTime expiresAt;

    /**
     * 是否已使用
     */
    @Column(name = "is_used", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '验证码是否已使用'")
    private Boolean isUsed = false;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "DATETIME COMMENT '创建时间'")
    private LocalDateTime createdAt;

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