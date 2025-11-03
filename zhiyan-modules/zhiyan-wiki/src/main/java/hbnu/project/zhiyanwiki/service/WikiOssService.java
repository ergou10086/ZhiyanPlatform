package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.file.FileTypeUtils;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyanwiki.model.dto.WikiAttachmentDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiAttachmentQueryDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiAttachmentUploadDTO;
import hbnu.project.zhiyanwiki.model.entity.WikiAttachment;
import hbnu.project.zhiyanwiki.model.enums.AttachmentType;
import hbnu.project.zhiyanwiki.repository.WikiAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * wiki页面的对象存储服务类
 * 让wiki页面的图片能够上传到 MinIO 中，使得wiki页面支持图片上传
 *
 * @author ErgouTree
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiOssService {

    private final MinioService minioService;
    private final WikiAttachmentRepository attachmentRepository;

    // Wiki附件使用的桶类型
    private static final BucketType WIKI_BUCKET_TYPE = BucketType.WIKI_ASSETS;

    // 支持的图片格式
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );

    /**
     * 上传单个附件
     *
     * @param file      文件
     * @param uploadDTO 上传配置
     * @return 附件DTO
     */
    @Transactional
    public WikiAttachmentDTO uploadAttachment(MultipartFile file, WikiAttachmentUploadDTO uploadDTO) {
        try {
            // 验证文件
            validateFile(file);

            // 生成对象键（存储路径）
            String objectKey = generateObjectKey(
                    uploadDTO.getProjectId(),
                    uploadDTO.getWikiPageId(),
                    file.getOriginalFilename()
            );

            // 获取文件信息
            String originalFilename = file.getOriginalFilename();
            String fileExtension = FileTypeUtils.getExtension(file);
            long fileSize = file.getSize();

            // 确定附件类型
            AttachmentType attachmentType = determineAttachmentType(fileExtension, uploadDTO.getAttachmentType());

            // 上传到MinIO
            var uploadResult = minioService.uploadFile(file, WIKI_BUCKET_TYPE, objectKey);

            // 保存附件元数据到数据库
            WikiAttachment attachment = WikiAttachment.builder()
                    .wikiPageId(uploadDTO.getWikiPageId())
                    .projectId(uploadDTO.getProjectId())
                    .attachmentType(attachmentType)
                    .fileName(originalFilename)
                    .fileSize(fileSize)
                    .fileType(fileExtension)
                    .bucketName(WIKI_BUCKET_TYPE.name().toLowerCase())
                    .objectKey(objectKey)
                    .fileUrl(uploadResult.getUrl())
                    .description(uploadDTO.getDescription())
                    .uploadBy(uploadDTO.getUploadBy())
                    .uploadAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();

            attachment = attachmentRepository.save(attachment);

            log.info("Wiki附件上传成功: attachmentId={}, fileName={}, size={}", 
                    attachment.getId(), originalFilename, fileSize);

            return convertToDTO(attachment);

        } catch (Exception e) {
            log.error("Wiki附件上传失败: projectId={}, wikiPageId={}", 
                    uploadDTO.getProjectId(), uploadDTO.getWikiPageId(), e);
            throw new ServiceException("附件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传附件
     *
     * @param files     文件列表
     * @param uploadDTO 上传配置
     * @return 附件DTO列表
     */
    @Transactional
    public List<WikiAttachmentDTO> uploadAttachments(MultipartFile[] files, WikiAttachmentUploadDTO uploadDTO) {
        List<WikiAttachmentDTO> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                WikiAttachmentDTO dto = uploadAttachment(file, uploadDTO);
                results.add(dto);
            } catch (Exception e) {
                log.error("批量上传中某文件失败: fileName={}", file.getOriginalFilename(), e);
                // 继续处理其他文件
            }
        }

        log.info("批量上传完成: 总数={}, 成功={}", files.length, results.size());
        return results;
    }

    /**
     * 获取Wiki页面的所有附件
     *
     * @param wikiPageId Wiki页面ID
     * @return 附件列表
     */
    public List<WikiAttachmentDTO> getPageAttachments(Long wikiPageId) {
        List<WikiAttachment> attachments = attachmentRepository.findByWikiPageIdAndIsDeletedFalse(wikiPageId);
        return attachments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取Wiki页面的图片列表
     *
     * @param wikiPageId Wiki页面ID
     * @return 图片附件列表
     */
    public List<WikiAttachmentDTO> getPageImages(Long wikiPageId) {
        List<WikiAttachment> attachments = attachmentRepository
                .findByWikiPageIdAndAttachmentTypeAndIsDeletedFalse(wikiPageId, AttachmentType.IMAGE);
        return attachments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取Wiki页面的文件列表
     *
     * @param wikiPageId Wiki页面ID
     * @return 文件附件列表
     */
    public List<WikiAttachmentDTO> getPageFiles(Long wikiPageId) {
        List<WikiAttachment> attachments = attachmentRepository
                .findByWikiPageIdAndAttachmentTypeAndIsDeletedFalse(wikiPageId, AttachmentType.FILE);
        return attachments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询项目的附件（分页+高级筛选）
     *
     * @param queryDTO 查询条件
     * @return 附件分页结果
     */
    public Page<WikiAttachmentDTO> queryAttachments(WikiAttachmentQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(
                queryDTO.getPage(),
                queryDTO.getSize(),
                Sort.Direction.fromString(queryDTO.getSortDirection()),
                queryDTO.getSortBy()
        );

        Page<WikiAttachment> attachmentPage;

        // 根据不同条件查询
        if (queryDTO.getFileName() != null && !queryDTO.getFileName().isEmpty()) {
            // 文件名模糊查询
            attachmentPage = attachmentRepository.findByProjectIdAndFileNameContainingAndIsDeletedFalse(
                    queryDTO.getProjectId(), queryDTO.getFileName(), pageable);
        } else if (queryDTO.getAttachmentType() != null && !queryDTO.getAttachmentType().isEmpty()) {
            // 按类型查询
            AttachmentType type = AttachmentType.valueOf(queryDTO.getAttachmentType().toUpperCase());
            attachmentPage = attachmentRepository.findByProjectIdAndAttachmentTypeAndIsDeletedFalse(
                    queryDTO.getProjectId(), type, pageable);
        } else {
            // 查询全部
            attachmentPage = attachmentRepository.findByProjectIdAndIsDeletedFalse(
                    queryDTO.getProjectId(), pageable);
        }

        return attachmentPage.map(this::convertToDTO);
    }

    /**
     * 获取附件详情
     *
     * @param attachmentId 附件ID
     * @return 附件DTO
     */
    public WikiAttachmentDTO getAttachment(Long attachmentId) {
        WikiAttachment attachment = attachmentRepository.findByIdAndIsDeletedFalse(attachmentId)
                .orElseThrow(() -> new ServiceException("附件不存在或已被删除"));
        return convertToDTO(attachment);
    }

    /**
     * 下载附件（返回文件流）
     *
     * @param attachmentId 附件ID
     * @return 文件输入流
     */
    public InputStream downloadAttachment(Long attachmentId) {
        WikiAttachment attachment = attachmentRepository.findByIdAndIsDeletedFalse(attachmentId)
                .orElseThrow(() -> new ServiceException("附件不存在或已被删除"));

        try {
            InputStream inputStream = minioService.downloadFile(WIKI_BUCKET_TYPE, attachment.getObjectKey());
            log.info("附件下载: attachmentId={}, fileName={}", attachmentId, attachment.getFileName());
            return inputStream;
        } catch (Exception e) {
            log.error("附件下载失败: attachmentId={}", attachmentId, e);
            throw new ServiceException("附件下载失败: " + e.getMessage());
        }
    }

    /**
     * 软删除附件（标记为删除，不从MinIO删除）
     *
     * @param attachmentId 附件ID
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        WikiAttachment attachment = attachmentRepository.findByIdAndIsDeletedFalse(attachmentId)
                .orElseThrow(() -> new ServiceException("附件不存在或已被删除"));

        attachment.setIsDeleted(true);
        attachment.setDeletedAt(LocalDateTime.now());
        attachmentRepository.save(attachment);

        log.info("附件软删除成功: attachmentId={}, fileName={}", attachmentId, attachment.getFileName());
    }

    /**
     * 物理删除附件（从数据库和MinIO彻底删除）
     *
     * @param attachmentId 附件ID
     */
    @Transactional
    public void deleteAttachmentPermanently(Long attachmentId) {
        WikiAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ServiceException("附件不存在"));

        try {
            // 从MinIO删除
            minioService.deleteFile(WIKI_BUCKET_TYPE, attachment.getObjectKey());

            // 从数据库删除
            attachmentRepository.delete(attachment);

            log.info("附件物理删除成功: attachmentId={}, fileName={}", attachmentId, attachment.getFileName());
        } catch (Exception e) {
            log.error("附件物理删除失败: attachmentId={}", attachmentId, e);
            throw new ServiceException("附件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除Wiki页面的所有附件（物理删除）
     *
     * @param wikiPageId Wiki页面ID
     */
    @Transactional
    public void deletePageAttachments(Long wikiPageId) {
        List<WikiAttachment> attachments = attachmentRepository.findByWikiPageIdAndIsDeletedFalse(wikiPageId);

        for (WikiAttachment attachment : attachments) {
            try {
                // 从MinIO删除
                minioService.deleteFile(WIKI_BUCKET_TYPE, attachment.getObjectKey());

                // 从数据库删除
                attachmentRepository.delete(attachment);

                log.debug("删除附件: attachmentId={}, fileName={}", attachment.getId(), attachment.getFileName());
            } catch (Exception e) {
                log.error("删除附件失败: attachmentId={}", attachment.getId(), e);
                // 继续删除其他附件
            }
        }

        log.info("批量删除Wiki页面附件完成: wikiPageId={}, count={}", wikiPageId, attachments.size());
    }

    /**
     * 获取项目的附件统计信息
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    public Map<String, Object> getProjectAttachmentStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();

        // 统计总数
        long totalCount = attachmentRepository.countByProjectIdAndIsDeletedFalse(projectId);
        stats.put("totalCount", totalCount);

        // 统计图片数量
        Page<WikiAttachment> images = attachmentRepository.findByProjectIdAndAttachmentTypeAndIsDeletedFalse(
                projectId, AttachmentType.IMAGE, PageRequest.of(0, 1));
        stats.put("imageCount", images.getTotalElements());

        // 统计文件数量
        Page<WikiAttachment> files = attachmentRepository.findByProjectIdAndAttachmentTypeAndIsDeletedFalse(
                projectId, AttachmentType.FILE, PageRequest.of(0, 1));
        stats.put("fileCount", files.getTotalElements());

        // 统计总大小
        long totalSize = attachmentRepository.sumFileSizeByProjectId(projectId);
        stats.put("totalSize", totalSize);
        stats.put("totalSizeFormatted", formatFileSize(totalSize));

        // 获取最近上传的附件
        List<WikiAttachment> recentAttachments = attachmentRepository.findRecentAttachments(
                projectId, PageRequest.of(0, 5));
        stats.put("recentAttachments", recentAttachments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return stats;
    }

    /**
     * 验证上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new ServiceException("文件名不能为空");
        }

        long fileSize = file.getSize();
        if (fileSize == 0) {
            throw new ServiceException("文件大小为0");
        }

        // 限制文件大小（例如：最大100MB）
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (fileSize > maxSize) {
            throw new ServiceException("文件大小超过限制（最大100MB）");
        }
    }

    /**
     * 生成MinIO对象键（存储路径）
     * 格式：wiki/{projectId}/{wikiPageId}/{timestamp}_{uuid}_{filename}
     */
    private String generateObjectKey(Long projectId, Long wikiPageId, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String safeFilename = sanitizeFilename(originalFilename);
        
        return String.format("wiki/%d/%d/%s_%s_%s",
                projectId, wikiPageId, timestamp, uuid, safeFilename);
    }

    /**
     * 清理文件名（移除非法字符）
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown";
        }
        // 替换非法字符为下划线
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 确定附件类型
     */
    private AttachmentType determineAttachmentType(String fileExtension, String userSpecifiedType) {
        // 如果用户指定了类型，优先使用
        if (userSpecifiedType != null && !userSpecifiedType.isEmpty()) {
            try {
                return AttachmentType.valueOf(userSpecifiedType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的附件类型: {}, 将自动判断", userSpecifiedType);
            }
        }

        // 根据文件扩展名自动判断
        if (fileExtension != null && IMAGE_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            return AttachmentType.IMAGE;
        }

        return AttachmentType.FILE;
    }

    /**
     * 转换实体为DTO
     */
    private WikiAttachmentDTO convertToDTO(WikiAttachment attachment) {
        return WikiAttachmentDTO.builder()
                .id(String.valueOf(attachment.getId()))
                .wikiPageId(String.valueOf(attachment.getWikiPageId()))
                .projectId(String.valueOf(attachment.getProjectId()))
                .attachmentType(attachment.getAttachmentType().name())
                .fileName(attachment.getFileName())
                .fileSize(attachment.getFileSize())
                .fileSizeFormatted(formatFileSize(attachment.getFileSize()))
                .fileType(attachment.getFileType())
                .fileUrl(attachment.getFileUrl())
                .description(attachment.getDescription())
                .uploadBy(String.valueOf(attachment.getUploadBy()))
                .uploadAt(attachment.getUploadAt())
                .build();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
