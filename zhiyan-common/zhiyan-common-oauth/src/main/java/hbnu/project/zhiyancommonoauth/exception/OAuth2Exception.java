package hbnu.project.zhiyancommonoauth.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

import java.io.Serial;

/**
 * OAuth2 异常类
 *
 * @author ErgouTree
 * @rewrite yui
 */
public class OAuth2Exception extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    // 无异常原因的构造方法（调用父类的 module + defaultMessage 构造）
    public OAuth2Exception(String message) {
        super("oauth2", message); // 父类已有此构造：BaseException(String module, String defaultMessage)
    }

    // 有异常原因的构造方法（调用父类的 message + Throwable 构造，并补充 module）
    public OAuth2Exception(String message, Throwable cause) {
        super(message, cause); // 先调用父类处理 message 和 cause
        this.module = "oauth2"; // 手动设置当前异常的模块为 "oauth2"
    }
}