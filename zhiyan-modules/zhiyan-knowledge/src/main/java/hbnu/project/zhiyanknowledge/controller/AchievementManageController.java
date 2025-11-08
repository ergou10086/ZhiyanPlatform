package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ControllerException;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonidempotent.annotation.Idempotent;
import hbnu.project.zhiyancommonidempotent.enums.IdempotentType;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.permission.KnowledgeSecurityUtils;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.service.AchievementDetailsService;
import hbnu.project.zhiyanknowledge.service.AchievementFileService;
import hbnu.project.zhiyanknowledge.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 成果管理接口
 * 能够创建成果，并且编写对应的详情，给成果内的文件管理做基础
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/zhiyan/achievement")
@Tag(name = "成果管理", description = "成果创建、详情编辑，成果查询等管理成果的接口")
@AccessLog("成果管理")
public class AchievementManageController {

    @Autowired
    private final AchievementService achievementService;

    @Autowired
    private final AchievementDetailsService achievementDetailsService;

    @Autowired
    private final AchievementFileService achievementFileService;

    @Autowired
    private final AchievementRepository achievementRepository;

    @Autowired
    private final KnowledgeSecurityUtils knowledgeSecurityUtils;

    /**
     * 创建成果
     * 创建一个新的成果，包含基本信息和详情数据
     */
    @PostMapping("/create")
    @Operation(summary = "创建成果", description = "为指定项目创建新的成果记录")
    @OperationLog(module = "成果管理", type = OperationType.INSERT, description = "创建成果", recordParams = true, recordResult = true)
    @Idempotent(type = IdempotentType.PARAM, timeout = 5, timeUnit = TimeUnit.SECONDS, message = "成果创建请求重复，请勿频繁提交", deleteOnError = true)   // 幂等注解 - 使用参数防重，5秒内相同参数不允许重复提交，失败时允许重试
    public R<AchievementDTO> createAchievement(
            @Valid @RequestBody CreateAchievementDTO createDTO
    ){
        log.info("创建成果请求: projectId={}, title={}, type={}",
                createDTO.getProjectId(), createDTO.getTitle(), createDTO.getType());

        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            createDTO.setCreatorId(userId);
        }

        // 权限检查：必须是项目成员
        knowledgeSecurityUtils.requireProjectMember(createDTO.getProjectId());

        AchievementDTO result = achievementDetailsService.createAchievementWithDetails(createDTO);

