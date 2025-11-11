package hbnu.project.zhiyanknowledge.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonidempotent.annotation.Idempotent;
import hbnu.project.zhiyancommonidempotent.enums.IdempotentType;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanknowledge.model.dto.AchievementFileDTO;
import hbnu.project.zhiyanknowledge.model.dto.UploadFileDTO;
import hbnu.project.zhiyanknowledge.service.AchievementFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 成果文件上传接口
 * 负责把文件上传到成果中
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/zhiyan/achievement/file")
@Tag(name = "成果的文件管理", description = "成果文件上传等管理")
@AccessLog("成果文件管理")
public class AchievementFileController {

    @Autowired
    private AchievementFileService achievementFileService;
    
    @Autowired
    private hbnu.project.zhiyancommonbasic.utils.JwtUtils jwtUtils;

    /**
     * 上传成果文件
     * 上传单个文件到指定成果
     */
    @PostMapping("/upload")
    @Operation(summary = "上传成果文件", description = "为指定成果上传单个文件")
    @OperationLog(module = "成果文件管理", type = OperationType.UPLOAD, description = "上传成果文件", recordParams = false, recordResult = true)
    @SentinelResource(value = "knowledge:file:upload", blockHandler = "uploadBlockHandler", fallback = "uploadFallback")
    @Idempotent(type = IdempotentType.PARAM, timeout = 3, message = "文件上传中，请勿重复提交")    // 添加幂等注解 - 基于成果ID和文件名防重，10秒内不允许重复上传
    public R<AchievementFileDTO> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "成果ID") @RequestParam("achievementId") Long achievementId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("上传成果文件: achievementId={}, fileName={}, size={}, userId={}",
                achievementId, file.getOriginalFilename(), file.getSize(), userId);

        UploadFileDTO uploadDTO = UploadFileDTO.builder()
                .achievementId(achievementId)
                .uploadBy(userId)
                .build();

        AchievementFileDTO fileDTO = achievementFileService.uploadFile(file, uploadDTO);

        log.info("文件上传成功: fileId={}, url={}", fileDTO.getId(), fileDTO.getFileUrl());
        return R.ok(fileDTO, "文件上传成功");
    }


    /**
     * 批量上传成果文件
     * 批量上传多个文件到指定成果
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传成果文件", description = "为指定成果批量上传多个文件")
    @OperationLog(module = "成果文件管理", type = OperationType.UPLOAD,description = "批量上传成果文件", recordParams = false, recordResult = true)
    @SentinelResource(value = "knowledge:file:uploadBatch", blockHandler = "uploadBatchBlockHandler", fallback = "uploadBatchFallback")
    @Idempotent(type = IdempotentType.PARAM, timeout = 3, message = "文件上传中，请勿重复提交")
    public R<List<AchievementFileDTO>> uploadFilesBatch(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "成果ID") @RequestParam("achievementId") Long achievementId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("批量上传成果文件: achievementId={}, fileCount={}, userId={}", 
                achievementId, files.size(), userId);

        List<AchievementFileDTO> fileDTOs = achievementFileService.uploadFilesBatch(files, achievementId, userId);

        log.info("批量上传成功: fileCount={}", fileDTOs.size());
        return R.ok(fileDTOs, String.format("成功上传%d个文件", fileDTOs.size()));
    }


    /**
     * 查询成果的所有文件
     * 获取指定成果下的所有文件列表
     */
    @GetMapping("/{achievementId}/files")
    @Operation(summary = "查询成果文件列表", description = "获取指定成果下的所有文件")
    @OperationLog(module = "成果文件管理", type = OperationType.QUERY,description = "获取指定成果下的所有文件", recordParams = true, recordResult = true)
    public R<List<AchievementFileDTO>> getAchievementFiles(
            @Parameter(description = "成果ID") @PathVariable Long achievementId) {

        log.info("查询成果文件列表: achievementId={}", achievementId);

        List<AchievementFileDTO> files = achievementFileService.getFilesByAchievementId(achievementId);

        return R.ok(files, "查询成功");
    }


    /**
     * 删除成果文件
     * 删除指定的文件
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除成果文件", description = "删除指定的成果文件")
    @OperationLog(module = "成果文件管理", type = OperationType.DELETE,description = "删除指定成果的文件", recordParams = true, recordResult = true)
    @Idempotent(type = IdempotentType.SPEL, key = "#fileId", timeout = 3, message = "文件删除中，请勿重复操作")
    public R<Void> deleteAchievementFile(
            @Parameter(description = "文件ID")  @PathVariable Long fileId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("删除成果文件: fileId={}, userId={}", fileId, userId);

        achievementFileService.deleteFile(fileId, userId);

        log.info("文件删除成功: fileId={}", fileId);
        return R.ok(null, "文件删除成功");
    }


    /**
     * 获取文件下载URL
     * 生成临时的文件下载链接
     */
    @GetMapping("/{fileId}/download-url")
    @Operation(summary = "获取文件下载URL", description = "生成文件的临时下载链接")
    @OperationLog(module = "成果文件管理", type = OperationType.OTHER, description = "生成文件的临时下载链接", recordParams = false, recordResult = true)
    public R<String> getFileDownloadUrl(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @Parameter(description = "过期时间(秒)") @RequestParam(defaultValue = "3600") Integer expirySeconds
    ) {
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("获取文件下载URL: fileId={}, userId={}, expirySeconds={}", fileId, userId, expirySeconds);
        
        // 检查用户是否已登录
        if (userId == null) {
            log.error("用户未登录或token失效: fileId={}", fileId);
            return R.fail(401, "用户未登录或登录已过期，请重新登录");
        }

        try {
            String downloadUrl = achievementFileService.getFileDownloadUrl(fileId, userId, expirySeconds);
            log.info("文件下载URL生成成功: fileId={}, userId={}", fileId, userId);
            return R.ok(downloadUrl, "下载链接生成成功");
        } catch (Exception e) {
            log.error("获取文件下载URL失败: fileId={}, userId={}, error={}", fileId, userId, e.getMessage(), e);
            return R.fail(500, "获取下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 直接下载文件（流式下载）
     * 不使用预签名URL，直接通过后端代理下载
     * 支持通过查询参数传递access_token
     */
    @GetMapping("/{fileId}/download")
    @Operation(summary = "直接下载文件", description = "直接下载文件，不返回URL")
    @OperationLog(module = "成果文件管理", type = OperationType.DOWNLOAD, description = "直接下载文件", recordParams = true, recordResult = false)
    public void downloadFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @Parameter(description = "访问令牌（可选，也可通过Header传递）") @RequestParam(value = "access_token", required = false) String accessToken,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        Long userId = null;
        
        try {
            // 1. 尝试从安全上下文获取用户ID（通过Authorization header）
            try {
                userId = SecurityUtils.getUserId();
                log.info("从安全上下文获取到用户ID: {}", userId);
            } catch (Exception e) {
                log.debug("从安全上下文获取用户ID失败，尝试从查询参数获取token");
            }
            
            // 2. 如果安全上下文中没有用户信息，且提供了access_token查询参数，则手动验证token
            if (userId == null && accessToken != null && !accessToken.isEmpty()) {
                log.info("使用查询参数中的access_token进行认证");
                try {
                    // 使用JWT工具类验证token并提取用户ID
                    userId = extractUserIdFromToken(accessToken);
                    log.info("从access_token中提取到用户ID: {}", userId);
                } catch (Exception e) {
                    log.error("验证access_token失败: {}", e.getMessage());
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token验证失败或已过期，请重新登录");
                    return;
                }
            }
            
            // 3. 如果仍然没有获取到用户ID，返回401未授权
            if (userId == null) {
                log.warn("无法获取用户身份信息: fileId={}", fileId);
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("未授权访问，请先登录");
                return;
            }
            
            log.info("直接下载文件: fileId={}, userId={}", fileId, userId);
            achievementFileService.downloadFile(fileId, userId, response);
            
        } catch (Exception e) {
            log.error("文件下载失败: fileId={}, userId={}, error={}", fileId, userId, e.getMessage(), e);
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("文件下载失败: " + e.getMessage());
            } catch (java.io.IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        }
    }
    
    /**
     * 从JWT token中提取用户ID
     * @param token JWT token字符串
     * @return 用户ID
     */
    private Long extractUserIdFromToken(String token) {
        try {
            // 使用项目统一的JWT工具类解析token
            String userId = jwtUtils.parseToken(token);
            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("无法从token中提取用户ID");
            }
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.error("用户ID格式错误: {}", e.getMessage());
            throw new RuntimeException("用户ID格式无效", e);
        } catch (Exception e) {
            log.error("解析JWT token失败: {}", e.getMessage());
            throw new RuntimeException("Token无效或已过期", e);
        }
    }

    // ==================== Sentinel 限流和降级处理方法 ====================

    /**
     * 单文件上传限流处理
     */
    public R<AchievementFileDTO> uploadBlockHandler(MultipartFile file, Long achievementId, BlockException ex) {
        log.warn("[Sentinel] 文件上传被限流: achievementId={}, fileName={}, {}", 
                achievementId, file.getOriginalFilename(), ex.getClass().getSimpleName());
        return R.fail(429, "文件上传请求过于频繁，请稍后再试");
    }

    /**
     * 单文件上传降级处理（服务异常）
     */
    public R<AchievementFileDTO> uploadFallback(MultipartFile file, Long achievementId, Throwable throwable) {
        log.error("[Sentinel] 文件上传服务异常降级: achievementId={}, fileName={}", 
                achievementId, file.getOriginalFilename(), throwable);
        return R.fail(503, "文件上传服务暂时不可用，请稍后重试");
    }

    /**
     * 批量文件上传限流处理
     */
    public R<List<AchievementFileDTO>> uploadBatchBlockHandler(List<MultipartFile> files, Long achievementId, BlockException ex) {
        log.warn("[Sentinel] 批量文件上传被限流: achievementId={}, fileCount={}, {}", 
                achievementId, files.size(), ex.getClass().getSimpleName());
        return R.fail(429, "批量上传请求过于频繁，请稍后再试");
    }

    /**
     * 批量文件上传降级处理（服务异常）
     */
    public R<List<AchievementFileDTO>> uploadBatchFallback(List<MultipartFile> files, Long achievementId, Throwable throwable) {
        log.error("[Sentinel] 批量文件上传服务异常降级: achievementId={}, fileCount={}", 
                achievementId, files.size(), throwable);
        return R.fail(503, "批量上传服务暂时不可用，请稍后重试");
    }

}
