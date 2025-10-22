package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import hbnu.project.zhiyanknowledge.service.AchievementDetailsService;
import hbnu.project.zhiyanknowledge.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 成果管理接口
 * 能够创建成果，并且编写对应的详情，给成果内的文件管理做基础
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("zhiyan/achievement")
@Tag(name = "成果管理", description = "成果创建、详情编辑")
public class AchievementManageController {

    @Autowired
    private final AchievementService achievementService;

    @Autowired
    private final AchievementDetailsService achievementDetailsService;


    /**
     * 创建成果
     * 创建一个新的成果，包含基本信息和详情数据
     */
    @PostMapping("/create")
    @Operation(summary = "创建成果", description = "为指定项目创建新的成果记录")
    public R<AchievementDTO> createAchievement(
            @Valid @RequestBody CreateAchievementDTO createDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ){
        log.info("创建成果请求: projectId={}, title={}, type={}",
                createDTO.getProjectId(), createDTO.getTitle(), createDTO.getType());

        // 设置创建者ID，从请求头获取，之后应修改成从安全上下文获取
        if (userId != null) {
            createDTO.setCreatorId(userId);
        }

        AchievementDTO result = achievementDetailsService.createAchievementWithDetails(createDTO);

        log.info("成果创建成功: achievementId={}", result.getId());
        return R.ok(result, "成果创建成功");
    }


    /**
     * 更新成果详情数据
     * 更新成果的详细信息和摘要
     */
    @PutMapping("/details")
    @Operation(summary = "更新成果详情", description = "更新成果的详细信息JSON和摘要")
    public R<Void> updateAchievementDetails(
            @Valid @RequestBody UpdateDetailDataDTO updateDTO
    ){
        log.info("更新成果详情: achievementId={}", updateDTO.getAchievementId());

        achievementDetailsService.updateDetailData(updateDTO);

        log.info("成果详情更新成功: achievementId={}", updateDTO.getAchievementId());
        return R.ok(null, "成果详情更新成功");
    }


    /**
     * 获取成果详情
     * 查询成果的完整信息，包含详情数据
     */
    @GetMapping("/{achievementId}")
    @Operation(summary = "获取成果详情", description = "根据ID查询成果的完整信息")
    public R<AchievementDetailDTO> getAchievementDetail(
            @Parameter(description = "成果ID") @PathVariable Long achievementId) {

        log.info("查询成果详情: achievementId={}", achievementId);

        AchievementDetailDTO detail = achievementDetailsService.getAchievementDetail(achievementId);

        return R.ok(detail, "查询成功");
    }


