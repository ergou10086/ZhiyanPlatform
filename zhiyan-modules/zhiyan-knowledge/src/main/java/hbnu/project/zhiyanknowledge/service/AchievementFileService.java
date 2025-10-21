package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.AchievementFileDTO;
import hbnu.project.zhiyanknowledge.model.dto.UploadFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 成果文件服务接口
 * 提供成果文件的上传、下载、删除等功能
 *
 * @author ErgouTree
 */
public interface AchievementFileService {
    /**
     * 上传成果文件
     *
     * @param file      文件
     * @param uploadDTO 上传DTO
     * @return 文件信息
     */
    AchievementFileDTO uploadFile(MultipartFile file, UploadFileDTO uploadDTO);

    /**
     * 批量上传成果文件
     *
     * @param files        文件列表
     * @param achievementId 成果ID
     * @param uploadBy      上传者ID
     * @return 文件信息列表
     */
    List<AchievementFileDTO> uploadFilesBatch(List<MultipartFile> files, Long achievementId, Long uploadBy);

    /**
     * 下载成果文件
     *
     * @param fileId 文件ID
     * @param userId 当前用户ID（用于权限验证）
     * @return 文件流
     */
    InputStream downloadFile(Long fileId, Long userId);

    /**
     * 获取文件预签名下载URL
     *
     * @param fileId       文件ID
     * @param userId       当前用户ID
     * @param expirySeconds 过期时间（秒）
     * @return 预签名URL
     */
    String getFileDownloadUrl(Long fileId, Long userId, Integer expirySeconds);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param userId 当前用户ID（用于权限验证）
     */
    void deleteFile(Long fileId, Long userId);

    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID列表
     * @param userId  当前用户ID
     */
    void deleteFiles(List<Long> fileIds, Long userId);

    /**
     * 查询成果的所有文件
     * @param achievementId 成果ID
     * @return 文件列表
     */
    List<AchievementFileDTO> getFilesByAchievementId(Long achievementId);

    /**
     * 获取文件详情
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    AchievementFileDTO getFileById(Long fileId);

    /**
     * 统计成果的文件数量
     *
     * @param achievementId 成果ID
     * @return 文件数量
     */
    long countFilesByAchievementId(Long achievementId);

    /**
     * 检查用户是否有权限访问文件
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasFilePermission(Long fileId, Long userId);
}
