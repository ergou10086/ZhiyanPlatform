package hbnu.project.zhiyanapisystem.service;

import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.form.ChangeEmailBody;
import hbnu.project.zhiyanauth.model.form.ChangePasswordBody;
import hbnu.project.zhiyancommonbasic.domain.R;

/**
 * 密码和邮箱管理服务接口
 * 定义密码修改、邮箱修改、密码验证和密码强度检查等功能
 *
 * @author ErgouTree
 */
public interface SysPasswordService {

    /**
     * 修改用户密码
     * 验证当前密码正确性后，更新为新密码
     *
     * @param userId              用户ID（密码所属用户）
     * @param changePasswordBody  修改密码表单数据（包含当前密码、新密码、确认密码）
     * @return R<Void> - 成功返回"密码修改成功"；失败返回错误信息（如参数为空、密码不一致、当前密码错误等）
     */
    R<Void> changePassword(Long userId, ChangePasswordBody changePasswordBody);

    /**
     * 修改用户邮箱
     * 验证新邮箱的验证码后，更新用户邮箱地址
     *
     * @param userId           用户ID（邮箱所属用户）
     * @param changeEmailBody  修改邮箱表单数据（包含新邮箱、验证码）
     * @return R<UserDTO> - 成功返回更新后的用户信息；失败返回错误信息（如验证码无效、邮箱已被使用等）
     */
    R<UserDTO> changeEmail(Long userId, ChangeEmailBody changeEmailBody);

    /**
     * 验证用户密码
     * 检查输入的密码与用户存储的密码哈希是否匹配
     *
     * @param userId   用户ID（待验证的用户）
     * @param password 待验证的密码（明文）
     * @return R<Boolean> - 验证成功返回true；失败返回false或错误信息（如用户不存在）
     */
    R<Boolean> validatePassword(Long userId, String password);

    /**
     * 检查密码强度
     * 基于密码长度、字符类型组合等评估密码安全强度
     *
     * @param password 待检查的密码（明文）
     * @return R<String> - 返回密码强度等级（"弱"、"中"、"强"）及评估信息
     */
    R<String> checkPasswordStrength(String password);
}
