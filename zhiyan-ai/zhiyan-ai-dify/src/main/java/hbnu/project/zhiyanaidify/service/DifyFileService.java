package hbnu.project.zhiyanaidify.service;

import hbnu.project.zhiyanaidify.model.response.DifyFileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Dify 文件服务接口
 * 处理文件上传到 Dify
 *
 * @author ErgouTree
 */
public interface DifyFileService {

    /**
     * 上传单个文件到 Dify
     *
     * @param file 文件
     * @param userId 用户 ID
     * @return 文件上传响应
     */
    DifyFileUploadResponse uploadFile(MultipartFile file, Long userId);

    /**
     * 批量上传文件到 Dify
     *
     * @param files 文件列表
     * @param userId 用户 ID
     * @return 文件上传响应列表
     */
    List<DifyFileUploadResponse> uploadFiles(List<MultipartFile> files, Long userId);

    /**
     * 从知识库文件 ID 获取文件并上传到 Dify
     *
     * @param fileIds 知识库文件 ID 列表
     * @param userId 用户 ID
     * @return Dify 文件上传响应列表
     */
    List<DifyFileUploadResponse> uploadKnowledgeFiles(List<Long> fileIds, Long userId);

    /**
     * 删除 Dify 上的文件
     *
     * @param fileId Dify 文件 ID
     * @param userId 用户 ID
     */
    void deleteFile(String fileId, Long userId);

    /**
     * 预览或下载文件
     *
     * @param fileId Dify 文件 ID
     * @param userId 用户 ID
     * @return 文件资源
     */
    Resource previewFile(String fileId, Long userId);

    /**
     * 获取文件字节内容
     *
     * @param fileId Dify 文件 ID
     * @param userId 用户 ID
     * @return 文件字节数组
     */
    byte[] getFileBytes(String fileId, Long userId);
}

