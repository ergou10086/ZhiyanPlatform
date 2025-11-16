package hbnu.project.zhiyancommonoauth.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2提供商类型枚举
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum OAuth2ProviderType {

    /**
     * GitHub
     */
    GITHUB("github", "GitHub"),

    /**
     * 预留：Google
     */
    GOOGLE("google", "Google"),

    /**
     * 预留：微信
     */
    WECHAT("wechat", "微信"),

    /**
     * 预留：QQ
     * 这两个估计写不上了
     */
    QQ("qq", "QQ");


    private final String code;
    private final String name;
}
