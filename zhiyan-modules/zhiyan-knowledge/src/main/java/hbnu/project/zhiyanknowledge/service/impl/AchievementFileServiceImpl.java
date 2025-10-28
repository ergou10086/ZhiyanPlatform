package hbnu.project.zhiyanknowledge.service.impl;


import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.file.FileTypeUtils;
import hbnu.project.zhiyancommonoss.entity.FileUploadRequest;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyancommonoss.util.MinioUtils;
import hbnu.project.zhiyanknowledge.mapper.AchievementConverter;
import hbnu.project.zhiyanknowledge.model.dto.AchievementFileDTO;
import hbnu.project.zhiyanknowledge.model.dto.FileContextDTO;
import hbnu.project.zhiyanknowledge.model.dto.UploadFileDTO;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.entity.AchievementFile;
import hbnu.project.zhiyanknowledge.repository.AchievementFileRepository;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.service.AchievementFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 成果文件服务实现
 * 成果上传的各种服务实现，数据写入到mysql，文件上传到minio等
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementFileServiceImpl implements AchievementFileService {

    @Autowired
    private final MinioService minioService;

    @Autowired
    private final MinioUtils minioUtils;

    @Autowired
    private final AchievementFileRepository achievementFileRepository;

    @Autowired
    private final AchievementRepository achievementRepository;

    private final AchievementConverter achievementConverter;

    /**
     * 默认预签名URL过期时间（3天）
     */
    private static final int DEFAULT_EXPIRY_SECONDS = 3 * 24 * 3600;

    /**
     * 上传成果文件
     *
     * @param file      文件
     * @param uploadDTO 上传DTO
     * @return 文件信息
     */
    @Override
    @Transactional
    public AchievementFileDTO uploadFile(MultipartFile file, UploadFileDTO uploadDTO) {
        log.info("开始上传成果文件: achievementId={}, fileName={}",
                uploadDTO.getAchievementId(), file.getOriginalFilename());

        // 1. 验证是否存在成果
        Achievement achievement = achievementRepository.findById(uploadDTO.getAchievementId())
                .orElseThrow(() -> new ServiceException("成果不存在"));

        // 2. 获取文件信息
        String originalFilename = file.getOriginalFilename();
        if(StringUtils.isEmpty(originalFilename)){
            throw new ServiceException("文件名不能为空");
        }

        String fileExtension = FileTypeUtils.getExtension(file);
        long fileSize = file.getSize();

        // 3. 检查是否存在同名文件，如果存在则删除旧文件（真正的覆盖）
        Optional<AchievementFile> existingFile = achievementFileRepository
                .findByAchievementIdAndFileName(uploadDTO.getAchievementId(), originalFilename);

        Integer newVersion = 1;
        if (existingFile.isPresent()) {
            AchievementFile oldFile = existingFile.get();
            log.info("检测到同名文件，执行覆盖删除: fileId={}, objectKey={}",
                    oldFile.getId(), oldFile.getObjectKey());

            // 先从 MinIO 删除旧文件
            try {
                minioService.deleteFile(BucketType.ACHIEVEMENT_FILES, oldFile.getObjectKey());
                log.info("MinIO旧文件删除成功: objectKey={}", oldFile.getObjectKey());
                // 再从数据库删除旧文件记录
                achievementFileRepository.deleteById(oldFile.getId());
                log.info("数据库旧文件记录删除成功: fileId={}", oldFile.getId());
            } catch (Exception e) {
                log.error("MinIO旧文件删除失败: objectKey={}", oldFile.getObjectKey(), e);
                // MinIO 删除失败不更新数据库
            }
        }

        // 4. 生成对象键（版本号固定为1，先不写版本管理）
        String objectKey = minioUtils.buildAchievementObjectKey(
                achievement.getProjectId(),
                uploadDTO.getAchievementId(),
                originalFilename
        );

        // 5. 上传到MinIO
        FileUploadRequest uploadResult;
        try {
            uploadResult = minioService.uploadFile(file, BucketType.ACHIEVEMENT_FILES, objectKey);
        } catch (ServiceException e) {
            log.error("文件上传MinIO失败", e);
            throw new ServiceException("文件上传失败: " + e.getMessage());
        }

        // 6. 保存文件记录到数据库
        AchievementFile achievementFile = AchievementFile.builder()
                .achievementId(uploadDTO.getAchievementId())
                .fileName(originalFilename)
                .fileSize(fileSize)
                .fileType(fileExtension)
                .bucketName(minioUtils.getBucketName(BucketType.ACHIEVEMENT_FILES))
                .objectKey(objectKey)
                .minioUrl(uploadResult.getUrl())
                .uploadBy(uploadDTO.getUploadBy())
                .uploadAt(LocalDateTime.now())
                .build();

        achievementFile = achievementFileRepository.save(achievementFile);

        log.info("文件上传成功: fileId={}, objectKey={}", achievementFile.getId(), objectKey);

        return achievementConverter.fileToDTO(achievementFile);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AchievementFileDTO> uploadFilesBatch(List<MultipartFile> files, Long achievementId, Long uploadBy) {
        if (files == null || files.isEmpty()) {
            throw new ServiceException("文件列表不能为空");
        }

        List<AchievementFileDTO> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                UploadFileDTO uploadDTO = UploadFileDTO.builder()
                        .achievementId(achievementId)
                        .uploadBy(uploadBy)
                        .build();

                AchievementFileDTO result = uploadFile(file, uploadDTO);
                results.add(result);
            } catch (Exception e) {
                log.error("批量上传文件出现失败: fileName={}", file.getOriginalFilename(), e);
                // 继续处理其他文件，不阻塞
            }
        }

        return results;
    }


    /**
     * 下载成果文件
     *
     * @param fileId 文件ID
     * @param userId 当前用户ID（用于权限验证）
     * @return 文件流
     */
    @Override
    public InputStream downloadFile(Long fileId, Long userId) {
        log.info("开始下载文件: fileId={}, userId={}", fileId, userId);

        // 1. 查询文件是否存在
        AchievementFile file = achievementFileRepository.findById(fileId)
                .orElseThrow(() -> new ServiceException("文件不存在"));

        // 2. 验证权限
        if (!hasFilePermission(fileId, userId)) {
            throw new ServiceException("无权限访问该文件");
        }

        // 3. 从MinIO下载文件
        try {
            InputStream stream = minioService.downloadFile(BucketType.ACHIEVEMENT_FILES, file.getObjectKey());
            log.info("文件下载成功: fileId={}", fileId);
            return stream;
        } catch (Exception e) {
            log.error("文件下载失败: fileId={}", fileId, e);
            throw new ServiceException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预签名下载URL
     *
     * @param fileId        文件ID
     * @param userId        当前用户ID
     * @param expirySeconds 过期时间（秒）
     * @return 预签名URL
     */
    @Override
    public String getFileDownloadUrl(Long fileId, Long userId, Integer expirySeconds) {
        log.info("生成文件下载链接: fileId={}, userId={}", fileId, userId);

        // 1. 查询文件是否存在
        AchievementFile file = achievementFileRepository.findById(fileId)
                .orElseThrow(() -> new ServiceException("文件不存在"));

        // 2. 验证权限
        if (!hasFilePermission(fileId, userId)) {
            throw new ServiceException("无权限访问该文件");
        }

        // 3. 生成预签名URL
        int expiry = expirySeconds != null ? expirySeconds : DEFAULT_EXPIRY_SECONDS;
        try{
            String url = minioService.getPresignedUrl(BucketType.ACHIEVEMENT_FILES, file.getObjectKey(), expiry);
            log.info("生成文件下载链接成功: fileId={}, expiry={}s", fileId, expiry);
            return url;
        }catch (ServiceException e) {
            log.error("生成文件下载链接失败: fileId={}", fileId, e);
            throw new ServiceException("生成下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param userId 当前用户ID（用于权限验证）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId, Long userId) {
        log.info("开始删除文件: fileId={}, userId={}", fileId, userId);

        // 1. 查询文件记录
        AchievementFile file = achievementFileRepository.findById(fileId)
                .orElseThrow(() -> new ServiceException("文件不存在"));

        // 2. 验证权限（不需要添加更严格的权限控制，项目组成员谁都能删除就得了）
        if (!hasFilePermission(fileId, userId)) {
            throw new ServiceException("无权限删除该文件");
        }

        // 3. 从MinIO删除文件
        try {
            minioService.deleteFile(BucketType.ACHIEVEMENT_FILES, file.getObjectKey());
            log.info("MinIO文件删除成功: objectKey={}", file.getObjectKey());
            // 4. 删除数据库记录
            achievementFileRepository.deleteById(fileId);
            log.info("文件删除成功: fileId={}", fileId);
        } catch (ServiceException e) {
            log.error("MinIO文件删除失败: objectKey={}", file.getObjectKey(), e);
            // MinIO删除失败，不继续删除数据库记录，避免悬空
        }
    }

    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID列表
     * @param userId  当前用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(List<Long> fileIds, Long userId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        for (Long fileId : fileIds) {
            try {
                deleteFile(fileId, userId);
            } catch (Exception e) {
                log.error("批量删除文件失败: fileId={}", fileId, e);
                // 继续处理其他文件
            }
        }
    }

    /**
     * 查询成果的所有文件
     *
     * @param achievementId 成果ID
     * @return 文件列表
     */
    @Override
    public List<AchievementFileDTO> getFilesByAchievementId(Long achievementId) {
        // 1. 查询该成果下的所有文件
        List<AchievementFile> files = achievementFileRepository.findByAchievementId(achievementId);

        // 返回空集合而非null
        if (CollectionUtils.isEmpty(files)) {
            return Collections.emptyList();
        }

        // 2. 转换为DTO列表
        return files.stream()
                .map(achievementFile -> achievementConverter.fileToDTO(achievementFile))
                .collect(Collectors.toList());
    }

    /**
     * 由id获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件dto
     */
    @Override
    public AchievementFileDTO getFileById(Long fileId) {
        AchievementFile achievementFile = achievementFileRepository.findById(fileId)
                .orElseThrow(() -> new ServiceException("文件不存在"));
        return achievementConverter.fileToDTO(achievementFile);
    }

    /**
     * 统计成果的文件数量
     *
     * @param achievementId 成果ID
     * @return 文件数量
     */
    @Override
    public long countFilesByAchievementId(Long achievementId) {
        return achievementFileRepository.countByAchievementId(achievementId);
    }

    /**
     * 检查用户是否有权限访问文件
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    @Override
    public boolean hasFilePermission(Long fileId, Long userId) {
        // TODO: 实现真实的权限验证逻辑
        // 1. 查询文件所属的成果
        // 2. 查询成果所属的项目
        // 3. 验证用户是否是项目成员
//
//        // 查询项目成员表进行更完善的权限验证
//        boolean isCreator = achievement.getCreatorId().equals(userId);
//
//        log.debug("权限检查: fileId={}, userId={}, isUploader={}, isCreator={}",
//                fileId, userId, isUploader, isCreator);
//
//        return isUploader || isCreator;
        return false;
    }

    /**
     * 获取文件上下文（用于 AI 对话）
     *
     * @param fileId 文件 ID
     * @return 文件上下文信息
     */
    @Override
    public hbnu.project.zhiyanknowledge.model.dto.FileContextDTO getFileContext(Long fileId) {
        log.info("[文件上下文] 获取文件信息: fileId={}", fileId);

        AchievementFile file = achievementFileRepository.findById(fileId)
                .orElse(null);

        if (file == null) {
            log.warn("[文件上下文] 文件不存在: fileId={}", fileId);
            return null;
        }

        // 生成预签名 URL
        String fileUrl = null;
        try {
            // 使用 MinioService 的 getPresignedUrl 方法
            BucketType bucketType = BucketType.ACHIEVEMENT_FILES;
            fileUrl = minioService.getPresignedUrl(
                    bucketType,
                    file.getObjectKey(),
                    DEFAULT_EXPIRY_SECONDS
            );
        } catch (Exception e) {
            log.error("[文件上下文] 生成文件 URL 失败: fileId={}", fileId, e);
            // 失败时使用 MinIO URL 作为备用
            fileUrl = file.getMinioUrl();
        }


        return FileContextDTO.builder()
                .fileId(String.valueOf(file.getId()))
                .achievementId(String.valueOf(file.getAchievementId()))
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .fileSizeFormatted(formatFileSize(file.getFileSize()))
                .fileUrl(fileUrl)
                .uploaderName(null) // TODO: 获取上传者姓名
                .uploadAt(file.getUploadAt())
                .extension(file.getFileType())  // fileType 就是扩展名
                .content(null) // TODO: 如果需要提取文件内容/摘要
                .build();
    }

    /**
     * 批量获取文件上下文（用于 AI 对话）
     *
     * @param fileIds 文件 ID 列表
     * @return 文件上下文列表
     */
    @Override
    public List<hbnu.project.zhiyanknowledge.model.dto.FileContextDTO> getFileContexts(List<Long> fileIds) {
        log.info("[文件上下文批量] 获取文件信息: fileIds={}, count={}", fileIds, fileIds.size());

        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<hbnu.project.zhiyanknowledge.model.dto.FileContextDTO> contexts = new ArrayList<>();

        for (Long fileId : fileIds) {
            hbnu.project.zhiyanknowledge.model.dto.FileContextDTO context = getFileContext(fileId);
            if (context != null) {
                contexts.add(context);
            }
        }

        log.info("[文件上下文批量] 成功获取 {} 个文件信息", contexts.size());
        return contexts;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null) {
            return null;
        }

        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
