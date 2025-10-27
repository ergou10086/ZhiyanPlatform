package hbnu.project.zhiyancommonbasic.exception.auth;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

/**
 * 认证控制器异常
 * 专门用于用户认证相关的Controller层异常
 *
 * @author ErgouTree
 */
public class AuthControllerException extends BaseException {

    private static final long serialVersionUID = 1L;

    // 模块名称
    private static final String MODULE = "auth-controller";

    /**
     * 错误码常量
     */
    public static final String AVATAR_UPLOAD_FAILED = "avatar_upload_failed";
    public static final String USER_NOT_FOUND = "user_not_found";
    public static final String PARAM_VALIDATION_FAILED = "param_validation_failed";
    public static final String OPERATION_NOT_ALLOWED = "operation_not_allowed";

    public AuthControllerException(String code, Object[] args, String defaultMessage) {
        super(MODULE, code, args, defaultMessage);
    }

    public AuthControllerException(String code, String defaultMessage) {
        this(code, null, defaultMessage);
    }

    public AuthControllerException(String defaultMessage) {
        super(MODULE, defaultMessage);
    }

    public AuthControllerException(String message, Throwable e) {
        super(message, e);
    }

    /**
     * 快速创建异常 - 头像上传失败
     */
    public static AuthControllerException avatarUploadFailed(String message) {
        return new AuthControllerException(AVATAR_UPLOAD_FAILED, new Object[]{message}, "头像上传失败: " + message);
    }

    /**
     * 快速创建异常 - 用户不存在
     */
    public static AuthControllerException userNotFound(Long userId) {
        return new AuthControllerException(USER_NOT_FOUND, new Object[]{userId}, "用户不存在: " + userId);
    }

    /**
     * 快速创建异常 - 参数验证失败
     */
    public static AuthControllerException paramValidationFailed(String field, String message) {
        return new AuthControllerException(PARAM_VALIDATION_FAILED, new Object[]{field, message},
                "参数验证失败[" + field + "]: " + message);
    }

    /**
     * 快速创建异常 - 操作不允许
     */
    public static AuthControllerException operationNotAllowed(String operation) {
        return new AuthControllerException(OPERATION_NOT_ALLOWED, new Object[]{operation},
                "操作不允许: " + operation);
    }
}
