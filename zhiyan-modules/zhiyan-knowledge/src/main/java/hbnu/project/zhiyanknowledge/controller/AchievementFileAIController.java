package hbnu.project.zhiyanknowledge.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
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
    @SentinelResource(value = "knowledge:ai:getFileById", blockHandler = "getFileBlockHandler", fallback = "getFileFallback")
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
    @SentinelResource(value = "knowledge:ai:getFilesBatch", blockHandler = "getFilesBatchBlockHandler", fallback = "getFilesBatchFallback")
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
    @SentinelResource(value = "knowledge:ai:getFileUrl", blockHandler = "getFileUrlBlockHandler", fallback = "getFileUrlFallback")
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


    // ==================== Sentinel 熔断降级处理方法 ====================

    /**
     * 获取单个文件限流处理
     */
    public R<FileContextDTO> getFileBlockHandler(Long fileId, BlockException ex) {
        log.warn("[Sentinel] AI获取文件被限流: fileId={}, {}", fileId, ex.getClass().getSimpleName());
        return R.fail(429, "文件查询请求过于频繁，请稍后再试");
    }

    /**
     * 获取单个文件降级处理（服务异常）
     */
    public R<FileContextDTO> getFileFallback(Long fileId, Throwable throwable) {
        log.error("[Sentinel] AI获取文件服务异常降级: fileId={}", fileId, throwable);
        return R.fail(503, "文件查询服务暂时不可用，请稍后重试");
    }

    /**
     * 批量获取文件限流处理
     */
    public R<List<FileContextDTO>> getFilesBatchBlockHandler(List<Long> fileIds, BlockException ex) {
        log.warn("[Sentinel] AI批量获取文件被限流: count={}, {}", fileIds.size(), ex.getClass().getSimpleName());
        return R.fail(429, "批量查询请求过于频繁，请稍后再试");
    }

    /**
     * 批量获取文件降级处理（服务异常）
     */
    public R<List<FileContextDTO>> getFilesBatchFallback(List<Long> fileIds, Throwable throwable) {
        log.error("[Sentinel] AI批量获取文件服务异常降级: count={}", fileIds.size(), throwable);
        return R.fail(503, "批量查询服务暂时不可用，请稍后重试");
    }

    /**
     * 获取文件URL限流处理
     */
    public R<String> getFileUrlBlockHandler(Long fileId, Long userId, BlockException ex) {
        log.warn("[Sentinel] AI获取文件URL被限流: fileId={}, {}", fileId, ex.getClass().getSimpleName());
        return R.fail(429, "获取下载链接请求过于频繁，请稍后再试");
    }

    /**
     * 获取文件URL降级处理（服务异常）
     */
    public R<String> getFileUrlFallback(Long fileId, Long userId, Throwable throwable) {
        log.error("[Sentinel] AI获取文件URL服务异常降级: fileId={}", fileId, throwable);
        return R.fail(503, "获取下载链接服务暂时不可用，请稍后重试");
    }
}
