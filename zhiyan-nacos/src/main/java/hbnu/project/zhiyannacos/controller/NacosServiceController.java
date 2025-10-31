package hbnu.project.zhiyannacos.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyannacos.service.NacosNamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nacos 服务管理 Controller
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/nacos/service")
@RequiredArgsConstructor
@Tag(name = "Nacos服务管理", description = "服务注册发现和实例管理")
public class NacosServiceController {

    private final NacosNamingService nacosNamingService;

    @GetMapping("/list")
    @Operation(summary = "获取服务列表", description = "分页获取所有注册的服务")
    public R<ListView<String>> getServiceList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int pageSize) {
        try {
            ListView<String> serviceList = nacosNamingService.getServiceList(pageNo, pageSize);
            return R.ok(serviceList);
        } catch (NacosException e) {
            log.error("获取服务列表失败", e);
            return R.fail("获取服务列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{serviceName}/instances")
    @Operation(summary = "获取服务实例", description = "获取指定服务的所有实例")
    public R<List<Instance>> getServiceInstances(
            @Parameter(description = "服务名称") @PathVariable String serviceName) {
        try {
            List<Instance> instances = nacosNamingService.getServiceInstances(serviceName);
            return R.ok(instances);
        } catch (NacosException e) {
            log.error("获取服务实例失败: serviceName={}", serviceName, e);
            return R.fail("获取服务实例失败: " + e.getMessage());
        }
    }

    @GetMapping("/{serviceName}/healthy-instances")
    @Operation(summary = "获取健康实例", description = "获取指定服务的健康实例")
    public R<List<Instance>> getHealthyInstances(
            @Parameter(description = "服务名称") @PathVariable String serviceName) {
        try {
            List<Instance> instances = nacosNamingService.getHealthyInstances(serviceName);
            return R.ok(instances);
        } catch (NacosException e) {
            log.error("获取健康实例失败: serviceName={}", serviceName, e);
            return R.fail("获取健康实例失败: " + e.getMessage());
        }
    }
}