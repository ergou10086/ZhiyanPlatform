package hbnu.project.zhiyannacos.controller;

import com.alibaba.nacos.api.exception.NacosException;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyannacos.info.ConfigHistoryInfo;
import hbnu.project.zhiyannacos.info.ConfigInfo;
import hbnu.project.zhiyannacos.service.ConfigHistoryService;
import hbnu.project.zhiyannacos.service.NacosConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nacos 配置管理 Controller
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/nacos/config")
@RequiredArgsConstructor
@Tag(name = "Nacos配置管理", description = "配置的增删改查、监听器管理和历史记录")
public class NacosConfigController {

    private final NacosConfigService nacosConfigService;
    private final ConfigHistoryService configHistoryService;

    @GetMapping("/{dataId}")
    @Operation(summary = "获取配置", description = "根据dataId和group获取配置内容")
    public R<String> getConfig(
            @Parameter(description = "配置ID") @PathVariable String dataId,
            @Parameter(description = "分组，默认DEFAULT_GROUP") @RequestParam(required = false) String group) {
        try {
            String config = nacosConfigService.getConfig(dataId, group);
            return R.ok(config);
        } catch (NacosException e) {
            log.error("获取配置失败: dataId={}, group={}", dataId, group, e);
            return R.fail("获取配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/{dataId}")
    @Operation(summary = "发布配置", description = "发布或更新配置")
    public R<Boolean> publishConfig(
            @Parameter(description = "配置ID") @PathVariable String dataId,
            @Parameter(description = "分组") @RequestParam(required = false) String group,
            @Parameter(description = "配置内容") @RequestBody String content) {
        try {
            boolean result = nacosConfigService.publishConfig(dataId, group, content);
            return result ? R.ok(true, "发布成功") : R.fail("发布失败");
        } catch (NacosException e) {
            log.error("发布配置失败: dataId={}, group={}", dataId, group, e);
            return R.fail("发布配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{dataId}")
    @Operation(summary = "删除配置", description = "删除指定配置")
    public R<Boolean> removeConfig(
            @Parameter(description = "配置ID") @PathVariable String dataId,
            @Parameter(description = "分组") @RequestParam(required = false) String group) {
        try {
            boolean result = nacosConfigService.removeConfig(dataId, group);
            return result ? R.ok(true, "删除成功") : R.fail("删除失败");
        } catch (NacosException e) {
            log.error("删除配置失败: dataId={}, group={}", dataId, group, e);
            return R.fail("删除配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取配置列表", description = "分页获取所有配置列表")
    public R<List<ConfigInfo>> getConfigList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "搜索关键字（可选）") @RequestParam(required = false) String search) {
        try {
            List<ConfigInfo> configList = nacosConfigService.getConfigList(pageNo, pageSize, search);
            return R.ok(configList, "获取配置列表成功");
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return R.fail("获取配置列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/listener/{dataId}")
    @Operation(summary = "添加配置监听器", description = "为指定配置添加变更监听器，配置变更时会自动记录历史")
    public R<Boolean> addListener(
            @Parameter(description = "配置ID") @PathVariable String dataId,
            @Parameter(description = "分组") @RequestParam(required = false) String group,
            @Parameter(description = "操作人", example = "admin") @RequestParam(defaultValue = "admin") String operator) {
        try {
            nacosConfigService.addConfigListener(dataId, group, operator);
            return R.ok(true, "添加监听器成功");
        } catch (NacosException e) {
            log.error("添加监听器失败: dataId={}, group={}", dataId, group, e);
            return R.fail("添加监听器失败: " + e.getMessage());
        }
    }

    @GetMapping("/history/{dataId}")
    @Operation(summary = "获取配置历史", description = "获取指定配置的变更历史记录")
    public R<List<ConfigHistoryInfo>> getHistory(
            @Parameter(description = "配置ID") @PathVariable String dataId,
            @Parameter(description = "分组") @RequestParam(required = false, defaultValue = "DEFAULT_GROUP") String group) {
        try {
            List<ConfigHistoryInfo> history = configHistoryService.getHistory(dataId, group);
            return R.ok(history, "获取历史记录成功");
        } catch (Exception e) {
            log.error("获取历史记录失败: dataId={}, group={}", dataId, group, e);
            return R.fail("获取历史记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/history/all")
    @Operation(summary = "获取所有配置历史", description = "获取所有配置的变更历史记录")
    public R<List<ConfigHistoryInfo>> getAllHistory() {
        try {
            List<ConfigHistoryInfo> history = configHistoryService.getAllHistory();
            return R.ok(history, "获取所有历史记录成功");
        } catch (Exception e) {
            log.error("获取所有历史记录失败", e);
            return R.fail("获取所有历史记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/history/recent")
    @Operation(summary = "获取最近的配置历史", description = "获取最近N条配置变更历史")
    public R<List<ConfigHistoryInfo>> getRecentHistory(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ConfigHistoryInfo> history = configHistoryService.getRecentHistory(limit);
            return R.ok(history, "获取最近历史记录成功");
        } catch (Exception e) {
            log.error("获取最近历史记录失败", e);
            return R.fail("获取最近历史记录失败: " + e.getMessage());
        }
    }
}