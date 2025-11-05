package hbnu.project.zhiyanaicoze.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Coze 文件代理服务接口
 * 用于生成临时可访问的文件URL
 *
 * @author ErgouTree
 */
public interface CozeFileProxyService {

    /**
     * 为上传的文件生成临时访问URL
     *
     * @param file 文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String generateFileUrl(MultipartFile file, Long userId);

    /**
     * 为知识库文件生成临时访问URL
     *
     * @param fileId 知识库文件ID
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String generateKnowledgeFileUrl(Long fileId, Long userId);

    /**
     * 批量为文件生成临时访问URL
     *
     * @param files 文件列表
     * @param userId 用户ID
     * @return 文件访问URL列表
     */
    List<String> generateFileUrls(List<MultipartFile> files, Long userId);

    /**
     * 批量为知识库文件生成临时访问URL
     *
     * @param fileIds 知识库文件ID列表
     * @param userId 用户ID
     * @return 文件访问URL列表
     */
    List<String> generateKnowledgeFileUrls(List<Long> fileIds, Long userId);

    /**
     * 通过token获取文件信息
     *
     * @param token 访问token
     * @return 文件信息
     */
    FileInfo getFileByToken(String token);

    /**
     * 文件信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class FileInfo {
        /**
         * 文件字节数组
         */
        private byte[] fileBytes;

        /**
         * 文件名
         */
        private String fileName;

        /**
         * 内容类型
         */
        private String contentType;
    }
}

