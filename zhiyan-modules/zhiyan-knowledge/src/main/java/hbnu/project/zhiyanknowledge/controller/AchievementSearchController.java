package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementQueryDTO;
import hbnu.project.zhiyanknowledge.service.AchievementSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 成果查询搜索接口
 * 负责成果的各种查询、搜索功能
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/zhiyan/achievement/search")
@Tag(name = "成果查询搜索", description = "成果的多条件查询、搜索、列表获取等")
public class AchievementSearchController {

    @Autowired
    private final AchievementSearchService achievementSearchService;

    /**
     * 分页查询成果列表
     * 支持多条件组合查询
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询成果列表", description = "支持多条件组合查询成果")
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
    public R<Page<AchievementDTO>> queryAchievements(
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
    public R<AchievementDTO> getAchievementByName(
            @Parameter(description = "成果名称") @PathVariable String achievementName
    ) {
        log.info("根据名称查询成果: achievementName={}", achievementName);

        AchievementDTO result = achievementSearchService.getAchievementByName(achievementName);

        return R.ok(result, "查询成功");
    }
}
