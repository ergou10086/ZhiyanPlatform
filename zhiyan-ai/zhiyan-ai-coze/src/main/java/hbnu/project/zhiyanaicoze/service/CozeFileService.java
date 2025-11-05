package hbnu.project.zhiyanaicoze.service;

import hbnu.project.zhiyanaicoze.model.response.CozeFileDetailResponse;
import hbnu.project.zhiyanaicoze.model.response.CozeFileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Coze 文件服务接口
 *
 * @author ErgouTree
 */
public interface CozeFileService {

    /**
     * 上传单个文件到 Coze
     *
     * @param file 文件
     * @param userId 用户ID
     * @return 上传响应
     */
    CozeFileUploadResponse uploadFile(MultipartFile file, Long userId);

    /**
     * 批量上传文件到 Coze
     *
     * @param files 文件列表
     * @param userId 用户ID
     * @return 上传响应列表
     */
    List<CozeFileUploadResponse> uploadFiles(List<MultipartFile> files, Long userId);

    /**
     * 从知识库上传文件到 Coze
     *
     * @param fileIds 知识库文件ID列表
     * @param userId 用户ID
     * @return Coze 文件ID列表
     */
    List<String> uploadKnowledgeFiles(List<Long> fileIds, Long userId);

    /**
     * 从知识库上传文件到 Coze（返回详细信息）
     *
     * @param fileIds 知识库文件ID列表
     * @param userId 用户ID
     * @return 上传响应列表（包含文件详细信息）
     */
    List<CozeFileUploadResponse> uploadKnowledgeFilesWithDetails(List<Long> fileIds, Long userId);

    /**
     * 查询文件详情
     *
     * @param fileId Coze 文件ID
     * @return 文件详情
     */
    CozeFileDetailResponse getFileDetail(String fileId);
}
