package hbnu.project.zhiyanknowledge.service.impl;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyancommonoss.util.MinioUtils;
import hbnu.project.zhiyanknowledge.mapper.AchievementConverter;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.entity.AchievementFile;
import hbnu.project.zhiyanknowledge.model.entity.FileUploadSession;
import hbnu.project.zhiyanknowledge.repository.AchievementFileRepository;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.repository.FileUploadSessionRepository;
import hbnu.project.zhiyanknowledge.service.ChunkedUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分片上传服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkedUploadServiceImpl implements ChunkedUploadService {

    @Autowired
    private final MinioService minioService;

    @Autowired
    private final MinioUtils minioUtils;

    @Autowired
    private final FileUploadSessionRepository sessionRepository;

    @Autowired
    private final AchievementRepository achievementRepository;

    @Autowired
    private final AchievementFileRepository fileRepository;

    private final AchievementConverter achievementConverter;

    // 分片大小阈值：超过此大小才使用分片上传
    private static final long MULTIPART_THRESHOLD = 30 * 1024 * 1024; // 30MB

    /**
     * 初始化分片上传
     *
     * @param dto    初始化请求
     * @param userId 用户ID
     * @return 上传会话信息
     */
    @Override
    @Transactional
    public UploadSessionDTO initiateUpload(InitiateUploadDTO dto, Long userId) {
        log.info("初始化分片上传: achievementId={}, fileName={}, fileSize={}, userId={}",
                dto.getAchievementId(), dto.getFileName(), dto.getFileSize(), userId);

        // 1.验证成果的存在性
        Achievement achievement = achievementRepository.findById(dto.getAchievementId())
                .orElseThrow(() -> new ServiceException("成果不存在"));

        // 2. 检查文件大小是否需要分片上传
        if (dto.getFileSize() < MULTIPART_THRESHOLD) {
            throw new ServiceException("文件大小小于30MB，请使用普通上传接口");
        }

        // 3. 生成对象键
        String objectKey = minioUtils.buildAchievementObjectKey(
                achievement.getProjectId(),
                dto.getAchievementId(),
                dto.getFileName()
        );

        // 4. 计算分片信息
        int chunkSize = dto.getChunkSize();
        int totalChunks = (int) Math.ceil((double) dto.getFileSize() / chunkSize);

        // 5. 初始化MinIO分片上传
        String bucketName = minioUtils.getBucketName(BucketType.ACHIEVEMENT_FILES);
        String uploadId = minioService.initiateMultipartUpload(bucketName, objectKey);

        // 6. 创建上传会话记录
        FileUploadSession session = FileUploadSession.builder()
                .uploadId(uploadId)
                .achievementId(dto.getAchievementId())
                .fileName(dto.getFileName())
                .fileSize(dto.getFileSize())
                .chunkSize(chunkSize)
                .totalChunks(totalChunks)
                .objectKey(objectKey)
                .bucketName(bucketName)
                .uploadBy(userId)
                .status(FileUploadSession.UploadStatus.IN_PROGRESS)
                .build();

        session.setUploadedChunks(new ArrayList<>());
        session = sessionRepository.save(session);

        log.info("分片上传初始化成功: sessionId={}, uploadId={}, totalChunks={}",
                session.getId(), uploadId, totalChunks);

        return achievementConverter.toUploadSessionDTO(session);
    }

    /**
     * 上传分片
     *
     * @param uploadId    上传ID
     * @param chunkNumber 分片号
     * @param chunkData   分片数据
     * @param chunkSize   分片大小
     * @param userId      用户ID
     * @return 上传会话信息(包含进度)
     */
    @Override
    public UploadSessionDTO uploadChunk(String uploadId, Integer chunkNumber, InputStream chunkData, Long chunkSize, Long userId) {
        log.info("上传分片: uploadId={}, chunkNumber={}, size={}", uploadId, chunkNumber, chunkSize);

        // 1. 查询上传会话
        FileUploadSession session = sessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new ServiceException("上传会话不存在"));

        // 2. 验证权限
        if(!session.getUploadBy().equals(userId)){
            throw new ServiceException("无权限操作此上传会话");
        }

        // 3. 验证状态
        if (session.getStatus() != FileUploadSession.UploadStatus.IN_PROGRESS) {
            throw new ServiceException("上传会话状态异常: " + session.getStatus());
        }

        // 4. 验证分片号
        if (chunkNumber < 1 || chunkNumber > session.getTotalChunks()) {
            throw new ServiceException("分片号无效: " + chunkNumber);
        }

        // 5. 检查此分片是否已上传
        List<Integer> uploadedChunks = session.getUploadedChunks();
        if (uploadedChunks.contains(chunkNumber)) {
            log.info("分片已存在，跳过上传: uploadId={}, chunkNumber={}", uploadId, chunkNumber);
            return achievementConverter.toUploadSessionDTO(session);
        }

        try {
            // 6. 上传分片到MinIO
            String etag = minioService.uploadPart(
                    session.getBucketName(),
                    session.getObjectKey(),
                    uploadId,
                    chunkNumber,
                    chunkData,
                    chunkSize
            );

            // 7. 更新会话记录
            session.addUploadedChunk(chunkNumber);
            session = sessionRepository.save(session);

            log.info("分片上传成功: uploadId={}, chunkNumber={}, progress={}%",
                    uploadId, chunkNumber, session.getProgress());

            return achievementConverter.toUploadSessionDTO(session);
        } catch (ServiceException e) {
            log.error("分片上传失败: uploadId={}, chunkNumber={}", uploadId, chunkNumber, e);
            throw new ServiceException("分片上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 完成上传
     *
     * @param uploadId 上传ID
     * @param userId   用户ID
     * @return 文件信息
     */
    @Override
    public AchievementFileDTO completeUpload(String uploadId, Long userId) {
        log.info("完成分片上传: uploadId={}, userId={}", uploadId, userId);

        // 1. 查询上传会话
        FileUploadSession session = sessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new ServiceException("上传会话不存在"));

        // 2.认证权限
        if(!session.getUploadBy().equals(userId)){
            throw new ServiceException("无权限操作此上传会话");
        }

        // 3.验证状态
        if(session.getStatus() != FileUploadSession.UploadStatus.IN_PROGRESS){
            throw new ServiceException("上传会话状态异常: " + session.getStatus());
        }

        // 4.验证所有分片是否都已上传
        List<Integer> uploadedChunks = session.getUploadedChunks();
        if (uploadedChunks.size() != session.getTotalChunks()) {
            throw new ServiceException(String.format("分片未完全上传: 已上传%d/%d",
                    uploadedChunks.size(), session.getTotalChunks()));
        }

        try{
            // 5. 直接在存储端列举已上传分片并合并（兼容 MinIO 8.x / AWS S3 客户端）
            String etag = minioService.completeMultipartUploadAuto(
                    session.getBucketName(),
                    session.getObjectKey(),
                    uploadId
            );

            // 6. 检查同名文件并删除
            Optional<AchievementFile> existingFile = fileRepository
                    .findByAchievementIdAndFileName(session.getAchievementId(), session.getFileName());

            if(existingFile.isPresent()){
                AchievementFile oldFile = existingFile.get();
                log.info("检测到同名文件，执行覆盖删除: fileId={}", oldFile.getId());
                try {
                    minioService.deleteFile(BucketType.ACHIEVEMENT_FILES, oldFile.getObjectKey());
                    fileRepository.deleteById(oldFile.getId());
                } catch (Exception e) {
                    log.error("删除旧文件失败: fileId={}", oldFile.getId(), e);
                }
            }

            // 7. 保存文件记录到数据库
            String fileExtension = session.getFileName().substring(
                    session.getFileName().lastIndexOf(".") + 1);

            String fileUrl = minioUtils.getFileUrl(session.getBucketName(), session.getObjectKey());

            AchievementFile achievementFile = AchievementFile.builder()
                    .achievementId(session.getAchievementId())
                    .fileName(session.getFileName())
                    .fileSize(session.getFileSize())
                    .fileType(fileExtension)
                    .bucketName(session.getBucketName())
                    .objectKey(session.getObjectKey())
                    .minioUrl(fileUrl)
                    .uploadBy(userId)
                    .uploadAt(LocalDateTime.now())
                    .build();

            achievementFile = fileRepository.save(achievementFile);

            // 8。更新会话状态为已完成
            session.setStatus(FileUploadSession.UploadStatus.COMPLETED);
            sessionRepository.save(session);

            log.info("分片上传完成: sessionId={}, fileId={}", session.getId(), achievementFile.getId());

            return achievementConverter.fileToDTO(achievementFile);
        }catch (Exception e) {
            log.error("完成分片上传失败: uploadId={}", uploadId, e);
            // 更新会话状态为失败
            session.setStatus(FileUploadSession.UploadStatus.FAILED);
            sessionRepository.save(session);
            throw new ServiceException("完成分片上传失败了: " + e.getMessage(), e);
        }
    }

    /**
     * 取消上传
     *
     * @param uploadId 上传ID
     * @param userId   用户ID
     */
    @Override
    @Transactional
    public void cancelUpload(String uploadId, Long userId) {
        log.info("取消分片上传: uploadId={}, userId={}", uploadId, userId);

        // 1.查询上传会话
        FileUploadSession session = sessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new ServiceException("上传会话不存在"));

        // 2.验证权限
        if (!session.getUploadBy().equals(userId)) {
            throw new ServiceException("无权限操作此上传会话");
        }

        try{
            // 3. 取消MinIO分片上传
            minioService.abortMultipartUpload(
                    session.getBucketName(),
                    session.getObjectKey(),
                    uploadId
            );

            // 4.更新会话状态
            session.setStatus(FileUploadSession.UploadStatus.CANCELLED);
            sessionRepository.save(session);

            log.info("分片上传已取消: sessionId={}", session.getId());
        }catch (Exception e) {
            log.error("取消分片上传失败: uploadId={}", uploadId, e);
            throw new ServiceException("取消分片上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询上传会话
     *
     * @param uploadId 上传ID
     * @param userId   用户ID
     * @return 上传会话信息
     */
    @Override
    public UploadSessionDTO getUploadSession(String uploadId, Long userId) {
        FileUploadSession session = sessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new ServiceException("上传会话不存在"));

        // 验证权限
        if (!session.getUploadBy().equals(userId)) {
            throw new ServiceException("无权限查看此上传会话");
        }

        return achievementConverter.toUploadSessionDTO(session);
    }

    /**
     * 获取用户的所有进行中上传会话
     *
     * @param userId 用户ID
     * @return 上传会话列表
     */
    @Override
    public List<UploadSessionDTO> getUserUploadSessions(Long userId) {
        List<FileUploadSession> sessions = sessionRepository.findByUploadByAndStatus(
                userId, FileUploadSession.UploadStatus.IN_PROGRESS);

        return sessions.stream()
                .map(achievementConverter::toUploadSessionDTO)
                .collect(Collectors.toList());
    }
}
