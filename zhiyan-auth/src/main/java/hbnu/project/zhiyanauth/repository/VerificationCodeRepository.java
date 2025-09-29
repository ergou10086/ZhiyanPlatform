package hbnu.project.zhiyanauth.repository;

import hbnu.project.zhiyanauth.model.entity.VerificationCode;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 验证码数据访问接口
 *
 * @author ErgouTree
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * 根据邮箱、类型查找最新的未使用验证码
     *
     * @param email 邮箱
     * @param type 验证码类型
     * @return 验证码对象（可能为空）
     */
    Optional<VerificationCode> findTopByEmailAndTypeAndIsUsedFalseOrderByCreatedAtDesc(
            String email, VerificationCodeType type);

    /**
     * 根据邮箱、验证码、类型查找未使用的验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param type 验证码类型
     * @return 验证码对象（可能为空）
     */
    Optional<VerificationCode> findByEmailAndCodeAndTypeAndIsUsedFalse(
            String email, String code, VerificationCodeType type);

    /**
     * 删除过期的验证码
     *
     * @param now 当前时间
     * @return 删除的记录数
     */
    @Query("DELETE FROM VerificationCode vc WHERE vc.expiresAt < :now")
    int deleteExpiredCodes(@Param("now") LocalDateTime now);
}