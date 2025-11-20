package hbnu.project.zhiyanknowledge.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.mapper.AchievementConverter;
import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementQueryDTO;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.service.AchievementSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成果查询搜索接口
 * 负责成果的各种查询、搜索功能
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/zhiyan/achievement/search")   //未修改
@Tag(name = "成果查询搜索", description = "成果的多条件查询、搜索、列表获取等")
@AccessLog("成果搜索")
public class AchievementSearchController {

    @Resource
    private final AchievementSearchService achievementSearchService;

    @Resource
    private final AchievementRepository achievementRepository;

    @Resource
    private final AchievementConverter achievementConverter;

    /**
     * 分页查询成果列表
     * 支持多条件组合查询
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询成果列表", description = "支持多条件组合查询成果")
    @OperationLog(module = "成果搜索", type = OperationType.QUERY, description = "分页查询成果", recordResult = false)  // 列表数据不记录响应
    @SentinelResource(value = "knowledge:achievement:query", blockHandler = "queryBlockHandler")
    public R<Page<AchievementDTO>> queryAchievements(
            @Valid @RequestBody AchievementQueryDTO queryDTO
    ){
        log.info("分页查询成果: queryDTO={}", queryDTO);

        // 构建分页参数
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(queryDTO.getSortOrder())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                queryDTO.getSortBy()
        );

        Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);

        Page<AchievementDTO> result = achievementSearchService.queryAchievements(queryDTO, pageable);

        log.info("查询成功: totalElements={}, totalPages={}",
                result.getTotalElements(), result.getTotalPages());
        return R.ok(result, "查询成功");
    }


    /**
     * 根据项目ID查询成果列表
     * 简化查询，只需要项目ID
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "根据项目ID查询成果列表", description = "查询指定项目下的所有成果")
    @OperationLog(module = "成果搜索", type = OperationType.QUERY, description = "根据项目ID查询成果列表", recordResult = false)  // 列表数据不记录响应
    @SentinelResource(value = "knowledge:achievement:queryByProject", blockHandler = "queryByProjectBlockHandler")
    public R<Page<AchievementDTO>> queryAchievementsByProject(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "页码")  @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String sortOrder
    ){
        log.info("根据项目ID查询成果: projectId={}, page={}, size={}", projectId, page, size);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(sortOrder)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AchievementDTO> result = achievementSearchService.getAchievementsByProjectId(projectId, pageable);

        return R.ok(result, "查询成功");
    }


    /**
     * 组合搜索
     * 根据关键字搜索成果（搜索标题、摘要等字段）
     */
    @GetMapping("/search")
    @Operation(summary = "组合搜索", description = "根据关键字搜索成果")
    @OperationLog(module = "成果搜索", type = OperationType.QUERY, description = "根据关键字搜索成果", recordResult = false)
    @SentinelResource(value = "knowledge:achievement:search", blockHandler = "searchBlockHandler")
    public R<Page<AchievementDTO>> searchAchievements(
            @Parameter(description = "搜索关键字") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("组合搜索成果: keyword={}", keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<AchievementDTO> result = achievementSearchService.combinationSearch(keyword, pageable);

        return R.ok(result, "搜索成功");
    }


    /**
     * 根据成果名称查询
     * 模糊匹配成果标题
     */
    @GetMapping("/search/name/{achievementName}")
    @Operation(summary = "根据成果名称查询", description = "模糊匹配成果标题")
    @OperationLog(module = "成果搜索", type = OperationType.QUERY, description = "模糊匹配成果标题", recordResult = false)
    public R<AchievementDTO> getAchievementByName(
            @Parameter(description = "成果名称") @PathVariable String achievementName
    ) {
        log.info("根据名称查询成果: achievementName={}", achievementName);

        AchievementDTO result = achievementSearchService.getAchievementByName(achievementName);

        return R.ok(result, "查询成功");
    }


    /**
     * 批量查询成果信息（供其他服务调用）
     */
    @GetMapping("/batch")
    @Operation(summary = "批量查询成果", description = "根据ID列表批量查询成果信息")
    public R<List<AchievementDTO>> getAchievementsByIds(
            @RequestParam("ids") String ids) {
        log.info("批量查询成果: ids={}", ids);

        List<Long> achievementIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<Achievement> achievements = achievementRepository.findAllById(achievementIds);

        // 只返回公开的成果
        List<AchievementDTO> dtoList = achievements.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsPublic()))
                .map(achievementConverter::toDTO)
                .collect(Collectors.toList());

        return R.ok(dtoList);
    }

    // ==================== Sentinel 限流处理方法 ====================
    /**
     * 分页查询限流处理
     */
    public R<Page<AchievementDTO>> queryBlockHandler(AchievementQueryDTO queryDTO, BlockException ex) {
        log.warn("[Sentinel] 成果分页查询被限流: {}", ex.getClass().getSimpleName());
        return R.fail(429, "查询请求过于频繁，请稍后再试");
    }

    /**
     * 根据项目ID查询限流处理
     */
    public R<Page<AchievementDTO>> queryByProjectBlockHandler(
            Long projectId, Integer page, Integer size, String sortBy, String sortOrder, BlockException ex) {
        log.warn("[Sentinel] 项目成果查询被限流: projectId={}, {}", projectId, ex.getClass().getSimpleName());
        return R.fail(429, "查询请求过于频繁，请稍后再试");
    }

    /**
     * 组合搜索限流处理
     */
    public R<Page<AchievementDTO>> searchBlockHandler(String keyword, Integer page, Integer size, BlockException ex) {
        log.warn("[Sentinel] 成果搜索被限流: keyword={}, {}", keyword, ex.getClass().getSimpleName());
        return R.fail(429, "搜索请求过于频繁，请稍后再试");
    }
}