        log.info("成果创建成功: achievementId={}", result.getId());
        return R.ok(result, "成果创建成功");
    }


    /**
     * 更新成果状态
     * 修改成果的发布状态（草稿、已发布、归档等）
     */
    @PatchMapping("/{achievementId}/status")
    @Operation(summary = "更新成果状态", description = "修改成果的发布状态")
    @OperationLog(module = "成果管理", type = OperationType.UPDATE, description = "更新成果状态", recordParams = true, recordResult = false)
    @Idempotent(type = IdempotentType.SPEL, key = "#achievementId + ':' + #status", timeout = 2, message = "状态更新中，请稍候")
    public R<Void> updateAchievementStatus(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @Parameter(description = "新状态") @RequestParam AchievementStatus status
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("更新成果状态: achievementId={}, status={}, userId={}",
                achievementId, status, userId);

        // 权限检查：必须有编辑权限
        knowledgeSecurityUtils.requireEdit(achievementId);

        achievementService.updateAchievementStatus(achievementId, status, userId);

        log.info("成果状态更新成功: achievementId={}, newStatus={}", achievementId, status);
        return R.ok(null, "状态更新成功");
    }


    /**
     * 获取成果详情
     * 查询成果的完整信息，包含详情数据
     */
    @GetMapping("/{achievementId}")
    @Operation(summary = "获取成果详情", description = "根据ID查询成果的完整信息")
    @OperationLog(module = "成果管理", type = OperationType.QUERY, description = "查询成果详情", recordParams = true, recordResult = false ) // 查询结果数据量大，不记录响应)
    public R<AchievementDetailDTO>  getAchievementDetail(
            @Parameter(description = "成果ID") @PathVariable Long achievementId) {

        log.info("查询成果详情: achievementId={}", achievementId);

        // 权限检查：必须有访问权限
        knowledgeSecurityUtils.requireAccess(achievementId);
        AchievementDetailDTO detail = achievementDetailsService.getAchievementDetail(achievementId);

        return R.ok(detail, "查询成功");
    }


    /**
     * 删除成果
     * 删除成果及其关联的所有数据（详情、文件等）
     */
    @DeleteMapping("/{achievementId}")
    @Operation(summary = "删除成果", description = "删除指定成果及其关联数据")
    @OperationLog(module = "成果管理", type = OperationType.DELETE, description = "删除成果", recordParams = true, recordResult = false)
    @Transactional(rollbackFor = Exception.class)
    @Idempotent(type = IdempotentType.SPEL, key = "#achievementId", timeout = 3, message = "删除操作正在处理中")
    public R<Void> deleteAchievement(
            @Parameter(description = "成果ID") @PathVariable Long achievementId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("删除成果开始: achievementId={}, userId={}", achievementId, userId);

        // 权限检查：必须有删除权限
        knowledgeSecurityUtils.requireDelete(achievementId);

        // 1. 删除所有关联的文件（数据库记录 + MinIO文件）
        List<AchievementFileDTO> files = achievementFileService.getFilesByAchievementId(achievementId);
        if (!files.isEmpty()) {
            List<Long> fileIds = files.stream()
                    .map(AchievementFileDTO::getId)
                    .map(Long::valueOf)  // 将String类型的ID转换为Long类型
                    .collect(Collectors.toList());
            log.info("开始删除成果关联的文件: achievementId={}, fileCount={}", achievementId, fileIds.size());
            achievementFileService.deleteFiles(fileIds, userId);
            log.info("成果关联文件删除完成: achievementId={}", achievementId);
        }

        // 2. 删除详情数据
        log.info("开始删除成果详情: achievementId={}", achievementId);
        achievementDetailsService.deleteDetailByAchievementId(achievementId);
        log.info("成果详情删除完成: achievementId={}", achievementId);

        // 3. 删除成果主表记录
        log.info("开始删除成果主表记录: achievementId={}", achievementId);
        achievementRepository.deleteById(achievementId);
        log.info("成果主表记录删除完成: achievementId={}", achievementId);

        log.info("成果删除全部完成: achievementId={}", achievementId);
        return R.ok(null, "成果删除成功");
    }


    /**
     * 更新成果公开性
     * 修改成果是否公开（需要编辑权限）
     */
    @PatchMapping("/{achievementId}/visibility")
    @Operation(summary = "更新成果的公开性", description = "修改成果的公开/私有状态")
    @OperationLog(module = "成果管理", type = OperationType.UPDATE, description = "更新成果公开性", recordParams = true, recordResult = false)
    @Idempotent(type = IdempotentType.SPEL, key = "#achievementId + ':visibility'", timeout = 2, message = "公开性更新中，请稍候")
    public R<Void> updateAchievementVisibility(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @Parameter(description = "公开性") @RequestParam Boolean isPublic
    ){
        Long userId = SecurityUtils.getUserId();
        log.info("更新成果公开性: achievementId={}, isPublic={}, userId={}",
                achievementId, isPublic, userId);

        // 权限检查：必须有编辑权限
        knowledgeSecurityUtils.requireEdit(achievementId);

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ControllerException("成果不存在"));

        achievement.setIsPublic(isPublic);
        achievementRepository.save(achievement);

        log.info("成果公开性更新成功: achievementId={}, isPublic={}", achievementId, isPublic);
        return R.ok(null, "公开性更新成功");
    }
}
