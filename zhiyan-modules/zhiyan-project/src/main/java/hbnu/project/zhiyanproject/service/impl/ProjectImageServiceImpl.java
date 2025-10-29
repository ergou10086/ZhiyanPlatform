package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.file.FileUploadException;
import hbnu.project.zhiyancommonbasic.utils.file.FileTypeUtils;
import hbnu.project.zhiyancommonoss.entity.FileUploadRequest;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyanproject.model.dto.ImageUploadResponse;
import hbnu.project.zhiyanproject.service.ProjectImageService;
import hbnu.project.zhiyanproject.utils.ProjectSecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 项目图片服务实现类
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectImageServiceImpl implements ProjectImageService {

    private final MinioService minioService;
    private final hbnu.project.zhiyanproject.repository.ProjectRepository projectRepository;
    private final ProjectSecurityUtils projectSecurityUtils;

    /**
     * 允许的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /**
     * 最大文件大小：5MB
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public R<ImageUploadResponse> uploadProjectImage(MultipartFile file, Long projectId, Long userId) {
        try {
            // 1. 验证文件
            if (file == null || file.isEmpty()) {
                return R.fail("文件不能为空");
            }

            // 2. 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return R.fail("文件大小不能超过5MB");
            }

            // 3. 验证文件类型
            String fileExtension = FileTypeUtils.getExtension(file);
            if (!ALLOWED_IMAGE_TYPES.contains(fileExtension.toLowerCase())) {
                return R.fail("只支持以下图片格式: " + String.join(", ", ALLOWED_IMAGE_TYPES));
            }

            // 4. 生成文件存储路径
            String objectKey = generateObjectKey(projectId, userId, fileExtension);

            // 5. 上传到MinIO
            FileUploadRequest uploadResult = minioService.uploadFile(
                    file,
                    BucketType.PROJECT_COVERS,
                    objectKey
            );

            // 6. 如果提供了projectId，更新项目的imageUrl字段
            if (projectId != null) {
                try {
                    // ✅ 权限检查：只有项目拥有者才能上传项目图片
                    projectSecurityUtils.requireOwner(projectId);
                    
                    var project = projectRepository.findById(projectId);
                    if (project.isPresent()) {
                        var proj = project.get();
                        proj.setImageUrl(uploadResult.getUrl());
                        projectRepository.save(proj);
                        log.info("已更新项目[{}]的图片URL: {}", projectId, uploadResult.getUrl());
                    } else {
                        log.warn("项目[{}]不存在，无法更新图片URL", projectId);
                        return R.fail("项目不存在");
                    }
                } catch (SecurityException e) {
                    log.warn("用户[{}]无权上传项目[{}]的图片: {}", userId, projectId, e.getMessage());
                    return R.fail("只有项目创建者可以上传项目图片");
                } catch (Exception e) {
                    log.error("更新项目图片URL失败: projectId={}", projectId, e);
                    return R.fail("更新项目图片失败: " + e.getMessage());
                }
            }
            
            // 7. 构建响应
            ImageUploadResponse response = ImageUploadResponse.builder()
                    .imageUrl(uploadResult.getUrl())
                    .originalFilename(uploadResult.getFilename())
                    .fileSize(file.getSize())
                    .eTag(uploadResult.getETag())
                    .build();

            log.info("项目图片上传成功: userId={}, projectId={}, url={}", 
                    userId, projectId, uploadResult.getUrl());

            return R.ok(response, "图片上传成功");

        } catch (FileUploadException e) {
            log.error("项目图片上传失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("图片上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("项目图片上传异常: userId={}, projectId={}", userId, projectId, e);
            return R.fail("图片上传失败，请稍后重试");
        }
    }

    @Override
    public R<Void> deleteProjectImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return R.fail("图片URL不能为空");
            }

            // 从URL中提取对象键（路径）
            String objectKey = extractObjectKeyFromUrl(imageUrl);
            
            // 删除MinIO中的文件
            minioService.deleteFile(BucketType.PROJECT_COVERS, objectKey);

            log.info("项目图片删除成功: url={}", imageUrl);
            return R.ok(null, "图片删除成功");

        } catch (Exception e) {
            log.error("项目图片删除失败: url={}", imageUrl, e);
            return R.fail("图片删除失败: " + e.getMessage());
        }
    }

    /**
     * 生成对象存储键（路径）
     * 格式：年/月/日期_项目ID_UUID.扩展名
     * 例如：2025/01/20250123_1234_uuid.jpg
     */
    private String generateObjectKey(Long projectId, Long userId, String extension) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        String filename;
        if (projectId != null) {
            filename = String.format("%s_p%d_%s.%s", dateStr, projectId, uuid.substring(0, 8), extension);
        } else {
            filename = String.format("%s_u%d_%s.%s", dateStr, userId, uuid.substring(0, 8), extension);
        }
        
        return String.format("%s/%s/%s", year, month, filename);
    }

    /**
     * 从URL中提取对象键
     * 例如：http://localhost:9000/project-covers/2025/01/file.jpg -> 2025/01/file.jpg
     */
    private String extractObjectKeyFromUrl(String url) {
        // 简单实现：提取桶名称后面的路径
        String bucketName = BucketType.PROJECT_COVERS.getBucketName();
        int bucketIndex = url.indexOf(bucketName + "/");
        if (bucketIndex != -1) {
            return url.substring(bucketIndex + bucketName.length() + 1);
        }
        
        // 如果无法识别，返回URL的最后部分
        int lastSlash = url.lastIndexOf('/');
        return url.substring(lastSlash + 1);
    }
}

