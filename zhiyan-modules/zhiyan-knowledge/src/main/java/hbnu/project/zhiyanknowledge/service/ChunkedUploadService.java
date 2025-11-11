package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 分片上传服务接口
 * 支持大文件断点续传
 *
 * @author ErgouTree
 */
public interface ChunkedUploadService {

    /**
     * 初始化分片上传
     *
     * @param dto 初始化请求
     * @param userId 用户ID
     * @return 上传会话信息
     */
    UploadSessionDTO initiateUpload(InitiateUploadDTO dto, Long userId);

    /**
     * 上传分片
     *
     * @param uploadId 上传ID
     * @param chunkNumber 分片号
     * @param chunkData 分片数据
     * @param userId 用户ID
     * @return 上传会话信息(包含进度)
     */
    UploadSessionDTO uploadChunk(String uploadId, Integer chunkNumber,
                                 InputStream chunkData, Long chunkSize, Long userId);

    /**
     * 完成上传
     *
     * @param uploadId 上传ID
     * @param userId 用户ID
     * @return 文件信息
     */
    AchievementFileDTO completeUpload(String uploadId, Long userId);

    /**
     * 取消上传
     *
     * @param uploadId 上传ID
     * @param userId 用户ID
     */
    void cancelUpload(String uploadId, Long userId);

    /**
     * 查询上传会话
     *
     * @param uploadId 上传ID
     * @param userId 用户ID
     * @return 上传会话信息
     */
    UploadSessionDTO getUploadSession(String uploadId, Long userId);

    /**
     * 获取用户的所有进行中上传会话
     *
     * @param userId 用户ID
     * @return 上传会话列表
     */
    List<UploadSessionDTO> getUserUploadSessions(Long userId);
}
