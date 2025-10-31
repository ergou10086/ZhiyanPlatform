package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.permission.KnowledgeSecurityUtils;
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
public class AchievementManageController {

    @Autowired
    private final AchievementService achievementService;

    @Autowired
    private final AchievementDetailsService achievementDetailsService;

    @Autowired
    private final KnowledgeSecurityUtils knowledgeSecurityUtils;

    /**
     * 创建成果
     * 创建一个新的成果，包含基本信息和详情数据
     */
    @PostMapping("/create")
    @Operation(summary = "创建成果", description = "为指定项目创建新的成果记录")
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
    public R<Void> deleteAchievement(
            @Parameter(description = "成果ID") @PathVariable Long achievementId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("删除成果: achievementId={}, userId={}", achievementId, userId);

        // 权限检查：必须有删除权限
        knowledgeSecurityUtils.requireDelete(achievementId);

        // 删除详情数据
        achievementDetailsService.deleteDetailByAchievementId(achievementId);

        log.info("成果删除成功: achievementId={}", achievementId);
        return R.ok(null, "成果删除成功");
    }
}
