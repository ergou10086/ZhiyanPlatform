package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanknowledge.model.dto.FileContextDTO;
import hbnu.project.zhiyanknowledge.service.AchievementFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成果文件 AI 调用接口
 * 供 AI 模块调用，获取文件信息用于对话上下文
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge/files")
@RequiredArgsConstructor
@Tag(name = "文件 AI 接口", description = "供 AI 模块调用的文件查询接口")
public class AchievementFileAIController {

    private final AchievementFileService achievementFileService;

    /**
     * 根据文件 ID 获取文件上下文信息
     *
     * @param fileId 文件 ID
     * @return 文件上下文
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "获取单个文件信息", description = "根据文件 ID 获取文件的详细信息")
    public R<FileContextDTO> getFileById(
            @Parameter(description = "文件 ID") @PathVariable Long fileId
    ) {
        log.info("[文件查询] 获取文件: fileId={}", fileId);

        FileContextDTO fileContext = achievementFileService.getFileContext(fileId);

        if (fileContext == null) {
            return R.fail("文件不存在");
        }

        return R.ok(fileContext);
    }

    /**
     * 批量获取文件上下文信息
     *
     * @param fileIds 文件 ID 列表
     * @return 文件上下文列表
     */
    @GetMapping("/batch")
    @Operation(summary = "批量获取文件信息", description = "根据文件 ID 列表批量获取文件信息")
    public R<List<FileContextDTO>> getFilesByIds(
            @Parameter(description = "文件 ID 列表") @RequestParam("fileIds") List<Long> fileIds
    ) {
        log.info("[文件批量查询] 获取文件: fileIds={}, count={}", fileIds, fileIds.size());

        List<FileContextDTO> fileContexts = achievementFileService.getFileContexts(fileIds);

        return R.ok(fileContexts, String.format("成功获取 %d 个文件", fileContexts.size()));
    }

    /**
     * 获取文件下载 URL
     *
     * @param fileId 文件 ID
     * @param userId 用户 ID（可选）
     * @return 文件 URL
     */
    @GetMapping("/{fileId}/url")
    @Operation(summary = "获取文件 URL", description = "获取文件的下载链接")
    public R<String> getFileUrl(
            @Parameter(description = "文件 ID") @PathVariable Long fileId,
            @Parameter(description = "用户 ID") @RequestParam(value = "userId", required = false) Long userId
    ) {
        if (userId == null) {
            userId = SecurityUtils.getUserId();
        }

        log.info("[文件 URL] 获取下载链接: fileId={}, userId={}", fileId, userId);

        String url = achievementFileService.getFileDownloadUrl(fileId, userId, 3600);

        return R.ok(url);
    }
}
