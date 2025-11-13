package hbnu.project.zhiyanactivelog.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum LoginType {

    /**
     * 密码登录
     */
    PASSWORD("PASSWORD", "密码登录"),

    /**
     * 第三方登录（OAuth）
     */
    OAUTH("OAUTH", "第三方登录");

    private final String code;
    private final String desc;
}
