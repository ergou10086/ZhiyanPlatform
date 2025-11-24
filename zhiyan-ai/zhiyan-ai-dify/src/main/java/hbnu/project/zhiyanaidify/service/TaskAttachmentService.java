package hbnu.project.zhiyanaidify.service;

import hbnu.project.zhiyanaidify.model.response.DifyFileUploadResponse;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 任务附件处理服务
 * 负责从MinIO下载任务附件并上传到Dify
 *
 * @author ErgouTree
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAttachmentService {

    private final MinioService minioService;
    private final DifyFileService difyFileService;

    /**
     * 从MinIO下载任务附件并上传到Dify
     *
     * @param attachmentUrls 附件URL列表
     * @param userId 用户ID
     * @return Dify文件ID列表
     */
    public List<String> downloadAndUploadAttachments(List<String> attachmentUrls, Long userId) {
        if (attachmentUrls == null || attachmentUrls.isEmpty()) {
            log.debug("附件URL列表为空，跳过处理");
            return Collections.emptyList();
        }

        List<String> difyFileIds = new ArrayList<>();

        try {
            log.info("解析到{}个附件URL", attachmentUrls.size());

            // 遍历每个附件URL
            for (String url : attachmentUrls) {
                try {
                    log.info("开始处理附件: url={}", url);

                    // 3. 从MinIO下载文件
                    InputStream fileStream = downloadFileFromMinio(url);
                    String fileName = extractFileNameFromUrl(url);

                    // 4. 转换为MultipartFile
                    MultipartFile multipartFile = convertToMultipartFile(fileStream, fileName);

                    // 5. 上传到Dify
                    DifyFileUploadResponse response = difyFileService.uploadFile(multipartFile, userId);

                    if (response != null && response.getFileId() != null) {
                        difyFileIds.add(response.getFileId());
                        log.info("✅ 附件上传到Dify成功: fileName={}, difyFileId={}",
                                fileName, response.getFileId());
                    } else {
                        log.warn("⚠️ 附件上传到Dify失败，响应为空: fileName={}", fileName);
                    }

                } catch (Exception e) {
                    log.error("❌ 处理附件失败: url={}", url, e);
                    // 继续处理下一个文件，不中断整个流程
                }
            }

            log.info("附件处理完成: 总数={}, 成功上传到Dify={}", attachmentUrls.size(), difyFileIds.size());

        } catch (Exception e) {
            log.error("下载并上传附件失败", e);
            throw new RuntimeException("处理任务附件失败: " + e.getMessage(), e);
        }

        return difyFileIds;
    }

    /**
     * 从MinIO下载文件
     */
    private InputStream downloadFileFromMinio(String fileUrl) {
        // 从URL提取objectKey
        String objectKey = extractObjectKeyFromUrl(fileUrl);
        log.debug("从MinIO下载文件: objectKey={}", objectKey);

        // 使用MinioService下载文件
        return minioService.downloadFile(BucketType.TASK_SUBMISSION, objectKey);
    }

    /**
     * 从URL中提取objectKey
     * 例如: http://localhost:9000/tasksubmission/submissions/123/20251108/abc.pdf
     * 提取: submissions/123/20251108/abc.pdf
     */
    private String extractObjectKeyFromUrl(String fileUrl) {
        String bucketName = BucketType.TASK_SUBMISSION.getBucketName();
        int bucketIndex = fileUrl.indexOf(bucketName);

        if (bucketIndex == -1) {
            throw new IllegalArgumentException("无效的文件URL: " + fileUrl);
        }

        String objectKey = fileUrl.substring(bucketIndex + bucketName.length());
        return objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * 将InputStream转换为MultipartFile
     */
    private MultipartFile convertToMultipartFile(InputStream inputStream, String fileName)
            throws IOException {
        byte[] bytes = inputStream.readAllBytes();

        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                // 根据文件扩展名判断类型
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                return switch (extension) {
                    case "pdf" -> "application/pdf";
                    case "doc", "docx" -> "application/msword";
                    case "xls", "xlsx" -> "application/vnd.ms-excel";
                    case "png" -> "image/png";
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "txt" -> "text/plain";
                    default -> "application/octet-stream";
                };
            }

            @Override
            public boolean isEmpty() {
                return bytes.length == 0;
            }

            @Override
            public long getSize() {
                return bytes.length;
            }

            @Override
            public byte[] getBytes() {
                return bytes;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.write(dest.toPath(), bytes);
            }
        };
    }
}
