package hbnu.project.zhiyanactivelog.repository;

import hbnu.project.zhiyanactivelog.model.entity.LoginLog;
import hbnu.project.zhiyanactivelog.model.enums.LoginStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 登录日志数据访问层
 *
 * @author ErgouTree
 */
@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog,Long> {

    /**
     * 根据用户ID查询登录日志（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 登录日志分页列表
     */
    Page<LoginLog> findByUserIdOrderByLoginTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据用户ID和登录状态查询登录日志（分页）
     *
     * @param userId 用户ID
     * @param loginStatus 登录状态
     * @param pageable 分页参数
     * @return 登录日志分页列表
     */
    Page<LoginLog> findByUserIdAndLoginStatusOrderByLoginTimeDesc(Long userId, LoginStatus loginStatus, Pageable pageable);

    /**
     * 根据时间范围查询登录日志（分页）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 登录日志分页列表
     */
    @Query("SELECT l FROM LoginLog l WHERE l.loginTime BETWEEN :startTime AND :endTime ORDER BY l.loginTime DESC")
    Page<LoginLog> findByLoginTimeBetween(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          Pageable pageable);

    /**
     * 根据IP地址查询登录日志（分页）
     *
     * @param loginIp IP地址
     * @param pageable 分页参数
     * @return 登录日志分页列表
     */
    Page<LoginLog> findByLoginIpOrderByLoginTimeDesc(String loginIp, Pageable pageable);

    /**
     * 统计用户登录次数
     *
     * @param userId 用户ID
     * @return 登录次数
     */
    long countByUserId(Long userId);

    /**
     * 统计用户成功登录次数
     *
     * @param userId 用户ID
     * @return 成功登录次数
     */
    long countByUserIdAndLoginStatus(Long userId, LoginStatus loginStatus);

    /**
     * 查询用户最近一次登录记录
     *
     * @param userId 用户ID
     * @return 最近一次登录记录
     */
    LoginLog findFirstByUserIdOrderByLoginTimeDesc(Long userId);
}
