package hbnu.project.zhiyancommonoauth.utils;

import hbnu.project.zhiyancommonoauth.config.properties.OAuth2Properties;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OAuth2工具类
 *
 * @author ErgouTree
 */
@Component
@RequiredArgsConstructor
public class OAuth2Utils {

    private final OAuth2Properties oAuth2Properties;

    /**
     * 构建回调URL
     *
     * @param provider 提供商名称
     * @return 完整的回调URL
     */
    public String buildCallbackUrl(String provider) {
        String baseUrl = oAuth2Properties.getCallbackBaseUrl();
        if (StringUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("OAuth2回调地址基础路径未配置");
        }
        // 移除末尾的斜杠
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/oauth2/callback/" + provider;
    }

    /**
     * 验证提供商是否启用
     *
     * @param provider 提供商名称
     * @return 是否启用
     */
    public boolean isProviderEnabled(String provider) {
        if (StringUtils.isEmpty(provider)) {
            return false;
        }
        // 可以根据需要扩展验证逻辑
        return oAuth2Properties.isEnabled();
    }
}
