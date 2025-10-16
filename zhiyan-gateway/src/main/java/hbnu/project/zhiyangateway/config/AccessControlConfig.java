package hbnu.project.zhiyangateway.config;

import hbnu.project.zhiyangateway.model.AccessControlRule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 访问控制配置
 * 从配置文件加载访问控制规则
 *
 * @author ErgouTree
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "gateway.access-control")
public class AccessControlConfig {

    /**
     * 是否启用访问控制
     * TODO:开发时候通常为否，接口完成时候调动为启动
     */
    private Boolean enabled = false;

    /**
     * 访问控制规则列表
     */
    private List<AccessControlRule> rules = new ArrayList<>();


    /**
     * 初始化后打印配置
     */
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.warn("⚠️ 网关访问控制已禁用");
            return;
        }

        if (rules.isEmpty()) {
            log.warn("⚠️ 网关访问控制规则为空，将使用默认规则");
            loadDefaultRules();
        }

        // 按优先级排序
        rules.sort(Comparator.comparing(AccessControlRule::getPriority));

        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║          🔐 网关访问控制规则已加载                         ║");
        log.info("╠════════════════════════════════════════════════════════╣");
        log.info("║  规则数量: {}", String.format("%-43d", rules.size()) + "║");

        for (int i = 0; i < rules.size(); i++) {
            AccessControlRule rule = rules.get(i);
            if (Boolean.TRUE.equals(rule.getEnabled())) {
                log.info("║  [{}] {}", String.format("%2d", i + 1), String.format("%-44s", rule.getPathPattern()) + "║");
                if (rule.getAllowedRoles() != null && !rule.getAllowedRoles().isEmpty()) {
                    log.info("║      角色: {}", String.format("%-41s", rule.getAllowedRoles()) + "║");
                }
                if (rule.getAllowedMethods() != null && !rule.getAllowedMethods().isEmpty()) {
                    log.info("║      方法: {}", String.format("%-41s", rule.getAllowedMethods()) + "║");
                }
                if (rule.getAllowedIps() != null && !rule.getAllowedIps().isEmpty()) {
                    log.info("║      IP: {}", String.format("%-43s", rule.getAllowedIps()) + "║");
                }
            }
        }
        log.info("╚════════════════════════════════════════════════════════╝");
    }


    /**
     * 加载默认规则
     */
    private void loadDefaultRules() {
        // 管理员接口保护
        rules.add(AccessControlRule.builder()
                // TODO: 这里控制开发者需要的路径匹配
                .pathPattern("")
                .allowedRoles(Set.of("ADMIN", "SUPER_ADMIN"))
                .allowedMethods(Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE))
                .priority(10)
                .enabled(true)
                .description("管理员接口保护")
                .denyMessage("需要管理员/开发者权限")
                .build());

        // 内部服务接口保护
        rules.add(AccessControlRule.builder()
                // TODO: 这里控制内部需要保护的接口的路径匹配
                .pathPattern("")
                .internalOnly(true)
                .priority(5)
                .enabled(true)
                .description("内部服务接口")
                .denyMessage("仅允许内部服务调用")
                .build());
    }


    /**
     * 获取启用的规则
     */
    public List<AccessControlRule> getEnabledRules() {
        return rules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getEnabled()))
                .toList();
    }
}
