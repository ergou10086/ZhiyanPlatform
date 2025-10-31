package hbnu.project.zhiyannacos.listener;

import com.alibaba.nacos.api.config.listener.Listener;
import hbnu.project.zhiyannacos.service.ConfigHistoryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

/**
 * Nacos 配置变更监听器
 * 监听配置变更并自动记录历史
 *
 * @author ErgouTree
 */
@Slf4j
public class ConfigChangeListener implements Listener {

    private final String dataId;
    private final String group;
    private final ConfigHistoryService configHistoryService;
    private final String operator;

    /**
     * 构造函数
     *
     * @param dataId                配置ID
     * @param group                 分组
     * @param configHistoryService  配置历史服务
     * @param operator              操作人
     */
    public ConfigChangeListener(String dataId, String group, 
                                ConfigHistoryService configHistoryService, 
                                String operator) {
        this.dataId = dataId;
        this.group = group;
        this.configHistoryService = configHistoryService;
        this.operator = operator;
    }

    @Override
    public Executor getExecutor() {
        // 返回 null 表示使用默认的通知线程池
        return null;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔔 配置变更通知");
        log.info("📝 DataId: {}", dataId);
        log.info("📁 Group: {}", group);
        log.info("👤 Operator: {}", operator);
        log.info("⏰ Time: {}", java.time.LocalDateTime.now());
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.debug("📄 新配置内容:\n{}", configInfo);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 记录配置变更历史
        try {
            if (configHistoryService != null) {
                configHistoryService.recordHistory(
                        dataId, 
                        group, 
                        configInfo, 
                        "UPDATE", 
                        operator
                );
                log.info("✅ 配置变更历史已记录");
            }
        } catch (Exception e) {
            log.error("❌ 记录配置历史失败", e);
        }
    }
}
