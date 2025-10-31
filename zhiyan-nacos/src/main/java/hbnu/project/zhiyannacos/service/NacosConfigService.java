package hbnu.project.zhiyannacos.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import hbnu.project.zhiyannacos.config.properties.NacosManagementProperties;
import hbnu.project.zhiyannacos.info.ConfigInfo;
import hbnu.project.zhiyannacos.listener.ConfigChangeListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Nacos 配置管理服务，接口化发布配置的服务类
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NacosConfigService {

    private final ConfigService configService;
    private final NacosManagementProperties properties;
    private final ConfigHistoryService configHistoryService;


    /**
     * 获取配置
     */
    public String getConfig(String dataId, String group) throws NacosException {
        if (group == null || group.isEmpty()) {
            group = properties.getGroup();
        }

        log.info("获取配置: dataId={}, group={}", dataId, group);
        return configService.getConfig(dataId, group, 5000);
    }


    /**
     * 发布配置
     */
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        if (group == null || group.isEmpty()) {
            group = properties.getGroup();
        }

        log.info("发布配置: dataId={}, group={}", dataId, group);
        boolean result = configService.publishConfig(dataId, group, content);
        
        // 记录配置变更历史
        if (result) {
            configHistoryService.recordHistory(dataId, group, content, "CREATE/UPDATE", "admin");
        }
        
        return result;
    }


    /**
     * 删除配置
     */
    public boolean removeConfig(String dataId, String group) throws NacosException {
        if (group == null || group.isEmpty()) {
            group = properties.getGroup();
        }

        log.info("删除配置: dataId={}, group={}", dataId, group);
        boolean result = configService.removeConfig(dataId, group);
        
        // 记录配置删除历史
        if (result) {
            configHistoryService.recordHistory(dataId, group, "", "DELETE", "admin");
        }
        
        return result;
    }


    /**
     * 添加配置监听器
     */
    public void addListener(String dataId, String group, Listener listener) throws NacosException {
        if (group == null || group.isEmpty()) {
            group = properties.getGroup();
        }

        log.info("添加配置监听器: dataId={}, group={}", dataId, group);
        configService.addListener(dataId, group, listener);
    }


    /**
     * 移除配置监听器
     */
    public void removeListener(String dataId, String group, Listener listener) {
        if (group == null || group.isEmpty()) {
            group = properties.getGroup();
        }

        log.info("移除配置监听器: dataId={}, group={}", dataId, group);
        configService.removeListener(dataId, group, listener);
    }


    /**
     * 获取配置列表
     * 通过 Nacos Open API 获取配置列表
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @param search   搜索关键字（可选）
     * @return 配置列表
     */
    public List<ConfigInfo> getConfigList(int pageNo, int pageSize, String search) {
        List<ConfigInfo> configList = new ArrayList<>();
        
        try {
            // 构建 Nacos Open API URL
            String baseUrl = "http://" + properties.getServerAddr();
            String url = baseUrl + "/nacos/v1/cs/configs";
            
            // 构建请求参数
            HttpRequest request = HttpRequest.get(url)
                    .form("pageNo", pageNo)
                    .form("pageSize", pageSize)
                    .form("tenant", properties.getNamespace());
            
            // 如果有搜索关键字，添加到请求中
            if (StrUtil.isNotBlank(search)) {
                request.form("search", "accurate")
                       .form("dataId", search);
            }
            
            // 如果需要认证
            if (StrUtil.isNotBlank(properties.getUsername()) && 
                StrUtil.isNotBlank(properties.getPassword())) {
                // 先登录获取 accessToken
                String accessToken = login();
                if (StrUtil.isNotBlank(accessToken)) {
                    request.form("accessToken", accessToken);
                }
            }
            
            // 发送请求
            HttpResponse response = request.execute();
            
            if (response.isOk()) {
                String body = response.body();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                
                // 解析配置列表
                if (jsonObject.containsKey("pageItems")) {
                    JSONArray pageItems = jsonObject.getJSONArray("pageItems");
                    for (int i = 0; i < pageItems.size(); i++) {
                        JSONObject item = pageItems.getJSONObject(i);
                        ConfigInfo configInfo = ConfigInfo.builder()
                                .dataId(item.getStr("dataId"))
                                .group(item.getStr("group"))
                                .content(item.getStr("content"))
                                .md5(item.getStr("md5"))
                                .type(item.getStr("type"))
                                .build();
                        configList.add(configInfo);
                    }
                }
                
                log.info("获取配置列表成功: 共{}条", configList.size());
            } else {
                log.error("获取配置列表失败: HTTP {}", response.getStatus());
            }
            
        } catch (Exception e) {
            log.error("获取配置列表异常", e);
        }
        
        return configList;
    }

    
    /**
     * 添加配置监听器（带历史记录）
     *
     * @param dataId   配置ID
     * @param group    分组
     * @param operator 操作人
     */
    public void addConfigListener(String dataId, String group, String operator) throws NacosException {
        if (group == null || group.isEmpty()) {
            group = properties.getGroup();
        }

        // 创建配置变更监听器
        ConfigChangeListener listener = new ConfigChangeListener(
                dataId, 
                group, 
                configHistoryService, 
                operator
        );

        log.info("添加配置监听器: dataId={}, group={}, operator={}", dataId, group, operator);
        configService.addListener(dataId, group, listener);
    }

    /**
     * 登录 Nacos 获取 accessToken
     */
    private String login() {
        try {
            String baseUrl = "http://" + properties.getServerAddr();
            String loginUrl = baseUrl + "/nacos/v1/auth/login";
            
            HttpResponse response = HttpRequest.post(loginUrl)
                    .form("username", properties.getUsername())
                    .form("password", properties.getPassword())
                    .execute();
            
            if (response.isOk()) {
                JSONObject jsonObject = JSONUtil.parseObj(response.body());
                return jsonObject.getStr("accessToken");
            }
        } catch (Exception e) {
            log.error("登录 Nacos 失败", e);
        }
        return null;
    }
}
