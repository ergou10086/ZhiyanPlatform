package hbnu.project.zhiyanaicoze.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 知识库服务 Feign 客户端
 *
 * @author ErgouTree
 */
@FeignClient(name = "zhiyan-knowledge-service", path = "/api/knowledge")
public interface KnowledgeServiceClient {

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件字节数组
     */
    @GetMapping("/files/{fileId}/download")
    byte[] downloadFile(@PathVariable("fileId") Long fileId);

    /**
     * 获取文件名
     *
     * @param fileId 文件ID
     * @return 文件名
     */
    @GetMapping("/files/{fileId}/name")
    String getFileName(@PathVariable("fileId") Long fileId);

    /**
     * 获取文件内容类型
     *
     * @param fileId 文件ID
     * @return 内容类型
     */
    @GetMapping("/files/{fileId}/content-type")
    String getFileContentType(@PathVariable("fileId") Long fileId);
}
