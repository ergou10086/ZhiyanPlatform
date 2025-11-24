package hbnu.project.zhiyanauth.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyanauth.model.dto.UserAchievementDTO;
import hbnu.project.zhiyanauth.model.form.AchievementLinkBody;
import hbnu.project.zhiyanauth.model.form.UpdateAchievementLinkBody;
import hbnu.project.zhiyanauth.service.UserAchievementService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户学术成果关联控制器
 * 提供用户手动关联学术成果的功能
 *
 * @author ErgouTree
 */
@RestController
@RequestMapping("/zhiyan/auth/users/achievements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户学术成果", description = "用户学术成果关联管理")
@AccessLog("用户学术成果")
public class UserAchievementController {

    @Resource
    private final UserAchievementService userAchievementService;


    /**
     * 关联学术成果
     * 用户手动关联所在项目内的公开成果
     */
    @PostMapping("/link")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "关联学术成果", description = "用户手动关联所在项目内的公开成果")
    @OperationLog(module = "用户学术成果", type = OperationType.CREATE, description = "关联学术成果")
    public R<UserAchievementDTO> linkAchievement(@Valid @RequestBody AchievementLinkBody linkBody) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail("用户未登录");
        }

        log.info("用户[{}]关联学术成果: {}", userId, linkBody);
        return userAchievementService.linkAchievement(userId, linkBody);
    }


    /**
     * 取消关联学术成果
     */
    @DeleteMapping("/{achievementId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "取消关联学术成果", description = "取消用户与学术成果的关联")
    @OperationLog(module = "用户学术成果", type = OperationType.DELETE, description = "取消关联学术成果")
    public R<Void> unlinkAchievement(
            @Parameter(description = "成果ID", required = true)
            @PathVariable Long achievementId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail("用户未登录");
        }

        log.info("用户[{}]取消关联成果[{}]", userId, achievementId);
        return userAchievementService.unlinkAchievement(userId, achievementId);
    }


    /**
     * 更新成果关联信息（排序、备注）
     */
    @PutMapping("/{achievementId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新成果关联信息", description = "更新展示顺序或备注说明")
    @OperationLog(module = "用户学术成果", type = OperationType.UPDATE, description = "更新成果关联信息")
    public R<UserAchievementDTO> updateAchievementLink(
            @Parameter(description = "成果ID", required = true)
            @PathVariable Long achievementId,
            @Valid @RequestBody UpdateAchievementLinkBody updateBody) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail("用户未登录");
        }

        log.info("用户[{}]更新成果[{}]关联信息", userId, achievementId);
        return userAchievementService.updateAchievementLink(userId, achievementId, updateBody);
    }


    /**
     * 查询当前用户关联的所有成果
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的学术成果", description = "查询当前用户关联的所有学术成果")
    @OperationLog(module = "用户学术成果", type = OperationType.QUERY, description = "查询我的学术成果", recordResult = false)
    public R<List<UserAchievementDTO>> getMyAchievements() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail("用户未登录");
        }

        log.info("查询用户[{}]的学术成果", userId);
        return userAchievementService.getUserAllAchievements(userId);
    }


    /**
     * 查询指定用户的学术成果（公开接口，以后预留）
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户学术成果", description = "查询指定用户关联的学术成果（公开）")
    public R<List<UserAchievementDTO>> getUserAchievements(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("查询用户[{}]的学术成果", userId);
        return userAchievementService.getUserAllAchievements(userId);
    }
}
