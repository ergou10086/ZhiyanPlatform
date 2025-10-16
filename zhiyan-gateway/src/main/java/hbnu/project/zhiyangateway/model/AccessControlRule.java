package hbnu.project.zhiyangateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

/**
 * 访问控制规则
 * 定义接口的访问限制
 *
 * @author akoiv
 * @rewrite ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessControlRule {

    /**
     * 路径匹配模式（支持Ant风格）
     * 例如：/api/admin/**, /api/user/delete
     */
    private String pathPattern;

    /**
     * 允许的角色列表
     * 例如：["ADMIN", "SUPER_ADMIN"]
     * null 或空表示不限制角色
     */
    private Set<String> allowedRoles;

    /**
     * 允许的IP地址列表
     * 支持单个IP：192.168.1.1
     * 支持IP段：192.168.1.*
     * 支持CIDR：192.168.1.0/24
     * null 或空表示不限制IP
     */
    private Set<String> allowedIps;

    /**
     * 允许的HTTP方法
     * 例如：["GET", "POST"]
     * null 或空表示不限制方法
     */
    private Set<HttpMethod> allowedMethods;

    /**
     * 是否仅允许内部服务调用
     * true = 仅允许带有内部服务标识的请求
     */
    private Boolean internalOnly;

    /**
     * 规则优先级（数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 是否启用此规则
     */
    private Boolean enabled;

    /**
     * 拒绝访问时的提示消息
     */
    private String denyMessage;

    /**
     * 创建默认规则
     */
    public static AccessControlRule createDefault(String pathPattern) {
        return AccessControlRule.builder()
                .pathPattern(pathPattern)
                .priority(100)
                .enabled(true)
                .denyMessage("访问被拒绝")
                .internalOnly(false)
                .build();
    }
}