    /**
     * 分页查询成果列表
     * 支持多条件组合查询
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询成果列表", description = "支持项目ID、类型、状态等多条件组合查询")
    public R<PageResultDTO<AchievementDTO>> queryAchievements(
            @Valid @RequestBody AchievementQueryDTO queryDTO) {

        log.info("分页查询成果: projectId={}, type={}, status={}, page={}, size={}",
                queryDTO.getProjectId(), queryDTO.getType(), queryDTO.getStatus(),
                queryDTO.getPage(), queryDTO.getSize());

        PageResultDTO<AchievementDTO> result = achievementService.queryAchievements(queryDTO);

        return R.ok(result, "查询成功");
    }


    /**
     * 根据项目ID查询成果列表
     * 获取项目下的所有成果（不分页）
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "根据项目ID查询成果列表", description = "获取指定项目下的所有成果")
    public R<List<AchievementDTO>> getAchievementsByProject(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {

        log.info("查询项目成果列表: projectId={}", projectId);

        List<AchievementDTO> achievements = achievementService.getAchievementsByProjectId(projectId);

        return R.ok(achievements, "查询成功");
    }


    /**
     * 根据创建者ID查询成果列表
     * 查询用户创建的成果（分页）
     */
    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "根据创建者ID查询成果列表", description = "查询指定用户创建的成果")
    public R<PageResultDTO<AchievementDTO>> getAchievementsByCreator(
            @Parameter(description = "创建者ID") @PathVariable Long creatorId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {

        log.info("查询创建者成果: creatorId={}, page={}, size={}", creatorId, page, size);

        PageResultDTO<AchievementDTO> result = achievementService.getAchievementsByCreatorId(creatorId, page, size);

        return R.ok(result, "查询成功");
    }


    /**
     * 更新成果基本信息
     * 支持更新标题、类型、状态、摘要等信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新成果基本信息", description = "更新成果的标题、类型、状态、摘要等信息")
    public R<AchievementDTO> updateAchievement(
            @Valid @RequestBody UpdateAchievementDTO updateDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("更新成果: achievementId={}, userId={}", updateDTO.getId(), userId);

        AchievementDTO result = achievementService.updateAchievement(updateDTO, userId);

        log.info("成果更新成功: achievementId={}", result.getId());
        return R.ok(result, "成果更新成功");
    }


    /**
     * 更新成果标题
     * 快速更新成果标题
     */
    @PutMapping("/{achievementId}/title")
    @Operation(summary = "更新成果标题", description = "快速更新成果的标题")
    public R<Void> updateAchievementTitle(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @Parameter(description = "新标题") @RequestParam String title,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("更新成果标题: achievementId={}, title={}", achievementId, title);

        achievementService.updateAchievementTitle(achievementId, title, userId);

        return R.ok(null, "标题更新成功");
    }


    /**
     * 更新成果状态
     * 修改成果的发布状态
     */
    @PutMapping("/{achievementId}/status")
    @Operation(summary = "更新成果状态", description = "修改成果的发布状态（草稿、审核中、已发布、已过时）")
    public R<Void> updateAchievementStatus(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @Parameter(description = "新状态") @RequestParam AchievementStatus status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("更新成果状态: achievementId={}, status={}", achievementId, status);

        achievementService.updateAchievementStatus(achievementId, status, userId);

        return R.ok(null, "状态更新成功");
    }


    /**
     * 更新成果摘要
     * 快速更新成果摘要
     */
    @PutMapping("/{achievementId}/abstract")
    @Operation(summary = "更新成果摘要", description = "快速更新成果的摘要内容")
    public R<Void> updateAchievementAbstract(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @Parameter(description = "摘要内容") @RequestBody String abstractText,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("更新成果摘要: achievementId={}", achievementId);

        achievementDetailsService.updateAbstract(achievementId, abstractText);

        return R.ok(null, "摘要更新成功");
    }


    /**
     * 删除成果
     * 删除指定的成果（级联删除详情和文件）
     */
    @DeleteMapping("/{achievementId}")
    @Operation(summary = "删除成果", description = "删除指定的成果，会级联删除详情和文件记录")
    public R<Void> deleteAchievement(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("删除成果: achievementId={}, userId={}", achievementId, userId);

        achievementService.deleteAchievement(achievementId, userId);

        log.info("成果删除成功: achievementId={}", achievementId);
        return R.ok(null, "成果删除成功");
    }


    /**
     * 批量删除成果
     * 批量删除多个成果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除成果", description = "批量删除多个成果")
    public R<Void> batchDeleteAchievements(
            @Parameter(description = "成果ID列表") @RequestBody List<Long> achievementIds,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("批量删除成果: count={}, userId={}", achievementIds.size(), userId);

        achievementService.batchDeleteAchievements(achievementIds, userId);

        log.info("批量删除成功: count={}", achievementIds.size());
        return R.ok(null, String.format("成功删除%d个成果", achievementIds.size()));
    }


    /**
     * 获取项目成果统计信息
     * 统计项目下的成果总数、各状态数量、各类型数量、文件总数等
     */
    @GetMapping("/project/{projectId}/stats")
    @Operation(summary = "获取项目成果统计信息", description = "统计项目下的成果数量、状态分布、类型分布等")
    public R<Map<String, Object>> getProjectAchievementStats(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {

        log.info("查询项目成果统计: projectId={}", projectId);

        Map<String, Object> stats = achievementService.getProjectAchievementStats(projectId);

        return R.ok(stats, "统计成功");
    }


    /**
     * 获取所有系统预设模板
     * 返回所有成果类型的预设模板定义
     */
    @GetMapping("/templates")
    @Operation(summary = "获取所有系统预设模板", description = "返回所有成果类型的预设模板定义")
    public R<List<AchievementTemplateDTO>> getAllSystemTemplates() {

        log.info("查询所有系统预设模板");

        List<AchievementTemplateDTO> templates = achievementDetailsService.getAllSystemTemplates();

        return R.ok(templates, "查询成功");
    }


    /**
     * 根据类型获取成果模板
     * 获取指定成果类型的模板定义
     */
    @GetMapping("/templates/{type}")
    @Operation(summary = "根据类型获取成果模板", description = "获取指定成果类型的模板定义")
    public R<AchievementTemplateDTO> getTemplateByType(
            @Parameter(description = "成果类型") @PathVariable AchievementType type) {

        log.info("查询成果类型模板: type={}", type);

        AchievementTemplateDTO template = achievementDetailsService.getTemplateByType(type);

        return R.ok(template, "查询成功");
    }


    /**
     * 创建自定义模板
     * 为CUSTOM类型成果创建自定义字段模板
     */
    @PostMapping("/templates/custom")
    @Operation(summary = "创建自定义模板", description = "为CUSTOM类型成果创建自定义字段模板")
    public R<AchievementTemplateDTO> createCustomTemplate(
            @Valid @RequestBody AchievementTemplateDTO templateDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("创建自定义模板: templateName={}, userId={}", templateDTO.getTemplateName(), userId);

        AchievementTemplateDTO result = achievementDetailsService.createCustomTemplate(templateDTO);

        log.info("自定义模板创建成功: templateId={}", result.getTemplateId());
        return R.ok(result, "模板创建成功");
    }


    /**
     * 检查成果是否存在
     * 用于前端验证
     */
    @GetMapping("/{achievementId}/exists")
    @Operation(summary = "检查成果是否存在", description = "用于验证成果ID是否有效")
    public R<Boolean> checkAchievementExists(
            @Parameter(description = "成果ID") @PathVariable Long achievementId) {

        log.info("检查成果是否存在: achievementId={}", achievementId);

        boolean exists = achievementService.existsById(achievementId);

        return R.ok(exists, exists ? "成果存在" : "成果不存在");
    }

}
