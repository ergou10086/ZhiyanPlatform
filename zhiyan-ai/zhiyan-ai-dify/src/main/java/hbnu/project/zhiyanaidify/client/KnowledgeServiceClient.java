package hbnu.project.zhiyanaidify.client;

import hbnu.project.zhiyanaidify.model.dto.FileContextDTO;
import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 知识库服务 Feign 客户端
 *
 * @author ErgouTree
 */
@FeignClient(name = "zhiyan-knowledge", configuration = hbnu.project.zhiyanaidify.config.DifyFeignConfig.class)
public interface KnowledgeServiceClient {

    /**
     * 根据文件 ID 获取文件信息
     *
     * @param fileId 文件 ID
     * @return 文件上下文
     */
    @GetMapping("/api/knowledge/files/{fileId}")
    R<FileContextDTO> getFileById(@PathVariable("fileId") Long fileId);

    /**
     * 批量获取文件信息
     *
     * @param fileIds 文件 ID 列表
     * @return 文件上下文列表
     */
    @GetMapping("/api/knowledge/files/batch")
    R<List<FileContextDTO>> getFilesByIds(@RequestParam("fileIds") List<Long> fileIds);


    /**
     * 获取文件下载 URL
     *
     * @param fileId 文件 ID
     * @param userId 用户 ID（可选）
     * @return 文件 URL
     */
    @GetMapping("/api/knowledge/files/{fileId}/url")
    R<String> getFileUrl(
            @PathVariable("fileId") Long fileId,
            @RequestParam(value = "userId", required = false) Long userId
    );
}
