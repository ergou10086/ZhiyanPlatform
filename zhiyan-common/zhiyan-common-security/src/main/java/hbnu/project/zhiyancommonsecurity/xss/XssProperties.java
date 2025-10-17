package hbnu.project.zhiyancommonsecurity.xss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS跨站脚本配置
 *
 * @author ErgouTree
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "security.xss")
public class XssProperties {

    /**
     * 是否启用XSS防护
     */
    private boolean enabled = true;

    /**
     * 排除的URL路径（支持Ant风格通配符）
     */
    private List<String> excludeUrls = new ArrayList<>();
}
