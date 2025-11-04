package hbnu.project.zhiyancommonoss.util;

import hbnu.project.zhiyancommonbasic.exception.file.FileValidationException;
import hbnu.project.zhiyancommonbasic.utils.DateUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.file.FileTypeUtils;
import hbnu.project.zhiyancommonbasic.utils.file.FileUtils;
import hbnu.project.zhiyancommonbasic.utils.file.MimeTypeUtils;
import hbnu.project.zhiyancommonbasic.utils.id.IdUtils;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.properties.MinioProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;

/**
 * Minio对象存储工具
 * @author ErgouTree
 */
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioProperties minioProperties;

    /**
     * 根据桶类型获取桶名称
     */
    public String getBucketName(BucketType bucketType) {
        MinioProperties.BucketConfig buckets = minioProperties.getBuckets();
        switch (bucketType) {
            case ACHIEVEMENT_FILES:
                return buckets.getAchievementFiles();
            case WIKI_ASSETS:
                return buckets.getWikiAssets();
            case TEMP_UPLOADS:
                return buckets.getTempUploads();
            case PROJECT_COVERS:
                return buckets.getProjectCovers();
            case USER_AVATARS:
                return buckets.getUserAvatars();
            default:
                throw new IllegalArgumentException("未知的桶类型: " + bucketType);
        }
    }


    /**
     * 构建文件访问URL
     */
    public String getFileUrl(String bucketName, String objectKey) {
        return String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, objectKey);
    }

    /**
     * 校验文件
     */
    public void validateFile(MultipartFile file, String fileExtension, long fileSize) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new FileValidationException("文件不能为空");
        }

        // 检查文件名
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new FileValidationException("文件名不能为空");
        }

        if (!FileUtils.isValidFilename(Objects.requireNonNull(originalFilename))) {
            throw new FileValidationException("文件名包含非法字符");
        }

        // 检查文件大小
        MinioProperties.UploadConfig uploadConfig = minioProperties.getUpload();

        // 判断是否为图片
        boolean isImage = Arrays.asList(MimeTypeUtils.IMAGE_EXTENSION).contains(fileExtension.toLowerCase());

        long maxSize = isImage ? uploadConfig.getMaxImageSize() : uploadConfig.getMaxFileSize();
        if (fileSize > maxSize) {
            String maxSizeStr = FileTypeUtils.formatFileSize(maxSize);
            throw new FileValidationException("文件大小超过限制，最大允许: " + maxSizeStr);
        }

        // 检查文件类型（图片桶只能传图片）
        String[] allowedTypes = isImage ? uploadConfig.getAllowedImageTypes() : uploadConfig.getAllowedFileTypes();

        // 如果配置为 * 则允许所有类型
        if (allowedTypes != null && allowedTypes.length > 0 && !"*".equals(allowedTypes[0])) {
            boolean typeAllowed = Arrays.asList(allowedTypes).contains(fileExtension.toLowerCase());
            if (!typeAllowed) {
                throw new FileValidationException("不支持的文件类型: " + fileExtension);
            }
        }
    }


    /**
     * 生成对象键（包含项目和成果信息）
     * 用于成果文件上传
     *
     * @param projectId      项目ID
     * @param achievementId  成果ID
     * @param originalFilename 原始文件名
     * @return 对象键
     */
    public String buildAchievementObjectKey(Long projectId, Long achievementId,
                                             String originalFilename) {
        // 格式: project-{projectId}/achievement-{achievementId}_{filename}
        return String.format("project-%d/achievement-%d_%s",
                projectId, achievementId, originalFilename);
    }


    /**
     * 生成对象键（包含项目和文档信息）
     * 用于Wiki资源上传
     *
     * @param projectId    项目ID
     * @param documentId   文档ID
     * @param resourceType 资源类型（images/attachments）
     * @param filename     文件名
     * @return 对象键
     */
    public String buildWikiObjectKey(Long projectId, String documentId,
                                     String resourceType, String filename) {
        // 格式: project-{projectId}/{type}/{documentId}/{timestamp}_{filename}
        String timestamp = DateUtils.dateTimeNow();
        return String.format("project-%d/%s/%s/%s_%s",
                projectId, resourceType, documentId, timestamp, filename);
    }


    /**
     * 生成临时上传对象键
     *
     * @param userId   用户ID
     * @param filename 文件名
     * @return 对象键
     */
    public String buildTempObjectKey(Long userId, String filename) {
        // 格式: user-{userId}/{sessionId}/{uuid}_{filename}
        String sessionId = IdUtils.fastSimpleUUID();
        String uuid = IdUtils.fastSimpleUUID();
        return String.format("user-%d/%s/%s_%s", userId, sessionId, uuid, filename);
    }


    /**
     * 生成项目封面对象键
     *
     * @param projectId 项目ID
     * @param extension 文件扩展名
     * @return 对象键
     */
    public String buildProjectCoverObjectKey(Long projectId, String extension) {
        // 格式: original/project-{projectId}.{ext}
        return String.format("original/project-%d.%s", projectId, extension);
    }


    /**
     * 生成用户头像对象键
     *
     * @param userId    用户ID
     * @param extension 文件扩展名
     * @return 对象键
     */
    public String buildUserAvatarObjectKey(Long userId, String extension) {
        // 格式: original/user-{userId}.{ext}
        return String.format("original/user-%d.%s", userId, extension);
    }
}
