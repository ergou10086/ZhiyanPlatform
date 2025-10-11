package hbnu.project.zhiyanauth.service.impl;


import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.model.form.ChangeEmailBody;
import hbnu.project.zhiyanauth.model.form.ChangePasswordBody;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.service.SysPasswordService;
import hbnu.project.zhiyanauth.service.VerificationCodeService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 密码和邮箱管理服务实现类
 * 提供密码修改、邮箱修改等功能
 * TODO:忘记密码
 * 
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPasswordServiceImpl implements SysPasswordService {

    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;

    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param changePasswordBody 修改密码表单
     * @return 修改结果
     */
    @Transactional
    public R<Void> changePassword(Long userId, ChangePasswordBody changePasswordBody) {
        try {
            // 1. 参数校验
            if (changePasswordBody == null || userId == null) {
                return R.fail("参数不能为空");
            }

            // 2. 密码确认校验
            if (!changePasswordBody.getNewPassword().equals(changePasswordBody.getConfirmPassword())) {
                return R.fail("两次输入的新密码不一致");
            }

            // 3. 查找用户
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();

            // 4. 验证当前密码
            if (!SecurityUtils.matchesPassword(changePasswordBody.getCurrentPassword(), user.getPasswordHash())) {
                log.warn("修改密码失败 - 当前密码错误: 用户ID {}", userId);
                return R.fail("当前密码错误");
            }

            // 5. 更新密码
            user.setPasswordHash(SecurityUtils.encryptPassword(changePasswordBody.getNewPassword()));
            userRepository.save(user);

            log.info("密码修改成功 - 用户ID: {}", userId);
            return R.ok(null, "密码修改成功");

        } catch (Exception e) {
            log.error("密码修改异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("密码修改失败，请稍后重试");
        }
    }


    /**
     * 修改邮箱
     * 
     * @param userId 用户ID
     * @param changeEmailBody 修改邮箱表单
     * @return 修改结果
     */
    @Transactional
    public R<UserDTO> changeEmail(Long userId, ChangeEmailBody changeEmailBody) {
        try {
            // 1. 验证新邮箱的验证码
            R<Boolean> codeValidation = verificationCodeService.validateCode(
                    changeEmailBody.getNewEmail(),
                    changeEmailBody.getVerificationCode(),
                    VerificationCodeType.CHANGE_EMAIL
            );

            if (!codeValidation.getData()) {
                return R.fail("验证码验证失败");
            }

            // 2. 检查新邮箱是否已被使用
            if (userRepository.existsByEmail(changeEmailBody.getNewEmail())) {
                return R.fail("该邮箱已被其他用户使用");
            }

            // 3. 更新用户邮箱
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            user.setEmail(changeEmailBody.getNewEmail());
            userRepository.save(user);

            log.info("用户邮箱修改成功 - 用户ID: {}, 新邮箱: {}", userId, changeEmailBody.getNewEmail());
            return R.ok(null, "邮箱修改成功");
        } catch (Exception e) {
            log.error("邮箱修改异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("邮箱修改失败，请稍后重试");
        }
    }



    /**
     * 验证用户密码
     * 
     * @param userId 用户ID
     * @param password 密码
     * @return 验证结果
     */
    public R<Boolean> validatePassword(Long userId, String password) {
        try {
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            boolean matches = SecurityUtils.matchesPassword(password, user.getPasswordHash());
            
            return R.ok(matches, matches ? "密码验证成功" : "密码错误");

        } catch (Exception e) {
            log.error("密码验证异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("密码验证失败");
        }
    }


    /**
     * 检查密码强度
     * 
     * @param password 密码
     * @return 密码强度评估结果
     */
    public R<String> checkPasswordStrength(String password) {
        try {
            if (password == null || password.length() < 6) {
                return R.ok("弱", "密码长度至少6位");
            }

            int score = 0;
            
            // 长度检查
            if (password.length() >= 8) score++;
            if (password.length() >= 12) score++;
            
            // 字符类型检查
            if (password.matches(".*[a-z].*")) score++; // 小写字母
            if (password.matches(".*[A-Z].*")) score++; // 大写字母
            if (password.matches(".*[0-9].*")) score++; // 数字
            if (password.matches(".*[^a-zA-Z0-9].*")) score++; // 特殊字符

            String strength;
            String message;
            
            if (score <= 2) {
                strength = "弱";
                message = "建议使用更复杂的密码";
            } else if (score <= 4) {
                strength = "中";
                message = "密码强度适中";
            } else {
                strength = "强";
                message = "密码强度很好";
            }

            return R.ok(strength, message);

        } catch (Exception e) {
            log.error("密码强度检查异常: {}", e.getMessage(), e);
            return R.fail("密码强度检查失败");
        }
    }
}
