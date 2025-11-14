package hbnu.project.zhiyanproject.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyancommonoss.entity.FileUploadRequest;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 任务提交文件上传控制器
 * 处理任务提交相关的文件上传、删除等操作
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/zhiyan/projects/tasks/submissions/files")    // 原 /api/projects/tasks/submissions/files
@RequiredArgsConstructor
@Tag(name = "任务提交文件管理", description = "任务提交附件上传、删除等接口")
@SecurityRequirement(name = "Bearer Authentication")
@AccessLog("任务提交文件管理")
public class TaskSubmissionFileController {

    private final MinioService minioService;

    /**
     * 上传单个附件
     * 业务场景：任务提交时上传附件（支持各类文档、图片、压缩包等）
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传任务提交附件", description = "上传任务提交相关的附件文件")
    @OperationLog(module = "任务提交文件", type = OperationType.INSERT, description = "上传附件", recordParams = false, recordResult = true)
    public R<FileUploadRequest> uploadFile(
            @RequestParam("file") @Parameter(description = "文件", required = true) MultipartFile file) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("上传文件失败: 用户未登录");
            return R.fail("请先登录");
        }

        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }

        log.info("用户[{}]上传任务提交附件: filename={}, size={}", 
                userId, file.getOriginalFilename(), file.getSize());

        try {
            // 生成文件路径：submissions/{userId}/{date}/{uuid}_{filename}
            String objectKey = generateObjectKey(userId, file.getOriginalFilename());

            // 上传到MinIO
            FileUploadRequest result = minioService.uploadFile(file, BucketType.TASK_SUBMISSION, objectKey);

            log.info("任务提交附件上传成功: userId={}, url={}", userId, result.getUrl());
            return R.ok(result, "文件上传成功");

        } catch (Exception e) {
            log.error("上传任务提交附件失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传附件
     * 业务场景：一次性上传多个附件
     */
    @PostMapping("/upload-batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量上传任务提交附件", description = "一次性上传多个附件文件")
    @OperationLog(module = "任务提交文件", type = OperationType.INSERT, description = "批量上传附件", recordParams = false, recordResult = true)
    public R<List<FileUploadRequest>> uploadFiles(
            @RequestParam("files") @Parameter(description = "文件列表", required = true) MultipartFile[] files) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("批量上传文件失败: 用户未登录");
            return R.fail("请先登录");
        }

        if (files == null || files.length == 0) {
            return R.fail("文件列表不能为空");
        }

        log.info("用户[{}]批量上传任务提交附件: count={}", userId, files.length);

        List<FileUploadRequest> results = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                String objectKey = generateObjectKey(userId, file.getOriginalFilename());
                FileUploadRequest result = minioService.uploadFile(file, BucketType.TASK_SUBMISSION, objectKey);
                results.add(result);
                log.info("文件上传成功: {}", result.getUrl());

            } catch (Exception e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                failedFiles.add(file.getOriginalFilename());
            }
        }

        if (!failedFiles.isEmpty()) {
            log.warn("部分文件上传失败: {}", failedFiles);
            return R.ok(results, String.format("成功上传%d个文件，%d个文件上传失败", 
                    results.size(), failedFiles.size()));
        }

        log.info("批量上传完成: userId={}, successCount={}", userId, results.size());
        return R.ok(results, String.format("成功上传%d个文件", results.size()));
    }

    /**
     * 删除附件
     * 业务场景：撤回提交或修改提交时删除之前上传的附件
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除任务提交附件", description = "删除已上传的附件文件")
    @OperationLog(module = "任务提交文件", type = OperationType.DELETE, description = "删除附件", recordParams = true, recordResult = false)
    public R<Void> deleteFile(
            @RequestParam @Parameter(description = "文件URL", required = true) String fileUrl) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("删除文件失败: 用户未登录");
            return R.fail("请先登录");
        }

        log.info("用户[{}]删除任务提交附件: url={}", userId, fileUrl);

        try {
            // 从URL中提取objectKey
            String objectKey = extractObjectKeyFromUrl(fileUrl);

            // 验证文件路径是否属于当前用户（安全检查）
            if (!objectKey.startsWith("submissions/" + userId + "/")) {
                log.warn("用户[{}]尝试删除非本人上传的文件: {}", userId, objectKey);
                return R.fail("无权删除该文件");
            }

            // 删除文件
            minioService.deleteFile(BucketType.TASK_SUBMISSION, objectKey);

            log.info("任务提交附件删除成功: userId={}, objectKey={}", userId, objectKey);
            return R.ok(null, "文件删除成功");

        } catch (Exception e) {
            log.error("删除任务提交附件失败", e);
            return R.fail("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除附件
     */
    @DeleteMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量删除任务提交附件", description = "批量删除已上传的附件文件")
    @OperationLog(module = "任务提交文件", type = OperationType.DELETE, description = "批量删除附件", recordParams = true, recordResult = false)
    public R<Void> deleteFiles(
            @RequestBody @Parameter(description = "文件URL列表", required = true) List<String> fileUrls) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("批量删除文件失败: 用户未登录");
            return R.fail("请先登录");
        }

        if (fileUrls == null || fileUrls.isEmpty()) {
            return R.fail("文件URL列表不能为空");
        }

        log.info("用户[{}]批量删除任务提交附件: count={}", userId, fileUrls.size());

        List<String> objectKeys = new ArrayList<>();
        for (String fileUrl : fileUrls) {
            try {
                String objectKey = extractObjectKeyFromUrl(fileUrl);

                // 验证文件路径是否属于当前用户（安全检查）
                if (!objectKey.startsWith("submissions/" + userId + "/")) {
                    log.warn("用户[{}]尝试删除非本人上传的文件: {}", userId, objectKey);
                    continue;
                }

                objectKeys.add(objectKey);
            } catch (Exception e) {
                log.error("解析文件URL失败: {}", fileUrl, e);
            }
        }

        if (objectKeys.isEmpty()) {
            return R.fail("没有有效的文件可删除");
        }

        try {
            minioService.deleteFiles(BucketType.TASK_SUBMISSION, objectKeys);
            log.info("批量删除任务提交附件成功: userId={}, count={}", userId, objectKeys.size());
            return R.ok(null, String.format("成功删除%d个文件", objectKeys.size()));

        } catch (Exception e) {
            log.error("批量删除任务提交附件失败", e);
            return R.fail("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL（预签名URL，用于临时访问私有文件）
     * 业务场景：查看提交详情时，生成临时访问链接
     */
    @GetMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取文件临时访问URL", description = "生成文件的临时访问链接（有效期7天）")
    public R<String> getPresignedUrl(
            @RequestParam @Parameter(description = "文件URL", required = true) String fileUrl) {

        log.debug("生成文件预签名URL: {}", fileUrl);

        try {
            String objectKey = extractObjectKeyFromUrl(fileUrl);

            // 生成7天有效期的预签名URL
            String presignedUrl = minioService.getPresignedUrl(
                    BucketType.TASK_SUBMISSION, 
                    objectKey, 
                    7 * 24 * 60 * 60  // 7天
            );

            return R.ok(presignedUrl);

        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            return R.fail("生成访问链接失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件（代理下载，避免前端CORS问题）
     * 业务场景：前端点击下载按钮时，通过后端代理下载文件
     */
    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "下载文件", description = "下载任务提交的附件文件（代理下载）")
    public void downloadFile(
            @RequestParam @Parameter(description = "文件URL", required = true) String fileUrl,
            jakarta.servlet.http.HttpServletResponse response) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("下载文件失败: 用户未登录");
            response.setStatus(401);
            return;
        }

        log.info("用户[{}]下载文件: url={}", userId, fileUrl);

        try {
            // 从URL中提取objectKey
            String objectKey = extractObjectKeyFromUrl(fileUrl);
            
            log.info("下载文件: objectKey={}", objectKey);

            // 从objectKey中提取文件名
            String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
            
            // 下载文件
            java.io.InputStream inputStream = minioService.downloadFile(
                    BucketType.TASK_SUBMISSION, 
                    objectKey
            );

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));

            // 将文件流写入响应
            java.io.OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            inputStream.close();

            log.info("文件下载成功: userId={}, fileName={}", userId, fileName);

        } catch (Exception e) {
            log.error("下载文件失败", e);
            response.setStatus(500);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成对象键（文件存储路径）
     * 格式: submissions/{userId}/{date}/{uuid}_{filename}
     */
    private String generateObjectKey(Long userId, String originalFilename) {
        // 获取当前日期（用于分组）
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 生成唯一ID
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 构建路径
        return String.format("submissions/%d/%s/%s%s", userId, date, uuid, extension);
    }

    /**
     * 从URL中提取objectKey
     * 例如: http://localhost:9000/tasksubmission/submissions/123/20251108/abc.pdf
     * 提取: submissions/123/20251108/abc.pdf
     */
    private String extractObjectKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        // 查找桶名称后的路径部分
        String bucketName = BucketType.TASK_SUBMISSION.getBucketName();
        int bucketIndex = fileUrl.indexOf(bucketName);
        
        if (bucketIndex == -1) {
            throw new IllegalArgumentException("无效的文件URL");
        }

        // 提取objectKey（桶名称之后的部分）
        String objectKey = fileUrl.substring(bucketIndex + bucketName.length());
        
        // 去除前导斜杠
        if (objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
        }

        return objectKey;
    }
}
