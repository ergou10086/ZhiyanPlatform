package hbnu.project.zhiyanactivelog.model.entity;

import hbnu.project.zhiyanactivelog.model.enums.LoginStatus;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "login_log", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_login_time", columnList = "login_time"),
        @Index(name = "idx_login_ip", columnList = "login_ip"),
        @Index(name = "idx_user_login_time", columnList = "user_id, login_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog {

    /**
     * 日志ID（雪花ID）
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '日志ID（雪花ID）'")
    private Long id;

    /**
     * 用户ID
     */
    @LongToString
    @Column(name = "user_id", nullable = false, columnDefinition = "BIGINT COMMENT '用户ID'")
    private Long userId;

    /**
     * 用户名（冗余字段，便于查询）
     */
    @Column(name = "username", length = 100, columnDefinition = "VARCHAR(100) COMMENT '用户名（冗余字段，便于查询）'")
    private String username;

    /**
     * 登录IP地址
     */
    @Column(name = "login_ip", length = 50, columnDefinition = "VARCHAR(50) COMMENT '登录IP地址'")
    private String loginIp;

    /**
     * 登录地区
     */
    @Column(name = "login_location", length = 200, columnDefinition = "VARCHAR(200) COMMENT '登录地区（如：北京市）'")
    private String loginLocation;

    /**
     * 用户代理（浏览器信息）
     */
    @Column(name = "user_agent", length = 500, columnDefinition = "VARCHAR(500) COMMENT '用户代理（浏览器信息）'")
    private String userAgent;

    /**
     * 登录状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '登录状态：SUCCESS-成功, FAILED-失败'")
    private LoginStatus loginStatus = LoginStatus.SUCCESS;

    /**
     * 失败原因（登录失败时记录）
     */
    @Column(name = "failure_reason", length = 500, columnDefinition = "VARCHAR(500) COMMENT '失败原因（登录失败时记录）'")
    private String failureReason;

    /**
     * 登录时间
     */
    @Column(name = "login_time", nullable = false, columnDefinition = "DATETIME NOT NULL COMMENT '登录时间'")
    private LocalDateTime loginTime;

    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        if (this.loginTime == null) {
            this.loginTime = LocalDateTime.now();
        }
    }
}
