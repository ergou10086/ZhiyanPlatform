package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.dto.AvatarDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.service.AvatarService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.exception.file.FileValidationException;
import hbnu.project.zhiyancommonbasic.utils.JsonUtils;
import hbnu.project.zhiyancommonbasic.utils.file.FileTypeUtils;
import hbnu.project.zhiyancommonoss.entity.FileUploadRequest;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.ImageService;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyancommonoss.util.MinioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 用户头像服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private final UserRepository userRepository;
    private final MinioService minioService;
    private final ImageService imageService;
    private final MinioUtils minioUtils;

    // 支持的图片尺寸
    private static final int[] THUMBNAIL_SIZES = {32, 64, 128, 256};

    // 最大头像文件大小：5MB
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;


    /**
     * 上传用户头像
     * 前端已完成裁剪，后端生成多尺寸缩略图
     *
     * @param userId 用户ID
     * @param file   图片文件（已裁剪）
     * @return 头像URL信息
     */
    @Override
    public R<AvatarDTO> uploadAvatar(Long userId, MultipartFile file) {
        try {
            log.info("开始上传用户头像: userId={}, filename={}", userId, file.getOriginalFilename());

            // 1. 验证用户是否存在
            Optional<User> userOpt = userRepository.findByIdAndIsDeletedFalse(userId);
            if (userOpt.isEmpty()) {
                return R.fail("用户不存在");
            }
            User user = userOpt.get();

            // 2. 验证文件情况
            validateAvatarFile(file);

            // 3. 删除旧头像（如果存在）
            deleteOldAvatar(userId);

            // 4. 获取文件扩展名
            String extension = FileTypeUtils.getExtension(file);

            // 5. 上传原图
            String originalObjectKey = minioUtils.buildUserAvatarObjectKey(userId, extension);
            FileUploadRequest originalUpload = minioService.uploadFile(
                    file,
                    BucketType.USER_AVATARS,
                    originalObjectKey
            );

            // 6.生成多尺寸的缩略图
            Map<String, String> thumbnailUrls = new HashMap<>();
            for (int size : THUMBNAIL_SIZES) {
                String thumbnailUrl = null;
                try {
                    thumbnailUrl = generateAndUploadThumbnail(userId, file.getInputStream(), size);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                thumbnailUrls.put(String.valueOf(size), thumbnailUrl);
            }

            // 7.构建头像信息JSON
            AvatarDTO avatarDTO = AvatarDTO.builder()
                    .minioUrl(originalUpload.getUrl())
                    .cdnUrl(originalUpload.getUrl())
                    .sizes(thumbnailUrls)
                    .build();
            
            log.info("构建AvatarDTO成功: minioUrl={}, cdnUrl={}, sizes={}", 
                avatarDTO.getMinioUrl(), avatarDTO.getCdnUrl(), avatarDTO.getSizes().keySet());

            // 8.更新用户头像URL字段（存储JSON）
            String avatarJson = JsonUtils.toJsonString(avatarDTO);
            user.setAvatarUrl(avatarJson);
            userRepository.save(user);

            log.info("用户头像上传成功: userId={}, originalUrl={}", userId, originalUpload.getUrl());
            R<AvatarDTO> result = R.ok(avatarDTO, "头像上传成功");
            log.info("准备返回响应: code={}, msg={}, data不为空={}", result.getCode(), result.getMsg(), result.getData() != null);
            return result;
        } catch (ServiceException e) {
            // 处理异常
            log.error("头像上传失败: userId={}", userId, e);
            return R.fail("头像上传失败: " + e.getMessage());
        } catch (Exception e) {
            // 捕获所有其他异常
            log.error("头像上传异常: userId={}, 异常信息: {}", userId, e.getMessage(), e);
            return R.fail("头像上传异常: " + e.getMessage());
        }
    }


    /**
     * 获取用户头像URL信息
     *
     * @param userId 用户ID
     * @return 头像URL信息
     */
    @Override
    @Transactional(readOnly = true)
    public R<AvatarDTO> getAvatarInfo(Long userId) {
        try{
            Optional<User> userOpt = userRepository.findByIdAndIsDeletedFalse(userId);
            if (userOpt.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = userOpt.get();
            String avatarUrl = user.getAvatarUrl();

            try{
                AvatarDTO avatarDTO = JsonUtils.parseObject(avatarUrl, AvatarDTO.class);
                return R.ok(avatarDTO);
            }catch (ServiceException e) {
                // 如果不是JSON格式（旧数据），构建简单的DTO
                AvatarDTO avatarDTO = AvatarDTO.builder()
                        .minioUrl(avatarUrl)
                        .cdnUrl(avatarUrl)
                        .sizes(new HashMap<>())
                        .build();
                return R.ok(avatarDTO);
            }
        }catch (ServiceException e) {
            log.error("获取用户头像信息失败: userId={}", userId, e);
            return R.fail("获取头像信息失败");
        }
    }


    /**
     * 验证头像文件
     */
    private void validateAvatarFile(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new FileValidationException("头像文件不能为空");
        }


        // 检查文件大小
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new FileValidationException("头像文件大小不能超过5MB");
        }


        // 检查文件类型
        String extension = FileTypeUtils.getExtension(file).toLowerCase();
        List<String> allowedTypes = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
        if (!allowedTypes.contains(extension)) {
            throw new FileValidationException("不支持这种格式的图片");
        }

        // 验证图片是否有效
        try {
            boolean isValid = imageService.validateImage(file.getInputStream());
            if (!isValid) {
                throw new FileValidationException("无效的图片文件");
            }
        } catch (Exception e) {
            throw new FileValidationException("图片验证失败: " + e.getMessage());
        }
    }


    /**
     * 生成并上传缩略图
     */
    private String generateAndUploadThumbnail(Long userId, InputStream originalStream, int size) {
        try {
            // 生成缩略图
            byte[] thumbnailBytes = imageService.generateThumbnail(
                    originalStream,
                    size,
                    size,
                    false
            );

            // 构建缩略图对象键
            String thumbnailObjectKey = String.format("thumbnail/user-%d_%d.jpg", userId, size);

            // 上传缩略图
            FileUploadRequest uploadRequest = minioService.uploadBytes(
                    thumbnailBytes,
                    BucketType.USER_AVATARS,
                    thumbnailObjectKey,
                    "image/jpeg"
            );
            return uploadRequest.getUrl();
        }catch (ServiceException e) {
            log.error("生成缩略图失败: userId={}, size={}", userId, size, e);
            throw new RuntimeException("生成缩略图失败", e);
        }
    }


    /**
     * 覆盖（删除）旧头像
     */
    private void deleteOldAvatar(Long userId) {
        try{
            // 删除原图（支持多种扩展名）
            String[] extensions = {"jpg", "jpeg", "png", "webp", "gif"};
            for (String extension : extensions) {
                String objectKey = minioUtils.buildUserAvatarObjectKey(userId, extension);
                if (minioService.fileExists(BucketType.USER_AVATARS, objectKey)) {
                    minioService.deleteFile(BucketType.USER_AVATARS, objectKey);
                }
            }

            // 删除所有尺寸的缩略图
            for (int size : THUMBNAIL_SIZES) {
                String thumbnailKey = String.format("thumbnail/user-%d_%d.jpg", userId, size);
                if (minioService.fileExists(BucketType.USER_AVATARS, thumbnailKey)) {
                    minioService.deleteFile(BucketType.USER_AVATARS, thumbnailKey);
                }
            }

            log.debug("旧头像删除成功: userId={}", userId);
        }catch (ServiceException e){
            log.warn("删除旧头像失败（可能不存在）: userId={}", userId, e);
            // 不抛出异常，允许继续执行，要不然容易悬空
        }
    }
}
