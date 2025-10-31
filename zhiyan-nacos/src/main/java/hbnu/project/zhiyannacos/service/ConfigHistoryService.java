package hbnu.project.zhiyannacos.service;

import hbnu.project.zhiyannacos.info.ConfigHistoryInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 配置历史记录服务
 * 用于记录和查询配置变更历史
 *
 * @author ErgouTree
 */
@Slf4j
@Service
public class ConfigHistoryService {

    // 使用内存存储配置历史（实际项目中应该使用数据库）
    private final ConcurrentHashMap<String, List<ConfigHistoryInfo>> historyMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 记录配置变更
     *
     * @param dataId   配置ID
     * @param group    分组
     * @param content  配置内容
     * @param opType   操作类型（CREATE/UPDATE/DELETE）
     * @param operator 操作人
     */
    public void recordHistory(String dataId, String group, String content, String opType, String operator) {
        String key = buildKey(dataId, group);
        
        ConfigHistoryInfo historyInfo = ConfigHistoryInfo.builder()
                .id(idGenerator.getAndIncrement())
                .dataId(dataId)
                .group(group)
                .content(content)
                .opType(opType)
                .operator(operator != null ? operator : "system")
                .createTime(LocalDateTime.now())
                .build();

        historyMap.computeIfAbsent(key, k -> new ArrayList<>()).add(historyInfo);
        
        log.info("记录配置历史: dataId={}, group={}, opType={}, operator={}", 
                dataId, group, opType, operator);
    }

    /**
     * 获取指定配置的历史记录
     *
     * @param dataId 配置ID
     * @param group  分组
     * @return 历史记录列表
     */
    public List<ConfigHistoryInfo> getHistory(String dataId, String group) {
        String key = buildKey(dataId, group);
        List<ConfigHistoryInfo> history = historyMap.get(key);
        
        if (history == null) {
            return new ArrayList<>();
        }
        
        // 返回按时间倒序排列的历史记录
        return history.stream()
                .sorted((h1, h2) -> h2.getCreateTime().compareTo(h1.getCreateTime()))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有配置的历史记录
     *
     * @return 所有历史记录
     */
    public List<ConfigHistoryInfo> getAllHistory() {
        return historyMap.values().stream()
                .flatMap(List::stream)
                .sorted((h1, h2) -> h2.getCreateTime().compareTo(h1.getCreateTime()))
                .collect(Collectors.toList());
    }

    /**
     * 获取最近N条历史记录
     *
     * @param limit 限制数量
     * @return 最近的历史记录
     */
    public List<ConfigHistoryInfo> getRecentHistory(int limit) {
        return getAllHistory().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 清空历史记录
     */
    public void clearHistory() {
        historyMap.clear();
        log.info("已清空所有配置历史记录");
    }

    /**
     * 清空指定配置的历史记录
     *
     * @param dataId 配置ID
     * @param group  分组
     */
    public void clearHistory(String dataId, String group) {
        String key = buildKey(dataId, group);
        historyMap.remove(key);
        log.info("已清空配置历史记录: dataId={}, group={}", dataId, group);
    }

    /**
     * 构建存储键
     */
    private String buildKey(String dataId, String group) {
        return dataId + ":" + group;
    }
}

