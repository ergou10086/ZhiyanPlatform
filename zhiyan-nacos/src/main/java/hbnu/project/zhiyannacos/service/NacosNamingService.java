package hbnu.project.zhiyannacos.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import hbnu.project.zhiyannacos.config.properties.NacosManagementProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Nacos 服务管理，实现接口化管理服务
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NacosNamingService {

    private final NamingService namingService;

    private final NacosManagementProperties properties;



    /**
     * 获取所有服务列表
     */
    public ListView<String> getServiceList(int pageNo, int pageSize) throws NacosException {
        log.info("获取服务列表: pageNo={}, pageSize={}", pageNo, pageSize);
        return namingService.getServicesOfServer(pageNo, pageSize, properties.getGroup());
    }

    /**
     * 获取服务实例列表
     */
    public List<Instance> getServiceInstances(String serviceName) throws NacosException {
        log.info("获取服务实例列表: serviceName={}", serviceName);
        return namingService.getAllInstances(serviceName, properties.getGroup());
    }

    /**
     * 获取健康的服务实例
     */
    public List<Instance> getHealthyInstances(String serviceName) throws NacosException {
        log.info("获取健康服务实例: serviceName={}", serviceName);
        return namingService.selectInstances(serviceName, properties.getGroup(), true);
    }

    /**
     * 注册服务实例
     */
    public void registerInstance(String serviceName, String ip, int port) throws NacosException {
        log.info("注册服务实例: serviceName={}, ip={}, port={}", serviceName, ip, port);
        namingService.registerInstance(serviceName, properties.getGroup(), ip, port);
    }

    /**
     * 注销服务实例
     */
    public void deregisterInstance(String serviceName, String ip, int port) throws NacosException {
        log.info("注销服务实例: serviceName={}, ip={}, port={}", serviceName, ip, port);
        namingService.deregisterInstance(serviceName, properties.getGroup(), ip, port);
    }
}
