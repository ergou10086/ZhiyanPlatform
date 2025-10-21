package hbnu.project.zhiyancommonloadbalancer.core;

import cn.hutool.core.net.NetUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 自定义 SpringCloud 负载均衡算法
 *
 * 该负载均衡器优先选择本地服务实例（与当前服务在同一主机），
 * 当没有本地实例可用时，采用伪装随机算法从可用实例中选择
 * 抽选思路来自闪耀星骑士伪随机抽卡
 *
 * @author yui
 */
@Slf4j
@AllArgsConstructor
public class CustomSpringCloudLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    /**
     * 目标服务的服务ID
     */
    private final String serviceId;

    /**
     * 服务实例列表提供者，用于获取可用的服务实例列表
     * ObjectProvider提供了延迟获取实例的能力，支持依赖注入
     */
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    /**
     * 选择一个服务实例来处理当前请求
     *
     * @param request 负载均衡请求对象，包含请求上下文等信息
     * @return 包含选中服务实例的响应对象的Mono流
     */
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        // 获取服务实例列表提供者
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        // 从提供者获取服务实例列表
        return supplier.get(request).next().map(serviceInstances -> processInstanceResponse(supplier, serviceInstances));
    }

    /**
     * 处理服务实例列表响应
     * <p>
     * 负责从可用服务实例中选择合适的实例，并触发回调（如果支持）
     *
     * @param supplier          服务实例列表提供者
     * @param serviceInstances  可用的服务实例列表
     * @return 包含选中服务实例的响应对象
     */
    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
                                                              List<ServiceInstance> serviceInstances) {
        // 从服务实例列表中选择一个实例
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances);
        // 如果提供者支持选中实例回调，并且成功选择了实例，则触发回调
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    /**
     * 核心选择逻辑实现
     * <p>
     * 优先选择本地服务实例（主机IP与当前服务IP匹配），
     * 若没有本地实例则随机选择一个可用实例
     *
     * @param instances 可用的服务实例列表
     * @return 包含选中服务实例的响应对象，若没有可用实例则返回空响应
     */
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
        // 若实例列表为空，返回空响应并记录警告日志
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + serviceId);
            }
            return new EmptyResponse();
        }

        // 遍历实例列表，寻找本地实例（主机IP在当前服务的IPv4地址列表中）
        for (ServiceInstance instance : instances) {
            if (NetUtil.localIpv4s().contains(instance.getHost())) {
                return new DefaultResponse(instance);
            }
        }

        // 若没有本地实例，使用随机算法选择一个实例
        return new DefaultResponse(instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
    }

}
