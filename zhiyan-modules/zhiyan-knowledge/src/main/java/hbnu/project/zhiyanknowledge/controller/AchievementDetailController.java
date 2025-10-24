package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import hbnu.project.zhiyanknowledge.service.AchievementDetailsService;
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
 * 成果详情管理接口
 * 负责成果详情的增删改查、模板管理等
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/zhiyan/achievement/detail")
@Tag(name = "成果详情管理", description = "成果详情编辑、模板管理、数据验证等")
public class AchievementDetailController {

    @Autowired
    private final AchievementDetailsService achievementDetailsService;


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
     * 批量更新成果详情字段
     * 支持部分字段更新
     */
    @PatchMapping("/{achievementId}/fields")
    @Operation(summary = "批量更新详情字段", description = "部分更新成果的详情字段")
    public R<AchievementDetailDTO> updateDetailFields(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @RequestBody Map<String, Object> fieldUpdates
    ) {
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("批量更新详情字段: achievementId={}, fieldsCount={}, userId={}", 
                achievementId, fieldUpdates.size(), userId);

        AchievementDetailDTO result = achievementDetailsService.updateDetailFields(
                achievementId, fieldUpdates, userId
        );

        return R.ok(result, "字段更新成功");
    }



    /**
     * 更新成果摘要
     * 单独更新摘要信息
     */
    @PatchMapping("/{achievementId}/abstract")
    @Operation(summary = "更新成果摘要", description = "单独更新成果的摘要信息")
    public R<Void> updateAbstract(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @Parameter(description = "摘要内容") @RequestBody String abstractText
    ) {
        log.info("更新成果摘要: achievementId={}", achievementId);

        achievementDetailsService.updateAbstract(achievementId, abstractText);

        return R.ok(null, "摘要更新成功");
    }


    /**
     * 获取所有系统预设模板
     * 返回所有成果类型的字段模板
     */
    @GetMapping("/templates")
    @Operation(summary = "获取所有系统模板", description = "获取所有成果类型的预设字段模板")
    public R<List<AchievementTemplateDTO>> getAllSystemTemplates() {
        log.info("获取所有系统模板");

        List<AchievementTemplateDTO> templates = achievementDetailsService.getAllSystemTemplates();

        return R.ok(templates, "查询成功");
    }


    /**
     * 根据类型获取模板
     * 获取指定成果类型的字段模板
     */
    @GetMapping("/template/{type}")
    @Operation(summary = "根据类型获取模板", description = "获取指定成果类型的字段模板")
    public R<AchievementTemplateDTO> getTemplateByType(
            @Parameter(description = "成果类型") @PathVariable AchievementType type
    ) {
        log.info("获取成果类型模板: type={}", type);

        AchievementTemplateDTO template = achievementDetailsService.getTemplateByType(type);

        return R.ok(template, "查询成功");
    }


    /**
     * 创建自定义模板
     * 用户可以创建自己的成果字段模板
     */
    @PostMapping("/template/custom")
    @Operation(summary = "创建自定义模板", description = "创建用户自定义的成果字段模板")
    public R<AchievementTemplateDTO> createCustomTemplate(
            @Valid @RequestBody AchievementTemplateDTO templateDTO
    ) {
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("创建自定义模板: templateName={}, userId={}",
                templateDTO.getTemplateName(), userId);

        AchievementTemplateDTO result = achievementDetailsService.createCustomTemplate(templateDTO);

        log.info("自定义模板创建成功: templateId={}", result.getTemplateId());
        return R.ok(result, "自定义模板创建成功");
    }


    /**
     * 根据模板初始化成果详情
     * 为已存在的成果根据模板初始化详情数据
     */
    @PostMapping("/{achievementId}/initialize")
    @Operation(summary = "根据模板初始化详情", description = "为成果根据模板初始化详情数据")
    public R<AchievementDetailDTO> initializeTemplate(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @Parameter(description = "成果类型") @RequestParam String type,
            @RequestBody(required = false) Map<String, Object> initialData
    ){
        // 手动转换
        AchievementType achievementType = AchievementType.getByCode(type);
        if (achievementType == null) {
            throw new IllegalArgumentException("无效的成果类型: " + type);
        }

        log.info("根据模板初始化详情: achievementId={}, type={}", achievementId, type);

        AchievementDetailDTO result = achievementDetailsService.initializeDetailByTemplate(
                achievementId, achievementType, initialData
        );

        return  R.ok(result, "模板初始化成功");
    }


    /**
     * 验证成果详情数据
     * 验证详情数据是否符合模板要求
     */
    @PostMapping("/{achievementId}/validate")
    @Operation(summary = "验证成果详情数据", description = "验证详情数据是否符合模板要求")
    public R<Boolean> validateDetailData(
            @Parameter(description = "成果ID") @PathVariable Long achievementId,
            @RequestBody Map<String, Object> detailData
    ) {
        log.info("验证成果详情数据: achievementId={}", achievementId);
        boolean isValid = achievementDetailsService.validateDetailData(achievementId, detailData);
        return R.ok(isValid, isValid ? "验证通过" : "验证失败");
    }
}
