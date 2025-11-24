package hbnu.project.zhiyanauth.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "remember_me_tokens")
public class RememberMeToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  用户id
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     *  remember me token
     */
    @Column(unique = true, length = 128)
    private String token;

    /**
     *  remember me token的过期时间
     */
    @Column(name = "expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime;

    /**
     *  remember me token的创建时间
     */
    @Column(name = "created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
