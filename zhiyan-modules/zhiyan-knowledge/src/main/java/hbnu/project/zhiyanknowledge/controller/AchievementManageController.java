package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.entity.AchievementDetail;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private final AchievementFileService achievementFileService;

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
    public R<AchievementDetailDTO>  getAchievementDetail(
            @Parameter(description = "成果ID") @PathVariable Long achievementId) {

        log.info("查询成果详情: achievementId={}", achievementId);

        AchievementDetailDTO detail = achievementDetailsService.getAchievementDetail(achievementId);

        return R.ok(detail, "查询成功");
    }



}
