package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.exception.file.FileUploadException;
import hbnu.project.zhiyancommonbasic.utils.file.FileTypeUtils;
import hbnu.project.zhiyancommonoss.entity.FileUploadRequest;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.ImageService;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyanproject.model.dto.ImageUploadResponse;
import hbnu.project.zhiyanproject.service.ProjectImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

    private final ImageService imageService;

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


    /**
     * 上传项目图片
     *
     * @param file 图片文件
     * @param projectId 项目ID（可选，用于生成路径）
     * @param userId 用户ID
     * @return 图片上传结果
     */
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

            // 4. 验证图片有效性
            try (InputStream imageStream = file.getInputStream()) {
                if (!imageService.validateImage(imageStream)) {
                    return R.fail("图片文件无效或已损坏");
                }
            }

            // 5. 如果存在 projectId，检查并删除旧的封面图
            if (projectId != null) {
                deleteOldProjectCover(projectId);
            }

            // 6. 生成文件存储路径（按照 MinIO 桶设计规范）
            String objectKey = generateObjectKey(projectId, userId, fileExtension);

            // 7. 上传原图到 MinIO
            FileUploadRequest uploadResult = minioService.uploadFile(
                    file,
                    BucketType.PROJECT_COVERS,
                    objectKey
            );

            // 8. 可选：生成缩略图（用于列表展示等场景）
            generateThumbnails(file, projectId, userId, fileExtension);

            // 9. 构建响应
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
        } catch (ServiceException | IOException e) {
            log.error("项目图片上传异常: userId={}, projectId={}", userId, projectId, e);
            return R.fail("图片上传失败，请稍后重试");
        }
    }



    /**
     * 删除项目图片
     *
     * @param imageUrl 图片URL
     * @return 删除结果
     */
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

        } catch (ServiceException e) {
            log.error("项目图片删除失败: url={}", imageUrl, e);
            return R.fail("图片删除失败: " + e.getMessage());
        }
    }


    /**
     * 生成对象存储键（路径）
     * 按照 MinIO 桶设计规范：original/project-{project_id}.{ext}
     * 如果没有 projectId（临时上传），则使用用户ID
     *
     * @param projectId 项目ID（可选）
     * @param userId 用户ID
     * @param extension 文件扩展名
     * @return 对象键
     */
    private String generateObjectKey(Long projectId, Long userId, String extension) {
        if (projectId != null) {
            // 项目封面图：original/project-{project_id}.{ext}
            return String.format("original/project-%d.%s", projectId, extension);
        } else {
            // 临时上传（用于创建项目前预览）：temp/user-{user_id}-{timestamp}.{ext}
            long timestamp = System.currentTimeMillis();
            return String.format("temp/user-%d-%d.%s", userId, timestamp, extension);
        }
    }


    /**
     * 从URL中提取对象键
     * 例如：<a href="http://localhost:9000/project-covers/original/project-123.jpg">...</a>
     *      -> original/project-123.jpg
     *
     * @param url 完整URL
     * @return 对象键
     */
    private String extractObjectKeyFromUrl(String url) {
        String bucketName = BucketType.PROJECT_COVERS.getBucketName();
        int bucketIndex = url.indexOf(bucketName + "/");

        if (bucketIndex != -1) {
            return url.substring(bucketIndex + bucketName.length() + 1);
        }

        // 如果无法识别桶名，尝试提取路径部分
        int pathStart = url.indexOf("/", url.indexOf("//") + 2);
        if (pathStart != -1) {
            String path = url.substring(pathStart + 1);
            // 移除桶名
            if (path.startsWith(bucketName + "/")) {
                return path.substring(bucketName.length() + 1);
            }
            return path;
        }

        throw new IllegalArgumentException("无法从URL中提取对象键: " + url);
    }



    /**
     * 删除旧的项目封面图
     * 根据项目ID查找并删除所有相关的封面图文件
     *
     * @param projectId 项目ID
     */
    private void deleteOldProjectCover(Long projectId) {
        try{
            // 列出该项目的所有封面图
            String prefix = "original/project-" + projectId + ".";
            List<String> oldFiles = minioService.listFiles(BucketType.PROJECT_COVERS, prefix);

            // 删除所有旧文件
            if (!oldFiles.isEmpty()) {
                minioService.deleteFiles(BucketType.PROJECT_COVERS, oldFiles);
                log.info("删除旧项目封面图: projectId={}, count={}", projectId, oldFiles.size());
            }
        }catch (ServiceException e){
            log.warn("删除旧项目封面图失败: projectId={}", projectId, e);
            // 不抛出异常，允许继续上传新图片
        }
    }

    /**
     * 生成缩略图（可选功能）
     * 用于列表展示、预览等场景
     *
     * @param file 原始文件
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param extension 文件扩展名
     * @return 缩略图URL映射（尺寸 -> URL）
     */
    private Map<String, String> generateThumbnails(MultipartFile file, Long projectId,
                                                   Long userId, String extension) {
        Map<String, String> thumbnails = new HashMap<>();

        // 定义需要生成的缩略图尺寸
        Map<String, int[]> sizes = new HashMap<>();
        sizes.put("small", new int[]{200, 150});   // 列表缩略图
        sizes.put("medium", new int[]{400, 300});  // 卡片展示
        sizes.put("large", new int[]{800, 600});   // 详情页

        try (InputStream originalStream = file.getInputStream()) {
            for (Map.Entry<String, int[]> entry : sizes.entrySet()) {
                String sizeName = entry.getKey();
                int[] dimensions = entry.getValue();

                // 生成缩略图
                byte[] thumbnailBytes = imageService.generateThumbnail(
                        originalStream,
                        dimensions[0],
                        dimensions[1],
                        true  // 保持宽高比
                );

                // 生成缩略图存储路径
                String thumbnailKey = generateThumbnailKey(projectId, userId, sizeName, extension);

                // 上传缩略图
                FileUploadRequest uploadResult = minioService.uploadBytes(
                        thumbnailBytes,
                        BucketType.PROJECT_COVERS,
                        thumbnailKey,
                        "image/jpeg"
                );

                thumbnails.put(sizeName, uploadResult.getUrl());

                // 重置流位置以供下一次使用
                originalStream.reset();
            }

            log.info("成功生成项目封面缩略图: projectId={}, count={}", projectId, thumbnails.size());

        } catch (Exception e) {
            log.error("生成缩略图失败: projectId={}", projectId, e);
            // 不抛出异常，缩略图生成失败不影响原图上传
        }

        return thumbnails;
    }


    /**
     * 生成缩略图存储键
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param sizeName 尺寸名称
     * @param extension 文件扩展名
     * @return 缩略图存储键
     */
    private String generateThumbnailKey(Long projectId, Long userId, String sizeName, String extension) {
        if (projectId != null) {
            return String.format("thumbnail/project-%d-%s.jpg", projectId, sizeName);
        } else {
            long timestamp = System.currentTimeMillis();
            return String.format("temp/user-%d-%d-%s.jpg", userId, timestamp, sizeName);
        }
    }
}

