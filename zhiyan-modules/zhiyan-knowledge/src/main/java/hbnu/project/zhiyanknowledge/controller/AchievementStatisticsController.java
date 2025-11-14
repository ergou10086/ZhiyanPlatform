package hbnu.project.zhiyanknowledge.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.service.AchievementService;
import hbnu.project.zhiyanknowledge.service.AchievementSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 成果统计分析接口
 * 负责成果的各种统计和分析功能
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/zhiyan/achievement/statistics")     // 未修改
@Tag(name = "成果统计分析", description = "成果数量统计、类型分布、状态统计等")
public class AchievementStatisticsController {

    @Autowired
    private final AchievementService achievementService;

    @Autowired
    private final AchievementSearchService achievementSearchService;

    /**
     * 获取项目成果统计信息
     * 统计项目下的成果数量、类型分布等
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "获取项目成果统计", description = "统计项目下的成果数量、类型分布等信息")
    @SentinelResource(value = "knowledge:statistics:project", blockHandler = "projectStatsBlockHandler")
    public R<Map<String, Object>> getProjectAchievementStats(
            @Parameter(description = "项目ID") @PathVariable Long projectId
    ){
        log.info("获取项目成果统计: projectId={}", projectId);
        Map<String, Object> stats = achievementService.getProjectAchievementStats(projectId);
        return R.ok(stats, "统计成功");
    }


    /**
     * 按状态统计成果数量
     * 统计各状态下的成果数量
     */
    @GetMapping("/status/{projectId}")
    @Operation(summary = "按状态统计成果数量", description = "统计项目下各状态的成果数量")
    @SentinelResource(value = "knowledge:statistics:status", blockHandler = "statusStatsBlockHandler")
    public R<Map<AchievementStatus, Long>> countByStatus(
            @Parameter(description = "项目ID") @PathVariable Long projectId
    ) {
        log.info("按状态统计成果: projectId={}", projectId);
        Map<AchievementStatus, Long> stats = achievementService.countByStatus(projectId);
        return R.ok(stats, "统计成功");
    }


    
    /**
     * 按类型统计成果数量
     * 统计各类型下的成果数量
     */
    @GetMapping("/type/{projectId}")
    @Operation(summary = "按类型统计成果数量", description = "统计项目下各类型的成果数量")
    @SentinelResource(value = "knowledge:statistics:type", blockHandler = "typeStatsBlockHandler")
    public R<Map<String, Long>> statisticsByType(
            @Parameter(description = "项目ID") @PathVariable Long projectId
    ) {
        log.info("按类型统计成果: projectId={}", projectId);
        Map<String, Long> stats = achievementSearchService.statisticsByType(projectId);
        return R.ok(stats, "统计成功");
    }

    
    // ==================== Sentinel 限流处理方法 ====================

    /**
     * 项目成果统计限流处理
     */
    public R<Map<String, Object>> projectStatsBlockHandler(Long projectId, BlockException ex) {
        log.warn("[Sentinel] 项目成果统计被限流: projectId={}, {}", projectId, ex.getClass().getSimpleName());
        return R.fail(429, "统计请求过于频繁，请稍后再试");
    }

    /**
     * 按状态统计限流处理
     */
    public R<Map<AchievementStatus, Long>> statusStatsBlockHandler(Long projectId, BlockException ex) {
        log.warn("[Sentinel] 按状态统计被限流: projectId={}, {}", projectId, ex.getClass().getSimpleName());
        return R.fail(429, "统计请求过于频繁，请稍后再试");
    }

    /**
     * 按类型统计限流处理
     */
    public R<Map<String, Long>> typeStatsBlockHandler(Long projectId, BlockException ex) {
        log.warn("[Sentinel] 按类型统计被限流: projectId={}, {}", projectId, ex.getClass().getSimpleName());
        return R.fail(429, "统计请求过于频繁，请稍后再试");
    }
}
